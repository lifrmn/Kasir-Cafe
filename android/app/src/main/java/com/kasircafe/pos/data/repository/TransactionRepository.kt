package com.kasircafe.pos.data.repository

import com.kasircafe.pos.domain.model.Transaction
import com.kasircafe.pos.domain.model.TransactionDetail
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>
    suspend fun refreshTransactions()
    suspend fun createTransaction(
        total: Long,
        diskon: Long,
        pajak: Long,
        grandTotal: Long,
        metodePembayaran: String,
        kasir: String,
        detail: List<TransactionDetail>
    )
}
