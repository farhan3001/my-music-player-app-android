package com.my.music.app.data.remote

import com.my.music.app.model.MusicSearchResponse
import javax.inject.Inject

class ItunesDataSource @Inject constructor(
    private val api: ItunesApi
) {
    suspend fun searchSongsRemote(term: String, limit: Int): MusicSearchResponse =
        api.searchSongs(term = term, limit = limit)
}