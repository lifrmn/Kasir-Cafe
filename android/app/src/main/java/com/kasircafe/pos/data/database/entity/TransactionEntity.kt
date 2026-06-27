package com.kasircafe.pos.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tanggal: String,
    val total: Long,
    val diskon: Long,
    val pajak: Long,
    val grandTotal: Long,
    val metodePembayaran: String,
    val kasir: String
)

@Entity(tableName = "transaction_details", primaryKeys = ["transactionId", "produkId"])
data class TransactionDetailEntity(
    val transactionId: Long,
    val produkId: Long,
    val jumlah: Int,
    val harga: Long,
    val subtotal: Long
)
