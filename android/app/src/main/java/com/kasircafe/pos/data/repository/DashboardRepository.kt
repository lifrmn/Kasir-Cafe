package com.kasircafe.pos.data.repository

import com.kasircafe.pos.domain.model.DashboardSummary

interface DashboardRepository {
    suspend fun getDailySummary(): DashboardSummary
}
