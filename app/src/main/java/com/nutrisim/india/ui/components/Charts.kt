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

                if (isEmpty) {
                    drawCircle(color = CardDark2, radius = radius, center = Offset(cx, cy), style = Stroke(width = strokeWidth))
                }
            }

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

@Composable
fun MacroBarChart(
    protein: Float, proteinGoal: Float,
    carbs: Float,   carbsGoal: Float,
    fats: Float,    fatsGoal: Float,
    fiber: Float = 0f, fiberGoal: Float = 30f,
    calories: Float, caloriesGoal: Float,
    modifier: Modifier = Modifier
) {
    data class Bar(val label: String, val value: Float, val goal: Float, val color: Color, val unit: String)

    val bars = listOf(
        Bar("Calories", calories, caloriesGoal, CaloriesColor, "kcal"),
        Bar("Protein",  protein,  proteinGoal,  ProteinColor,  "g"),
        Bar("Carbs",    carbs,    carbsGoal,    CarbsColor,    "g"),
        Bar("Fats",     fats,     fatsGoal,     FatsColor,     "g"),
        Bar("Fibre",    fiber,    fiberGoal,    FiberColor,    "g")
    )

    var triggered by remember(protein, carbs, fats, fiber, calories) { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "bars"
    )
    LaunchedEffect(protein, carbs, fats, fiber, calories) { triggered = true }

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
                    drawRoundRect(
                        color        = CardDark2,
                        cornerRadius = CornerRadius(6f),
                        size         = size
                    )
                    val fillW = (size.width * fraction.coerceIn(0f, 1f) * progress)
                    if (fillW > 0f) {
                        drawRoundRect(
                            color        = bar.color,
                            cornerRadius = CornerRadius(6f),
                            size         = Size(fillW, size.height)
                        )
                    }
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

@Composable
fun AminoAcidChart(
    aminoAcids: com.nutrisim.india.data.AminoAcids,
    modifier: Modifier = Modifier
) {
    data class AminoBar(val name: String, val value: Float, val rda: Float, val color: Color)

    val allBars = listOf(
        AminoBar("Leu", aminoAcids.leucine,       2.73f, Color(0xFF7C4DFF)),
        AminoBar("Lys", aminoAcids.lysine,        2.10f, Color(0xFF00BCD4)),
        AminoBar("Val", aminoAcids.valine,        1.82f, Color(0xFF8BC34A)),
        AminoBar("Ile", aminoAcids.isoleucine,    1.40f, Color(0xFFFF9800)),
        AminoBar("Thr", aminoAcids.threonine,     1.05f, Color(0xFF26A69A)),
        AminoBar("Phe", aminoAcids.phenylalanine, 1.75f, Color(0xFFEC407A)),
        AminoBar("Met", aminoAcids.methionine,    1.05f, Color(0xFFFF5722)),
        AminoBar("His", aminoAcids.histidine,     0.70f, Color(0xFF42A5F5)),
        AminoBar("Trp", aminoAcids.tryptophan,    0.28f, Color(0xFFFFCA28))
    ).filter { it.value > 0.01f }

    if (allBars.isEmpty()) return
    val maxRda = allBars.maxOf { maxOf(it.rda, it.value) } * 1.25f

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
            val slotW = size.width / allBars.size
            val barW  = slotW * 0.55f
            val offX  = (slotW - barW) / 2f
            allBars.forEachIndexed { i, bar ->
                val x    = i * slotW + offX
                val rdaY = size.height - (bar.rda / maxRda * size.height)
                val barH = (bar.value / maxRda * size.height)
                drawRect(color = Color.White.copy(alpha = 0.25f), topLeft = Offset(x, rdaY), size = Size(barW, 2f))
                if (barH > 0f) {
                    drawRoundRect(
                        color = bar.color.copy(alpha = 0.88f),
                        topLeft = Offset(x, size.height - barH),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(4f)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            allBars.forEach { bar ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(bar.name, fontSize = 10.sp, color = bar.color, fontWeight = FontWeight.Bold)
                    Text("${String.format("%.2f", bar.value)}g", fontSize = 9.sp, color = TextSecondary)
                }
            }
        }
    }
}
