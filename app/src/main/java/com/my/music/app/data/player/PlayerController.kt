package com.my.music.app.data.player

interface PlayerController {
    val isPlaying: Boolean
    val currentPosition: Long
    val duration: Long

    fun setMediaItem(uri: String)
    fun play()
    fun pause()
    fun seekTo(positionMillis: Long)
    fun addListener(listener: PlayerListener)
    fun release()
}

interface PlayerListener {
    fun onIsPlayingChanged(isPlaying: Boolean)
    fun onPlaybackStateChanged(state: PlayerPlaybackState)
}

enum class PlayerPlaybackState {
    IDLE, BUFFERING, READY, ENDED
}