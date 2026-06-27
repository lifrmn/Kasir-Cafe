package com.kasircafe.pos.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasircafe.pos.data.local.SessionDataStore
import com.kasircafe.pos.data.repository.DashboardRepository
import com.kasircafe.pos.domain.model.DashboardSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val summary: DashboardSummary = DashboardSummary(0, 0, 0),
    val isAdmin: Boolean = false,
    val error: String = ""
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
        observeRole()
    }

    private fun observeRole() {
        viewModelScope.launch {
            sessionDataStore.roleFlow.collect { role ->
                _uiState.update { it.copy(isAdmin = role == "admin") }
            }
        }
    }

    fun loadSummary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = "") }
            runCatching { repository.getDailySummary() }
                .onSuccess { summary ->
                    _uiState.update { it.copy(isLoading = false, summary = summary, error = "") }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error = throwable.message ?: "Gagal memuat dashboard")
                    }
                }
        }
    }
}
