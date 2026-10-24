package ru.obroki.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SumToPayTest {

    private fun item(
        amount: Double = 100.0,
        debt: Double? = null,
        day: Int = 10,
        year: Int = 2026,
        month: Int = 10,
        paid: Boolean = false,
        title: String = "ЖКХ",
        payee: String = "Горсвет",
        id: Long = 1,
    ) = PaymentItem(
        id = id,
        title = title,
        payee = payee,
        amount = amount,
        debt = debt,
        dueDay = day,
        dueYear = year,
        dueMonth = month,
        isPaid = paid,
    )

    @Test
    fun unpaidInWindow_counted() {
        val all = listOf(item(amount = 50.0, day = 25, month = 10))
        val sum = SumToPay.calculate(
            all,
            LocalDate.of(2026, 10, 23),
            LocalDate.of(2026, 10, 31),
        )
        assertEquals(50.0, sum, 0.001)
    }

    @Test
    fun paidNotCounted() {
        val all = listOf(item(amount = 50.0, day = 25, month = 10, paid = true))
        val sum = SumToPay.calculate(
            all,
            LocalDate.of(2026, 10, 23),
            LocalDate.of(2026, 10, 31),
        )
        assertEquals(0.0, sum, 0.001)
    }

    @Test
    fun projectsFutureMonthsWithoutDebt() {
        // Latest unpaid Oct 10; project through Dec 31 → Nov + Dec virtuals
        val all = listOf(item(amount = 100.0, day = 10, month = 10, year = 2026, debt = null))
        val sum = SumToPay.calculate(
            all,
            LocalDate.of(2026, 10, 1),
            LocalDate.of(2026, 12, 31),
        )
        // Oct (real) + Nov + Dec = 300
        assertEquals(300.0, sum, 0.001)
    }

    @Test
    fun projectionStopsWhenDebtExhausts() {
        // debt 150, amount 100 → after Oct, next clone debt=50; after that debt'=-50 stop
        // So: Oct real + Nov virtual (debt 50 on it means one more would not clone after paying Nov)
        // Start remainingDebt=150 on Oct instance
        // First virtual Nov: nextDebt = 150-100 = 50 > 0 → include Nov, remaining becomes 50
        // Second virtual Dec: nextDebt = 50-100 = -50 <= 0 → stop
        // Total = Oct 100 + Nov 100 = 200
        val all = listOf(item(amount = 100.0, day = 10, month = 10, year = 2026, debt = 150.0))
        val sum = SumToPay.calculate(
            all,
            LocalDate.of(2026, 10, 1),
            LocalDate.of(2026, 12, 31),
        )
        assertEquals(200.0, sum, 0.001)
    }

    @Test
    fun customDateBeforeToday_isZero() {
        val all = listOf(item(day = 10, month = 10))
        val sum = SumToPay.calculate(
            all,
            LocalDate.of(2026, 10, 23),
            LocalDate.of(2026, 10, 20),
        )
        assertEquals(0.0, sum, 0.001)
    }

    @Test
    fun rowHighlightPriority() {
        val today = LocalDate.of(2026, 10, 23)
        val paid = item(paid = true, day = 1, month = 10)
        assertEquals(RowHighlight.PAID, PaymentDates.rowHighlight(paid, today))

        val overdue = item(paid = false, day = 1, month = 10)
        assertEquals(RowHighlight.OVERDUE, PaymentDates.rowHighlight(overdue, today))

        val soon = item(paid = false, day = 25, month = 10)
        assertEquals(RowHighlight.SOON, PaymentDates.rowHighlight(soon, today))

        val later = item(paid = false, day = 5, month = 11)
        assertEquals(RowHighlight.NONE, PaymentDates.rowHighlight(later, today))
    }

    @Test
    fun periodNextMonth_example() {
        val (from, to) = PaymentDates.periodNextMonth(LocalDate.of(2026, 10, 23))
        assertEquals(LocalDate.of(2026, 10, 23), from)
        assertEquals(LocalDate.of(2026, 11, 22), to)
    }

    @Test
    fun formatMoney_includesRubleSign() {
        assertEquals("1234 ₽", SumToPay.formatMoney(1234.0))
        assertEquals("12.50 ₽", SumToPay.formatMoney(12.5))
    }

    @Test
    fun dayHighlights_includeProjectedFutureMonth() {
        val all = listOf(item(amount = 100.0, day = 10, month = 10, year = 2026, debt = null))
        val today = LocalDate.of(2026, 10, 23)
        val nov = SumToPay.dayHighlightsForMonth(all, java.time.YearMonth.of(2026, 11), today)
        assertEquals(setOf(RowHighlight.NONE), nov[10])
    }

    @Test
    fun capitalizeFirstWord_russian() {
        assertEquals("Аренда", TextFormat.capitalizeFirstWord("аренда"))
        assertEquals("Аренда жилья", TextFormat.capitalizeFirstWord("аренда жилья"))
        assertEquals("ЖКХ", TextFormat.capitalizeFirstWord("ЖКХ"))
        assertEquals("  Плата", TextFormat.capitalizeFirstWord("  плата"))
    }
}
