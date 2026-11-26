package com.example.core

import androidx.annotation.Keep
import com.example.data.model.GoalMode
import com.example.data.model.MealType

@Keep
enum class AppLang(val code: String) {
    BN("bn"),
    EN("en");

    companion object {
        fun fromCode(code: String?): AppLang =
            entries.firstOrNull { it.code == code } ?: BN
    }
}

data class UiStrings(
    val lang: AppLang
) {
    private val bn: Boolean get() = lang == AppLang.BN

    val appTitle get() = if (bn) "খাবারক্যাল" else "KhabarCal"
    val appTagline get() = if (bn) "ছবি তুলেই দেশি ও বিদেশি খাবারের ক্যালরি জানুন" else "Snap a meal photo for instant Bangla & global calories"
    val signInTitle get() = if (bn) "প্রতিদিনের স্মার্ট পুষ্টি সঙ্গী" else "Your Smart Daily Nutrition Companion"
    val signInSubtitle get() = if (bn) "ভাত-মাছ, কাচ্চি, ফুচকা থেকে বার্গার—এক ক্লিকেই ক্যালরি, প্রোটিন ও কার্বস মাপুন সম্পূর্ণ বিনামূল্যে।" else "From Kacchi Biryani & Hilsa to Burgers — instant AI calories, macros & daily coaching, 100% free."
    val signInWithGoogle get() = if (bn) "Google দিয়ে চালিয়ে যান" else "Continue with Google"
    val freeZeroCostBadge get() = if (bn) "১০০% ফ্রি AI স্ক্যান • ক্লাউড ব্যাকআপ" else "100% Free AI Scan • Cloud Sync"
    val switchLangLabel get() = if (bn) "EN" else "বাংলা"

    // Navigation Tabs
    val navHome get() = if (bn) "হোম" else "Today"
    val navFoods get() = if (bn) "খাবার" else "Foods"
    val navProgress get() = if (bn) "প্রগ্রেস" else "Progress"
    val navProfile get() = if (bn) "লক্ষ্য" else "Goals"

    // Home Dashboard
    val todayLabel get() = if (bn) "আজ" else "Today"
    val yesterdayLabel get() = if (bn) "গতকাল" else "Yesterday"
    fun streakDays(days: Int) = if (bn) "$days দিন স্ট্রিক" else "$days day streak"
    val remainingLabel get() = if (bn) "বাকি আছে" else "Remaining"
    val overGoalLabel get() = if (bn) "বেশি হয়েছে" else "Over goal"
    val eatenLabel get() = if (bn) "খাওয়া হয়েছে" else "Eaten"
    val goalLabel get() = if (bn) "দৈনিক লক্ষ্য" else "Daily Goal"
    val kcalUnit get() = "kcal"
    val proteinLabel get() = if (bn) "প্রোটিন" else "Protein"
    val carbsLabel get() = if (bn) "কার্বস" else "Carbs"
    val fatLabel get() = if (bn) "ফ্যাট" else "Fat"
    val fiberLabel get() = if (bn) "ফাইবার" else "Fiber"

    // Quick Scan Bar
    val scanSectionTitle get() = if (bn) "খাবার যোগ করুন (AI স্ক্যান)" else "Log Food with AI"
    val cameraScanBtn get() = if (bn) "ছবি তুলুন" else "Take Photo"
    val galleryScanBtn get() = if (bn) "গ্যালারি" else "Gallery"
    val textAiBtn get() = if (bn) "লিখে মাপুন" else "Describe"
    val analyzingTitle get() = if (bn) "AI আপনার খাবার বিশ্লেষণ করছে..." else "AI is analyzing your meal..."
    val analyzingSubtitle get() = if (bn) "দেশি ও বিদেশি আইটেম এবং ক্যালরি হিসাব হচ্ছে" else "Detecting dishes, portion size & macros"

    // Water Tracker
    val waterTitle get() = if (bn) "দৈনিক পানি" else "Daily Water"
    val waterAddGlass get() = if (bn) "+২৫০ মি.লি." else "+250 ml"
    val waterGoalReached get() = if (bn) "লক্ষ্য পূরণ!" else "Goal met!"

    // AI Coach
    val coachTitle get() = if (bn) "স্মার্ট AI পুষ্টি পরামর্শ" else "Smart AI Coach"
    val coachEmptyHint get() = if (bn) "আজকের খাবারের ওপর ভিত্তি করে AI থেকে ব্যক্তিগত পরামর্শ নিন।" else "Get a personalized nutrition tip based on today's meals."
    val coachAskBtn get() = if (bn) "পরামর্শ নিন" else "Get Tip"
    val coachRefreshing get() = if (bn) "হিসাব হচ্ছে..." else "Thinking..."

    // Meals Section
    val todaysMealsTitle get() = if (bn) "আজকের খাবারের তালিকা" else "Today's Meals"
    val emptyMealsTitle get() = if (bn) "এখনও কোনো খাবার যোগ করা হয়নি" else "No meals logged yet"
    val emptyMealsDesc get() = if (bn) "উপরের ক্যামেরা বা গ্যালারি বাটনে চাপ দিয়ে আপনার খাবারের ছবি তুলুন অথবা লিখে যোগ করুন।" else "Tap Take Photo, Gallery, or Describe above to scan your first meal."
    val deleteMeal get() = if (bn) "মুছুন" else "Delete"

    fun mealTypeLabel(type: MealType): String = when (type) {
        MealType.BREAKFAST -> if (bn) "সকালের নাস্তা" else "Breakfast"
        MealType.LUNCH -> if (bn) "দুপুরের খাবার" else "Lunch"
        MealType.DINNER -> if (bn) "রাতের খাবার" else "Dinner"
        MealType.SNACK -> if (bn) "বিকেলের নাস্তা" else "Snack"
    }

    // Scan Result Sheet
    val scanResultTitle get() = if (bn) "খাবারের বিশ্লেষণ" else "Meal Analysis"
    val healthScoreLabel get() = if (bn) "স্বাস্থ্য স্কোর" else "Health Score"
    val detectedItemsLabel get() = if (bn) "প্লেটে যা পাওয়া গেছে" else "Detected Items"
    val portionSizeLabel get() = if (bn) "পরিমাণ ঠিক করুন" else "Adjust Portion"
    val oilLevelLabel get() = if (bn) "তেল/রান্নার ধরন" else "Oil / Cooking"
    val oilLight get() = if (bn) "কম তেল (-১৫%)" else "Light Oil (-15%)"
    val oilNormal get() = if (bn) "স্বাভাবিক" else "Normal"
    val oilRich get() = if (bn) "বেশি তেল (+২০%)" else "Rich/Oily (+20%)"
    val selectMealSlot get() = if (bn) "কোন বেলার খাবার?" else "Meal Slot"
    val saveMealBtn get() = if (bn) "আজকের হিসাবে যোগ করুন" else "Log Meal to Today"
    val cancelBtn get() = if (bn) "বাতিল" else "Cancel"
    val notFoodTitle get() = if (bn) "ছবিতে কোনো খাবার শনাক্ত হয়নি" else "No food detected in image"
    val notFoodSubtitle get() = if (bn) "দয়া করে প্লেটের পরিষ্কার ছবি তুলুন অথবা খাবারের নাম লিখে যোগ করুন।" else "Please capture a clear plate photo or type your meal instead."

    // Text Food Dialog
    val textDialogTitle get() = if (bn) "লিখে খাবার যোগ করুন" else "Describe Your Meal"
    val textDialogHint get() = if (bn) "যেমন: ২ কাপ সাদা ভাত, ১ পিস রুই মাছ ও মসুর ডাল" else "e.g., 1 plate Kacchi Biryani and Borhani"
    val textDialogExamples get() = if (bn) "জনপ্রিয় উদাহরণ (ট্যাপ করুন):" else "Quick examples (tap to fill):"
    val analyzeTextBtn get() = if (bn) "AI দিয়ে ক্যালরি মাপুন" else "Analyze with AI"

    // Food Catalog Screen
    val catalogTitle get() = if (bn) "দেশি ও বিদেশি খাবারের গাইড" else "Bangla & Global Food Guide"
    val searchFoodHint get() = if (bn) "খাবারের নাম লিখুন (যেমন: বিরিয়ানি, ইলিশ, বার্গার)" else "Search foods (e.g., Biryani, Hilsa, Burger)"
    val askAiSearchBtn get() = if (bn) "AI দিয়ে এই খাবারটি খুঁজুন" else "Ask AI for this dish"
    val addBtn get() = if (bn) "যোগ করুন" else "Add"

    // Progress Screen
    val progressTitle get() = if (bn) "সাপ্তাহিক প্রগ্রেস ও বিশ্লেষণ" else "Weekly Insights & Trends"
    val weeklyChartTitle get() = if (bn) "গত ৭ দিনের ক্যালরি" else "Last 7 Days Calories"
    val avgCaloriesLabel get() = if (bn) "গড় দৈনিক ক্যালরি" else "Avg Daily Calories"
    val totalMealsLoggedLabel get() = if (bn) "মোট লগ করা খাবার" else "Total Meals Logged"
    val macroBalanceTitle get() = if (bn) "সাপ্তাহিক ম্যাক্রো অনুপাত" else "Weekly Macro Split"
    val achievementsTitle get() = if (bn) "অর্জন ও ব্যাজ" else "Badges & Milestones"

    // Profile & Goals Screen
    val profileTitle get() = if (bn) "লক্ষ্য ও প্রোফাইল" else "Goals & Profile"
    val goalModeTitle get() = if (bn) "আপনার স্বাস্থ্য লক্ষ্য" else "Your Fitness Goal"
    fun goalModeLabel(mode: GoalMode): String = when (mode) {
        GoalMode.LOSE -> if (bn) "ওজন কমানো" else "Lose Weight"
        GoalMode.MAINTAIN -> if (bn) "ওজন ধরে রাখা" else "Maintain"
        GoalMode.GAIN -> if (bn) "পেশী/ওজন বাড়ানো" else "Gain Muscle"
    }
    val bodyMetricsTitle get() = if (bn) "দৈনিক ক্যালরি ক্যালকুলেটর" else "Smart Goal Calculator"
    val weightLabel get() = if (bn) "বর্তমান ওজন (কেজি)" else "Weight (kg)"
    val targetWeightLabel get() = if (bn) "লক্ষ্য ওজন (কেজি)" else "Target (kg)"
    val heightLabel get() = if (bn) "উচ্চতা (সে.মি.)" else "Height (cm)"
    val ageLabel get() = if (bn) "বয়স (বছর)" else "Age (yrs)"
    val autoCalcBtn get() = if (bn) "অটো ক্যালকুলেট ও সেভ" else "Auto-Calculate & Save"
    val customGoalsTitle get() = if (bn) "দৈনিক লক্ষ্য কাস্টমাইজ করুন" else "Daily Targets"
    val saveGoalsBtn get() = if (bn) "লক্ষ্য সেভ করুন" else "Save Targets"
    val languageSettingTitle get() = if (bn) "অ্যাপের ভাষা" else "App Language"
    val localModeTitle get() = if (bn) "লোকাল মোড চালু আছে" else "On-Device Storage"
    val localModeDesc get() = if (bn) "সব খাবার ও লক্ষ্য ফোনে সেভ হচ্ছে (লগইন ছাড়াই)" else "Meals & goals saved locally (no login needed)"
    val enableCloudSyncBtn get() = if (bn) "Google ব্যাকআপ" else "Cloud Sync"
    val signOutBtn get() = if (bn) "সাইন আউট" else "Sign Out"
    val cameraPermissionDenied get() = if (bn) "ছবি তোলার জন্য ক্যামেরা অনুমতি প্রয়োজন।" else "Camera permission is required to take food photos."
}
