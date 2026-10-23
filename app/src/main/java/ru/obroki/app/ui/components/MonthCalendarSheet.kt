package ru.obroki.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthCalendarSheet(
    paymentDays: Set<Int>,
    today: LocalDate = LocalDate.now(),
    onDismiss: () -> Unit,
) {
    val ym = YearMonth.from(today)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val locale = Locale("ru")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = ym.month.getDisplayName(TextStyle.FULL_STANDALONE, locale)
                    .replaceFirstChar { it.titlecase(locale) } + " ${ym.year}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("пн", "вт", "ср", "чт", "пт", "сб", "вс").forEach { d ->
                    Text(
                        text = d,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            val firstDow = ym.atDay(1).dayOfWeek
            // Monday = 1 ... Sunday = 7 in ISO; offset from Monday
            val offset = (firstDow.value - DayOfWeek.MONDAY.value + 7) % 7
            val daysInMonth = ym.lengthOfMonth()
            val cells = offset + daysInMonth
            val rows = (cells + 6) / 7

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    for (col in 0..6) {
                        val index = row * 7 + col
                        val dayNum = index - offset + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (dayNum in 1..daysInMonth) {
                                val isToday = today.dayOfMonth == dayNum &&
                                    today.monthValue == ym.monthValue &&
                                    today.year == ym.year
                                val hasPayment = dayNum in paymentDays
                                val shape = CircleShape
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(shape)
                                        .then(
                                            if (hasPayment) {
                                                Modifier.background(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                                )
                                            } else Modifier
                                        )
                                        .then(
                                            if (isToday) {
                                                Modifier.border(
                                                    2.dp,
                                                    MaterialTheme.colorScheme.primary,
                                                    shape,
                                                )
                                            } else Modifier
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = dayNum.toString(),
                                        fontWeight = if (isToday || hasPayment) FontWeight.Bold else FontWeight.Normal,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = "Обводка — сегодня · заливка — день с платежом",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}
