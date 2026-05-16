package com.project.markmyday.algorithm

import android.util.Log
import com.project.markmyday.data.model.DaySchedule
import com.project.markmyday.data.model.Period
import com.project.markmyday.data.model.SubjectQuota
import com.project.markmyday.data.model.Teacher
import com.project.markmyday.data.model.Timetable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

class TimetableGenerator {

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    // 1. Generate Quota
    fun generateDefaultQuota(
        category: String,
        homeTeacher: Teacher?,
        availableTeachers: List<Teacher>
    ): MutableMap<String, SubjectQuota> {
        val quotaMap = mutableMapOf<String, SubjectQuota>()

        val subjectDistribution = when (category) {
            "Primary" -> mapOf(
                "Telugu" to 6, "Hindi" to 6, "English" to 6,
                "Math" to 8, "Science" to 8, "Social" to 8
            ) // Total 42
            "Secondary" -> mapOf(
                "Telugu" to 6, "Hindi" to 6, "English" to 6,
                "Math" to 14, "Science" to 14, "Social" to 14
            ) // Total 60
            "High School" -> mapOf(
                "Telugu" to 6, "Hindi" to 6, "English" to 6,
                "Math" to 11, "Phy" to 11, "Bio" to 10, "Social" to 10
            ) // Total 60
            else -> emptyMap()
        }

        for ((subject, count) in subjectDistribution) {
            val assignedTeacher = if (homeTeacher?.subject == subject) {
                homeTeacher
            } else {
                availableTeachers.firstOrNull { it.subject == subject && it.classesTaughtCategories.contains(category) }
            }

            quotaMap[subject] = SubjectQuota(
                subject = subject,
                classCount = count,
                teacherId = assignedTeacher?.teacherId ?: "UNASSIGNED",
                teacherName = assignedTeacher?.name ?: "No Teacher"
            )
        }
        return quotaMap
    }

    // 2. The Backtracking Scheduler
    suspend fun generateScheduleForClass(
        category: String,
        quotaMap: Map<String, SubjectQuota>,
        homeTeacherId: String,
        allExistingTimetables: List<Timetable>,
        currentClassName: String
    ): Map<String, DaySchedule> = withContext(Dispatchers.Default) {

        val periodsPerDay = if (category == "Primary") 7 else 10
        val totalSlotsInWeek = periodsPerDay * daysOfWeek.size

        // Make a mutable copy of quotas to track remaining classes
        val workingQuota = quotaMap.toMutableMap()
        val assignedClasses = workingQuota.values.sumOf { it.classCount }
        val leisurePeriodsNeeded = totalSlotsInWeek - assignedClasses

        if (leisurePeriodsNeeded > 0) {
            workingQuota["Leisure"] = SubjectQuota("Leisure", "NONE", "Free Period", leisurePeriodsNeeded)
        }

        val schedule = mutableMapOf<String, MutableList<Period?>>()
        for (day in daysOfWeek) {
            schedule[day] = MutableList(periodsPerDay) { null }
        }

        // Pre-fill Period 1 with Home Teacher, BUT respect the quota!
        val homeTeacherSubject = workingQuota.values.firstOrNull { it.teacherId == homeTeacherId }?.subject
        if (homeTeacherSubject != null) {
            for (day in daysOfWeek) {
                val currentCount = workingQuota[homeTeacherSubject]!!.classCount
                if (currentCount > 0) {
                    val (start, end) = getPeriodTimings(1)
                    schedule[day]!![0] = Period(
                        periodNumber = 1,
                        startTime = start,
                        endTime = end,
                        subject = homeTeacherSubject,
                        teacherId = homeTeacherId,
                        teacherName = workingQuota[homeTeacherSubject]!!.teacherName
                    )
                    workingQuota[homeTeacherSubject] = workingQuota[homeTeacherSubject]!!.copy(classCount = currentCount - 1)
                }
            }
        }

        // Start Backtracking from Day 0, Period 0
        val success = solve(schedule, workingQuota, 0, 0, periodsPerDay, allExistingTimetables, currentClassName)
        if (!success) Log.e("TimetableAlgo", "Could not find a 100% conflict-free schedule. Returning partial.")

        // Convert Map<String, MutableList<Period?>> to Map<String, DaySchedule>
        return@withContext schedule.mapValues { (_, periods) ->
            DaySchedule(periods.filterNotNull())
        }
    }

