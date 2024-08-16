package com.paperline.news.ui.article

import android.content.Intent
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paperline.news.data.Article
import com.paperline.news.data.ArticleText
import com.paperline.news.data.NewsRepository
import com.paperline.news.ui.components.PaperlineTopBar
import com.paperline.news.ui.components.StoryImage

@Composable
fun ArticleRoute(
    repository: NewsRepository,
    url: String,
    onBack: () -> Unit,
) {
    val viewModel: ArticleViewModel = viewModel(factory = ArticleViewModel.factory(repository))
    LaunchedEffect(url) { viewModel.open(url) }
    ArticleScreen(
        state = viewModel.ui,
        onBack = onBack,
        onBookmark = viewModel::toggleBookmark,
    )
}

@Composable
fun ArticleScreen(
    state: ArticleUiState,
    onBack: () -> Unit,
    onBookmark: () -> Unit,
) {
    val context = LocalContext.current
    val article = state.article
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PaperlineTopBar(
                title = "Paperline",
                onNavigation = onBack,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationLabel = "Go back",
            )
        },
        bottomBar = {
            if (article != null) {
                BottomAppBar(containerColor = MaterialTheme.colorScheme.surface) {
                    TextButton(onClick = onBookmark) {
                        Text(
                            text = if (state.bookmarked) "Saved" else "Save",
                            color = if (state.bookmarked) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                    TextButton(
                        onClick = { share(context, article) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text("Share")
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(
                        onClick = { openOriginal(context, article) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("Original")
                    }
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when {
                article == null && state.loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                article == null -> {
                    Text(
                        text = state.error ?: "This story isn't available.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                else -> {
                    ArticleBody(article = article, loading = state.loading, error = state.error)
                }
            }
        }
    }
}

@Composable
private fun ArticleBody(
    article: Article,
    loading: Boolean,
    error: String?,
) {
    val paragraphs = ArticleText.paragraphs(article.body, article.description)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (!article.image.isNullOrBlank()) {
            StoryImage(
                url = article.image,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
            )
        }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Text(
                text = article.publisher.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() },
            )
            val time = ArticleText.publishedLabel(article.publishedAt)
            val byline = listOfNotNull(
                article.author?.let { "By $it" },
                time.takeIf { it.isNotBlank() },
            ).joinToString(" · ")
            if (byline.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = byline,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .width(40.dp)
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )
            if (error != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(16.dp))
            SelectionContainer {
                Column {
                    if (paragraphs.isEmpty()) {
                        Text(
                            text = "This story didn't include a full text. Open the original to read it at the publisher.",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    } else {
                        paragraphs.forEach { paragraph ->
                            Text(paragraph, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(14.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "The publisher's page is the source of this story.",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun share(context: Context, article: Article) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, article.title)
        putExtra(Intent.EXTRA_TEXT, "${article.title}\n${article.url}")
    }
    context.startActivity(Intent.createChooser(intent, "Share story"))
}

private fun openOriginal(context: Context, article: Article) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.url)))
    }
}
