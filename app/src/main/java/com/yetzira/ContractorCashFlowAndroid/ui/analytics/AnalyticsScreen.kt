package com.yetzira.ContractorCashFlowAndroid.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.ui.components.AnalyticsCard
import com.yetzira.ContractorCashFlowAndroid.ui.components.AnalyticsPeriodPicker
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProLayoutDefaults
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProColors
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            .padding(top = KablanProLayoutDefaults.TopSectionSpacing),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnalyticsPeriodPicker(
            options = AnalyticsPeriod.entries.toList(),
            selectedOption = state.selectedPeriod,
            onOptionSelected = viewModel::setSelectedPeriod,
            optionLabel = { stringResource(it.chipLabelResId) },
            modifier = Modifier.fillMaxWidth()
        )

        KpiRow(state = state)
        IncomeExpenseDonutCard(state = state)

        if (state.showMonthlyTrend) {
            MonthlyTrendCard(state = state)
        }

        InvoiceStatusCard(state = state)
        ExpensesByCategoryCard(state = state)
        if (state.hasBudgetData) {
            BudgetUtilizationCard(state = state)
        }
        TopProjectsCard(state = state)
    }
}

@Composable
private fun KpiRow(state: AnalyticsUiState) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isTablet = maxWidth >= 600.dp
        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyticsMetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.analytics_income),
                    value = formatCurrency(state.totalIncome, state.currency),
                    accentColor = KablanProColors.IncomeGreen,
                    icon = Icons.AutoMirrored.Filled.TrendingDown
                )
                AnalyticsMetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.analytics_expenses),
                    value = formatCurrency(state.totalExpenses, state.currency),
                    accentColor = KablanProColors.ExpenseRed,
                    icon = Icons.AutoMirrored.Filled.TrendingUp
                )
                AnalyticsMetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.analytics_net_balance),
                    value = formatCurrency(state.netBalance, state.currency),
                    accentColor = if (state.netBalance >= 0.0) KablanProColors.IncomeGreen else KablanProColors.ExpenseRed,
                    icon = Icons.Default.AccountBalance
                )
                AnalyticsMetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.analytics_overdue),
                    value = formatCurrency(state.overdueAmount, state.currency),
                    accentColor = if (state.overdueAmount > 0.0) KablanProColors.ExpenseRed else MaterialTheme.colorScheme.onSurface,
                    icon = Icons.Default.Warning
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyticsMetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.analytics_net_balance),
                    value = formatCurrency(state.netBalance, state.currency),
                    accentColor = if (state.netBalance >= 0.0) KablanProColors.IncomeGreen else KablanProColors.ExpenseRed,
                    icon = Icons.Default.AccountBalance
                )
                AnalyticsMetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.analytics_overdue),
                    value = formatCurrency(state.overdueAmount, state.currency),
                    accentColor = if (state.overdueAmount > 0.0) KablanProColors.ExpenseRed else MaterialTheme.colorScheme.onSurface,
                    icon = Icons.Default.Warning
                )
            }
        }
    }
}

@Composable
private fun AnalyticsMetricCard(
    title: String,
    value: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    AnalyticsCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
    }
}

