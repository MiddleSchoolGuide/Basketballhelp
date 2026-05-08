package com.example.basketballhelp.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basketballhelp.domain.repository.GoalRepository
import com.example.basketballhelp.domain.repository.PlayerRepository
import com.example.basketballhelp.domain.repository.SessionRepository
import com.example.basketballhelp.domain.usecase.SessionAnalytics
import com.example.basketballhelp.ui.app.DashboardUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val playerRepository: PlayerRepository,
    private val sessionRepository: SessionRepository,
    private val goalRepository: GoalRepository,
) : ViewModel() {
    private val status = MutableStateFlow(DashboardUiState())

    val uiState = combine(
        status,
        playerRepository.observePlayer(),
        sessionRepository.observeSessions(),
        goalRepository.observeGoals(),
    ) { statusState, player, sessions, goals ->
        statusState.copy(
            player = player,
            sessions = sessions.sortedByDescending { it.sessionDate },
            goals = goals,
            goalProgress = goals.map { SessionAnalytics.goalProgress(it, sessions) },
            recommendations = SessionAnalytics.recommendations(sessions),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            status.update { it.copy(isLoading = true, error = null) }
            runCatching {
                playerRepository.refresh(1)
                sessionRepository.refresh(1)
                goalRepository.refresh(1)
            }.onSuccess {
                status.update { it.copy(isLoading = false, error = null) }
            }.onFailure { error ->
                status.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }
}
