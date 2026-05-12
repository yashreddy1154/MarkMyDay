package com.project.markmyday.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.markmyday.R
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.tooling.preview.Preview

/**
 * Pixelated Retro Attendance Screen for Home Teacher / 1st Period Teacher
 */

data class RetroStudent(
    val id: Int,
    val rollNo: String,
    val name: String,
    var isPresent: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetroAttendanceScreen(onBack: () -> Unit = {}) {
    // Sample student data
    val students = remember {
        mutableStateListOf(
            RetroStudent(1, "01", "ARIANA GRANDE", false),
            RetroStudent(2, "02", "BILLIE EILISH", false),
            RetroStudent(3, "03", "CHAPPELL ROAN", false),
            RetroStudent(4, "04", "DOJA CAT", false),
            RetroStudent(5, "05", "FINNEAS", false),
            RetroStudent(6, "06", "GRACIE ABRAMS", false),
            RetroStudent(7, "07", "HALSEY", false),
            RetroStudent(8, "08", "ICE SPICE", false)
        )
    }

    var showSnackbar by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val animatedBgColor by animateColorAsState(
        targetValue = Color(0xFF0A0F0A),
        animationSpec = tween(3000, easing = LinearEasing),
        label = "bg"
    )

    val scanlineOffset = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scanlineOffset.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(showSnackbar) {
        showSnackbar?.let {
            snackbarHostState.showSnackbar(it)
            showSnackbar = null
        }
    }

    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    val periodInfo = "⚡ PERIOD 1 · HOME TEACHER ⚡"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(animatedBgColor)
                .pixelGridOverlay()
                .scanlineOverlay(scanlineOffset.value)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        BlinkingCursorText(
                            text = ">_ ATTENDANCE_MATRIX",
                            fontSize = 20.sp,
                            color = Color(0xFF33FF33)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = periodInfo,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFF88FF88),
                            letterSpacing = 2.sp
                        )
                    }
                    Text(
                        text = currentDate,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = Color(0xFF55FF55),
                        modifier = Modifier
                            .border(1.dp, Color(0xFF33FF33), RoundedCornerShape(0.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                PixelBorderCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = Color(0xFF33FF33)
                ) {
                    val presentCount = students.count { it.isPresent }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatChip("PRESENT", presentCount, Color(0xFF33FF33))
                        StatChip("ABSENT", students.size - presentCount, Color(0xFFFF5555))
                        StatChip("TOTAL", students.size, Color(0xFFFFAA44))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RetroPixelButton(
                        text = "ALL PRESENT",
                        onClick = {
                            students.forEachIndexed { index, s -> students[index] = s.copy(isPresent = true) }
                            showSnackbar = "✅ ALL STUDENTS MARKED PRESENT"
                        },
                        backgroundColor = Color(0xFF115511),
                        borderColor = Color(0xFF33FF33),
                        modifier = Modifier.weight(1f)
                    )
                    RetroPixelButton(
                        text = "ALL ABSENT",
                        onClick = {
                            students.forEachIndexed { index, s -> students[index] = s.copy(isPresent = false) }
                            showSnackbar = "❌ ALL STUDENTS MARKED ABSENT"
                        },
                        backgroundColor = Color(0xFF331111),
                        borderColor = Color(0xFFFF5555),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(students, key = { it.id }) { student ->
                        AnimatedPixelStudentRow(
                            student = student,
                            onToggle = { newStatus ->
                                val index = students.indexOf(student)
                                if (index != -1) {
                                    students[index] = student.copy(isPresent = newStatus)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                RetroPixelButton(
                    text = "📀 COMMIT ATTENDANCE 📀",
                    onClick = {
                        val presentCount = students.count { it.isPresent }
                        showSnackbar = "SAVED: $presentCount present"
                    },
                    backgroundColor = Color(0xFF003300),
                    borderColor = Color(0xFFAAFFAA),
                    modifier = Modifier.fillMaxWidth(),
                    textColor = Color(0xFFAAFFAA),
                    extraGlow = true
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AnimatedPixelStudentRow(
    student: RetroStudent,
    onToggle: (Boolean) -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (student.isPresent) Color(0xFF113311) else Color(0xFF1A1111),
        animationSpec = tween(150),
        label = "rowBg"
    )
    val borderColor = if (student.isPresent) Color(0xFF66FF66) else Color(0xFF993333)

    PixelBorderCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = borderColor,
        innerPadding = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${student.rollNo}  ${student.name}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = Color(0xFFCCFFCC),
                    letterSpacing = 0.5.sp,
                    style = TextStyle.Default.copy(
                        shadow = Shadow(
                            color = Color(0xFF33FF33).copy(alpha = 0.5f),
                            offset = Offset(1f, 1f),
                            blurRadius = 0f
                        )
                    )
                )
            }
            RetroPixelButton(
                text = if (student.isPresent) "● PRESENT" else "○ ABSENT",
                onClick = { onToggle(!student.isPresent) },
                backgroundColor = if (student.isPresent) Color(0xFF226622) else Color(0xFF442222),
                borderColor = if (student.isPresent) Color(0xFF88FF88) else Color(0xFFFF8888),
                textColor = if (student.isPresent) Color(0xFFAAFFAA) else Color(0xFFFFAAAA),
                modifier = Modifier.defaultMinSize(minWidth = 100.dp),
                extraGlow = false,
                animatedScale = true
            )
        }
    }
}

@Composable
fun PixelBorderCard(
    modifier: Modifier = Modifier,
    borderColor: Color,
    innerPadding: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .border(2.dp, borderColor, RoundedCornerShape(0.dp))
            .background(Color(0xFF050805))
            .padding(innerPadding)
            .drawWithCache {
                onDrawBehind {
                    val strokeWidth = 2.dp.toPx()
                    val cornerSize = 6.dp.toPx()
                    drawLine(borderColor, Offset(0f, cornerSize), Offset(0f, strokeWidth), strokeWidth = strokeWidth)
                    drawLine(borderColor, Offset(cornerSize, 0f), Offset(strokeWidth, 0f), strokeWidth = strokeWidth)
                    drawLine(borderColor, Offset(size.width, cornerSize), Offset(size.width, strokeWidth), strokeWidth = strokeWidth)
                    drawLine(borderColor, Offset(size.width - cornerSize, 0f), Offset(size.width - strokeWidth, 0f), strokeWidth = strokeWidth)
                    drawLine(borderColor, Offset(0f, size.height - cornerSize), Offset(0f, size.height - strokeWidth), strokeWidth = strokeWidth)
                    drawLine(borderColor, Offset(cornerSize, size.height), Offset(strokeWidth, size.height), strokeWidth = strokeWidth)
                    drawLine(borderColor, Offset(size.width, size.height - cornerSize), Offset(size.width, size.height - strokeWidth), strokeWidth = strokeWidth)
                    drawLine(borderColor, Offset(size.width - cornerSize, size.height), Offset(size.width - strokeWidth, size.height), strokeWidth = strokeWidth)
                }
            },
        content = content
    )
}

@Composable
fun StatChip(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = color.copy(alpha = 0.8f),
            letterSpacing = 1.sp
        )
        Text(
            text = value.toString().padStart(2, '0'),
            fontFamily = FontFamily.Monospace,
            fontSize = 24.sp,
            color = color
        )
    }
}

@Composable
fun RetroPixelButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    textColor: Color = Color(0xFFCCFFCC),
    extraGlow: Boolean = false,
    animatedScale: Boolean = false
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed && animatedScale) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Button(
        onClick = {
            pressed = true
            onClick()
        },
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        modifier = modifier
            .scale(scale)
            .border(1.dp, borderColor, RoundedCornerShape(0.dp)),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = textColor,
            letterSpacing = 1.sp
        )
    }
    
    LaunchedEffect(pressed) {
        if (pressed) {
            delay(150)
            pressed = false
        }
    }
}

