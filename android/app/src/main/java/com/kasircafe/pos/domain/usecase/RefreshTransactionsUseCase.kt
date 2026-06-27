package com.kasircafe.pos.domain.usecase

import com.kasircafe.pos.data.repository.TransactionRepository
import javax.inject.Inject

class RefreshTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke() = repository.refreshTransactions()
}
