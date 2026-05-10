package com.example.basketballhelp.ui.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.basketballhelp.domain.usecase.SessionAnalytics
import com.example.basketballhelp.ui.components.ProgressRow
import com.example.basketballhelp.ui.components.ScreenScaffold
import com.example.basketballhelp.ui.components.SectionCard
import com.example.basketballhelp.ui.components.SectionTitle
import com.example.basketballhelp.ui.components.StatPill

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val player = state.player
    val players = state.players
    val sessions = state.sessions
    val goals = state.goals
    var isCreating by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val name = remember { mutableStateOf("") }
    val age = remember { mutableStateOf("") }
    val position = remember { mutableStateOf("") }
    val notes = remember { mutableStateOf("") }
    val bestScore = sessions.maxOfOrNull(SessionAnalytics::developmentScore) ?: 0
    val totalMinutes = sessions.sumOf { it.durationMinutes ?: 0 }
    val sevenDay = SessionAnalytics.averageForWindow(sessions, 7) { it.leftHandControl }
    val fourteenDay = SessionAnalytics.averageForWindow(sessions, 14) { it.leftHandControl }
    val latest = sessions.firstOrNull()
    val first = sessions.lastOrNull()
    val canDeleteCurrentPlayer = players.size > 1 && player != null

    LaunchedEffect(player?.id, isCreating) {
        if (isCreating) {
            name.value = ""
            age.value = "13"
            position.value = "3/4/5"
            notes.value = ""
        } else {
            name.value = player?.name.orEmpty()
            age.value = player?.age?.toString().orEmpty()
            position.value = player?.positionFocus.orEmpty()
            notes.value = player?.notes.orEmpty()
        }
    }

    ScreenScaffold(title = "Profile") { padding ->
        if (state.isLoading && player == null) {
            CircularProgressIndicator(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.error != null) {
                    item {
                        Text(state.error, color = MaterialTheme.colorScheme.error)
                        Button(onClick = viewModel::refresh) { Text("Retry") }
                    }
                }
                item {
                    SectionCard {
                        SectionTitle("Player Roster", "Switch between players or add a new one")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            players.forEach { rosterPlayer ->
                                val selected = rosterPlayer.id == player?.id && !isCreating
                                if (selected) {
                                    Button(onClick = { }) { Text(rosterPlayer.name) }
                                } else {
                                    OutlinedButton(onClick = {
                                        isCreating = false
                                        viewModel.selectPlayer(rosterPlayer.id)
                                    }) { Text(rosterPlayer.name) }
                                }
                            }
                            OutlinedButton(onClick = { isCreating = true }) { Text("New Player") }
                        }
                    }
                }
                item {
                    SectionCard {
                        SectionTitle(if (isCreating) "Create Player" else (player?.name ?: "Player"))
                        OutlinedTextField(value = name.value, onValueChange = { name.value = it }, label = { Text("Name") })
                        OutlinedTextField(value = age.value, onValueChange = { age.value = it }, label = { Text("Age") })
                        OutlinedTextField(value = position.value, onValueChange = { position.value = it }, label = { Text("Position Focus") })
                        OutlinedTextField(value = notes.value, onValueChange = { notes.value = it }, label = { Text("Notes") }, minLines = 3)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    if (isCreating) {
                                        viewModel.createPlayer(name.value, age.value.toIntOrNull() ?: 13, position.value, notes.value)
                                        isCreating = false
                                    } else {
                                        viewModel.updatePlayer(name.value, age.value.toIntOrNull() ?: 13, position.value, notes.value)
                                    }
                                },
                                enabled = !state.isSaving && name.value.isNotBlank(),
                            ) {
                                Text(if (isCreating) "Create Player" else "Save Player")
                            }
                            if (isCreating) {
                                OutlinedButton(onClick = { isCreating = false }, enabled = !state.isSaving) { Text("Cancel") }
                            } else if (canDeleteCurrentPlayer) {
                                OutlinedButton(onClick = { confirmDelete = true }, enabled = !state.isSaving) { Text("Delete Player") }
                            }
                        }
                    }
                }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatPill("Total Sessions", sessions.size.toString())
                        StatPill("Best Score", bestScore.toString())
                        StatPill("Total Minutes", totalMinutes.toString())
                    }
                }
                item {
                    SectionCard {
                        SectionTitle("Left-Hand Averages")
                        Text("7-day average ${"%.1f".format(sevenDay)}")
                        Text("14-day average ${"%.1f".format(fourteenDay)}")
                    }
                }
                item {
                    SectionCard {
                        SectionTitle("Baseline to Target")
                        goals.forEach { goal ->
                            val current = SessionAnalytics.goalProgress(goal, sessions).currentValue
                            ProgressRow(
                                label = goal.metricName,
                                value = "${goal.baselineValue.toInt()} -> ${current.toInt()} -> ${goal.targetValue.toInt()}",
                                progress = SessionAnalytics.goalProgress(goal, sessions).progress,
                            )
                        }
                    }
                }
                item {
                    SectionCard {
                        SectionTitle("Development Score Change")
                        val delta = if (first != null && latest != null) {
                            SessionAnalytics.developmentScore(latest) - SessionAnalytics.developmentScore(first)
                        } else {
                            0
                        }
                        Text("Improvement $delta points", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }

    if (confirmDelete && player != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete player?") },
            text = { Text("This removes ${player.name} and switches to another roster player if one exists.") },
            confirmButton = {
                Button(onClick = {
                    confirmDelete = false
                    viewModel.deletePlayer(player.id)
                }) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            },
        )
    }
}
