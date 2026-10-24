package ru.obroki.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import ru.obroki.app.domain.PaymentItem
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
                    • «Сумма к оплате» — сколько осталось заплатить за выбранный период (включая будущие месяцы).
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
    amount: Double,
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
            Text(ru.obroki.app.domain.SumToPay.formatMoney(amount))
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        },
    )
}
