package com.paperline.news.ui.article

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
import com.paperline.news.data.toNewsMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class ArticleUiState(
    val article: Article? = null,
    val loading: Boolean = true,
    val error: String? = null,
    val bookmarked: Boolean = false,
)

class ArticleViewModel(
    private val repository: NewsRepository,
) : ViewModel() {
    var ui by mutableStateOf(ArticleUiState())
        private set

    private var loadJob: Job? = null

    fun open(url: String) {
        if (url.isBlank()) return
        loadJob?.cancel()
        val seed = repository.peekByUrl(url)
        ui = ArticleUiState(
            article = seed,
            loading = true,
            bookmarked = seed?.let { repository.isBookmarked(it.id) } == true,
        )
        loadJob = viewModelScope.launch {
            try {
                val full = repository.article(url)
                ui = ArticleUiState(
                    article = full,
                    loading = false,
                    bookmarked = repository.isBookmarked(full.id),
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                val fallback = ui.article ?: repository.peekByUrl(url)
                ui = ArticleUiState(
                    article = fallback,
                    loading = false,
                    error = error.toNewsMessage(),
                    bookmarked = fallback?.let { repository.isBookmarked(it.id) } == true,
                )
            }
        }
    }

    fun toggleBookmark() {
        val article = ui.article ?: return
        val saved = repository.toggleBookmark(article)
        ui = ui.copy(bookmarked = saved)
    }

    companion object {
        fun factory(repository: NewsRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { ArticleViewModel(repository) }
        }
    }
}
