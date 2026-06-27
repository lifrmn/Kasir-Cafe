package com.kasircafe.pos.domain.usecase

import com.kasircafe.pos.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePendingTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<Int> = repository.observePendingCount()
}
