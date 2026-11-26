package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.DailyLog
import com.example.data.model.GoalMode
import com.example.data.model.MealLog
import com.example.data.model.MealSource
import com.example.data.model.MealType
import com.example.data.model.UserProfile
import com.google.firebase.Timestamp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.localNutritionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "khabarcal_local_data"
)

class LocalNutritionStore(private val context: Context) : NutritionStore {

    private val profileKey = stringPreferencesKey("local_profile_json")
    private val mealsKey = stringPreferencesKey("local_meals_json")
    private val dailyLogsKey = stringPreferencesKey("local_daily_logs_json")

    override fun observeUserProfile(userId: String): Flow<UserProfile?> =
        context.localNutritionDataStore.data.map { prefs ->
            val raw = prefs[profileKey]
            if (raw.isNullOrBlank()) null else decodeProfile(raw, userId)
        }

    override fun observeMeals(userId: String): Flow<List<MealLog>> =
        context.localNutritionDataStore.data.map { prefs ->
            val raw = prefs[mealsKey]
            if (raw.isNullOrBlank()) emptyList() else decodeMeals(raw, userId)
        }

    override fun observeDailyLogs(userId: String): Flow<List<DailyLog>> =
        context.localNutritionDataStore.data.map { prefs ->
            val raw = prefs[dailyLogsKey]
            if (raw.isNullOrBlank()) emptyList() else decodeDailyLogs(raw, userId)
        }

