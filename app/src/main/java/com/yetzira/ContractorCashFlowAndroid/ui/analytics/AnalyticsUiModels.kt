package com.yetzira.ContractorCashFlowAndroid.ui.analytics

import androidx.compose.ui.graphics.Color
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseCategory
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val DAY_IN_MILLIS = 86_400_000L

enum class AnalyticsPeriod(val days: Int?, val shortLabel: String) {
    WEEK(days = 7, shortLabel = "WEEK"),
    MONTH(days = 30, shortLabel = "30D"),
    QUARTER(days = 90, shortLabel = "90D"),
    YEAR(days = 365, shortLabel = "1Y"),
    ALL(days = null, shortLabel = "All")
}

data class AnalyticsUiState(
    val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.MONTH,
    val currency: CurrencyOption = CurrencyOption.ILS,
    val netBalance: Double = 0.0,
    val overdueAmount: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val monthlyTrend: List<MonthlyTrendPointUi> = emptyList(),
    val invoiceStatus: List<InvoiceStatusUi> = emptyList(),
    val expensesByCategory: List<ExpenseCategoryUi> = emptyList(),
    val budgetUtilization: List<ProjectBudgetUi> = emptyList(),
    val averageBudgetUtilization: Double = 0.0,
    val topProjects: List<TopProjectUi> = emptyList()
) {
    val hasDonutData: Boolean
        get() = totalIncome > 0.0 || totalExpenses > 0.0

    val showMonthlyTrend: Boolean
        get() = selectedPeriod != AnalyticsPeriod.WEEK

    val invoiceStatusTotal: Double
        get() = invoiceStatus.sumOf { it.amount }
}

data class MonthlyTrendPointUi(
    val label: String,
    val income: Double,
    val expenses: Double
)

