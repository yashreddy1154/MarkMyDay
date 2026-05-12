package com.project.markmyday.ui.utils

import androidx.compose.ui.graphics.Color

object TeacherUtils {
    fun getClassColor(className: String): Color {
        val classNum = className.filter { it.isDigit() }.toIntOrNull() ?: 0
        return when (classNum) {
            1 -> Color(0xFF1E88E5) // Deep Blue
            2 -> Color(0xFF43A047) // Emerald Green
            3 -> Color(0xFFFFB300) // Amber
            4 -> Color(0xFFE53935) // Coral Red
            5 -> Color(0xFF8E24AA) // Purple
            6 -> Color(0xFF00ACC1) // Teal
            7 -> Color(0xFFD81B60) // Pink
            8 -> Color(0xFF5D4037) // Brown
            9 -> Color(0xFF3949AB) // Indigo
            10 -> Color(0xFFFB8C00) // Dark Orange
            else -> Color(0xFF607D8B) // Grey for unknown
        }
    }
}
