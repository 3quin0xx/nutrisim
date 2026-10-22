package com.nutrisim.india.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nutrisim.india.api.GeminiService
import com.nutrisim.india.data.*
import com.nutrisim.india.utils.PrefsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ─── UI State ──────────────────────────────────────────────────────────────────
data class NutriUiState(
    val mealEntries: List<MealEntry> = emptyList(),
    val dailyTotals: DailyTotals = DailyTotals(),
    val nutritionGoals: NutritionGoals = NutritionGoals(),
    val geminiApiKey: String = "",
    // Meal builder
    val selectedFood: FoodItem? = null,
    val quantity: Float = 100f,
    val pieceCount: Int = 1,
    val useCustomPrice: Boolean = false,
    val customPriceInput: Float? = null,
    // AI
    val aiQueryText: String = "",
    val geminiLoading: Boolean = false,
    val geminiError: String? = null,
    val geminiResult: GeminiFoodResult? = null
)

// ─── Serializable form for DataStore persistence ───────────────────────────────
data class PersistedEntry(
    val id: String,
    val foodItemId: String,
    val weightGrams: Float,
    val customPriceINRPer100: Float?
) {
    fun toMealEntry(): MealEntry? {
        val food = FoodCatalog.getById(foodItemId) ?: return null
        return MealEntry(
            id = id,
            foodItem = food,
            weightGrams = weightGrams,
            customPriceINRPer100 = customPriceINRPer100
        )
    }
    companion object {
        fun from(entry: MealEntry) = PersistedEntry(
            id = entry.id,
            foodItemId = entry.foodItem.id,
            weightGrams = entry.weightGrams,
            customPriceINRPer100 = entry.customPriceINRPer100
        )
    }
}

