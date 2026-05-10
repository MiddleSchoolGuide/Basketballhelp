package com.example.basketballhelp.data.network.dto

data class PlayerDto(
    val id: Int,
    val name: String,
    val age: Int,
    val positionFocus: String,
    val notes: String?,
    val createdAt: String,
)

data class SessionDto(
    val id: Int,
    val playerId: Int,
    val sessionDate: String,
    val durationMinutes: Int?,
    val leftHandControl: Float,
    val rightHandControl: Float,
    val formShooting: Float,
    val guideHand: Float,
    val freeThrowsMade: Int,
    val freeThrowsAttempted: Int,
    val spotShootingMade: Int,
    val spotShootingAttempted: Int,
    val closeRangeMade: Int,
    val closeRangeAttempted: Int,
    val stopPopSpeed: Float,
    val footwork: Float,
    val bigPlayerSkill: Float,
    val confidence: Float,
    val coachNotes: String?,
    val createdAt: String,
    val updatedAt: String,
)

data class GoalDto(
    val id: Int,
    val playerId: Int,
    val metricName: String,
    val baselineValue: Float,
    val targetValue: Float,
    val targetDate: String?,
    val createdAt: String,
)

data class GoalPayloadDto(
    val playerId: Int,
    val metricName: String,
    val baselineValue: Float,
    val targetValue: Float,
    val targetDate: String?,
)

data class DrillChecklistItemDto(
    val id: Int,
    val drillName: String,
    val category: String,
    val targetReps: String?,
    val description: String?,
    val completed: Boolean,
    val completionId: Int?,
    val notes: String?,
)

data class SessionPayloadDto(
    val playerId: Int,
    val sessionDate: String,
    val durationMinutes: Int?,
    val leftHandControl: Float,
    val rightHandControl: Float,
    val formShooting: Float,
    val guideHand: Float,
    val freeThrowsMade: Int,
    val freeThrowsAttempted: Int,
    val spotShootingMade: Int,
    val spotShootingAttempted: Int,
    val closeRangeMade: Int,
    val closeRangeAttempted: Int,
    val stopPopSpeed: Float,
    val footwork: Float,
    val bigPlayerSkill: Float,
    val confidence: Float,
    val coachNotes: String?,
)

data class PlayerPayloadDto(
    val name: String,
    val age: Int,
    val positionFocus: String,
    val notes: String?,
)

data class DrillCompletionPayloadDto(
    val drillId: Int,
    val date: String,
    val completed: Boolean,
    val notes: String?,
    val sessionId: Int?,
)

data class AiPracticePlanRequestDto(
    val playerId: Int,
)

data class AiPracticePlanDto(
    val headline: String,
    val summary: String,
    val focusAreas: List<AiFocusAreaDto>,
    val nextSessionPlan: List<AiSessionBlockDto>,
    val caution: String?,
)

data class AiFocusAreaDto(
    val title: String,
    val reason: String,
    val adjustment: String,
)

data class AiSessionBlockDto(
    val phase: String,
    val drill: String,
    val minutes: Int,
    val target: String,
)

data class ApiErrorDto(
    val error: String?,
)
