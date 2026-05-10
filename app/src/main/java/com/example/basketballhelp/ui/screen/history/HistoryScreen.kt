package com.example.basketballhelp.ui.screen.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.basketballhelp.domain.model.Session
import com.example.basketballhelp.domain.usecase.SessionAnalytics
import com.example.basketballhelp.ui.components.ExpandableRow
import com.example.basketballhelp.ui.components.ScreenScaffold
import com.example.basketballhelp.util.formatDisplayDate

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onEditSession: (Int) -> Unit,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val sessions = uiState.sessions
    var expandedId by remember { mutableIntStateOf(0) }
    var deleteTarget by remember { mutableStateOf<Session?>(null) }

    ScreenScaffold(title = "History") { padding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (uiState.error != null) {
                    item {
                        Text(uiState.error, color = MaterialTheme.colorScheme.error)
                        Button(onClick = viewModel::refresh) { Text("Retry") }
                    }
                }
                itemsIndexed(sessions, key = { _, item -> item.id }) { index, session ->
                    ExpandableRow(
                        title = formatDisplayDate(session.sessionDate) + if (index == 0) "  Latest" else "",
                        subtitle = "FT ${SessionAnalytics.percent(session.freeThrowsMade, session.freeThrowsAttempted)}%  •  Spot ${SessionAnalytics.percent(session.spotShootingMade, session.spotShootingAttempted)}%  •  Score ${SessionAnalytics.developmentScore(session)}",
                        expanded = expandedId == session.id,
                        onToggle = { expandedId = if (expandedId == session.id) 0 else session.id },
                    ) {
                        Text("Duration ${session.durationMinutes ?: 0} min")
                        Text("Left Hand ${session.leftHandControl}/10")
                        Text("Right Hand ${session.rightHandControl}/10")
                        Text("Form ${session.formShooting}/10")
                        Text("Guide Hand ${session.guideHand}/10")
                        Text("Free Throws ${session.freeThrowsMade}/${session.freeThrowsAttempted} (${SessionAnalytics.percent(session.freeThrowsMade, session.freeThrowsAttempted)}%)")
                        Text("Spot Shooting ${session.spotShootingMade}/${session.spotShootingAttempted} (${SessionAnalytics.percent(session.spotShootingMade, session.spotShootingAttempted)}%)")
                        Text("Close Range ${session.closeRangeMade}/${session.closeRangeAttempted} (${SessionAnalytics.percent(session.closeRangeMade, session.closeRangeAttempted)}%)")
                        Text(
                            "Overall Shooting ${
                                SessionAnalytics.percent(
                                    session.freeThrowsMade + session.spotShootingMade + session.closeRangeMade,
                                    session.freeThrowsAttempted + session.spotShootingAttempted + session.closeRangeAttempted,
                                )
                            }%",
                        )
                        session.coachNotes?.let { Text("Notes: $it") }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { onEditSession(session.id) }) { Text("Edit") }
                            OutlinedButton(onClick = { deleteTarget = session }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { session ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete session?") },
            text = { Text("This removes the session from history.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteSession(session)
                    deleteTarget = null
                }) { Text("Delete") }
            },
            dismissButton = { OutlinedButton(onClick = { deleteTarget = null }) { Text("Cancel") } },
        )
    }
}
