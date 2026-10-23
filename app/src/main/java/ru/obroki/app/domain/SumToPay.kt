package ru.obroki.app.domain

import java.time.LocalDate
import java.time.YearMonth

data class ActiveSeries(
    val title: String,
    val payee: String,
    val dueDay: Int,
    val amount: Double,
    /** Latest known instance year/month for this series. */
    val lastYear: Int,
    val lastMonth: Int,
    /** Remaining debt after the last known instance, or null if unlimited. */
    val debtAfterLast: Double?,
)

object SumToPay {

    /**
     * Sum of unpaid amounts in [from, to], including virtual projected months
     * for still-active series (per plan §7).
     */
    fun calculate(all: List<PaymentItem>, from: LocalDate, to: LocalDate): Double {
        if (to.isBefore(from)) return 0.0

        var sum = 0.0

        // 1) Real unpaid in window
        for (item in all) {
            if (item.isPaid) continue
            val d = item.effectiveDate
            if (!d.isBefore(from) && !d.isAfter(to)) {
                sum += item.amount
            }
        }

        // 2) Active series for projection
        val series = collectActiveSeries(all)
        for (s in series) {
            sum += projectSeries(s, all, from, to)
        }
        return sum
    }

    /**
     * Active series:
     * - all unpaid; plus
     * - for each (title,payee,dueDay) the latest by (year,month) if debt==null or debt>0
     *   (series still alive). Paid with debt≤0 do not continue the series.
     */
    fun collectActiveSeries(all: List<PaymentItem>): List<ActiveSeries> {
        val byKey = all.groupBy { PaymentDedup.seriesKey(it) }
        val result = mutableListOf<ActiveSeries>()

        for ((_, items) in byKey) {
            val latest = items.maxWithOrNull(
                compareBy<PaymentItem> { it.dueYear }.thenBy { it.dueMonth }
            ) ?: continue

            val alive = latest.debt == null || latest.debt > 0.0
            if (!alive) continue

            // debtAfterLast: after this instance is "accounted", remaining for next clones
            // For projection we start from the month AFTER latest; remaining debt for next
            // virtual payment is: if debt null → null; else debt (still on latest unpaid)
            // or debt-amount if latest is paid... Plan says:
            // "моделировать убывание debt − amount на каждый виртуальный (и уже учтённый будущий) платёж"
            // Start after last known instance. If latest unpaid with debt D, next clone gets D-amount
            // when paid. For projection of future months not in DB:
            // - Start month = next after last known
            // - If debt set on last: remaining for first virtual = if last unpaid: last.debt (the
            //   virtual is a future payment of the series — wait, re-read plan)
            //
            // Plan §7:
            // "стартовать с месяца следующего после последнего известного экземпляра этой серии"
            // "Если у серии задан debt: моделировать убывание debt − amount на каждый виртуальный
            // (и уже учтённый будущий) платёж; как только следующий debt' ≤ 0 — дальше не проектируем"
            //
            // So remaining debt walking forward starts from the debt on the last known instance.
            // First virtual step: debt' = debt - amount; if debt' <= 0 stop before creating that
            // virtual (same as clone rule). Actually: "на каждый виртуальный" — apply debt-amount
            // when including each virtual; stop when next would have debt' <= 0.
            //
            // Clone rule: when paying instance with debt D, clone gets D-amount, and only if >0.
            // So from last known with debt D:
            // - Next month instance would have debt = D - amount (if last were paid)
            // - If last is unpaid, it's still "the" instance for that month; projection starts
            //   AFTER it, so first projected has debt = D - amount.
            //
            // If last.debt is null → unlimited.
            // If last.debt > 0 → first projected needs debt' = last.debt - amount > 0 to exist.

            result.add(
                ActiveSeries(
                    title = latest.title,
                    payee = latest.payee,
                    dueDay = latest.dueDay,
                    amount = latest.amount,
                    lastYear = latest.dueYear,
                    lastMonth = latest.dueMonth,
                    debtAfterLast = latest.debt,
                )
            )
        }
        return result
    }

    private fun projectSeries(
        series: ActiveSeries,
        all: List<PaymentItem>,
        from: LocalDate,
        to: LocalDate,
    ): Double {
        var sum = 0.0
        var ym = YearMonth.of(series.lastYear, series.lastMonth).plusMonths(1)
        var remainingDebt = series.debtAfterLast

        while (true) {
            // Clone gate: if debt was set, next instance only if remainingDebt - amount > 0
            // remainingDebt here is debt on previous instance; next clone debt' = remaining - amount
            if (remainingDebt != null) {
                val nextDebt = remainingDebt - series.amount
                if (nextDebt <= 0.0) break
            }

            val eff = PaymentDates.effectiveDate(ym.year, ym.monthValue, series.dueDay)
            if (eff.isAfter(to)) break

            val existsInDb = all.any {
                PaymentDedup.matches(
                    it,
                    series.title,
                    series.payee,
                    series.dueDay,
                    ym.year,
                    ym.monthValue,
                )
            }

            if (!existsInDb && !eff.isBefore(from)) {
                sum += series.amount
            }

            // Advance debt as if this (virtual or skipped-existing) payment consumed amount
            // Plan: model decrease on each virtual AND already-accounted future payment
            if (remainingDebt != null) {
                remainingDebt = remainingDebt - series.amount
            }

            ym = ym.plusMonths(1)
        }
        return sum
    }

    fun formatMoney(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.2f", value)
        }
    }
}