    override suspend fun saveOrUpdateProfile(userId: String, profile: UserProfile): Result<Unit> {
        return try {
            val safeProfile = profile.copy(
                userId = userId,
                displayName = profile.displayName.trim().take(80).ifBlank { "KhabarCal" },
                dailyCalorieGoal = profile.dailyCalorieGoal.coerceIn(800, 6000),
                proteinGoalG = profile.proteinGoalG.coerceIn(20, 400),
                carbsGoalG = profile.carbsGoalG.coerceIn(20, 700),
                fatGoalG = profile.fatGoalG.coerceIn(10, 300),
                waterGoalMl = profile.waterGoalMl.coerceIn(500, 6000),
                weightKg = profile.weightKg.coerceIn(20.0, 300.0),
                targetWeightKg = profile.targetWeightKg.coerceIn(20.0, 300.0),
                heightCm = profile.heightCm.coerceIn(80, 250),
                age = profile.age.coerceIn(10, 110),
                goalMode = GoalMode.safeValueOf(profile.goalMode).name,
                preferredLang = if (profile.preferredLang == "en") "en" else "bn"
            )
            context.localNutritionDataStore.edit { prefs ->
                prefs[profileKey] = encodeProfile(safeProfile)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addMeal(userId: String, meal: MealLog): Result<Unit> {
        return try {
            require(DATE_KEY_REGEX.matches(meal.dateKey)) { "Invalid dateKey format." }
            val nowSec = System.currentTimeMillis() / 1000L
            val newMeal = meal.copy(
                id = meal.id.ifBlank { UUID.randomUUID().toString() },
                userId = userId,
                mealType = MealType.safeValueOf(meal.mealType).name,
                dishNameBn = meal.dishNameBn.trim().take(120).ifBlank { "খাবার" },
                dishNameEn = meal.dishNameEn.trim().take(120).ifBlank { "Meal" },
                cuisineType = meal.cuisineType.trim().take(40).ifBlank { "Mixed" },
                calories = meal.calories.coerceIn(0, 10000),
                proteinG = meal.proteinG.coerceIn(0.0, 500.0),
                carbsG = meal.carbsG.coerceIn(0.0, 1000.0),
                fatG = meal.fatG.coerceIn(0.0, 500.0),
                fiberG = meal.fiberG.coerceIn(0.0, 300.0),
                portionMultiplier = meal.portionMultiplier.coerceIn(0.25, 5.0),
                healthScore = meal.healthScore.coerceIn(1, 10),
                source = MealSource.safeValueOf(meal.source).name,
                itemsSummary = meal.itemsSummary.trim().take(400),
                aiTipBn = meal.aiTipBn.trim().take(300),
                aiTipEn = meal.aiTipEn.trim().take(300),
                createdAt = Timestamp(nowSec, 0),
                updatedAt = Timestamp(nowSec, 0)
            )
            context.localNutritionDataStore.edit { prefs ->
                val current = prefs[mealsKey]?.let { decodeMeals(it, userId) }.orEmpty()
                val updated = listOf(newMeal) + current
                prefs[mealsKey] = encodeMeals(updated)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMeal(userId: String, mealId: String): Result<Unit> {
        return try {
            require(mealId.isNotBlank()) { "Meal ID must not be blank." }
            context.localNutritionDataStore.edit { prefs ->
                val current = prefs[mealsKey]?.let { decodeMeals(it, userId) }.orEmpty()
                val updated = current.filterNot { it.id == mealId }
                prefs[mealsKey] = encodeMeals(updated)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun upsertWaterIntake(userId: String, dateKey: String, newWaterMl: Int): Result<Unit> {
        return try {
            require(DATE_KEY_REGEX.matches(dateKey)) { "Invalid dateKey format." }
            val safeWater = newWaterMl.coerceIn(0, 10000)
            val nowSec = System.currentTimeMillis() / 1000L
            context.localNutritionDataStore.edit { prefs ->
                val current = prefs[dailyLogsKey]?.let { decodeDailyLogs(it, userId) }.orEmpty().toMutableList()
                val idx = current.indexOfFirst { it.dateKey == dateKey }
                if (idx >= 0) {
                    current[idx] = current[idx].copy(
                        waterMl = safeWater,
                        updatedAt = Timestamp(nowSec, 0)
                    )
                } else {
                    current.add(
                        DailyLog(
                            id = dateKey,
                            userId = userId,
                            dateKey = dateKey,
                            waterMl = safeWater,
                            coachTip = "",
                            createdAt = Timestamp(nowSec, 0),
                            updatedAt = Timestamp(nowSec, 0)
                        )
                    )
                }
                prefs[dailyLogsKey] = encodeDailyLogs(current)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveCoachTip(userId: String, dateKey: String, coachTip: String): Result<Unit> {
        return try {
            require(DATE_KEY_REGEX.matches(dateKey)) { "Invalid dateKey format." }
            val safeTip = coachTip.trim().take(500)
            val nowSec = System.currentTimeMillis() / 1000L
            context.localNutritionDataStore.edit { prefs ->
                val current = prefs[dailyLogsKey]?.let { decodeDailyLogs(it, userId) }.orEmpty().toMutableList()
                val idx = current.indexOfFirst { it.dateKey == dateKey }
                if (idx >= 0) {
                    current[idx] = current[idx].copy(
                        coachTip = safeTip,
                        updatedAt = Timestamp(nowSec, 0)
                    )
                } else {
                    current.add(
                        DailyLog(
                            id = dateKey,
                            userId = userId,
                            dateKey = dateKey,
                            waterMl = 0,
                            coachTip = safeTip,
                            createdAt = Timestamp(nowSec, 0),
                            updatedAt = Timestamp(nowSec, 0)
                        )
                    )
                }
                prefs[dailyLogsKey] = encodeDailyLogs(current)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun encodeProfile(p: UserProfile): String = JSONObject().apply {
        put("userId", p.userId)
        put("displayName", p.displayName)
        put("dailyCalorieGoal", p.dailyCalorieGoal)
        put("proteinGoalG", p.proteinGoalG)
        put("carbsGoalG", p.carbsGoalG)
        put("fatGoalG", p.fatGoalG)
        put("waterGoalMl", p.waterGoalMl)
        put("weightKg", p.weightKg)
        put("targetWeightKg", p.targetWeightKg)
        put("heightCm", p.heightCm)
        put("age", p.age)
        put("goalMode", p.goalMode)
        put("preferredLang", p.preferredLang)
    }.toString()

    private fun decodeProfile(raw: String, fallbackUid: String): UserProfile? = try {
        val o = JSONObject(raw)
        UserProfile(
            userId = o.optString("userId", fallbackUid),
            displayName = o.optString("displayName", "KhabarCal"),
            dailyCalorieGoal = o.optInt("dailyCalorieGoal", 2000),
            proteinGoalG = o.optInt("proteinGoalG", 100),
            carbsGoalG = o.optInt("carbsGoalG", 240),
            fatGoalG = o.optInt("fatGoalG", 65),
            waterGoalMl = o.optInt("waterGoalMl", 2500),
            weightKg = o.optDouble("weightKg", 65.0),
            targetWeightKg = o.optDouble("targetWeightKg", 62.0),
            heightCm = o.optInt("heightCm", 168),
            age = o.optInt("age", 28),
            goalMode = o.optString("goalMode", GoalMode.MAINTAIN.name),
            preferredLang = o.optString("preferredLang", "bn")
        )
    } catch (_: Exception) {
        null
    }

    private fun encodeMeals(meals: List<MealLog>): String {
        val arr = JSONArray()
        meals.forEach { m ->
            arr.put(JSONObject().apply {
                put("id", m.id)
                put("userId", m.userId)
                put("dateKey", m.dateKey)
                put("mealType", m.mealType)
                put("dishNameBn", m.dishNameBn)
                put("dishNameEn", m.dishNameEn)
                put("cuisineType", m.cuisineType)
                put("calories", m.calories)
                put("proteinG", m.proteinG)
                put("carbsG", m.carbsG)
                put("fatG", m.fatG)
                put("fiberG", m.fiberG)
                put("portionMultiplier", m.portionMultiplier)
                put("healthScore", m.healthScore)
                put("source", m.source)
                put("itemsSummary", m.itemsSummary)
                put("aiTipBn", m.aiTipBn)
                put("aiTipEn", m.aiTipEn)
                put("createdAtSec", m.createdAt?.seconds ?: 0L)
            })
        }
        return arr.toString()
    }

    private fun decodeMeals(raw: String, fallbackUid: String): List<MealLog> = try {
        val arr = JSONArray(raw)
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val sec = o.optLong("createdAtSec", 0L)
                add(
                    MealLog(
                        id = o.optString("id"),
                        userId = o.optString("userId", fallbackUid),
                        dateKey = o.optString("dateKey"),
                        mealType = o.optString("mealType", MealType.LUNCH.name),
                        dishNameBn = o.optString("dishNameBn"),
                        dishNameEn = o.optString("dishNameEn"),
                        cuisineType = o.optString("cuisineType", "Bangladeshi"),
                        calories = o.optInt("calories", 0),
                        proteinG = o.optDouble("proteinG", 0.0),
                        carbsG = o.optDouble("carbsG", 0.0),
                        fatG = o.optDouble("fatG", 0.0),
                        fiberG = o.optDouble("fiberG", 0.0),
                        portionMultiplier = o.optDouble("portionMultiplier", 1.0),
                        healthScore = o.optInt("healthScore", 7),
                        source = o.optString("source", MealSource.PHOTO_AI.name),
                        itemsSummary = o.optString("itemsSummary", ""),
                        aiTipBn = o.optString("aiTipBn", ""),
                        aiTipEn = o.optString("aiTipEn", ""),
                        createdAt = if (sec > 0L) Timestamp(sec, 0) else null
                    )
                )
            }
        }
    } catch (_: Exception) {
        emptyList()
    }

    private fun encodeDailyLogs(logs: List<DailyLog>): String {
        val arr = JSONArray()
        logs.forEach { d ->
            arr.put(JSONObject().apply {
                put("id", d.id)
                put("userId", d.userId)
                put("dateKey", d.dateKey)
                put("waterMl", d.waterMl)
                put("coachTip", d.coachTip)
            })
        }
        return arr.toString()
    }

    private fun decodeDailyLogs(raw: String, fallbackUid: String): List<DailyLog> = try {
        val arr = JSONArray(raw)
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    DailyLog(
                        id = o.optString("id", o.optString("dateKey")),
                        userId = o.optString("userId", fallbackUid),
                        dateKey = o.optString("dateKey"),
                        waterMl = o.optInt("waterMl", 0),
                        coachTip = o.optString("coachTip", "")
                    )
                )
            }
        }
    } catch (_: Exception) {
        emptyList()
    }

    private companion object {
        val DATE_KEY_REGEX = Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$")
    }
}
