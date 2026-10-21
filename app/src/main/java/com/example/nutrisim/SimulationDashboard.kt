package com.example.nutrisim.ui

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutrisim.model.*
import com.example.nutrisim.viewmodel.SimulationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationDashboard(viewModel: SimulationViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        CustomFoodDialog(
            onDismiss = { showDialog = false },
            onAdd = { n, p, c, pr, cb, f -> viewModel.addCustomIngredient(n, p, c, pr, cb, f); showDialog = false }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("NutriSim India", fontWeight = FontWeight.Bold) }, actions = { IconButton(onClick = { viewModel.loadDefaultSampleMeal() }) { Icon(Icons.Default.Refresh, "Reset") } }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("FINAL SUMMARY", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium); Text("Total Weight: ${uiState.totalWeightGrams.roundToInt()} g") }
                            Surface(color = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, shape = RoundedCornerShape(14.dp)) {
                                Column(modifier = Modifier.padding(14.dp, 8.dp), horizontalAlignment = Alignment.End) { Text("TOTAL COST", style = MaterialTheme.typography.labelSmall); Text(String.format(Locale.US, "₹%.2f", uiState.totalCostRupees), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) }
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
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Quick Add Staples", fontWeight = FontWeight.Bold)
                    Button(onClick = { showDialog = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) { Text("✨ AI Add Food", style = MaterialTheme.typography.labelMedium) }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    items(IndianMarketCatalog.allIngredients) { ing -> AssistChip(onClick = { viewModel.addIngredientToMeal(ing) }, label = { Text("+ ${ing.name}") }) }
                }
            }
            items(uiState.simulatedItems, key = { it.instanceId }) { item ->
                OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.ingredient.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("${item.ingredient.hindiOrLocalSubtext} • ₹${item.activePricePerKg.roundToInt()}/kg", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(String.format(Locale.US, "₹%.2f", item.calculatedCostRupees), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                IconButton(onClick = { viewModel.removeIngredientFromMeal(item.instanceId) }) { Icon(Icons.Default.DeleteOutline, "Remove", tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                        
                        // RESTORED LOCAL/MANUAL PRICE ADJUSTMENT
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = item.pricingMode == PricingMode.LOCAL_MARKET, onClick = { viewModel.updateItemPricingMode(item.instanceId, PricingMode.LOCAL_MARKET) }, label = { Text("Local Market") })
                            FilterChip(selected = item.pricingMode == PricingMode.MANUAL, onClick = { viewModel.updateItemPricingMode(item.instanceId, PricingMode.MANUAL) }, label = { Text("Manual Price") })
                        }
                        AnimatedVisibility(visible = item.pricingMode == PricingMode.MANUAL) {
                            Slider(value = item.manualPricePerKg.toFloat(), onValueChange = { viewModel.updateItemManualPrice(item.instanceId, (it / 5f).roundToInt() * 5.0) }, valueRange = 0f..2000f)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${item.calculatedCalories.roundToInt()} kcal", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(String.format(Locale.US, "P: %.1fg", item.calculatedProtein), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(String.format(Locale.US, "C: %.1fg", item.calculatedCarbs), style = MaterialTheme.typography.labelSmall)
                            Text(String.format(Locale.US, "F: %.1fg", item.calculatedFats), style = MaterialTheme.typography.labelSmall)
                        }
                        val pt = if (item.ingredient.pieceWeightGrams != null) " (~${(item.weightGrams / item.ingredient.pieceWeightGrams).roundToInt()} pieces)" else ""
                        Text("Weight: ${item.weightGrams.roundToInt()} g$pt", fontWeight = FontWeight.Bold)
                        Slider(value = item.weightGrams, onValueChange = { viewModel.updateItemWeight(item.instanceId, (it / 5f).roundToInt() * 5f) }, valueRange = 0f..1000f)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomFoodDialog(onDismiss: () -> Unit, onAdd: (String, Double, Double, Double, Double, Double) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("NutriSimPrefs", Context.MODE_PRIVATE) }
    
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var cals by remember { mutableStateOf("") }
    var prot by remember { mutableStateOf("") }
    var carb by remember { mutableStateOf("") }
    var fats by remember { mutableStateOf("") }
    
    var apiKey by remember { mutableStateOf(prefs.getString("gemini_key", "") ?: "") }
    var isFetching by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss, title = { Text("Add Custom Food") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("✨ AI Auto-Estimate", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        
                        OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("Gemini API Key") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Food Query (e.g. 4 Chapatis)") }, singleLine = true, modifier = Modifier.weight(1f))
                            Button(
                                onClick = {
                                    if (apiKey.isBlank() || name.isBlank()) { errorMsg = "Need Key & Food"; return@Button }
                                    
                                    prefs.edit().putString("gemini_key", apiKey.trim()).apply()
                                    isFetching = true; errorMsg = null
                                    
                                    scope.launch {
                                        try {
                                            val res = fetchNutrition(name, apiKey.trim())
                                            price = res["price"].toString()
                                            cals = res["cals"].toString()
                                            prot = res["prot"].toString()
                                            carb = res["carb"].toString()
                                            fats = res["fats"].toString()
                                        } catch (e: Exception) {
                                            errorMsg = e.message ?: "Unknown Error"
                                        } finally {
                                            isFetching = false
                                        }
                                    }
                                }, enabled = !isFetching
                            ) { Text(if (isFetching) "..." else "Fetch") }
                        }
                        if (errorMsg != null) Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top=8.dp)) {
                    OutlinedTextField(value = cals, onValueChange = { cals = it }, label = { Text("Cals") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = prot, onValueChange = { prot = it }, label = { Text("Prot(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = carb, onValueChange = { carb = it }, label = { Text("Carb(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = fats, onValueChange = { fats = it }, label = { Text("Fats(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Total Estimated Price (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { if (name.isNotBlank()) onAdd(name, price.toDoubleOrNull() ?: 0.0, cals.toDoubleOrNull() ?: 0.0, prot.toDoubleOrNull() ?: 0.0, carb.toDoubleOrNull() ?: 0.0, fats.toDoubleOrNull() ?: 0.0) }) { Text("Add to Meal") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

suspend fun fetchNutrition(query: String, apiKey: String): Map<String, Double> = withContext(Dispatchers.IO) {
    val urlStr = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
    val url = URL(urlStr)
    val conn = url.openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.setRequestProperty("Content-Type", "application/json")
    conn.setRequestProperty("x-goog-api-key", apiKey)
    conn.doOutput = true
    
    val prompt = "Estimate nutritional value for: $query. Return ONLY raw JSON with exact keys: 'price' (estimated price in INR), 'cals' (calories), 'prot' (protein in g), 'carb' (carbohydrates in g), 'fats' (fats in g). No markdown, no text."
    val jsonPayload = """{"contents": [{"parts": [{"text": "$prompt"}]}],"generationConfig": {"responseMimeType": "application/json"}}"""
    
    conn.outputStream.use { os -> os.write(jsonPayload.toByteArray(Charsets.UTF_8)) }
    
    if (conn.responseCode == 200) {
        val response = conn.inputStream.bufferedReader().readText()
        try {
            val data = JSONObject(JSONObject(response).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text").replace("```json", "").replace("```", "").trim())
            mapOf("price" to data.optDouble("price", 0.0), "cals" to data.optDouble("cals", 0.0), "prot" to data.optDouble("prot", 0.0), "carb" to data.optDouble("carb", 0.0), "fats" to data.optDouble("fats", 0.0))
        } catch (e: Exception) {
            throw Exception("Failed to parse AI JSON response")
        }
    } else {
        val err = conn.errorStream?.bufferedReader()?.readText() ?: "No error stream"
        throw Exception("API Error ${conn.responseCode}: $err".take(100))
    }
}

@Composable
fun MacroPill(label: String, value: String, unit: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(label, style = MaterialTheme.typography.labelSmall); Text("$value $unit", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium) }
    }
}
