package com.example.basketballhelp.ui.screen.log

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

@Composable
fun LogSessionScreen(
    viewModel: LogSessionViewModel,
    onSaved: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.clearSaved()
            onSaved()
        }
    }

    ScreenScaffold(title = "Log Session") { padding ->
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Step ${state.currentStep + 1} of 5", color = MaterialTheme.colorScheme.primary)
            SessionFormStep(
                form = state.form,
                step = state.currentStep,
                modifier = Modifier.weight(1f),
                onFormChange = { form -> viewModel.updateForm { form } },
            )
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.currentStep > 0) {
                    Button(onClick = viewModel::previousStep, modifier = Modifier.weight(1f)) { Text("Back") }
                }
                if (state.currentStep < 4) {
                    Button(onClick = viewModel::nextStep, modifier = Modifier.weight(1f)) { Text("Next") }
                } else {
                    Button(onClick = viewModel::save, modifier = Modifier.weight(1f), enabled = !state.isSaving) {
                        if (state.isSaving) CircularProgressIndicator() else Text("Save Session")
                    }
                }
            }
        }
    }
}
