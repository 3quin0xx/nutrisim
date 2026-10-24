package com.nutrisim.india.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutrisim.india.ui.components.*
import com.nutrisim.india.ui.theme.*
import com.nutrisim.india.viewmodel.NutriViewModel

@Composable
fun DashboardScreen(viewModel: NutriViewModel) {
    val state by viewModel.state.collectAsState()
    val totals = state.dailyTotals
    val goals  = state.nutritionGoals
    val proteinCostPer = if (totals.protein > 0.1f) totals.totalCostINR / totals.protein else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text("Today's Summary", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Text(
                "${state.mealEntries.size} item${if (state.mealEntries.size == 1) "" else "s"} logged",
                fontSize = 13.sp, color = TextSecondary
            )
        }

        CostSummaryCard(
            totalCost = totals.totalCostINR,
            costPerGramProtein = proteinCostPer,
            modifier = Modifier.fillMaxWidth()
        )

        // 5-up macro cards (including Fibre)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MacroCard("Cal",     totals.calories, "kcal", CaloriesColor, Modifier.weight(1f))
            MacroCard("Protein", totals.protein,  "g",    ProteinColor,  Modifier.weight(1f))
            MacroCard("Carbs",   totals.carbs,    "g",    CarbsColor,    Modifier.weight(1f))
            MacroCard("Fats",    totals.fats,     "g",    FatsColor,     Modifier.weight(1f))
            MacroCard("Fibre",   totals.fiber,    "g",    FiberColor,    Modifier.weight(1f))
        }

        ChartCard(title = "Macro Split") {
            MacroPieChart(
                protein = totals.protein,
                carbs   = totals.carbs,
                fats    = totals.fats,
                totalCalories = totals.calories,
                modifier = Modifier.fillMaxWidth()
            )
        }

        ChartCard(title = "Goals Progress") {
            MacroBarChart(
                protein      = totals.protein,  proteinGoal  = goals.proteinGoal,
                carbs        = totals.carbs,    carbsGoal    = goals.carbsGoal,
                fats         = totals.fats,     fatsGoal     = goals.fatsGoal,
                fiber        = totals.fiber,    fiberGoal    = goals.fiberGoal,
                calories     = totals.calories, caloriesGoal = goals.caloriesGoal,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (totals.protein > 1f) {
            ChartCard(title = "Amino Acid Profile", subtitle = "vs. WHO daily RDA (white lines)") {
                AminoAcidChart(
                    leucine    = totals.aminoAcids.leucine,
                    lysine     = totals.aminoAcids.lysine,
                    methionine = totals.aminoAcids.methionine,
                    valine     = totals.aminoAcids.valine,
                    isoleucine = totals.aminoAcids.isoleucine,
                    modifier   = Modifier.fillMaxWidth()
                )
            }
        }

        if (state.mealEntries.isNotEmpty()) {
            ChartCard(title = "Logged Meals") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.mealEntries.forEachIndexed { idx, entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${entry.foodItem.emoji} ${entry.foodItem.name}",
                                    fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary
                                )
                                Text(
                                    "${entry.weightGrams.toInt()}g · ${entry.calories.toInt()} kcal · " +
                                    "${String.format("%.1f", entry.protein)}g P · ${String.format("%.1f", entry.fiber)}g Fib · ₹${String.format("%.1f", entry.costINR)}",
                                    fontSize = 11.sp, color = TextSecondary
                                )
                            }
                            IconButton(onClick = { viewModel.removeMealEntry(entry.id) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = ErrorRed.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (idx < state.mealEntries.lastIndex) {
                            HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { viewModel.clearAll() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        Text("Clear All")
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardDark)
                    .padding(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🍽️", fontSize = 42.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("No meals logged yet", fontSize = 16.sp, color = TextSecondary)
                    Text("Tap the Builder tab to add food", fontSize = 12.sp, color = TextTertiary)
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun ChartCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .padding(16.dp)
    ) {
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            if (subtitle != null) {
                Text(subtitle, fontSize = 10.sp, color = TextTertiary)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
