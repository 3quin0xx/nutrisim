package com.nutrisim.india.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutrisim.india.data.FoodCatalog
import com.nutrisim.india.data.FoodCategory
import com.nutrisim.india.data.FoodItem
import com.nutrisim.india.data.FoodUnit
import com.nutrisim.india.ui.theme.*
import com.nutrisim.india.viewmodel.NutriViewModel

@Composable
fun MealBuilderScreen(viewModel: NutriViewModel) {
    val state by viewModel.state.collectAsState()
    var selectedCategory by remember { mutableStateOf<FoodCategory?>(null) }
    var searchText by remember { mutableStateOf("") }

    val filteredFoods = remember(selectedCategory, searchText) {
        FoodCatalog.items.filter { food ->
            (selectedCategory == null || food.category == selectedCategory) &&
            (searchText.isEmpty() || food.name.contains(searchText, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("Meal Builder", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary) }

        item { AiCard(viewModel = viewModel) }

        item {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search foods…", color = TextTertiary) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Saffron,
                    unfocusedBorderColor = DividerDark,
                    focusedTextColor     = TextPrimary,
                    unfocusedTextColor   = TextPrimary,
                    cursorColor          = Saffron
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick  = { selectedCategory = null },
                        label    = { Text("All") },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Saffron,
                            selectedLabelColor     = BackgroundDark
                        )
                    )
                }
                items(FoodCategory.values().toList()) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick  = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label    = { Text("${cat.emoji} ${cat.displayName}") },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Saffron,
                            selectedLabelColor     = BackgroundDark
                        )
                    )
                }
            }
        }

        state.selectedFood?.let { food ->
            item { FoodDetailPanel(food = food, viewModel = viewModel) }
        }

        items(filteredFoods) { food ->
            FoodRow(
                food       = food,
                isSelected = state.selectedFood?.id == food.id,
                onClick    = { viewModel.selectFood(food) }
            )
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun FoodRow(food: FoodItem, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Saffron.copy(alpha = 0.14f) else CardDark)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(food.emoji, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(food.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                Text(
                    "${food.caloriesPer100.toInt()} kcal · ${food.proteinPer100}g P · ${food.carbsPer100}g C · ${food.fatsPer100}g F /100g",
                    fontSize = 11.sp, color = TextSecondary
                )
            }
            Text("₹${food.defaultPriceINRPer100}/100g", fontSize = 11.sp, color = AccentGold)
        }
    }
}

