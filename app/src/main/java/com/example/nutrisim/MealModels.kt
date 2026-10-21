package com.example.nutrisim.model

import java.util.UUID

enum class AminoAcidCategory(val title: String) {
    ESSENTIAL("Essential Amino Acids (EAA)"),
    BCAA("Branched-Chain Amino Acids (BCAA - Subset of EAA)"),
    CONDITIONALLY_ESSENTIAL("Conditionally Essential & Non-Essential")
}

enum class AminoAcid(
    val displayName: String,
    val category: AminoAcidCategory,
    val driGrams: Double,
    val primaryRole: String
) {
    LEUCINE("Leucine", AminoAcidCategory.BCAA, 2.73, "Triggers muscle protein synthesis (mTOR pathway)"),
    ISOLEUCINE("Isoleucine", AminoAcidCategory.BCAA, 1.40, "Muscular glucose uptake & energy regulation"),
    VALINE("Valine", AminoAcidCategory.BCAA, 1.82, "Tissue repair & nitrogen balance maintenance"),
    LYSINE("Lysine", AminoAcidCategory.ESSENTIAL, 2.10, "Collagen synthesis, calcium absorption & carnitine production"),
    METHIONINE("Methionine", AminoAcidCategory.ESSENTIAL, 1.05, "Sulfur donor, metabolism & detoxification"),
    THREONINE("Threonine", AminoAcidCategory.ESSENTIAL, 1.05, "Structural protein formation"),
    PHENYLALANINE("Phenylalanine", AminoAcidCategory.ESSENTIAL, 1.75, "Precursor for tyrosine, dopamine & norepinephrine"),
    TRYPTOPHAN("Tryptophan", AminoAcidCategory.ESSENTIAL, 0.28, "Precursor for serotonin & melatonin biosynthesis"),
    HISTIDINE("Histidine", AminoAcidCategory.ESSENTIAL, 0.70, "Carnosine synthesis & blood buffering"),
    ARGININE("Arginine", AminoAcidCategory.CONDITIONALLY_ESSENTIAL, 3.50, "Nitric oxide (NO) precursor & vasodilation"),
    GLUTAMIC_ACID("Glutamine / Glutamic Acid", AminoAcidCategory.CONDITIONALLY_ESSENTIAL, 5.00, "Enterocyte fuel & immune modulation"),
    GLYCINE("Glycine", AminoAcidCategory.CONDITIONALLY_ESSENTIAL, 3.00, "Collagen triple-helix structure & creatine synthesis")
}

enum class PricingMode(val label: String) {
    LOCAL_MARKET("Local Market Price (India)"),
    MANUAL("Manual Price")
}

data class Ingredient(
    val id: String,
    val name: String,
    val hindiOrLocalSubtext: String,
    val defaultMarketPricePerKg: Double,
    val marketUnitNote: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatsPer100g: Double,
    val aminoAcidProfilePer100g: Map<AminoAcid, Double>
)

data class SimulatedItem(
    val instanceId: String = UUID.randomUUID().toString(),
    val ingredient: Ingredient,
    val weightGrams: Float = 100f,
    val pricingMode: PricingMode = PricingMode.LOCAL_MARKET,
    val manualPricePerKg: Double = ingredient.defaultMarketPricePerKg
) {
    val activePricePerKg: Double
        get() = when (pricingMode) {
            PricingMode.LOCAL_MARKET -> ingredient.defaultMarketPricePerKg
            PricingMode.MANUAL -> manualPricePerKg.coerceAtLeast(0.0)
        }

    val calculatedCostRupees: Double
        get() = (weightGrams.toDouble() / 1000.0) * activePricePerKg

    private val weightRatioPer100g: Double
        get() = weightGrams.toDouble() / 100.0

    val calculatedCalories: Double get() = ingredient.caloriesPer100g * weightRatioPer100g
    val calculatedProtein: Double get() = ingredient.proteinPer100g * weightRatioPer100g
    val calculatedCarbs: Double get() = ingredient.carbsPer100g * weightRatioPer100g
    val calculatedFats: Double get() = ingredient.fatsPer100g * weightRatioPer100g

    fun getAminoAcidYieldGrams(aminoAcid: AminoAcid): Double {
        val basePer100g = ingredient.aminoAcidProfilePer100g[aminoAcid] ?: 0.0
        return basePer100g * weightRatioPer100g
    }
}

data class AminoAcidSimulationStat(
    val aminoAcid: AminoAcid,
    val totalGrams: Double,
    val driPercentage: Double,
    val topContributorName: String,
    val topContributorGrams: Double,
    val topContributorSharePercent: Double
)

