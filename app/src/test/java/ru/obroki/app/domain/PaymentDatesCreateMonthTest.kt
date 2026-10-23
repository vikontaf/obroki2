package ru.obroki.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class PaymentDatesCreateMonthTest {

    @Test
    fun dayBeforeToday_goesToNextMonth() {
        val today = LocalDate.of(2026, 10, 23)
        assertEquals(YearMonth.of(2026, 11), PaymentDates.createYearMonth(10, today))
        assertEquals(YearMonth.of(2026, 11), PaymentDates.createYearMonth(22, today))
    }

    @Test
    fun dayEqualsToday_staysCurrentMonth() {
        val today = LocalDate.of(2026, 10, 23)
        assertEquals(YearMonth.of(2026, 10), PaymentDates.createYearMonth(23, today))
    }

    @Test
    fun dayAfterToday_staysCurrentMonth() {
        val today = LocalDate.of(2026, 10, 23)
        assertEquals(YearMonth.of(2026, 10), PaymentDates.createYearMonth(24, today))
        assertEquals(YearMonth.of(2026, 10), PaymentDates.createYearMonth(31, today))
    }

    @Test
    fun decemberPastDay_rollsToNextYear() {
        val today = LocalDate.of(2026, 12, 20)
        assertEquals(YearMonth.of(2027, 1), PaymentDates.createYearMonth(5, today))
    }

    @Test
    fun pastDayIntoShortMonth_effectiveDateRollsDown() {
        // dueDay 30 already passed on 31 Jan → February; effective day = 28 in 2026
        val today = LocalDate.of(2026, 1, 31)
        val ym = PaymentDates.createYearMonth(30, today)
        assertEquals(YearMonth.of(2026, 2), ym)
        assertEquals(LocalDate.of(2026, 2, 28), PaymentDates.effectiveDate(ym.year, ym.monthValue, 30))
    }
}
