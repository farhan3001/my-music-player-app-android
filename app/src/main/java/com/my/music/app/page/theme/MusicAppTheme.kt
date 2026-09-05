package com.my.music.app.page.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.my.music.app.R

data class AppColors(
    val playerSilver: Color,
    val playerSilverDark: Color
)

private val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        playerSilver = Color.Unspecified,
        playerSilverDark = Color.Unspecified
    )
}

val MaterialTheme.appColors: AppColors
    @Composable
    get() = LocalAppColors.current

@Composable
fun MusicAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = lightColorScheme(
        primary = colorResource(R.color.light_blue_primary),
        onPrimary = colorResource(R.color.light_blue_on_primary),

        primaryContainer = colorResource(
            R.color.light_blue_primary_container
        ),
        onPrimaryContainer = colorResource(
            R.color.light_blue_on_primary_container
        ),

        secondary = colorResource(
            R.color.light_blue_secondary
        ),
        onSecondary = colorResource(
            R.color.light_blue_on_secondary
        ),

        background = colorResource(
            R.color.light_blue_background
        ),
        onBackground = colorResource(
            R.color.light_blue_on_background
        ),

        surface = colorResource(
            R.color.light_blue_surface
        ),
        onSurface = colorResource(
            R.color.light_blue_on_surface
        ),

        surfaceVariant = colorResource(
            R.color.light_blue_surface_variant
        ),
        onSurfaceVariant = colorResource(
            R.color.light_blue_on_surface_variant
        ),

        outline = colorResource(
            R.color.light_blue_outline
        )
    )

    val appColors = AppColors(
        playerSilver = colorResource(R.color.player_silver),
        playerSilverDark = colorResource(R.color.player_silver_dark)
    )

    CompositionLocalProvider(
        LocalAppColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}