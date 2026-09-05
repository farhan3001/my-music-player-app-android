package com.my.music.app.data.remote

import com.my.music.app.model.MusicSearchResponse
import com.my.music.app.predeterminedClassesObject.FakeItunesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import kotlinx.coroutines.test.runTest

class ItunesDataSourceTest {

    @Test
    fun searchSongsRemoteForwardsTermAndLimitToTheApi(): Unit = runTest {
        val fakeApi = FakeItunesApi()
        val response = MusicSearchResponse(resultCount = 0, results = emptyList())
        fakeApi.responseToReturn = response
        val dataSource = ItunesDataSource(fakeApi)

        val result = dataSource.searchSongsRemote(term = "drake", limit = 20)

        assertEquals("drake", fakeApi.lastTerm)
        assertEquals(20, fakeApi.lastLimit)
        assertSame(response, result)
    }
}