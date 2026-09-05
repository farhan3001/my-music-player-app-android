package com.my.music.app.viewModel

import com.my.music.app.MainDispatcherRule
import com.my.music.app.config.AppConfig.MIN_MILLIS_RESTART_MEDIA
import com.my.music.app.config.AppConfig.SEARCH_DEBOUNCE_MILLIS
import com.my.music.app.model.Music
import com.my.music.app.predeterminedClassesObject.FakeMusicRepository
import com.my.music.app.predeterminedClassesObject.FakePlayerController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class MusicViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeMusicRepository
    private lateinit var player: FakePlayerController
    private lateinit var viewModel: MusicViewModel

    private fun createViewModel(): MusicViewModel {
        repository = FakeMusicRepository()
        player = FakePlayerController()
        return MusicViewModel(repository, player)
    }

    private fun song(
        id: Long,
        name: String = "Song $id"
    ) = Music(
        id = id,
        trackName = name,
        artistName = "Artist $id",
        artworkUrl = "art$id.jpg",
        previewUrl = "preview$id.mp3",
        trackTimeMillis = 200_000L
    )

    @Test
    fun initialStateIsEmptyWhenDefaultQueryIsBlank() = runTest {
        viewModel = createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state.musicList.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun rapidQueryChangesOnlySearchFinalQuery() = runTest {
        viewModel = createViewModel()
        repository.songsToReturn = listOf(song(9))

        viewModel.onSearchQueryChanged("dr")
        advanceTimeBy(100.milliseconds)

        viewModel.onSearchQueryChanged("dra")
        advanceTimeBy(100.milliseconds)

        viewModel.onSearchQueryChanged("drake")

        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS.milliseconds)
        advanceUntilIdle()

        assertEquals(listOf(song(9)), viewModel.uiState.value.musicList)
        assertEquals(1, repository.requestedLimits.size)
    }

    @Test
    fun blankQueryClearsResultsWithoutCallingRepository() = runTest {
        viewModel = createViewModel()

        val callsBefore = repository.requestedLimits.size

        viewModel.onSearchQueryChanged("")

        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS.milliseconds)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.musicList.isEmpty())
        assertEquals(callsBefore, repository.requestedLimits.size)
    }

    @Test
    fun searchSuccessUpdatesMusicList() = runTest {
        viewModel = createViewModel()

        repository.songsToReturn = listOf(song(1), song(2))

        viewModel.onSearchQueryChanged("weeknd")

        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS.milliseconds)
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.musicList.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun searchFailureShowsError() = runTest {
        viewModel = createViewModel()

        repository.shouldThrow = true

        viewModel.onSearchQueryChanged("weeknd")

        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS.milliseconds)
        advanceUntilIdle()

        assertEquals("Something went wrong", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun loadMoreSongsAppendsNewSongs() = runTest {

        viewModel = createViewModel()
        repository.songsToReturn = (1L..20L).map { song(it) }

        viewModel.onSearchQueryChanged("weeknd")

        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS.milliseconds)
        advanceUntilIdle()

        repository.songsToReturn = (1L..40L).map { song(it) }

        viewModel.loadMoreSongs()

        advanceUntilIdle()

        assertEquals(40, viewModel.uiState.value.musicList.size)
        assertFalse(viewModel.uiState.value.isLoadingMore)
    }

    @Test
    fun selectingSongStartsPlayback() = runTest {
        viewModel = createViewModel()

        val target = song(1)
        viewModel.onSongSelected(target)

        val state = viewModel.uiState.value

        assertEquals(target, state.currentTrack)
        assertTrue(state.isPlayerVisible)
        assertTrue(state.isPlaying)
        assertEquals(target.previewUrl, player.lastUri)

        player.pause()
    }

    @Test
    fun selectingCurrentTrackTogglesPause() = runTest {
        viewModel = createViewModel()
        val target = song(1)

        viewModel.onSongSelected(target)
        viewModel.onSongSelected(target)

        assertFalse(viewModel.uiState.value.isPlaying)
    }

    @Test
    fun nextTrackWrapsToFirstSong() = runTest {
        viewModel = createViewModel()

        val songs = listOf(song(1), song(2), song(3))

        repository.songsToReturn = songs

        viewModel.onSearchQueryChanged("weeknd")

        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS.milliseconds)
        advanceUntilIdle()

        viewModel.onSongSelected(songs.last())
        viewModel.onNextTrack()

        assertEquals(songs.first(), viewModel.uiState.value.currentTrack)

        player.pause()
    }

    @Test
    fun previousTrackMovesToPreviousSongWhenNearBeginning() = runTest {
        viewModel = createViewModel()

        val songs = listOf(song(1), song(2), song(3))

        repository.songsToReturn = songs
        viewModel.onSearchQueryChanged("weeknd")

        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS.milliseconds)
        advanceUntilIdle()

        viewModel.onSongSelected(songs[1])
        viewModel.onSeek(MIN_MILLIS_RESTART_MEDIA - 1)
        viewModel.onPreviousTrack()

        assertEquals(songs[0], viewModel.uiState.value.currentTrack)

        player.pause()
    }

    @Test
    fun previousTrackWrapsToLastSong() = runTest {
        viewModel = createViewModel()

        val songs = listOf(song(1), song(2), song(3))

        repository.songsToReturn = songs

        viewModel.onSearchQueryChanged("weeknd")

        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS.milliseconds)
        advanceUntilIdle()

        viewModel.onSongSelected(songs.first())
        viewModel.onPreviousTrack()

        assertEquals(songs.last(), viewModel.uiState.value.currentTrack)

        player.pause()
    }

    @Test
    fun previousTrackRestartsCurrentSong() = runTest {
        viewModel = createViewModel()

        val songs = listOf(song(1), song(2), song(3))

        repository.songsToReturn = songs
        viewModel.onSearchQueryChanged("weeknd")

        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS.milliseconds)
        advanceUntilIdle()

        viewModel.onSongSelected(songs[1])
        viewModel.onSeek(MIN_MILLIS_RESTART_MEDIA)
        viewModel.onPreviousTrack()

        assertEquals(songs[1], viewModel.uiState.value.currentTrack)
        assertEquals(0L, viewModel.uiState.value.positionMillis)

        player.pause()
    }

    @Test
    fun seekUpdatesPlayerAndState() = runTest {
        viewModel = createViewModel()
        viewModel.onSeek(15_000L)

        assertEquals(15_000L, player.currentPosition)
        assertEquals(15_000L, viewModel.uiState.value.positionMillis)
    }

    @Test
    fun trackEndStopsPlayback() = runTest {
        viewModel = createViewModel()

        val target = song(1)

        viewModel.onSongSelected(target)
        player.simulateTrackEnded()

        val state = viewModel.uiState.value

        assertFalse(state.isPlaying)
        assertEquals(state.durationMillis, state.positionMillis)
    }

    @Test
    fun onClearedReleasesPlayer() = runTest {
        viewModel = createViewModel()
        viewModel.onCleared()

        assertTrue(player.releaseCalled)
    }
}