package com.kasircafe.pos.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kasircafe.pos.data.database.entity.PendingTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PendingTransactionEntity): Long

    @Query("SELECT * FROM pending_transactions ORDER BY id ASC")
    suspend fun getAll(): List<PendingTransactionEntity>

    @Query("DELETE FROM pending_transactions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM pending_transactions")
    fun observeCount(): Flow<Int>
}