data class MealSimulationUiState(
    val simulatedItems: List<SimulatedItem> = emptyList(),
    val totalWeightGrams: Double = 0.0,
    val totalCostRupees: Double = 0.0,
    val totalCalories: Double = 0.0,
    val totalProteinGrams: Double = 0.0,
    val totalCarbsGrams: Double = 0.0,
    val totalFatsGrams: Double = 0.0,
    val costPerGramProtein: Double = 0.0,
    val aminoAcidStats: List<AminoAcidSimulationStat> = emptyList(),
    val expandedAminoAcids: Set<AminoAcid> = emptySet()
)

object IndianMarketCatalog {
    val PANEER = Ingredient("paneer_fresh", "Paneer (Cottage Cheese)", "Fresh Dairy Paneer", 450.0, "₹450 / kg baseline", 296.0, 18.3, 3.6, 22.0, mapOf(AminoAcid.LEUCINE to 1.78, AminoAcid.ISOLEUCINE to 0.98, AminoAcid.VALINE to 1.22, AminoAcid.LYSINE to 1.48, AminoAcid.METHIONINE to 0.52, AminoAcid.THREONINE to 0.81, AminoAcid.TRYPTOPHAN to 0.25, AminoAcid.PHENYLALANINE to 0.95, AminoAcid.HISTIDINE to 0.53, AminoAcid.ARGININE to 0.68, AminoAcid.GLUTAMIC_ACID to 3.85, AminoAcid.GLYCINE to 0.35))
    val CHICKEN_BREAST = Ingredient("chicken_breast", "Chicken Breast (Boneless)", "Raw Skinless Fillet", 280.0, "₹280 / kg baseline", 120.0, 22.5, 0.0, 2.6, mapOf(AminoAcid.LEUCINE to 1.75, AminoAcid.ISOLEUCINE to 1.15, AminoAcid.VALINE to 1.14, AminoAcid.LYSINE to 1.98, AminoAcid.METHIONINE to 0.62, AminoAcid.THREONINE to 0.96, AminoAcid.TRYPTOPHAN to 0.26, AminoAcid.PHENYLALANINE to 0.90, AminoAcid.HISTIDINE to 0.72, AminoAcid.ARGININE to 1.38, AminoAcid.GLUTAMIC_ACID to 3.30, AminoAcid.GLYCINE to 1.05))
    val WHOLE_EGGS = Ingredient("whole_eggs", "Whole Eggs (Poultry)", "₹120/doz eq. ₹200/kg", 200.0, "₹200 / kg eq.", 143.0, 12.6, 0.7, 9.5, mapOf(AminoAcid.LEUCINE to 1.09, AminoAcid.ISOLEUCINE to 0.67, AminoAcid.VALINE to 0.86, AminoAcid.LYSINE to 0.91, AminoAcid.METHIONINE to 0.38, AminoAcid.THREONINE to 0.56, AminoAcid.TRYPTOPHAN to 0.17, AminoAcid.PHENYLALANINE to 0.68, AminoAcid.HISTIDINE to 0.31, AminoAcid.ARGININE to 0.82, AminoAcid.GLUTAMIC_ACID to 1.68, AminoAcid.GLYCINE to 0.43))
    val DAL_LENTILS = Ingredient("moong_toor_dal", "Dal (Yellow Moong / Toor)", "Dry Split Lentils", 140.0, "₹140 / kg baseline", 343.0, 24.0, 59.0, 1.2, mapOf(AminoAcid.LEUCINE to 1.85, AminoAcid.ISOLEUCINE to 1.01, AminoAcid.VALINE to 1.24, AminoAcid.LYSINE to 1.68, AminoAcid.METHIONINE to 0.29, AminoAcid.THREONINE to 0.78, AminoAcid.TRYPTOPHAN to 0.24, AminoAcid.PHENYLALANINE to 1.44, AminoAcid.HISTIDINE to 0.68, AminoAcid.ARGININE to 1.65, AminoAcid.GLUTAMIC_ACID to 4.20, AminoAcid.GLYCINE to 0.95))
    val WHEY_PROTEIN = Ingredient("whey_concentrate", "Whey Protein Concentrate", "Standard 80% WPC Powder", 3200.0, "₹3,200 / kg baseline", 390.0, 78.0, 7.5, 5.2, mapOf(AminoAcid.LEUCINE to 8.60, AminoAcid.ISOLEUCINE to 4.80, AminoAcid.VALINE to 4.60, AminoAcid.LYSINE to 7.20, AminoAcid.METHIONINE to 1.70, AminoAcid.THREONINE to 5.40, AminoAcid.TRYPTOPHAN to 1.40, AminoAcid.PHENYLALANINE to 2.50, AminoAcid.HISTIDINE to 1.40, AminoAcid.ARGININE to 1.90, AminoAcid.GLUTAMIC_ACID to 13.50, AminoAcid.GLYCINE to 1.40))
    val allIngredients = listOf(CHICKEN_BREAST, PANEER, WHOLE_EGGS, DAL_LENTILS, WHEY_PROTEIN)
}
