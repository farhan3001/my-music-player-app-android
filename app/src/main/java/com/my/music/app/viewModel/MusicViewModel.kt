package com.my.music.app.viewModel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.music.app.config.AppConfig.DEFAULT_SEARCH_TERM
import com.my.music.app.config.AppConfig.INITIAL_PAGE_SIZE
import com.my.music.app.config.AppConfig.LOAD_MORE_SIZE
import com.my.music.app.config.AppConfig.MAX_RESULTS
import com.my.music.app.config.AppConfig.MIN_MILLIS_RESTART_MEDIA
import com.my.music.app.config.AppConfig.PROGRESS_UPDATE_INTERVAL_MILLIS
import com.my.music.app.config.AppConfig.SEARCH_DEBOUNCE_MILLIS
import com.my.music.app.data.MusicRepository
import com.my.music.app.data.player.PlayerController
import com.my.music.app.data.player.PlayerListener
import com.my.music.app.data.player.PlayerPlaybackState
import com.my.music.app.model.Music
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val repository: MusicRepository,
    private val player: PlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var progressJob: Job? = null

    private var currentLimit = INITIAL_PAGE_SIZE
    private var hasMoreResults = true

    init {
        player.addListener(object : PlayerListener {

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressUpdates()
                } else {
                    progressJob?.cancel()
                }
            }

            override fun onPlaybackStateChanged(state: PlayerPlaybackState) {
                when (state) {
                    PlayerPlaybackState.READY -> _uiState.update {
                        it.copy(durationMillis = player.duration.coerceAtLeast(0L))
                    }
                    PlayerPlaybackState.ENDED -> {
                        progressJob?.cancel()
                        _uiState.update {
                            it.copy(isPlaying = false, positionMillis = it.durationMillis)
                        }
                    }
                    PlayerPlaybackState.BUFFERING, PlayerPlaybackState.IDLE -> Unit
                }
            }
        })

        onSearchQueryChanged(DEFAULT_SEARCH_TERM)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
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
            repository.searchSongs(query = query, limit = currentLimit)
        }.onSuccess { results ->
            if (results.size < currentLimit) hasMoreResults = false
            _uiState.update { it.copy(musicList = results, isLoading = false) }
        }.onFailure {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Something went wrong") }
        }
    }

    fun loadMoreSongs() {
        val state = _uiState.value

        when {
            state.isLoading -> return
            state.isLoadingMore -> return
            !hasMoreResults -> return
            state.searchQuery.isBlank() -> return
            currentLimit >= MAX_RESULTS -> {
                hasMoreResults = false
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            val newLimit = (currentLimit + LOAD_MORE_SIZE).coerceAtMost(MAX_RESULTS)

            runCatching {
                repository.searchSongs(query = state.searchQuery, limit = newLimit)
            }.onSuccess { results ->

                val newSongs = results.filter { it.id !in state.musicList.map { it.id }.toHashSet() }
                currentLimit = newLimit

                if (results.size < newLimit) { hasMoreResults = false }
                if (newSongs.isEmpty()) { hasMoreResults = false }

                _uiState.update { currentState ->
                    currentState.copy(
                        musicList = currentState.musicList + newSongs,
                        isLoadingMore = false
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun onSongSelected(music: Music) {
        if (_uiState.value.currentTrack?.id == music.id) {
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

        player.setMediaItem(music.previewUrl)
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun onNextTrack() = jumpTo(offset = 1)

    fun onPreviousTrack() {
        if(_uiState.value.positionMillis < MIN_MILLIS_RESTART_MEDIA) {
            jumpTo(offset = -1)
        } else {
            toStart()
        }
    }

    fun toStart() = onSeek(0L)

    private fun jumpTo(offset: Int) {
        val list =  _uiState.value.musicList
        if (list.isEmpty()) return

        val currentIndex = list.indexOfFirst { it.id == _uiState.value.currentTrack?.id }
        if (currentIndex == -1) return

        val nextIndex = (currentIndex + offset + list.size) % list.size
        onSongSelected(list[nextIndex])
    }

    fun onSeek(positionMillis: Long) {
        player.seekTo(positionMillis)
        _uiState.update { it.copy(positionMillis = positionMillis) }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                _uiState.update {
                    it.copy(positionMillis = player.currentPosition.coerceAtLeast(0L))
                }
                delay(PROGRESS_UPDATE_INTERVAL_MILLIS.milliseconds)
            }
        }
    }

    public override fun onCleared() {
        searchJob?.cancel()
        progressJob?.cancel()
        player.release()
        super.onCleared()
    }
}