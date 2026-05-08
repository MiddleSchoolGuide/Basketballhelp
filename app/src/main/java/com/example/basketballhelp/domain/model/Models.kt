package com.example.basketballhelp.domain.model

data class Player(
    val id: Int,
    val name: String,
    val age: Int,
    val positionFocus: String,
    val notes: String?,
    val createdAt: Long,
)

data class Session(
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
    val createdAt: Long,
    val updatedAt: Long,
)

data class Goal(
    val id: Int,
    val playerId: Int,
    val metricName: String,
    val baselineValue: Float,
    val targetValue: Float,
    val targetDate: String?,
    val createdAt: Long,
)

data class DrillChecklistItem(
    val id: Int,
    val drillName: String,
    val category: String,
    val targetReps: String?,
    val description: String?,
)

data class SessionDrillCompletion(
    val id: Int,
    val sessionId: Int?,
    val drillId: Int,
    val date: String,
    val completed: Boolean,
    val notes: String?,
)

data class DrillWithCompletion(
    val drill: DrillChecklistItem,
    val completion: SessionDrillCompletion?,
)
