package com.yetzira.ContractorCashFlowAndroid.ui.expenses

import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseCategory
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborDetailsEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborType
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity

data class ExpenseListItemUi(
    val expense: ExpenseEntity,
    val projectName: String?
)

data class ExpenseFilterState(
    val category: ExpenseCategory? = null,
    val hasStartDate: Boolean = false,
    val hasEndDate: Boolean = false,
    val startDate: Long? = null,
    val endDate: Long? = null
) {
    val isActive: Boolean
        get() = category != null || hasStartDate || hasEndDate
}

data class ExpensesListUiState(
    val query: String = "",
    val filters: ExpenseFilterState = ExpenseFilterState(),
    val expenses: List<ExpenseListItemUi> = emptyList()
)

data class WorkerOptionUi(
    val worker: LaborDetailsEntity,
    val laborType: LaborType?,
    val hourlyRate: Double?,
    val dailyRate: Double?,
    val contractPrice: Double?
)

data class ExpenseFormUiState(
    val expenseId: String? = null,
    val category: ExpenseCategory = ExpenseCategory.MATERIALS,
    val amount: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val projectId: String? = null,
    val workerId: String? = null,
    val unitsWorked: String = "",
    val laborTypeSnapshot: LaborType? = null,
    val notes: String = "",
    val receiptImageUri: String? = null,
    val projects: List<ProjectEntity> = emptyList(),
    val workers: List<WorkerOptionUi> = emptyList(),
    val isAmountReadOnly: Boolean = false,
    val canSave: Boolean = false,
    val selectedDates: List<Long> = emptyList()
) {
    val useMultiDatePicker: Boolean
        get() = category == ExpenseCategory.LABOR && laborTypeSnapshot == LaborType.DAILY

    val selectedDayCount: Int
        get() = selectedDates.size
}

sealed class ExpenseSaveResult {
    data object None : ExpenseSaveResult()
    data object Saved : ExpenseSaveResult()
    data class BudgetWarning(
        val utilizationPercent: Int,
        val projectName: String = "",
        val totalExpenses: Double = 0.0,
        val budget: Double = 0.0
    ) : ExpenseSaveResult()
}

