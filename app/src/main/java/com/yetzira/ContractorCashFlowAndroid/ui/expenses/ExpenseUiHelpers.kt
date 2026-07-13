package com.yetzira.ContractorCashFlowAndroid.ui.expenses

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseCategory
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatAmountInput
import kotlin.math.abs
import kotlin.math.roundToLong

fun formatExpenseAmountForInput(amount: Double): String =
    formatAmountInput(abs(amount).roundToLong().toString())

fun displayExpenseDescription(description: String): String {
    val trimmed = description.trim()
    return when {
        trimmed.startsWith("Labor: ", ignoreCase = true) -> trimmed.removePrefix("Labor: ").trim()
        trimmed.startsWith("Labor：") -> trimmed.removePrefix("Labor：").trim()
        else -> trimmed
    }
}

fun expenseCategoryIcon(category: ExpenseCategory?): ImageVector = when (category) {
    ExpenseCategory.MATERIALS -> Icons.Default.Handyman
    ExpenseCategory.LABOR -> Icons.Default.Person
    ExpenseCategory.EQUIPMENT -> Icons.Default.Build
    ExpenseCategory.MISC, null -> Icons.Default.MoreHoriz
}

fun expenseCategoryColor(category: ExpenseCategory?): Color {
    val argb = category?.chartColor ?: ExpenseCategory.MISC.chartColor
    return Color(argb)
}

@Composable
fun expenseCategoryLabel(category: ExpenseCategory?): String {
    if (category == null) return ""
    return stringResource(category.labelResId)
}
