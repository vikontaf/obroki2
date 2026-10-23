package ru.obroki.app.domain

import java.time.LocalDate

/**
 * Domain model for a monthly payment instance.
 * [debt] is optional: null means "not set" (always clone on pay).
 */
data class PaymentItem(
    val id: Long = 0L,
    val title: String,
    val payee: String,
    val amount: Double,
    val debt: Double?,
    val dueDay: Int,
    val dueYear: Int,
    val dueMonth: Int,
    val isPaid: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val effectiveDate: LocalDate
        get() = PaymentDates.effectiveDate(dueYear, dueMonth, dueDay)
}

enum class RowHighlight {
    NONE,
    PAID,
    OVERDUE,
    SOON,
}

enum class AppThemeMode {
    DARK,
    LIGHT,
    SYSTEM,
}

enum class SumPeriodKind {
    NEXT_WEEK,
    NEXT_MONTH,
    UNTIL_WEEK_END,
    UNTIL_MONTH_END,
    UNTIL_CUSTOM_DATE,
}
