package com.kasircafe.pos.domain.usecase

import com.kasircafe.pos.data.repository.TransactionRepository
import com.kasircafe.pos.domain.model.TransactionDetail
import javax.inject.Inject

class CreateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    /**
     * @return true jika transaksi terkirim ke backend, false jika disimpan ke antrian offline.
     */
    suspend operator fun invoke(
        total: Long,
        diskon: Long,
        pajak: Long,
        grandTotal: Long,
        metodePembayaran: String,
        kasir: String,
        detail: List<TransactionDetail>
    ): Boolean = repository.createTransaction(
        total = total,
        diskon = diskon,
        pajak = pajak,
        grandTotal = grandTotal,
        metodePembayaran = metodePembayaran,
        kasir = kasir,
        detail = detail
    )
}
