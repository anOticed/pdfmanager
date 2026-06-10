package me.notanoticed.pdfmanager.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
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
    val compress: Color,
    val iconBackground: Color,
    val iconBackgroundDisabled: Color
)

internal data class AppIconColors(
    val default: Color,
    val white: Color,
    val blue: Color,
    val green: Color,
    val red: Color,
    val compress: Color,
    val reorder: Color,
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
        compress = Color(0xFF8B5CF6),
        iconBackground = Color(0xFF4A5462),
        iconBackgroundDisabled = Color(0xFF2F343C)
    ),
    icon = AppIconColors(
        default = Color(0xFFA7B3C6),
        white = Color(0xFFEAEFF7),
        blue = Color(0xFF3B82F6),
        green = Color(0xFF059568),
        red = Color(0xFFE82D2C),
        compress = Color(0xFF8B5CF6),
        reorder = Color(0xFF2DD4BF),
        yellow = Color(0xFFFFB74D),
        rename = Color(0xFFB38CFF),
        split = Color(0xFFFFA24C),
        share = Color(0xFF5ED0FF),
        print = Color(0xFF8FA2FF),
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
        compress = Color(0xFF7C3AED),
        iconBackground = Color(0xFFDEE7F1),
        iconBackgroundDisabled = Color(0xFFE9EFF6)
    ),
    icon = AppIconColors(
        default = Color(0xFF5A6C84),
        white = Color(0xFFFFFFFF),
        blue = Color(0xFF1F6BFF),
        green = Color(0xFF20B874),
        red = Color(0xFFD94B4B),
        compress = Color(0xFF7C3AED),
        reorder = Color(0xFF0F9F8C),
        yellow = Color(0xFFE49B34),
        rename = Color(0xFF7A52E8),
        split = Color(0xFFE97818),
        share = Color(0xFF007FB8),
        print = Color(0xFF4F63D8),
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

internal val LocalAppPalette = staticCompositionLocalOf { DarkPalette }

internal fun paletteForTheme(darkTheme: Boolean): AppColorPalette {
    return if (darkTheme) DarkPalette else LightPalette
}

/* -------------------- COLORS -------------------- */
object Colors {
    object Primary {
        val blue: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.primary.blue
        val lightBlue: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.primary.lightBlue
        val darkBlue: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.primary.darkBlue
        val green: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.primary.green
        val red: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.primary.red
        val yellow: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.primary.yellow
        val white: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.primary.white
        val lightGray: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.primary.lightGray
        val gray: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.primary.gray
        val slateGray: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.primary.slateGray
        val darkSlate: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.primary.darkSlate
        val nearBlack: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.primary.nearBlack
        val charcoal: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.primary.charcoal
    }

    object Background {
        val app: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.background.app
        val surface: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.background.surface
    }

    object Surface {
        val card: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.surface.card
        val selectedCard: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.surface.selectedCard
        val thumbnail: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.surface.thumbnail
        val charcoalSlate: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.surface.charcoalSlate
    }

    object Text {
        val primary: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.text.primary
        val secondary: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.text.secondary
        val muted: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.text.muted
        val blue: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.text.blue
    }

    object Border {
        val default: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.border.default
        val blue: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.border.blue
        val darkBlue: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.border.darkBlue
        val lightBlue: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.border.lightBlue
        val gray: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.border.gray
        val darkGray: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.border.darkGray
        val subtle: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.border.subtle
    }

    object Button {
        val blue: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.button.blue
        val primaryPressed: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.button.primaryPressed
        val darkSlate: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.button.darkSlate
        val skyBlue: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.button.skyBlue
        val lightGray: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.button.lightGray
        val outline: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.button.outline
        val green: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.button.green
        val red: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.button.red
        val compress: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.button.compress
        val iconBackground: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.button.iconBackground
        val iconBackgroundDisabled: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.button.iconBackgroundDisabled
    }

    object Icon {
        val default: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.default
        val white: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.white
        val blue: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.blue
        val green: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.green
        val red: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.red
        val compress: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.compress
        val reorder: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.reorder
        val yellow: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.yellow
        val rename: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.rename
        val split: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.split
        val share: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.share
        val print: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.print
        val gray: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.gray
        val disabledGray: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.disabledGray
        val darkGray: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.darkGray
        val pdf: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.pdf
        val merge: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.merge
        val lock: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.lock
        val delete: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.delete
        val thumbnailBackground: Color
            @Composable
            @ReadOnlyComposable
            get() = LocalAppPalette.current.icon.thumbnailBackground
    }
}
/* ----------------------------------------------- */
