package com.kasircafe.pos.data.repository.impl

import com.kasircafe.pos.data.api.PosApiService
import com.kasircafe.pos.data.api.model.TransactionCreateRequest
import com.kasircafe.pos.data.database.dao.PendingTransactionDao
import com.kasircafe.pos.data.database.dao.TransactionDao
import com.kasircafe.pos.data.database.entity.PendingTransactionEntity
import com.kasircafe.pos.data.database.entity.TransactionDetailEntity
import com.kasircafe.pos.data.mapper.toDomain
import com.kasircafe.pos.data.mapper.toDto
import com.kasircafe.pos.data.mapper.toEntity
import com.kasircafe.pos.data.repository.TransactionRepository
import com.kasircafe.pos.domain.model.Transaction
import com.kasircafe.pos.domain.model.TransactionDetail
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val api: PosApiService,
    private val dao: TransactionDao,
    private val pendingDao: PendingTransactionDao,
    private val moshi: Moshi
) : TransactionRepository {
    private val requestAdapter = moshi.adapter(TransactionCreateRequest::class.java)

    override fun observeTransactions(): Flow<List<Transaction>> =
        dao.observeTransactions().map { list -> list.map { it.toDomain() } }

    override fun observePendingCount(): Flow<Int> = pendingDao.observeCount()

    override suspend fun refreshTransactions() {
        runCatching { syncPendingTransactions() }
        val remote = api.getTransactions().map { it.toDomain().toEntity() }
        remote.forEach { dao.insertTransaction(it) }
    }

    override suspend fun syncPendingTransactions(): Int {
        var synced = 0
        for (item in pendingDao.getAll()) {
            val request = runCatching { requestAdapter.fromJson(item.payload) }.getOrNull()
            if (request == null) {
                pendingDao.delete(item.id)
                continue
            }
            try {
                persistRemote(api.createTransaction(request).toDomain(), request)
                pendingDao.delete(item.id)
                synced++
            } catch (e: IOException) {
                break
            }
        }
        return synced
    }

    override suspend fun createTransaction(
        total: Long,
        diskon: Long,
        pajak: Long,
        grandTotal: Long,
        metodePembayaran: String,
        kasir: String,
        detail: List<TransactionDetail>
    ): Boolean {
        val request = TransactionCreateRequest(
            total = total,
            diskon = diskon,
            pajak = pajak,
            grand_total = grandTotal,
            metode_pembayaran = metodePembayaran,
            kasir = kasir,
            detail = detail.map { it.toDto() }
        )

        return try {
            persistRemote(api.createTransaction(request).toDomain(), request)
            true
        } catch (e: IOException) {
            pendingDao.insert(PendingTransactionEntity(payload = requestAdapter.toJson(request)))
            false
        }
    }

    private suspend fun persistRemote(created: Transaction, request: TransactionCreateRequest) {
        val transactionId = dao.insertTransaction(created.toEntity())
        dao.insertDetails(
            request.detail.map {
                TransactionDetailEntity(
                    transactionId = transactionId,
                    produkId = it.produk_id,
                    jumlah = it.jumlah,
                    harga = it.harga,
                    subtotal = it.subtotal
                )
            }
        )
    }
}
