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
    private val playersFlow = MutableStateFlow<List<Player>>(emptyList())
    private val playerFlow = MutableStateFlow<Player?>(null)

    override fun observePlayers(): Flow<List<Player>> = playersFlow.asStateFlow()

    override fun observePlayer(): Flow<Player?> = playerFlow.asStateFlow()

    override suspend fun refresh(playerId: Int?) {
        runCatching {
            val players = api.getPlayers().map { it.toDomain() }.sortedBy { it.createdAt }
            playersFlow.value = players
            val resolvedId = playerId
                ?: playerFlow.value?.id?.takeIf { currentId -> players.any { it.id == currentId } }
                ?: players.firstOrNull()?.id
            playerFlow.value = players.firstOrNull { it.id == resolvedId }
        }.getOrElse { throw it.toApiException() }
    }

    override suspend fun getPlayers(): List<Player> {
        if (playersFlow.value.isEmpty()) refresh()
        return playersFlow.value
    }

    override suspend fun getCurrentPlayer(): Player? {
        if (playerFlow.value == null) refresh()
        return playerFlow.value
    }

    override suspend fun getPlayer(playerId: Int): Player? {
        return runCatching { api.getPlayer(playerId).toDomain() }
            .onSuccess { player ->
                playerFlow.value = player
                playersFlow.value = playersFlow.value
                    .filterNot { it.id == player.id }
                    .plus(player)
                    .sortedBy { it.createdAt }
            }
            .getOrElse { throw it.toApiException() }
    }

    override suspend fun selectPlayer(playerId: Int) {
        if (playersFlow.value.isEmpty()) refresh(playerId)
        val selected = playersFlow.value.firstOrNull { it.id == playerId } ?: getPlayer(playerId)
        playerFlow.value = selected
    }

    override suspend fun upsert(player: Player): Player {
        return runCatching {
            val updated = if (player.id == 0) {
                api.createPlayer(player.toPayload())
            } else {
                api.updatePlayer(player.id, player.toPayload())
            }
            val domain = updated.toDomain()
            refresh(domain.id)
            domain
        }.getOrElse { throw it.toApiException() }
    }

    override suspend fun deletePlayer(playerId: Int) {
        runCatching {
            api.deletePlayer(playerId)
            val remaining = playersFlow.value.filterNot { it.id == playerId }
            val nextId = remaining.firstOrNull()?.id
            refresh(nextId)
        }.getOrElse { throw it.toApiException() }
    }
}