    private suspend fun solve(
        schedule: MutableMap<String, MutableList<Period?>>,
        quota: MutableMap<String, SubjectQuota>,
        dayIndex: Int,
        periodIndex: Int, // 0-indexed internally
        periodsPerDay: Int,
        allExistingTimetables: List<Timetable>,
        currentClassName: String
    ): Boolean {
        yield() // Keep UI responsive

        if (dayIndex == daysOfWeek.size) return true // Done!
        val currentDay = daysOfWeek[dayIndex]

        if (periodIndex >= periodsPerDay) {
            return solve(schedule, quota, dayIndex + 1, 0, periodsPerDay, allExistingTimetables, currentClassName)
        }

        // If slot is already filled (like Home Teacher in Period 1), skip to the next period
        if (schedule[currentDay]!![periodIndex] != null) {
            return solve(schedule, quota, dayIndex, periodIndex + 1, periodsPerDay, allExistingTimetables, currentClassName)
        }

        // Heuristic: Try subjects with the highest remaining classCount first.
        // This prevents getting stuck at the end of the week with 4 classes of the same subject.
        val availableSubjects = quota.values
            .filter { it.classCount > 0 }
            .sortedByDescending { it.classCount }

        for (item in availableSubjects) {
            if (isValidPlacement(schedule, currentDay, periodIndex, item, allExistingTimetables, currentClassName)) {

                // 1. PLACE
                val (start, end) = getPeriodTimings(periodIndex + 1)
                schedule[currentDay]!![periodIndex] = Period(periodIndex + 1, startTime = start, endTime = end, subject = item.subject, teacherId = item.teacherId, teacherName = item.teacherName)
                quota[item.subject] = item.copy(classCount = item.classCount - 1)

                // 2. RECURSE
                if (solve(schedule, quota, dayIndex, periodIndex + 1, periodsPerDay, allExistingTimetables, currentClassName)) {
                    return true
                }

                // 3. BACKTRACK (Safely restore the original snapshot)
                schedule[currentDay]!![periodIndex] = null
                quota[item.subject] = item // Puts the original count back exactly as it was!
            }
        }
        return false
    }

    private fun isValidPlacement(
        schedule: MutableMap<String, MutableList<Period?>>,
        day: String,
        periodIndex: Int,
        item: SubjectQuota,
        allExistingTimetables: List<Timetable>,
        currentClassName: String
    ): Boolean {
        val todaysClasses = schedule[day]!!.filterNotNull()

        if (item.subject == "Leisure") {
            return todaysClasses.count { it.subject == "Leisure" } < 1
        }

        // Rule 1: Cross-class conflict check
        val isTeacherBusyElsewhere = allExistingTimetables.any { tb ->
            tb.className != currentClassName &&
                    tb.weeklySchedule[day]?.periods?.any { it.periodNumber == periodIndex + 1 && it.teacherId == item.teacherId } == true
        }
        if (isTeacherBusyElsewhere) return false

        // Rule 2: Max 3 of same subject per day
        if (todaysClasses.count { it.subject == item.subject } >= 3) return false

        // Rule 3: No 3 consecutive
        if (periodIndex >= 2) {
            val p1 = schedule[day]!![periodIndex - 1]?.subject
            val p2 = schedule[day]!![periodIndex - 2]?.subject
            if (p1 == item.subject && p2 == item.subject) return false
        }

        return true
    }

    private fun getPeriodTimings(periodNumber: Int): Pair<String, String> {
        return when (periodNumber) {
            1 -> "09:00 AM" to "09:45 AM"
            2 -> "09:45 AM" to "10:30 AM"
            3 -> "10:30 AM" to "11:15 AM"
            4 -> "11:30 AM" to "12:15 PM" // After Short Break
            5 -> "12:15 PM" to "01:00 PM"
            6 -> "01:40 PM" to "02:25 PM" // After Lunch Break
            7 -> "02:25 PM" to "03:10 PM"
            8 -> "03:20 PM" to "04:05 PM" // After Short Break
            9 -> "04:05 PM" to "04:50 PM"
            10 -> "04:50 PM" to "05:30 PM"
            else -> "" to ""
        }
    }

}