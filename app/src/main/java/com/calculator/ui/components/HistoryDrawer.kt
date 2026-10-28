package com.calculator.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.data.history.HistoryItem
import com.calculator.ui.theme.SkinTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryPanel(
    history: List<HistoryItem>,
    onRecallResult: (HistoryItem) -> Unit,
    onRecallExpression: (HistoryItem) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onClearHistory: () -> Unit,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = SkinTheme.colors
    val typography = SkinTheme.typography
    val shapes = SkinTheme.shapes

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_panel")
            .clip(RoundedCornerShape(shapes.displayCornerRadius.coerceAtMost(16.dp)))
            .background(colors.surface)
            .border(shapes.displayBorderWidth, colors.border, RoundedCornerShape(shapes.displayCornerRadius.coerceAtMost(16.dp)))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HISTORY TAPE (${history.size})",
                style = typography.headerTitle,
                color = colors.textPrimary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (history.isNotEmpty()) {
                    TextButton(
                        onClick = onClearHistory,
                        modifier = Modifier.testTag("btn_clear_history")
                    ) {
                        Text(
                            text = "CLEAR",
                            style = typography.programmerBaseLabel,
                            color = colors.error
                        )
                    }
                }
                if (onClose != null) {
                    TextButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("btn_close_history")
                    ) {
                        Text(
                            text = "CLOSE",
                            style = typography.programmerBaseLabel,
                            color = colors.buttonAccent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No calculation history yet.",
                    style = typography.displayPreview,
                    color = colors.textSecondary,
                    modifier = Modifier.testTag("history_empty_state")
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_item_${item.id}")
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.displayBackground)
                            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                            .combinedClickable(
                                onClick = { onRecallResult(item) },
                                onLongClick = { onRecallExpression(item) }
                            )
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.mode.displayName.uppercase(),
                                style = typography.programmerBaseLabel.copy(fontSize = 10.sp),
                                color = colors.buttonAccent
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "EXPR",
                                    style = typography.programmerBaseLabel.copy(fontSize = 10.sp),
                                    color = colors.textSecondary,
                                    modifier = Modifier
                                        .testTag("history_recall_expr_${item.id}")
                                        .clickable { onRecallExpression(item) }
                                )
                                Text(
                                    text = "DEL",
                                    style = typography.programmerBaseLabel.copy(fontSize = 10.sp),
                                    color = colors.error,
                                    modifier = Modifier
                                        .testTag("history_delete_${item.id}")
                                        .clickable { onDeleteItem(item.id) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.expression,
                            style = typography.displayExpression.copy(fontSize = 14.sp),
                            color = colors.textSecondary
                        )
                        Text(
                            text = "= ${item.result}",
                            style = typography.displayResult.copy(fontSize = 20.sp),
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
