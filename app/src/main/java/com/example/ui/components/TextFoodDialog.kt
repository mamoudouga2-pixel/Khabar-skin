package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.AppLang
import com.example.core.UiStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextFoodDialog(
    strings: UiStrings,
    initialQuery: String = "",
    onAnalyze: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember(initialQuery) { mutableStateOf(initialQuery) }
    val examples = remember(strings.lang) {
        if (strings.lang == AppLang.BN) {
            listOf(
                "১ প্লেট কাচ্চি বিরিয়ানি ও বোরহানি",
                "২ কাপ সাদা ভাত, রুই মাছ ও মসুর ডাল",
                "২টি পরোটা, ডিম ভাজি ও দুধ চা",
                "৬ পিস ফুচকা ও ১ গ্লাস লেবুর শরবত"
            )
        } else {
            listOf(
                "1 plate Mutton Kacchi Biryani & Borhani",
                "2 cups rice, fried Hilsa fish & dal",
                "2 parathas, egg omelette & milk tea",
                "Grilled chicken burger & 1 apple"
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = strings.textDialogTitle,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(strings.textDialogHint) },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("text_food_input")
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = strings.textDialogExamples,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    examples.forEach { sample ->
                        AssistChip(
                            onClick = { query = sample },
                            label = {
                                Text(
                                    text = sample,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (query.isNotBlank()) {
                        onAnalyze(query.trim())
                    }
                },
                enabled = query.isNotBlank(),
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .testTag("analyze_text_food_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.analyzeTextBtn)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelBtn)
            }
        }
    )
}
