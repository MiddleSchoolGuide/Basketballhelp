package com.example.basketballhelp.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basketballhelp.domain.model.Goal
import com.example.basketballhelp.domain.model.Player
import com.example.basketballhelp.domain.model.Session
import com.example.basketballhelp.domain.repository.GoalRepository
import com.example.basketballhelp.domain.repository.PlayerRepository
import com.example.basketballhelp.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val player: Player? = null,
    val sessions: List<Session> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class ProfileViewModel(
    private val playerRepository: PlayerRepository,
    private val sessionRepository: SessionRepository,
    private val goalRepository: GoalRepository,
) : ViewModel() {
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)

    val uiState = combine(
        playerRepository.observePlayer(),
        sessionRepository.observeSessions(),
        goalRepository.observeGoals(),
        _saving,
        _error,
    ) { player, sessions, goals, saving, error ->
        ProfileUiState(
            player = player,
            sessions = sessions.sortedByDescending { it.sessionDate },
            goals = goals,
            isLoading = saving && player == null && sessions.isEmpty() && goals.isEmpty(),
            error = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _saving.value = true
            _error.value = null
            runCatching {
                playerRepository.refresh(1)
                sessionRepository.refresh(1)
                goalRepository.refresh(1)
            }.onFailure {
                _error.value = it.message
            }
            _saving.value = false
        }
    }

    fun updatePlayer(name: String, age: Int, positionFocus: String, notes: String) {
        val player = uiState.value.player ?: return
        viewModelScope.launch {
            _saving.value = true
            runCatching {
                playerRepository.upsert(player.copy(name = name, age = age, positionFocus = positionFocus, notes = notes))
            }.onFailure {
                _error.value = it.message
            }
            _saving.value = false
        }
    }
}
