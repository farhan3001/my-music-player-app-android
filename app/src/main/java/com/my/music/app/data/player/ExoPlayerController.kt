package com.my.music.app.data.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ExoPlayerController @Inject constructor(
    @ApplicationContext context: Context
) : PlayerController {

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    override val isPlaying: Boolean
        get() = exoPlayer.isPlaying

    override val currentPosition: Long
        get() = exoPlayer.currentPosition

    override val duration: Long
        get() = exoPlayer.duration

    override fun setMediaItem(uri: String) {
        exoPlayer.setMediaItem(MediaItem.fromUri(uri))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    override fun play() = exoPlayer.play()

    override fun pause() = exoPlayer.pause()

    override fun seekTo(positionMillis: Long) = exoPlayer.seekTo(positionMillis)

    override fun addListener(listener: PlayerListener) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                listener.onIsPlayingChanged(isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val state = when (playbackState) {
                    Player.STATE_IDLE -> PlayerPlaybackState.IDLE
                    Player.STATE_BUFFERING -> PlayerPlaybackState.BUFFERING
                    Player.STATE_READY -> PlayerPlaybackState.READY
                    Player.STATE_ENDED -> PlayerPlaybackState.ENDED
                    else -> PlayerPlaybackState.IDLE
                }
                listener.onPlaybackStateChanged(state)
            }
        })
    }

    override fun release() = exoPlayer.release()
}