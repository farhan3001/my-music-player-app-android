package com.my.music.app.model

import com.google.gson.annotations.SerializedName

data class MusicSearchResponse(
    @SerializedName("resultCount") val resultCount: Int,
    @SerializedName("results") val results: List<MusicTrack>
)

data class MusicTrack(
    @SerializedName("trackId") val trackId: Long?,
    @SerializedName("trackName") val trackName: String?,
    @SerializedName("artistName") val artistName: String?,
    @SerializedName("artworkUrl100") val artworkUrl100: String?,
    @SerializedName("previewUrl") val previewUrl: String?,
    @SerializedName("trackTimeMillis") val trackTimeMillis: Long?
)