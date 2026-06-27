package com.kasircafe.pos.data.repository.impl

import com.kasircafe.pos.data.api.PosApiService
import com.kasircafe.pos.data.repository.DashboardRepository
import com.kasircafe.pos.domain.model.DashboardSummary
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val api: PosApiService
) : DashboardRepository {
    override suspend fun getDailySummary(): DashboardSummary {
        val report = api.getReport()
        return DashboardSummary(
            totalPenjualan = report.harian.total_penjualan,
            transaksi = report.harian.transaksi,
            profit = report.harian.profit
        )
    }
}
