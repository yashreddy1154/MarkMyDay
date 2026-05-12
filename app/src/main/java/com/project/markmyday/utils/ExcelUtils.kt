package com.project.markmyday.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.project.markmyday.viewmodel.TeacherAttendanceStatus
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExcelUtils {
    data class StudentAttendanceRecord(
        val studentName: String,
        val studentId: String,
        val className: String,
        val date: String,
        val status: String,
        val subject: String
    )

    fun exportAttendanceToExcel(context: Context, presentList: List<TeacherAttendanceStatus>, absentList: List<TeacherAttendanceStatus>) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Attendance")

        // Create Header Row
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Teacher Name")
        headerRow.createCell(1).setCellValue("Teacher ID")
        headerRow.createCell(2).setCellValue("Status")
        headerRow.createCell(3).setCellValue("Scan Time")

        var rowIdx = 1

        // Add Present Teachers
        presentList.forEach { status ->
            val row = sheet.createRow(rowIdx++)
            row.createCell(0).setCellValue(status.teacher.name)
            row.createCell(1).setCellValue(status.teacher.teacherId)
            row.createCell(2).setCellValue("Present")
            row.createCell(3).setCellValue(status.scanTime ?: "N/A")
        }

        // Add Absent Teachers
        absentList.forEach { status ->
            val row = sheet.createRow(rowIdx++)
            row.createCell(0).setCellValue(status.teacher.name)
            row.createCell(1).setCellValue(status.teacher.teacherId)
            row.createCell(2).setCellValue("Absent")
            row.createCell(3).setCellValue("-")
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fileName = "Attendance_$today.xlsx"
        val file = File(context.getExternalFilesDir(null), fileName)

        try {
            val fileOut = FileOutputStream(file)
            workbook.write(fileOut)
            fileOut.close()
            workbook.close()

            shareExcelFile(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareExcelFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Attendance Spreadsheet"))
    }

    fun exportStudentAttendanceToExcel(context: Context, records: List<StudentAttendanceRecord>) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Student Attendance")

        // Create Header Row
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Date")
        headerRow.createCell(1).setCellValue("Student Name")
        headerRow.createCell(2).setCellValue("Student ID")
        headerRow.createCell(3).setCellValue("Class")
        headerRow.createCell(4).setCellValue("Subject")
        headerRow.createCell(5).setCellValue("Status")

        records.forEachIndexed { index, record ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(record.date)
            row.createCell(1).setCellValue(record.studentName)
            row.createCell(2).setCellValue(record.studentId)
            row.createCell(3).setCellValue(record.className)
            row.createCell(4).setCellValue(record.subject)
            row.createCell(5).setCellValue(record.status)
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fileName = "Student_Attendance_Log_$today.xlsx"
        val file = File(context.getExternalFilesDir(null), fileName)

        try {
            val fileOut = FileOutputStream(file)
            workbook.write(fileOut)
            fileOut.close()
            workbook.close()

            shareExcelFile(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
