package com.kasircafe.pos.data.repository.impl

import com.kasircafe.pos.data.api.PosApiService
import com.kasircafe.pos.data.api.model.TransactionCreateRequest
import com.kasircafe.pos.data.database.dao.TransactionDao
import com.kasircafe.pos.data.database.entity.TransactionDetailEntity
import com.kasircafe.pos.data.mapper.toDomain
import com.kasircafe.pos.data.mapper.toDto
import com.kasircafe.pos.data.mapper.toEntity
import com.kasircafe.pos.data.repository.TransactionRepository
import com.kasircafe.pos.domain.model.Transaction
import com.kasircafe.pos.domain.model.TransactionDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val api: PosApiService,
    private val dao: TransactionDao
) : TransactionRepository {
    override fun observeTransactions(): Flow<List<Transaction>> =
        dao.observeTransactions().map { list -> list.map { it.toDomain() } }

    override suspend fun refreshTransactions() {
        val remote = api.getTransactions().map { it.toDomain().toEntity() }
        remote.forEach { dao.insertTransaction(it) }
    }

    override suspend fun createTransaction(
        total: Long,
        diskon: Long,
        pajak: Long,
        grandTotal: Long,
        metodePembayaran: String,
        kasir: String,
        detail: List<TransactionDetail>
    ) {
        val request = TransactionCreateRequest(
            total = total,
            diskon = diskon,
            pajak = pajak,
            grand_total = grandTotal,
            metode_pembayaran = metodePembayaran,
            kasir = kasir,
            detail = detail.map { it.toDto() }
        )

        val created = api.createTransaction(request).toDomain()
        val transactionId = dao.insertTransaction(created.toEntity())
        dao.insertDetails(
            detail.map {
                TransactionDetailEntity(
                    transactionId = transactionId,
                    produkId = it.produkId,
                    jumlah = it.jumlah,
                    harga = it.harga,
                    subtotal = it.subtotal
                )
            }
        )
    }
}
