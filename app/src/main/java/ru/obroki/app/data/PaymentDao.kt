package ru.obroki.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY dueYear ASC, dueMonth ASC, dueDay ASC")
    fun observeAll(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments")
    suspend fun getAll(): List<PaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PaymentEntity): Long

    @Update
    suspend fun update(entity: PaymentEntity)

    @Delete
    suspend fun delete(entity: PaymentEntity)

    @Query(
        """
        DELETE FROM payments
        WHERE isPaid = 1
          AND NOT (dueYear = :year AND dueMonth = :month)
        """
    )
    suspend fun clearPaidExceptMonth(year: Int, month: Int): Int
}
