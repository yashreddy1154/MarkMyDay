package com.project.markmyday.ui.utils

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

fun calculateAgeFromDateOfBirth(dateOfBirth: Date?): Int? {
    if (dateOfBirth == null) return null
    val today = Calendar.getInstance()
    val dobCal = Calendar.getInstance().apply { time = dateOfBirth }
    var age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
        age--
    }
    return age
}

fun formatDateForDisplay(date: Date?): String {
    if (date == null) return ""
    val formatter = DateTimeFormatter.ofPattern("ddMMyyyy")
        .withZone(ZoneId.systemDefault())
    return formatter.format(date.toInstant())
}
