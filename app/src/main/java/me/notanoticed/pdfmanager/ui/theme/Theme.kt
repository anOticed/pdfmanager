/**
 * Material theme setup.
 *
 * Applies the project color scheme and typography through MaterialTheme.
 */

package me.notanoticed.pdfmanager.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext



/* -------------------- COLOR SCHEME -------------------- */
private val DarkColorScheme = darkColorScheme(
    background = Colors.Background.app,
    surface = Colors.Surface.card,
    primary = Colors.Primary.blue,
    onPrimary = Colors.Primary.white,
    onSurface = Colors.Primary.white,
    onBackground = Colors.Primary.white,
    secondary = Colors.Primary.darkBlue
)

private val LightColorScheme = lightColorScheme(
    background = Color(0xFFF7F9FC),
    surface = Color(0xFFFFFFFF),
    primary = Colors.Primary.blue,
    onPrimary = Color.White,
    onBackground = Color(0xFF0E1624),
    onSurface = Color(0xFF0E1624),
    secondary = Colors.Primary.darkBlue
)
/* ------------------------------------------------------ */



/* -------------------- TYPOGRAPHY / SHAPES -------------------- */
private val appTypography = Typography
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
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = appTypography,
        shapes = appShapes,
        content = content
    )
}
/* ------------------------------------------------------- */