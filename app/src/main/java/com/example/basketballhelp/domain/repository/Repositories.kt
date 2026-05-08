package com.example.basketballhelp.domain.repository

import com.example.basketballhelp.domain.model.DrillWithCompletion
import com.example.basketballhelp.domain.model.Goal
import com.example.basketballhelp.domain.model.Player
import com.example.basketballhelp.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun observePlayer(): Flow<Player?>
    suspend fun refresh(playerId: Int = 1)
    suspend fun getPlayer(playerId: Int = 1): Player?
    suspend fun upsert(player: Player)
}

interface SessionRepository {
    fun observeSessions(): Flow<List<Session>>
    fun observeLatestSession(): Flow<Session?>
    suspend fun refresh(playerId: Int = 1, limit: Int? = null)
    suspend fun getSessions(): List<Session>
    suspend fun getSessionById(sessionId: Int): Session?
    suspend fun saveSession(session: Session): Int
    suspend fun deleteSession(session: Session)
}

interface GoalRepository {
    fun observeGoals(): Flow<List<Goal>>
    suspend fun refresh(playerId: Int = 1)
    suspend fun getGoals(): List<Goal>
    suspend fun createGoal(goal: Goal): Goal
}

interface DrillRepository {
    fun observeDailyChecklist(date: String): Flow<List<DrillWithCompletion>>
    suspend fun refresh(date: String)
    suspend fun toggleCompletion(drillId: Int, date: String, completed: Boolean)
}
