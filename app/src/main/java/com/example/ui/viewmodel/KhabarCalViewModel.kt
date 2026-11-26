package com.example.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AppLang
import com.example.data.ai.FoodAiService
import com.example.data.model.CuratedFoodItem
import com.example.data.model.DailyLog
import com.example.data.model.FoodAnalysisResult
import com.example.data.model.GoalMode
import com.example.data.model.MealLog
import com.example.data.model.MealSource
import com.example.data.model.MealType
import com.example.data.model.UserProfile
import com.example.data.preferences.UserPrefsRepository
import com.example.data.repository.NutritionStore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private const val TAG = "KhabarCalVM"

sealed interface DataUiState<out T> {
    data object Loading : DataUiState<Nothing>
    data class Success<T>(val data: T) : DataUiState<T>
    data class Error(val message: String) : DataUiState<Nothing>
}

enum class OilAdjustment(val factor: Double) {
    LIGHT(0.85),
    NORMAL(1.0),
    RICH(1.20)
}

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data object Analyzing : ScanUiState
    data class Preview(
        val analysis: FoodAnalysisResult,
        val photoBitmap: Bitmap?,
        val selectedMealType: MealType,
        val portionMultiplier: Double,
        val oilAdjustment: OilAdjustment,
        val excludedItemIndices: Set<Int> = emptySet(),
        val isSaving: Boolean = false
    ) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

data class DailyTrendPoint(
    val dateKey: String,
    val shortLabel: String,
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int
)

data class DashboardSnapshot(
    val selectedDateKey: String = todayDateKey(),
    val profile: UserProfile = UserProfile(),
    val allMeals: List<MealLog> = emptyList(),
    val selectedDayMeals: List<MealLog> = emptyList(),
    val selectedDayWaterMl: Int = 0,
    val selectedDayCoachTip: String = "",
    val totalCalories: Int = 0,
    val totalProteinG: Int = 0,
    val totalCarbsG: Int = 0,
    val totalFatG: Int = 0,
    val totalFiberG: Int = 0,
    val currentStreakDays: Int = 0,
    val weeklyTrend: List<DailyTrendPoint> = emptyList(),
    val isSyncError: String? = null
)

