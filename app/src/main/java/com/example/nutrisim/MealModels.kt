package com.example.nutrisim.model

import java.util.UUID

enum class AminoAcidCategory(val title: String) {
    ESSENTIAL("Essential Amino Acids (EAA)"),
    BCAA("Branched-Chain Amino Acids (BCAA - Subset of EAA)"),
    CONDITIONALLY_ESSENTIAL("Conditionally Essential & Non-Essential")
}

enum class AminoAcid(val displayName: String, val category: AminoAcidCategory, val driGrams: Double, val primaryRole: String) {
    LEUCINE("Leucine", AminoAcidCategory.BCAA, 2.73, "Triggers muscle protein synthesis"),
    ISOLEUCINE("Isoleucine", AminoAcidCategory.BCAA, 1.40, "Muscular glucose uptake"),
    VALINE("Valine", AminoAcidCategory.BCAA, 1.82, "Tissue repair & nitrogen balance"),
    LYSINE("Lysine", AminoAcidCategory.ESSENTIAL, 2.10, "Collagen synthesis"),
    METHIONINE("Methionine", AminoAcidCategory.ESSENTIAL, 1.05, "Metabolism & detoxification"),
    THREONINE("Threonine", AminoAcidCategory.ESSENTIAL, 1.05, "Structural protein formation"),
    PHENYLALANINE("Phenylalanine", AminoAcidCategory.ESSENTIAL, 1.75, "Precursor for dopamine"),
    TRYPTOPHAN("Tryptophan", AminoAcidCategory.ESSENTIAL, 0.28, "Precursor for serotonin"),
    HISTIDINE("Histidine", AminoAcidCategory.ESSENTIAL, 0.70, "Carnosine synthesis"),
    ARGININE("Arginine", AminoAcidCategory.CONDITIONALLY_ESSENTIAL, 3.50, "Nitric oxide precursor"),
    GLUTAMIC_ACID("Glutamine / Glutamic Acid", AminoAcidCategory.CONDITIONALLY_ESSENTIAL, 5.00, "Enterocyte fuel"),
    GLYCINE("Glycine", AminoAcidCategory.CONDITIONALLY_ESSENTIAL, 3.00, "Creatine synthesis")
}

enum class PricingMode(val label: String) { LOCAL_MARKET("Local Market"), MANUAL("Manual Price") }

data class Ingredient(
    val id: String, val name: String, val hindiOrLocalSubtext: String,
    val defaultMarketPricePerKg: Double, val marketUnitNote: String,
    val caloriesPer100g: Double, val proteinPer100g: Double, val carbsPer100g: Double, val fatsPer100g: Double,
    val aminoAcidProfilePer100g: Map<AminoAcid, Double>
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
    val PANEER = Ingredient("paneer_fresh", "Paneer", "Fresh Dairy", 450.0, "₹450/kg", 296.0, 18.3, 3.6, 22.0, mapOf(AminoAcid.LEUCINE to 1.78, AminoAcid.LYSINE to 1.48))
    val CHICKEN_BREAST = Ingredient("chicken_breast", "Chicken Breast", "Raw Skinless", 280.0, "₹280/kg", 120.0, 22.5, 0.0, 2.6, mapOf(AminoAcid.LEUCINE to 1.75, AminoAcid.LYSINE to 1.98))
    val WHOLE_EGGS = Ingredient("whole_eggs", "Whole Eggs", "₹120/doz eq. ₹200/kg", 200.0, "₹200/kg eq.", 143.0, 12.6, 0.7, 9.5, mapOf(AminoAcid.LEUCINE to 1.09, AminoAcid.LYSINE to 0.91))
    val DAL_LENTILS = Ingredient("moong_toor_dal", "Dal (Moong/Toor)", "Dry Split", 140.0, "₹140/kg", 343.0, 24.0, 59.0, 1.2, mapOf(AminoAcid.LEUCINE to 1.85, AminoAcid.LYSINE to 1.68))
    val WHEY_PROTEIN = Ingredient("whey_concentrate", "Whey Protein", "80% WPC", 3200.0, "₹3200/kg", 390.0, 78.0, 7.5, 5.2, mapOf(AminoAcid.LEUCINE to 8.60, AminoAcid.LYSINE to 7.20))
    val allIngredients = listOf(CHICKEN_BREAST, PANEER, WHOLE_EGGS, DAL_LENTILS, WHEY_PROTEIN)
}