@Composable
private fun FoodDetailPanel(food: FoodItem, viewModel: NutriViewModel) {
    val state by viewModel.state.collectAsState()

    var customPriceText by remember(food.id) {
        val def = food.defaultPriceINRPer100
        mutableStateOf(if (def % 1f == 0f) def.toInt().toString() else def.toString())
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(CardDark2, CardDark)))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Text("${food.emoji} ${food.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

            if (food.unit == FoodUnit.PIECES) {
                Text("How many pieces?", fontSize = 13.sp, color = TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.setPieceCount(state.pieceCount - 1) }) {
                        Icon(Icons.Default.Remove, null, tint = Saffron)
                    }
                    Text(
                        "${state.pieceCount}",
                        fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                    IconButton(onClick = { viewModel.setPieceCount(state.pieceCount + 1) }) {
                        Icon(Icons.Default.Add, null, tint = Saffron)
                    }
                    Text("= ${state.quantity.toInt()}g", fontSize = 13.sp, color = TextSecondary)
                }
            } else {
                Text("Weight: ${state.quantity.toInt()}g", fontSize = 13.sp, color = TextSecondary)
                Slider(
                    value         = state.quantity,
                    onValueChange = { viewModel.setQuantity(it) },
                    valueRange    = 10f..500f,
                    colors        = SliderDefaults.colors(
                        thumbColor        = Saffron,
                        activeTrackColor  = Saffron,
                        inactiveTrackColor= DividerDark
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(50f, 100f, 150f, 200f, 300f).forEach { p ->
                        AssistChip(
                            onClick = { viewModel.setQuantity(p) },
                            label = { Text("${p.toInt()}g", fontSize = 11.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (state.quantity == p) Saffron.copy(0.25f) else CardDark
                            )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Custom Price", fontSize = 13.sp, color = TextSecondary)
                    Text(
                        "Default: ₹${food.defaultPriceINRPer100}/100g",
                        fontSize = 11.sp, color = TextTertiary
                    )
                }
                Switch(
                    checked  = state.useCustomPrice,
                    onCheckedChange = { on ->
                        if (on) {
                            val def = food.defaultPriceINRPer100
                            customPriceText = if (def % 1f == 0f) def.toInt().toString() else def.toString()
                            viewModel.setCustomPrice(def)
                        } else {
                            viewModel.setCustomPrice(null)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Saffron,
                        checkedTrackColor = Saffron.copy(alpha = 0.4f)
                    )
                )
            }

            if (state.useCustomPrice) {
                OutlinedTextField(
                    value = customPriceText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                            customPriceText = input
                            viewModel.setCustomPrice(input.toFloatOrNull() ?: 0f)
                        }
                    },
                    label = { Text("₹ per 100g", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Saffron, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp), singleLine = true
                )
            }

            val sf = state.quantity / 100f
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    Triple("Cal",  food.caloriesPer100 * sf, CaloriesColor),
                    Triple("P",    food.proteinPer100  * sf, ProteinColor),
                    Triple("C",    food.carbsPer100    * sf, CarbsColor),
                    Triple("F",    food.fatsPer100     * sf, FatsColor)
                ).forEach { (label, value, color) ->
                    Box(
                        modifier = Modifier.weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color.copy(alpha = 0.15f))
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${value.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
                            Text(label, fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }
            }

            val displayPrice = if (state.useCustomPrice) state.customPriceInput ?: 0f else food.defaultPriceINRPer100
            Text("Cost: ₹${String.format("%.1f", displayPrice * sf)}", fontSize = 13.sp, color = AccentGold, fontWeight = FontWeight.SemiBold)

            Button(
                onClick = { viewModel.addMealEntry() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                shape  = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = BackgroundDark)
                Spacer(Modifier.width(8.dp))
                Text("Add to Meal", fontWeight = FontWeight.Bold, color = BackgroundDark)
            }
        }
    }
}

@Composable
private fun AiCard(viewModel: NutriViewModel) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF0D1F3C), Color(0xFF091526))))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✨", fontSize = 18.sp)
                Spacer(Modifier.width(6.dp))
                Text("AI Auto-Estimate", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AccentTeal)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.aiQueryText,
                    onValueChange = { viewModel.setAiQuery(it) },
                    placeholder = { Text("e.g. 4 paranthas, 200g dal…", color = TextTertiary, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = AccentTeal,
                        unfocusedBorderColor = DividerDark,
                        focusedTextColor     = TextPrimary,
                        unfocusedTextColor   = TextPrimary,
                        cursorColor          = AccentTeal
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                Button(
                    onClick  = { viewModel.runGeminiQuery() },
                    enabled  = !state.geminiLoading && state.aiQueryText.isNotEmpty(),
                    colors   = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 16.dp)
                ) {
                    if (state.geminiLoading) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = BackgroundDark, strokeWidth = 2.dp)
                    } else {
                        Text("Ask", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                }
            }

            state.geminiError?.let {
                Text("⚠ $it", fontSize = 12.sp, color = ErrorRed)
            }

            state.geminiResult?.let { result ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AccentTeal.copy(alpha = 0.10f))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(result.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            "${result.weightGrams.toInt()}g total · ${result.calories.toInt()} kcal · " +
                            "${result.proteinG.toInt()}g P · ${result.carbsG.toInt()}g C · ${result.fatsG.toInt()}g F",
                            fontSize = 12.sp, color = TextSecondary
                        )
                        Text("Est. ₹${String.format("%.1f", result.estimatedPriceINR)}", fontSize = 12.sp, color = AccentGold)
                        if (result.notes.isNotEmpty()) {
                            Text(result.notes, fontSize = 11.sp, color = TextTertiary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick  = { viewModel.addGeminiResult() },
                                modifier = Modifier.weight(1f),
                                colors   = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                shape    = RoundedCornerShape(10.dp)
                            ) {
                                Text("✅ Add to Meal", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.dismissGeminiResult() },
                                colors  = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                            ) {
                                Text("✕")
                            }
                        }
                    }
                }
            }
        }
    }
}
