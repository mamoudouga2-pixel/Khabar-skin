package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.AppLang
import com.example.core.UiStrings
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.DangerColor
import com.example.ui.theme.FatColor
import com.example.ui.theme.HealthGoodColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.StreakFlameColor
import com.example.ui.theme.WaterColor
import com.example.ui.viewmodel.DailyTrendPoint
import com.example.ui.viewmodel.DashboardSnapshot
import java.util.Locale
import kotlin.math.max
import kotlin.math.pow

@Composable
fun ProgressScreen(
    snapshot: DashboardSnapshot,
    strings: UiStrings,
    modifier: Modifier = Modifier
) {
    val activeDays = snapshot.weeklyTrend.filter { it.calories > 0 }
    val avgCalories = if (activeDays.isNotEmpty()) {
        activeDays.sumOf { it.calories } / activeDays.size
    } else 0
    val weeklyProtein = snapshot.weeklyTrend.sumOf { it.proteinG }
    val weeklyCarbs = snapshot.weeklyTrend.sumOf { it.carbsG }
    val weeklyFat = snapshot.weeklyTrend.sumOf { it.fatG }
    val totalMacroG = max(weeklyProtein + weeklyCarbs + weeklyFat, 1)

    var selectedDayIndex by remember(snapshot.weeklyTrend.size) {
        mutableIntStateOf((snapshot.weeklyTrend.size - 1).coerceAtLeast(0))
    }
    val selectedDay = snapshot.weeklyTrend.getOrNull(selectedDayIndex)

    val heightM = (snapshot.profile.heightCm / 100.0).coerceAtLeast(0.8)
    val bmi = snapshot.profile.weightKg / heightM.pow(2.0)
    val bmiText = String.format(Locale.US, "%.1f", bmi)

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
            item {
                Text(
                    text = strings.progressTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // KPI Summary Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiStatCard(
                        label = strings.avgCaloriesLabel,
                        value = "$avgCalories kcal",
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        label = strings.streakDays(snapshot.currentStreakDays),
                        value = "${snapshot.currentStreakDays}",
                        accent = StreakFlameColor,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        label = strings.totalMealsLoggedLabel,
                        value = "${snapshot.allMeals.size}",
                        accent = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Weight & BMI Snapshot Card
            item {
                val bn = strings.lang == AppLang.BN
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MonitorWeight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (bn) "ওজন ও BMI অবস্থা" else "Weight & BMI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (bn) {
                                        "${snapshot.profile.weightKg} কেজি → লক্ষ্য ${snapshot.profile.targetWeightKg} কেজি"
                                    } else {
                                        "${snapshot.profile.weightKg} kg → Goal ${snapshot.profile.targetWeightKg} kg"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "BMI $bmiText",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Interactive 7-Day Calorie Trend Bar Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.weeklyChartTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${strings.goalLabel}: ${snapshot.profile.dailyCalorieGoal}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        if (selectedDay != null) {
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "${selectedDay.dateKey} • ${selectedDay.calories} kcal  (P ${selectedDay.proteinG}g / C ${selectedDay.carbsG}g / F ${selectedDay.fatG}g)",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        WeeklyCaloriesBarChart(
                            points = snapshot.weeklyTrend,
                            selectedIndex = selectedDayIndex,
                            goalCalories = snapshot.profile.dailyCalorieGoal,
                            onSelectDay = { selectedDayIndex = it }
                        )
                    }
                }
            }

            // Weekly Macro Split Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = strings.macroBalanceTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            val pWeight = (weeklyProtein.toFloat() / totalMacroG).coerceAtLeast(0.02f)
                            val cWeight = (weeklyCarbs.toFloat() / totalMacroG).coerceAtLeast(0.02f)
                            val fWeight = (weeklyFat.toFloat() / totalMacroG).coerceAtLeast(0.02f)
                            Box(
                                modifier = Modifier
                                    .weight(pWeight)
                                    .fillMaxSize()
                                    .background(ProteinColor)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(cWeight)
                                    .fillMaxSize()
                                    .background(CarbsColor)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(fWeight)
                                    .fillMaxSize()
                                    .background(FatColor)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MacroLegend(strings.proteinLabel, "${weeklyProtein}g", ProteinColor)
                            MacroLegend(strings.carbsLabel, "${weeklyCarbs}g", CarbsColor)
                            MacroLegend(strings.fatLabel, "${weeklyFat}g", FatColor)
                        }
                    }
                }
            }

            // Gamified Badges for Daily Retention
            item {
                val bn = strings.lang == AppLang.BN
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = strings.achievementsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            BadgeRow(
                                icon = Icons.Rounded.AutoAwesome,
                                title = if (bn) "প্রথম AI স্ক্যান" else "First Meal Logged",
                                unlocked = snapshot.allMeals.isNotEmpty(),
                                color = HealthGoodColor
                            )
                            BadgeRow(
                                icon = Icons.Rounded.LocalFireDepartment,
                                title = if (bn) "৩ দিনের নিয়মিত স্ট্রিক" else "3-Day Logging Streak",
                                unlocked = snapshot.currentStreakDays >= 3,
                                color = StreakFlameColor
                            )
                            BadgeRow(
                                icon = Icons.Rounded.EmojiEvents,
                                title = if (bn) "৭ দিনের গোল্ড স্ট্রিক" else "7-Day Gold Streak",
                                unlocked = snapshot.currentStreakDays >= 7,
                                color = CarbsColor
                            )
                            BadgeRow(
                                icon = Icons.Rounded.WaterDrop,
                                title = if (bn) "দৈনিক পানির লক্ষ্য পূরণ" else "Daily Hydration Goal Met",
                                unlocked = snapshot.selectedDayWaterMl >= snapshot.profile.waterGoalMl,
                                color = WaterColor
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun KpiStatCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WeeklyCaloriesBarChart(
    points: List<DailyTrendPoint>,
    selectedIndex: Int,
    goalCalories: Int,
    onSelectDay: (Int) -> Unit
) {
    val maxVal = max(points.maxOfOrNull { it.calories } ?: 0, goalCalories).coerceAtLeast(1000)
    val primaryColor = MaterialTheme.colorScheme.primary
    val goalLineColor = MaterialTheme.colorScheme.secondary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val count = points.size.coerceAtLeast(1)
            val slotWidth = size.width / count
            val barWidth = (slotWidth * 0.48f).coerceAtMost(36.dp.toPx())
            val chartHeight = size.height - 8.dp.toPx()

            val goalY = chartHeight * (1f - (goalCalories.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f))
            drawLine(
                color = goalLineColor,
                start = Offset(0f, goalY),
                end = Offset(size.width, goalY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )

            points.forEachIndexed { index, pt ->
                val centerX = slotWidth * index + slotWidth / 2f
                val left = centerX - barWidth / 2f

                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, chartHeight),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )

                if (pt.calories > 0) {
                    val ratio = (pt.calories.toFloat() / maxVal.toFloat()).coerceIn(0.04f, 1f)
                    val barH = chartHeight * ratio
                    val barColor = if (pt.calories > goalCalories) DangerColor else primaryColor
                    val alpha = if (index == selectedIndex) 1f else 0.72f
                    drawRoundRect(
                        color = barColor.copy(alpha = alpha),
                        topLeft = Offset(left, chartHeight - barH),
                        size = Size(barWidth, barH),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            points.forEachIndexed { index, pt ->
                val isSelected = index == selectedIndex
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    modifier = Modifier.clickable { onSelectDay(index) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (pt.calories > 0) "${pt.calories}" else "-",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = pt.shortLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroLegend(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BadgeRow(
    icon: ImageVector,
    title: String,
    unlocked: Boolean,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (unlocked) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (unlocked) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (unlocked) FontWeight.Bold else FontWeight.Normal,
                color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
