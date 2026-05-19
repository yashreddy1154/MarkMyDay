package com.project.markmyday.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.project.markmyday.R

@Composable
fun getSubjectColorForGrid(subject: String): Color {
    val telugu = stringResource(R.string.subject_telugu_label).lowercase()
    val hindi = stringResource(R.string.subject_hindi_label).lowercase()
    val english = stringResource(R.string.subject_english_label).lowercase()
    val math = stringResource(R.string.subject_math_label).lowercase()
    val physics = stringResource(R.string.subject_physics_label).lowercase()
    val biology = stringResource(R.string.subject_biology_label).lowercase()
    val science = stringResource(R.string.subject_science_label).lowercase()
    val social = stringResource(R.string.subject_social_label).lowercase()
    val computer = stringResource(R.string.subject_computer_label).lowercase()

    return when (subject.lowercase()) {
        telugu -> Color(0xFF1E88E5) // Blue
        math -> Color(0xFF9E9E9E) // Grey
        physics -> Color(0xFF03A9F4) // Light Blue
        biology -> Color(0xFF4CAF50) // Green
        science -> Color(0xFF26A69A) // Mix of Blue and Green (Teal)
        social -> Color(0xFFFF9800) // Orange
        hindi -> Color(0xFFE91E63) // Pink/Custom
        english -> Color(0xFF673AB7) // Deep Purple/Custom
        computer -> Color(0xFF212121) // Black
        else -> Color(0xFF6200EE) // Default Purple
    }
}
