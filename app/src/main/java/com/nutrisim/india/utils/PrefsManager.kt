package com.nutrisim.india.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nutrisim_prefs")

class PrefsManager(private val context: Context) {

    companion object {
        val KEY_GEMINI_API    = stringPreferencesKey("gemini_api_key")
        val KEY_CAL_GOAL      = floatPreferencesKey("goal_calories")
        val KEY_PROTEIN_GOAL  = floatPreferencesKey("goal_protein")
        val KEY_CARBS_GOAL    = floatPreferencesKey("goal_carbs")
        val KEY_FATS_GOAL     = floatPreferencesKey("goal_fats")
        val KEY_MEAL_JSON     = stringPreferencesKey("meal_entries_json")
    }

    val geminiApiKey: Flow<String>  = context.dataStore.data.map { it[KEY_GEMINI_API] ?: "" }
    val caloriesGoal: Flow<Float>   = context.dataStore.data.map { it[KEY_CAL_GOAL] ?: 2000f }
    val proteinGoal:  Flow<Float>   = context.dataStore.data.map { it[KEY_PROTEIN_GOAL] ?: 150f }
    val carbsGoal:    Flow<Float>   = context.dataStore.data.map { it[KEY_CARBS_GOAL] ?: 250f }
    val fatsGoal:     Flow<Float>   = context.dataStore.data.map { it[KEY_FATS_GOAL] ?: 65f }
    val mealJson:     Flow<String>  = context.dataStore.data.map { it[KEY_MEAL_JSON] ?: "[]" }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { it[KEY_GEMINI_API] = key }
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
