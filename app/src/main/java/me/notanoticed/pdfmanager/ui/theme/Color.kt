/**
 * Design tokens (colors).
 *
 * To make light/dark mode work across the whole UI, the active token palette is switched
 * at runtime by PdfManagerTheme.
 */

package me.notanoticed.pdfmanager.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

internal data class AppPrimaryColors(
    val blue: Color,
    val lightBlue: Color,
    val darkBlue: Color,
    val green: Color,
    val red: Color,
    val yellow: Color,
    val white: Color,
    val lightGray: Color,
    val gray: Color,
    val slateGray: Color,
    val darkSlate: Color,
    val nearBlack: Color,
    val charcoal: Color
)

internal data class AppBackgroundColors(
    val app: Color,
    val surface: Color
)

internal data class AppSurfaceColors(
    val card: Color,
    val selectedCard: Color,
    val thumbnail: Color,
    val charcoalSlate: Color
)

internal data class AppTextColors(
    val primary: Color,
    val secondary: Color,
    val muted: Color,
    val blue: Color
)

internal data class AppBorderColors(
    val default: Color,
    val blue: Color,
    val darkBlue: Color,
    val lightBlue: Color,
    val gray: Color,
    val darkGray: Color,
    val subtle: Color
)

internal data class AppButtonColors(
    val blue: Color,
    val primaryPressed: Color,
    val darkSlate: Color,
    val skyBlue: Color,
    val lightGray: Color,
    val outline: Color,
    val green: Color,
    val red: Color,
    val iconBackground: Color,
    val iconBackgroundDisabled: Color
)

internal data class AppIconColors(
    val default: Color,
    val white: Color,
    val blue: Color,
    val green: Color,
    val red: Color,
    val yellow: Color,
    val gray: Color,
    val disabledGray: Color,
    val darkGray: Color,
    val pdf: Color,
    val merge: Color,
    val lock: Color,
    val delete: Color,
    val thumbnailBackground: Color
)

internal data class AppColorPalette(
    val primary: AppPrimaryColors,
    val background: AppBackgroundColors,
    val surface: AppSurfaceColors,
    val text: AppTextColors,
    val border: AppBorderColors,
    val button: AppButtonColors,
    val icon: AppIconColors
)

private val DarkPalette = AppColorPalette(
    primary = AppPrimaryColors(
        blue = Color(0xFF3B82F6),
        lightBlue = Color(0x2E3B82F6),
        darkBlue = Color(0xFF2F5EA8),
        green = Color(0xFF059568),
        red = Color(0xFFE82D2C),
        yellow = Color(0xFFFFB74D),
        white = Color(0xFFEAEFF7),
        lightGray = Color(0xFFA7B3C6),
        gray = Color(0xFF7D8592),
        slateGray = Color(0xFF374151),
        darkSlate = Color(0xFF374050),
        nearBlack = Color(0xFF111318),
        charcoal = Color(0xFF1B1E24)
    ),
    background = AppBackgroundColors(
        app = Color(0xFF111318),
        surface = Color(0xFF1B1E24)
    ),
    surface = AppSurfaceColors(
        card = Color(0xFF1B1E24),
        selectedCard = Color(0xFF195CCD),
        thumbnail = Color(0xFF2A2F37),
        charcoalSlate = Color(0xB01F2937)
    ),
    text = AppTextColors(
        primary = Color(0xFFEAEFF7),
        secondary = Color(0xFFA7B3C6),
        muted = Color(0xFF7D8592),
        blue = Color(0xFF3B82F6)
    ),
    border = AppBorderColors(
        default = Color(0xFFA7B3C6),
        blue = Color(0xFF3B82F6),
        darkBlue = Color(0xFF202B4C),
        lightBlue = Color(0xFF3379FF),
        gray = Color(0xFF7D8592),
        darkGray = Color(0xFF252832),
        subtle = Color(0x33FFFFFF)
    ),
    button = AppButtonColors(
        blue = Color(0xFF3B82F6),
        primaryPressed = Color(0xFF2F5EA8),
        darkSlate = Color(0xFF374050),
        skyBlue = Color(0xFF79AFFF),
        lightGray = Color(0xFFA7B3C6),
        outline = Color(0x33FFFFFF),
        green = Color(0xFF059568),
        red = Color(0xFFE82D2C),
        iconBackground = Color(0xFF4A5462),
        iconBackgroundDisabled = Color(0xFF2F343C)
    ),
    icon = AppIconColors(
        default = Color(0xFFA7B3C6),
        white = Color(0xFFEAEFF7),
        blue = Color(0xFF3B82F6),
        green = Color(0xFF059568),
        red = Color(0xFFE82D2C),
        yellow = Color(0xFFFFB74D),
        gray = Color(0xFF7D8592),
        disabledGray = Color(0xFF696C6F),
        darkGray = Color(0xFF2A2F37),
        pdf = Color(0xFFA7B3C6),
        merge = Color(0xFF059568),
        lock = Color(0xFFFFB74D),
        delete = Color(0xFFE82D2C),
        thumbnailBackground = Color(0xFF2A2F37)
    )
)

