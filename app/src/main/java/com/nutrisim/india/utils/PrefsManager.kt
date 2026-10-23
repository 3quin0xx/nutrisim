package com.nutrisim.india.utils

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nutrisim_prefs")

class PrefsManager(private val context: Context) {

    companion object {
        val KEY_CUSTOM_API    = stringPreferencesKey("custom_api_blob")
        val KEY_CAL_GOAL      = floatPreferencesKey("goal_calories")
        val KEY_PROTEIN_GOAL  = floatPreferencesKey("goal_protein")
        val KEY_CARBS_GOAL    = floatPreferencesKey("goal_carbs")
        val KEY_FATS_GOAL     = floatPreferencesKey("goal_fats")
        val KEY_MEAL_JSON     = stringPreferencesKey("meal_entries_json")

        private const val MASK = 90

        // XOR-obfuscated default key (never stored as a plain string in APK or source)
        private val DEFAULT_KEY_BYTES = intArrayOf(
            27, 11, 116, 27, 56, 98, 8, 20, 108, 22, 0, 61, 28, 44, 59, 16,
            46, 52, 10, 45, 27, 45, 49, 14, 48, 43, 55, 98, 50, 110, 109, 44,
            30, 9, 52, 49, 29, 0, 12, 43, 57, 9, 55, 45, 52, 13, 19, 61,
            14, 27, 49, 110, 27
        )

        private fun getBuiltInKey(): String =
            DEFAULT_KEY_BYTES.map { (it xor MASK).toChar() }.joinToString("")

        private fun obfuscate(raw: String): String {
            val xored = raw.map { (it.code xor MASK).toByte() }.toByteArray()
            return Base64.encodeToString(xored, Base64.NO_WRAP)
        }

        private fun deobfuscate(blob: String): String {
            return try {
                val bytes = Base64.decode(blob, Base64.NO_WRAP)
                bytes.map { (it.toInt() xor MASK).toChar() }.joinToString("")
            } catch (e: Exception) {
                ""
            }
        }
    }

    // Returns true if user saved a custom override key, false if using built-in default
    val hasCustomKey: Flow<Boolean> = context.dataStore.data.map {
        !it[KEY_CUSTOM_API].isNullOrEmpty()
    }

    // Internal flow that provides custom key if set, otherwise built-in default key
    val effectiveApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        val blob = prefs[KEY_CUSTOM_API]
        if (!blob.isNullOrEmpty()) {
            val decoded = deobfuscate(blob)
            if (decoded.isNotEmpty()) decoded else getBuiltInKey()
        } else {
            getBuiltInKey()
        }
    }

    val caloriesGoal: Flow<Float> = context.dataStore.data.map { it[KEY_CAL_GOAL] ?: 2000f }
    val proteinGoal:  Flow<Float> = context.dataStore.data.map { it[KEY_PROTEIN_GOAL] ?: 150f }
    val carbsGoal:    Flow<Float> = context.dataStore.data.map { it[KEY_CARBS_GOAL] ?: 250f }
    val fatsGoal:     Flow<Float> = context.dataStore.data.map { it[KEY_FATS_GOAL] ?: 65f }
    val mealJson:     Flow<String> = context.dataStore.data.map { it[KEY_MEAL_JSON] ?: "[]" }

    suspend fun saveCustomApiKey(key: String) {
        val trimmed = key.trim()
        context.dataStore.edit {
            if (trimmed.isEmpty()) {
                it.remove(KEY_CUSTOM_API)
            } else {
                it[KEY_CUSTOM_API] = obfuscate(trimmed)
            }
        }
    }

    suspend fun resetToBuiltInKey() {
        context.dataStore.edit { it.remove(KEY_CUSTOM_API) }
    }

    suspend fun saveGoals(calories: Float, protein: Float, carbs: Float, fats: Float) {
        context.dataStore.edit {
            it[KEY_CAL_GOAL]     = calories
            it[KEY_PROTEIN_GOAL] = protein
            it[KEY_CARBS_GOAL]   = carbs
            it[KEY_FATS_GOAL]    = fats
        }
    }

    suspend fun saveMealJson(json: String) {
        context.dataStore.edit { it[KEY_MEAL_JSON] = json }
    }
}
