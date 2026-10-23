package ru.obroki.app.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

object PaymentDates {

    /** Effective due date = min(dueDay, last day of month). */
    fun effectiveDate(year: Int, month: Int, dueDay: Int): LocalDate {
        val ym = YearMonth.of(year, month)
        val day = dueDay.coerceIn(1, ym.lengthOfMonth())
        return ym.atDay(day)
    }

    fun nextMonth(year: Int, month: Int): Pair<Int, Int> {
        val next = YearMonth.of(year, month).plusMonths(1)
        return next.year to next.monthValue
    }

    /**
     * Year/month for a newly created payment with the given day-of-month.
     * If [dueDay] is already past in the calendar month of [today] (strictly less
     * than today's day), schedule in the next month; otherwise keep the current month.
     * Day-of-month rollover for short months is handled by [effectiveDate].
     */
    fun createYearMonth(dueDay: Int, today: LocalDate = LocalDate.now()): YearMonth {
        val current = YearMonth.from(today)
        return if (dueDay < today.dayOfMonth) current.plusMonths(1) else current
    }

    fun daysUntil(from: LocalDate, to: LocalDate): Long =
        java.time.temporal.ChronoUnit.DAYS.between(from, to)

    fun rowHighlight(item: PaymentItem, today: LocalDate): RowHighlight {
        if (item.isPaid) return RowHighlight.PAID
        val due = item.effectiveDate
        if (due.isBefore(today)) return RowHighlight.OVERDUE
        val days = daysUntil(today, due)
        if (days in 0..6) return RowHighlight.SOON
        return RowHighlight.NONE
    }

    /** «Ближайшая неделя»: today .. today+6 (7 calendar days including today). */
    fun periodNextWeek(today: LocalDate): Pair<LocalDate, LocalDate> =
        today to today.plusDays(6)

    /** «Ближайший месяц»: today .. today + 1 month − 1 day. */
    fun periodNextMonth(today: LocalDate): Pair<LocalDate, LocalDate> =
        today to today.plusMonths(1).minusDays(1)

    /** «До конца текущей недели»: today .. Sunday (week Mon–Sun). */
    fun periodUntilWeekEnd(today: LocalDate): Pair<LocalDate, LocalDate> {
        val sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        return today to sunday
    }

    /** «До конца текущего месяца»: today .. last day of month. */
    fun periodUntilMonthEnd(today: LocalDate): Pair<LocalDate, LocalDate> {
        val end = YearMonth.from(today).atEndOfMonth()
        return today to end
    }

    fun periodUntilCustom(today: LocalDate, to: LocalDate): Pair<LocalDate, LocalDate> =
        today to to
}
