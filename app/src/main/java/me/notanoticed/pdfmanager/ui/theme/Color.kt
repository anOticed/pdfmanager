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
    val rename: Color,
    val split: Color,
    val share: Color,
    val print: Color,
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
        rename = Color(0xFFB38CFF),
        split = Color(0xFFFFA24C),
        share = Color(0xFF5ED0FF),
        print = Color(0xFF9FB0C7),
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
        blue = Color(0xFF1F6BFF),
        lightBlue = Color(0x1F1F6BFF),
        darkBlue = Color(0xFF1858D6),
        green = Color(0xFF20B874),
        red = Color(0xFFD94B4B),
        yellow = Color(0xFFE49B34),
        white = Color(0xFFFFFFFF),
        lightGray = Color(0xFF5A6C84),
        gray = Color(0xFF74859C),
        slateGray = Color(0xFFC4D0E0),
        darkSlate = Color(0xFFDCE5F0),
        nearBlack = Color(0xFF0F172A),
        charcoal = Color(0xFF1B2434)
    ),
    background = AppBackgroundColors(
        app = Color(0xFFF2F5FA),
        surface = Color(0xFFFFFFFF)
    ),
    surface = AppSurfaceColors(
        card = Color(0xFFFFFFFF),
        selectedCard = Color(0xFFE0EAFF),
        thumbnail = Color(0xFFE1E9F3),
        charcoalSlate = Color(0xFFE7EEF7)
    ),
    text = AppTextColors(
        primary = Color(0xFF172235),
        secondary = Color(0xFF596A81),
        muted = Color(0xFF7C8CA2),
        blue = Color(0xFF1F6BFF)
    ),
    border = AppBorderColors(
        default = Color(0xFFCBD6E3),
        blue = Color(0xFF1F6BFF),
        darkBlue = Color(0xFFBFCCDD),
        lightBlue = Color(0xFF5A95FF),
        gray = Color(0xFF95A5BA),
        darkGray = Color(0xFFD1DAE6),
        subtle = Color(0x240F172A)
    ),
    button = AppButtonColors(
        blue = Color(0xFF1F6BFF),
        primaryPressed = Color(0xFF1858D6),
        darkSlate = Color(0xFF556983),
        skyBlue = Color(0xFF5C9CFF),
        lightGray = Color(0xFFD9E2EC),
        outline = Color(0x180F172A),
        green = Color(0xFF20B874),
        red = Color(0xFFD94B4B),
        iconBackground = Color(0xFFDEE7F1),
        iconBackgroundDisabled = Color(0xFFE9EFF6)
    ),
    icon = AppIconColors(
        default = Color(0xFF5A6C84),
        white = Color(0xFFFFFFFF),
        blue = Color(0xFF1F6BFF),
        green = Color(0xFF20B874),
        red = Color(0xFFD94B4B),
        yellow = Color(0xFFE49B34),
        rename = Color(0xFF7A52E8),
        split = Color(0xFFE97818),
        share = Color(0xFF007FB8),
        print = Color(0xFF5F7088),
        gray = Color(0xFF74859C),
        disabledGray = Color(0xFF9EACBF),
        darkGray = Color(0xFFBCC9D9),
        pdf = Color(0xFF5A6C84),
        merge = Color(0xFF20B874),
        lock = Color(0xFFE49B34),
        delete = Color(0xFFD94B4B),
        thumbnailBackground = Color(0xFFBCC9D9)
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
        val rename: Color get() = activePalette.icon.rename
        val split: Color get() = activePalette.icon.split
        val share: Color get() = activePalette.icon.share
        val print: Color get() = activePalette.icon.print
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
