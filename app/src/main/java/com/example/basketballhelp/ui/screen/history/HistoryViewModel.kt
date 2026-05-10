package com.example.basketballhelp.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basketballhelp.domain.model.Session
import com.example.basketballhelp.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val sessions: List<Session> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class HistoryViewModel(
    private val playerRepository: com.example.basketballhelp.domain.repository.PlayerRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val sessions: StateFlow<List<Session>> = sessionRepository.observeSessions()
        .map { it.sortedByDescending(Session::sessionDate) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            sessions.collect { items ->
                _uiState.update { it.copy(sessions = items) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                playerRepository.refresh()
                sessionRepository.refresh(playerRepository.getCurrentPlayer()?.id)
            }
                .onSuccess { _uiState.update { it.copy(isLoading = false, error = null) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, error = error.message) } }
        }
    }

    fun deleteSession(session: Session) {
        viewModelScope.launch {
            runCatching {
                sessionRepository.deleteSession(session)
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }
        }
    }
}
