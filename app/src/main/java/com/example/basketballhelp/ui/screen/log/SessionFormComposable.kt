package com.example.basketballhelp.ui.screen.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.basketballhelp.domain.usecase.SessionAnalytics
import com.example.basketballhelp.ui.components.SectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionFormStep(
    form: SessionFormState,
    step: Int,
    modifier: Modifier = Modifier,
    onFormChange: (SessionFormState) -> Unit,
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (step < 0) {
            item { SessionInfoStep(form, onFormChange) }
            item { BallHandlingStep(form, onFormChange) }
            item { ShootingStep(form, onFormChange) }
            item { AthleticismStep(form, onFormChange) }
            item { NotesStep(form, onFormChange) }
        } else {
            item {
                when (step) {
                    0 -> SessionInfoStep(form, onFormChange)
                    1 -> BallHandlingStep(form, onFormChange)
                    2 -> ShootingStep(form, onFormChange)
                    3 -> AthleticismStep(form, onFormChange)
                    else -> NotesStep(form, onFormChange)
                }
            }
        }
    }
}

@Composable
private fun SessionInfoStep(form: SessionFormState, onChange: (SessionFormState) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Session Info", "Date and workout length")
        NumberField("Duration Minutes", form.durationMinutes.toString()) {
            onChange(form.copy(durationMinutes = it.toIntOrNull() ?: 0))
        }
        OutlinedTextField(
            value = form.sessionDate,
            onValueChange = { onChange(form.copy(sessionDate = it)) },
            label = { Text("Session Date (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun BallHandlingStep(form: SessionFormState, onChange: (SessionFormState) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Ball Handling", "Rate control and confidence from 0 to 10")
        SliderField("Left-Hand Control", form.leftHandControl) { onChange(form.copy(leftHandControl = it)) }
        SliderField("Right-Hand Control", form.rightHandControl) { onChange(form.copy(rightHandControl = it)) }
        SliderField("Confidence", form.confidence) { onChange(form.copy(confidence = it)) }
    }
}

@Composable
private fun ShootingStep(form: SessionFormState, onChange: (SessionFormState) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Shooting", "Ratings plus live shooting percentage preview")
        SliderField("Form Shooting", form.formShooting) { onChange(form.copy(formShooting = it)) }
        SliderField("Guide Hand", form.guideHand) { onChange(form.copy(guideHand = it)) }
        NumberField("Free Throws Made", form.freeThrowsMade.toString()) { onChange(form.copy(freeThrowsMade = it.toIntOrNull() ?: 0)) }
        NumberField("Free Throws Attempted", form.freeThrowsAttempted.toString()) { onChange(form.copy(freeThrowsAttempted = it.toIntOrNull() ?: 0)) }
        NumberField("Spot Shooting Made", form.spotShootingMade.toString()) { onChange(form.copy(spotShootingMade = it.toIntOrNull() ?: 0)) }
        NumberField("Spot Shooting Attempted", form.spotShootingAttempted.toString()) { onChange(form.copy(spotShootingAttempted = it.toIntOrNull() ?: 0)) }
        NumberField("Close Range Made", form.closeRangeMade.toString()) { onChange(form.copy(closeRangeMade = it.toIntOrNull() ?: 0)) }
        NumberField("Close Range Attempted", form.closeRangeAttempted.toString()) { onChange(form.copy(closeRangeAttempted = it.toIntOrNull() ?: 0)) }
        PreviewCard("FT Preview", "${SessionAnalytics.percent(form.freeThrowsMade, form.freeThrowsAttempted)}%")
        PreviewCard("Spot Preview", "${SessionAnalytics.percent(form.spotShootingMade, form.spotShootingAttempted)}%")
    }
}

@Composable
private fun AthleticismStep(form: SessionFormState, onChange: (SessionFormState) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Athleticism", "Movement and big-player skill work")
        SliderField("Stop-and-Pop Speed", form.stopPopSpeed) { onChange(form.copy(stopPopSpeed = it)) }
        SliderField("Footwork", form.footwork) { onChange(form.copy(footwork = it)) }
        SliderField("Big Player Skill", form.bigPlayerSkill) { onChange(form.copy(bigPlayerSkill = it)) }
    }
}

@Composable
private fun NotesStep(form: SessionFormState, onChange: (SessionFormState) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Notes", "Final summary before save")
        OutlinedTextField(
            value = form.coachNotes,
            onValueChange = { onChange(form.copy(coachNotes = it)) },
            label = { Text("Coach Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
        )
        OutlinedCard(colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Summary", style = MaterialTheme.typography.titleMedium)
                Text("Date: ${form.sessionDate}")
                Text("Duration: ${form.durationMinutes} min")
                Text("Left Hand: ${form.leftHandControl}/10")
                Text("FT: ${SessionAnalytics.percent(form.freeThrowsMade, form.freeThrowsAttempted)}%")
                Text("Spot: ${SessionAnalytics.percent(form.spotShootingMade, form.spotShootingAttempted)}%")
            }
        }
    }
}

@Composable
private fun SliderField(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Text("$label ${"%.1f".format(value)}")
        Slider(value = value, onValueChange = onValueChange, valueRange = 0f..10f)
    }
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PreviewCard(label: String, value: String) {
    OutlinedCard {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}
