package com.nutrisim.india.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutrisim.india.data.MealEntry
import com.nutrisim.india.ui.theme.*
import com.nutrisim.india.viewmodel.DietPreference
import com.nutrisim.india.viewmodel.NutriViewModel
import com.nutrisim.india.viewmodel.PlanGoal

@Composable
fun BudgetScreen(viewModel: NutriViewModel) {
    val state  by viewModel.state.collectAsState()
    val totals = state.dailyTotals

    LaunchedEffect(Unit) {
        if (state.generatedPlan.isEmpty()) {
            viewModel.generateDietPlan()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "title") {
            Text("Budget & Diet Planner", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        }

        // ── Smart Diet Plan Generator Card ─────────────────────────────────
        item(key = "diet_planner_card") {
            DietPlannerCard(viewModel = viewModel)
        }

        // ── Total spend card ───────────────────────────────────────────────
        item(key = "spend_card") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF2A1A00), Color(0xFF150D00))))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Total Logged Spend Today", fontSize = 13.sp, color = TextSecondary)
                    Text(
                        "₹${String.format("%.2f", totals.totalCostINR)}",
                        fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = AccentGold
                    )

                    HorizontalDivider(color = DividerDark)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatCol("Protein", "${totals.protein.toInt()}g", ProteinColor)
                        StatCol("₹/g Protein",
                            if (totals.protein > 0.1f) "₹${String.format("%.2f", totals.totalCostINR / totals.protein)}" else "—",
                            Saffron
                        )
                        StatCol("Items", "${state.mealEntries.size}", AccentTeal)
                        StatCol("Calories", "${totals.calories.toInt()} kcal", CaloriesColor)
                    }
                }
            }
        }

        if (state.mealEntries.isNotEmpty()) {
            item(key = "adjust_header") {
                Column {
                    Text("Adjust Local Market Prices", fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("Slide to set your local rate (₹ per 100g)", fontSize = 11.sp, color = TextTertiary)
                }
            }
            items(state.mealEntries, key = { it.id }) { entry ->
                PriceSliderCard(entry = entry) { newPrice ->
                    viewModel.updateEntryPrice(entry.id, newPrice)
                }
            }
        }
    }
}

@Composable
private fun DietPlannerCard(viewModel: NutriViewModel) {
    val state by viewModel.state.collectAsState()
    var localBudget by remember(state.dailyBudgetINR) { mutableStateOf(state.dailyBudgetINR) }
    var planApplied by remember { mutableStateOf(false) }

    val planCost = state.generatedPlan.sumOf { it.costINR.toDouble() }.toFloat()
    val planCal  = state.generatedPlan.sumOf { it.calories.toDouble() }.toFloat()
    val planProt = state.generatedPlan.sumOf { it.protein.toDouble() }.toFloat()
    val planCarb = state.generatedPlan.sumOf { it.carbs.toDouble() }.toFloat()
    val planFat  = state.generatedPlan.sumOf { it.fats.toDouble() }.toFloat()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🥗 Custom Budget Diet Plan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Saffron)

            // 1. Goal Selection (Weight Loss vs Weight Gain)
            Text("1. Select Goal", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                PlanGoal.values().forEach { goal ->
                    FilterChip(
                        selected = state.selectedPlanGoal == goal,
                        onClick  = {
                            planApplied = false
                            viewModel.setPlanGoal(goal)
                        },
                        label    = { Text("${goal.emoji} ${goal.label}") },
                        modifier = Modifier.weight(1f),
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Saffron,
                            selectedLabelColor     = BackgroundDark
                        )
                    )
                }
            }

            // 2. Diet Preference (Pure Veg / Non-Veg / Both)
            Text("2. Diet Preference", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                DietPreference.values().forEach { pref ->
                    FilterChip(
                        selected = state.selectedDietPref == pref,
                        onClick  = {
                            planApplied = false
                            viewModel.setDietPref(pref)
                        },
                        label    = { Text("${pref.emoji} ${pref.label}", fontSize = 11.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentTeal,
                            selectedLabelColor     = BackgroundDark
                        )
                    )
                }
            }

            // 3. Budget Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("3. Daily Budget Target", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                Text("₹${localBudget.toInt()} / day", fontSize = 14.sp, color = AccentGold, fontWeight = FontWeight.ExtraBold)
            }
            Slider(
                value = localBudget,
                onValueChange = { localBudget = it; planApplied = false },
                onValueChangeFinished = {
                    viewModel.setDailyBudget(localBudget)
                    viewModel.generateDietPlan()
                },
                valueRange = 60f..500f,
                colors = SliderDefaults.colors(
                    thumbColor         = AccentGold,
                    activeTrackColor   = AccentGold,
                    inactiveTrackColor = DividerDark
                )
            )

            // 4. Generated Plan Breakdown
            if (state.generatedPlan.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardDark2)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "${state.selectedPlanGoal.label} (${state.selectedDietPref.label})",
                                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                            )
                            Text("₹${planCost.toInt()}/day", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = AccentGold)
                        }

                        Text(
                            "${planCal.toInt()} kcal · ${planProt.toInt()}g P · ${planCarb.toInt()}g C · ${planFat.toInt()}g F",
                            fontSize = 12.sp, color = AccentTeal, fontWeight = FontWeight.SemiBold
                        )

                        HorizontalDivider(color = DividerDark, modifier = Modifier.padding(vertical = 4.dp))

                        state.generatedPlan.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${item.foodItem.emoji} ${item.foodItem.name} (${item.weightGrams.toInt()}g)",
                                    fontSize = 12.sp, color = TextPrimary
                                )
                                Text(
                                    "${item.protein.toInt()}g P · ₹${item.costINR.toInt()}",
                                    fontSize = 11.sp, color = TextSecondary
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.applyGeneratedPlanToMealLog()
                        planApplied = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (planApplied) SuccessGreen else Saffron),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (planApplied) "✅ Loaded into Today's Dashboard!" else "Load Full Diet Plan into Today's Log",
                        color = BackgroundDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCol(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun PriceSliderCard(entry: MealEntry, onPriceChange: (Float) -> Unit) {
    var sliderVal by remember(entry.id) { mutableStateOf(entry.effectivePrice) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("${entry.foodItem.emoji} ${entry.foodItem.name}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Text("${entry.weightGrams.toInt()}g", fontSize = 11.sp, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₹${String.format("%.1f", sliderVal * entry.weightGrams / 100f)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                    if (entry.protein > 0.1f) {
                        Text(
                            "₹${String.format("%.2f", (sliderVal * entry.weightGrams / 100f) / entry.protein)}/g P",
                            fontSize = 10.sp, color = ProteinColor
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("₹${sliderVal.toInt()}", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.width(38.dp))
                Slider(
                    value = sliderVal,
                    onValueChange = { sliderVal = it },
                    onValueChangeFinished = { onPriceChange(sliderVal) },
                    valueRange = 1f..600f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor         = Saffron,
                        activeTrackColor   = Saffron,
                        inactiveTrackColor = DividerDark
                    )
                )
                Text("/100g", fontSize = 11.sp, color = TextTertiary, modifier = Modifier.width(38.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${entry.calories.toInt()} kcal", fontSize = 11.sp, color = CaloriesColor)
                Text("${String.format("%.1f", entry.protein)}g P", fontSize = 11.sp, color = ProteinColor)
                Text("${String.format("%.1f", entry.carbs)}g C",   fontSize = 11.sp, color = CarbsColor)
                Text("${String.format("%.1f", entry.fats)}g F",    fontSize = 11.sp, color = FatsColor)
            }
        }
    }
}