private val LightPalette = AppColorPalette(
    primary = AppPrimaryColors(
        blue = Color(0xFF2F6FE4),
        lightBlue = Color(0x223B82F6),
        darkBlue = Color(0xFF1E4FAF),
        green = Color(0xFF1E9E74),
        red = Color(0xFFD95B5B),
        yellow = Color(0xFFF2AA3D),
        white = Color(0xFF0F172A),
        lightGray = Color(0xFF5B667A),
        gray = Color(0xFF7B8797),
        slateGray = Color(0xFF9AA6B7),
        darkSlate = Color(0xFFD9E2F0),
        nearBlack = Color(0xFFF5F7FB),
        charcoal = Color(0xFFFFFFFF)
    ),
    background = AppBackgroundColors(
        app = Color(0xFFF5F7FB),
        surface = Color(0xFFFFFFFF)
    ),
    surface = AppSurfaceColors(
        card = Color(0xFFFFFFFF),
        selectedCard = Color(0xFFDCEAFF),
        thumbnail = Color(0xFFE8EEF7),
        charcoalSlate = Color(0xFFF1F5FB)
    ),
    text = AppTextColors(
        primary = Color(0xFF0F172A),
        secondary = Color(0xFF5B667A),
        muted = Color(0xFF7B8797),
        blue = Color(0xFF2F6FE4)
    ),
    border = AppBorderColors(
        default = Color(0xFFD4DCEA),
        blue = Color(0xFF2F6FE4),
        darkBlue = Color(0xFFC9D7EE),
        lightBlue = Color(0xFF6FA4FF),
        gray = Color(0xFF98A3B4),
        darkGray = Color(0xFFE1E8F2),
        subtle = Color(0x140F172A)
    ),
    button = AppButtonColors(
        blue = Color(0xFFD9E8FF),
        primaryPressed = Color(0xFFC2D8FF),
        darkSlate = Color(0xFFE1E8F2),
        skyBlue = Color(0xFF6FA4FF),
        lightGray = Color(0xFFD4DCEA),
        outline = Color(0x140F172A),
        green = Color(0xFFD6F3E8),
        red = Color(0xFFFADCDC),
        iconBackground = Color(0xFFD9E2F0),
        iconBackgroundDisabled = Color(0xFFE7ECF4)
    ),
    icon = AppIconColors(
        default = Color(0xFF5B667A),
        white = Color(0xFF0F172A),
        blue = Color(0xFF2F6FE4),
        green = Color(0xFF1E9E74),
        red = Color(0xFFD95B5B),
        yellow = Color(0xFFF2AA3D),
        gray = Color(0xFF7B8797),
        disabledGray = Color(0xFFA4AFBF),
        darkGray = Color(0xFFD9E2F0),
        pdf = Color(0xFF5B667A),
        merge = Color(0xFF1E9E74),
        lock = Color(0xFFF2AA3D),
        delete = Color(0xFFD95B5B),
        thumbnailBackground = Color(0xFFD9E2F0)
    )
)

