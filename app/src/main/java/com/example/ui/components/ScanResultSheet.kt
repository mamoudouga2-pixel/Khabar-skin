package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.core.AppLang
import com.example.core.UiStrings
import com.example.data.model.MealType
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.FatColor
import com.example.ui.theme.HealthGoodColor
import com.example.ui.theme.HealthWarnColor
import com.example.ui.theme.ProteinColor
import com.example.ui.viewmodel.OilAdjustment
import com.example.ui.viewmodel.ScanUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScanResultSheet(
    scanState: ScanUiState,
    strings: UiStrings,
    onSelectMealType: (MealType) -> Unit,
    onSelectPortion: (Double) -> Unit,
    onSelectOil: (OilAdjustment) -> Unit,
    onToggleItem: (Int) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    if (scanState is ScanUiState.Idle) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (scanState) {
                ScanUiState.Idle -> Unit

                ScanUiState.Analyzing -> {
                    Spacer(Modifier.height(20.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(52.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = strings.analyzingTitle,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = strings.analyzingSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(36.dp))
                }

                is ScanUiState.Error -> {
                    Spacer(Modifier.height(12.dp))
                    Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = scanState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(20.dp))
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("scan_error_close_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(strings.cancelBtn)
                    }
                    Spacer(Modifier.height(24.dp))
                }

                is ScanUiState.Preview -> {
                    val analysis = scanState.analysis

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.scanResultTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_scan_sheet_btn")
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = strings.cancelBtn)
                        }
                    }

                    if (!analysis.foodDetected) {
                        Spacer(Modifier.height(12.dp))
                        Icon(
                            imageVector = Icons.Rounded.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = strings.notFoodTitle,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = strings.notFoodSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(strings.cancelBtn)
                        }
                        Spacer(Modifier.height(24.dp))
                        return@ModalBottomSheet
                    }

                    val activeItems = analysis.items.filterIndexed { index, _ ->
                        index !in scanState.excludedItemIndices
                    }
                    val useItemsSum = analysis.items.isNotEmpty()
                    val baseCalories = if (useItemsSum) activeItems.sumOf { it.calories } else analysis.totalCalories
                    val baseProtein = if (useItemsSum) activeItems.sumOf { it.proteinG } else analysis.totalProteinG
                    val baseCarbs = if (useItemsSum) activeItems.sumOf { it.carbsG } else analysis.totalCarbsG
                    val baseFat = if (useItemsSum) activeItems.sumOf { it.fatG } else analysis.totalFatG

                    val factor = scanState.portionMultiplier * scanState.oilAdjustment.factor
                    val scaledCalories = (baseCalories * factor).roundToInt()
                    val scaledProtein = (baseProtein * scanState.portionMultiplier).roundToInt()
                    val scaledCarbs = (baseCarbs * scanState.portionMultiplier).roundToInt()
                    val scaledFat = (baseFat * factor).roundToInt()

                    // Photo + Dish Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (scanState.photoBitmap != null) {
                            Image(
                                bitmap = scanState.photoBitmap.asImageBitmap(),
                                contentDescription = analysis.dishNameEn,
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(14.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (strings.lang == AppLang.BN) analysis.dishNameBn else analysis.dishNameEn,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (strings.lang == AppLang.BN) analysis.dishNameEn else analysis.dishNameBn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = analysis.cuisineType,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                val scoreColor = if (analysis.healthScore >= 7) HealthGoodColor else HealthWarnColor
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = scoreColor.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "${strings.healthScoreLabel}: ${analysis.healthScore}/10",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = scoreColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Big Calorie & Macro Strip
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$scaledCalories",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = strings.kcalUnit,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            MacroMiniBadge(strings.proteinLabel, "${scaledProtein}g", ProteinColor)
                            MacroMiniBadge(strings.carbsLabel, "${scaledCarbs}g", CarbsColor)
                            MacroMiniBadge(strings.fatLabel, "${scaledFat}g", FatColor)
                        }
                    }

                    // Interactive Detected Items Breakdown (check/uncheck individual plate items!)
                    if (analysis.items.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = strings.detectedItemsLabel,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(6.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            analysis.items.forEachIndexed { idx, item ->
                                val included = idx !in scanState.excludedItemIndices
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onToggleItem(idx) }
                                        .padding(vertical = 4.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Checkbox(
                                            checked = included,
                                            onCheckedChange = { onToggleItem(idx) }
                                        )
                                        Column {
                                            Text(
                                                text = if (strings.lang == AppLang.BN) item.nameBn else item.nameEn,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                textDecoration = if (included) null else TextDecoration.LineThrough,
                                                color = if (included) {
                                                    MaterialTheme.colorScheme.onSurface
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                            Text(
                                                text = item.portionText,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${(item.calories * factor).roundToInt()} kcal",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (included) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                                if (idx < analysis.items.lastIndex) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    )
                                }
                            }
                        }
                    }

                    // Portion Multiplier Chips
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = strings.portionSizeLabel,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    val portions = listOf(0.5, 0.75, 1.0, 1.25, 1.5, 2.0)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        portions.forEach { mult ->
                            val label = if (mult == 1.0) "1x" else "${mult}x"
                            FilterChip(
                                selected = scanState.portionMultiplier == mult,
                                onClick = { onSelectPortion(mult) },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Oil Adjustment Chips
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = strings.oilLevelLabel,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = scanState.oilAdjustment == OilAdjustment.LIGHT,
                            onClick = { onSelectOil(OilAdjustment.LIGHT) },
                            label = { Text(strings.oilLight) }
                        )
                        FilterChip(
                            selected = scanState.oilAdjustment == OilAdjustment.NORMAL,
                            onClick = { onSelectOil(OilAdjustment.NORMAL) },
                            label = { Text(strings.oilNormal) }
                        )
                        FilterChip(
                            selected = scanState.oilAdjustment == OilAdjustment.RICH,
                            onClick = { onSelectOil(OilAdjustment.RICH) },
                            label = { Text(strings.oilRich) }
                        )
                    }

                    // Meal Slot Chips
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = strings.selectMealSlot,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MealType.entries.forEach { type ->
                            FilterChip(
                                selected = scanState.selectedMealType == type,
                                onClick = { onSelectMealType(type) },
                                label = { Text(strings.mealTypeLabel(type)) }
                            )
                        }
                    }

                    // AI Tip Box
                    val tip = if (strings.lang == AppLang.BN) analysis.tipBn else analysis.tipEn
                    if (tip.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = onSave,
                        enabled = !scanState.isSaving && (!useItemsSum || activeItems.isNotEmpty()),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("save_scanned_meal_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (scanState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = strings.saveMealBtn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(22.dp))
                }
            }
        }
    }
}

@Composable
private fun MacroMiniBadge(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
