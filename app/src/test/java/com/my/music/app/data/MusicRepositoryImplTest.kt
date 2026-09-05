package com.my.music.app.data

import com.my.music.app.data.remote.ItunesDataSource
import com.my.music.app.model.MusicSearchResponse
import com.my.music.app.model.MusicTrack
import com.my.music.app.predeterminedClassesObject.FakeItunesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MusicRepositoryImplTest {

    private lateinit var fakeApi: FakeItunesApi
    private lateinit var repository: MusicRepositoryImpl

    @Before
    fun setUp() {
        fakeApi = FakeItunesApi()
        repository = MusicRepositoryImpl(ItunesDataSource(fakeApi))
    }

    @Test
    fun mapsValidTracksToMusicDomainObjects() = runTest {
        fakeApi.responseToReturn = MusicSearchResponse(
            resultCount = 1,
            results = listOf(
                MusicTrack(
                    trackId = 1L,
                    trackName = "Blinding Lights",
                    artistName = "The Weeknd",
                    artworkUrl100 = "https://example.com/art.jpg",
                    previewUrl = "https://example.com/preview.m4a",
                    trackTimeMillis = 200_000L
                )
            )
        )

        val result = repository.searchSongs(query = "the weeknd", limit = 10)

        assertEquals(1, result.size)
        assertEquals("Blinding Lights", result[0].trackName)
        assertEquals("The Weeknd", result[0].artistName)
        assertEquals(200_000L, result[0].trackTimeMillis)
    }

    @Test
    fun dropsTracksMissingRequiredFields() = runTest {
        fakeApi.responseToReturn = MusicSearchResponse(
            resultCount = 3,
            results = listOf(
                MusicTrack(1L, "Valid Song", "Artist", "art.jpg", "preview.m4a", 1000L),
                MusicTrack(2L, null, "Artist", "art.jpg", "preview.m4a", 1000L),
                MusicTrack(3L, "No Preview", "Artist", "art.jpg", null, 1000L)
            )
        )

        val result = repository.searchSongs(query = "test", limit = 10)

        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
    }

    @Test
    fun defaultsMissingTrackTimeMillisToZero() = runTest {
        fakeApi.responseToReturn = MusicSearchResponse(
            resultCount = 1,
            results = listOf(
                MusicTrack(1L, "Song", "Artist", "art.jpg", "preview.m4a", trackTimeMillis = null)
            )
        )

        val result = repository.searchSongs(query = "test", limit = 10)

        assertEquals(0L, result[0].trackTimeMillis)
    }

    @Test
    fun forwardsQueryAndLimitThroughTheDataSource() = runTest {
        fakeApi.responseToReturn = MusicSearchResponse(resultCount = 0, results = emptyList())

        repository.searchSongs(query = "drake", limit = 15)

        assertEquals("drake", fakeApi.lastTerm)
        assertEquals(15, fakeApi.lastLimit)
    }

    @Test
    fun emptyResultsReturnsEmptyList() = runTest {
        fakeApi.responseToReturn = MusicSearchResponse(resultCount = 0, results = emptyList())

        val result = repository.searchSongs(query = "zzz", limit = 10)

        assertTrue(result.isEmpty())
    }
}