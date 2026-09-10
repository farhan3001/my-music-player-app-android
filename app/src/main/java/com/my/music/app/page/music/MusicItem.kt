package com.my.music.app.page.music

import android.graphics.drawable.Animatable
import android.widget.ImageView
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.my.music.app.R
import com.my.music.app.model.Music

@Composable
fun MusicListItem(
    music: Music,
    isPlaying: Boolean,
    isCurrentTrack: Boolean,
    onClick: (Music) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isCurrentTrack) {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                } else {
                    Color.Transparent
                }
            )
            .clickable { onClick(music) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = music.artworkUrl,
            contentDescription = music.trackName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {

            Text(
                text = music.trackName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = music.artistName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isCurrentTrack) {
            PlayingGif(
                gifResId = R.drawable.gif_play,
                isPlaying = isPlaying,
                modifier = Modifier.size(height = 52.dp, width = 52.dp)
            )
        }
    }
}

@Composable
fun PlayingGif(
    gifResId: Int,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setImageResource(gifResId)
            }
        },
        update = { imageView ->
            val drawable = imageView.drawable

            if (drawable is Animatable) {
                if (isPlaying) {
                    drawable.start()
                } else {
                    drawable.stop()
                }
            }
        }
    )
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalAnimationApi
@Preview
private fun PreviewDashboardCallDoctor() {
    MusicListItem(
        music = Music(
            id = 1,
            trackName = "abcd",
            artistName = "abcd",
            artworkUrl = "artworkUrl",
            previewUrl = "previewUrl",
            trackTimeMillis = 22000
        ),
        isCurrentTrack = true,
        onClick = {},
        isPlaying = true,
    )
}