private var activePalette by mutableStateOf(DarkPalette)

internal fun paletteForTheme(darkTheme: Boolean): AppColorPalette {
    return if (darkTheme) DarkPalette else LightPalette
}

internal fun updateActivePalette(darkTheme: Boolean) {
    activePalette = paletteForTheme(darkTheme)
}

/* -------------------- COLORS -------------------- */
object Colors {
    object Primary {
        val blue: Color get() = activePalette.primary.blue
        val lightBlue: Color get() = activePalette.primary.lightBlue
        val darkBlue: Color get() = activePalette.primary.darkBlue
        val green: Color get() = activePalette.primary.green
        val red: Color get() = activePalette.primary.red
        val yellow: Color get() = activePalette.primary.yellow
        val white: Color get() = activePalette.primary.white
        val lightGray: Color get() = activePalette.primary.lightGray
        val gray: Color get() = activePalette.primary.gray
        val slateGray: Color get() = activePalette.primary.slateGray
        val darkSlate: Color get() = activePalette.primary.darkSlate
        val nearBlack: Color get() = activePalette.primary.nearBlack
        val charcoal: Color get() = activePalette.primary.charcoal
    }

    object Background {
        val app: Color get() = activePalette.background.app
        val surface: Color get() = activePalette.background.surface
    }

    object Surface {
        val card: Color get() = activePalette.surface.card
        val selectedCard: Color get() = activePalette.surface.selectedCard
        val thumbnail: Color get() = activePalette.surface.thumbnail
        val charcoalSlate: Color get() = activePalette.surface.charcoalSlate
    }

    object Text {
        val primary: Color get() = activePalette.text.primary
        val secondary: Color get() = activePalette.text.secondary
        val muted: Color get() = activePalette.text.muted
        val blue: Color get() = activePalette.text.blue
    }

    object Border {
        val default: Color get() = activePalette.border.default
        val blue: Color get() = activePalette.border.blue
        val darkBlue: Color get() = activePalette.border.darkBlue
        val lightBlue: Color get() = activePalette.border.lightBlue
        val gray: Color get() = activePalette.border.gray
        val darkGray: Color get() = activePalette.border.darkGray
        val subtle: Color get() = activePalette.border.subtle
    }

    object Button {
        val blue: Color get() = activePalette.button.blue
        val primaryPressed: Color get() = activePalette.button.primaryPressed
        val darkSlate: Color get() = activePalette.button.darkSlate
        val skyBlue: Color get() = activePalette.button.skyBlue
        val lightGray: Color get() = activePalette.button.lightGray
        val outline: Color get() = activePalette.button.outline
        val green: Color get() = activePalette.button.green
        val red: Color get() = activePalette.button.red
        val iconBackground: Color get() = activePalette.button.iconBackground
        val iconBackgroundDisabled: Color get() = activePalette.button.iconBackgroundDisabled
    }

    object Icon {
        val default: Color get() = activePalette.icon.default
        val white: Color get() = activePalette.icon.white
        val blue: Color get() = activePalette.icon.blue
        val green: Color get() = activePalette.icon.green
        val red: Color get() = activePalette.icon.red
        val yellow: Color get() = activePalette.icon.yellow
        val gray: Color get() = activePalette.icon.gray
        val disabledGray: Color get() = activePalette.icon.disabledGray
        val darkGray: Color get() = activePalette.icon.darkGray
        val pdf: Color get() = activePalette.icon.pdf
        val merge: Color get() = activePalette.icon.merge
        val lock: Color get() = activePalette.icon.lock
        val delete: Color get() = activePalette.icon.delete
        val thumbnailBackground: Color get() = activePalette.icon.thumbnailBackground
    }
}
/* ----------------------------------------------- */
