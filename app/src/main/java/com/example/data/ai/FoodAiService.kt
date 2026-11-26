package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.core.AppLang
import com.example.data.model.DetectedFoodItem
import com.example.data.model.FoodAnalysisResult
import com.example.data.model.MealSource
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.roundToInt

private const val TAG = "FoodAiService"

class FoodAiService {

    private val detectedItemSchema = Schema.obj(
        properties = mapOf(
            "nameBn" to Schema.string(description = "Item name in natural Bengali script"),
            "nameEn" to Schema.string(description = "Item name in English"),
            "portionText" to Schema.string(description = "Estimated portion size, e.g. 1.5 cups or 1 piece (120g)"),
            "calories" to Schema.integer(description = "Estimated calories in kcal"),
            "proteinG" to Schema.double(description = "Protein in grams"),
            "carbsG" to Schema.double(description = "Carbohydrates in grams"),
            "fatG" to Schema.double(description = "Fat in grams")
        )
    )

    private val foodAnalysisSchema = Schema.obj(
        properties = mapOf(
            "foodDetected" to Schema.boolean(description = "True if food or beverage is present, false otherwise"),
            "dishNameBn" to Schema.string(description = "Overall meal title in Bengali"),
            "dishNameEn" to Schema.string(description = "Overall meal title in English"),
            "cuisineType" to Schema.string(description = "Cuisine such as Bangladeshi, South Asian, Fast Food, Asian, International, Beverage"),
            "confidenceNote" to Schema.string(description = "Brief note on visual portion estimation"),
            "healthScore" to Schema.integer(description = "Nutrition balance score from 1 (unhealthy) to 10 (very balanced)"),
            "totalCalories" to Schema.integer(description = "Total estimated calories in kcal"),
            "totalProteinG" to Schema.double(description = "Total protein grams"),
            "totalCarbsG" to Schema.double(description = "Total carbs grams"),
            "totalFatG" to Schema.double(description = "Total fat grams"),
            "totalFiberG" to Schema.double(description = "Total fiber grams"),
            "items" to Schema.array(
                items = detectedItemSchema,
                description = "Individual food items visible on the plate or described in text"
            ),
            "tipBn" to Schema.string(description = "Short, actionable nutrition tip in Bengali (max 140 chars)"),
            "tipEn" to Schema.string(description = "Short, actionable nutrition tip in English (max 140 chars)")
        )
    )