class KhabarCalViewModel(
    private val repository: NutritionStore,
    private val aiService: FoodAiService,
    private val prefsRepo: UserPrefsRepository,
    private val currentUserId: String,
    private val authDisplayName: String
) : ViewModel() {

    private val _selectedDateKey = MutableStateFlow(todayDateKey())
    val selectedDateKey: StateFlow<String> = _selectedDateKey.asStateFlow()

    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    private val _coachLoading = MutableStateFlow(false)
    val coachLoading: StateFlow<Boolean> = _coachLoading.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    val preferredLang: StateFlow<AppLang> = prefsRepo.preferredLang
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = AppLang.BN
        )

    private val profileFlow: StateFlow<DataUiState<UserProfile?>> =
        repository.observeUserProfile(currentUserId)
            .map<UserProfile?, DataUiState<UserProfile?>> { DataUiState.Success(it) }
            .catch { error ->
                val firestoreError = unwrapFirestoreError(error)
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Error observing user profile", root)
                emit(DataUiState.Error(root.localizedMessage ?: "Failed to load profile"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = DataUiState.Loading
            )

    private val mealsFlow: StateFlow<DataUiState<List<MealLog>>> =
        repository.observeMeals(currentUserId)
            .map<List<MealLog>, DataUiState<List<MealLog>>> { DataUiState.Success(it) }
            .catch { error ->
                val firestoreError = unwrapFirestoreError(error)
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Error observing meals", root)
                emit(DataUiState.Error(root.localizedMessage ?: "Failed to load meals"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = DataUiState.Loading
            )

    private val dailyLogsFlow: StateFlow<DataUiState<List<DailyLog>>> =
        repository.observeDailyLogs(currentUserId)
            .map<List<DailyLog>, DataUiState<List<DailyLog>>> { DataUiState.Success(it) }
            .catch { error ->
                val firestoreError = unwrapFirestoreError(error)
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Error observing daily logs", root)
                emit(DataUiState.Error(root.localizedMessage ?: "Failed to load daily logs"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = DataUiState.Loading
            )

    val dashboardState: StateFlow<DashboardSnapshot> = combine(
        _selectedDateKey,
        profileFlow,
        mealsFlow,
        dailyLogsFlow
    ) { dateKey, profState, mealsState, logsState ->
        val defaultProfile = UserProfile(
            userId = currentUserId,
            displayName = authDisplayName.ifBlank { "KhabarCal User" }
        )
        val profile = (profState as? DataUiState.Success)?.data ?: defaultProfile
        val meals = (mealsState as? DataUiState.Success)?.data ?: emptyList()
        val logs = (logsState as? DataUiState.Success)?.data ?: emptyList()

        val dayMeals = meals
            .filter { it.dateKey == dateKey }
            .sortedByDescending { it.createdAt?.seconds ?: 0L }

        val dayLog = logs.firstOrNull { it.dateKey == dateKey }

        val errorMsg = listOfNotNull(
            (profState as? DataUiState.Error)?.message,
            (mealsState as? DataUiState.Error)?.message,
            (logsState as? DataUiState.Error)?.message
        ).firstOrNull()

        DashboardSnapshot(
            selectedDateKey = dateKey,
            profile = profile,
            allMeals = meals,
            selectedDayMeals = dayMeals,
            selectedDayWaterMl = dayLog?.waterMl ?: 0,
            selectedDayCoachTip = dayLog?.coachTip.orEmpty(),
            totalCalories = dayMeals.sumOf { it.calories },
            totalProteinG = dayMeals.sumOf { it.proteinG }.roundToInt(),
            totalCarbsG = dayMeals.sumOf { it.carbsG }.roundToInt(),
            totalFatG = dayMeals.sumOf { it.fatG }.roundToInt(),
            totalFiberG = dayMeals.sumOf { it.fiberG }.roundToInt(),
            currentStreakDays = computeStreakDays(meals),
            weeklyTrend = buildWeeklyTrend(meals),
            isSyncError = errorMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = DashboardSnapshot(
            profile = UserProfile(
                userId = currentUserId,
                displayName = authDisplayName.ifBlank { "KhabarCal User" }
            )
        )
    )

    init {
        // Ensure initial default profile exists for brand new users
        viewModelScope.launch {
            profileFlow.collect { state ->
                if (state is DataUiState.Success && state.data == null) {
                    repository.saveOrUpdateProfile(
                        currentUserId,
                        UserProfile(
                            userId = currentUserId,
                            displayName = authDisplayName.ifBlank { "KhabarCal User" },
                            preferredLang = preferredLang.value.code
                        )
                    )
                }
            }
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val next = if (preferredLang.value == AppLang.BN) AppLang.EN else AppLang.BN
            prefsRepo.setPreferredLang(next)
            val currentProf = dashboardState.value.profile
            repository.saveOrUpdateProfile(
                currentUserId,
                currentProf.copy(preferredLang = next.code)
            )
        }
    }

    fun shiftSelectedDate(deltaDays: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        try {
            cal.time = sdf.parse(_selectedDateKey.value) ?: Date()
        } catch (_: Exception) {
            cal.time = Date()
        }
        cal.add(Calendar.DAY_OF_YEAR, deltaDays)
        val candidate = sdf.format(cal.time)
        val today = todayDateKey()
        _selectedDateKey.value = if (candidate > today) today else candidate
    }

    fun selectToday() {
        _selectedDateKey.value = todayDateKey()
    }

    fun analyzePhotoMeal(bitmap: Bitmap, preferredMealType: MealType? = null) {
        viewModelScope.launch {
            _scanState.value = ScanUiState.Analyzing
            try {
                val result = aiService.analyzeFoodPhoto(bitmap, preferredLang.value)
                _scanState.value = ScanUiState.Preview(
                    analysis = result,
                    photoBitmap = bitmap,
                    selectedMealType = preferredMealType ?: guessDefaultMealType(),
                    portionMultiplier = 1.0,
                    oilAdjustment = OilAdjustment.NORMAL
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Photo analysis timed out", e)
                _scanState.value = ScanUiState.Error(
                    if (preferredLang.value == AppLang.BN) {
                        "সময় বেশি লেগেছে। অনুগ্রহ করে আবার চেষ্টা করুন।"
                    } else {
                        "Analysis timed out after 60 seconds. Please try again."
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Photo analysis failed", e)
                _scanState.value = ScanUiState.Error(
                    e.localizedMessage ?: "Failed to analyze photo."
                )
            }
        }
    }

    fun analyzeTextMeal(description: String, preferredMealType: MealType? = null) {
        if (description.isBlank()) return
        viewModelScope.launch {
            _scanState.value = ScanUiState.Analyzing
            try {
                val result = aiService.analyzeFoodText(description.trim(), preferredLang.value)
                _scanState.value = ScanUiState.Preview(
                    analysis = result,
                    photoBitmap = null,
                    selectedMealType = preferredMealType ?: guessDefaultMealType(),
                    portionMultiplier = 1.0,
                    oilAdjustment = OilAdjustment.NORMAL
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Text analysis timed out", e)
                _scanState.value = ScanUiState.Error(
                    if (preferredLang.value == AppLang.BN) {
                        "সময় বেশি লেগেছে। আবার চেষ্টা করুন।"
                    } else {
                        "Analysis timed out. Please try again."
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Text analysis failed", e)
                _scanState.value = ScanUiState.Error(
                    e.localizedMessage ?: "Failed to analyze meal description."
                )
            }
        }
    }

    fun openQuickCatalogPreview(item: CuratedFoodItem, preferredMealType: MealType? = null) {
        val analysis = FoodAnalysisResult(
            foodDetected = true,
            dishNameBn = item.nameBn,
            dishNameEn = item.nameEn,
            cuisineType = item.cuisineType,
            confidenceNote = "${item.servingBn} / ${item.servingEn}",
            healthScore = item.healthScore,
            totalCalories = item.calories,
            totalProteinG = item.proteinG,
            totalCarbsG = item.carbsG,
            totalFatG = item.fatG,
            totalFiberG = item.fiberG,
            items = emptyList(),
            tipBn = "সুষম খাবারের জন্য সাথে তাজা শাকসবজি বা সালাদ যোগ করুন।",
            tipEn = "Pair with fresh greens or salad for extra micronutrients.",
            source = MealSource.QUICK_DB
        )
        _scanState.value = ScanUiState.Preview(
            analysis = analysis,
            photoBitmap = null,
            selectedMealType = preferredMealType ?: guessDefaultMealType(),
            portionMultiplier = 1.0,
            oilAdjustment = OilAdjustment.NORMAL
        )
    }

    fun updatePreviewMealType(mealType: MealType) {
        val current = _scanState.value as? ScanUiState.Preview ?: return
        _scanState.value = current.copy(selectedMealType = mealType)
    }

    fun updatePreviewPortion(multiplier: Double) {
        val current = _scanState.value as? ScanUiState.Preview ?: return
        _scanState.value = current.copy(portionMultiplier = multiplier)
    }

    fun updatePreviewOil(oilAdjustment: OilAdjustment) {
        val current = _scanState.value as? ScanUiState.Preview ?: return
        _scanState.value = current.copy(oilAdjustment = oilAdjustment)
    }

    fun togglePreviewItem(index: Int) {
        val current = _scanState.value as? ScanUiState.Preview ?: return
        val next = if (index in current.excludedItemIndices) {
            current.excludedItemIndices - index
        } else {
            current.excludedItemIndices + index
        }
        _scanState.value = current.copy(excludedItemIndices = next)
    }

    fun dismissScanSheet() {
        _scanState.value = ScanUiState.Idle
    }

    fun savePreviewMeal() {
        val preview = _scanState.value as? ScanUiState.Preview ?: return
        if (!preview.analysis.foodDetected) {
            _scanState.value = ScanUiState.Idle
            return
        }
        viewModelScope.launch {
            _scanState.value = preview.copy(isSaving = true)
            val a = preview.analysis
            val activeItems = a.items.filterIndexed { idx, _ -> idx !in preview.excludedItemIndices }
            val useItemsSum = a.items.isNotEmpty()
            val baseCalories = if (useItemsSum) activeItems.sumOf { it.calories } else a.totalCalories
            val baseProtein = if (useItemsSum) activeItems.sumOf { it.proteinG } else a.totalProteinG
            val baseCarbs = if (useItemsSum) activeItems.sumOf { it.carbsG } else a.totalCarbsG
            val baseFat = if (useItemsSum) activeItems.sumOf { it.fatG } else a.totalFatG

            val factor = preview.portionMultiplier * preview.oilAdjustment.factor
            val summary = activeItems.joinToString(" • ") { item ->
                val scaledKcal = (item.calories * factor).roundToInt()
                val label = if (preferredLang.value == AppLang.BN) item.nameBn else item.nameEn
                "$label (${scaledKcal} kcal)"
            }.take(400)

            val meal = MealLog(
                userId = currentUserId,
                dateKey = _selectedDateKey.value,
                mealType = preview.selectedMealType.name,
                dishNameBn = a.dishNameBn,
                dishNameEn = a.dishNameEn,
                cuisineType = a.cuisineType,
                calories = (baseCalories * factor).roundToInt(),
                proteinG = (baseProtein * preview.portionMultiplier),
                carbsG = (baseCarbs * preview.portionMultiplier),
                fatG = (baseFat * factor),
                fiberG = (a.totalFiberG * preview.portionMultiplier),
                portionMultiplier = preview.portionMultiplier,
                healthScore = a.healthScore,
                source = a.source.name,
                itemsSummary = summary,
                aiTipBn = a.tipBn,
                aiTipEn = a.tipEn
            )
            val result = repository.addMeal(currentUserId, meal)
            if (result.isSuccess) {
                _scanState.value = ScanUiState.Idle
                _actionMessage.value = if (preferredLang.value == AppLang.BN) {
                    "খাবার সফলভাবে যোগ হয়েছে!"
                } else {
                    "Meal logged!"
                }
            } else {
                _scanState.value = ScanUiState.Error(
                    result.exceptionOrNull()?.localizedMessage ?: "Failed to save meal."
                )
            }
        }
    }

    fun deleteMeal(mealId: String) {
        viewModelScope.launch {
            val result = repository.deleteMeal(currentUserId, mealId)
            if (result.isFailure) {
                _actionMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to delete meal."
            }
        }
    }

    fun adjustWater(deltaMl: Int) {
        viewModelScope.launch {
            val currentWater = dashboardState.value.selectedDayWaterMl
            val updated = (currentWater + deltaMl).coerceIn(0, 10000)
            val res = repository.upsertWaterIntake(currentUserId, _selectedDateKey.value, updated)
            if (res.isFailure) {
                _actionMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to update water."
            }
        }
    }

    fun refreshDailyCoachTip() {
        val snap = dashboardState.value
        if (_coachLoading.value) return
        viewModelScope.launch {
            _coachLoading.value = true
            try {
                val mealsSummary = snap.selectedDayMeals.joinToString(", ") {
                    "${it.dishNameEn} (${it.calories} kcal)"
                }
                val tip = aiService.generateDailyCoachTip(
                    mealsSummary = mealsSummary,
                    eatenCalories = snap.totalCalories,
                    goalCalories = snap.profile.dailyCalorieGoal,
                    proteinG = snap.totalProteinG,
                    proteinGoalG = snap.profile.proteinGoalG,
                    waterMl = snap.selectedDayWaterMl,
                    lang = preferredLang.value
                )
                repository.saveCoachTip(currentUserId, snap.selectedDateKey, tip)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Coach tip generation failed", e)
                _actionMessage.value = e.localizedMessage ?: "Could not load AI Coach tip."
            } finally {
                _coachLoading.value = false
            }
        }
    }

    fun saveGoals(
        weightKg: Double,
        targetWeightKg: Double,
        heightCm: Int,
        age: Int,
        goalMode: GoalMode,
        dailyCalorieGoal: Int,
        proteinGoalG: Int,
        carbsGoalG: Int,
        fatGoalG: Int,
        waterGoalMl: Int
    ) {
        viewModelScope.launch {
            val updated = dashboardState.value.profile.copy(
                weightKg = weightKg,
                targetWeightKg = targetWeightKg,
                heightCm = heightCm,
                age = age,
                goalMode = goalMode.name,
                dailyCalorieGoal = dailyCalorieGoal,
                proteinGoalG = proteinGoalG,
                carbsGoalG = carbsGoalG,
                fatGoalG = fatGoalG,
                waterGoalMl = waterGoalMl,
                preferredLang = preferredLang.value.code
            )
            val res = repository.saveOrUpdateProfile(currentUserId, updated)
            _actionMessage.value = if (res.isSuccess) {
                if (preferredLang.value == AppLang.BN) "লক্ষ্য সেভ হয়েছে!" else "Goals saved!"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to save goals."
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? =
        generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()

    private fun guessDefaultMealType(): MealType {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..10 -> MealType.BREAKFAST
            in 11..15 -> MealType.LUNCH
            in 16..18 -> MealType.SNACK
            else -> MealType.DINNER
        }
    }

    private fun computeStreakDays(meals: List<MealLog>): Int {
        if (meals.isEmpty()) return 0
        val loggedDates = meals.map { it.dateKey }.toSet()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val today = sdf.format(cal.time)
        // Allow streak to continue if user hasn't logged today yet but logged yesterday
        if (!loggedDates.contains(today)) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        var streak = 0
        while (loggedDates.contains(sdf.format(cal.time))) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    private fun buildWeeklyTrend(meals: List<MealLog>): List<DailyTrendPoint> {
        val keyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val labelFmt = SimpleDateFormat("EEE", Locale.US)
        val byDate = meals.groupBy { it.dateKey }
        val result = mutableListOf<DailyTrendPoint>()
        for (offset in -6..0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, offset)
            val key = keyFmt.format(cal.time)
            val dayMeals = byDate[key].orEmpty()
            result.add(
                DailyTrendPoint(
                    dateKey = key,
                    shortLabel = labelFmt.format(cal.time).take(3),
                    calories = dayMeals.sumOf { it.calories },
                    proteinG = dayMeals.sumOf { it.proteinG }.roundToInt(),
                    carbsG = dayMeals.sumOf { it.carbsG }.roundToInt(),
                    fatG = dayMeals.sumOf { it.fatG }.roundToInt()
                )
            )
        }
        return result
    }
}

fun todayDateKey(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return sdf.format(Date())
}

fun yesterdayDateKey(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -1)
    return sdf.format(cal.time)
}
