package ru.obroki.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.obroki.app.data.AppDatabase
import ru.obroki.app.data.PaymentRepository
import ru.obroki.app.data.ThemePreferences
import ru.obroki.app.domain.AppThemeMode
import ru.obroki.app.domain.PaymentDates
import ru.obroki.app.domain.PaymentItem
import ru.obroki.app.domain.SumPeriodKind
import ru.obroki.app.domain.SumToPay
import java.time.LocalDate
import java.time.YearMonth

data class UiMessage(val text: String)

class PaymentsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.get(application)
    private val repo = PaymentRepository(db.paymentDao())
    private val themePrefs = ThemePreferences(application)

    val payments: StateFlow<List<PaymentItem>> = repo.observePayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val themeMode: StateFlow<AppThemeMode> = themePrefs.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppThemeMode.SYSTEM)

    private val _message = MutableStateFlow<UiMessage?>(null)
    val message: StateFlow<UiMessage?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun createPayment(
        title: String,
        payee: String,
        amount: Double,
        debt: Double?,
        dueDay: Int,
        year: Int = YearMonth.now().year,
        month: Int = YearMonth.now().monthValue,
    ) {
        viewModelScope.launch {
            repo.create(
                PaymentItem(
                    title = title.trim(),
                    payee = payee.trim(),
                    amount = amount,
                    debt = debt,
                    dueDay = dueDay.coerceIn(1, 31),
                    dueYear = year,
                    dueMonth = month,
                    isPaid = false,
                )
            )
        }
    }

    fun updatePayment(item: PaymentItem, amount: Double, debt: Double?) {
        viewModelScope.launch {
            repo.update(item.copy(amount = amount, debt = debt))
        }
    }

    fun deletePayment(item: PaymentItem) {
        viewModelScope.launch { repo.delete(item) }
    }

    fun togglePaid(item: PaymentItem) {
        viewModelScope.launch { repo.togglePaid(item) }
    }

    fun clearPaid() {
        viewModelScope.launch {
            repo.clearPaidExceptCurrentMonth()
            _message.value = UiMessage("Погашенные платежи прошлых месяцев удалены")
        }
    }

    fun cycleTheme() {
        viewModelScope.launch {
            val current = themeMode.value
            val next = themePrefs.cycleTheme(current)
            _message.value = UiMessage("Тема: ${themeLabel(next)}")
        }
    }

    fun sumForPeriod(kind: SumPeriodKind, customTo: LocalDate? = null): Double {
        val today = LocalDate.now()
        val (from, to) = when (kind) {
            SumPeriodKind.NEXT_WEEK -> PaymentDates.periodNextWeek(today)
            SumPeriodKind.NEXT_MONTH -> PaymentDates.periodNextMonth(today)
            SumPeriodKind.UNTIL_WEEK_END -> PaymentDates.periodUntilWeekEnd(today)
            SumPeriodKind.UNTIL_MONTH_END -> PaymentDates.periodUntilMonthEnd(today)
            SumPeriodKind.UNTIL_CUSTOM_DATE -> {
                val end = customTo ?: today
                PaymentDates.periodUntilCustom(today, end)
            }
        }
        return SumToPay.calculate(payments.value, from, to)
    }

    fun paymentDaysInCurrentMonth(): Set<Int> {
        val ym = YearMonth.now()
        return payments.value
            .filter { it.dueYear == ym.year && it.dueMonth == ym.monthValue }
            .map { it.effectiveDate.dayOfMonth }
            .toSet()
    }

    companion object {
        fun themeLabel(mode: AppThemeMode): String = when (mode) {
            AppThemeMode.DARK -> "тёмная"
            AppThemeMode.LIGHT -> "светлая"
            AppThemeMode.SYSTEM -> "системная"
        }
    }
}
