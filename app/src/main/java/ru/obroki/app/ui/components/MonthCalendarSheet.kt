package ru.obroki.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.obroki.app.domain.RowHighlight
import ru.obroki.app.ui.theme.RowOverdueBgDark
import ru.obroki.app.ui.theme.RowPaidBgDark
import ru.obroki.app.ui.theme.RowSoonBgDark
import ru.obroki.app.ui.theme.StatusMixedFallback
import ru.obroki.app.ui.theme.StatusOverdue
import ru.obroki.app.ui.theme.StatusPaid
import ru.obroki.app.ui.theme.StatusSoon
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthCalendarSheet(
    /** Day-of-month → distinct row highlights for payments due that day. */
    dayHighlights: Map<Int, Set<RowHighlight>>,
    today: LocalDate = LocalDate.now(),
    onDismiss: () -> Unit,
) {
    val ym = YearMonth.from(today)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val locale = Locale("ru")
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f

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
                                val highlights = dayHighlights[dayNum].orEmpty()
                                val statusColors = calendarStatusColors(highlights, dark)
                                val hasStatus = statusColors.isNotEmpty()
                                CalendarDayCell(
                                    dayNum = dayNum,
                                    isToday = isToday,
                                    statusColors = statusColors,
                                    emphasize = isToday || hasStatus,
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "Обводка — сегодня · цвет дня — статус платежа (сегменты при нескольких)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNum: Int,
    isToday: Boolean,
    statusColors: List<Color>,
    emphasize: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isToday) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (statusColors.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize().padding(3.dp)) {
                val stroke = Stroke(width = size.minDimension * 0.12f, cap = StrokeCap.Butt)
                val inset = stroke.width / 2f
                val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
                val topLeft = Offset(inset, inset)
                if (statusColors.size == 1) {
                    drawArc(
                        color = statusColors[0].copy(alpha = 0.55f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = true,
                        topLeft = topLeft,
                        size = arcSize,
                    )
                    drawArc(
                        color = statusColors[0],
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke,
                    )
                } else {
                    val sweep = 360f / statusColors.size
                    statusColors.forEachIndexed { i, color ->
                        val start = -90f + i * sweep
                        drawArc(
                            color = color.copy(alpha = 0.45f),
                            startAngle = start,
                            sweepAngle = sweep,
                            useCenter = true,
                            topLeft = topLeft,
                            size = arcSize,
                        )
                        drawArc(
                            color = color,
                            startAngle = start,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = stroke,
                        )
                    }
                }
            }
        }
        Text(
            text = dayNum.toString(),
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

/**
 * Status colors for a calendar day, matching list row highlights.
 * Multiple distinct statuses → one color each (segmented). NONE ignored.
 * If somehow empty after filtering but caller passed mixed unknowns → blue fallback unused here.
 */
private fun calendarStatusColors(highlights: Set<RowHighlight>, dark: Boolean): List<Color> {
    val ordered = listOf(RowHighlight.PAID, RowHighlight.OVERDUE, RowHighlight.SOON)
        .filter { it in highlights }
    if (ordered.isEmpty()) return emptyList()
    return ordered.map { highlightColor(it, dark) }
}

private fun highlightColor(highlight: RowHighlight, dark: Boolean): Color = when (highlight) {
    RowHighlight.PAID -> if (dark) RowPaidBgDark else StatusPaid
    RowHighlight.OVERDUE -> if (dark) RowOverdueBgDark else StatusOverdue
    RowHighlight.SOON -> if (dark) RowSoonBgDark else StatusSoon
    RowHighlight.NONE -> StatusMixedFallback
}
