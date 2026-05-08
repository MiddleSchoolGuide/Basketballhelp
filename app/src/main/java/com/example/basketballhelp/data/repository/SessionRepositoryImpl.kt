package com.example.basketballhelp.data.repository

import com.example.basketballhelp.data.network.service.HoopDevApiService
import com.example.basketballhelp.data.network.util.toApiException
import com.example.basketballhelp.data.network.util.toDomain
import com.example.basketballhelp.data.network.util.toPayload
import com.example.basketballhelp.domain.model.Session
import com.example.basketballhelp.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class SessionRepositoryImpl(
    private val api: HoopDevApiService,
) : SessionRepository {
    private val sessionsFlow = MutableStateFlow<List<Session>>(emptyList())

    override fun observeSessions(): Flow<List<Session>> = sessionsFlow.asStateFlow()

    override fun observeLatestSession(): Flow<Session?> = sessionsFlow.map { it.maxByOrNull(Session::sessionDate) }

    override suspend fun refresh(playerId: Int, limit: Int?) {
        runCatching {
            sessionsFlow.value = api.getSessions(playerId, limit).map { it.toDomain() }
        }.getOrElse { throw it.toApiException() }
    }

    override suspend fun getSessions(): List<Session> = sessionsFlow.value

    override suspend fun getSessionById(sessionId: Int): Session? {
        return runCatching { api.getSession(sessionId).toDomain() }
            .getOrElse { throw it.toApiException() }
    }

    override suspend fun saveSession(session: Session): Int {
        return runCatching {
            val saved = if (session.id == 0) {
                api.createSession(session.toPayload())
            } else {
                api.updateSession(session.id, session.toPayload())
            }.toDomain()
            refresh(playerId = session.playerId)
            saved.id
        }.getOrElse { throw it.toApiException() }
    }

    override suspend fun deleteSession(session: Session) {
        runCatching {
            api.deleteSession(session.id)
            refresh(playerId = session.playerId)
        }.getOrElse { throw it.toApiException() }
    }
}
