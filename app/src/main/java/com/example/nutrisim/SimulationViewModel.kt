package com.example.nutrisim.viewmodel
import androidx.lifecycle.ViewModel
import com.example.nutrisim.model.*
import kotlinx.coroutines.flow.*
import java.util.UUID
class SimulationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MealSimulationUiState())
    val uiState = _uiState.asStateFlow()
    init { loadDefaultSampleMeal() }
    fun loadDefaultSampleMeal() {
        val initialItems = listOf(
            SimulatedItem(ingredient = IndianMarketCatalog.CHAPATI, weightGrams = 80f),
            SimulatedItem(ingredient = IndianMarketCatalog.DAL, weightGrams = 100f)
        )
        _uiState.value = buildComputedState(initialItems, emptySet())
    }
    fun addIngredientToMeal(ingredient: Ingredient) {
        _uiState.update { current -> buildComputedState(current.simulatedItems + SimulatedItem(ingredient = ingredient), current.expandedAminoAcids) }
    }
    fun addCustomIngredient(name: String, price: Double, cals: Double, prot: Double, carbs: Double, fats: Double) {
        val custom = Ingredient(
            id = UUID.randomUUID().toString(), name = name, hindiOrLocalSubtext = "Custom Entry (per 100g)",
            defaultMarketPricePerKg = price, marketUnitNote = "₹$price/kg",
            caloriesPer100g = cals, proteinPer100g = prot, carbsPer100g = carbs, fatsPer100g = fats, emptyMap()
        )
        addIngredientToMeal(custom)
    }
    fun removeIngredientFromMeal(instanceId: String) {
        _uiState.update { current -> buildComputedState(current.simulatedItems.filterNot { it.instanceId == instanceId }, current.expandedAminoAcids) }
    }
    fun updateItemWeight(instanceId: String, weight: Float) {
        _uiState.update { current -> buildComputedState(current.simulatedItems.map { if (it.instanceId == instanceId) it.copy(weightGrams = weight) else it }, current.expandedAminoAcids) }
    }
    fun updateItemPricingMode(instanceId: String, mode: PricingMode) {
        _uiState.update { current -> buildComputedState(current.simulatedItems.map { if (it.instanceId == instanceId) it.copy(pricingMode = mode) else it }, current.expandedAminoAcids) }
    }
    fun updateItemManualPrice(instanceId: String, price: Double) {
        _uiState.update { current -> buildComputedState(current.simulatedItems.map { if (it.instanceId == instanceId) it.copy(pricingMode = PricingMode.MANUAL, manualPricePerKg = price) else it }, current.expandedAminoAcids) }
    }
    fun toggleAminoAcidExpanded(aminoAcid: AminoAcid) {
        _uiState.update { current ->
            val set = if (current.expandedAminoAcids.contains(aminoAcid)) current.expandedAminoAcids - aminoAcid else current.expandedAminoAcids + aminoAcid
            current.copy(expandedAminoAcids = set)
        }
    }
    fun setAllAminoAcidsExpanded(expand: Boolean) = _uiState.update { it.copy(expandedAminoAcids = if (expand) AminoAcid.entries.toSet() else emptySet()) }
    fun clearAllItems() = _uiState.update { buildComputedState(emptyList(), it.expandedAminoAcids) }
    private fun buildComputedState(items: List<SimulatedItem>, expandedSet: Set<AminoAcid>): MealSimulationUiState {
        var tW=0.0; var tC=0.0; var tCal=0.0; var tP=0.0; var tCb=0.0; var tF=0.0
        items.forEach { tW+=it.weightGrams; tC+=it.calculatedCostRupees; tCal+=it.calculatedCalories; tP+=it.calculatedProtein; tCb+=it.calculatedCarbs; tF+=it.calculatedFats }
        val costP = if (tP > 0) tC / tP else 0.0
        val stats = AminoAcid.entries.map { aa ->
            var sum = 0.0; var topN = "None"; var topG = 0.0
            items.forEach { val y = it.getAminoAcidYieldGrams(aa); sum+=y; if(y>topG){topG=y; topN=it.ingredient.name} }
            AminoAcidSimulationStat(aa, sum, if(aa.driGrams>0)(sum/aa.driGrams)*100 else 0.0, topN, topG, if(sum>0)(topG/sum)*100 else 0.0)
        }
        return MealSimulationUiState(items, tW, tC, tCal, tP, tCb, tF, costP, stats, expandedSet)
    }
}
