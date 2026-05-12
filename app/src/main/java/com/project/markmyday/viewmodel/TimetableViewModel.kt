package com.project.markmyday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.project.markmyday.data.model.Student
import com.project.markmyday.data.model.Teacher
import com.project.markmyday.data.model.Timetable
import com.project.markmyday.data.repository.StudentRepository
import com.project.markmyday.data.repository.TeacherRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class TimetableState {
    object Idle : TimetableState()
    object Loading : TimetableState()
    object Success : TimetableState()
    data class Error(val message: String) : TimetableState()
}

class TimetableViewModel(
    private val teacherRepository: TeacherRepository = TeacherRepository(),
    private val studentRepository: StudentRepository = StudentRepository(),
    private val timetableRepository: com.project.markmyday.data.repository.TimetableRepository = com.project.markmyday.data.repository.TimetableRepository(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _state = MutableStateFlow<TimetableState>(TimetableState.Idle)
    val state: StateFlow<TimetableState> = _state.asStateFlow()

    private val _allTeachers = MutableStateFlow<List<Teacher>>(emptyList())
    val allTeachers: StateFlow<List<Teacher>> = _allTeachers.asStateFlow()

    private val _allStudents = MutableStateFlow<List<Student>>(emptyList())
    val allStudents: StateFlow<List<Student>> = _allStudents.asStateFlow()

    private val _allTimetables = MutableStateFlow<List<Timetable>>(emptyList())
    val allTimetables: StateFlow<List<Timetable>> = _allTimetables.asStateFlow()

    private val _selectedClassName = MutableStateFlow<String?>(null)
    val selectedClassName: StateFlow<String?> = _selectedClassName.asStateFlow()

    private val _selectedDay = MutableStateFlow("Monday")
    val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()

    // Key: Class name (e.g., "Class 1"), Value: Selected Teacher
    private val _classTeacherAssignments = MutableStateFlow<Map<String, Teacher?>>(
        (1..10).associate { "Class $it" to null }
    )
    val classTeacherAssignments: StateFlow<Map<String, Teacher?>> = _classTeacherAssignments.asStateFlow()

    // Key: Class name, Value: List of Student IDs
    private val _classStudentAssignments = MutableStateFlow<Map<String, List<String>>>(
        (1..10).associate { "Class $it" to emptyList() }
    )
    val classStudentAssignments: StateFlow<Map<String, List<String>>> = _classStudentAssignments.asStateFlow()

    init {
        fetchTeachers()
        fetchStudents()
        fetchTimetables()
    }

    private fun fetchTeachers() {
        viewModelScope.launch {
            teacherRepository.getAllTeachers().collect { teachers ->
                _allTeachers.value = teachers
            }
        }
    }

    private fun fetchStudents() {
        viewModelScope.launch {
            studentRepository.getAllStudents().collect { students ->
                _allStudents.value = students
                
                // Initialize students for each class if not already loaded from Firestore
                val currentAssignments = _classStudentAssignments.value.toMutableMap()
                (1..10).forEach { i ->
                    val className = "Class $i"
                    if (currentAssignments[className].isNullOrEmpty()) {
                        val classStudents = students.filter { it.studentClass == i.toString() }
                        currentAssignments[className] = classStudents.map { it.studentId }
                    }
                }
                _classStudentAssignments.value = currentAssignments
            }
        }
    }

    private fun fetchTimetables() {
        viewModelScope.launch {
            timetableRepository.getAllTimetables().collect { timetables ->
                _allTimetables.value = timetables
                
                val currentTeacherAssignments = _classTeacherAssignments.value.toMutableMap()
                val currentStudentAssignments = _classStudentAssignments.value.toMutableMap()
                
                timetables.forEach { timetable ->
                    val teacher = _allTeachers.value.find { it.teacherId == timetable.homeTeacherId }
                    if (teacher != null) {
                        currentTeacherAssignments[timetable.className] = teacher
                    }
                    if (timetable.studentList.isNotEmpty()) {
                        currentStudentAssignments[timetable.className] = timetable.studentList
                    }
                }
                _classTeacherAssignments.value = currentTeacherAssignments
                _classStudentAssignments.value = currentStudentAssignments
            }
        }
    }

    fun selectClass(className: String) {
        _selectedClassName.value = className
        // No longer changing step here, as it's part of Step 3 UI
    }

    fun selectDay(day: String) {
        _selectedDay.value = day
    }

    fun isTeacherBusy(teacherId: String, day: String, periodNumber: Int): Boolean {
        return _allTimetables.value.any { timetable ->
            val daySchedule = timetable.weeklySchedule[day]
            daySchedule?.periods?.any { it.periodNumber == periodNumber && it.teacherId == teacherId } ?: false
        }
    }

    fun getSubjectQuotas(className: String): Map<String, Pair<Int, Int>> {
        val timetable = _allTimetables.value.find { it.className == className } ?: return emptyMap()
        val counts = mutableMapOf<String, Int>()
        
        timetable.weeklySchedule.values.forEach { daySchedule ->
            daySchedule.periods.forEach { period ->
                if (period.subject.isNotBlank()) {
                    counts[period.subject] = counts.getOrDefault(period.subject, 0) + 1
                }
            }
        }

        // Target quotas from the model if available, otherwise defaults
        val targets: Map<String, Int> = if (timetable.weeklyQuota.isNotEmpty()) {
            timetable.weeklyQuota.mapValues { it.value.classCount }
        } else {
            mapOf(
                "Math" to 7,
                "Telugu" to 6,
                "English" to 6,
                "Science" to 5,
                "Social" to 5,
                "Hindi" to 5
            )
        }

        return targets.mapValues { (subject, target) ->
            counts.getOrDefault(subject, 0) to target
        }
    }

    fun savePeriod(
        className: String,
        day: String,
        periodNumber: Int,
        startTime: String,
        endTime: String,
        subject: String,
        teacher: Teacher?
    ) {
        viewModelScope.launch {
            _state.value = TimetableState.Loading
            try {
                val currentTimetable = _allTimetables.value.find { it.className == className } ?: Timetable(className = className)
                val currentWeeklySchedule = currentTimetable.weeklySchedule.toMutableMap()
                val currentDaySchedule = currentWeeklySchedule[day] ?: com.project.markmyday.data.model.DaySchedule()
                val currentPeriods = currentDaySchedule.periods.toMutableList()
                
                val existingPeriodIndex = currentPeriods.indexOfFirst { it.periodNumber == periodNumber }
                val newPeriod = com.project.markmyday.data.model.Period(
                    periodNumber = periodNumber,
                    startTime = startTime,
                    endTime = endTime,
                    subject = subject,
                    teacherId = teacher?.teacherId ?: "",
                    teacherName = teacher?.name ?: ""
                )

                if (existingPeriodIndex != -1) {
                    currentPeriods[existingPeriodIndex] = newPeriod
                } else {
                    currentPeriods.add(newPeriod)
                }

                currentWeeklySchedule[day] = currentDaySchedule.copy(periods = currentPeriods.sortedBy { it.periodNumber })
                val updatedTimetable = currentTimetable.copy(weeklySchedule = currentWeeklySchedule)
                
                timetableRepository.updateTimetable(updatedTimetable)
                _state.value = TimetableState.Success
            } catch (e: Exception) {
                _state.value = TimetableState.Error(e.localizedMessage ?: "Failed to save period")
            }
        }
    }

    fun assignTeacherToClass(className: String, teacher: Teacher?) {
        val currentMap = _classTeacherAssignments.value.toMutableMap()
        
        // Rule: One teacher must have only one home section.
        // If the teacher is already selected for another class, deselect them for other class.
        if (teacher != null) {
            currentMap.entries.forEach { (cls, t) ->
                if (t?.teacherId == teacher.teacherId) {
                    currentMap[cls] = null
                }
            }
        }
        
        // Rule: One class must only have one home teacher.
        currentMap[className] = teacher
        _classTeacherAssignments.value = currentMap
    }

    fun updateClassStudents(className: String, studentIds: List<String>) {
        val currentMap = _classStudentAssignments.value.toMutableMap()
        currentMap[className] = studentIds
        _classStudentAssignments.value = currentMap
    }

    fun nextStep() {
        if (_currentStep.value < 3) {
            when (_currentStep.value) {
                1 -> saveStep1Data()
                2 -> saveStep2Data()
                else -> _currentStep.value += 1
            }
        }
    }

    private fun saveStep1Data() {
        viewModelScope.launch {
            _state.value = TimetableState.Loading
            try {
                val assignments = _classTeacherAssignments.value
                
                // Update 'timetable' and 'teachers'
                assignments.forEach { (className, teacher) ->
                    if (teacher != null) {
                        // We use merge here to preserve studentIds if they exist
                        val timetableData = hashMapOf(
                            "className" to className,
                            "homeTeacherId" to teacher.teacherId,
                            "homeTeacherName" to teacher.name
                        )
                        firestore.collection("timetable").document(className)
                            .set(timetableData, com.google.firebase.firestore.SetOptions.merge()).await()
                        
                        // 2. Update 'teachers' collection
                        val updatedTeacher = teacher.copy(homeSection = className)
                        teacherRepository.updateTeacher(updatedTeacher)

                        // 3. Update 'users' collection
                        val userQuery = firestore.collection("users")
                            .whereEqualTo("teacherId", teacher.teacherId)
                            .get()
                            .await()
                        
                        for (doc in userQuery.documents) {
                            firestore.collection("users").document(doc.id)
                                .update("home_section", className).await()
                        }
                    }
                }
                
                _state.value = TimetableState.Success
                _currentStep.value = 2
            } catch (e: Exception) {
                _state.value = TimetableState.Error(e.localizedMessage ?: "Failed to save assignments")
            }
        }
    }

    private fun saveStep2Data() {
        viewModelScope.launch {
            _state.value = TimetableState.Loading
            try {
                val studentAssignments = _classStudentAssignments.value
                
                studentAssignments.forEach { (className, studentIds) ->
                    if (studentIds.isNotEmpty()) {
                        val data = mapOf("studentList" to studentIds)
                        firestore.collection("timetable").document(className)
                            .set(data, com.google.firebase.firestore.SetOptions.merge()).await()
                    }
                }
                
                _state.value = TimetableState.Success
                _currentStep.value = 3
            } catch (e: Exception) {
                _state.value = TimetableState.Error(e.localizedMessage ?: "Failed to save student list")
            }
        }
    }

    fun goToStep(step: Int) {
        if (step in 1..3) {
            _currentStep.value = step
        }
    }

    fun getTeacherConflict(teacherId: String, day: String, periodNumber: Int, currentClassName: String): String? {
        return _allTimetables.value.find { timetable ->
            timetable.className != currentClassName &&
            timetable.weeklySchedule[day]?.periods?.any { it.periodNumber == periodNumber && it.teacherId == teacherId } == true
        }?.className
    }

    fun saveWeeklyQuota(className: String, quotaMap: Map<String, com.project.markmyday.data.model.SubjectQuota>, total: Int) {
        viewModelScope.launch {
            _state.value = TimetableState.Loading
            try {
                val currentTimetable = _allTimetables.value.find { it.className == className } ?: Timetable(className = className)
                val updatedTimetable = currentTimetable.copy(
                    weeklyQuota = quotaMap,
                    totalWeeklyClasses = total
                )
                timetableRepository.updateTimetable(updatedTimetable)
                _state.value = TimetableState.Success
            } catch (e: Exception) {
                _state.value = TimetableState.Error(e.localizedMessage ?: "Failed to save quota")
            }
        }
    }

    fun saveWeeklySchedule(className: String, schedule: Map<String, com.project.markmyday.data.model.DaySchedule>) {
        viewModelScope.launch {
            _state.value = TimetableState.Loading
            try {
                val currentTimetable = _allTimetables.value.find { it.className == className } ?: Timetable(className = className)
                val updatedTimetable = currentTimetable.copy(weeklySchedule = schedule)
                timetableRepository.updateTimetable(updatedTimetable)
                _state.value = TimetableState.Success
            } catch (e: Exception) {
                _state.value = TimetableState.Error(e.localizedMessage ?: "Failed to save schedule")
            }
        }
    }

    fun previousStep() {
        if (_currentStep.value > 1) {
            _currentStep.value -= 1
        }
    }

    fun resetState() {
        _state.value = TimetableState.Idle
    }
}
