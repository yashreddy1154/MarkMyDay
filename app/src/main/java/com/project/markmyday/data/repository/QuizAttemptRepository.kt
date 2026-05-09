package com.project.markmyday.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.project.markmyday.data.model.QuizAttempt

class QuizAttemptRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("quiz_attempts", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveAttempt(attempt: QuizAttempt) {
        val json = gson.toJson(attempt)
        prefs.edit().putString(attempt.quizId, json).apply()
    }

    fun getAttempt(quizId: String): QuizAttempt? {
        val json = prefs.getString(quizId, null) ?: return null
        return try {
            gson.fromJson(json, QuizAttempt::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clearAttempt(quizId: String) {
        prefs.edit().remove(quizId).apply()
    }
}
