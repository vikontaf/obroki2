package ru.obroki.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.obroki.app.domain.PaymentItem
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
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = payee,
                    onValueChange = { payee = it },
                    label = { Text("Получатель") },
                    singleLine = true,
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Инструкция") },
        text = {
            Text(
                """
                • Короткое нажатие — отметить оплату. Появится платёж на следующий месяц (если нет дубликата и остаток к оплате не исчерпан).
                • Повторное нажатие на уже отмеченный (оплаченный) пункт возвращает его в неоплаченные; уже созданный платёж на следующий месяц при этом не удаляется.
                • Долгое нажатие — изменить сумму / остаток к оплате или удалить.
                • Календарь — текущий месяц: сегодня и дни с платежами (цвета как в списке).
                • «Сумма к оплате» — сколько осталось заплатить за выбранный период (включая будущие месяцы).
                • Очистка — удаляет оплаченные платежи прошлых месяцев.
                • Цвета: зелёный — оплачен, красный — просрочен, жёлтый — меньше 7 дней.
                """.trimIndent()
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Понятно") }
        },
    )
}

@Composable
fun DonateStubDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Благодарность разработчику") },
        text = {
            Text("Спасибо за интерес к приложению «Оброки2»! Ссылка на донат появится в одной из следующих версий.")
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
        title = { Text(title) },
        text = {
            Text("К оплате: ${ru.obroki.app.domain.SumToPay.formatMoney(amount)}")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        },
    )
}
