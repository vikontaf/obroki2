package ru.obroki.app.domain

import java.util.Locale

object TextFormat {

    private val ru = Locale("ru", "RU")

    /**
     * Capitalize the first letter of the first word (sentence case).
     * Russian-friendly via [Locale] titlecase; leaves the rest of the string unchanged.
     */
    fun capitalizeFirstWord(raw: String, locale: Locale = ru): String {
        val i = raw.indexOfFirst { it.isLetter() }
        if (i < 0) return raw
        val ch = raw[i]
        val upper = ch.titlecase(locale)
        if (ch.toString() == upper) return raw
        return raw.replaceRange(i, i + 1, upper)
    }
}
