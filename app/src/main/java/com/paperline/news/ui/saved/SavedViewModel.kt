package com.paperline.news.ui.saved

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
import kotlinx.coroutines.launch

class SavedViewModel(
    private val repository: NewsRepository,
) : ViewModel() {
    var articles by mutableStateOf<List<Article>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            repository.bookmarks.collect { articles = it }
        }
    }

    fun remove(article: Article) {
        if (repository.isBookmarked(article.id)) {
            repository.toggleBookmark(article)
        }
    }

    companion object {
        fun factory(repository: NewsRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { SavedViewModel(repository) }
        }
    }
}
