package com.kasircafe.pos.domain.usecase

import com.kasircafe.pos.data.repository.TransactionRepository
import com.kasircafe.pos.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<Transaction>> = repository.observeTransactions()
}
