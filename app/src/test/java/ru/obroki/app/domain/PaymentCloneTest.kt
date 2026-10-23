package ru.obroki.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentCloneTest {

    private fun item(
        debt: Double? = null,
        amount: Double = 100.0,
        day: Int = 15,
        year: Int = 2026,
        month: Int = 10,
        paid: Boolean = false,
        title: String = "Аренда",
        payee: String = "Иван",
    ) = PaymentItem(
        id = 1,
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
    fun unpaidWithoutDebt_createsCloneWithNullDebt() {
        val result = PaymentClone.onTapPaid(item(debt = null), emptyList())
        assertTrue(result.markPaid)
        assertTrue(result.createClone)
        assertNotNull(result.clone)
        assertNull(result.clone!!.debt)
        assertEquals(2026, result.clone!!.dueYear)
        assertEquals(11, result.clone!!.dueMonth)
        assertFalse(result.clone!!.isPaid)
    }

    @Test
    fun unpaidWithDebt_stopsWhenDebtExhausted() {
        val result = PaymentClone.onTapPaid(item(debt = 80.0, amount = 100.0), emptyList())
        assertTrue(result.markPaid)
        assertFalse(result.createClone)
        assertNull(result.clone)
    }

    @Test
    fun unpaidWithDebt_clonesWithReducedDebt() {
        val result = PaymentClone.onTapPaid(item(debt = 250.0, amount = 100.0), emptyList())
        assertTrue(result.createClone)
        assertEquals(150.0, result.clone!!.debt!!, 0.001)
        assertEquals(100.0, result.clone!!.amount, 0.001)
    }

    @Test
    fun unpaidWithDebt_remainingLessThanAmount_clonesWithReducedAmount() {
        // debt' = 150 − 100 = 50 < amount → last installment amount = debt'
        val result = PaymentClone.onTapPaid(item(debt = 150.0, amount = 100.0), emptyList())
        assertTrue(result.createClone)
        assertEquals(50.0, result.clone!!.debt!!, 0.001)
        assertEquals(50.0, result.clone!!.amount, 0.001)
    }

    @Test
    fun unpaidWithDebt_remainingEqualsAmount_keepsSeriesAmount() {
        val result = PaymentClone.onTapPaid(item(debt = 200.0, amount = 100.0), emptyList())
        assertTrue(result.createClone)
        assertEquals(100.0, result.clone!!.debt!!, 0.001)
        assertEquals(100.0, result.clone!!.amount, 0.001)
    }

    @Test
    fun cloneAmount_capsAtRemainingDebt() {
        assertEquals(100.0, PaymentClone.cloneAmount(100.0, null), 0.001)
        assertEquals(100.0, PaymentClone.cloneAmount(100.0, 150.0), 0.001)
        assertEquals(40.0, PaymentClone.cloneAmount(100.0, 40.0), 0.001)
    }

    @Test
    fun dedupPreventsSecondClone() {
        val existing = listOf(
            item(year = 2026, month = 11, paid = false).copy(id = 2),
        )
        val result = PaymentClone.onTapPaid(item(debt = null), existing)
        assertTrue(result.markPaid)
        assertFalse(result.createClone)
    }

    @Test
    fun dedupIgnoresCaseAndTrim() {
        val existing = listOf(
            item(title = " аренда ", payee = "ИВАН", year = 2026, month = 11).copy(id = 2),
        )
        val result = PaymentClone.onTapPaid(item(title = "Аренда", payee = "иван"), existing)
        assertFalse(result.createClone)
    }

    @Test
    fun paidTapOnlyUnmarks() {
        val result = PaymentClone.onTapPaid(item(paid = true), emptyList())
        assertFalse(result.markPaid)
        assertFalse(result.createClone)
    }

    @Test
    fun rollover_day31_toFebruary() {
        val result = PaymentClone.onTapPaid(
            item(day = 31, year = 2026, month = 1, debt = null),
            emptyList(),
        )
        assertTrue(result.createClone)
        assertEquals(2, result.clone!!.dueMonth)
        assertEquals(31, result.clone!!.dueDay) // desired day kept
        assertEquals(28, result.clone!!.effectiveDate.dayOfMonth) // 2026 not leap
    }
}
