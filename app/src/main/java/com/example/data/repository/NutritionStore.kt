package com.example.data.repository

import com.example.data.model.DailyLog
import com.example.data.model.MealLog
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface NutritionStore {
    fun observeUserProfile(userId: String): Flow<UserProfile?>
    fun observeMeals(userId: String): Flow<List<MealLog>>
    fun observeDailyLogs(userId: String): Flow<List<DailyLog>>
    suspend fun saveOrUpdateProfile(userId: String, profile: UserProfile): Result<Unit>
    suspend fun addMeal(userId: String, meal: MealLog): Result<Unit>
    suspend fun deleteMeal(userId: String, mealId: String): Result<Unit>
    suspend fun upsertWaterIntake(userId: String, dateKey: String, newWaterMl: Int): Result<Unit>
    suspend fun saveCoachTip(userId: String, dateKey: String, coachTip: String): Result<Unit>
}
