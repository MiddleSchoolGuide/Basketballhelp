package com.example.basketballhelp.domain.repository

import com.example.basketballhelp.domain.model.DrillWithCompletion
import com.example.basketballhelp.domain.model.Goal
import com.example.basketballhelp.domain.model.Player
import com.example.basketballhelp.domain.model.Session
import com.example.basketballhelp.domain.model.AiPracticePlanResult
import com.example.basketballhelp.domain.usecase.RecommendationCard
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun observePlayers(): Flow<List<Player>>
    fun observePlayer(): Flow<Player?>
    suspend fun refresh(playerId: Int? = null)
    suspend fun getPlayers(): List<Player>
    suspend fun getCurrentPlayer(): Player?
    suspend fun getPlayer(playerId: Int): Player?
    suspend fun selectPlayer(playerId: Int)
    suspend fun upsert(player: Player): Player
    suspend fun deletePlayer(playerId: Int)
}

interface SessionRepository {
    fun observeSessions(): Flow<List<Session>>
    fun observeLatestSession(): Flow<Session?>
    suspend fun refresh(playerId: Int? = null, limit: Int? = null)
    suspend fun getSessions(): List<Session>
    suspend fun getSessionById(sessionId: Int): Session?
    suspend fun saveSession(session: Session): Int
    suspend fun deleteSession(session: Session)
}

interface GoalRepository {
    fun observeGoals(): Flow<List<Goal>>
    suspend fun refresh(playerId: Int? = null)
    suspend fun getGoals(): List<Goal>
    suspend fun createGoal(goal: Goal): Goal
}

interface DrillRepository {
    fun observeDailyChecklist(date: String): Flow<List<DrillWithCompletion>>
    suspend fun refresh(date: String)
    suspend fun toggleCompletion(drillId: Int, date: String, completed: Boolean)
}

interface AiCoachingRepository {
    fun isConfigured(): Boolean
    suspend fun generatePracticePlan(
        player: Player,
        sessions: List<Session>,
        goals: List<Goal>,
        fallbackRecommendations: List<RecommendationCard>,
    ): AiPracticePlanResult
}
