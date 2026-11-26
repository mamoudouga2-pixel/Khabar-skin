package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
enum class GoalMode {
    LOSE, MAINTAIN, GAIN;

    companion object {
        fun safeValueOf(raw: String?): GoalMode =
            entries.firstOrNull { it.name == raw } ?: MAINTAIN
    }
}

@Keep
enum class MealType {
    BREAKFAST, LUNCH, DINNER, SNACK;

    companion object {
        fun safeValueOf(raw: String?): MealType =
            entries.firstOrNull { it.name == raw } ?: LUNCH
    }
}

@Keep
enum class MealSource {
    PHOTO_AI, TEXT_AI, QUICK_DB, MANUAL;

    companion object {
        fun safeValueOf(raw: String?): MealSource =
            entries.firstOrNull { it.name == raw } ?: MANUAL
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    val userId: String = "",
    val displayName: String = "User",
    val dailyCalorieGoal: Int = 2000,
    val proteinGoalG: Int = 100,
    val carbsGoalG: Int = 240,
    val fatGoalG: Int = 65,
    val waterGoalMl: Int = 2500,
    val weightKg: Double = 65.0,
    val targetWeightKg: Double = 62.0,
    val heightCm: Int = 168,
    val age: Int = 28,
    val goalMode: String = GoalMode.MAINTAIN.name,
    val preferredLang: String = "bn",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class MealLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateKey: String = "",
    val mealType: String = MealType.LUNCH.name,
    val dishNameBn: String = "",
    val dishNameEn: String = "",
    val cuisineType: String = "Bangladeshi",
    val calories: Int = 0,
    val proteinG: Double = 0.0,
    val carbsG: Double = 0.0,
    val fatG: Double = 0.0,
    val fiberG: Double = 0.0,
    val portionMultiplier: Double = 1.0,
    val healthScore: Int = 7,
    val source: String = MealSource.PHOTO_AI.name,
    val itemsSummary: String = "",
    val aiTipBn: String = "",
    val aiTipEn: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DailyLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateKey: String = "",
    val waterMl: Int = 0,
    val coachTip: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class DetectedFoodItem(
    val nameBn: String,
    val nameEn: String,
    val portionText: String,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double
)

data class FoodAnalysisResult(
    val foodDetected: Boolean,
    val dishNameBn: String,
    val dishNameEn: String,
    val cuisineType: String,
    val confidenceNote: String,
    val healthScore: Int,
    val totalCalories: Int,
    val totalProteinG: Double,
    val totalCarbsG: Double,
    val totalFatG: Double,
    val totalFiberG: Double,
    val items: List<DetectedFoodItem>,
    val tipBn: String,
    val tipEn: String,
    val source: MealSource
)

enum class FoodCategory {
    ALL, BANGLADESHI, FAST_FOOD, SNACKS_SWEETS, DRINKS_FRUITS
}

data class CuratedFoodItem(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val servingBn: String,
    val servingEn: String,
    val cuisineType: String,
    val category: FoodCategory,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fiberG: Double,
    val healthScore: Int
)
