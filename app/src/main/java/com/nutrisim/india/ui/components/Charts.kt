package com.nutrisim.india.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutrisim.india.ui.theme.*

// ─── Macro Donut Pie Chart ────────────────────────────────────────────────────
@Composable
fun MacroPieChart(
    protein: Float,
    carbs: Float,
    fats: Float,
    totalCalories: Float,
    modifier: Modifier = Modifier
) {
    val total = protein + carbs + fats
    val isEmpty = total <= 0f

    data class Segment(val label: String, val value: Float, val color: Color, val grams: Float)

    val segments = if (isEmpty) {
        listOf(Segment("No data", 1f, CardDark2, 0f))
    } else {
        listOf(
            Segment("Protein", protein / total, ProteinColor, protein),
            Segment("Carbs",   carbs / total,   CarbsColor,   carbs),
            Segment("Fats",    fats / total,     FatsColor,    fats)
        )
    }

    var triggered by remember(protein, carbs, fats) { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "pie"
    )
    LaunchedEffect(protein, carbs, fats) { triggered = true }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = size.minDimension * 0.17f
                val radius      = size.minDimension / 2f - strokeWidth / 2f
                val cx = size.width / 2f
                val cy = size.height / 2f

                var startAngle = -90f
                for (seg in segments) {
                    val sweep = seg.value * 360f * progress
                    drawArc(
                        color      = seg.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter  = false,
                        topLeft    = Offset(cx - radius, cy - radius),
                        size       = Size(radius * 2f, radius * 2f),
                        style      = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweep
                }

                // Background track when empty
                if (isEmpty) {
                    drawCircle(color = CardDark2, radius = radius, center = Offset(cx, cy), style = Stroke(width = strokeWidth))
                }
            }

            // Centre label
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isEmpty) "—" else "${totalCalories.toInt()}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isEmpty) TextTertiary else TextPrimary
                )
                Text("kcal", fontSize = 11.sp, color = TextSecondary)
            }
        }

        Spacer(Modifier.height(14.dp))

        // Legend row
        if (!isEmpty) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                segments.forEach { seg ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = seg.color, modifier = Modifier.size(10.dp)) {}
                        Spacer(Modifier.width(5.dp))
                        Column {
                            Text(
                                text = "${seg.grams.toInt()}g",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = seg.color
                            )
                            Text(seg.label, fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

// ─── Macro Progress Bar Chart ─────────────────────────────────────────────────
@Composable
fun MacroBarChart(
    protein: Float, proteinGoal: Float,
    carbs: Float,   carbsGoal: Float,
    fats: Float,    fatsGoal: Float,
    calories: Float, caloriesGoal: Float,
    modifier: Modifier = Modifier
) {
    data class Bar(val label: String, val value: Float, val goal: Float, val color: Color, val unit: String)

    val bars = listOf(
        Bar("Calories", calories, caloriesGoal, CaloriesColor, "kcal"),
        Bar("Protein",  protein,  proteinGoal,  ProteinColor,  "g"),
        Bar("Carbs",    carbs,    carbsGoal,    CarbsColor,    "g"),
        Bar("Fats",     fats,     fatsGoal,     FatsColor,     "g")
    )

    var triggered by remember(protein, carbs, fats, calories) { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "bars"
    )
    LaunchedEffect(protein, carbs, fats, calories) { triggered = true }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        bars.forEach { bar ->
            val fraction = if (bar.goal > 0f) (bar.value / bar.goal) else 0f
            val isOver   = fraction > 1f

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(bar.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(
                        text = "${bar.value.toInt()} / ${bar.goal.toInt()} ${bar.unit}",
                        fontSize = 11.sp,
                        color = if (isOver) ErrorRed else TextSecondary
                    )
                }
                Spacer(Modifier.height(5.dp))

                Canvas(modifier = Modifier.fillMaxWidth().height(12.dp)) {
                    // Track
                    drawRoundRect(
                        color        = CardDark2,
                        cornerRadius = CornerRadius(6f),
                        size         = size
                    )
                    // Fill
                    val fillW = (size.width * fraction.coerceIn(0f, 1f) * progress)
                    if (fillW > 0f) {
                        drawRoundRect(
                            color        = bar.color,
                            cornerRadius = CornerRadius(6f),
                            size         = Size(fillW, size.height)
                        )
                    }
                    // Overshoot marker
                    if (isOver && progress >= 1f) {
                        drawRoundRect(
                            color        = ErrorRed.copy(alpha = 0.8f),
                            cornerRadius = CornerRadius(6f),
                            topLeft      = Offset(size.width - 10f, 0f),
                            size         = Size(10f, size.height)
                        )
                    }
                }
            }
        }
    }
}

// ─── Amino Acid Column Chart ──────────────────────────────────────────────────
@Composable
fun AminoAcidChart(
    leucine: Float,
    lysine: Float,
    methionine: Float,
    valine: Float,
    isoleucine: Float,
    modifier: Modifier = Modifier
) {
    // RDA for a 70kg adult (g/day — WHO)
    data class AminoBar(val name: String, val value: Float, val rda: Float, val color: Color)

    val bars = listOf(
        AminoBar("Leu", leucine,    2.73f, Color(0xFF7C4DFF)),
        AminoBar("Lys", lysine,     2.10f, Color(0xFF00BCD4)),
        AminoBar("Met", methionine, 1.05f, Color(0xFFFF5722)),
        AminoBar("Val", valine,     1.82f, Color(0xFF8BC34A)),
        AminoBar("Ile", isoleucine, 1.40f, Color(0xFFFF9800))
    )

    val maxRda = bars.maxOf { it.rda } * 1.3f

    var triggered by remember(leucine) { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "amino"
    )
    LaunchedEffect(leucine) { triggered = true }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            val slotW   = size.width / bars.size
            val barW    = slotW * 0.55f
            val offsetX = (slotW - barW) / 2f

            bars.forEachIndexed { i, bar ->
                val x    = i * slotW + offsetX
                val rdaY = size.height - (bar.rda / maxRda * size.height)
                val barH = (bar.value / maxRda * size.height) * progress

                // RDA line
                drawRect(
                    color   = Color.White.copy(alpha = 0.25f),
                    topLeft = Offset(x, rdaY),
                    size    = Size(barW, 2f)
                )

                // Bar
                if (barH > 0f) {
                    val barTop = size.height - barH
                    drawRoundRect(
                        color        = bar.color.copy(alpha = 0.88f),
                        topLeft      = Offset(x, barTop),
                        size         = Size(barW, barH),
                        cornerRadius = CornerRadius(4f)
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            bars.forEach { bar ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(bar.name, fontSize = 11.sp, color = bar.color, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (bar.value > 0f) "${String.format("%.1f", bar.value)}g" else "—",
                        fontSize = 10.sp,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}
