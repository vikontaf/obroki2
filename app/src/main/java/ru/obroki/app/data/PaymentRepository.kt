package ru.obroki.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.obroki.app.domain.PaymentClone
import ru.obroki.app.domain.PaymentItem
import java.time.LocalDate
import java.time.YearMonth

class PaymentRepository(private val dao: PaymentDao) {

    fun observePayments(): Flow<List<PaymentItem>> =
        dao.observeAll().map { list ->
            list.map { it.toDomain() }
                .sortedBy { it.effectiveDate }
        }

    suspend fun create(item: PaymentItem): Long =
        dao.insert(item.copy(id = 0).toEntity())

    suspend fun update(item: PaymentItem) {
        dao.update(item.toEntity())
    }

    suspend fun delete(item: PaymentItem) {
        dao.delete(item.toEntity())
    }

    suspend fun togglePaid(item: PaymentItem) {
        val all = dao.getAll().map { it.toDomain() }
        val result = PaymentClone.onTapPaid(item, all)
        dao.update(item.copy(isPaid = result.markPaid).toEntity())
        if (result.createClone && result.clone != null) {
            dao.insert(result.clone.toEntity())
        }
    }

    suspend fun clearPaidExceptCurrentMonth(today: LocalDate = LocalDate.now()) {
        val ym = YearMonth.from(today)
        dao.clearPaidExceptMonth(ym.year, ym.monthValue)
    }
}
