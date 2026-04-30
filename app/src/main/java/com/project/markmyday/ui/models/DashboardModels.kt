package com.project.markmyday.ui.models

import androidx.compose.ui.graphics.vector.ImageVector

data class DashboardTile(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int? = null,
    val badgeText: String? = null
)

data class TimetableEntry(
    val subject: String,
    val code: String,
    val time: String,
    val room: String
)
