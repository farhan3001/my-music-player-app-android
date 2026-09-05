package com.my.music.app.predeterminedClassesObject

import com.my.music.app.data.player.PlayerController
import com.my.music.app.data.player.PlayerListener
import com.my.music.app.data.player.PlayerPlaybackState

class FakePlayerController : PlayerController {

    private var listener: PlayerListener? = null

    var lastUri: String? = null
        private set

    var releaseCalled: Boolean = false
        private set

    override var isPlaying: Boolean = false
        private set

    override var currentPosition: Long = 0L
    override var duration: Long = 30_000L

    override fun setMediaItem(uri: String) {
        lastUri = uri
        currentPosition = 0L
        isPlaying = true
        listener?.onPlaybackStateChanged(PlayerPlaybackState.READY)
        listener?.onIsPlayingChanged(true)
    }

    override fun play() {
        isPlaying = true
        listener?.onIsPlayingChanged(true)
    }

    override fun pause() {
        isPlaying = false
        listener?.onIsPlayingChanged(false)
    }

    override fun seekTo(positionMillis: Long) {
        currentPosition = positionMillis
    }

    override fun addListener(listener: PlayerListener) {
        this.listener = listener
    }

    override fun release() {
        releaseCalled = true
    }

    fun simulateTrackEnded() {
        isPlaying = false
        currentPosition = duration
        listener?.onPlaybackStateChanged(PlayerPlaybackState.ENDED)
        listener?.onIsPlayingChanged(false)
    }
}