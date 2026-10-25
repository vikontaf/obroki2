package ru.obroki.app.domain

import java.time.LocalDate
import java.time.YearMonth

object IncomeProjection {

    /**
     * Sum of scheduled income occurrences whose effective date falls in [from, to].
     * Day-of-month uses the same last-day-of-month rollover as payments.
     */
    fun calculate(receipts: List<IncomeReceipt>, from: LocalDate, to: LocalDate): Double {
        if (receipts.isEmpty() || to.isBefore(from)) return 0.0

        var sum = 0.0
        var ym = YearMonth.from(from)
        val endYm = YearMonth.from(to)
        while (!ym.isAfter(endYm)) {
            for (receipt in receipts) {
                if (receipt.dayOfMonth !in 1..31 || receipt.amount <= 0.0) continue
                val eff = PaymentDates.effectiveDate(ym.year, ym.monthValue, receipt.dayOfMonth)
                if (!eff.isBefore(from) && !eff.isAfter(to)) {
                    sum += receipt.amount
                }
            }
            ym = ym.plusMonths(1)
        }
        return sum
    }
}
