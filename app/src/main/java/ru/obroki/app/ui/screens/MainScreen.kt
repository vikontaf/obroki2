package ru.obroki.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.obroki.app.domain.PaymentItem
import ru.obroki.app.domain.SumPeriodKind
import ru.obroki.app.ui.PaymentsViewModel
import ru.obroki.app.ui.components.ConfirmDeleteDialog
import ru.obroki.app.ui.components.CreatePaymentDialog
import ru.obroki.app.ui.components.DonateStubDialog
import ru.obroki.app.ui.components.EditPaymentDialog
import ru.obroki.app.ui.components.ItemActionsDialog
import ru.obroki.app.ui.components.MonthCalendarSheet
import ru.obroki.app.ui.components.PaymentRow
import ru.obroki.app.ui.components.SumResultDialog
import ru.obroki.app.ui.components.TutorialDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private sealed class DialogState {
    data object None : DialogState()
    data object Create : DialogState()
    data object Calendar : DialogState()
    data object Tutorial : DialogState()
    data object Donate : DialogState()
    data object CustomDatePicker : DialogState()
    data class ItemActions(val item: PaymentItem) : DialogState()
    data class Edit(val item: PaymentItem) : DialogState()
    data class ConfirmDelete(val item: PaymentItem) : DialogState()
    data class SumResult(val title: String, val amount: Double) : DialogState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: PaymentsViewModel) {
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    var dialog by remember { mutableStateOf<DialogState>(DialogState.None) }
    var appMenuOpen by remember { mutableStateOf(false) }
    var sumMenuOpen by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }
    val today = LocalDate.now()

    LaunchedEffect(message) {
        val msg = message ?: return@LaunchedEffect
        snackbar.showSnackbar(msg.text)
        viewModel.clearMessage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Оброки") },
                actions = {
                    IconButton(onClick = { dialog = DialogState.Calendar }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "Календарь")
                    }
                    Box {
                        IconButton(onClick = { sumMenuOpen = true }) {
                            Icon(Icons.Filled.Payments, contentDescription = "Сумма к оплате")
                        }
                        DropdownMenu(
                            expanded = sumMenuOpen,
                            onDismissRequest = { sumMenuOpen = false },
                        ) {
                            Text(
                                text = "Сумма к оплате",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Ближайшая неделя") },
                                onClick = {
                                    sumMenuOpen = false
                                    val amount = viewModel.sumForPeriod(SumPeriodKind.NEXT_WEEK)
                                    dialog = DialogState.SumResult("Ближайшая неделя", amount)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Ближайший месяц") },
                                onClick = {
                                    sumMenuOpen = false
                                    val amount = viewModel.sumForPeriod(SumPeriodKind.NEXT_MONTH)
                                    dialog = DialogState.SumResult("Ближайший месяц", amount)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("До конца текущей недели") },
                                onClick = {
                                    sumMenuOpen = false
                                    val amount = viewModel.sumForPeriod(SumPeriodKind.UNTIL_WEEK_END)
                                    dialog = DialogState.SumResult("До конца текущей недели", amount)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("До конца текущего месяца") },
                                onClick = {
                                    sumMenuOpen = false
                                    val amount = viewModel.sumForPeriod(SumPeriodKind.UNTIL_MONTH_END)
                                    dialog = DialogState.SumResult("До конца текущего месяца", amount)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("До произвольной даты") },
                                onClick = {
                                    sumMenuOpen = false
                                    dialog = DialogState.CustomDatePicker
                                },
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { appMenuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Меню")
                        }
                        DropdownMenu(
                            expanded = appMenuOpen,
                            onDismissRequest = { appMenuOpen = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Создать платёж") },
                                onClick = {
                                    appMenuOpen = false
                                    dialog = DialogState.Create
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Очистить погашенные") },
                                onClick = {
                                    appMenuOpen = false
                                    viewModel.clearPaid()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Функции приложения") },
                                onClick = {
                                    appMenuOpen = false
                                    dialog = DialogState.Tutorial
                                },
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Сменить тему, сейчас — ${PaymentsViewModel.themeLabel(themeMode)}"
                                    )
                                },
                                onClick = {
                                    appMenuOpen = false
                                    viewModel.cycleTheme()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Благодарность разработчику") },
                                onClick = {
                                    appMenuOpen = false
                                    dialog = DialogState.Donate
                                },
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (payments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Пока нет платежей.\nОткройте меню → «Создать платёж».",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                items(payments, key = { it.id }) { item ->
                    PaymentRow(
                        item = item,
                        today = today,
                        onTap = { viewModel.togglePaid(item) },
                        onLongPress = { dialog = DialogState.ItemActions(item) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    when (val d = dialog) {
        DialogState.None -> Unit
        DialogState.Create -> CreatePaymentDialog(
            onDismiss = { dialog = DialogState.None },
            onSave = { title, payee, amount, debt, dueDay ->
                viewModel.createPayment(title, payee, amount, debt, dueDay)
                dialog = DialogState.None
            },
        )
        DialogState.Calendar -> MonthCalendarSheet(
            paymentDays = viewModel.paymentDaysInCurrentMonth(),
            today = today,
            onDismiss = { dialog = DialogState.None },
        )
        DialogState.Tutorial -> TutorialDialog(onDismiss = { dialog = DialogState.None })
        DialogState.Donate -> DonateStubDialog(onDismiss = { dialog = DialogState.None })
        is DialogState.ItemActions -> ItemActionsDialog(
            item = d.item,
            onDismiss = { dialog = DialogState.None },
            onEdit = { dialog = DialogState.Edit(d.item) },
            onDelete = { dialog = DialogState.ConfirmDelete(d.item) },
        )
        is DialogState.Edit -> EditPaymentDialog(
            item = d.item,
            onDismiss = { dialog = DialogState.None },
            onSave = { amount, debt ->
                viewModel.updatePayment(d.item, amount, debt)
                dialog = DialogState.None
            },
        )
        is DialogState.ConfirmDelete -> ConfirmDeleteDialog(
            item = d.item,
            onDismiss = { dialog = DialogState.None },
            onConfirm = {
                viewModel.deletePayment(d.item)
                dialog = DialogState.None
            },
        )
        is DialogState.SumResult -> SumResultDialog(
            title = d.title,
            amount = d.amount,
            onDismiss = { dialog = DialogState.None },
        )
        DialogState.CustomDatePicker -> {
            val pickerState = rememberDatePickerState(
                initialSelectedDateMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            )
            DatePickerDialog(
                onDismissRequest = { dialog = DialogState.None },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            val millis = pickerState.selectedDateMillis
                            if (millis != null) {
                                val selected = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                                val amount = viewModel.sumForPeriod(
                                    SumPeriodKind.UNTIL_CUSTOM_DATE,
                                    selected,
                                )
                                dialog = DialogState.SumResult(
                                    "До ${selected}",
                                    amount,
                                )
                            } else {
                                dialog = DialogState.None
                            }
                        },
                    ) { Text("Выбрать") }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { dialog = DialogState.None },
                    ) { Text("Отмена") }
                },
            ) {
                DatePicker(state = pickerState)
            }
        }
    }
}