@Composable
private fun IncomeExpenseDonutCard(state: AnalyticsUiState) {
    AnalyticsCard {
        SectionTitle(title = stringResource(R.string.analytics_income_vs_expenses))
        if (!state.hasDonutData) {
            EmptySection(
                message = stringResource(R.string.analytics_empty_income_expenses),
                icon = Icons.Default.PieChart
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChart(
                        income = state.totalIncome,
                        expenses = state.totalExpenses,
                        modifier = Modifier.fillMaxSize()
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.analytics_net_balance),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatCurrency(state.netBalance, state.currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (state.netBalance >= 0.0) KablanProColors.IncomeGreen else KablanProColors.ExpenseRed
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LegendRow(
                        label = stringResource(R.string.analytics_income),
                        value = formatCurrency(state.totalIncome, state.currency),
                        color = KablanProColors.IncomeGreen
                    )
                    LegendRow(
                        label = stringResource(R.string.analytics_expenses),
                        value = formatCurrency(state.totalExpenses, state.currency),
                        color = KablanProColors.ExpenseRed
                    )
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    income: Double,
    expenses: Double,
    modifier: Modifier = Modifier
) {
    val total = income + expenses
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = modifier.padding(8.dp)) {
        val strokeWidth = size.minDimension * 0.16f
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        drawArc(
            color = backgroundColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        if (total > 0.0) {
            var startAngle = -90f
            listOf(
                income to KablanProColors.IncomeGreen,
                expenses to KablanProColors.ExpenseRed
            ).forEach { (value, color) ->
                if (value > 0.0) {
                    val sweep = ((value / total) * 360.0).toFloat()
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweep
                }
            }
        }
    }
}

@Composable
private fun MonthlyTrendCard(state: AnalyticsUiState) {
    AnalyticsCard {
        SectionTitle(title = stringResource(R.string.analytics_monthly_trend))
        if (state.monthlyTrend.isEmpty()) {
            EmptySection(
                message = stringResource(R.string.analytics_empty_monthly_trend),
                icon = Icons.AutoMirrored.Filled.ShowChart
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                VicoMonthlyTrendChart(
                    points = state.monthlyTrend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    state.monthlyTrend.forEach { point ->
                        Text(
                            text = point.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendRow(
                    label = stringResource(R.string.analytics_income),
                    value = formatCurrency(state.totalIncome, state.currency),
                    color = KablanProColors.IncomeGreen,
                    modifier = Modifier.weight(1f)
                )
                LegendRow(
                    label = stringResource(R.string.analytics_expenses),
                    value = formatCurrency(state.totalExpenses, state.currency),
                    color = KablanProColors.ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InvoiceStatusCard(state: AnalyticsUiState) {
    AnalyticsCard {
        SectionTitle(title = stringResource(R.string.analytics_invoice_status))

        if (state.invoiceStatusTotal <= 0.0) {
            EmptySection(
                message = stringResource(R.string.analytics_empty_invoices),
                icon = Icons.Default.Description
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .padding(top = 6.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
            ) {
                state.invoiceStatus.forEachIndexed { index, segment ->
                    if (segment.amount > 0.0) {
                        Box(
                            modifier = Modifier
                                .weight(segment.amount.toFloat())
                                .fillMaxSize()
                                .background(
                                    color = segment.color,
                                    shape = when (index) {
                                        0 -> RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
                                        state.invoiceStatus.lastIndex -> RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)
                                        else -> RoundedCornerShape(0.dp)
                                    }
                                )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.invoiceStatus.forEach { segment ->
                    LegendRow(
                        label = stringResource(segment.labelResId),
                        value = "${formatCurrency(segment.amount, state.currency)} • ${formatPercent(segment.percentage)}",
                        color = segment.color
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpensesByCategoryCard(state: AnalyticsUiState) {
    AnalyticsCard {
        SectionTitle(title = stringResource(R.string.analytics_expenses_by_category))
        if (state.expensesByCategory.isEmpty()) {
            EmptySection(
                message = stringResource(R.string.analytics_empty_categories),
                icon = Icons.Default.BarChart
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val maxAmount = state.expensesByCategory.maxOfOrNull { it.amount } ?: 1.0
                state.expensesByCategory.forEach { category ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(category.labelResId),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = formatCurrency(category.amount, state.currency),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HorizontalValueBar(
                            fillFraction = (category.amount / maxAmount).toFloat(),
                            color = category.color,
                            trailingLabel = formatPercent(category.percentage)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalValueBar(
    fillFraction: Float,
    color: Color,
    trailingLabel: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(7.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fillFraction.coerceIn(0f, 1f))
                    .height(14.dp)
                    .background(color, RoundedCornerShape(7.dp))
            )
        }
        Text(
            text = trailingLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BudgetUtilizationCard(state: AnalyticsUiState) {
    val averageColor = budgetUtilizationColor(state.averageBudgetUtilization)

    AnalyticsCard {
        SectionTitle(title = stringResource(R.string.analytics_budget_utilization))
        Text(
            text = stringResource(
                R.string.analytics_average_utilization,
                String.format(Locale.getDefault(), "%.0f%%", state.averageBudgetUtilization)
            ),
            style = MaterialTheme.typography.titleSmall,
            color = averageColor,
            fontWeight = FontWeight.SemiBold
        )

        Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.budgetUtilization.forEach { item ->
                BudgetProjectRow(item = item, currency = state.currency)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BudgetLegendItem(
                color = KablanProColors.BudgetBlue,
                label = stringResource(R.string.analytics_budget_under_80)
            )
            BudgetLegendItem(
                color = KablanProColors.PendingOrange,
                label = stringResource(R.string.analytics_budget_80_to_100)
            )
            BudgetLegendItem(
                color = KablanProColors.ExpenseRed,
                label = stringResource(R.string.analytics_budget_over_100)
            )
        }
    }
}

@Composable
private fun BudgetLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BudgetProjectRow(item: ProjectBudgetUi, currency: CurrencyOption) {
    val barColor = budgetUtilizationColor(item.utilization)
    val fillFraction = if (item.budget > 0.0) {
        (item.spent / item.budget).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.projectName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format(Locale.getDefault(), "%.0f%%", item.utilization),
                style = MaterialTheme.typography.labelLarge,
                color = barColor
            )
        }

        HorizontalValueBar(
            fillFraction = fillFraction,
            color = barColor,
            trailingLabel = formatCurrency(item.spent, currency)
        )
    }
}

private fun budgetUtilizationColor(utilization: Double): Color = when {
    utilization >= 100.0 -> KablanProColors.ExpenseRed
    utilization >= 80.0 -> KablanProColors.PendingOrange
    else -> KablanProColors.BudgetBlue
}

@Composable
private fun TopProjectsCard(state: AnalyticsUiState) {
    AnalyticsCard {
        SectionTitle(title = stringResource(R.string.analytics_top_projects))
        if (state.topProjects.isEmpty()) {
            EmptySection(
                message = stringResource(R.string.analytics_empty_top_projects),
                icon = Icons.Default.Folder
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.topProjects.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${item.rank}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(40.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.projectName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = item.clientName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatCurrency(item.income, state.currency),
                                color = KablanProColors.IncomeGreen,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = formatSignedCurrency(item.balanceDelta, state.currency),
                                color = if (item.balanceDelta >= 0.0) KablanProColors.IncomeGreen else KablanProColors.ExpenseRed,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendRow(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color = color, shape = RoundedCornerShape(5.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun EmptySection(message: String, icon: ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(36.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatCurrency(amount: Double, currency: CurrencyOption): String {
    return "${String.format(Locale.getDefault(), "%,.2f", amount)} ${currency.symbol}"
}

private fun formatSignedCurrency(amount: Double, currency: CurrencyOption): String {
    val prefix = if (amount >= 0.0) "+" else "−"
    return prefix + formatCurrency(kotlin.math.abs(amount), currency)
}

private fun formatPercent(value: Float): String = String.format(Locale.getDefault(), "%.0f%%", value)
