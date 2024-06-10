package com.paperline.news.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class NewsRepository(
    private val api: NewsApi,
    private val preferences: UserPreferences,
    private val feedCache: FeedCache,
) {
    private val memory = LinkedHashMap<String, Article>()

    private val _bookmarks = MutableStateFlow(preferences.bookmarks())
    val bookmarks: StateFlow<List<Article>> = _bookmarks.asStateFlow()

    private val _followedIds = MutableStateFlow(preferences.followedIds())
    val followedIds: StateFlow<Set<String>> = _followedIds.asStateFlow()

    init {
        _bookmarks.value.forEach { memory[it.id] = it }
    }

    suspend fun headlines(topic: Topic, query: String, offset: Int): List<Article> {
        val trimmed = query.trim()
        val page = when {
            trimmed.isNotEmpty() -> api.search(
                query = trimmed,
                date = "7d",
                sort = "relevance",
                size = PAGE,
                offset = offset,
            )
            topic.query == null -> {
                val latest = api.search(
                    query = null,
                    date = "24h",
                    sort = "date",
                    size = PAGE,
                    offset = offset,
                )
                if (latest.isEmpty() && offset == 0) {
                    api.search(query = null, date = "7d", sort = "date", size = PAGE, offset = 0)
                } else {
                    latest
                }
            }
            else -> api.search(
                query = topic.query,
                date = "7d",
                sort = "date",
                size = PAGE,
                offset = offset,
            )
        }
        page.forEach { memory[it.id] = it }
        if (offset == 0 && page.isNotEmpty()) {
            val key = cacheKey(topic, trimmed)
            withContext(Dispatchers.IO) { feedCache.write(key, page) }
        }
        return page
    }

    suspend fun cachedHeadlines(topic: Topic, query: String): List<Article>? {
        val key = cacheKey(topic, query.trim())
        val cached = withContext(Dispatchers.IO) { feedCache.read(key) } ?: return null
        cached.forEach { memory[it.id] = it }
        return cached
    }

    suspend fun article(url: String): Article {
        val full = api.article(url)
        memory[full.id] = full
        if (isBookmarked(full.id)) updateBookmark(full)
        return full
    }

    fun peekByUrl(url: String): Article? {
        memory.values.firstOrNull { it.url == url }?.let { return it }
        return _bookmarks.value.firstOrNull { it.url == url }
    }

    fun isBookmarked(id: String): Boolean = _bookmarks.value.any { it.id == id }

    fun toggleBookmark(article: Article): Boolean {
        val saved = isBookmarked(article.id)
        val next = if (saved) {
            _bookmarks.value.filterNot { it.id == article.id }
        } else {
            (listOf(article) + _bookmarks.value.filterNot { it.id == article.id }).take(MAX_BOOKMARKS)
        }
        _bookmarks.value = next
        preferences.setBookmarks(next)
        memory[article.id] = article
        return !saved
    }

    fun updateBookmark(article: Article) {
        if (!isBookmarked(article.id)) return
        val next = _bookmarks.value.map { if (it.id == article.id) article else it }
        _bookmarks.value = next
        preferences.setBookmarks(next)
        memory[article.id] = article
    }

    fun toggleTopic(id: String) {
        if (Topics.all.none { it.id == id }) return
        val current = _followedIds.value.toMutableSet()
        if (id in current) {
            if (current.size == 1) return
            current.remove(id)
        } else {
            current.add(id)
        }
        _followedIds.value = current
        preferences.setFollowedIds(current)
    }

    private fun cacheKey(topic: Topic, query: String): String =
        if (query.isBlank()) topic.id else "search:$query"

    private companion object {
        const val PAGE = 20
        const val MAX_BOOKMARKS = 100
    }
}
