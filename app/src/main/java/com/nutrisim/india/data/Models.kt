package com.nutrisim.india.data

data class AminoAcids(
    val leucine: Float = 0f, val lysine: Float = 0f, val methionine: Float = 0f,
    val valine: Float = 0f, val isoleucine: Float = 0f, val threonine: Float = 0f,
    val phenylalanine: Float = 0f, val tryptophan: Float = 0f, val histidine: Float = 0f
) {
    fun scale(f: Float) = AminoAcids(
        leucine * f, lysine * f, methionine * f, valine * f,
        isoleucine * f, threonine * f, phenylalanine * f, tryptophan * f, histidine * f
    )
    operator fun plus(o: AminoAcids) = AminoAcids(
        leucine + o.leucine, lysine + o.lysine, methionine + o.methionine, valine + o.valine,
        isoleucine + o.isoleucine, threonine + o.threonine, phenylalanine + o.phenylalanine,
        tryptophan + o.tryptophan, histidine + o.histidine
    )
    fun formatPresent(): String = listOf(
        "Leu" to leucine, "Lys" to lysine, "Val" to valine, "Ile" to isoleucine,
        "Thr" to threonine, "Phe" to phenylalanine, "Met" to methionine, "His" to histidine, "Trp" to tryptophan
    ).filter { it.second > 0.02f }.joinToString(" · ") { "${it.first}${String.format("%.2f", it.second)}g" }
}

enum class FoodUnit { GRAMS, ML, PIECES }

enum class FoodCategory(val displayName: String, val emoji: String) {
    GRAIN("Grains", "🌾"), PROTEIN("Protein", "🥩"), DAIRY("Dairy", "🥛"),
    LEGUME("Legumes", "🫘"), VEGETABLE("Vegetables", "🥦"), FRUIT("Fruits", "🍌"),
    FAT("Fats & Oils", "🫙"), SUPPLEMENT("Supplements", "💊")
}

data class FoodItem(
    val id: String, val name: String, val category: FoodCategory,
    val unit: FoodUnit = FoodUnit.GRAMS,
    val caloriesPer100: Float, val proteinPer100: Float, val carbsPer100: Float, val fatsPer100: Float,
    val fiberPer100: Float = 0f, val sodiumMgPer100: Float = 0f,
    val vitamins: String = "", val minerals: String = "", val customAminosText: String = "",
    val aminoAcidsPer100: AminoAcids = AminoAcids(),
    val gramsPerPiece: Float = 0f, val defaultPriceINRPer100: Float, val emoji: String = "🍽️"
)

data class MealEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val foodItem: FoodItem, val weightGrams: Float, val customPriceINRPer100: Float? = null
) {
    private val sf get() = weightGrams / 100f
    val calories get() = foodItem.caloriesPer100 * sf
    val protein get() = foodItem.proteinPer100 * sf
    val carbs get() = foodItem.carbsPer100 * sf
    val fats get() = foodItem.fatsPer100 * sf
    val fiber get() = foodItem.fiberPer100 * sf
    val sodiumMg get() = foodItem.sodiumMgPer100 * sf
    val saltGrams get() = (sodiumMg * 2.5f) / 1000f
    val aminoAcids get() = foodItem.aminoAcidsPer100.scale(sf)
    val effectivePrice get() = customPriceINRPer100 ?: foodItem.defaultPriceINRPer100
    val costINR get() = effectivePrice * sf
    val costPerGramProtein get() = if (protein > 0.01f) costINR / protein else 0f
}

data class DailyTotals(
    val calories: Float = 0f, val protein: Float = 0f, val carbs: Float = 0f,
    val fats: Float = 0f, val fiber: Float = 0f, val sodiumMg: Float = 0f,
    val totalCostINR: Float = 0f, val aminoAcids: AminoAcids = AminoAcids(),
    val presentVitamins: String = "", val presentMinerals: String = ""
) {
    val saltGrams get() = (sodiumMg * 2.5f) / 1000f
}

data class NutritionGoals(
    val caloriesGoal: Float = 2000f, val proteinGoal: Float = 150f,
    val carbsGoal: Float = 250f, val fatsGoal: Float = 65f, val fiberGoal: Float = 30f
)

data class GeminiFoodResult(
    val name: String, val weightGrams: Float, val calories: Float,
    val proteinG: Float, val carbsG: Float, val fatsG: Float, val fiberG: Float = 0f,
    val sodiumMg: Float = 0f, val essentialAminoAcids: String = "",
    val vitamins: String = "", val minerals: String = "",
    val estimatedPriceINR: Float, val notes: String = ""
)
