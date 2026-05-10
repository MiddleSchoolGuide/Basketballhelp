package com.example.basketballhelp.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basketballhelp.domain.model.Goal
import com.example.basketballhelp.domain.model.Player
import com.example.basketballhelp.domain.model.Session
import com.example.basketballhelp.domain.repository.GoalRepository
import com.example.basketballhelp.domain.repository.PlayerRepository
import com.example.basketballhelp.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val players: List<Player> = emptyList(),
    val player: Player? = null,
    val sessions: List<Session> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
)

class ProfileViewModel(
    private val playerRepository: PlayerRepository,
    private val sessionRepository: SessionRepository,
    private val goalRepository: GoalRepository,
) : ViewModel() {
    private data class ProfileStateSnapshot(
        val players: List<Player>,
        val player: Player?,
        val sessions: List<Session>,
        val goals: List<Goal>,
        val saving: Boolean,
    )

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)

    private val snapshot = combine(
        playerRepository.observePlayers(),
        playerRepository.observePlayer(),
        sessionRepository.observeSessions(),
        goalRepository.observeGoals(),
        _saving,
    ) { players, player, sessions, goals, saving ->
        ProfileStateSnapshot(
            players = players,
            player = player,
            sessions = sessions.sortedByDescending { it.sessionDate },
            goals = goals,
            saving = saving,
        )
    }

    val uiState = combine(snapshot, _error) { snapshot, error ->
        ProfileUiState(
            players = snapshot.players,
            player = snapshot.player,
            sessions = snapshot.sessions,
            goals = snapshot.goals,
            isLoading = snapshot.saving && snapshot.player == null && snapshot.sessions.isEmpty() && snapshot.goals.isEmpty() && snapshot.players.isEmpty(),
            isSaving = snapshot.saving,
            error = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _saving.value = true
            _error.value = null
            runCatching {
                playerRepository.refresh()
                val playerId = playerRepository.getCurrentPlayer()?.id
                sessionRepository.refresh(playerId)
                goalRepository.refresh(playerId)
            }.onFailure {
                _error.value = it.message
            }
            _saving.value = false
        }
    }

    fun updatePlayer(name: String, age: Int, positionFocus: String, notes: String) {
        val player = uiState.value.player ?: return
        viewModelScope.launch {
            _saving.value = true
            runCatching {
                playerRepository.upsert(player.copy(name = name, age = age, positionFocus = positionFocus, notes = notes))
            }.onFailure {
                _error.value = it.message
            }
            _saving.value = false
        }
    }

    fun createPlayer(name: String, age: Int, positionFocus: String, notes: String) {
        viewModelScope.launch {
            _saving.value = true
            _error.value = null
            runCatching {
                val created = playerRepository.upsert(
                    Player(
                        id = 0,
                        name = name,
                        age = age,
                        positionFocus = positionFocus,
                        notes = notes.ifBlank { null },
                        createdAt = System.currentTimeMillis(),
                    ),
                )
                sessionRepository.refresh(created.id)
                goalRepository.refresh(created.id)
            }.onFailure {
                _error.value = it.message
            }
            _saving.value = false
        }
    }

    fun selectPlayer(playerId: Int) {
        viewModelScope.launch {
            _saving.value = true
            _error.value = null
            runCatching {
                playerRepository.selectPlayer(playerId)
                sessionRepository.refresh(playerId)
                goalRepository.refresh(playerId)
            }.onFailure {
                _error.value = it.message
            }
            _saving.value = false
        }
    }

    fun deletePlayer(playerId: Int) {
        viewModelScope.launch {
            _saving.value = true
            _error.value = null
            runCatching {
                playerRepository.deletePlayer(playerId)
                val nextId = playerRepository.getCurrentPlayer()?.id
                sessionRepository.refresh(nextId)
                goalRepository.refresh(nextId)
            }.onFailure {
                _error.value = it.message
            }
            _saving.value = false
        }
    }
}
