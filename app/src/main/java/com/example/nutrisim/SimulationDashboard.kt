package com.example.nutrisim.ui

import androidx.compose.animation.*
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
                title = { Text("NutriSim India", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.loadDefaultSampleMeal() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TOTAL MEAL COST: ₹${String.format(Locale.US, "%.2f", uiState.totalCostRupees)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Calories: ${uiState.totalCalories.roundToInt()} kcal | Protein: ${String.format(Locale.US, "%.1f", uiState.totalProteinGrams)}g")
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IndianMarketCatalog.allIngredients.forEach { ingredient ->
                        AssistChip(
                            onClick = { viewModel.addIngredientToMeal(ingredient) },
                            label = { Text("+ ${ingredient.name}") }
                        )
                    }
                }
            }

            items(uiState.simulatedItems, key = { it.instanceId }) { item ->
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.ingredient.name, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.removeIngredientFromMeal(item.instanceId) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Text("Weight: ${item.weightGrams.roundToInt()}g | Cost: ₹${String.format(Locale.US, "%.2f", item.calculatedCostRupees)}")
                        Slider(
                            value = item.weightGrams,
                            onValueChange = { viewModel.updateItemWeight(item.instanceId, it) },
                            valueRange = 0f..1000f
                        )
                    }
                }
            }
        }
    }
}
