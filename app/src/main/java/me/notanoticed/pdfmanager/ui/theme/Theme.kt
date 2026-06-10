package me.notanoticed.pdfmanager.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat



/* -------------------- COLOR SCHEME -------------------- */
private fun createColorScheme(
    darkTheme: Boolean,
    palette: AppColorPalette
) = if (darkTheme) {
    darkColorScheme(
        background = palette.background.app,
        surface = palette.surface.card,
        primary = palette.primary.blue,
        onPrimary = palette.primary.white,
        onSurface = palette.text.primary,
        onBackground = palette.text.primary,
        secondary = palette.primary.darkBlue
    )
} else {
    lightColorScheme(
        background = palette.background.app,
        surface = palette.surface.card,
        primary = palette.primary.blue,
        onPrimary = palette.primary.white,
        onBackground = palette.text.primary,
        onSurface = palette.text.primary,
        secondary = palette.primary.darkBlue
    )
}
/* ------------------------------------------------------ */



/* -------------------- TYPOGRAPHY / SHAPES -------------------- */
private val appTypography = AppTypography
private val appShapes = Shapes(
    extraSmall = RoundedCornerShape(6),
    small = RoundedCornerShape(10),
    medium = RoundedCornerShape(16),
    large = RoundedCornerShape(22)
)
/* ------------------------------------------------------------- */



/* -------------------- THEME WRAPPER -------------------- */
@Composable
fun PdfManagerTheme(
    darkTheme: Boolean = true,
    applySystemBars: Boolean = true,
    content: @Composable () -> Unit
) {
    val palette = paletteForTheme(darkTheme)
    val colorScheme = createColorScheme(
        darkTheme = darkTheme,
        palette = palette
    )
    val view = LocalView.current

    if (applySystemBars) {
        SideEffect {
            val activity = view.context.findActivity() ?: return@SideEffect
            activity.window.statusBarColor = AndroidColor.TRANSPARENT
            activity.window.navigationBarColor = AndroidColor.TRANSPARENT

            WindowCompat.getInsetsController(activity.window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAppPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = appTypography,
            shapes = appShapes,
            content = content
        )
    }
}
/* ------------------------------------------------------- */

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