data class InvoiceStatusUi(
    val label: String,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

data class ExpenseCategoryUi(
    val label: String,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

data class ProjectBudgetUi(
    val projectId: String,
    val projectName: String,
    val spent: Double,
    val remaining: Double,
    val budget: Double,
    val utilization: Double
)

data class TopProjectUi(
    val rank: Int,
    val projectId: String,
    val projectName: String,
    val clientName: String,
    val income: Double,
    val balanceDelta: Double
)

internal object AnalyticsCalculator {
    fun buildUiState(
        selectedPeriod: AnalyticsPeriod,
        currency: CurrencyOption,
        expenses: List<ExpenseEntity>,
        invoices: List<InvoiceEntity>,
        projects: List<ProjectEntity>,
        now: Long = System.currentTimeMillis()
    ): AnalyticsUiState {
        val range = selectedPeriod.toDateRange(now)
        val filteredExpenses = expenses.filter { expense -> range == null || expense.date in range }
        val filteredInvoices = invoices.filter { invoice -> range == null || invoice.createdDate in range }
        val overdueInvoices = invoices.filter { invoice ->
            !invoice.isPaid && invoice.dueDate < now && (range == null || invoice.dueDate in range)
        }

        val totalIncome = filteredInvoices.filter { it.isPaid }.sumOf { it.amount }
        val totalExpenses = filteredExpenses.sumOf { it.amount }
        val netBalance = totalIncome - totalExpenses
        val overdueAmount = overdueInvoices.sumOf { it.amount }

        val paidAmount = filteredInvoices.filter { it.isPaid }.sumOf { it.amount }
        val pendingAmount = filteredInvoices.filter { !it.isPaid && it.dueDate >= now }.sumOf { it.amount }
        val overdueStatusAmount = filteredInvoices.filter { !it.isPaid && it.dueDate < now }.sumOf { it.amount }
        val invoiceStatusTotal = paidAmount + pendingAmount + overdueStatusAmount

        val invoiceStatus = listOf(
            InvoiceStatusUi(
                label = "Paid",
                amount = paidAmount,
                percentage = percentageOf(paidAmount, invoiceStatusTotal),
                color = KablanProColors.IncomeGreen
            ),
            InvoiceStatusUi(
                label = "Pending",
                amount = pendingAmount,
                percentage = percentageOf(pendingAmount, invoiceStatusTotal),
                color = KablanProColors.PendingOrange
            ),
            InvoiceStatusUi(
                label = "Overdue",
                amount = overdueStatusAmount,
                percentage = percentageOf(overdueStatusAmount, invoiceStatusTotal),
                color = KablanProColors.ExpenseRed
            )
        )

        val expensesByCategory = filteredExpenses
            .groupBy { ExpenseCategory.fromString(it.category) }
            .mapNotNull { (category, items) ->
                category?.let {
                    val amount = items.sumOf { item -> item.amount }
                    ExpenseCategoryUi(
                        label = category.name.lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        amount = amount,
                        percentage = percentageOf(amount, totalExpenses),
                        color = Color(category.chartColor)
                    )
                }
            }
            .sortedByDescending { it.amount }

        val monthlyTrend = buildMonthlyTrend(
            selectedPeriod = selectedPeriod,
            filteredExpenses = filteredExpenses,
            filteredInvoices = filteredInvoices,
            now = now
        )

        val projectExpenseTotals = filteredExpenses
            .filter { !it.projectId.isNullOrBlank() }
            .groupBy { it.projectId!! }
            .mapValues { (_, items) -> items.sumOf { it.amount } }

        val projectIncomeTotals = filteredInvoices
            .filter { it.isPaid && !it.projectId.isNullOrBlank() }
            .groupBy { it.projectId!! }
            .mapValues { (_, items) -> items.sumOf { it.amount } }

        val budgetUtilization = projects
            .filter { it.budget > 0.0 }
            .map { project ->
                val spent = projectExpenseTotals[project.id] ?: 0.0
                val remaining = (project.budget - spent).coerceAtLeast(0.0)
                val utilization = if (project.budget > 0.0) (spent / project.budget) * 100.0 else 0.0
                ProjectBudgetUi(
                    projectId = project.id,
                    projectName = project.name,
                    spent = spent,
                    remaining = remaining,
                    budget = project.budget,
                    utilization = utilization
                )
            }
            .sortedByDescending { it.utilization }
            .take(10)

        val averageBudgetUtilization = budgetUtilization
            .map { it.utilization }
            .average()
            .takeUnless { it.isNaN() }
            ?: 0.0

        val topProjects = projects
            .map { project ->
                val income = projectIncomeTotals[project.id] ?: 0.0
                val spent = projectExpenseTotals[project.id] ?: 0.0
                Triple(project, income, income - spent)
            }
            .filter { (_, income, balance) -> income > 0.0 || balance != 0.0 }
            .sortedByDescending { (_, income, _) -> income }
            .take(5)
            .mapIndexed { index, (project, income, balanceDelta) ->
                TopProjectUi(
                    rank = index + 1,
                    projectId = project.id,
                    projectName = project.name,
                    clientName = project.clientName,
                    income = income,
                    balanceDelta = balanceDelta
                )
            }

        return AnalyticsUiState(
            selectedPeriod = selectedPeriod,
            currency = currency,
            netBalance = netBalance,
            overdueAmount = overdueAmount,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            monthlyTrend = monthlyTrend,
            invoiceStatus = invoiceStatus,
            expensesByCategory = expensesByCategory,
            budgetUtilization = budgetUtilization,
            averageBudgetUtilization = averageBudgetUtilization,
            topProjects = topProjects
        )
    }

    private fun buildMonthlyTrend(
        selectedPeriod: AnalyticsPeriod,
        filteredExpenses: List<ExpenseEntity>,
        filteredInvoices: List<InvoiceEntity>,
        now: Long
    ): List<MonthlyTrendPointUi> {
        if (selectedPeriod == AnalyticsPeriod.WEEK) return emptyList()
        if (filteredExpenses.isEmpty() && filteredInvoices.none { it.isPaid }) return emptyList()

        val paidInvoices = filteredInvoices.filter { it.isPaid }
        val allTimestamps = buildList {
            addAll(filteredExpenses.map { it.date })
            addAll(paidInvoices.map { it.createdDate })
            add(now)
        }
        val startMonth = when (selectedPeriod) {
            AnalyticsPeriod.ALL -> monthStartOf(allTimestamps.minOrNull() ?: now)
            else -> monthStartOf(selectedPeriod.toDateRange(now)?.first ?: now)
        }
        val endMonth = monthStartOf(now)
        val monthFormatter = SimpleDateFormat("MMM yy", Locale.getDefault())

        val months = generateMonthStarts(startMonth, endMonth)
        return months.map { monthStart ->
            val monthEnd = monthEndOf(monthStart)
            MonthlyTrendPointUi(
                label = monthFormatter.format(Date(monthStart)),
                income = paidInvoices
                    .filter { it.createdDate in monthStart..monthEnd }
                    .sumOf { it.amount },
                expenses = filteredExpenses
                    .filter { it.date in monthStart..monthEnd }
                    .sumOf { it.amount }
            )
        }
    }

    private fun generateMonthStarts(startMonth: Long, endMonth: Long): List<Long> {
        val calendar = Calendar.getInstance().apply { timeInMillis = startMonth }
        return buildList {
            while (calendar.timeInMillis <= endMonth) {
                add(calendar.timeInMillis)
                calendar.add(Calendar.MONTH, 1)
            }
        }
    }

    private fun monthStartOf(timestamp: Long): Long = Calendar.getInstance().run {
        timeInMillis = timestamp
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        timeInMillis
    }

    private fun monthEndOf(monthStart: Long): Long = Calendar.getInstance().run {
        timeInMillis = monthStart
        add(Calendar.MONTH, 1)
        add(Calendar.MILLISECOND, -1)
        timeInMillis
    }

    private fun percentageOf(amount: Double, total: Double): Float {
        if (amount <= 0.0 || total <= 0.0) return 0f
        return ((amount / total) * 100.0).toFloat()
    }
}

private fun AnalyticsPeriod.toDateRange(now: Long): LongRange? {
    val periodDays = days ?: return null
    return (now - (periodDays * DAY_IN_MILLIS))..now
}

