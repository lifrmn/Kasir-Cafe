package com.kasircafe.pos.data.repository

import com.kasircafe.pos.domain.model.AuditSummary

interface AuditRepository {
    suspend fun getSummary(days: Int = 30): AuditSummary
}
