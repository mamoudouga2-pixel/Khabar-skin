package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.DailyLog
import com.example.data.model.GoalMode
import com.example.data.model.MealLog
import com.example.data.model.MealSource
import com.example.data.model.MealType
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NutritionRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) : NutritionStore {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(expectedUserId: String? = null): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        if (expectedUserId != null) {
            check(uid == expectedUserId) { "Signed-in user changed; refusing cross-user operation." }
        }
        return uid
    }

    override fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    override fun observeMeals(userId: String): Flow<List<MealLog>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/meals"
        emitAll(
            db.collection("users").document(uid).collection("meals")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        MealLog::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    override fun observeDailyLogs(userId: String): Flow<List<DailyLog>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/daily_logs"
        emitAll(
            db.collection("users").document(uid).collection("daily_logs")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        DailyLog::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    override suspend fun saveOrUpdateProfile(userId: String, profile: UserProfile): Result<Unit> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val docRef = db.collection("users").document(uid)
            val existingSnap = docRef.get().await()

            val safeName = profile.displayName.trim().take(80).ifBlank { "KhabarCal User" }
            val safeCal = profile.dailyCalorieGoal.coerceIn(800, 6000)
            val safeProtein = profile.proteinGoalG.coerceIn(20, 400)
            val safeCarbs = profile.carbsGoalG.coerceIn(20, 700)
            val safeFat = profile.fatGoalG.coerceIn(10, 300)
            val safeWater = profile.waterGoalMl.coerceIn(500, 6000)
            val safeWeight = profile.weightKg.coerceIn(20.0, 300.0)
            val safeTargetWeight = profile.targetWeightKg.coerceIn(20.0, 300.0)
            val safeHeight = profile.heightCm.coerceIn(80, 250)
            val safeAge = profile.age.coerceIn(10, 110)
            val safeGoalMode = GoalMode.safeValueOf(profile.goalMode).name
            val safeLang = if (profile.preferredLang == "en") "en" else "bn"

            if (existingSnap.exists()) {
                val updatePayload = mapOf(
                    "displayName" to safeName,
                    "dailyCalorieGoal" to safeCal,
                    "proteinGoalG" to safeProtein,
                    "carbsGoalG" to safeCarbs,
                    "fatGoalG" to safeFat,
                    "waterGoalMl" to safeWater,
                    "weightKg" to safeWeight,
                    "targetWeightKg" to safeTargetWeight,
                    "heightCm" to safeHeight,
                    "age" to safeAge,
                    "goalMode" to safeGoalMode,
                    "preferredLang" to safeLang,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            } else {
                val createPayload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "dailyCalorieGoal" to safeCal,
                    "proteinGoalG" to safeProtein,
                    "carbsGoalG" to safeCarbs,
                    "fatGoalG" to safeFat,
                    "waterGoalMl" to safeWater,
                    "weightKg" to safeWeight,
                    "targetWeightKg" to safeTargetWeight,
                    "heightCm" to safeHeight,
                    "age" to safeAge,
                    "goalMode" to safeGoalMode,
                    "preferredLang" to safeLang,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun addMeal(userId: String, meal: MealLog): Result<Unit> {
        var docPath = "users/$userId/meals"
        return try {
            val uid = requireUserId(userId)
            require(DATE_KEY_REGEX.matches(meal.dateKey)) { "Invalid dateKey format." }
            val docRef = db.collection("users").document(uid).collection("meals").document()
            docPath = docRef.path

            val payload = mapOf(
                "userId" to uid,
                "dateKey" to meal.dateKey,
                "mealType" to MealType.safeValueOf(meal.mealType).name,
                "dishNameBn" to meal.dishNameBn.trim().take(120).ifBlank { "খাবার" },
                "dishNameEn" to meal.dishNameEn.trim().take(120).ifBlank { "Meal" },
                "cuisineType" to meal.cuisineType.trim().take(40).ifBlank { "Mixed" },
                "calories" to meal.calories.coerceIn(0, 10000),
                "proteinG" to meal.proteinG.coerceIn(0.0, 500.0),
                "carbsG" to meal.carbsG.coerceIn(0.0, 1000.0),
                "fatG" to meal.fatG.coerceIn(0.0, 500.0),
                "fiberG" to meal.fiberG.coerceIn(0.0, 300.0),
                "portionMultiplier" to meal.portionMultiplier.coerceIn(0.25, 5.0),
                "healthScore" to meal.healthScore.coerceIn(1, 10),
                "source" to MealSource.safeValueOf(meal.source).name,
                "itemsSummary" to meal.itemsSummary.trim().take(400),
                "aiTipBn" to meal.aiTipBn.trim().take(300),
                "aiTipEn" to meal.aiTipEn.trim().take(300),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun deleteMeal(userId: String, mealId: String): Result<Unit> {
        val docPath = "users/$userId/meals/$mealId"
        return try {
            val uid = requireUserId(userId)
            require(mealId.isNotBlank()) { "Meal ID must not be blank." }
            db.collection("users").document(uid).collection("meals").document(mealId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun upsertWaterIntake(userId: String, dateKey: String, newWaterMl: Int): Result<Unit> {
        val docPath = "users/$userId/daily_logs/$dateKey"
        return try {
            val uid = requireUserId(userId)
            require(DATE_KEY_REGEX.matches(dateKey)) { "Invalid dateKey format." }
            val safeWater = newWaterMl.coerceIn(0, 10000)
            val docRef = db.collection("users").document(uid)
                .collection("daily_logs").document(dateKey)
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                docRef.update(
                    mapOf(
                        "waterMl" to safeWater,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "dateKey" to dateKey,
                        "waterMl" to safeWater,
                        "coachTip" to "",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun saveCoachTip(userId: String, dateKey: String, coachTip: String): Result<Unit> {
        val docPath = "users/$userId/daily_logs/$dateKey"
        return try {
            val uid = requireUserId(userId)
            require(DATE_KEY_REGEX.matches(dateKey)) { "Invalid dateKey format." }
            val safeTip = coachTip.trim().take(500)
            val docRef = db.collection("users").document(uid)
                .collection("daily_logs").document(dateKey)
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                docRef.update(
                    mapOf(
                        "coachTip" to safeTip,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "dateKey" to dateKey,
                        "waterMl" to 0,
                        "coachTip" to safeTip,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    private companion object {
        val DATE_KEY_REGEX = Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$")
    }
}
