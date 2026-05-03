package me.notanoticed.pdfmanager.feature.settings

import androidx.annotation.StringRes
import me.notanoticed.pdfmanager.R

enum class AppLanguage(
    val code: String,
    @StringRes val labelRes: Int
) {
    ENGLISH("en", R.string.language_english),
    FRENCH("fr", R.string.language_french),
    GERMAN("de", R.string.language_german),
    JAPANESE("ja", R.string.language_japanese),
    MANDARIN("zh", R.string.language_mandarin),
    SLOVAK("sk", R.string.language_slovak),
    SPANISH("es", R.string.language_spanish),
    UKRAINIAN("uk", R.string.language_ukrainian);

    companion object {
        val options: List<AppLanguage> = listOf(
            ENGLISH,
            FRENCH,
            GERMAN,
            JAPANESE,
            MANDARIN,
            SLOVAK,
            SPANISH,
            UKRAINIAN
        )

        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code == code } ?: ENGLISH
        }

        fun fromLanguageTag(tag: String?): AppLanguage {
            val normalized = tag
                ?.substringBefore('-')
                ?.lowercase()

            return fromCode(normalized)
        }
    }
}
