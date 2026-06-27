package com.kasircafe.pos.data.repository.impl

import com.kasircafe.pos.data.api.PosApiService
import com.kasircafe.pos.data.mapper.toDomain
import com.kasircafe.pos.data.repository.AuditRepository
import com.kasircafe.pos.domain.model.AuditSummary
import javax.inject.Inject

class AuditRepositoryImpl @Inject constructor(
    private val api: PosApiService
) : AuditRepository {
    override suspend fun getSummary(days: Int): AuditSummary =
        api.getAuthAuditSummary(days).toDomain()
}
