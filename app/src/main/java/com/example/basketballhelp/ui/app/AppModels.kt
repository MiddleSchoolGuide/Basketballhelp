package com.example.basketballhelp.ui.app

import com.example.basketballhelp.domain.model.AiPracticePlanResult
import com.example.basketballhelp.domain.model.Goal
import com.example.basketballhelp.domain.model.Player
import com.example.basketballhelp.domain.model.Session
import com.example.basketballhelp.domain.usecase.GoalProgress
import com.example.basketballhelp.domain.usecase.RecommendationCard

data class DashboardUiState(
    val player: Player? = null,
    val sessions: List<Session> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val goalProgress: List<GoalProgress> = emptyList(),
    val recommendations: List<RecommendationCard> = emptyList(),
    val aiPracticePlan: AiPracticePlanResult = AiPracticePlanResult.Disabled,
    val aiPracticePlanRefreshing: Boolean = false,
    val aiPracticePlanUpdatedAt: Long? = null,
    val aiPracticePlanJustUpdated: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
)
