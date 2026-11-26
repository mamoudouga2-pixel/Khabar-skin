package com.example

import com.example.core.AppLang
import com.example.core.UiStrings
import com.example.data.catalog.BangladeshiFoodCatalog
import com.example.data.model.FoodCategory
import com.example.data.model.GoalMode
import com.example.data.model.MealSource
import com.example.data.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionLogicTest {

    @Test
    fun catalog_containsBangladeshiAndGlobalDishes() {
        val items = BangladeshiFoodCatalog.items
        assertTrue(items.size >= 25)
        assertTrue(items.any { it.category == FoodCategory.BANGLADESHI && it.nameEn.contains("Kacchi") })
        assertTrue(items.any { it.category == FoodCategory.FAST_FOOD && it.nameEn.contains("Burger") })
        assertTrue(items.all { it.calories > 0 && it.healthScore in 1..10 })
    }

    @Test
    fun enums_handleUnknownValuesSafely() {
        assertEquals(GoalMode.MAINTAIN, GoalMode.safeValueOf("UNKNOWN"))
        assertEquals(GoalMode.LOSE, GoalMode.safeValueOf("LOSE"))
        assertEquals(MealType.LUNCH, MealType.safeValueOf(null))
        assertEquals(MealType.BREAKFAST, MealType.safeValueOf("BREAKFAST"))
        assertEquals(MealSource.MANUAL, MealSource.safeValueOf("INVALID"))
        assertEquals(MealSource.PHOTO_AI, MealSource.safeValueOf("PHOTO_AI"))
    }

    @Test
    fun strings_switchBetweenBengaliAndEnglish() {
        val bn = UiStrings(AppLang.BN)
        val en = UiStrings(AppLang.EN)
        assertEquals("খাবারক্যাল", bn.appTitle)
        assertEquals("KhabarCal", en.appTitle)
        assertEquals("সকালের নাস্তা", bn.mealTypeLabel(MealType.BREAKFAST))
        assertEquals("Breakfast", en.mealTypeLabel(MealType.BREAKFAST))
    }
}
