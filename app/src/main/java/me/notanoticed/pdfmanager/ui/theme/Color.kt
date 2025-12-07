package me.notanoticed.pdfmanager.ui.theme

import androidx.compose.ui.graphics.Color

/* -------------------- COLORS -------------------- */
object Colors {
    object Primary {
        val blue = Color(0xFF3B82F6)
        val lightBlue = blue.copy(alpha = 0.18f)
        val darkBlue = Color(0xFF2F5EA8)


        val green = Color(0xFF059568)
        val red = Color(0xFFE82D2C)
        val yellow = Color(0xFFFFB74D)


        val white = Color(0xFFEAEFF7)
        val lightGray = Color(0xFFA7B3C6)
        val gray = Color(0xFF7D8592)
        val slateGray = Color(0xFF374151)
        val darkSlate = Color(0xFF374050)
        val nearBlack = Color(0xFF111318)
        val charcoal = Color(0xFF1B1E24)
    }

    object Background {
        val app = Primary.nearBlack
        val surface = Primary.charcoal
    }

    object Surface {
        val card = Primary.charcoal
        val selectedCard = Color(0xFF195CCD)
        val thumbnail = Color(0xFF2A2F37)
        val charcoalSlate = Color(0xB01F2937)
    }

    object Text {
        val primary = Primary.white
        val secondary = Primary.lightGray
        val muted = Primary.gray
        val blue = Primary.blue
    }

    object Border {
        val default = Primary.lightGray
        val blue = Primary.blue
        val darkBlue = Color(0xFF202B4C)
        val lightBlue = Color(0xFF3379FF)
        val gray = Primary.gray
        val darkGray = Color(0xFF252832)
        val subtle = Color.Gray.copy(alpha = 0.2f)
    }

    object Button {
        val blue = Primary.blue
        val primaryPressed = Primary.darkBlue
        val darkSlate = Primary.darkSlate
        val skyBlue = Color(0xFF79AFFF)
        val lightGray = Primary.lightGray
        val outline = Border.subtle
        val green = Primary.green
        val red = Primary.red
        val iconBackground = Color(0xFF4A5462)
        val iconBackgroundDisabled = Color(0xFF2F343C)
    }

    object Icon {
        val default = Primary.lightGray
        val white = Primary.white
        val blue = Primary.blue
        val green = Primary.green
        val red = Primary.red
        val yellow = Primary.yellow
        val gray = Primary.gray
        val disabledGray = Color(0xFF696C6F)
        val darkGray = Color(0xFF2A2F37)


        val pdf = default
        val merge = green
        val lock = yellow
        val delete = red
        val thumbnailBackground = darkGray
    }
}
/* ----------------------------------------------- */