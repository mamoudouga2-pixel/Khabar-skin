package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.AppLang
import com.example.core.UiStrings
import com.example.data.model.GoalMode
import com.example.data.model.UserProfile
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    profile: UserProfile,
    userEmail: String,
    strings: UiStrings,
    onToggleLang: () -> Unit,
    onSaveGoals: (
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
    ) -> Unit,
    onSignOut: () -> Unit,
    onEnableCloudSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    var weightText by remember(profile.weightKg) { mutableStateOf(profile.weightKg.toString()) }
    var targetWeightText by remember(profile.targetWeightKg) { mutableStateOf(profile.targetWeightKg.toString()) }
    var heightText by remember(profile.heightCm) { mutableStateOf(profile.heightCm.toString()) }
    var ageText by remember(profile.age) { mutableStateOf(profile.age.toString()) }
    var goalMode by remember(profile.goalMode) { mutableStateOf(GoalMode.safeValueOf(profile.goalMode)) }

    var calText by remember(profile.dailyCalorieGoal) { mutableStateOf(profile.dailyCalorieGoal.toString()) }
    var proteinText by remember(profile.proteinGoalG) { mutableStateOf(profile.proteinGoalG.toString()) }
    var carbsText by remember(profile.carbsGoalG) { mutableStateOf(profile.carbsGoalG.toString()) }
    var fatText by remember(profile.fatGoalG) { mutableStateOf(profile.fatGoalG.toString()) }
    var waterText by remember(profile.waterGoalMl) { mutableStateOf(profile.waterGoalMl.toString()) }

    fun persistCurrentInputs() {
        val w = (weightText.toDoubleOrNull() ?: profile.weightKg).coerceIn(20.0, 300.0)
        val tw = (targetWeightText.toDoubleOrNull() ?: profile.targetWeightKg).coerceIn(20.0, 300.0)
        val h = (heightText.toIntOrNull() ?: profile.heightCm).coerceIn(80, 250)
        val a = (ageText.toIntOrNull() ?: profile.age).coerceIn(10, 110)
        val cal = (calText.toIntOrNull() ?: profile.dailyCalorieGoal).coerceIn(800, 6000)
        val p = (proteinText.toIntOrNull() ?: profile.proteinGoalG).coerceIn(20, 400)
        val c = (carbsText.toIntOrNull() ?: profile.carbsGoalG).coerceIn(20, 700)
        val f = (fatText.toIntOrNull() ?: profile.fatGoalG).coerceIn(10, 300)
        val water = (waterText.toIntOrNull() ?: profile.waterGoalMl).coerceIn(500, 6000)
        onSaveGoals(w, tw, h, a, goalMode, cal, p, c, f, water)
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
            item {
                Text(
                    text = strings.profileTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Account Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.displayName.firstOrNull()?.uppercase() ?: "K",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (userEmail.isBlank()) strings.localModeTitle else profile.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = userEmail.ifBlank { strings.localModeDesc },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (userEmail.isNotBlank()) {
                            OutlinedButton(
                                onClick = onSignOut,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("sign_out_btn"),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(strings.signOutBtn, color = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            OutlinedButton(
                                onClick = onEnableCloudSync,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("enable_cloud_sync_btn"),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CloudUpload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(strings.enableCloudSyncBtn)
                            }
                        }
                    }
                }
            }

            // Language Switcher Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
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
                            Icon(
                                imageVector = Icons.Rounded.Translate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = strings.languageSettingTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (strings.lang == AppLang.BN) "বাংলা (Bengali)" else "English",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        FilledTonalButton(
                            onClick = onToggleLang,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("profile_lang_toggle"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(strings.switchLangLabel, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Smart BMR/TDEE Calculator Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = strings.goalModeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GoalMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = goalMode == mode,
                                    onClick = { goalMode = mode },
                                    label = { Text(strings.goalModeLabel(mode)) }
                                )
                            }
                        }

                        Text(
                            text = strings.bodyMetricsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricField(
                                value = weightText,
                                onValueChange = { weightText = it },
                                label = strings.weightLabel,
                                modifier = Modifier.weight(1f)
                            )
                            MetricField(
                                value = targetWeightText,
                                onValueChange = { targetWeightText = it },
                                label = strings.targetWeightLabel,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricField(
                                value = heightText,
                                onValueChange = { heightText = it },
                                label = strings.heightLabel,
                                modifier = Modifier.weight(1f)
                            )
                            MetricField(
                                value = ageText,
                                onValueChange = { ageText = it },
                                label = strings.ageLabel,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                val w = (weightText.toDoubleOrNull() ?: 65.0).coerceIn(20.0, 300.0)
                                val h = (heightText.toIntOrNull() ?: 168).coerceIn(80, 250)
                                val a = (ageText.toIntOrNull() ?: 28).coerceIn(10, 110)
                                val bmr = (10.0 * w) + (6.25 * h) - (5.0 * a) - 40.0
                                val tdee = (bmr * 1.45).roundToInt()
                                val targetCal = when (goalMode) {
                                    GoalMode.LOSE -> (tdee - 400).coerceIn(1200, 4500)
                                    GoalMode.MAINTAIN -> tdee.coerceIn(1400, 4500)
                                    GoalMode.GAIN -> (tdee + 350).coerceIn(1600, 5500)
                                }
                                val protein = ((w * 1.6).roundToInt()).coerceIn(50, 280)
                                val fat = ((targetCal * 0.27) / 9.0).roundToInt().coerceIn(30, 180)
                                val carbs = ((targetCal - protein * 4 - fat * 9) / 4.0)
                                    .roundToInt().coerceIn(80, 550)
                                val water = (w * 35.0).roundToInt().coerceIn(1500, 4500)

                                calText = targetCal.toString()
                                proteinText = protein.toString()
                                carbsText = carbs.toString()
                                fatText = fat.toString()
                                waterText = water.toString()
                                persistCurrentInputs()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("auto_calculate_goals_btn"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Rounded.Calculate, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.autoCalcBtn, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Custom Daily Targets Editor Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = strings.customGoalsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricField(
                                value = calText,
                                onValueChange = { calText = it },
                                label = "${strings.goalLabel} (kcal)",
                                modifier = Modifier.weight(1f)
                            )
                            MetricField(
                                value = waterText,
                                onValueChange = { waterText = it },
                                label = "${strings.waterTitle} (ml)",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricField(
                                value = proteinText,
                                onValueChange = { proteinText = it },
                                label = "${strings.proteinLabel} (g)",
                                modifier = Modifier.weight(1f)
                            )
                            MetricField(
                                value = carbsText,
                                onValueChange = { carbsText = it },
                                label = "${strings.carbsLabel} (g)",
                                modifier = Modifier.weight(1f)
                            )
                            MetricField(
                                value = fatText,
                                onValueChange = { fatText = it },
                                label = "${strings.fatLabel} (g)",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = { persistCurrentInputs() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("save_goals_btn"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.saveGoalsBtn, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun MetricField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (input.all { it.isDigit() || it == '.' }) {
                onValueChange(input)
            }
        },
        label = { Text(label, maxLines = 1) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    )
}
