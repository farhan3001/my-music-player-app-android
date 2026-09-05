package com.my.music.app.viewModel

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.my.music.app.data.MusicRepository
import com.my.music.app.model.Music
import com.my.music.app.config.AppConfig.DEFAULT_SEARCH_TERM
import com.my.music.app.config.AppConfig.INITIAL_PAGE_SIZE
import com.my.music.app.config.AppConfig.LOAD_MORE_SIZE
import com.my.music.app.config.AppConfig.MAX_RESULTS
import com.my.music.app.config.AppConfig.PROGRESS_UPDATE_INTERVAL_MILLIS
import com.my.music.app.config.AppConfig.SEARCH_DEBOUNCE_MILLIS
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@Immutable
data class MusicUiState(
    val searchQuery: String = "",
    val musicList: List<Music> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val currentTrack: Music? = null,
    val isPlaying: Boolean = false,
    val isPlayerVisible: Boolean = false,
    val positionMillis: Long = 0L,
    val durationMillis: Long = 0L
)

@HiltViewModel
class MusicViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private val player: ExoPlayer =
        ExoPlayer.Builder(context).build()

    private var searchJob: Job? = null
    private var progressJob: Job? = null

    private var currentLimit = INITIAL_PAGE_SIZE

    private var hasMoreResults = true

    init {
        player.addListener(
            object : Player.Listener {

                override fun onIsPlayingChanged(
                    isPlaying: Boolean
                ) {
                    _uiState.update {
                        it.copy(
                            isPlaying = isPlaying
                        )
                    }

                    if (isPlaying) {
                        startProgressUpdates()
                    } else {
                        progressJob?.cancel()
                    }
                }

                override fun onPlaybackStateChanged(
                    playbackState: Int
                ) {
                    when (playbackState) {

                        Player.STATE_READY -> {
                            _uiState.update {
                                it.copy(
                                    durationMillis = player
                                        .duration
                                        .coerceAtLeast(0L)
                                )
                            }
                        }

                        Player.STATE_ENDED -> {
                            progressJob?.cancel()

                            _uiState.update {
                                it.copy(
                                    isPlaying = false,
                                    positionMillis = it.durationMillis
                                )
                            }
                        }

                        Player.STATE_BUFFERING -> {

                        }

                        Player.STATE_IDLE -> {
                        }
                    }
                }
            }
        )

        // Initial search
        onSearchQueryChanged(DEFAULT_SEARCH_TERM)
    }

    fun onSearchQueryChanged(query: String) {

        _uiState.update {
            it.copy(
                searchQuery = query
            )
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS.milliseconds)
            search(query)
        }
    }

    private suspend fun search(query: String) {

        if (query.isBlank()) {

            currentLimit = INITIAL_PAGE_SIZE
            hasMoreResults = true

            _uiState.update {
                it.copy(
                    musicList = emptyList(),
                    isLoading = false,
                    isLoadingMore = false,
                    errorMessage = null
                )
            }
            return
        }

        currentLimit = INITIAL_PAGE_SIZE
        hasMoreResults = true

        _uiState.update {
            it.copy(
                musicList = emptyList(),
                isLoading = true,
                isLoadingMore = false,
                errorMessage = null
            )
        }

        runCatching {
            repository.searchSongs(
                query = query,
                limit = currentLimit
            )
        }.onSuccess { results ->

            if (results.size < currentLimit) {
                hasMoreResults = false
            }

            _uiState.update {
                it.copy(
                    musicList = results,
                    isLoading = false
                )
            }

        }.onFailure {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Something went wrong"
                )
            }
        }
    }

    fun loadMoreSongs() {

        val state = _uiState.value

        if (state.isLoading) {
            return
        }

        if (state.isLoadingMore) {
            return
        }

        if (!hasMoreResults) {
            return
        }

        if (state.searchQuery.isBlank()) {
            return
        }

        if (currentLimit >= MAX_RESULTS) {
            hasMoreResults = false
            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoadingMore = true
                )
            }

            val newLimit = (
                    currentLimit + LOAD_MORE_SIZE
                    ).coerceAtMost(MAX_RESULTS)

            runCatching {

                repository.searchSongs(
                    query = state.searchQuery,
                    limit = newLimit
                )

            }.onSuccess { results ->

                val existingIds =
                    state.musicList
                        .map { it.id }
                        .toHashSet()

                val newSongs =
                    results.filter { music ->
                        music.id !in existingIds
                    }

                currentLimit = newLimit

                if (results.size < newLimit) {
                    hasMoreResults = false
                }
                if (newSongs.isEmpty()) {
                    hasMoreResults = false
                }

                _uiState.update { currentState ->

                    currentState.copy(
                        musicList =
                            currentState.musicList + newSongs,

                        isLoadingMore = false
                    )
                }

            }.onFailure {

                _uiState.update {
                    it.copy(
                        isLoadingMore = false
                    )
                }
            }
        }
    }

    fun onSongSelected(music: Music) {

        val current = _uiState.value.currentTrack

        if (current?.id == music.id) {
            togglePlayPause()
            return
        }

        _uiState.update {
            it.copy(
                currentTrack = music,
                isPlayerVisible = true,
                positionMillis = 0L,
                durationMillis = 0L
            )
        }

        player.setMediaItem(
            MediaItem.fromUri(music.previewUrl)
        )

        player.prepare()

        player.playWhenReady = true
    }

    fun togglePlayPause() {

        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun onNextTrack() {
        jumpTo(offset = 1)
    }

    fun onPreviousTrack() {
        jumpTo(offset = -1)
    }

    private fun jumpTo(offset: Int) {

        val state = _uiState.value
        val list = state.musicList

        if (list.isEmpty()) {
            return
        }

        val currentIndex =
            list.indexOfFirst {
                it.id == state.currentTrack?.id
            }

        if (currentIndex == -1) {
            return
        }

        val nextIndex = (currentIndex + offset + list.size) % list.size

        onSongSelected(list[nextIndex])
    }

    fun onSeek(positionMillis: Long) {

        player.seekTo(positionMillis)

        _uiState.update {
            it.copy(
                positionMillis = positionMillis
            )
        }
    }

    private fun startProgressUpdates() {

        progressJob?.cancel()

        progressJob = viewModelScope.launch {

            while (true) {

                _uiState.update {
                    it.copy(
                        positionMillis =
                            player.currentPosition
                                .coerceAtLeast(0L)
                    )
                }

                delay(PROGRESS_UPDATE_INTERVAL_MILLIS.milliseconds)
            }
        }
    }

    override fun onCleared() {

        searchJob?.cancel()
        progressJob?.cancel()
        player.release()
        super.onCleared()
    }
}