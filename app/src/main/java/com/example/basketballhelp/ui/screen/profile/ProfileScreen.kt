package com.example.basketballhelp.ui.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    val sessions = state.sessions
    val goals = state.goals
    val name = remember(player?.name) { mutableStateOf(player?.name.orEmpty()) }
    val age = remember(player?.age) { mutableStateOf(player?.age?.toString().orEmpty()) }
    val position = remember(player?.positionFocus) { mutableStateOf(player?.positionFocus.orEmpty()) }
    val notes = remember(player?.notes) { mutableStateOf(player?.notes.orEmpty()) }
    val bestScore = sessions.maxOfOrNull(SessionAnalytics::developmentScore) ?: 0
    val totalMinutes = sessions.sumOf { it.durationMinutes ?: 0 }
    val sevenDay = SessionAnalytics.averageForWindow(sessions, 7) { it.leftHandControl }
    val fourteenDay = SessionAnalytics.averageForWindow(sessions, 14) { it.leftHandControl }
    val latest = sessions.firstOrNull()
    val first = sessions.lastOrNull()

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
                        SectionTitle(player?.name ?: "Player")
                        OutlinedTextField(value = name.value, onValueChange = { name.value = it }, label = { Text("Name") })
                        OutlinedTextField(value = age.value, onValueChange = { age.value = it }, label = { Text("Age") })
                        OutlinedTextField(value = position.value, onValueChange = { position.value = it }, label = { Text("Position Focus") })
                        OutlinedTextField(value = notes.value, onValueChange = { notes.value = it }, label = { Text("Notes") }, minLines = 3)
                        Button(onClick = {
                            viewModel.updatePlayer(name.value, age.value.toIntOrNull() ?: 13, position.value, notes.value)
                        }) { Text("Save Player") }
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
}
