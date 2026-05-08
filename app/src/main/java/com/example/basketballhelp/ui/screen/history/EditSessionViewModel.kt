package com.example.basketballhelp.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basketballhelp.domain.repository.SessionRepository
import com.example.basketballhelp.domain.usecase.SessionValidation
import com.example.basketballhelp.ui.screen.log.SessionFormState
import com.example.basketballhelp.ui.screen.log.toFormState
import com.example.basketballhelp.ui.screen.log.toSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditSessionUiState(
    val form: SessionFormState = SessionFormState(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

class EditSessionViewModel(
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditSessionUiState())
    val uiState: StateFlow<EditSessionUiState> = _uiState.asStateFlow()

    fun load(sessionId: Int) {
        if (!_uiState.value.isLoading && _uiState.value.form.id == sessionId) return
        viewModelScope.launch {
            runCatching {
                sessionRepository.getSessionById(sessionId)
            }.onSuccess { session ->
                _uiState.update {
                    it.copy(
                        form = session?.toFormState() ?: SessionFormState(),
                        isLoading = false,
                        error = if (session == null) "Session not found." else null,
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    fun updateForm(transform: (SessionFormState) -> SessionFormState) {
        _uiState.update { it.copy(form = transform(it.form), error = null) }
    }

    fun save() {
        val error = SessionValidation.validate(_uiState.value.form)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }
        viewModelScope.launch {
            runCatching {
                val existing = sessionRepository.getSessionById(_uiState.value.form.id) ?: error("Session not found.")
                _uiState.update { it.copy(isSaving = true) }
                sessionRepository.saveSession(_uiState.value.form.toSession(createdAt = existing.createdAt, updatedAt = System.currentTimeMillis()))
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, saved = true) }
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, error = error.message) }
            }
        }
    }
}
