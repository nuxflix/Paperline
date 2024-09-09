package com.paperline.news.ui.interests

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paperline.news.data.NewsRepository
import com.paperline.news.ui.components.PaperlineTopBar

@Composable
fun InterestsRoute(
    repository: NewsRepository,
    showMenu: Boolean,
    onMenu: () -> Unit,
) {
    val viewModel: InterestsViewModel = viewModel(factory = InterestsViewModel.factory(repository))
    InterestsScreen(
        topics = viewModel.topics,
        showMenu = showMenu,
        onMenu = onMenu,
        onToggle = viewModel::toggle,
    )
}

@Composable
fun InterestsScreen(
    topics: List<TopicSelection>,
    showMenu: Boolean,
    onMenu: () -> Unit,
    onToggle: (String) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PaperlineTopBar(
                title = "Topics",
                onNavigation = if (showMenu) onMenu else null,
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(168.dp),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Follow a few topics. Home uses them as the sections you can browse. At least one stays on.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                )
            }
            items(topics, key = { it.topic.id }) { selection ->
                TopicCard(selection = selection, onClick = { onToggle(selection.topic.id) })
            }
        }
    }
}

@Composable
private fun TopicCard(
    selection: TopicSelection,
    onClick: () -> Unit,
) {
    val border = if (selection.selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    val container = if (selection.selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, border),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            if (selection.selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Following",
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
            }
            Text(selection.topic.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                text = selection.topic.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
