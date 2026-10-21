package com.example.nutrisim.model
import java.util.UUID
enum class AminoAcidCategory(val title: String) { ESSENTIAL("Essential"), BCAA("BCAA"), CONDITIONALLY_ESSENTIAL("Conditionally Essential") }
enum class AminoAcid(val displayName: String, val category: AminoAcidCategory, val driGrams: Double, val primaryRole: String) {
    LEUCINE("Leucine", AminoAcidCategory.BCAA, 2.73, "Muscle protein synthesis"),
    ISOLEUCINE("Isoleucine", AminoAcidCategory.BCAA, 1.40, "Glucose uptake"),
    VALINE("Valine", AminoAcidCategory.BCAA, 1.82, "Tissue repair"),
    LYSINE("Lysine", AminoAcidCategory.ESSENTIAL, 2.10, "Collagen synthesis"),
    METHIONINE("Methionine", AminoAcidCategory.ESSENTIAL, 1.05, "Metabolism"),
    THREONINE("Threonine", AminoAcidCategory.ESSENTIAL, 1.05, "Protein formation"),
    PHENYLALANINE("Phenylalanine", AminoAcidCategory.ESSENTIAL, 1.75, "Dopamine precursor"),
    TRYPTOPHAN("Tryptophan", AminoAcidCategory.ESSENTIAL, 0.28, "Serotonin precursor"),
    HISTIDINE("Histidine", AminoAcidCategory.ESSENTIAL, 0.70, "Carnosine synthesis"),
    ARGININE("Arginine", AminoAcidCategory.CONDITIONALLY_ESSENTIAL, 3.50, "Nitric oxide"),
    GLUTAMIC_ACID("Glutamine", AminoAcidCategory.CONDITIONALLY_ESSENTIAL, 5.00, "Enterocyte fuel"),
    GLYCINE("Glycine", AminoAcidCategory.CONDITIONALLY_ESSENTIAL, 3.00, "Creatine synthesis")
}
enum class PricingMode(val label: String) { LOCAL_MARKET("Local Market"), MANUAL("Manual Price") }
data class Ingredient(
    val id: String, val name: String, val hindiOrLocalSubtext: String,
    val defaultMarketPricePerKg: Double, val marketUnitNote: String,
    val caloriesPer100g: Double, val proteinPer100g: Double, val carbsPer100g: Double, val fatsPer100g: Double,
    val aminoAcidProfilePer100g: Map<AminoAcid, Double>, val pieceWeightGrams: Float? = null
)
data class SimulatedItem(
    val instanceId: String = UUID.randomUUID().toString(),
    val ingredient: Ingredient, val weightGrams: Float = 100f,
    val pricingMode: PricingMode = PricingMode.LOCAL_MARKET,
    val manualPricePerKg: Double = ingredient.defaultMarketPricePerKg
) {
    val activePricePerKg: Double get() = if (pricingMode == PricingMode.LOCAL_MARKET) ingredient.defaultMarketPricePerKg else manualPricePerKg.coerceAtLeast(0.0)
    val calculatedCostRupees: Double get() = (weightGrams.toDouble() / 1000.0) * activePricePerKg
    private val weightRatioPer100g: Double get() = weightGrams.toDouble() / 100.0
    val calculatedCalories: Double get() = ingredient.caloriesPer100g * weightRatioPer100g
    val calculatedProtein: Double get() = ingredient.proteinPer100g * weightRatioPer100g
    val calculatedCarbs: Double get() = ingredient.carbsPer100g * weightRatioPer100g
    val calculatedFats: Double get() = ingredient.fatsPer100g * weightRatioPer100g
    fun getAminoAcidYieldGrams(aminoAcid: AminoAcid): Double = (ingredient.aminoAcidProfilePer100g[aminoAcid] ?: 0.0) * weightRatioPer100g
}
data class AminoAcidSimulationStat(
    val aminoAcid: AminoAcid, val totalGrams: Double, val driPercentage: Double,
    val topContributorName: String, val topContributorGrams: Double, val topContributorSharePercent: Double
)
data class MealSimulationUiState(
    val simulatedItems: List<SimulatedItem> = emptyList(), val totalWeightGrams: Double = 0.0,
    val totalCostRupees: Double = 0.0, val totalCalories: Double = 0.0, val totalProteinGrams: Double = 0.0,
    val totalCarbsGrams: Double = 0.0, val totalFatsGrams: Double = 0.0, val costPerGramProtein: Double = 0.0,
    val aminoAcidStats: List<AminoAcidSimulationStat> = emptyList(), val expandedAminoAcids: Set<AminoAcid> = emptySet()
)
object IndianMarketCatalog {
    val PANEER = Ingredient("paneer", "Paneer", "Fresh Dairy", 450.0, "₹450/kg", 296.0, 18.3, 3.6, 22.0, emptyMap())
    val CHICKEN = Ingredient("chicken", "Chicken Breast", "Raw Skinless", 280.0, "₹280/kg", 120.0, 22.5, 0.0, 2.6, emptyMap())
    val EGGS = Ingredient("eggs", "Whole Eggs", "1 pc = ~50g", 200.0, "₹200/kg", 143.0, 12.6, 0.7, 9.5, emptyMap(), pieceWeightGrams = 50f)
    val DAL = Ingredient("dal", "Dal (Moong/Toor)", "Dry Split", 140.0, "₹140/kg", 343.0, 24.0, 59.0, 1.2, emptyMap())
    val RICE = Ingredient("rice", "White Rice", "Boiled/Cooked", 60.0, "₹60/kg", 130.0, 2.7, 28.0, 0.3, emptyMap())
    val CHAPATI = Ingredient("chapati", "Chapati / Roti", "1 pc = ~40g", 50.0, "₹50/kg", 297.0, 9.0, 46.0, 9.0, emptyMap(), pieceWeightGrams = 40f)
    val PARANTHA = Ingredient("parantha", "Plain Parantha", "1 pc = ~75g", 80.0, "₹80/kg", 328.0, 8.0, 48.0, 12.0, emptyMap(), pieceWeightGrams = 75f)
    val WHEY = Ingredient("whey", "Whey Protein", "80% WPC", 3200.0, "₹3200/kg", 390.0, 78.0, 7.5, 5.2, emptyMap())
    val allIngredients = listOf(CHICKEN, PANEER, EGGS, DAL, RICE, CHAPATI, PARANTHA, WHEY)
}
