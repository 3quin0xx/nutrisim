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

enum class PlanGoal(val label: String, val targetCal: Float, val targetProt: Float, val emoji: String) {
    WEIGHT_LOSS("Weight Loss", 1600f, 130f, "🔥"),
    WEIGHT_GAIN("Weight Gain", 2800f, 160f, "💪")
}

enum class DietPreference(val label: String, val emoji: String) {
    PURE_VEG("Pure Veg", "🌱"),
    NON_VEG("Non-Veg", "🍗"),
    BOTH("Veg + Non-Veg", "🍱")
}

data class NutriUiState(
    val mealEntries: List<MealEntry> = emptyList(),
    val dailyTotals: DailyTotals = DailyTotals(),
    val nutritionGoals: NutritionGoals = NutritionGoals(),
    val hasCustomApiKey: Boolean = false,
    val selectedFood: FoodItem? = null,
    val quantity: Float = 100f,
    val pieceCount: Int = 1,
    val useCustomPrice: Boolean = false,
    val customPriceInput: Float? = null,
    val aiQueryText: String = "",
    val geminiLoading: Boolean = false,
    val geminiError: String? = null,
    val geminiResult: GeminiFoodResult? = null,
    val selectedPlanGoal: PlanGoal = PlanGoal.WEIGHT_LOSS,
    val selectedDietPref: DietPreference = DietPreference.PURE_VEG,
    val dailyBudgetINR: Float = 150f,
    val generatedPlan: List<MealEntry> = emptyList()
)

data class PersistedEntry(
    val id: String,
    val foodItemId: String? = null,
    val foodItem: FoodItem? = null,
    val weightGrams: Float,
    val customPriceINRPer100: Float?
) {
    fun toMealEntry(): MealEntry? {
        val resolvedFood = foodItem ?: foodItemId?.let { FoodCatalog.getById(it) } ?: return null
        return MealEntry(
            id = id,
            foodItem = resolvedFood,
            weightGrams = weightGrams,
            customPriceINRPer100 = customPriceINRPer100
        )
    }
    companion object {
        fun from(entry: MealEntry) = PersistedEntry(
            id = entry.id,
            foodItemId = entry.foodItem.id,
            foodItem = entry.foodItem,
            weightGrams = entry.weightGrams,
            customPriceINRPer100 = entry.customPriceINRPer100
        )
    }
}

class NutriViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PrefsManager(application)
    private val geminiService = GeminiService()
    private val gson = Gson()

    private val _state = MutableStateFlow(NutriUiState())
    val state: StateFlow<NutriUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.hasCustomKey.collect { hasCustom ->
                _state.update { it.copy(hasCustomApiKey = hasCustom) }
            }
        }
        viewModelScope.launch {
            combine(
                prefs.caloriesGoal,
                prefs.proteinGoal,
                prefs.carbsGoal,
                prefs.fatsGoal,
                prefs.fiberGoal
            ) { cal, prot, carb, fat, fib ->
                NutritionGoals(cal, prot, carb, fat, fib)
            }.collect { goals ->
                _state.update { it.copy(nutritionGoals = goals) }
            }
        }
        viewModelScope.launch {
            val json = prefs.mealJson.first()
            if (json != "[]" && json.isNotEmpty()) {
                try {
                    val type = object : TypeToken<List<PersistedEntry>>() {}.type
                    val persisted: List<PersistedEntry> = gson.fromJson(json, type)
                    val entries = persisted.mapNotNull { it.toMealEntry() }
                    _state.update { it.copy(mealEntries = entries) }
                    updateTotals(entries)
                } catch (e: Exception) { }
            }
        }
    }

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

    fun setPlanGoal(goal: PlanGoal) {
        _state.update { it.copy(selectedPlanGoal = goal) }
        generateDietPlan()
    }

    fun setDietPref(pref: DietPreference) {
        _state.update { it.copy(selectedDietPref = pref) }
        generateDietPlan()
    }

    fun setDailyBudget(budget: Float) {
        _state.update { it.copy(dailyBudgetINR = budget) }
    }

    fun generateDietPlan() {
        val goal = _state.value.selectedPlanGoal
        val pref = _state.value.selectedDietPref
        val budget = _state.value.dailyBudgetINR

        val baseTemplate: List<Pair<String, Float>> = when (goal) {
            PlanGoal.WEIGHT_LOSS -> when (pref) {
                DietPreference.PURE_VEG -> listOf(
                    "oats" to 60f,
                    "soybean" to 120f,
                    "dal_masoor" to 200f,
                    "chapati" to 80f,
                    "paneer_low_fat" to 100f,
                    "curd" to 150f,
                    "spinach" to 100f
                )
                DietPreference.NON_VEG -> listOf(
                    "egg" to 150f,
                    "egg_white" to 150f,
                    "chicken_breast" to 200f,
                    "rohu_fish" to 150f,
                    "chapati" to 80f,
                    "spinach" to 100f
                )
                DietPreference.BOTH -> listOf(
                    "oats" to 50f,
                    "egg" to 150f,
                    "chicken_breast" to 150f,
                    "dal_toor" to 150f,
                    "chapati" to 80f,
                    "curd" to 100f,
                    "spinach" to 100f
                )
            }
            PlanGoal.WEIGHT_GAIN -> when (pref) {
                DietPreference.PURE_VEG -> listOf(
                    "oats" to 80f,
                    "milk_full" to 400f,
                    "banana" to 240f,
                    "peanuts" to 60f,
                    "rice_cooked" to 300f,
                    "rajma" to 200f,
                    "paneer" to 120f,
                    "chapati" to 120f
                )
                DietPreference.NON_VEG -> listOf(
                    "egg" to 200f,
                    "chicken_breast" to 200f,
                    "chicken_leg" to 150f,
                    "rice_cooked" to 350f,
                    "chapati" to 120f,
                    "banana" to 240f,
                    "potato" to 150f
                )
                DietPreference.BOTH -> listOf(
                    "oats" to 70f,
                    "milk_full" to 300f,
                    "egg" to 200f,
                    "chicken_breast" to 180f,
                    "rice_cooked" to 300f,
                    "dal_toor" to 150f,
                    "peanuts" to 50f,
                    "banana" to 240f
                )
            }
        }

        val rawEntries = baseTemplate.mapNotNull { (id, weight) ->
            val food = FoodCatalog.getById(id) ?: return@mapNotNull null
            MealEntry(foodItem = food, weightGrams = weight)
        }

        val rawCost = rawEntries.sumOf { it.costINR.toDouble() }.toFloat()
        val scale = if (rawCost > 0f) (budget / rawCost).coerceIn(0.45f, 1.6f) else 1f

        val scaledEntries = rawEntries.map { entry ->
            val roundedWeight = ((entry.weightGrams * scale) / 5f).toInt().coerceAtLeast(5) * 5f
            entry.copy(weightGrams = roundedWeight)
        }

        _state.update { it.copy(generatedPlan = scaledEntries) }
    }

    fun applyGeneratedPlanToMealLog() {
        val plan = _state.value.generatedPlan
        if (plan.isEmpty()) return
        val goal = _state.value.selectedPlanGoal
        val newGoals = if (goal == PlanGoal.WEIGHT_LOSS) {
            NutritionGoals(caloriesGoal = 1600f, proteinGoal = 130f, carbsGoal = 150f, fatsGoal = 50f, fiberGoal = 30f)
        } else {
            NutritionGoals(caloriesGoal = 2800f, proteinGoal = 160f, carbsGoal = 350f, fatsGoal = 85f, fiberGoal = 35f)
        }
        saveGoals(newGoals)
        _state.update { it.copy(mealEntries = plan, nutritionGoals = newGoals) }
        updateTotals(plan)
        persist(plan)
    }

    fun setAiQuery(text: String) {
        _state.update { it.copy(aiQueryText = text, geminiError = null) }
    }

    fun runGeminiQuery() {
        val query = _state.value.aiQueryText.trim()
        if (query.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(geminiLoading = true, geminiError = null, geminiResult = null) }
            val key = prefs.effectiveApiKey.first()
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
            fiberPer100 = (result.fiberG / w) * 100f,
            sodiumMgPer100 = (result.sodiumMg / w) * 100f,
            vitamins = result.vitamins,
            minerals = result.minerals,
            customAminosText = result.essentialAminoAcids,
            defaultPriceINRPer100 = (result.estimatedPriceINR / w) * 100f,
            emoji = "✨"
        )
        val entry = MealEntry(foodItem = syntheticFood, weightGrams = w)
        val newEntries = _state.value.mealEntries + entry
        _state.update { it.copy(mealEntries = newEntries, geminiResult = null, aiQueryText = "") }
        updateTotals(newEntries)
        persist(newEntries)
    }

    fun dismissGeminiResult() {
        _state.update { it.copy(geminiResult = null, geminiError = null) }
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch { prefs.saveCustomApiKey(key) }
    }

    fun resetToDefaultApiKey() {
        viewModelScope.launch { prefs.resetToBuiltInKey() }
    }

    fun saveGoals(goals: NutritionGoals) {
        viewModelScope.launch {
            prefs.saveGoals(goals.caloriesGoal, goals.proteinGoal, goals.carbsGoal, goals.fatsGoal, goals.fiberGoal)
        }
    }

    private fun updateTotals(entries: List<MealEntry>) {
        val totals = DailyTotals(
            calories = entries.sumOf { it.calories.toDouble() }.toFloat(),
            protein = entries.sumOf { it.protein.toDouble() }.toFloat(),
            carbs = entries.sumOf { it.carbs.toDouble() }.toFloat(),
            fats = entries.sumOf { it.fats.toDouble() }.toFloat(),
            fiber = entries.sumOf { it.fiber.toDouble() }.toFloat(),
            totalCostINR = entries.sumOf { it.costINR.toDouble() }.toFloat(),
            sodiumMg = entries.sumOf { it.sodiumMg.toDouble() }.toFloat(),
            aminoAcids = entries.fold(AminoAcids()) { acc, e -> acc + e.aminoAcids },
            presentVitamins = entries.mapNotNull { it.foodItem.vitamins.takeIf { v -> v.isNotBlank() } }.flatMap { it.split(",") }.map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString(" · "),
            presentMinerals = entries.mapNotNull { it.foodItem.minerals.takeIf { m -> m.isNotBlank() } }.flatMap { it.split(",") }.map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString(" · ")
        )
        _state.update { it.copy(dailyTotals = totals) }
    }

    private fun persist(entries: List<MealEntry>) {
        viewModelScope.launch {
            prefs.saveMealJson(gson.toJson(entries.map { PersistedEntry.from(it) }))
        }
    }
}
