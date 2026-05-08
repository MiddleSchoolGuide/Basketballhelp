package com.example.basketballhelp.data.network.util

import com.example.basketballhelp.data.network.dto.DrillChecklistItemDto
import com.example.basketballhelp.data.network.dto.GoalPayloadDto
import com.example.basketballhelp.data.network.dto.PlayerDto
import com.example.basketballhelp.data.network.dto.PlayerPayloadDto
import com.example.basketballhelp.data.network.dto.GoalDto
import com.example.basketballhelp.data.network.dto.SessionDto
import com.example.basketballhelp.data.network.dto.SessionPayloadDto
import com.example.basketballhelp.domain.model.DrillChecklistItem
import com.example.basketballhelp.domain.model.DrillWithCompletion
import com.example.basketballhelp.domain.model.Goal
import com.example.basketballhelp.domain.model.Player
import com.example.basketballhelp.domain.model.Session
import com.example.basketballhelp.domain.model.SessionDrillCompletion
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

fun PlayerDto.toDomain(): Player = Player(
    id = id,
    name = name,
    age = age,
    positionFocus = positionFocus,
    notes = notes,
    createdAt = createdAt.toEpochMillis(),
)

fun GoalDto.toDomain(): Goal = Goal(
    id = id,
    playerId = playerId,
    metricName = metricName,
    baselineValue = baselineValue,
    targetValue = targetValue,
    targetDate = targetDate?.toLocalDateString(),
    createdAt = createdAt.toEpochMillis(),
)

fun SessionDto.toDomain(): Session = Session(
    id = id,
    playerId = playerId,
    sessionDate = sessionDate.toLocalDateString(),
    durationMinutes = durationMinutes,
    leftHandControl = leftHandControl,
    rightHandControl = rightHandControl,
    formShooting = formShooting,
    guideHand = guideHand,
    freeThrowsMade = freeThrowsMade,
    freeThrowsAttempted = freeThrowsAttempted,
    spotShootingMade = spotShootingMade,
    spotShootingAttempted = spotShootingAttempted,
    closeRangeMade = closeRangeMade,
    closeRangeAttempted = closeRangeAttempted,
    stopPopSpeed = stopPopSpeed,
    footwork = footwork,
    bigPlayerSkill = bigPlayerSkill,
    confidence = confidence,
    coachNotes = coachNotes,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
)

fun DrillChecklistItemDto.toDomain(): DrillWithCompletion = DrillWithCompletion(
    drill = DrillChecklistItem(
        id = id,
        drillName = drillName,
        category = category,
        targetReps = targetReps,
        description = description,
    ),
    completion = if (completionId != null || completed || notes != null) {
        SessionDrillCompletion(
            id = completionId ?: 0,
            sessionId = null,
            drillId = id,
            date = "",
            completed = completed,
            notes = notes,
        )
    } else {
        null
    },
)

fun Session.toPayload(): SessionPayloadDto = SessionPayloadDto(
    playerId = playerId,
    sessionDate = sessionDate,
    durationMinutes = durationMinutes,
    leftHandControl = leftHandControl,
    rightHandControl = rightHandControl,
    formShooting = formShooting,
    guideHand = guideHand,
    freeThrowsMade = freeThrowsMade,
    freeThrowsAttempted = freeThrowsAttempted,
    spotShootingMade = spotShootingMade,
    spotShootingAttempted = spotShootingAttempted,
    closeRangeMade = closeRangeMade,
    closeRangeAttempted = closeRangeAttempted,
    stopPopSpeed = stopPopSpeed,
    footwork = footwork,
    bigPlayerSkill = bigPlayerSkill,
    confidence = confidence,
    coachNotes = coachNotes,
)

fun Player.toPayload(): PlayerPayloadDto = PlayerPayloadDto(
    name = name,
    age = age,
    positionFocus = positionFocus,
    notes = notes,
)

fun Goal.toPayload(): GoalPayloadDto = GoalPayloadDto(
    playerId = playerId,
    metricName = metricName,
    baselineValue = baselineValue,
    targetValue = targetValue,
    targetDate = targetDate,
)

private fun String.toEpochMillis(): Long {
    return runCatching { Instant.parse(this).toEpochMilli() }
        .recoverCatching { OffsetDateTime.parse(this).toInstant().toEpochMilli() }
        .getOrElse { 0L }
}

private fun String.toLocalDateString(): String {
    return runCatching { Instant.parse(this).atZone(ZoneId.systemDefault()).toLocalDate().toString() }
        .recoverCatching { OffsetDateTime.parse(this).toLocalDate().toString() }
        .recoverCatching { LocalDate.parse(this).toString() }
        .getOrElse { take(10) }
}
