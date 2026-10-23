package ru.obroki.app.domain

object PaymentDedup {

    fun normalizeKey(title: String, payee: String): Pair<String, String> =
        title.trim().lowercase() to payee.trim().lowercase()

    fun matches(
        aTitle: String,
        aPayee: String,
        aDueDay: Int,
        aYear: Int,
        aMonth: Int,
        bTitle: String,
        bPayee: String,
        bDueDay: Int,
        bYear: Int,
        bMonth: Int,
    ): Boolean {
        val (at, ap) = normalizeKey(aTitle, aPayee)
        val (bt, bp) = normalizeKey(bTitle, bPayee)
        return at == bt && ap == bp && aDueDay == bDueDay && aYear == bYear && aMonth == bMonth
    }

    fun matches(existing: PaymentItem, title: String, payee: String, dueDay: Int, year: Int, month: Int): Boolean =
        matches(
            existing.title, existing.payee, existing.dueDay, existing.dueYear, existing.dueMonth,
            title, payee, dueDay, year, month
        )

    fun seriesKey(title: String, payee: String, dueDay: Int): Triple<String, String, Int> {
        val (t, p) = normalizeKey(title, payee)
        return Triple(t, p, dueDay)
    }

    fun seriesKey(item: PaymentItem): Triple<String, String, Int> =
        seriesKey(item.title, item.payee, item.dueDay)
}
