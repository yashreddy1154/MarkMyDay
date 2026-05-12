package com.project.markmyday.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.markmyday.R
import com.project.markmyday.ui.theme.MarkMyDayTheme

@Composable
fun SchoolLogo(
    modifier: Modifier = Modifier
) {
    val darkBlue = Color(0xFF003B73)
    val logoYellow = Color(0xFFFFD700)
    val logoGreen = Color(0xFF4CAF50)
    val lightGreen = Color(0xFF8BC34A)

    Box(
        modifier = modifier
            .size(320.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 - 10.dp.toPx()

            // Outer blue ring
            drawCircle(
                color = darkBlue,
                radius = radius + 8.dp.toPx(),
                style = Stroke(width = 6.dp.toPx())
            )

            // Inner yellow ring
            drawCircle(
                color = logoYellow,
                radius = radius + 2.dp.toPx(),
                style = Stroke(width = 3.dp.toPx())
            )

            // Background Circle (White)
            drawCircle(
                color = Color.White,
                radius = radius
            )

            // Lawn (Green area at the bottom)
            val lawnPath = Path().apply {
                addArc(
                    Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius),
                    0f, 180f
                )
            }
            clipPath(lawnPath) {
                drawRect(
                    color = logoGreen,
                    topLeft = Offset(center.x - radius, center.y + radius * 0.2f),
                    size = Size(radius * 2, radius * 0.8f)
                )
                // Lighter green lawn details
                drawCircle(
                    color = lightGreen,
                    radius = radius * 0.85f,
                    center = Offset(center.x, center.y + radius * 0.5f)
                )
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.8f,
                    center = Offset(center.x, center.y + radius * 0.55f),
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }

        // School Building and Characters
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Building is background
            SchoolBuilding(
                modifier = Modifier.offset(y = (-40).dp),
                darkBlue = darkBlue,
                logoYellow = logoYellow
            )
            
            // Characters are foreground
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.offset(y = 20.dp)
            ) {
                Character(isAdult = false, color = darkBlue, logoYellow = logoYellow)
                Spacer(modifier = Modifier.width(8.dp))
                Character(isAdult = true, color = darkBlue, logoYellow = logoYellow)
                Spacer(modifier = Modifier.width(8.dp))
                Character(isAdult = false, color = darkBlue, logoYellow = logoYellow)
            }
        }

        // Banner
        Banner(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 50.dp),
            darkBlue = darkBlue,
            logoYellow = logoYellow,
            text = stringResource(R.string.logo_motto)
        )
        
        // Bottom Decoration
        BottomDecoration(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 15.dp),
            logoYellow = logoYellow,
            logoGreen = logoGreen
        )
    }
}

@Composable
private fun SchoolBuilding(modifier: Modifier = Modifier, darkBlue: Color, logoYellow: Color) {
    Canvas(modifier = modifier.size(120.dp, 100.dp)) {
        // Main block
        drawRect(color = logoYellow, topLeft = Offset(30.dp.toPx(), 25.dp.toPx()), size = Size(60.dp.toPx(), 70.dp.toPx()))
        // Side blocks
        drawRect(color = logoYellow, topLeft = Offset(5.dp.toPx(), 45.dp.toPx()), size = Size(25.dp.toPx(), 50.dp.toPx()))
        drawRect(color = logoYellow, topLeft = Offset(90.dp.toPx(), 45.dp.toPx()), size = Size(25.dp.toPx(), 50.dp.toPx()))
        
        // Roofs
        val mainRoof = Path().apply {
            moveTo(28.dp.toPx(), 25.dp.toPx())
            lineTo(60.dp.toPx(), 5.dp.toPx())
            lineTo(92.dp.toPx(), 25.dp.toPx())
            close()
        }
        drawPath(mainRoof, darkBlue)
        
        val leftRoof = Path().apply {
            moveTo(2.dp.toPx(), 45.dp.toPx())
            lineTo(33.dp.toPx(), 45.dp.toPx())
            lineTo(33.dp.toPx(), 40.dp.toPx())
            lineTo(2.dp.toPx(), 40.dp.toPx())
            close()
        }
        drawPath(leftRoof, darkBlue)
        
        val rightRoof = Path().apply {
            moveTo(87.dp.toPx(), 45.dp.toPx())
            lineTo(118.dp.toPx(), 45.dp.toPx())
            lineTo(118.dp.toPx(), 40.dp.toPx())
            lineTo(87.dp.toPx(), 40.dp.toPx())
            close()
        }
        drawPath(rightRoof, darkBlue)

        // Clock
        drawCircle(Color.White, radius = 10.dp.toPx(), center = Offset(60.dp.toPx(), 32.dp.toPx()))
        drawCircle(darkBlue, radius = 10.dp.toPx(), center = Offset(60.dp.toPx(), 32.dp.toPx()), style = Stroke(2.dp.toPx()))
        
        // Windows
        val winSize = Size(8.dp.toPx(), 10.dp.toPx())
        drawRect(darkBlue, Offset(40.dp.toPx(), 50.dp.toPx()), winSize)
        drawRect(darkBlue, Offset(40.dp.toPx(), 65.dp.toPx()), winSize)
        drawRect(darkBlue, Offset(72.dp.toPx(), 50.dp.toPx()), winSize)
        drawRect(darkBlue, Offset(72.dp.toPx(), 65.dp.toPx()), winSize)
        
        drawRect(darkBlue, Offset(12.dp.toPx(), 55.dp.toPx()), winSize)
        drawRect(darkBlue, Offset(12.dp.toPx(), 75.dp.toPx()), winSize)
        drawRect(darkBlue, Offset(100.dp.toPx(), 55.dp.toPx()), winSize)
        drawRect(darkBlue, Offset(100.dp.toPx(), 75.dp.toPx()), winSize)
        
        // Door
        val doorPath = Path().apply {
            addRoundRect(RoundRect(Rect(50.dp.toPx(), 75.dp.toPx(), 70.dp.toPx(), 95.dp.toPx()), cornerRadius = CornerRadius(10.dp.toPx())))
        }
        drawPath(doorPath, darkBlue)
        
        // Flag
        drawLine(darkBlue, Offset(60.dp.toPx(), 5.dp.toPx()), Offset(60.dp.toPx(), -15.dp.toPx()), strokeWidth = 2.dp.toPx())
        val flagPath = Path().apply {
            moveTo(60.dp.toPx(), -15.dp.toPx())
            lineTo(80.dp.toPx(), -10.dp.toPx())
            lineTo(60.dp.toPx(), -5.dp.toPx())
            close()
        }
        drawPath(flagPath, darkBlue)
    }
}

