package com.example.basketballhelp.ui.screen.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basketballhelp.domain.repository.PlayerRepository
import com.example.basketballhelp.domain.repository.SessionRepository
import com.example.basketballhelp.domain.usecase.SessionValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LogSessionUiState(
    val form: SessionFormState = SessionFormState(),
    val currentStep: Int = 0,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
)

class LogSessionViewModel(
    playerRepository: PlayerRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LogSessionUiState())
    val uiState: StateFlow<LogSessionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                playerRepository.getPlayer()
            }.onSuccess { player ->
                player?.let {
                    _uiState.update { state -> state.copy(form = state.form.copy(playerId = it.id)) }
                }
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }
        }
    }

    fun updateForm(transform: (SessionFormState) -> SessionFormState) {
        _uiState.update { it.copy(form = transform(it.form), error = null) }
    }

    fun nextStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep + 1).coerceAtMost(4)) }
    }

    fun previousStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }
    }

    fun save() {
        val form = _uiState.value.form
        val error = SessionValidation.validate(form)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val now = System.currentTimeMillis()
            runCatching {
                sessionRepository.saveSession(form.toSession(createdAt = now, updatedAt = now))
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, saved = true) }
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, error = error.message) }
            }
        }
    }

    fun clearSaved() {
        _uiState.update { it.copy(saved = false) }
    }
}
