package com.kasircafe.pos.presentation.audit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kasircafe.pos.domain.model.AuditSummary

@Composable
fun AuditScreen(
    viewModel: AuditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Observability Auth", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Ringkasan 30 hari terakhir",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (state.isLoading) {
            CircularProgressIndicator()
        }

        if (state.error.isNotBlank()) {
            Text(state.error, color = MaterialTheme.colorScheme.error)
        }

        state.summary?.let { summary ->
            AuditContent(summary)
        }

        OutlinedButton(onClick = { viewModel.loadSummary() }, modifier = Modifier.fillMaxWidth()) {
            Text("Muat Ulang")
        }
    }
}

@Composable
private fun AuditContent(summary: AuditSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricCard(modifier = Modifier.weight(1f), title = "Total Event", value = summary.totalEvents.toString())
        MetricCard(modifier = Modifier.weight(1f), title = "Login", value = summary.totalLogin.toString())
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricCard(
            modifier = Modifier.weight(1f),
            title = "Failed Login",
            value = summary.totalFailedLogin.toString(),
            danger = true
        )
        MetricCard(modifier = Modifier.weight(1f), title = "Logout", value = summary.totalLogout.toString())
    }
    MetricCard(modifier = Modifier.fillMaxWidth(), title = "Refresh", value = summary.totalRefresh.toString())

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Failed Login per Hari", style = MaterialTheme.typography.titleMedium)
            if (summary.failedLoginPerDay.isEmpty()) {
                Text("Tidak ada failed login.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                summary.failedLoginPerDay.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.day.take(10))
                        Text(item.total.toString(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Top IP Address", style = MaterialTheme.typography.titleMedium)
            if (summary.topIpAddresses.isEmpty()) {
                Text("Belum ada data IP.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                summary.topIpAddresses.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.ipAddress)
                        Text(item.total.toString(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    danger: Boolean = false
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
