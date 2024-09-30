package com.paperline.news.ui.saved

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paperline.news.data.Article
import com.paperline.news.data.NewsRepository
import com.paperline.news.ui.components.LatestRow
import com.paperline.news.ui.components.PaperlineTopBar

@Composable
fun SavedRoute(
    repository: NewsRepository,
    showMenu: Boolean,
    onMenu: () -> Unit,
    onOpenArticle: (Article) -> Unit,
) {
    val viewModel: SavedViewModel = viewModel(factory = SavedViewModel.factory(repository))
    SavedScreen(
        articles = viewModel.articles,
        showMenu = showMenu,
        onMenu = onMenu,
        onOpenArticle = onOpenArticle,
        onRemove = viewModel::remove,
    )
}

@Composable
fun SavedScreen(
    articles: List<Article>,
    showMenu: Boolean,
    onMenu: () -> Unit,
    onOpenArticle: (Article) -> Unit,
    onRemove: (Article) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PaperlineTopBar(
                title = "Saved",
                onNavigation = if (showMenu) onMenu else null,
            )
        },
    ) { padding ->
        if (articles.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Nothing saved", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Stories you save stay on this device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            ) {
                items(articles, key = { it.id }) { article ->
                    LatestRow(
                        article = article,
                        onClick = { onOpenArticle(article) },
                        trailing = {
                            IconButton(onClick = { onRemove(article) }) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove saved story")
                            }
                        },
                    )
                }
            }
        }
    }
}
