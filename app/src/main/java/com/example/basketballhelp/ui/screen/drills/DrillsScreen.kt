package com.example.basketballhelp.ui.screen.drills

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.basketballhelp.ui.components.ProgressRow
import com.example.basketballhelp.ui.components.ScreenScaffold
import com.example.basketballhelp.ui.components.SectionCard
import com.example.basketballhelp.ui.components.SectionTitle
import com.example.basketballhelp.ui.theme.Amber400
import com.example.basketballhelp.ui.theme.Green400
import com.example.basketballhelp.ui.theme.Orange500
import com.example.basketballhelp.ui.theme.Purple400
import com.example.basketballhelp.ui.theme.Sky400

@Composable
fun DrillsScreen(viewModel: DrillsViewModel) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val checklist = state.checklist
    val expanded = remember { mutableStateMapOf<Int, Boolean>() }
    val completedCount = checklist.count { it.completion?.completed == true }

    ScreenScaffold(title = "Daily Drills") { padding ->
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.error != null) {
                    item {
                        Text(state.error, color = MaterialTheme.colorScheme.error)
                        Button(onClick = viewModel::refresh) { Text("Retry") }
                    }
                }
                item {
                    SectionCard {
                        SectionTitle("Checklist Progress", if (completedCount == checklist.size && checklist.isNotEmpty()) "All drills complete. Strong work." else "Today's target stack")
                        ProgressRow("Completed", "$completedCount / ${checklist.size}", if (checklist.isEmpty()) 0f else completedCount / checklist.size.toFloat())
                    }
                }
                checklist.groupBy { it.drill.category }.forEach { (category, drills) ->
                    item {
                        Text(category, style = MaterialTheme.typography.titleMedium, color = categoryColor(category))
                    }
                    items(drills, key = { it.drill.id }) { item ->
                        SectionCard {
                            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Checkbox(
                                    checked = item.completion?.completed == true,
                                    onCheckedChange = { viewModel.toggle(item.drill.id, it) },
                                )
                                Column {
                                    Text(item.drill.drillName)
                                    item.drill.targetReps?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    Text(
                                        if (expanded[item.drill.id] == true) "Hide description" else "Show description",
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                            }
                            item.drill.description?.takeIf { expanded[item.drill.id] == true }?.let { Text(it) }
                            androidx.compose.material3.TextButton(onClick = { expanded[item.drill.id] = !(expanded[item.drill.id] ?: false) }) {
                                Text(if (expanded[item.drill.id] == true) "Collapse" else "Expand")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun categoryColor(category: String): Color = when (category) {
    "Weak Hand" -> Orange500
    "Shooting" -> Sky400
    "Free Throws" -> Green400
    "Footwork" -> Purple400
    else -> Amber400
}
