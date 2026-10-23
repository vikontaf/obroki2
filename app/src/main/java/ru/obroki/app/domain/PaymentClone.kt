package ru.obroki.app.domain

/**
 * Result of marking a payment as paid: whether to create a next-month clone,
 * and the clone fields if so.
 */
data class PayToggleResult(
    val markPaid: Boolean,
    val createClone: Boolean,
    val clone: PaymentItem? = null,
)

object PaymentClone {

    /**
     * Unpaid → paid: optionally create next-month clone.
     * Paid → unpaid: only flip flag; never touch clone.
     */
    fun onTapPaid(
        item: PaymentItem,
        existing: List<PaymentItem>,
    ): PayToggleResult {
        if (item.isPaid) {
            return PayToggleResult(markPaid = false, createClone = false)
        }

        val shouldClone = shouldCreateClone(item.debt, item.amount)
        if (!shouldClone) {
            return PayToggleResult(markPaid = true, createClone = false)
        }

        val (ny, nm) = PaymentDates.nextMonth(item.dueYear, item.dueMonth)
        val nextDebt = nextDebt(item.debt, item.amount)

        val duplicate = existing.any {
            PaymentDedup.matches(it, item.title, item.payee, item.dueDay, ny, nm)
        }
        if (duplicate) {
            return PayToggleResult(markPaid = true, createClone = false)
        }

        val clone = PaymentItem(
            id = 0L,
            title = item.title,
            payee = item.payee,
            amount = item.amount,
            debt = nextDebt,
            dueDay = item.dueDay,
            dueYear = ny,
            dueMonth = nm,
            isPaid = false,
        )
        return PayToggleResult(markPaid = true, createClone = true, clone = clone)
    }

    /**
     * debt null → always clone (clone debt null).
     * debt set → debt' = debt − amount; clone only if debt' > 0.
     */
    fun shouldCreateClone(debt: Double?, amount: Double): Boolean {
        if (debt == null) return true
        return debt - amount > 0.0
    }

    fun nextDebt(debt: Double?, amount: Double): Double? {
        if (debt == null) return null
        return debt - amount
    }
}
