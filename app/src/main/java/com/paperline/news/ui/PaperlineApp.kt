package com.paperline.news.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import com.paperline.news.data.Article
import com.paperline.news.data.NewsRepository
import com.paperline.news.ui.article.ArticleRoute
import com.paperline.news.ui.home.HomeRoute
import com.paperline.news.ui.interests.InterestsRoute
import com.paperline.news.ui.saved.SavedRoute
import kotlinx.coroutines.launch

@Composable
fun PaperlineApp(repository: NewsRepository) {
    val expanded = LocalConfiguration.current.screenWidthDp >= 840
    var destination by rememberSaveable { mutableStateOf(HOME) }
    var articleUrl by rememberSaveable { mutableStateOf("") }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun openArticle(article: Article) {
        articleUrl = article.url
    }

    fun closeArticle() {
        articleUrl = ""
    }

    fun go(next: String) {
        destination = next
        if (!expanded) articleUrl = ""
        scope.launch { drawerState.close() }
    }

    BackHandler(enabled = articleUrl.isNotEmpty() || destination != HOME) {
        if (articleUrl.isNotEmpty()) closeArticle() else destination = HOME
    }

    if (expanded) {
        Row(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            PaperlineRail(destination = destination, onDestination = ::go)
            Row(Modifier.weight(1f)) {
                Box(
                    Modifier
                        .weight(0.42f)
                        .fillMaxHeight(),
                ) {
                    Section(
                        destination = destination,
                        repository = repository,
                        showMenu = false,
                        onMenu = {},
                        onOpenArticle = ::openArticle,
                    )
                }
                VerticalDivider(color = MaterialTheme.colorScheme.outline)
                Box(
                    Modifier
                        .weight(0.58f)
                        .fillMaxHeight(),
                ) {
                    if (articleUrl.isNotEmpty()) {
                        ArticleRoute(
                            repository = repository,
                            url = articleUrl,
                            onBack = ::closeArticle,
                        )
                    } else {
                        ArticlePlaceholder()
                    }
                }
            }
        }
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surface) {
                    DrawerHeader()
                    DrawerDestination(
                        label = "Home",
                        icon = Icons.Filled.Home,
                        selected = destination == HOME,
                        onClick = { go(HOME) },
                    )
                    DrawerDestination(
                        label = "Saved",
                        icon = Icons.Filled.Bookmarks,
                        selected = destination == SAVED,
                        onClick = { go(SAVED) },
                    )
                    DrawerDestination(
                        label = "Topics",
                        icon = Icons.Filled.Tune,
                        selected = destination == TOPICS,
                        onClick = { go(TOPICS) },
                    )
                }
            },
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                if (articleUrl.isNotEmpty()) {
                    ArticleRoute(
                        repository = repository,
                        url = articleUrl,
                        onBack = ::closeArticle,
                    )
                } else {
                    Section(
                        destination = destination,
                        repository = repository,
                        showMenu = true,
                        onMenu = { scope.launch { drawerState.open() } },
                        onOpenArticle = ::openArticle,
                    )
                }
            }
        }
    }
}

@Composable
private fun Section(
    destination: String,
    repository: NewsRepository,
    showMenu: Boolean,
    onMenu: () -> Unit,
    onOpenArticle: (Article) -> Unit,
) {
    when (destination) {
        SAVED -> SavedRoute(
            repository = repository,
            showMenu = showMenu,
            onMenu = onMenu,
            onOpenArticle = onOpenArticle,
        )
        TOPICS -> InterestsRoute(
            repository = repository,
            showMenu = showMenu,
            onMenu = onMenu,
        )
        else -> HomeRoute(
            repository = repository,
            showMenu = showMenu,
            onMenu = onMenu,
            onOpenArticle = onOpenArticle,
        )
    }
}

@Composable
private fun PaperlineRail(
    destination: String,
    onDestination: (String) -> Unit,
) {
    NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
        Spacer(Modifier.height(12.dp))
        RailItem(HOME, "Home", Icons.Filled.Home, destination, onDestination)
        RailItem(SAVED, "Saved", Icons.Filled.Bookmarks, destination, onDestination)
        RailItem(TOPICS, "Topics", Icons.Filled.Tune, destination, onDestination)
    }
}

@Composable
private fun RailItem(
    id: String,
    label: String,
    icon: ImageVector,
    destination: String,
    onDestination: (String) -> Unit,
) {
    NavigationRailItem(
        selected = destination == id,
        onClick = { onDestination(id) },
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
    )
}

@Composable
private fun DrawerHeader() {
    Column(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 28.dp, vertical = 20.dp),
    ) {
        Text(
            text = "Paperline",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Stories from the open web",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DrawerDestination(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
    )
}

@Composable
private fun ArticlePlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("Paperline", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Select a story to read it here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private const val HOME = "home"
private const val SAVED = "saved"
private const val TOPICS = "topics"
