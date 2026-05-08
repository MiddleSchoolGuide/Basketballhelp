package com.example.basketballhelp.data.repository

import com.example.basketballhelp.data.network.dto.DrillCompletionPayloadDto
import com.example.basketballhelp.data.network.service.HoopDevApiService
import com.example.basketballhelp.data.network.util.toApiException
import com.example.basketballhelp.data.network.util.toDomain
import com.example.basketballhelp.domain.model.DrillWithCompletion
import com.example.basketballhelp.domain.repository.DrillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DrillRepositoryImpl(
    private val api: HoopDevApiService,
) : DrillRepository {
    private val flows = linkedMapOf<String, MutableStateFlow<List<DrillWithCompletion>>>()

    override fun observeDailyChecklist(date: String): Flow<List<DrillWithCompletion>> =
        flowFor(date).asStateFlow()

    override suspend fun refresh(date: String) {
        runCatching {
            flowFor(date).value = api.getDrills(date).map { it.toDomain() }
        }.getOrElse { throw it.toApiException() }
    }

    override suspend fun toggleCompletion(drillId: Int, date: String, completed: Boolean) {
        runCatching {
            api.saveDrillCompletion(
                DrillCompletionPayloadDto(
                    drillId = drillId,
                    date = date,
                    completed = completed,
                    notes = null,
                    sessionId = null,
                ),
            )
            refresh(date)
        }.getOrElse { throw it.toApiException() }
    }

    private fun flowFor(date: String): MutableStateFlow<List<DrillWithCompletion>> {
        return flows.getOrPut(date) { MutableStateFlow(emptyList()) }
    }
}
