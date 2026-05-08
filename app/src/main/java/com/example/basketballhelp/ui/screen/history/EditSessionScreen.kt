package com.example.basketballhelp.ui.screen.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.basketballhelp.ui.components.ScreenScaffold
import com.example.basketballhelp.ui.screen.log.SessionFormStep

@Composable
fun EditSessionScreen(
    viewModel: EditSessionViewModel,
    sessionId: Int,
    onSaved: () -> Unit,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    LaunchedEffect(sessionId) { viewModel.load(sessionId) }
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    ScreenScaffold(title = "Edit Session", onBack = onBack) { padding ->
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding))
        } else {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SessionFormStep(
                    form = state.form,
                    step = -1,
                    modifier = Modifier.weight(1f),
                    onFormChange = { form -> viewModel.updateForm { form } },
                )
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = viewModel::save, modifier = Modifier.weight(1f), enabled = !state.isSaving) {
                        if (state.isSaving) CircularProgressIndicator() else Text("Save Changes")
                    }
                }
            }
        }
    }
}
