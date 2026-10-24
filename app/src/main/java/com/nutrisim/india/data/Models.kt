package com.nutrisim.india.data

data class AminoAcids(
    val leucine: Float = 0f,
    val lysine: Float = 0f,
    val methionine: Float = 0f,
    val valine: Float = 0f,
    val isoleucine: Float = 0f,
    val threonine: Float = 0f,
    val phenylalanine: Float = 0f,
    val tryptophan: Float = 0f
) {
    fun scale(factor: Float) = AminoAcids(
        leucine = leucine * factor,
        lysine = lysine * factor,
        methionine = methionine * factor,
        valine = valine * factor,
        isoleucine = isoleucine * factor,
        threonine = threonine * factor,
        phenylalanine = phenylalanine * factor,
        tryptophan = tryptophan * factor
    )

    operator fun plus(other: AminoAcids) = AminoAcids(
        leucine = leucine + other.leucine,
        lysine = lysine + other.lysine,
        methionine = methionine + other.methionine,
        valine = valine + other.valine,
        isoleucine = isoleucine + other.isoleucine,
        threonine = threonine + other.threonine,
        phenylalanine = phenylalanine + other.phenylalanine,
        tryptophan = tryptophan + other.tryptophan
    )
}

enum class FoodUnit { GRAMS, ML, PIECES }

enum class FoodCategory(val displayName: String, val emoji: String) {
    GRAIN("Grains", "🌾"),
    PROTEIN("Protein", "🥩"),
    DAIRY("Dairy", "🥛"),
    LEGUME("Legumes", "🫘"),
    VEGETABLE("Vegetables", "🥦"),
    FRUIT("Fruits", "🍌"),
    FAT("Fats & Oils", "🫙"),
    SUPPLEMENT("Supplements", "💊")
}

data class FoodItem(
    val id: String,
    val name: String,
    val category: FoodCategory,
    val unit: FoodUnit = FoodUnit.GRAMS,
    val caloriesPer100: Float,
    val proteinPer100: Float,
    val carbsPer100: Float,
    val fatsPer100: Float,
    val fiberPer100: Float = 0f,
    val aminoAcidsPer100: AminoAcids = AminoAcids(),
    val gramsPerPiece: Float = 0f,
    val defaultPriceINRPer100: Float,
    val emoji: String = "🍽️"
)

data class MealEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val foodItem: FoodItem,
    val weightGrams: Float,
    val customPriceINRPer100: Float? = null
) {
    private val scaleFactor get() = weightGrams / 100f
    val calories get() = foodItem.caloriesPer100 * scaleFactor
    val protein get() = foodItem.proteinPer100 * scaleFactor
    val carbs get() = foodItem.carbsPer100 * scaleFactor
    val fats get() = foodItem.fatsPer100 * scaleFactor
    val fiber get() = foodItem.fiberPer100 * scaleFactor
    val aminoAcids get() = foodItem.aminoAcidsPer100.scale(scaleFactor)
    val effectivePrice get() = customPriceINRPer100 ?: foodItem.defaultPriceINRPer100
    val costINR get() = effectivePrice * scaleFactor
    val costPerGramProtein get() = if (protein > 0.01f) costINR / protein else 0f
}

data class DailyTotals(
    val calories: Float = 0f,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fats: Float = 0f,
    val fiber: Float = 0f,
    val totalCostINR: Float = 0f,
    val aminoAcids: AminoAcids = AminoAcids()
)

data class NutritionGoals(
    val caloriesGoal: Float = 2000f,
    val proteinGoal: Float = 150f,
    val carbsGoal: Float = 250f,
    val fatsGoal: Float = 65f,
    val fiberGoal: Float = 30f
)

data class GeminiFoodResult(
    val name: String,
    val weightGrams: Float,
    val calories: Float,
    val proteinG: Float,
    val carbsG: Float,
    val fatsG: Float,
    val fiberG: Float = 0f,
    val estimatedPriceINR: Float,
    val notes: String = ""
)
