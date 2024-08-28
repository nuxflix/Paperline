package com.paperline.news.ui.interests

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.paperline.news.data.NewsRepository
import com.paperline.news.data.Topic
import com.paperline.news.data.Topics
import kotlinx.coroutines.launch

data class TopicSelection(
    val topic: Topic,
    val selected: Boolean,
)

class InterestsViewModel(
    private val repository: NewsRepository,
) : ViewModel() {
    var topics by mutableStateOf<List<TopicSelection>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            repository.followedIds.collect { ids ->
                topics = Topics.all.map { topic ->
                    TopicSelection(topic = topic, selected = topic.id in ids)
                }
            }
        }
    }

    fun toggle(id: String) {
        repository.toggleTopic(id)
    }

    companion object {
        fun factory(repository: NewsRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { InterestsViewModel(repository) }
        }
    }
}
