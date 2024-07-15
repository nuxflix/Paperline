package com.paperline.news.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.paperline.news.data.Article
import com.paperline.news.data.NewsRepository
import com.paperline.news.data.Topic
import com.paperline.news.data.Topics
import com.paperline.news.data.dedupeArticles
import com.paperline.news.data.toNewsMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class HomeUiState(
    val topics: List<Topic> = Topics.all.filter { it.id in Topics.DEFAULT_IDS },
    val selectedTopicId: String = "world",
    val query: String = "",
    val featured: Article? = null,
    val topStories: List<Article> = emptyList(),
    val latest: List<Article> = emptyList(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val endReached: Boolean = false,
    val error: String? = null,
    val staleMessage: String? = null,
)

class HomeViewModel(
    private val repository: NewsRepository,
) : ViewModel() {
    var ui by mutableStateOf(HomeUiState())
        private set

    private var items: List<Article> = emptyList()
    private var topicId: String = "world"
    private var query: String = ""
    private var followed: Set<String> = Topics.DEFAULT_IDS
    private var offset: Int = 0
    private var loadedKey: String? = null
    private var started: Boolean = false
    private var loadJob: Job? = null
    private var moreJob: Job? = null
    private var queryJob: Job? = null
    private var requestId: Int = 0

    init {
        viewModelScope.launch {
            repository.followedIds.collect { ids ->
                val previous = topicId
                followed = ids
                if (topicId !in ids) {
                    topicId = Topics.all.firstOrNull { it.id in ids }?.id ?: Topics.all.first().id
                }
                val topicChanged = topicId != previous
                if (!started || topicChanged) {
                    started = true
                    reload(fromUser = false)
                } else {
                    publish(
                        loading = ui.loading,
                        refreshing = ui.refreshing,
                        loadingMore = ui.loadingMore,
                        endReached = ui.endReached,
                        error = ui.error,
                        staleMessage = ui.staleMessage,
                    )
                }
            }
        }
    }

    fun selectTopic(id: String) {
        if (id == topicId && query.isBlank()) return
        queryJob?.cancel()
        topicId = id
        query = ""
        reload(fromUser = false)
    }

    fun onQueryChange(value: String) {
        query = value.take(MAX_QUERY)
        ui = ui.copy(query = query)
        queryJob?.cancel()
        queryJob = viewModelScope.launch {
            delay(SEARCH_DELAY_MS)
            reload(fromUser = false)
        }
    }

    fun refresh() = reload(fromUser = true)

    fun loadMore() {
        if (ui.loading || ui.loadingMore || ui.refreshing || ui.endReached || ui.error != null) return
        val topic = Topics.byId(topicId)
        val requested = query.trim()
        val request = requestId
        ui = ui.copy(loadingMore = true)
        moreJob?.cancel()
        moreJob = viewModelScope.launch {
            try {
                val page = repository.headlines(topic, requested, offset)
                if (request != requestId) return@launch
                val merged = (items + page).dedupeArticles()
                if (merged.size == items.size) {
                    publish(
                        loading = false,
                        refreshing = false,
                        loadingMore = false,
                        endReached = true,
                        error = null,
                        staleMessage = null,
                    )
                    return@launch
                }
                items = merged
                offset += page.size
                publish(
                    loading = false,
                    refreshing = false,
                    loadingMore = false,
                    endReached = page.size < PAGE,
                    error = null,
                    staleMessage = null,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                publish(
                    loading = false,
                    refreshing = false,
                    loadingMore = false,
                    endReached = false,
                    error = error.toNewsMessage(),
                    staleMessage = null,
                )
            }
        }
    }

    private fun reload(fromUser: Boolean) {
        queryJob?.cancel()
        moreJob?.cancel()
        loadJob?.cancel()
        val request = ++requestId
        val topic = Topics.byId(topicId)
        val requested = query.trim()
        val key = "${topic.id}|$requested"
        loadJob = viewModelScope.launch {
            if (loadedKey != key) {
                items = emptyList()
                offset = 0
                loadedKey = null
            }
            if (items.isEmpty()) {
                val cached = repository.cachedHeadlines(topic, requested)
                if (request != requestId) return@launch
                if (!cached.isNullOrEmpty()) {
                    items = cached.dedupeArticles()
                    offset = items.size
                    publish(
                        loading = true,
                        refreshing = false,
                        endReached = false,
                        error = null,
                        staleMessage = null,
                    )
                } else {
                    publish(
                        loading = true,
                        refreshing = false,
                        endReached = false,
                        error = null,
                        staleMessage = null,
                    )
                }
            } else if (fromUser) {
                publish(
                    loading = false,
                    refreshing = true,
                    endReached = false,
                    error = null,
                    staleMessage = null,
                )
            }
            try {
                val page = repository.headlines(topic, requested, offset = 0)
                if (request != requestId) return@launch
                items = page.dedupeArticles()
                offset = page.size
                loadedKey = key
                publish(
                    loading = false,
                    refreshing = false,
                    endReached = page.size < PAGE,
                    error = null,
                    staleMessage = null,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (request != requestId) return@launch
                val message = error.toNewsMessage()
                if (items.isNotEmpty()) {
                    loadedKey = key
                    publish(
                        loading = false,
                        refreshing = false,
                        endReached = true,
                        error = null,
                        staleMessage = "Couldn't refresh. Showing the last saved headlines.",
                    )
                } else {
                    publish(
                        loading = false,
                        refreshing = false,
                        endReached = true,
                        error = message,
                        staleMessage = null,
                    )
                }
            }
        }
    }

    private fun publish(
        loading: Boolean,
        refreshing: Boolean,
        loadingMore: Boolean = false,
        endReached: Boolean,
        error: String?,
        staleMessage: String?,
    ) {
        val searching = query.isNotBlank()
        val featured = if (searching) null else items.firstOrNull()
        val rest = if (featured == null) items else items.drop(1)
        ui = HomeUiState(
            topics = Topics.all.filter { it.id in followed },
            selectedTopicId = topicId,
            query = query,
            featured = featured,
            topStories = if (searching) emptyList() else rest.take(TOP_STORIES),
            latest = if (searching) items else rest.drop(TOP_STORIES),
            loading = loading,
            refreshing = refreshing,
            loadingMore = loadingMore,
            endReached = endReached,
            error = error,
            staleMessage = staleMessage,
        )
    }

    companion object {
        private const val PAGE = 20
        private const val TOP_STORIES = 6
        private const val MAX_QUERY = 80
        private const val SEARCH_DELAY_MS = 350L

        fun factory(repository: NewsRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(repository) }
        }
    }
}
