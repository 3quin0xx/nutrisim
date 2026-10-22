package com.nutrisim.india.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutrisim.india.data.NutritionGoals
import com.nutrisim.india.ui.theme.*
import com.nutrisim.india.viewmodel.NutriViewModel

@Composable
fun SettingsScreen(viewModel: NutriViewModel) {
    val state by viewModel.state.collectAsState()

    var apiKeyInput by remember(state.geminiApiKey) { mutableStateOf(state.geminiApiKey) }
    var showKey     by remember { mutableStateOf(false) }
    var keySaved    by remember { mutableStateOf(false) }

    var caloriesInput by remember(state.nutritionGoals) { mutableStateOf(state.nutritionGoals.caloriesGoal.toInt().toString()) }
    var proteinInput  by remember(state.nutritionGoals) { mutableStateOf(state.nutritionGoals.proteinGoal.toInt().toString()) }
    var carbsInput    by remember(state.nutritionGoals) { mutableStateOf(state.nutritionGoals.carbsGoal.toInt().toString()) }
    var fatsInput     by remember(state.nutritionGoals) { mutableStateOf(state.nutritionGoals.fatsGoal.toInt().toString()) }
    var goalsSaved    by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)

        // ── Gemini API Key card ────────────────────────────────────────────
        SettingsCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✨", fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text("Gemini API Key", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            Text(
                "Get a free key at aistudio.google.com — it starts with AIzaSy…",
                fontSize = 11.sp, color = TextTertiary
            )

            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it; keySaved = false },
                label  = { Text("Paste API key here", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null, tint = TextSecondary
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = AccentTeal,
                    unfocusedBorderColor = DividerDark,
                    focusedTextColor     = TextPrimary,
                    unfocusedTextColor   = TextPrimary,
                    cursorColor          = AccentTeal
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Button(
                onClick = { viewModel.saveApiKey(apiKeyInput); keySaved = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                shape  = RoundedCornerShape(10.dp)
            ) {
                Text(if (keySaved) "✅ Saved!" else "Save Key", color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        }

        // ── Nutrition Goals card ───────────────────────────────────────────
        SettingsCard {
            Text("Daily Nutrition Goals", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)

            GoalField("Target Calories (kcal)", caloriesInput) { caloriesInput = it; goalsSaved = false }
            GoalField("Target Protein (g)",     proteinInput)  { proteinInput  = it; goalsSaved = false }
            GoalField("Target Carbs (g)",        carbsInput)    { carbsInput    = it; goalsSaved = false }
            GoalField("Target Fats (g)",         fatsInput)     { fatsInput     = it; goalsSaved = false }

            Button(
                onClick = {
                    viewModel.saveGoals(
                        NutritionGoals(
                            caloriesGoal = caloriesInput.toFloatOrNull() ?: 2000f,
                            proteinGoal  = proteinInput.toFloatOrNull()  ?: 150f,
                            carbsGoal    = carbsInput.toFloatOrNull()    ?: 250f,
                            fatsGoal     = fatsInput.toFloatOrNull()     ?: 65f
                        )
                    )
                    goalsSaved = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                shape  = RoundedCornerShape(10.dp)
            ) {
                Text(if (goalsSaved) "✅ Goals Saved!" else "Save Goals", color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        }

        // ── About card ─────────────────────────────────────────────────────
        SettingsCard {
            Text("About NutriSim India", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Version 2.0", fontSize = 13.sp, color = TextSecondary)
            Text("Built with Kotlin · Jetpack Compose · Material 3 · Gemini AI", fontSize = 12.sp, color = TextTertiary)
            Text("35+ Indian foods pre-loaded 🇮🇳", fontSize = 12.sp, color = TextTertiary)
            Text("GitHub: 3quin0xx/nutrisim", fontSize = 12.sp, color = AccentTeal)
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun GoalField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, color = TextSecondary) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Saffron,
            unfocusedBorderColor = DividerDark,
            focusedTextColor     = TextPrimary,
            unfocusedTextColor   = TextPrimary,
            cursorColor          = Saffron
        ),
        shape = RoundedCornerShape(10.dp)
    )
}
