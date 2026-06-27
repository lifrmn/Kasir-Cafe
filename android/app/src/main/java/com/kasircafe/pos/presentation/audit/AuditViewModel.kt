package com.kasircafe.pos.presentation.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasircafe.pos.data.repository.AuditRepository
import com.kasircafe.pos.domain.model.AuditSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuditUiState(
    val isLoading: Boolean = true,
    val summary: AuditSummary? = null,
    val error: String = ""
)

@HiltViewModel
class AuditViewModel @Inject constructor(
    private val repository: AuditRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuditUiState())
    val uiState: StateFlow<AuditUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    fun loadSummary(days: Int = 30) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = "") }
            runCatching { repository.getSummary(days) }
                .onSuccess { summary ->
                    _uiState.update { it.copy(isLoading = false, summary = summary, error = "") }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error = throwable.message ?: "Gagal memuat observability")
                    }
                }
        }
    }
}
