package com.paperline.news.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paperline.news.data.Article
import com.paperline.news.data.NewsRepository
import com.paperline.news.ui.components.FeaturedStory
import com.paperline.news.ui.components.LatestRow
import com.paperline.news.ui.components.PaperlineTopBar
import com.paperline.news.ui.components.TopStoryCard

@Composable
fun HomeRoute(
    repository: NewsRepository,
    showMenu: Boolean,
    onMenu: () -> Unit,
    onOpenArticle: (Article) -> Unit,
) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(repository))
    HomeScreen(
        state = viewModel.ui,
        showMenu = showMenu,
        onMenu = onMenu,
        onSelectTopic = viewModel::selectTopic,
        onQueryChange = viewModel::onQueryChange,
        onRefresh = viewModel::refresh,
        onLoadMore = viewModel::loadMore,
        onOpenArticle = onOpenArticle,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    showMenu: Boolean,
    onMenu: () -> Unit,
    onSelectTopic: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenArticle: (Article) -> Unit,
) {
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(searchOpen) {
        if (searchOpen) focusRequester.requestFocus()
    }
    val listState = rememberLazyListState()
    LaunchedEffect(listState, state.latest.size, state.topStories.size, state.endReached) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 2
        }.collect { nearEnd ->
            if (nearEnd) onLoadMore()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (searchOpen) {
                SearchTopBar(
                    query = state.query,
                    showMenu = showMenu,
                    onMenu = onMenu,
                    onQueryChange = onQueryChange,
                    onClose = {
                        searchOpen = false
                        onQueryChange("")
                    },
                    focusRequester = focusRequester,
                )
            } else {
                PaperlineTopBar(
                    title = "Paperline",
                    onNavigation = if (showMenu) onMenu else null,
                    actions = {
                        IconButton(onClick = { searchOpen = true }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search headlines")
                        }
                    },
                )
            }
        },
    ) { padding ->
        val empty = state.featured == null && state.latest.isEmpty() && state.topStories.isEmpty()
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when {
                state.loading && empty -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                state.error != null && empty -> {
                    MessagePane(
                        title = "No headlines yet",
                        body = state.error,
                        action = "Try again",
                        onAction = onRefresh,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                empty && !state.loading -> {
                    MessagePane(
                        title = if (state.query.isNotBlank()) "No matches" else "Nothing new",
                        body = if (state.query.isNotBlank()) {
                            "No stories match that search."
                        } else {
                            "No stories in this topic right now."
                        },
                        action = "Refresh",
                        onAction = onRefresh,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = state.refreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 28.dp),
                        ) {
                            item {
                                TopicChips(
                                    state = state,
                                    onSelectTopic = onSelectTopic,
                                )
                            }
                            if (state.staleMessage != null || (state.error != null && !empty)) {
                                item {
                                    Column(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                                        state.staleMessage?.let { message ->
                                            Text(
                                                text = message,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        if (state.error != null) {
                                            Text(
                                                text = state.error,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                            TextButton(onClick = onRefresh) { Text("Try again") }
                                        }
                                    }
                                }
                            }
                            state.featured?.let { featured ->
                                item(key = "featured-${featured.id}") {
                                    FeaturedStory(featured, onClick = { onOpenArticle(featured) })
                                }
                            }
                            if (state.topStories.isNotEmpty()) {
                                item { SectionLabel("TOP STORIES") }
                                item {
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        items(state.topStories, key = { it.id }) { article ->
                                            TopStoryCard(article, onClick = { onOpenArticle(article) })
                                        }
                                    }
                                }
                            }
                            if (state.latest.isNotEmpty()) {
                                item {
                                    SectionLabel(if (state.query.isNotBlank()) "RESULTS" else "LATEST")
                                }
                                items(state.latest, key = { it.id }) { article ->
                                    LatestRow(article, onClick = { onOpenArticle(article) })
                                }
                            }
                            if (state.loadingMore) {
                                item {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchTopBar(
    query: String,
    showMenu: Boolean,
    onMenu: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    focusRequester: FocusRequester,
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search headlines") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
        },
        navigationIcon = {
            if (showMenu) {
                IconButton(onClick = onMenu) {
                    Icon(Icons.Filled.Menu, contentDescription = "Open navigation")
                }
            }
        },
        actions = {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Close search")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

@Composable
private fun TopicChips(
    state: HomeUiState,
    onSelectTopic: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.topics, key = { it.id }) { topic ->
            FilterChip(
                selected = topic.id == state.selectedTopicId,
                onClick = { onSelectTopic(topic.id) },
                label = { Text(topic.title) },
                shape = RoundedCornerShape(2.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
            .semantics { heading() },
    )
}

@Composable
private fun MessagePane(
    title: String,
    body: String,
    action: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAction, shape = RoundedCornerShape(2.dp)) { Text(action) }
    }
}
