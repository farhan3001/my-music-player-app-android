package com.my.music.app.data.remote

import com.my.music.app.model.MusicSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {

    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int
    ): MusicSearchResponse
}