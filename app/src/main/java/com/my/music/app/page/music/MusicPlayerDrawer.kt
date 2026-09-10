package com.my.music.app.page.music

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.my.music.app.model.Music
import com.my.music.app.page.theme.appColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerDrawer(
    isPlaying: Boolean,
    positionMillis: Long,
    durationMillis: Long,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.appColors.playerSilver.copy(
            alpha = 0.7f
        ),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 12.dp
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(
                    24.dp,
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = onPreviousClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(onClick = onPlayPauseClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = if (isPlaying) {
                            Icons.Default.Pause
                        } else {
                            Icons.Default.PlayArrow
                        },
                        contentDescription = if (isPlaying) {
                            "Pause"
                        } else {
                            "Play"
                        },
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(onClick = onNextClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.Black
                    )
                }
            }

            val safeDuration = durationMillis.coerceAtLeast(1L)

            Slider(
                value = positionMillis
                    .coerceIn(0L, safeDuration)
                    .toFloat(),

                valueRange = 0f..safeDuration.toFloat(),

                onValueChange = {
                    onSeek(it.toLong())
                },

                colors = SliderDefaults.colors(
                    activeTrackColor = Color.Black,
                    inactiveTrackColor =  MaterialTheme.appColors.playerSilverDark,
                    thumbColor = Color.White
                ),

                track = { sliderState: SliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        colors = SliderDefaults.colors(
                            activeTrackColor = MaterialTheme.appColors.playerSilverDark,
                            inactiveTrackColor = Color.Black,
                            thumbColor = Color.Transparent
                        ),
                        thumbTrackGapSize = 0.dp,
                    )
                },

                thumb = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            )
                    )
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalAnimationApi
@Preview
private fun PreviewMusicPlayerDrawer() {
    MusicPlayerDrawer(
        isPlaying = true,
        positionMillis = 1200,
        durationMillis = 12000,
        onPlayPauseClick = {},
        onNextClick = {},
        onPreviousClick = {},
        onSeek = {},
        modifier = Modifier.fillMaxWidth()
    )
}