// ─── ViewModel ────────────────────────────────────────────────────────────────
class NutriViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PrefsManager(application)
    private val geminiService = GeminiService()
    private val gson = Gson()

    private val _state = MutableStateFlow(NutriUiState())
    val state: StateFlow<NutriUiState> = _state.asStateFlow()

    init {
        // Load API key
        viewModelScope.launch {
            prefs.geminiApiKey.collect { key ->
                _state.update { it.copy(geminiApiKey = key) }
            }
        }
        // Load nutrition goals
        viewModelScope.launch {
            combine(
                prefs.caloriesGoal,
                prefs.proteinGoal,
                prefs.carbsGoal,
                prefs.fatsGoal
            ) { cal, prot, carb, fat ->
                NutritionGoals(cal, prot, carb, fat)
            }.collect { goals ->
                _state.update { it.copy(nutritionGoals = goals) }
            }
        }
        // Load persisted meal entries (catalog items only — Gemini entries are session-only)
        viewModelScope.launch {
            prefs.mealJson.collect { json ->
                if (json == "[]" || json.isEmpty()) return@collect
                try {
                    val type = object : TypeToken<List<PersistedEntry>>() {}.type
                    val persisted: List<PersistedEntry> = gson.fromJson(json, type)
                    val entries = persisted.mapNotNull { it.toMealEntry() }
                    _state.update { it.copy(mealEntries = entries) }
                    updateTotals(entries)
                } catch (e: Exception) { /* ignore corrupt data */ }
            }
        }
    }

    // ─── Meal Builder ─────────────────────────────────────────────────────────
    fun selectFood(food: FoodItem) {
        val isPiece = food.unit == FoodUnit.PIECES
        _state.update {
            it.copy(
                selectedFood = food,
                pieceCount = 1,
                quantity = if (isPiece) food.gramsPerPiece else 100f,
                useCustomPrice = false,
                customPriceInput = null
            )
        }
    }

    fun setQuantity(grams: Float) {
        _state.update { it.copy(quantity = grams.coerceAtLeast(1f)) }
    }

    fun setPieceCount(count: Int) {
        val food = _state.value.selectedFood ?: return
        val clamped = count.coerceAtLeast(1)
        _state.update { it.copy(pieceCount = clamped, quantity = food.gramsPerPiece * clamped) }
    }

    fun setCustomPrice(price: Float?) {
        _state.update { it.copy(customPriceInput = price, useCustomPrice = price != null) }
    }

    fun addMealEntry() {
        val food = _state.value.selectedFood ?: return
        val entry = MealEntry(
            foodItem = food,
            weightGrams = _state.value.quantity,
            customPriceINRPer100 = if (_state.value.useCustomPrice) _state.value.customPriceInput else null
        )
        val newEntries = _state.value.mealEntries + entry
        _state.update { it.copy(mealEntries = newEntries, selectedFood = null, quantity = 100f) }
        updateTotals(newEntries)
        persist(newEntries)
    }

    fun removeMealEntry(id: String) {
        val newEntries = _state.value.mealEntries.filter { it.id != id }
        _state.update { it.copy(mealEntries = newEntries) }
        updateTotals(newEntries)
        persist(newEntries)
    }

    fun clearAll() {
        _state.update { it.copy(mealEntries = emptyList(), dailyTotals = DailyTotals()) }
        persist(emptyList())
    }

    fun updateEntryPrice(id: String, newPrice: Float) {
        val newEntries = _state.value.mealEntries.map {
            if (it.id == id) it.copy(customPriceINRPer100 = newPrice) else it
        }
        _state.update { it.copy(mealEntries = newEntries) }
        updateTotals(newEntries)
        persist(newEntries)
    }

    // ─── Gemini ──────────────────────────────────────────────────────────────
    fun setAiQuery(text: String) {
        _state.update { it.copy(aiQueryText = text, geminiError = null) }
    }

    fun runGeminiQuery() {
        val query = _state.value.aiQueryText.trim()
        val key = _state.value.geminiApiKey
        if (query.isEmpty()) return
        if (key.isEmpty()) {
            _state.update { it.copy(geminiError = "No API key — go to Settings and save your key.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(geminiLoading = true, geminiError = null, geminiResult = null) }
            geminiService.estimateMacros(query, key).fold(
                onSuccess = { result -> _state.update { it.copy(geminiLoading = false, geminiResult = result) } },
                onFailure = { err -> _state.update { it.copy(geminiLoading = false, geminiError = err.message ?: "Unknown error") } }
            )
        }
    }

    fun addGeminiResult() {
        val result = _state.value.geminiResult ?: return
        val w = result.weightGrams.coerceAtLeast(1f)
        val syntheticFood = FoodItem(
            id = "ai_${System.currentTimeMillis()}",
            name = result.name,
            category = FoodCategory.PROTEIN,
            caloriesPer100 = (result.calories / w) * 100f,
            proteinPer100 = (result.proteinG / w) * 100f,
            carbsPer100 = (result.carbsG / w) * 100f,
            fatsPer100 = (result.fatsG / w) * 100f,
            defaultPriceINRPer100 = (result.estimatedPriceINR / w) * 100f,
            emoji = "✨"
        )
        val entry = MealEntry(foodItem = syntheticFood, weightGrams = w)
        val newEntries = _state.value.mealEntries + entry
        _state.update { it.copy(mealEntries = newEntries, geminiResult = null, aiQueryText = "") }
        updateTotals(newEntries)
        // Don't persist AI entries — they have no catalog ID to look up on next launch
    }

    fun dismissGeminiResult() {
        _state.update { it.copy(geminiResult = null, geminiError = null) }
    }

    // ─── Settings ────────────────────────────────────────────────────────────
    fun saveApiKey(key: String) {
        viewModelScope.launch { prefs.saveApiKey(key.trim()) }
    }

    fun saveGoals(goals: NutritionGoals) {
        viewModelScope.launch {
            prefs.saveGoals(goals.caloriesGoal, goals.proteinGoal, goals.carbsGoal, goals.fatsGoal)
        }
    }

    // ─── Internal helpers ─────────────────────────────────────────────────────
    private fun updateTotals(entries: List<MealEntry>) {
        val totals = DailyTotals(
            calories = entries.sumOf { it.calories.toDouble() }.toFloat(),
            protein = entries.sumOf { it.protein.toDouble() }.toFloat(),
            carbs = entries.sumOf { it.carbs.toDouble() }.toFloat(),
            fats = entries.sumOf { it.fats.toDouble() }.toFloat(),
            totalCostINR = entries.sumOf { it.costINR.toDouble() }.toFloat(),
            aminoAcids = entries.fold(AminoAcids()) { acc, e -> acc + e.aminoAcids }
        )
        _state.update { it.copy(dailyTotals = totals) }
    }

    private fun persist(entries: List<MealEntry>) {
        viewModelScope.launch {
            val persistable = entries.filter { !it.foodItem.id.startsWith("ai_") }
            prefs.saveMealJson(gson.toJson(persistable.map { PersistedEntry.from(it) }))
        }
    }
}
