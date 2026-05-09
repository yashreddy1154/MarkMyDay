package com.project.markmyday.utils

import android.content.Context
import android.net.Uri
import com.project.markmyday.data.model.Question
import java.io.BufferedReader
import java.io.InputStreamReader

object FileParser {
    fun parseCsv(context: Context, uri: Uri): List<Question> {
        val questions = mutableListOf<Question>()
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            
            // Skip header (Subject, Class, Question, Option A, Option B, Option C, Option D, Correct Answer)
            val headerLine = reader.readLine()
            
            var line: String? = reader.readLine()
            while (line != null) {
                // Split by comma but handle potential quotes
                val tokens = line.split(",").map { it.trim().removeSurrounding("\"") }
                
                if (tokens.size >= 8) {
                    val subject = tokens[0]
                    val className = tokens[1]
                    val questionText = tokens[2]
                    val options = listOf(tokens[3], tokens[4], tokens[5], tokens[6])
                    val correctAnswerLabel = tokens[7] // e.g., "Option B"
                    
                    // Map "Option B" to the actual value of tokens[4]
                    val correctAnswer = when(correctAnswerLabel.replace(" ", "").lowercase()) {
                        "optiona" -> tokens[3]
                        "optionb" -> tokens[4]
                        "optionc" -> tokens[5]
                        "optiond" -> tokens[6]
                        else -> correctAnswerLabel // fallback
                    }
                    
                    questions.add(
                        Question(
                            text = questionText,
                            options = options,
                            correctAnswer = correctAnswer,
                            subject = subject,
                            className = className
                        )
                    )
                }
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return questions
    }
}
