package com.example.basketballhelp.ui.screen.drills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basketballhelp.domain.model.DrillWithCompletion
import com.example.basketballhelp.domain.repository.DrillRepository
import com.example.basketballhelp.util.todayIso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DrillsUiState(
    val checklist: List<DrillWithCompletion> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class DrillsViewModel(
    private val drillRepository: DrillRepository,
) : ViewModel() {
    private val date = todayIso()
    private val _uiState = MutableStateFlow(DrillsUiState())
    val uiState: StateFlow<DrillsUiState> = _uiState.asStateFlow()

    private val checklist: StateFlow<List<DrillWithCompletion>> = drillRepository.observeDailyChecklist(date)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            checklist.collect { items ->
                _uiState.update { it.copy(checklist = items) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { drillRepository.refresh(date) }
                .onSuccess { _uiState.update { it.copy(isLoading = false, error = null) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, error = error.message) } }
        }
    }

    fun toggle(drillId: Int, completed: Boolean) {
        viewModelScope.launch {
            runCatching {
                drillRepository.toggleCompletion(drillId, date, completed)
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }
        }
    }
}
