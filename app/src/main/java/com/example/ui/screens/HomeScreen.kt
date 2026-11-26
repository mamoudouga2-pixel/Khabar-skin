package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.AppLang
import com.example.core.UiStrings
import com.example.data.catalog.BangladeshiFoodCatalog
import com.example.data.model.CuratedFoodItem
import com.example.data.model.MealLog
import com.example.data.model.MealType
import com.example.ui.components.CalorieRingCard
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.FatColor
import com.example.ui.theme.HealthGoodColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.StreakFlameColor
import com.example.ui.theme.WaterColor
import com.example.ui.viewmodel.DashboardSnapshot
import com.example.ui.viewmodel.todayDateKey
import com.example.ui.viewmodel.yesterdayDateKey
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    snapshot: DashboardSnapshot,
    strings: UiStrings,
    coachLoading: Boolean,
    onToggleLang: () -> Unit,
    onShiftDate: (Int) -> Unit,
    onSelectToday: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onTextClick: (MealType?) -> Unit,
    onQuickAddFood: (CuratedFoodItem) -> Unit,
    onAdjustWater: (Int) -> Unit,
    onSetWaterExact: (Int) -> Unit,
    onRefreshCoachTip: () -> Unit,
    onDeleteMeal: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayKey = todayDateKey()
    val yesterdayKey = yesterdayDateKey()
    val dateLabel = when (snapshot.selectedDateKey) {
        todayKey -> strings.todayLabel
        yesterdayKey -> strings.yesterdayLabel
        else -> snapshot.selectedDateKey
    }

    val quickFavorites = remember {
        val ids = setOf("sada_bhat", "dim_bhaji", "dudh_cha", "ilish_bhaja", "kacchi_biryani", "atar_ruti", "chicken_burger")
        BangladeshiFoodCatalog.items.filter { it.id in ids }
    }

    val groupedMeals = remember(snapshot.selectedDayMeals) {
        MealType.entries.associateWith { slot ->
            snapshot.selectedDayMeals.filter { MealType.safeValueOf(it.mealType) == slot }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Greeting, Streak Badge & Language Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.appTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        val subtitle = if (snapshot.profile.displayName.isBlank() ||
                            snapshot.profile.displayName == "KhabarCal" ||
                            snapshot.profile.displayName == "KhabarCal User"
                        ) {
                            if (strings.lang == AppLang.BN) "প্রতিদিনের স্মার্ট ক্যালরি ডায়েরি" else "Smart Daily Calorie Diary"
                        } else {
                            snapshot.profile.displayName
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = StreakFlameColor.copy(alpha = 0.14f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = StreakFlameColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = strings.streakDays(snapshot.currentStreakDays),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StreakFlameColor
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onToggleLang,
                            modifier = Modifier.testTag("home_lang_toggle"),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Translate,
                                contentDescription = "Switch language",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(strings.switchLangLabel, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Date Navigation Pill
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onShiftDate(-1) },
                            modifier = Modifier.testTag("prev_day_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                contentDescription = "Previous day"
                            )
                        }
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (snapshot.selectedDateKey != todayKey) {
                                FilledTonalButton(
                                    onClick = onSelectToday,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(strings.todayLabel, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            IconButton(
                                onClick = { onShiftDate(1) },
                                enabled = snapshot.selectedDateKey < todayKey,
                                modifier = Modifier.testTag("next_day_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = "Next day"
                                )
                            }
                        }
                    }
                }
            }

            if (snapshot.isSyncError != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = snapshot.isSyncError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Hero Calorie Ring & Macro Card
            item {
                CalorieRingCard(
                    eatenCalories = snapshot.totalCalories,
                    goalCalories = snapshot.profile.dailyCalorieGoal,
                    proteinG = snapshot.totalProteinG,
                    proteinGoalG = snapshot.profile.proteinGoalG,
                    carbsG = snapshot.totalCarbsG,
                    carbsGoalG = snapshot.profile.carbsGoalG,
                    fatG = snapshot.totalFatG,
                    fatGoalG = snapshot.profile.fatGoalG,
                    strings = strings
                )
            }

            // Flagship Scanner Card: Prominent Camera + Gallery + Describe
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.scanSectionTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = onCameraClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("home_camera_scan_btn"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Rounded.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (strings.lang == AppLang.BN) "ছবি তুলে ক্যালরি মাপুন" else "Scan Meal with Camera",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SecondaryActionTile(
                                icon = Icons.Rounded.PhotoLibrary,
                                label = strings.galleryScanBtn,
                                onClick = onGalleryClick,
                                testTag = "home_gallery_scan_btn",
                                modifier = Modifier.weight(1f)
                            )
                            SecondaryActionTile(
                                icon = Icons.Rounded.EditNote,
                                label = strings.textAiBtn,
                                onClick = { onTextClick(null) },
                                testTag = "home_text_scan_btn",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 1-Tap Quick Favorite Dishes Strip
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (strings.lang == AppLang.BN) "এক ক্লিকে দ্রুত যোগ করুন" else "1-Tap Quick Add",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickFavorites, key = { it.id }) { food ->
                            Surface(
                                onClick = { onQuickAddFood(food) },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
                                modifier = Modifier.testTag("quick_chip_${food.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (strings.lang == AppLang.BN) food.nameBn else food.nameEn,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "${food.calories}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Interactive 8-Glass Water Tracker Card
            item {
                val waterGoal = snapshot.profile.waterGoalMl.coerceAtLeast(500)
                val waterProgress = (snapshot.selectedDayWaterMl.toFloat() / waterGoal).coerceIn(0f, 1f)
                val filledGlasses = (snapshot.selectedDayWaterMl / 250).coerceIn(0, 8)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(WaterColor.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.WaterDrop,
                                        contentDescription = null,
                                        tint = WaterColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = strings.waterTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${snapshot.selectedDayWaterMl} / $waterGoal ml",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = WaterColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { onAdjustWater(-250) },
                                    enabled = snapshot.selectedDayWaterMl >= 250,
                                    modifier = Modifier.testTag("water_minus_btn")
                                ) {
                                    Icon(Icons.Rounded.Remove, contentDescription = "Remove 250 ml")
                                }
                                FilledTonalButton(
                                    onClick = { onAdjustWater(250) },
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("water_plus_btn"),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(strings.waterAddGlass, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (glass in 1..8) {
                                val filled = glass <= filledGlasses
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (filled) WaterColor.copy(alpha = 0.18f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                        )
                                        .clickable { onSetWaterExact(glass * 250) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalDrink,
                                        contentDescription = "${glass * 250} ml",
                                        tint = if (filled) WaterColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { waterProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(50)),
                            color = WaterColor,
                            trackColor = WaterColor.copy(alpha = 0.15f)
                        )
                    }
                }
            }

            // Smart Daily AI Coach Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.42f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = strings.coachTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            FilledTonalButton(
                                onClick = onRefreshCoachTip,
                                enabled = !coachLoading,
                                modifier = Modifier
                                    .height(42.dp)
                                    .testTag("refresh_coach_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (coachLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(strings.coachAskBtn, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = snapshot.selectedDayCoachTip.ifBlank { strings.coachEmptyHint },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Today's Meals Header
            item {
                Text(
                    text = strings.todaysMealsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (snapshot.selectedDayMeals.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_empty_plate),
                                contentDescription = strings.emptyMealsTitle,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = strings.emptyMealsTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = strings.emptyMealsDesc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                MealType.entries.forEach { slot ->
                    val slotMeals = groupedMeals[slot].orEmpty()
                    val slotCalories = slotMeals.sumOf { it.calories }
                    item(key = "slot_header_${slot.name}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = strings.mealTypeLabel(slot),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (slotCalories > 0) {
                                    Text(
                                        text = "$slotCalories kcal",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onTextClick(slot) },
                                modifier = Modifier.testTag("add_slot_${slot.name}")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Add to ${strings.mealTypeLabel(slot)}",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    items(
                        items = slotMeals,
                        key = { it.id.ifBlank { "${it.dishNameEn}_${it.createdAt?.seconds}" } }
                    ) { meal ->
                        LoggedMealCard(
                            meal = meal,
                            strings = strings,
                            onDelete = { if (meal.id.isNotBlank()) onDeleteMeal(meal.id) }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun SecondaryActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(50.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LoggedMealCard(
    meal: MealLog,
    strings: UiStrings,
    onDelete: () -> Unit
) {
    val title = if (strings.lang == AppLang.BN) meal.dishNameBn else meal.dishNameEn
    val subtitle = if (strings.lang == AppLang.BN) meal.dishNameEn else meal.dishNameBn

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = meal.cuisineType,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${meal.calories} kcal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_meal_${meal.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = strings.deleteMeal,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (subtitle.isNotBlank() && subtitle != title) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (meal.itemsSummary.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = meal.itemsSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroTag("P ${meal.proteinG.roundToInt()}g", ProteinColor)
                MacroTag("C ${meal.carbsG.roundToInt()}g", CarbsColor)
                MacroTag("F ${meal.fatG.roundToInt()}g", FatColor)
                MacroTag("${meal.healthScore}/10", HealthGoodColor)
            }
        }
    }
}

@Composable
private fun MacroTag(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
