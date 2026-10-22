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
import com.nutrisim.india.viewmodel.NutriViewModel

@Composable
fun BudgetScreen(viewModel: NutriViewModel) {
    val state  by viewModel.state.collectAsState()
    val totals = state.dailyTotals

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Budget Tracker", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary) }

        // ── Total spend card ───────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF2A1A00), Color(0xFF150D00))))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Total Spend Today", fontSize = 13.sp, color = TextSecondary)
                    Text(
                        "₹${String.format("%.2f", totals.totalCostINR)}",
                        fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = AccentGold
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

        if (state.mealEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💰", fontSize = 42.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No items to budget yet", fontSize = 16.sp, color = TextSecondary)
                    }
                }
            }
        } else {
            item {
                Text("Adjust Prices per Item", fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Text("Slide to set your local market rate (₹ per 100g)", fontSize = 11.sp, color = TextTertiary)
            }
            items(state.mealEntries, key = { it.id }) { entry ->
                PriceSliderCard(entry = entry) { newPrice ->
                    viewModel.updateEntryPrice(entry.id, newPrice)
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
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
                    value         = sliderVal,
                    onValueChange = { sliderVal = it; onPriceChange(it) },
                    valueRange    = 1f..600f,
                    modifier      = Modifier.weight(1f),
                    colors        = SliderDefaults.colors(
                        thumbColor        = Saffron,
                        activeTrackColor  = Saffron,
                        inactiveTrackColor= DividerDark
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