@Composable
fun BlinkingCursorText(text: String, fontSize: TextUnit, color: Color) {
    var blinkState by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(530)
            blinkState = !blinkState
        }
    }
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = fontSize,
            color = color
        )
        Text(
            text = if (blinkState) "_" else " ",
            fontFamily = FontFamily.Monospace,
            fontSize = fontSize,
            color = color
        )
    }
}

fun Modifier.scanlineOverlay(offset: Float): Modifier = this.drawWithContent {
    drawContent()
    val lineSpacing = 6.dp.toPx()
    val startY = offset * lineSpacing
    var y = startY - lineSpacing * 2
    while (y < size.height) {
        drawLine(
            Color.White.copy(alpha = 0.05f),
            Offset(0f, y),
            Offset(size.width, y),
            strokeWidth = 1.dp.toPx()
        )
        y += lineSpacing
    }
    drawRect(
        color = Color(0xFF33FF33).copy(alpha = 0.06f),
        topLeft = Offset(size.width * 0.2f, size.height * offset),
        size = Size(size.width * 0.6f, 6.dp.toPx())
    )
}

fun Modifier.pixelGridOverlay(): Modifier = this.drawWithContent {
    drawContent()
    val gridSize = 12.dp.toPx()
    val divisions = (size.width / gridSize).toInt()
    if (divisions > 0) {
        val step = size.width / divisions
        for (x in 0..divisions) {
            drawLine(
                color = Color(0xFF33FF33).copy(alpha = 0.03f),
                start = Offset(x * step, 0f),
                end = Offset(x * step, size.height),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
    val yDivisions = (size.height / gridSize).toInt()
    for (y in 0..yDivisions) {
        drawLine(
            color = Color(0xFF33FF33).copy(alpha = 0.03f),
            start = Offset(0f, y * gridSize),
            end = Offset(size.width, y * gridSize),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewRetroAttendance() {
    MaterialTheme {
        RetroAttendanceScreen()
    }
}
