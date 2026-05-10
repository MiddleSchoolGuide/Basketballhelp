package com.example.basketballhelp.data.repository

import com.example.basketballhelp.data.network.service.HoopDevApiService
import com.example.basketballhelp.data.network.util.toApiException
import com.example.basketballhelp.data.network.util.toDomain
import com.example.basketballhelp.data.network.util.toPayload
import com.example.basketballhelp.domain.model.Goal
import com.example.basketballhelp.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GoalRepositoryImpl(
    private val api: HoopDevApiService,
) : GoalRepository {
    private val goalsFlow = MutableStateFlow<List<Goal>>(emptyList())

    override fun observeGoals(): Flow<List<Goal>> = goalsFlow.asStateFlow()

    override suspend fun refresh(playerId: Int?) {
        runCatching {
            goalsFlow.value = if (playerId == null) {
                emptyList()
            } else {
                api.getGoals(playerId).map { it.toDomain() }
            }
        }.getOrElse { throw it.toApiException() }
    }

    override suspend fun getGoals(): List<Goal> = goalsFlow.value

    override suspend fun createGoal(goal: Goal): Goal {
        return runCatching {
            api.createGoal(goal.toPayload()).toDomain().also {
                refresh(goal.playerId)
            }
        }.getOrElse { throw it.toApiException() }
    }
}