@Composable
private fun Character(isAdult: Boolean, color: Color, logoYellow: Color) {
    val height = if (isAdult) 90.dp else 70.dp
    val width = if (isAdult) 50.dp else 40.dp
    
    Canvas(modifier = Modifier.size(width, height)) {
        // Hair/Head
        drawCircle(color, radius = (width / 3).toPx(), center = Offset(size.width / 2, (height / 4).toPx()))
        
        // Body
        val bodyPath = Path().apply {
            moveTo(size.width * 0.1f, size.height)
            lineTo(size.width * 0.9f, size.height)
            lineTo(size.width * 0.85f, (height / 2.2f).toPx())
            lineTo(size.width * 0.15f, (height / 2.2f).toPx())
            close()
        }
        drawPath(bodyPath, color)
        
        // Collar/Yellow details
        val collarPath = Path().apply {
            moveTo(size.width * 0.4f, (height / 2.2f).toPx())
            lineTo(size.width * 0.5f, (height / 1.8f).toPx())
            lineTo(size.width * 0.6f, (height / 2.2f).toPx())
        }
        drawPath(collarPath, logoYellow, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
private fun Banner(modifier: Modifier, darkBlue: Color, logoYellow: Color, text: String) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // Side folds
            val leftFold = Path().apply {
                moveTo(w * 0.2f, h * 0.2f)
                lineTo(w * 0.1f, h * 0.4f)
                lineTo(w * 0.2f, h * 0.6f)
                close()
            }
            drawPath(leftFold, logoYellow)
            
            val rightFold = Path().apply {
                moveTo(w * 0.8f, h * 0.2f)
                lineTo(w * 0.9f, h * 0.4f)
                lineTo(w * 0.8f, h * 0.6f)
                close()
            }
            drawPath(rightFold, logoYellow)
            
            // Main ribbon
            val ribbonPath = Path().apply {
                moveTo(w * 0.2f, h * 0.2f)
                quadraticTo(w * 0.5f, h * 0.5f, w * 0.8f, h * 0.2f)
                lineTo(w * 0.8f, h * 0.7f)
                quadraticTo(w * 0.5f, h, w * 0.2f, h * 0.7f)
                close()
            }
            drawPath(ribbonPath, darkBlue)
        }
        
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun BottomDecoration(modifier: Modifier, logoYellow: Color, logoGreen: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        // Left Leaves
        Canvas(modifier = Modifier.size(50.dp, 25.dp)) {
            val leafPath = Path().apply {
                moveTo(size.width, size.height / 2)
                quadraticTo(size.width / 2, 0f, 0f, size.height / 2)
                quadraticTo(size.width / 2, size.height, size.width, size.height / 2)
            }
            drawPath(leafPath, logoGreen)
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Star
        Canvas(modifier = Modifier.size(28.dp)) {
            val path = Path().apply {
                val cx = size.width / 2
                val cy = size.height / 2
                val outerRadius = size.width / 2
                val innerRadius = size.width / 4
                for (i in 0 until 10) {
                    val angle = Math.toRadians(i * 36.0 - 90.0)
                    val r = if (i % 2 == 0) outerRadius else innerRadius
                    val x = cx + r * Math.cos(angle).toFloat()
                    val y = cy + r * Math.sin(angle).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(path, logoYellow)
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Right Leaves
        Canvas(modifier = Modifier.size(50.dp, 25.dp)) {
            val leafPath = Path().apply {
                moveTo(0f, size.height / 2)
                quadraticTo(size.width / 2, 0f, size.width, size.height / 2)
                quadraticTo(size.width / 2, size.height, 0f, size.height / 2)
            }
            drawPath(leafPath, logoGreen)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SchoolLogoPreview() {
    MarkMyDayTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            SchoolLogo()
        }
    }
}