    private val structuredModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = foodAnalysisSchema
            }
        )
    }

    private val coachModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    suspend fun analyzeFoodPhoto(bitmap: Bitmap, lang: AppLang): FoodAnalysisResult {
        val scaledBitmap = downscaleBitmap(bitmap, maxDimension = 1024)
        val promptText = """
            You are an expert clinical nutritionist specializing in Bangladeshi (Deshi), South Asian, and global cuisines.
            Analyze the food in this image carefully:
            - Identify every individual dish/item on the plate (e.g., steamed rice, lentil dal, hilsa/rui fish curry, alu bhorta, kacchi biryani, khichuri, paratha, fuchka, burger, pizza, salad, drinks).
            - Estimate realistic serving portions based on plate cues and standard Bangladeshi/international restaurant or home portions.
            - Provide accurate calories, protein, carbs, fat, and fiber totals matching the sum of detected items.
            - Give a balanced healthScore (1..10) and short, helpful tips in both Bengali and English.
            - If the image clearly contains NO food or drink, set foodDetected=false and totals to 0.
            Primary user UI language: ${if (lang == AppLang.BN) "Bengali" else "English"}.
        """.trimIndent()

        val response = withTimeout(60_000L) {
            structuredModel.generateContent(
                content {
                    image(scaledBitmap)
                    text(promptText)
                }
            )
        }
        val rawJson = response.text
            ?: throw IllegalStateException("AI returned an empty response.")
        return parseAnalysisJson(rawJson, MealSource.PHOTO_AI)
    }

    suspend fun analyzeFoodText(description: String, lang: AppLang): FoodAnalysisResult {
        val promptText = """
            You are an expert clinical nutritionist specializing in Bangladeshi (Deshi), South Asian, and global cuisines.
            Parse this meal description (written in Bengali, Banglish, or English): "$description"
            Break it down into individual items with realistic portions, calories, protein, carbs, fat, and fiber.
            If the input is not food or meaningless, set foodDetected=false.
            Provide dish titles and tips in both Bengali and English.
            Primary user UI language: ${if (lang == AppLang.BN) "Bengali" else "English"}.
        """.trimIndent()

        val response = withTimeout(60_000L) {
            structuredModel.generateContent(promptText)
        }
        val rawJson = response.text
            ?: throw IllegalStateException("AI returned an empty response.")
        return parseAnalysisJson(rawJson, MealSource.TEXT_AI)
    }

    suspend fun generateDailyCoachTip(
        mealsSummary: String,
        eatenCalories: Int,
        goalCalories: Int,
        proteinG: Int,
        proteinGoalG: Int,
        waterMl: Int,
        lang: AppLang
    ): String {
        val languageInstruction = if (lang == AppLang.BN) {
            "Reply ONLY in warm, practical Bengali (2 short sentences, under 280 characters)."
        } else {
            "Reply ONLY in warm, practical English (2 short sentences, under 280 characters)."
        }
        val prompt = """
            You are KhabarCal's daily nutrition coach for Bangladeshi and international diets.
            User's progress today:
            - Calories eaten: $eatenCalories / $goalCalories kcal
            - Protein: $proteinG / $proteinGoalG g
            - Water: $waterMl ml
            - Meals logged today: ${mealsSummary.ifBlank { "None logged yet today" }}
            Give one specific, practical suggestion for their next meal or hydration (e.g., suggesting affordable everyday foods like eggs, dal, fish, salad, roti, fruit).
            $languageInstruction
        """.trimIndent()

        val response = withTimeout(45_000L) {
            coachModel.generateContent(prompt)
        }
        val text = response.text?.trim()
        if (text.isNullOrBlank()) {
            throw IllegalStateException("Coach returned empty advice.")
        }
        return text.take(480)
    }

    private fun parseAnalysisJson(jsonString: String, source: MealSource): FoodAnalysisResult {
        return try {
            val json = JSONObject(jsonString)
            val foodDetected = json.optBoolean("foodDetected", true)
            val dishNameBn = json.optString("dishNameBn", "শনাক্তকৃত খাবার").ifBlank { "শনাক্তকৃত খাবার" }
            val dishNameEn = json.optString("dishNameEn", "Detected Meal").ifBlank { "Detected Meal" }
            val cuisineType = json.optString("cuisineType", "Bangladeshi").ifBlank { "Bangladeshi" }
            val confidenceNote = json.optString("confidenceNote", "")
            val healthScore = json.optInt("healthScore", 7).coerceIn(1, 10)
            val totalCalories = json.optInt("totalCalories", 0).coerceIn(0, 10000)
            val totalProteinG = json.optDouble("totalProteinG", 0.0).coerceIn(0.0, 500.0)
            val totalCarbsG = json.optDouble("totalCarbsG", 0.0).coerceIn(0.0, 1000.0)
            val totalFatG = json.optDouble("totalFatG", 0.0).coerceIn(0.0, 500.0)
            val totalFiberG = json.optDouble("totalFiberG", 0.0).coerceIn(0.0, 300.0)
            val tipBn = json.optString("tipBn", "সুষম খাবারের সাথে পর্যাপ্ত পানি পান করুন।")
            val tipEn = json.optString("tipEn", "Pair your meal with fresh greens and water.")

            val itemsArray = json.optJSONArray("items")
            val items = buildList {
                if (itemsArray != null) {
                    for (i in 0 until itemsArray.length()) {
                        val obj = itemsArray.optJSONObject(i) ?: continue
                        add(
                            DetectedFoodItem(
                                nameBn = obj.optString("nameBn", dishNameBn),
                                nameEn = obj.optString("nameEn", dishNameEn),
                                portionText = obj.optString("portionText", "1 serving"),
                                calories = obj.optInt("calories", 0).coerceAtLeast(0),
                                proteinG = obj.optDouble("proteinG", 0.0).coerceAtLeast(0.0),
                                carbsG = obj.optDouble("carbsG", 0.0).coerceAtLeast(0.0),
                                fatG = obj.optDouble("fatG", 0.0).coerceAtLeast(0.0)
                            )
                        )
                    }
                }
            }

            FoodAnalysisResult(
                foodDetected = foodDetected,
                dishNameBn = dishNameBn,
                dishNameEn = dishNameEn,
                cuisineType = cuisineType,
                confidenceNote = confidenceNote,
                healthScore = healthScore,
                totalCalories = totalCalories,
                totalProteinG = totalProteinG,
                totalCarbsG = totalCarbsG,
                totalFatG = totalFatG,
                totalFiberG = totalFiberG,
                items = items,
                tipBn = tipBn,
                tipEn = tipEn,
                source = source
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse AI JSON response: $jsonString", e)
            throw IllegalStateException("Could not parse AI nutrition breakdown.", e)
        }
    }

    private fun downscaleBitmap(source: Bitmap, maxDimension: Int): Bitmap {
        val maxSide = max(source.width, source.height)
        if (maxSide <= maxDimension) return source
        val scale = maxDimension.toFloat() / maxSide.toFloat()
        val newW = (source.width * scale).roundToInt().coerceAtLeast(1)
        val newH = (source.height * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, newW, newH, true)
    }
}
