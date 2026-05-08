package com.example.basketballhelp.data.repository

import com.example.basketballhelp.data.network.service.HoopDevApiService
import com.example.basketballhelp.data.network.util.toApiException
import com.example.basketballhelp.data.network.util.toDomain
import com.example.basketballhelp.data.network.util.toPayload
import com.example.basketballhelp.domain.model.Player
import com.example.basketballhelp.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerRepositoryImpl(
    private val api: HoopDevApiService,
) : PlayerRepository {
    private val playerFlow = MutableStateFlow<Player?>(null)

    override fun observePlayer(): Flow<Player?> = playerFlow.asStateFlow()

    override suspend fun refresh(playerId: Int) {
        playerFlow.value = api.getPlayer(playerId).toDomain()
    }

    override suspend fun getPlayer(playerId: Int): Player? {
        return runCatching { api.getPlayer(playerId).toDomain() }
            .onSuccess { playerFlow.value = it }
            .getOrElse { throw it.toApiException() }
    }

    override suspend fun upsert(player: Player) {
        runCatching {
            val updated = if (player.id == 0) {
                api.createPlayer(player.toPayload())
            } else {
                api.updatePlayer(player.id, player.toPayload())
            }
            playerFlow.value = updated.toDomain()
        }.getOrElse { throw it.toApiException() }
    }
}
