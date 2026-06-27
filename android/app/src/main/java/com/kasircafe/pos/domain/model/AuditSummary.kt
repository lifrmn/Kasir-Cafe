package com.kasircafe.pos.domain.model

data class AuditSummary(
    val totalEvents: Int,
    val totalLogin: Int,
    val totalFailedLogin: Int,
    val totalLogout: Int,
    val totalRefresh: Int,
    val failedLoginPerDay: List<FailedLoginPerDay>,
    val topIpAddresses: List<TopIpAddress>
)

data class FailedLoginPerDay(
    val day: String,
    val total: Int
)

data class TopIpAddress(
    val ipAddress: String,
    val total: Int
)
