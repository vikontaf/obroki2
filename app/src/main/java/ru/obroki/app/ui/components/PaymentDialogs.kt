package ru.obroki.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.obroki.app.R
import ru.obroki.app.domain.IncomeReceipt
import ru.obroki.app.domain.PaymentItem
import ru.obroki.app.domain.PeriodTotals
import ru.obroki.app.domain.SumToPay
import ru.obroki.app.domain.TextFormat
import java.time.YearMonth

@Composable
fun CreatePaymentDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, payee: String, amount: Double, debt: Double?, dueDay: Int) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var payee by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var debtText by remember { mutableStateOf("") }
    var dueDayText by remember { mutableStateOf(YearMonth.now().atEndOfMonth().dayOfMonth.coerceAtMost(28).toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый платёж") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = TextFormat.capitalizeFirstWord(it) },
                    label = { Text("Название") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = payee,
                    onValueChange = { payee = TextFormat.capitalizeFirstWord(it) },
                    label = { Text("Получатель") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Сумма") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = debtText,
                    onValueChange = { debtText = it },
                    label = { Text("Остаток к оплате (необязательно)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = dueDayText,
                    onValueChange = { dueDayText = it },
                    label = { Text("День месяца (1–31)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error!!, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountText.replace(',', '.').toDoubleOrNull()
                val debt = debtText.trim().takeIf { it.isNotEmpty() }?.replace(',', '.')?.toDoubleOrNull()
                val day = dueDayText.toIntOrNull()
                when {
                    title.isBlank() -> error = "Укажите название"
                    payee.isBlank() -> error = "Укажите получателя"
                    amount == null || amount <= 0 -> error = "Сумма должна быть больше 0"
                    debtText.isNotBlank() && (debt == null || debt < 0) -> error = "Некорректный остаток к оплате"
                    day == null || day !in 1..31 -> error = "День месяца: 1–31"
                    else -> onSave(title, payee, amount, debt, day)
                }
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}

@Composable
fun EditPaymentDialog(
    item: PaymentItem,
    onDismiss: () -> Unit,
    onSave: (amount: Double, debt: Double?) -> Unit,
) {
    var amountText by remember { mutableStateOf(item.amount.toString()) }
    var debtText by remember { mutableStateOf(item.debt?.toString().orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать") },
        text = {
            Column {
                Text("${item.title} · ${item.payee}")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Сумма") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = debtText,
                    onValueChange = { debtText = it },
                    label = { Text("Остаток к оплате (пусто = не задан)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error!!, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountText.replace(',', '.').toDoubleOrNull()
                val debtRaw = debtText.trim()
                val debt = if (debtRaw.isEmpty()) null else debtRaw.replace(',', '.').toDoubleOrNull()
                when {
                    amount == null || amount <= 0 -> error = "Сумма должна быть больше 0"
                    debtRaw.isNotEmpty() && (debt == null || debt < 0) -> error = "Некорректный остаток к оплате"
                    else -> onSave(amount, debt)
                }
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}

@Composable
fun ItemActionsDialog(
    item: PaymentItem,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.title) },
        text = { Text("Что сделать с платежом?") },
        confirmButton = {
            TextButton(onClick = onEdit) { Text("Редактировать") }
        },
        dismissButton = {
            Column {
                TextButton(onClick = onDelete) { Text("Удалить") }
                TextButton(onClick = onDismiss) { Text("Отмена") }
            }
        },
    )
}

@Composable
fun ConfirmDeleteDialog(
    item: PaymentItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить платёж?") },
        text = { Text("«${item.title}» будет удалён без возможности восстановления.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Удалить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}

@Composable
fun TutorialDialog(onDismiss: () -> Unit) {
    val scroll = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Инструкция") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(scroll),
            ) {
                Text(
                    """
                    • Короткое нажатие — отметить оплату. Появится платёж на следующий месяц (если нет дубликата и остаток к оплате не исчерпан).
                    • Повторное нажатие на уже отмеченный (оплаченный) пункт возвращает его в неоплаченные; уже созданный платёж на следующий месяц при этом не удаляется.
                    • Долгое нажатие — изменить сумму / остаток к оплате или удалить.
                    • Календарь — месяцы вперёд/назад: сегодня и дни с платежами, в том числе запланированные по серии (цвета как в списке).
                    • «Сумма к оплате» — расходы, доход за период, остаток средств и итог (включая будущие месяцы).
                    • Остаток средств в шапке — нажмите, чтобы изменить; учитывается в итоге «Сумма к оплате».
                    • «Установить ежемесячный доход» — дни месяца и суммы поступлений для расчёта дохода за период.
                    • Очистка — удаляет оплаченные платежи прошлых месяцев.
                    • Цвета: зелёный — оплачен, красный — просрочен, жёлтый — меньше 7 дней.
                    """.trimIndent()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Понятно") }
        },
    )
}

private const val DONATE_URL = "https://pay.cloudtips.ru/p/0ee47c60"

@Composable
fun DonateStubDialog(onDismiss: () -> Unit) {
    val scroll = rememberScrollState()
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Благодарность разработчику") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(scroll),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.author_name),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                )
                Image(
                    painter = painterResource(R.drawable.donate_qr),
                    contentDescription = "QR-код для доната",
                    modifier = Modifier
                        .size(220.dp)
                        .padding(vertical = 8.dp),
                    contentScale = ContentScale.Fit,
                )
                Text(
                    text = "Можно оплатить по QR-коду либо просто нажать ссылку:",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { uriHandler.openUri(DONATE_URL) }) {
                    Text(
                        text = DONATE_URL,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        },
    )
}

@Composable
fun SumResultDialog(
    title: String,
    totals: PeriodTotals,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                SumLine(label = "Расходы", value = totals.expenses)
                Spacer(Modifier.height(6.dp))
                SumLine(label = "Доход", value = totals.income)
                Spacer(Modifier.height(6.dp))
                SumLine(label = "Остаток средств", value = totals.balance)
                Spacer(Modifier.height(10.dp))
                SumLine(label = "Итог", value = totals.total, emphasize = true)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        },
    )
}

@Composable
private fun SumLine(label: String, value: Double, emphasize: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = SumToPay.formatMoney(value),
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun EditBalanceDialog(
    currentBalance: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit,
) {
    var balanceText by remember {
        mutableStateOf(
            if (currentBalance == currentBalance.toLong().toDouble()) {
                currentBalance.toLong().toString()
            } else {
                currentBalance.toString()
            },
        )
    }
    var error by remember { mutableStateOf<String?>(null) }

    fun applyDelta(delta: Double) {
        val current = balanceText.replace(',', '.').toDoubleOrNull() ?: 0.0
        val next = current + delta
        balanceText = if (next == next.toLong().toDouble()) {
            next.toLong().toString()
        } else {
            next.toString()
        }
        error = null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Остаток средств") },
        text = {
            Column {
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = {
                        balanceText = it
                        error = null
                    },
                    label = { Text("Сумма") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TextButton(onClick = { applyDelta(1000.0) }) { Text("+1000") }
                    TextButton(onClick = { applyDelta(-1000.0) }) { Text("-1000") }
                    TextButton(onClick = { applyDelta(5000.0) }) { Text("+5000") }
                    TextButton(onClick = { applyDelta(-5000.0) }) { Text("-5000") }
                }
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val value = balanceText.replace(',', '.').toDoubleOrNull()
                if (value == null) {
                    error = "Введите число"
                } else {
                    onSave(value)
                }
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}

@Composable
fun MonthlyIncomeDialog(
    initial: List<IncomeReceipt>,
    onDismiss: () -> Unit,
    onSave: (List<IncomeReceipt>) -> Unit,
) {
    data class DraftRow(val dayText: String, val amountText: String)

    var rows by remember {
        mutableStateOf(
            if (initial.isEmpty()) {
                listOf(DraftRow("", ""))
            } else {
                initial.map {
                    DraftRow(
                        dayText = it.dayOfMonth.toString(),
                        amountText = if (it.amount == it.amount.toLong().toDouble()) {
                            it.amount.toLong().toString()
                        } else {
                            it.amount.toString()
                        },
                    )
                }
            },
        )
    }
    var error by remember { mutableStateOf<String?>(null) }
    val scroll = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ежемесячный доход") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(scroll),
            ) {
                Text(
                    text = "Укажите день месяца и сумму поступления. Можно несколько строк.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                rows.forEachIndexed { index, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = row.dayText,
                            onValueChange = { value ->
                                rows = rows.toMutableList().also {
                                    it[index] = row.copy(dayText = value)
                                }
                                error = null
                            },
                            label = { Text("День") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(88.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = row.amountText,
                            onValueChange = { value ->
                                rows = rows.toMutableList().also {
                                    it[index] = row.copy(amountText = value)
                                }
                                error = null
                            },
                            label = { Text("Сумма") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = {
                                rows = rows.toMutableList().also { it.removeAt(index) }
                                    .ifEmpty { listOf(DraftRow("", "")) }
                                error = null
                            },
                        ) { Text("×") }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                TextButton(
                    onClick = {
                        rows = rows + DraftRow("", "")
                        error = null
                    },
                ) { Text("Добавить") }
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsed = mutableListOf<IncomeReceipt>()
                for (row in rows) {
                    val dayBlank = row.dayText.isBlank()
                    val amountBlank = row.amountText.isBlank()
                    if (dayBlank && amountBlank) continue
                    val day = row.dayText.toIntOrNull()
                    val amount = row.amountText.replace(',', '.').toDoubleOrNull()
                    when {
                        day == null || day !in 1..31 -> {
                            error = "День месяца: 1–31"
                            return@TextButton
                        }
                        amount == null || amount <= 0.0 -> {
                            error = "Сумма должна быть больше 0"
                            return@TextButton
                        }
                        else -> parsed.add(IncomeReceipt(dayOfMonth = day, amount = amount))
                    }
                }
                onSave(parsed.sortedBy { it.dayOfMonth })
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}
