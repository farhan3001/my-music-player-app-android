package com.my.music.app.predeterminedClassesObject

import com.my.music.app.data.remote.ItunesApi
import com.my.music.app.model.MusicSearchResponse

class FakeItunesApi : ItunesApi {

    lateinit var responseToReturn: MusicSearchResponse
    var lastTerm: String? = null
    var lastLimit: Int? = null

    override suspend fun searchSongs(
        term: String,
        media: String,
        entity: String,
        limit: Int
    ): MusicSearchResponse {
        lastTerm = term
        lastLimit = limit
        return responseToReturn
    }
}
