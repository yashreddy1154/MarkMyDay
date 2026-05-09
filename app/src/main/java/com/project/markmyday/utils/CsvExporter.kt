package com.project.markmyday.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.project.markmyday.data.model.StudentEngagementSummary
import java.io.File
import java.io.FileWriter

object CsvExporter {
    fun generateEngagementCsv(context: Context, summaries: List<StudentEngagementSummary>): Uri? {
        val fileName = "Daily_Engagement_Report_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)
        
        try {
            val writer = FileWriter(file)
            // Header
            writer.append("Student Name,Class,Video Title,Time Spent (Minutes)\n")
            
            for (summary in summaries) {
                for (stat in summary.videoStats.values) {
                    val minutes = stat.timeSpentSeconds / 60
                    writer.append("${summary.studentName},${summary.className},${stat.title},$minutes\n")
                }
            }
            
            writer.flush()
            writer.close()
            
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
