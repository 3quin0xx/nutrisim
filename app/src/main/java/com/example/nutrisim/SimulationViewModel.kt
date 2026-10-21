package com.example.nutrisim.viewmodel

import androidx.lifecycle.ViewModel
import com.example.nutrisim.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

class SimulationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MealSimulationUiState())
    val uiState: StateFlow<MealSimulationUiState> = _uiState.asStateFlow()

    init { loadDefaultSampleMeal() }

    fun loadDefaultSampleMeal() {
        val initialItems = listOf(
            SimulatedItem(ingredient = IndianMarketCatalog.CHICKEN_BREAST, weightGrams = 200f),
            SimulatedItem(ingredient = IndianMarketCatalog.DAL_LENTILS, weightGrams = 80f),
            SimulatedItem(ingredient = IndianMarketCatalog.WHEY_PROTEIN, weightGrams = 30f)
        )
        _uiState.value = buildComputedState(initialItems, setOf(AminoAcid.LEUCINE))
    }

    fun addIngredientToMeal(ingredient: Ingredient) {
        _uiState.update { current ->
            val updatedItems = current.simulatedItems + SimulatedItem(ingredient = ingredient, weightGrams = 100f)
            buildComputedState(updatedItems, current.expandedAminoAcids)
        }
    }

    fun removeIngredientFromMeal(instanceId: String) {
        _uiState.update { current ->
            val updatedItems = current.simulatedItems.filterNot { it.instanceId == instanceId }
            buildComputedState(updatedItems, current.expandedAminoAcids)
        }
    }

    fun updateItemWeight(instanceId: String, newWeightGrams: Float) {
        _uiState.update { current ->
            val updatedItems = current.simulatedItems.map { if (it.instanceId == instanceId) it.copy(weightGrams = newWeightGrams.coerceIn(0f, 2000f)) else it }
            buildComputedState(updatedItems, current.expandedAminoAcids)
        }
    }

    fun stepItemWeight(instanceId: String, deltaGrams: Float) {
        val target = _uiState.value.simulatedItems.find { it.instanceId == instanceId } ?: return
        updateItemWeight(instanceId, (target.weightGrams + deltaGrams).coerceIn(0f, 2000f))
    }

    fun updateItemPricingMode(instanceId: String, mode: PricingMode) {
        _uiState.update { current ->
            val updatedItems = current.simulatedItems.map { if (it.instanceId == instanceId) it.copy(pricingMode = mode) else it }
            buildComputedState(updatedItems, current.expandedAminoAcids)
        }
    }

    fun updateItemManualPrice(instanceId: String, customPrice: Double) {
        _uiState.update { current ->
            val updatedItems = current.simulatedItems.map { if (it.instanceId == instanceId) it.copy(pricingMode = PricingMode.MANUAL, manualPricePerKg = customPrice) else it }
            buildComputedState(updatedItems, current.expandedAminoAcids)
        }
    }

    fun toggleAminoAcidExpanded(aminoAcid: AminoAcid) {
        _uiState.update { current ->
            val updatedExpanded = if (current.expandedAminoAcids.contains(aminoAcid)) current.expandedAminoAcids - aminoAcid else current.expandedAminoAcids + aminoAcid
            current.copy(expandedAminoAcids = updatedExpanded)
        }
    }

    fun setAllAminoAcidsExpanded(expand: Boolean) {
        _uiState.update { current -> current.copy(expandedAminoAcids = if (expand) AminoAcid.entries.toSet() else emptySet()) }
    }

    fun clearAllItems() {
        _uiState.update { current -> buildComputedState(emptyList(), current.expandedAminoAcids) }
    }

    private fun buildComputedState(items: List<SimulatedItem>, expandedSet: Set<AminoAcid>): MealSimulationUiState {
        var totalWeight = 0.0
        var totalCost = 0.0
        var totalCals = 0.0
        var totalProt = 0.0
        var totalCarbs = 0.0
        var totalFats = 0.0

        for (item in items) {
            totalWeight += item.weightGrams.toDouble()
            totalCost += item.calculatedCostRupees
            totalCals += item.calculatedCalories
            totalProt += item.calculatedProtein
            totalCarbs += item.calculatedCarbs
            totalFats += item.calculatedFats
        }

        val costPerGramProt = if (totalProt > 0.0) totalCost / totalProt else 0.0

        val aminoStats = AminoAcid.entries.map { aminoAcid ->
            var sumGrams = 0.0
            var topSourceName = "None"
            var topSourceGrams = 0.0

            for (item in items) {
                val yield = item.getAminoAcidYieldGrams(aminoAcid)
                sumGrams += yield
                if (yield > topSourceGrams) {
                    topSourceGrams = yield
                    topSourceName = item.ingredient.name
                }
            }

            val driPct = if (aminoAcid.driGrams > 0.0) (sumGrams / aminoAcid.driGrams) * 100.0 else 0.0
            val topShare = if (sumGrams > 0.0) (topSourceGrams / sumGrams) * 100.0 else 0.0

            AminoAcidSimulationStat(aminoAcid, sumGrams, driPct, topSourceName, topSourceGrams, topShare)
        }

        return MealSimulationUiState(
            simulatedItems = items, totalWeightGrams = totalWeight, totalCostRupees = totalCost,
            totalCalories = totalCals, totalProteinGrams = totalProt, totalCarbsGrams = totalCarbs,
            totalFatsGrams = totalFats, costPerGramProtein = costPerGramProt,
            aminoAcidStats = aminoStats, expandedAminoAcids = expandedSet
        )
    }
}
