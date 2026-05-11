package com.project.markmyday.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.project.markmyday.data.model.CourseVideo
import com.project.markmyday.data.model.Question
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.util.regex.Pattern

object FileParser {
    fun parseCsv(context: Context, uri: Uri): List<Question> {
        val questions = mutableListOf<Question>()
        try {
            val extension = getFileExtension(context, uri)
            if (extension == "xls" || extension == "xlsx") {
                return parseQuestionExcel(context, uri)
            }

            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: return emptyList()
            val rows = parseCsvString(content)
            
            // Skip header
            for (i in 1 until rows.size) {
                val tokens = rows[i]
                if (tokens.size >= 8) {
                    val subject = tokens[0]
                    var className = tokens[1]
                    
                    // Requirement: GK or Current Affairs to all
                    if (subject.trim().lowercase() == "gk" || subject.trim().lowercase().contains("current affairs")) {
                        className = "ALL"
                    }
                    
                    val questionText = tokens[2]
                    val options = listOf(tokens[3], tokens[4], tokens[5], tokens[6])
                    val correctAnswerLabel = tokens[7]
                    
                    val correctAnswer = when(correctAnswerLabel.replace(" ", "").lowercase()) {
                        "optiona" -> tokens[3]
                        "optionb" -> tokens[4]
                        "optionc" -> tokens[5]
                        "optiond" -> tokens[6]
                        else -> correctAnswerLabel
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return questions
    }

    fun parseVideoCsv(context: Context, uri: Uri): List<CourseVideo> {
        val videos = mutableListOf<CourseVideo>()
        try {
            val extension = getFileExtension(context, uri)
            if (extension == "xls" || extension == "xlsx") {
                return parseVideoExcel(context, uri)
            }

            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: return emptyList()
            val rows = parseCsvString(content)
            
            // Skip header
            for (i in 1 until rows.size) {
                val tokens = rows[i]
                if (tokens.size >= 4) {
                    val subject = tokens[0]
                    val classLevel = tokens[1]
                    val title = tokens[2]
                    val url = tokens[3]
                    
                    val normalizedClass = if (!classLevel.startsWith("Class")) "Class $classLevel" else classLevel
                    val videoId = extractYoutubeId(url)
                    
                    if (videoId != null) {
                        videos.add(
                            CourseVideo(
                                video_id = videoId,
                                subject = subject,
                                class_level = normalizedClass,
                                title = title
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return videos
    }

    private fun parseVideoExcel(context: Context, uri: Uri): List<CourseVideo> {
        val videos = mutableListOf<CourseVideo>()
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return emptyList()
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)
            
            // Skip header
            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                if (row.lastCellNum >= 4) {
                    val subject = getCellValue(row.getCell(0))
                    val classLevel = getCellValue(row.getCell(1))
                    val title = getCellValue(row.getCell(2))
                    val url = getCellValue(row.getCell(3))
                    
                    val normalizedClass = if (!classLevel.startsWith("Class")) "Class $classLevel" else classLevel
                    val videoId = extractYoutubeId(url)
                    
                    if (videoId != null) {
                        videos.add(
                            CourseVideo(
                                video_id = videoId,
                                subject = subject,
                                class_level = normalizedClass,
                                title = title
                            )
                        )
                    }
                }
            }
            workbook.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return videos
    }

    private fun parseQuestionExcel(context: Context, uri: Uri): List<Question> {
        val questions = mutableListOf<Question>()
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return emptyList()
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)
            
            // Skip header
            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                if (row.lastCellNum >= 8) {
                    val subject = getCellValue(row.getCell(0))
                    var className = getCellValue(row.getCell(1))
                    
                    // Requirement: GK or Current Affairs to all
                    if (subject.trim().lowercase() == "gk" || subject.trim().lowercase().contains("current affairs")) {
                        className = "ALL"
                    }

                    val questionText = getCellValue(row.getCell(2))
                    val optA = getCellValue(row.getCell(3))
                    val optB = getCellValue(row.getCell(4))
                    val optC = getCellValue(row.getCell(5))
                    val optD = getCellValue(row.getCell(6))
                    val correctAnswerLabel = getCellValue(row.getCell(7))
                    
                    val correctAnswer = when(correctAnswerLabel.replace(" ", "").lowercase()) {
                        "optiona" -> optA
                        "optionb" -> optB
                        "optionc" -> optC
                        "optiond" -> optD
                        else -> correctAnswerLabel
                    }
                    
                    questions.add(
                        Question(
                            text = questionText,
                            options = listOf(optA, optB, optC, optD),
                            correctAnswer = correctAnswer,
                            subject = subject,
                            className = className
                        )
                    )
                }
            }
            workbook.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return questions
    }

    private fun getCellValue(cell: org.apache.poi.ss.usermodel.Cell?): String {
        if (cell == null) return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> cell.numericCellValue.toLong().toString()
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            else -> ""
        }.trim()
    }

    private fun getFileExtension(context: Context, uri: Uri): String? {
        return if (uri.scheme == "content") {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(context.contentResolver.getType(uri))
        } else {
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(java.io.File(uri.path!!)).toString())
        }
    }

    fun extractYoutubeId(url: String): String? {
        val cleanUrl = url.trim().removeSurrounding("\"").replace("[", "").replace("]", "").replace("(", "").replace(")", "")
        val patterns = listOf(
            Pattern.compile("v=([a-zA-Z0-9_-]{11})"),
            Pattern.compile("youtu.be/([a-zA-Z0-9_-]{11})"),
            Pattern.compile("embed/([a-zA-Z0-9_-]{11})"),
            Pattern.compile("/v/([a-zA-Z0-9_-]{11})")
        )
        for (p in patterns) {
            val m = p.matcher(cleanUrl)
            if (m.find()) return m.group(1)?.trim()
        }
        
        val potentialId = cleanUrl.split("/").last().split("=").last().trim()
        if (potentialId.length == 11 && Pattern.matches("[a-zA-Z0-9_-]{11}", potentialId)) {
            return potentialId
        }
        return null
    }

    private fun parseCsvString(csvData: String): List<List<String>> {
        val result = mutableListOf<List<String>>()
        var currentToken = StringBuilder()
        var currentRecord = mutableListOf<String>()
        var inQuotes = false
        
        var i = 0
        while (i < csvData.length) {
            val c = csvData[i]
            when {
                c == '\"' -> {
                    if (inQuotes && i + 1 < csvData.length && csvData[i+1] == '\"') {
                        currentToken.append('\"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    currentRecord.add(currentToken.toString())
                    currentToken = StringBuilder()
                }
                (c == '\n' || c == '\r') && !inQuotes -> {
                    if (c == '\r' && i + 1 < csvData.length && csvData[i+1] == '\n') i++
                    currentRecord.add(currentToken.toString())
                    if (currentRecord.isNotEmpty()) {
                        result.add(currentRecord.map { it.trim().removeSurrounding("\"").replace("\"\"", "\"") })
                    }
                    currentRecord = mutableListOf()
                    currentToken = StringBuilder()
                }
                else -> {
                    currentToken.append(c)
                }
            }
            i++
        }
        if (currentToken.isNotEmpty() || currentRecord.isNotEmpty()) {
            currentRecord.add(currentToken.toString())
            result.add(currentRecord.map { it.trim().removeSurrounding("\"").replace("\"\"", "\"") })
        }
        return result
    }
}
