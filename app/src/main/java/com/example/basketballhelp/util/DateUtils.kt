package com.example.basketballhelp.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val displayFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

fun todayIso(): String = LocalDate.now().toString()

fun formatDisplayDate(isoDate: String): String =
    LocalDate.parse(isoDate).format(displayFormatter)

fun formatTimestamp(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(displayFormatter)
