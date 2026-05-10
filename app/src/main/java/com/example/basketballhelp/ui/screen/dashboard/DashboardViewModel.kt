package com.example.basketballhelp.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basketballhelp.domain.model.AiPracticePlanResult
import com.example.basketballhelp.domain.repository.AiCoachingRepository
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val playerRepository: PlayerRepository,
    private val sessionRepository: SessionRepository,
    private val goalRepository: GoalRepository,
    private val aiCoachingRepository: AiCoachingRepository,
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
            status.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    aiPracticePlan = if (aiCoachingRepository.isConfigured()) AiPracticePlanResult.Loading else AiPracticePlanResult.Disabled,
                    aiPracticePlanRefreshing = aiCoachingRepository.isConfigured(),
                    aiPracticePlanJustUpdated = false,
                )
            }
            runCatching {
                playerRepository.refresh()
                val playerId = playerRepository.getCurrentPlayer()?.id
                sessionRepository.refresh(playerId)
                goalRepository.refresh(playerId)
            }.onSuccess {
                status.update { it.copy(isLoading = false, error = null) }
                loadAiPracticePlan()
            }.onFailure { error ->
                status.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    fun refreshAiPracticePlan() {
        viewModelScope.launch {
            status.update {
                it.copy(
                    aiPracticePlan = if (aiCoachingRepository.isConfigured() && it.aiPracticePlan is AiPracticePlanResult.Disabled) {
                        AiPracticePlanResult.Loading
                    } else {
                        it.aiPracticePlan
                    },
                    aiPracticePlanRefreshing = aiCoachingRepository.isConfigured(),
                    aiPracticePlanJustUpdated = false,
                )
            }
            loadAiPracticePlan()
        }
    }

    private suspend fun loadAiPracticePlan() {
        if (!aiCoachingRepository.isConfigured()) {
            status.update { it.copy(aiPracticePlan = AiPracticePlanResult.Disabled, aiPracticePlanRefreshing = false) }
            return
        }
        val currentState = uiState.value
        val player = currentState.player
        if (player == null || currentState.sessions.isEmpty()) {
            status.update {
                it.copy(
                    aiPracticePlan = AiPracticePlanResult.Error("Log a session to unlock AI practice planning."),
                    aiPracticePlanRefreshing = false,
                )
            }
            return
        }
        val result = aiCoachingRepository.generatePracticePlan(
            player = player,
            sessions = currentState.sessions,
            goals = currentState.goals,
            fallbackRecommendations = currentState.recommendations.ifEmpty { SessionAnalytics.recommendations(currentState.sessions) },
        )
        status.update {
            it.copy(
                aiPracticePlan = result,
                aiPracticePlanRefreshing = false,
                aiPracticePlanUpdatedAt = if (result is AiPracticePlanResult.Success) System.currentTimeMillis() else it.aiPracticePlanUpdatedAt,
                aiPracticePlanJustUpdated = result is AiPracticePlanResult.Success,
            )
        }
        if (result is AiPracticePlanResult.Success) {
            delay(3_000)
            status.update { current ->
                current.copy(aiPracticePlanJustUpdated = false)
            }
        }
    }
}
