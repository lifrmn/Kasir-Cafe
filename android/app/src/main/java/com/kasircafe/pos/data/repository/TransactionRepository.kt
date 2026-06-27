package com.kasircafe.pos.data.repository

import com.kasircafe.pos.domain.model.Transaction
import com.kasircafe.pos.domain.model.TransactionDetail
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>
    fun observePendingCount(): Flow<Int>
    suspend fun refreshTransactions()
    suspend fun syncPendingTransactions(): Int

    /**
     * Membuat transaksi. Mengembalikan true jika berhasil dikirim ke backend,
     * atau false jika koneksi gagal dan transaksi disimpan ke antrian offline.
     */
    suspend fun createTransaction(
        total: Long,
        diskon: Long,
        pajak: Long,
        grandTotal: Long,
        metodePembayaran: String,
        kasir: String,
        detail: List<TransactionDetail>
    ): Boolean
}
