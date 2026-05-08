package com.example.basketballhelp.ui.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.basketballhelp.domain.usecase.RecommendationTone
import com.example.basketballhelp.domain.usecase.SessionAnalytics
import com.example.basketballhelp.ui.components.BarGroupChart
import com.example.basketballhelp.ui.components.LineChart
import com.example.basketballhelp.ui.components.ProgressRow
import com.example.basketballhelp.ui.components.ScreenScaffold
import com.example.basketballhelp.ui.components.SectionCard
import com.example.basketballhelp.ui.components.SectionTitle
import com.example.basketballhelp.ui.components.StatPill
import com.example.basketballhelp.ui.theme.Amber400
import com.example.basketballhelp.ui.theme.Green400
import com.example.basketballhelp.ui.theme.Orange500
import com.example.basketballhelp.ui.theme.Purple400
import com.example.basketballhelp.ui.theme.Sky400
import com.example.basketballhelp.util.formatDisplayDate

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val sessions = state.sessions
    val latest = sessions.firstOrNull()
    val developmentScore = latest?.let(SessionAnalytics::developmentScore) ?: 0
    val shootingSummary = SessionAnalytics.aggregateShooting(sessions)

    ScreenScaffold(title = "Hoop Dev") { padding ->
        if (state.isLoading) {
            Column(modifier = Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null && state.sessions.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(state.error, color = MaterialTheme.colorScheme.error)
                Button(onClick = viewModel::refresh) { Text("Retry") }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.error != null) {
                    item {
                        SectionCard {
                            Text(state.error, color = MaterialTheme.colorScheme.error)
                            Button(onClick = viewModel::refresh) { Text("Retry") }
                        }
                    }
                }
                item {
                    SectionCard {
                        Text(state.player?.name.orEmpty(), style = MaterialTheme.typography.headlineMedium)
                        Text("Position focus ${state.player?.positionFocus.orEmpty()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Latest session ${latest?.sessionDate?.let(::formatDisplayDate).orEmpty()}")
                        Text("Total sessions ${sessions.size}")
                        latest?.durationMinutes?.let { Text("Latest duration $it min") }
                    }
                }
                item {
                    SectionCard {
                        SectionTitle("Development Score", "Latest session composite score")
                        Text("$developmentScore", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatPill("Free Throws %", "${latest?.let { SessionAnalytics.percent(it.freeThrowsMade, it.freeThrowsAttempted) } ?: 0}%", Orange500)
                        StatPill("Spot Shooting %", "${latest?.let { SessionAnalytics.percent(it.spotShootingMade, it.spotShootingAttempted) } ?: 0}%", Sky400)
                        StatPill("Left Hand /10", latest?.leftHandControl?.toString() ?: "0", Green400)
                    }
                }
                item {
                    SectionCard {
                        SectionTitle("2-Week Targets")
                        state.goalProgress.take(5).forEach {
                            ProgressRow(
                                label = it.goal.metricName,
                                value = "${it.currentValue.toInt()} / ${it.goal.targetValue.toInt()}",
                                progress = it.progress,
                            )
                        }
                    }
                }
                item {
                    SectionCard {
                        SectionTitle("Trend Charts")
                        Text("Left-Hand Control")
                        LineChart(sessions.reversed().map { it.leftHandControl })
                        Text("Form Shooting")
                        LineChart(sessions.reversed().map { it.formShooting }, color = Sky400)
                        Text("Free Throw %")
                        LineChart(sessions.reversed().map { SessionAnalytics.percent(it.freeThrowsMade, it.freeThrowsAttempted).toFloat() }, color = Green400)
                        Text("Spot Shooting %")
                        LineChart(sessions.reversed().map { SessionAnalytics.percent(it.spotShootingMade, it.spotShootingAttempted).toFloat() }, color = Purple400)
                    }
                }
                item {
                    latest?.let {
                        SectionCard {
                            SectionTitle("Latest Skill Ratings")
                            BarGroupChart(
                                values = listOf(
                                    "Left Hand" to it.leftHandControl,
                                    "Form" to it.formShooting,
                                    "Guide Hand" to it.guideHand,
                                    "Footwork" to it.footwork,
                                    "Stop-Pop" to it.stopPopSpeed,
                                    "Confidence" to it.confidence,
                                ),
                            )
                        }
                    }
                }
                item {
                    SectionCard {
                        SectionTitle("Coach Recommendations")
                        state.recommendations.forEach { card ->
                            val color = when (card.tone) {
                                RecommendationTone.SUCCESS -> Green400
                                RecommendationTone.WARNING -> Amber400
                                RecommendationTone.FOCUS -> Orange500
                                RecommendationTone.NEUTRAL -> Sky400
                            }
                            ProgressRow(card.title, "", 1f, color)
                            Text(card.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item {
                    latest?.coachNotes?.takeIf { it.isNotBlank() }?.let { notes ->
                        SectionCard {
                            SectionTitle("Latest Coach Notes")
                            Text(notes)
                        }
                    }
                }
                item {
                    SectionCard {
                        SectionTitle("Aggregate Shooting Summary")
                        shootingSummary.forEach { (label, value) ->
                            ProgressRow(label, "$value%", value / 100f)
                        }
                    }
                }
            }
        }
    }
}
