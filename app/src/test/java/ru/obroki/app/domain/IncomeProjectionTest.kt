package ru.obroki.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class IncomeProjectionTest {

    @Test
    fun sumsReceiptsInRange() {
        val receipts = listOf(
            IncomeReceipt(dayOfMonth = 1, amount = 10_000.0),
            IncomeReceipt(dayOfMonth = 15, amount = 5_000.0),
        )
        val sum = IncomeProjection.calculate(
            receipts,
            LocalDate.of(2026, 10, 1),
            LocalDate.of(2026, 10, 31),
        )
        assertEquals(15_000.0, sum, 0.001)
    }

    @Test
    fun excludesReceiptsOutsideRange() {
        val receipts = listOf(IncomeReceipt(dayOfMonth = 1, amount = 10_000.0))
        val sum = IncomeProjection.calculate(
            receipts,
            LocalDate.of(2026, 10, 5),
            LocalDate.of(2026, 10, 31),
        )
        assertEquals(0.0, sum, 0.001)
    }

    @Test
    fun projectsAcrossMonths() {
        val receipts = listOf(IncomeReceipt(dayOfMonth = 10, amount = 1_000.0))
        val sum = IncomeProjection.calculate(
            receipts,
            LocalDate.of(2026, 10, 1),
            LocalDate.of(2026, 12, 31),
        )
        assertEquals(3_000.0, sum, 0.001)
    }

    @Test
    fun day31RollsToLastDayOfShortMonth() {
        val receipts = listOf(IncomeReceipt(dayOfMonth = 31, amount = 2_000.0))
        // Feb 2026 has 28 days → effective 28 Feb
        val sum = IncomeProjection.calculate(
            receipts,
            LocalDate.of(2026, 2, 1),
            LocalDate.of(2026, 2, 28),
        )
        assertEquals(2_000.0, sum, 0.001)
    }

    @Test
    fun emptyScheduleIsZero() {
        val sum = IncomeProjection.calculate(
            emptyList(),
            LocalDate.of(2026, 10, 1),
            LocalDate.of(2026, 10, 31),
        )
        assertEquals(0.0, sum, 0.001)
    }

    @Test
    fun periodTotalsFormula() {
        val totals = PeriodTotals(expenses = 3_000.0, income = 10_000.0, balance = 5_000.0)
        assertEquals(12_000.0, totals.total, 0.001)
    }
}
