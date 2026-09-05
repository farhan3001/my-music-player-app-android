package com.my.music.app.page.music

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.my.music.app.page.theme.MusicAppTheme
import com.my.music.app.viewModel.MusicViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun MusicPage(
    viewModel: MusicViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo

            val lastVisibleItemIndex =
                layoutInfo.visibleItemsInfo.lastOrNull() ?.index ?: 0

            val totalItemsCount =
                layoutInfo.totalItemsCount

            lastVisibleItemIndex >= totalItemsCount - 3
        }
            .distinctUntilChanged().collect { shouldLoadMore ->
                if (shouldLoadMore) {
                    viewModel.loadMoreSongs()
                }
            }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // Search
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = {
                    Text("Search songs...")
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor =  MaterialTheme.colorScheme.outline
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Music List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {

                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color =  MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    uiState.errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    uiState.musicList.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No results found")
                        }
                    }

                    else -> {

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = PaddingValues(
                                bottom = 16.dp
                            )
                        ) {

                            items(
                                items = uiState.musicList,
                                key = { it.id }
                            ) { music ->

                                MusicListItem(
                                    music = music,
                                    isCurrentTrack =
                                        uiState.currentTrack?.id == music.id,
                                    onClick =
                                        viewModel::onSongSelected
                                )
                            }

                            if (uiState.isLoadingMore) {

                                item {

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment =
                                            Alignment.Center
                                    ) {

                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Player
            AnimatedVisibility(
                visible =
                    uiState.isPlayerVisible && uiState.currentTrack != null,
                enter =
                    slideInVertically { fullHeight ->
                        fullHeight
                    },
                exit =
                    slideOutVertically { fullHeight ->
                        fullHeight
                    }
            ) {
                val track = uiState.currentTrack
                if (track != null) {
                    MusicPlayerDrawer(
                        isPlaying = uiState.isPlaying,
                        positionMillis = uiState.positionMillis,
                        durationMillis = uiState.durationMillis,
                        onPlayPauseClick =
                            viewModel::togglePlayPause,
                        onNextClick =
                            viewModel::onNextTrack,
                        onPreviousClick =
                            viewModel::onPreviousTrack,
                        onSeek =
                            viewModel::onSeek
                    )
                }
            }
        }
    }
}