package com.my.music.app.model

data class Music(
    val id: Long,
    val trackName: String,
    val artistName: String,
    val artworkUrl: String,
    val previewUrl: String,
    val trackTimeMillis: Long
)
