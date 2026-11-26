package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.UiStrings
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.DangerColor
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import kotlin.math.abs

@Composable
fun CalorieRingCard(
    eatenCalories: Int,
    goalCalories: Int,
    proteinG: Int,
    proteinGoalG: Int,
    carbsG: Int,
    carbsGoalG: Int,
    fatG: Int,
    fatGoalG: Int,
    strings: UiStrings,
    modifier: Modifier = Modifier
) {
    val safeGoal = goalCalories.coerceAtLeast(1)
    val rawProgress = eatenCalories.toFloat() / safeGoal.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "calorieRingProgress"
    )
    val isOver = eatenCalories > goalCalories
    val remaining = goalCalories - eatenCalories

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryArc = if (isOver) DangerColor else MaterialTheme.colorScheme.primary
    val secondaryArc = if (isOver) Color(0xFFF97316) else MaterialTheme.colorScheme.tertiary

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Animated Radial Calorie Ring
                Box(
                    modifier = Modifier.size(152.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(144.dp)) {
                        val strokeWidth = 15.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                        val arcSize = Size(diameter, diameter)

                        drawArc(
                            color = trackColor,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        if (animatedProgress > 0f) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(primaryArc, secondaryArc, primaryArc)
                                ),
                                startAngle = 135f,
                                sweepAngle = 270f * animatedProgress,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${abs(remaining)}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isOver) DangerColor else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isOver) strings.overGoalLabel else strings.remainingLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = strings.kcalUnit,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                // Eaten vs Goal stats column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatSummaryPill(
                        label = strings.eatenLabel,
                        value = "$eatenCalories ${strings.kcalUnit}",
                        accent = MaterialTheme.colorScheme.primary
                    )
                    StatSummaryPill(
                        label = strings.goalLabel,
                        value = "$goalCalories ${strings.kcalUnit}",
                        accent = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // 3 Macro Progress Columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MacroProgressChip(
                    label = strings.proteinLabel,
                    current = proteinG,
                    goal = proteinGoalG,
                    color = ProteinColor,
                    modifier = Modifier.weight(1f)
                )
                MacroProgressChip(
                    label = strings.carbsLabel,
                    current = carbsG,
                    goal = carbsGoalG,
                    color = CarbsColor,
                    modifier = Modifier.weight(1f)
                )
                MacroProgressChip(
                    label = strings.fatLabel,
                    current = fatG,
                    goal = fatGoalG,
                    color = FatColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatSummaryPill(
    label: String,
    value: String,
    accent: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent
        )
    }
}

@Composable
private fun MacroProgressChip(
    label: String,
    current: Int,
    goal: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val safeGoal = goal.coerceAtLeast(1)
    val progress by animateFloatAsState(
        targetValue = (current.toFloat() / safeGoal.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "macroBar"
    )
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${current}g / ${goal}g",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = color,
            trackColor = color.copy(alpha = 0.18f)
        )
    }
}
