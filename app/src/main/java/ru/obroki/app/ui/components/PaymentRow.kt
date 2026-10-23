package ru.obroki.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.obroki.app.domain.PaymentDates
import ru.obroki.app.domain.PaymentItem
import ru.obroki.app.domain.RowHighlight
import ru.obroki.app.domain.SumToPay
import ru.obroki.app.ui.theme.RowOverdueBg
import ru.obroki.app.ui.theme.RowOverdueBgDark
import ru.obroki.app.ui.theme.RowPaidBg
import ru.obroki.app.ui.theme.RowPaidBgDark
import ru.obroki.app.ui.theme.RowSoonBg
import ru.obroki.app.ui.theme.RowSoonBgDark
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFmt = DateTimeFormatter.ofPattern("d MMM", Locale("ru"))

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PaymentRow(
    item: PaymentItem,
    today: LocalDate,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val highlight = PaymentDates.rowHighlight(item, today)
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bg = when (highlight) {
        RowHighlight.PAID -> if (dark) RowPaidBgDark else RowPaidBg
        RowHighlight.OVERDUE -> if (dark) RowOverdueBgDark else RowOverdueBg
        RowHighlight.SOON -> if (dark) RowSoonBgDark else RowSoonBg
        RowHighlight.NONE -> Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bg)
            .combinedClickable(onClick = onTap, onLongClick = onLongPress)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = if (item.isPaid) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = if (item.isPaid) "Оплачен" else "Не оплачен",
            tint = if (item.isPaid) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.payee,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val debtPart = item.debt?.let { " · остаток к оплате ${SumToPay.formatMoney(it)}" }.orEmpty()
            Text(
                text = "${item.effectiveDate.format(dateFmt)}$debtPart",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = SumToPay.formatMoney(item.amount),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
