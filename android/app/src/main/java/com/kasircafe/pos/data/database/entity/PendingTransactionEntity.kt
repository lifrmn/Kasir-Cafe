package com.kasircafe.pos.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_transactions")
data class PendingTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val payload: String,
    val createdAt: Long = System.currentTimeMillis()
)
