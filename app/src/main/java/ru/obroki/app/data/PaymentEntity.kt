package ru.obroki.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.obroki.app.domain.PaymentItem

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val payee: String,
    val amount: Double,
    val debt: Double?,
    val dueDay: Int,
    val dueYear: Int,
    val dueMonth: Int,
    val isPaid: Boolean,
    val createdAt: Long,
)

fun PaymentEntity.toDomain(): PaymentItem = PaymentItem(
    id = id,
    title = title,
    payee = payee,
    amount = amount,
    debt = debt,
    dueDay = dueDay,
    dueYear = dueYear,
    dueMonth = dueMonth,
    isPaid = isPaid,
    createdAt = createdAt,
)

fun PaymentItem.toEntity(): PaymentEntity = PaymentEntity(
    id = id,
    title = title,
    payee = payee,
    amount = amount,
    debt = debt,
    dueDay = dueDay,
    dueYear = dueYear,
    dueMonth = dueMonth,
    isPaid = isPaid,
    createdAt = createdAt,
)
