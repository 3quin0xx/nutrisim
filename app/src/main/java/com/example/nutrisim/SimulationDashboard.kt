package com.example.nutrisim.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutrisim.model.*
import com.example.nutrisim.viewmodel.SimulationViewModel
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationDashboard(viewModel: SimulationViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("NutriSim India", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Real-Time Macro, Amino Acid & ₹ Cost Engine", style = MaterialTheme.typography.labelMedium)
                    }
                },
                actions = { IconButton(onClick = { viewModel.loadDefaultSampleMeal() }) { Icon(Icons.Default.Refresh, "Reset") } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            
            // 1. Full Final Summary Card
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("FINAL SUMMARY", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                Text("Total Weight: ${uiState.totalWeightGrams.roundToInt()} g")
                            }
                            Surface(color = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, shape = RoundedCornerShape(14.dp)) {
                                Column(modifier = Modifier.padding(14.dp, 8.dp), horizontalAlignment = Alignment.End) {
                                    Text("TOTAL COST", style = MaterialTheme.typography.labelSmall)
                                    Text(String.format(Locale.US, "₹%.2f", uiState.totalCostRupees), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            MacroPill("Cals", "${uiState.totalCalories.roundToInt()}", "kcal")
                            MacroPill("Prot", String.format(Locale.US, "%.1f", uiState.totalProteinGrams), "g")
                            MacroPill("Carb", String.format(Locale.US, "%.1f", uiState.totalCarbsGrams), "g")
                            MacroPill("Fats", String.format(Locale.US, "%.1f", uiState.totalFatsGrams), "g")
                        }
                    }
                }
            }

            // 2. Ingredient Sourcing Bar
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(IndianMarketCatalog.allIngredients) { ing ->
                        AssistChip(
                            onClick = { viewModel.addIngredientToMeal(ing) },
                            label = { Text("+ ${ing.name} (₹${ing.defaultMarketPricePerKg.roundToInt()}/kg)") }
                        )
                    }
                }
            }

            // 3. Live Ingredient Controls (Dual Pricing & Weight)
            items(uiState.simulatedItems, key = { it.instanceId }) { item ->
                OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(item.ingredient.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("₹${item.activePricePerKg.roundToInt()}/kg", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(String.format(Locale.US, "₹%.2f", item.calculatedCostRupees), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                IconButton(onClick = { viewModel.removeIngredientFromMeal(item.instanceId) }) { Icon(Icons.Default.DeleteOutline, "Remove", tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = item.pricingMode == PricingMode.LOCAL_MARKET, onClick = { viewModel.updateItemPricingMode(item.instanceId, PricingMode.LOCAL_MARKET) }, label = { Text("Local Market") })
                            FilterChip(selected = item.pricingMode == PricingMode.MANUAL, onClick = { viewModel.updateItemPricingMode(item.instanceId, PricingMode.MANUAL) }, label = { Text("Manual Price") })
                        }
                        
                        AnimatedVisibility(visible = item.pricingMode == PricingMode.MANUAL) {
                            Slider(value = item.manualPricePerKg.toFloat(), onValueChange = { viewModel.updateItemManualPrice(item.instanceId, (it / 5f).roundToInt() * 5.0) }, valueRange = 0f..2000f)
                        }

                        Text("Weight: ${item.weightGrams.roundToInt()} g", fontWeight = FontWeight.Bold)
                        Slider(value = item.weightGrams, onValueChange = { viewModel.updateItemWeight(item.instanceId, (it / 5f).roundToInt() * 5f) }, valueRange = 0f..1000f)
                    }
                }
            }

            // 4. Amino Acid Accordion
            item {
                OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Amino Acid Profile", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        uiState.aminoAcidStats.forEach { stat ->
                            val isExp = uiState.expandedAminoAcids.contains(stat.aminoAcid)
                            Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { viewModel.toggleAminoAcidExpanded(stat.aminoAcid) }, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(stat.aminoAcid.displayName, fontWeight = FontWeight.Bold)
                                        Text("${stat.totalGrams.roundToInt()} g", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    AnimatedVisibility(visible = isExp) {
                                        Column(modifier = Modifier.padding(top = 8.dp)) {
                                            Text("DRI Met: ${stat.driPercentage.roundToInt()}%", style = MaterialTheme.typography.labelSmall)
                                            LinearProgressIndicator(progress = { (stat.driPercentage / 100f).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp))
                                            Text("Top Contributor: ${stat.topContributorName}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MacroPill(label: String, value: String, unit: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text("$value $unit", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        }
    }
}
