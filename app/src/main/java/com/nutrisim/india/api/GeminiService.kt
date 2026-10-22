package com.nutrisim.india.api

import com.google.gson.Gson
import com.nutrisim.india.data.GeminiFoodResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private val BASE_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    suspend fun estimateMacros(query: String, apiKey: String): Result<GeminiFoodResult> =
        withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    You are a precise Indian food nutritionist and pricing expert.
                    For this food query: "$query"
                    
                    Return ONLY valid JSON with no markdown fences, no extra text, just the JSON object:
                    {
                      "name": "exact food name",
                      "weightGrams": 100,
                      "calories": 0.0,
                      "proteinG": 0.0,
                      "carbsG": 0.0,
                      "fatsG": 0.0,
                      "estimatedPriceINR": 0.0,
                      "notes": "one brief note"
                    }
                    
                    Rules:
                    - weightGrams = total weight of the full query (e.g. if "3 paranthas" then ~225g)
                    - All macro values are for the TOTAL weight given, not per 100g
                    - estimatedPriceINR = realistic Indian market price in rupees
                    - Be precise; use real nutritional data
                """.trimIndent()

                val body = gson.toJson(
                    mapOf("contents" to listOf(mapOf("parts" to listOf(mapOf("text" to prompt)))))
                )

                val request = Request.Builder()
                    .url("$BASE_URL?key=$apiKey")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty response from Gemini"))

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Gemini API error ${response.code}: $responseBody")
                    )
                }

                val geminiResp = gson.fromJson(responseBody, GeminiApiResponse::class.java)
                val rawText = geminiResp.candidates
                    ?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: return@withContext Result.failure(Exception("No content in Gemini response"))

                // Strip any accidental markdown fences
                val cleanJson = rawText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                val parsed = gson.fromJson(cleanJson, GeminiJsonResult::class.java)

                Result.success(
                    GeminiFoodResult(
                        name = parsed.name ?: query,
                        weightGrams = parsed.weightGrams ?: 100f,
                        calories = parsed.calories ?: 0f,
                        proteinG = parsed.proteinG ?: 0f,
                        carbsG = parsed.carbsG ?: 0f,
                        fatsG = parsed.fatsG ?: 0f,
                        estimatedPriceINR = parsed.estimatedPriceINR ?: 0f,
                        notes = parsed.notes ?: ""
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ── Internal response shape ───────────────────────────────────────────────
    private data class GeminiApiResponse(val candidates: List<Candidate>?)
    private data class Candidate(val content: Content?)
    private data class Content(val parts: List<Part>?)
    private data class Part(val text: String?)

    private data class GeminiJsonResult(
        val name: String?,
        val weightGrams: Float?,
        val calories: Float?,
        val proteinG: Float?,
        val carbsG: Float?,
        val fatsG: Float?,
        val estimatedPriceINR: Float?,
        val notes: String?
    )
}
