package com.nutrisim.india.data

object FoodCatalog {

    val items: List<FoodItem> = listOf(

        // ── GRAINS ────────────────────────────────────────────────────────────
        FoodItem(
            id = "rice_cooked", name = "Rice (Cooked)", category = FoodCategory.GRAIN,
            caloriesPer100 = 130f, proteinPer100 = 2.7f, carbsPer100 = 28.2f, fatsPer100 = 0.3f,
            aminoAcidsPer100 = AminoAcids(leucine=0.22f, lysine=0.09f, methionine=0.06f, valine=0.15f, isoleucine=0.11f, threonine=0.10f, phenylalanine=0.14f, tryptophan=0.03f),
            defaultPriceINRPer100 = 5f, emoji = "🍚"
        ),
        FoodItem(
            id = "chapati", name = "Chapati", category = FoodCategory.GRAIN,
            unit = FoodUnit.PIECES, gramsPerPiece = 40f,
            caloriesPer100 = 297f, proteinPer100 = 7.9f, carbsPer100 = 52.4f, fatsPer100 = 6.3f,
            aminoAcidsPer100 = AminoAcids(leucine=0.77f, lysine=0.21f, methionine=0.18f, valine=0.46f, isoleucine=0.39f, threonine=0.28f, phenylalanine=0.58f, tryptophan=0.11f),
            defaultPriceINRPer100 = 10f, emoji = "🫓"
        ),
        FoodItem(
            id = "parantha", name = "Parantha", category = FoodCategory.GRAIN,
            unit = FoodUnit.PIECES, gramsPerPiece = 75f,
            caloriesPer100 = 260f, proteinPer100 = 5.3f, carbsPer100 = 37.3f, fatsPer100 = 10.4f,
            aminoAcidsPer100 = AminoAcids(leucine=0.52f, lysine=0.14f, methionine=0.12f, valine=0.31f, isoleucine=0.26f, threonine=0.19f, phenylalanine=0.39f, tryptophan=0.07f),
            defaultPriceINRPer100 = 13f, emoji = "🫓"
        ),
        FoodItem(
            id = "oats", name = "Oats (Rolled)", category = FoodCategory.GRAIN,
            caloriesPer100 = 389f, proteinPer100 = 16.9f, carbsPer100 = 66.3f, fatsPer100 = 6.9f,
            aminoAcidsPer100 = AminoAcids(leucine=1.28f, lysine=0.70f, methionine=0.31f, valine=0.94f, isoleucine=0.69f, threonine=0.57f, phenylalanine=0.90f, tryptophan=0.23f),
            defaultPriceINRPer100 = 20f, emoji = "🌾"
        ),
        FoodItem(
            id = "bread_ww", name = "Whole Wheat Bread", category = FoodCategory.GRAIN,
            unit = FoodUnit.PIECES, gramsPerPiece = 30f,
            caloriesPer100 = 247f, proteinPer100 = 12f, carbsPer100 = 43f, fatsPer100 = 3.5f,
            defaultPriceINRPer100 = 15f, emoji = "🍞"
        ),
        FoodItem(
            id = "poha", name = "Poha (Flattened Rice)", category = FoodCategory.GRAIN,
            caloriesPer100 = 387f, proteinPer100 = 8.1f, carbsPer100 = 81f, fatsPer100 = 2.3f,
            defaultPriceINRPer100 = 12f, emoji = "🌾"
        ),
        FoodItem(
            id = "upma", name = "Upma (Semolina)", category = FoodCategory.GRAIN,
            caloriesPer100 = 162f, proteinPer100 = 4.2f, carbsPer100 = 28f, fatsPer100 = 4.5f,
            defaultPriceINRPer100 = 8f, emoji = "🥣"
        ),

        // ── PROTEINS ──────────────────────────────────────────────────────────
        FoodItem(
            id = "chicken_breast", name = "Chicken Breast", category = FoodCategory.PROTEIN,
            caloriesPer100 = 165f, proteinPer100 = 31f, carbsPer100 = 0f, fatsPer100 = 3.6f,
            aminoAcidsPer100 = AminoAcids(leucine=2.47f, lysine=2.73f, methionine=0.91f, valine=1.49f, isoleucine=1.44f, threonine=1.39f, phenylalanine=1.29f, tryptophan=0.36f),
            defaultPriceINRPer100 = 45f, emoji = "🍗"
        ),
        FoodItem(
            id = "chicken_leg", name = "Chicken Leg (with skin)", category = FoodCategory.PROTEIN,
            caloriesPer100 = 215f, proteinPer100 = 22f, carbsPer100 = 0f, fatsPer100 = 13.5f,
            defaultPriceINRPer100 = 35f, emoji = "🍗"
        ),
        FoodItem(
            id = "egg", name = "Egg (Whole)", category = FoodCategory.PROTEIN,
            unit = FoodUnit.PIECES, gramsPerPiece = 50f,
            caloriesPer100 = 155f, proteinPer100 = 13f, carbsPer100 = 1.1f, fatsPer100 = 11f,
            aminoAcidsPer100 = AminoAcids(leucine=1.09f, lysine=0.91f, methionine=0.39f, valine=0.77f, isoleucine=0.71f, threonine=0.60f, phenylalanine=0.72f, tryptophan=0.17f),
            defaultPriceINRPer100 = 16f, emoji = "🥚"
        ),
        FoodItem(
            id = "egg_white", name = "Egg White", category = FoodCategory.PROTEIN,
            caloriesPer100 = 52f, proteinPer100 = 10.9f, carbsPer100 = 0.7f, fatsPer100 = 0.2f,
            aminoAcidsPer100 = AminoAcids(leucine=0.93f, lysine=0.73f, methionine=0.40f, valine=0.68f, isoleucine=0.62f, threonine=0.50f, phenylalanine=0.67f, tryptophan=0.16f),
            defaultPriceINRPer100 = 10f, emoji = "🥚"
        ),
        FoodItem(
            id = "paneer", name = "Paneer", category = FoodCategory.PROTEIN,
            caloriesPer100 = 265f, proteinPer100 = 18.3f, carbsPer100 = 1.2f, fatsPer100 = 20.8f,
            aminoAcidsPer100 = AminoAcids(leucine=1.80f, lysine=1.50f, methionine=0.49f, valine=1.18f, isoleucine=1.12f, threonine=0.80f, phenylalanine=1.00f, tryptophan=0.26f),
            defaultPriceINRPer100 = 65f, emoji = "🧀"
        ),
        FoodItem(
            id = "tofu", name = "Tofu", category = FoodCategory.PROTEIN,
            caloriesPer100 = 76f, proteinPer100 = 8f, carbsPer100 = 1.9f, fatsPer100 = 4.8f,
            aminoAcidsPer100 = AminoAcids(leucine=0.64f, lysine=0.53f, methionine=0.10f, valine=0.41f, isoleucine=0.39f, threonine=0.32f, phenylalanine=0.41f, tryptophan=0.10f),
            defaultPriceINRPer100 = 35f, emoji = "⬜"
        ),
        FoodItem(
            id = "rohu_fish", name = "Rohu Fish", category = FoodCategory.PROTEIN,
            caloriesPer100 = 97f, proteinPer100 = 16.6f, carbsPer100 = 0f, fatsPer100 = 3.3f,
            aminoAcidsPer100 = AminoAcids(leucine=1.32f, lysine=1.47f, methionine=0.49f, valine=0.80f, isoleucine=0.77f, threonine=0.75f, phenylalanine=0.69f, tryptophan=0.18f),
            defaultPriceINRPer100 = 40f, emoji = "🐟"
        ),
        FoodItem(
            id = "mutton", name = "Mutton (Cooked)", category = FoodCategory.PROTEIN,
            caloriesPer100 = 294f, proteinPer100 = 25f, carbsPer100 = 0f, fatsPer100 = 21f,
            defaultPriceINRPer100 = 100f, emoji = "🥩"
        ),

        // ── DAIRY ─────────────────────────────────────────────────────────────
        FoodItem(
            id = "milk_full", name = "Full Cream Milk", category = FoodCategory.DAIRY,
            unit = FoodUnit.ML,
            caloriesPer100 = 61f, proteinPer100 = 3.2f, carbsPer100 = 4.8f, fatsPer100 = 3.3f,
            aminoAcidsPer100 = AminoAcids(leucine=0.32f, lysine=0.26f, methionine=0.08f, valine=0.22f, isoleucine=0.20f, threonine=0.14f, phenylalanine=0.16f, tryptophan=0.05f),
            defaultPriceINRPer100 = 6f, emoji = "🥛"
        ),
        FoodItem(
            id = "curd", name = "Curd / Dahi", category = FoodCategory.DAIRY,
            caloriesPer100 = 61f, proteinPer100 = 3.5f, carbsPer100 = 4.7f, fatsPer100 = 3.3f,
            defaultPriceINRPer100 = 8f, emoji = "🥣"
        ),
        FoodItem(
            id = "paneer_low_fat", name = "Low-Fat Paneer", category = FoodCategory.DAIRY,
            caloriesPer100 = 160f, proteinPer100 = 20f, carbsPer100 = 2f, fatsPer100 = 7f,
            defaultPriceINRPer100 = 70f, emoji = "🧀"
        ),
        FoodItem(
            id = "whey_protein", name = "Whey Protein (Scoop)", category = FoodCategory.SUPPLEMENT,
            caloriesPer100 = 380f, proteinPer100 = 75f, carbsPer100 = 10f, fatsPer100 = 5f,
            aminoAcidsPer100 = AminoAcids(leucine=9.55f, lysine=8.47f, methionine=2.11f, valine=5.06f, isoleucine=5.69f, threonine=5.69f, phenylalanine=3.16f, tryptophan=1.90f),
            defaultPriceINRPer100 = 167f, emoji = "💪"
        ),

        // ── LEGUMES ──────────────────────────────────────────────────────────
        FoodItem(
            id = "dal_toor", name = "Toor Dal (Cooked)", category = FoodCategory.LEGUME,
            caloriesPer100 = 116f, proteinPer100 = 7.6f, carbsPer100 = 19.5f, fatsPer100 = 0.4f,
            aminoAcidsPer100 = AminoAcids(leucine=0.68f, lysine=0.55f, methionine=0.08f, valine=0.43f, isoleucine=0.39f, threonine=0.31f, phenylalanine=0.48f, tryptophan=0.08f),
            defaultPriceINRPer100 = 12f, emoji = "🫘"
        ),
        FoodItem(
            id = "dal_masoor", name = "Masoor Dal (Cooked)", category = FoodCategory.LEGUME,
            caloriesPer100 = 116f, proteinPer100 = 9f, carbsPer100 = 20f, fatsPer100 = 0.4f,
            defaultPriceINRPer100 = 15f, emoji = "🫘"
        ),
        FoodItem(
            id = "rajma", name = "Rajma (Kidney Beans, Cooked)", category = FoodCategory.LEGUME,
            caloriesPer100 = 127f, proteinPer100 = 8.7f, carbsPer100 = 22.8f, fatsPer100 = 0.5f,
            defaultPriceINRPer100 = 18f, emoji = "🫘"
        ),
        FoodItem(
            id = "chana", name = "Chickpeas / Chana (Cooked)", category = FoodCategory.LEGUME,
            caloriesPer100 = 164f, proteinPer100 = 8.9f, carbsPer100 = 27.4f, fatsPer100 = 2.6f,
            defaultPriceINRPer100 = 15f, emoji = "🫘"
        ),
        FoodItem(
            id = "peanuts", name = "Peanuts / Moongphali", category = FoodCategory.LEGUME,
            caloriesPer100 = 567f, proteinPer100 = 25.8f, carbsPer100 = 16.1f, fatsPer100 = 49.2f,
            aminoAcidsPer100 = AminoAcids(leucine=1.67f, lysine=0.93f, methionine=0.31f, valine=1.08f, isoleucine=0.90f, threonine=0.68f, phenylalanine=1.34f, tryptophan=0.25f),
            defaultPriceINRPer100 = 18f, emoji = "🥜"
        ),
        FoodItem(
            id = "soybean", name = "Soybean (Cooked)", category = FoodCategory.LEGUME,
            caloriesPer100 = 173f, proteinPer100 = 16.6f, carbsPer100 = 9.9f, fatsPer100 = 9f,
            aminoAcidsPer100 = AminoAcids(leucine=1.37f, lysine=1.08f, methionine=0.26f, valine=0.82f, isoleucine=0.78f, threonine=0.68f, phenylalanine=0.89f, tryptophan=0.24f),
            defaultPriceINRPer100 = 20f, emoji = "🫘"
        ),

        // ── VEGETABLES ────────────────────────────────────────────────────────
        FoodItem(
            id = "spinach", name = "Spinach / Palak", category = FoodCategory.VEGETABLE,
            caloriesPer100 = 23f, proteinPer100 = 2.9f, carbsPer100 = 3.6f, fatsPer100 = 0.4f,
            defaultPriceINRPer100 = 5f, emoji = "🥬"
        ),
        FoodItem(
            id = "potato", name = "Potato / Aloo", category = FoodCategory.VEGETABLE,
            caloriesPer100 = 77f, proteinPer100 = 2f, carbsPer100 = 17.5f, fatsPer100 = 0.1f,
            defaultPriceINRPer100 = 4f, emoji = "🥔"
        ),
        FoodItem(
            id = "onion", name = "Onion / Pyaaz", category = FoodCategory.VEGETABLE,
            caloriesPer100 = 40f, proteinPer100 = 1.1f, carbsPer100 = 9.3f, fatsPer100 = 0.1f,
            defaultPriceINRPer100 = 4f, emoji = "🧅"
        ),
        FoodItem(
            id = "tomato", name = "Tomato", category = FoodCategory.VEGETABLE,
            caloriesPer100 = 18f, proteinPer100 = 0.9f, carbsPer100 = 3.9f, fatsPer100 = 0.2f,
            defaultPriceINRPer100 = 6f, emoji = "🍅"
        ),
        FoodItem(
            id = "broccoli", name = "Broccoli", category = FoodCategory.VEGETABLE,
            caloriesPer100 = 34f, proteinPer100 = 2.8f, carbsPer100 = 6.6f, fatsPer100 = 0.4f,
            defaultPriceINRPer100 = 20f, emoji = "🥦"
        ),

        // ── FRUITS ────────────────────────────────────────────────────────────
        FoodItem(
            id = "banana", name = "Banana / Kela", category = FoodCategory.FRUIT,
            unit = FoodUnit.PIECES, gramsPerPiece = 120f,
            caloriesPer100 = 89f, proteinPer100 = 1.1f, carbsPer100 = 22.8f, fatsPer100 = 0.3f,
            defaultPriceINRPer100 = 8f, emoji = "🍌"
        ),
        FoodItem(
            id = "apple", name = "Apple / Seb", category = FoodCategory.FRUIT,
            unit = FoodUnit.PIECES, gramsPerPiece = 182f,
            caloriesPer100 = 52f, proteinPer100 = 0.3f, carbsPer100 = 14f, fatsPer100 = 0.2f,
            defaultPriceINRPer100 = 20f, emoji = "🍎"
        ),
        FoodItem(
            id = "mango", name = "Mango / Aam", category = FoodCategory.FRUIT,
            caloriesPer100 = 60f, proteinPer100 = 0.8f, carbsPer100 = 15f, fatsPer100 = 0.4f,
            defaultPriceINRPer100 = 15f, emoji = "🥭"
        ),

        // ── FATS & OILS ───────────────────────────────────────────────────────
        FoodItem(
            id = "ghee", name = "Ghee", category = FoodCategory.FAT,
            caloriesPer100 = 900f, proteinPer100 = 0f, carbsPer100 = 0f, fatsPer100 = 99.5f,
            defaultPriceINRPer100 = 60f, emoji = "🫙"
        ),
        FoodItem(
            id = "mustard_oil", name = "Mustard Oil", category = FoodCategory.FAT,
            caloriesPer100 = 884f, proteinPer100 = 0f, carbsPer100 = 0f, fatsPer100 = 100f,
            defaultPriceINRPer100 = 20f, emoji = "🫙"
        ),
        FoodItem(
            id = "almonds", name = "Almonds / Badam", category = FoodCategory.FAT,
            caloriesPer100 = 579f, proteinPer100 = 21.2f, carbsPer100 = 21.6f, fatsPer100 = 49.9f,
            aminoAcidsPer100 = AminoAcids(leucine=1.49f, lysine=0.58f, methionine=0.15f, valine=1.00f, isoleucine=0.74f, threonine=0.59f, phenylalanine=1.13f, tryptophan=0.21f),
            defaultPriceINRPer100 = 80f, emoji = "🌰"
        ),
        FoodItem(
            id = "peanut_butter", name = "Peanut Butter", category = FoodCategory.FAT,
            caloriesPer100 = 588f, proteinPer100 = 25f, carbsPer100 = 20f, fatsPer100 = 50f,
            defaultPriceINRPer100 = 45f, emoji = "🥜"
        )
    )

    fun getById(id: String): FoodItem? = items.find { it.id == id }
    fun getByCategory(category: FoodCategory): List<FoodItem> = items.filter { it.category == category }
    fun search(query: String): List<FoodItem> = items.filter {
        it.name.contains(query, ignoreCase = true) || it.category.displayName.contains(query, ignoreCase = true)
    }
}
