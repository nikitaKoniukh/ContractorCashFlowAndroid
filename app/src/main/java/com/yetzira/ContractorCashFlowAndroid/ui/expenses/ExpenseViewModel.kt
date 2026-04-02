package com.yetzira.ContractorCashFlowAndroid.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseCategory
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborType
import com.yetzira.ContractorCashFlowAndroid.data.repository.ExpenseRepositoryContract
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatAmountInput
import com.yetzira.ContractorCashFlowAndroid.ui.components.parseAmountInput
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModel(
    private val repository: ExpenseRepositoryContract
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filters = MutableStateFlow(ExpenseFilterState())
    private val editingExpenseId = MutableStateFlow<String?>(null)
    private val saveResult = MutableStateFlow<ExpenseSaveResult>(ExpenseSaveResult.None)

    private var recentlyDeletedExpense: ExpenseEntity? = null

    val listUiState: StateFlow<ExpensesListUiState> = combine(
        query,
        filters,
        repository.getAllExpenses(),
        repository.getAllProjects()
    ) { q, f, expenses, projects ->
        val filtered = expenses
            .asSequence()
            .filter { item ->
                q.isBlank() || item.descriptionText.contains(q, ignoreCase = true)
            }
            .filter { item ->
                f.category == null || item.category == f.category.name
            }
            .filter { item ->
                if (f.hasStartDate && f.startDate != null) item.date >= f.startDate else true
            }
            .filter { item ->
                if (f.hasEndDate && f.endDate != null) item.date <= f.endDate else true
            }
            .sortedByDescending { it.date }
            .map { expense ->
                ExpenseListItemUi(
                    expense = expense,
                    projectName = projects.firstOrNull { it.id == expense.projectId }?.name
                )
            }
            .toList()

        ExpensesListUiState(
            query = q,
            filters = f,
            expenses = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpensesListUiState()
    )

    val formUiState: StateFlow<ExpenseFormUiState> = editingExpenseId.flatMapLatest { expenseId ->
        combine(
            repository.getAllProjects(),
            repository.getAllWorkers(),
            repository.getAllExpenses()
        ) { projects, workers, expenses ->
            val editableExpense = expenseId?.let { id -> expenses.firstOrNull { it.id == id } }
            val activeProjects = projects.filter { it.isActive }
            val workerOptions = workers.map { worker ->
                val laborType = LaborType.fromString(worker.laborType)
                WorkerOptionUi(
                    worker = worker,
                    laborType = laborType,
                    hourlyRate = worker.hourlyRate,
                    dailyRate = worker.dailyRate,
                    contractPrice = worker.contractPrice
                )
            }

            val state = if (editableExpense != null) {
                ExpenseFormUiState(
                    expenseId = editableExpense.id,
                    category = ExpenseCategory.fromString(editableExpense.category) ?: ExpenseCategory.MATERIALS,
                    amount = formatAmountInput(editableExpense.amount.toLong().toString()),
                    description = editableExpense.descriptionText,
                    date = editableExpense.date,
                    projectId = editableExpense.projectId,
                    workerId = editableExpense.workerId,
                    unitsWorked = editableExpense.unitsWorked?.toString().orEmpty(),
                    laborTypeSnapshot = LaborType.fromString(editableExpense.laborTypeSnapshot),
                    notes = editableExpense.notes.orEmpty(),
                    receiptImageUri = editableExpense.receiptImageUri,
                    projects = activeProjects,
                    workers = workerOptions
                )
            } else {
                ExpenseFormUiState(
                    projects = activeProjects,
                    workers = workerOptions
                )
            }

            recalculateLaborDependentFields(state)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpenseFormUiState()
    )

    val lastSaveResult: StateFlow<ExpenseSaveResult> = saveResult

    fun resetSaveResult() {
        saveResult.value = ExpenseSaveResult.None
    }

    fun setSearchQuery(value: String) {
        query.value = value
    }

    fun applyFilters(newFilters: ExpenseFilterState) {
        filters.value = newFilters
    }

    fun clearFilters() {
        filters.value = ExpenseFilterState()
    }

    fun startCreate() {
        editingExpenseId.value = null
    }

    fun startEdit(expenseId: String) {
        editingExpenseId.value = expenseId
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            recentlyDeletedExpense = expense
            repository.deleteExpense(expense)
        }
    }

    fun deleteExpenseById(expenseId: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val expense = repository.getExpenseById(expenseId) ?: return@launch
            recentlyDeletedExpense = expense
            repository.deleteExpense(expense)
            onDone()
        }
    }

    fun undoDeleteExpense() {
        val toRestore = recentlyDeletedExpense ?: return
        viewModelScope.launch {
            repository.insertExpense(toRestore)
            recentlyDeletedExpense = null
        }
    }

    fun saveExpense(state: ExpenseFormUiState) {
        viewModelScope.launch {
            val parsedAmount = parseAmountInput(state.amount) ?: 0.0
            if (state.description.isBlank() || parsedAmount <= 0.0) return@launch

            // Multi-date save for daily labor
            if (state.useMultiDatePicker && state.selectedDates.isNotEmpty()) {
                val dayCount = state.selectedDayCount
                val dailyRate = parsedAmount / dayCount
                
                for (dateMillis in state.selectedDates) {
                    val entity = ExpenseEntity(
                        id = java.util.UUID.randomUUID().toString(),
                        category = state.category.name,
                        amount = dailyRate,
                        descriptionText = state.description,
                        date = dateMillis,
                        projectId = state.projectId,
                        workerId = state.workerId,
                        unitsWorked = 1.0,
                        laborTypeSnapshot = state.laborTypeSnapshot?.name,
                        notes = state.notes.ifBlank { null },
                        receiptImageUri = state.receiptImageUri
                    )
                    repository.insertExpense(entity)
                }
            } else {
                // Single expense save
                val entity = ExpenseEntity(
                    id = state.expenseId ?: java.util.UUID.randomUUID().toString(),
                    category = state.category.name,
                    amount = parsedAmount,
                    descriptionText = state.description,
                    date = state.date,
                    projectId = state.projectId,
                    workerId = state.workerId,
                    unitsWorked = state.unitsWorked.toDoubleOrNull(),
                    laborTypeSnapshot = state.laborTypeSnapshot?.name,
                    notes = state.notes.ifBlank { null },
                    receiptImageUri = state.receiptImageUri
                )

                if (state.expenseId == null) {
                    repository.insertExpense(entity)
                } else {
                    repository.updateExpense(entity)
                }
            }

            val budgetResult = evaluateBudgetWarning(state.projectId)
            saveResult.value = budgetResult ?: ExpenseSaveResult.Saved
        }
    }

    fun updateForm(
        current: ExpenseFormUiState,
        category: ExpenseCategory? = null,
        amount: String? = null,
        description: String? = null,
        date: Long? = null,
        projectId: String? = null,
        workerId: String? = null,
        unitsWorked: String? = null,
        laborTypeSnapshot: LaborType? = current.laborTypeSnapshot,
        selectedDates: List<Long>? = null
    ): ExpenseFormUiState {
        var updated = current.copy(
            category = category ?: current.category,
            amount = amount ?: current.amount,
            description = description ?: current.description,
            date = date ?: current.date,
            projectId = projectId ?: current.projectId,
            workerId = workerId ?: current.workerId,
            unitsWorked = unitsWorked ?: current.unitsWorked,
            laborTypeSnapshot = laborTypeSnapshot,
            selectedDates = selectedDates ?: current.selectedDates
        )

        // If category changed away from LABOR, clear labor-specific fields
        if (category != null && category != ExpenseCategory.LABOR && updated.category != ExpenseCategory.LABOR) {
            updated = updated.copy(
                workerId = null,
                unitsWorked = "",
                laborTypeSnapshot = null,
                notes = "",
                selectedDates = emptyList()
            )
        }

        // If labor type changed and multi-date is now active, clear units; if hourly, clear dates
        if (laborTypeSnapshot != null && laborTypeSnapshot != current.laborTypeSnapshot) {
            if (laborTypeSnapshot == LaborType.DAILY) {
                updated = updated.copy(unitsWorked = "")
            } else if (laborTypeSnapshot == LaborType.HOURLY) {
                updated = updated.copy(selectedDates = emptyList())
            }
        }

        return recalculateLaborDependentFields(updated)
    }

    private suspend fun evaluateBudgetWarning(projectId: String?): ExpenseSaveResult.BudgetWarning? {
        if (projectId == null) return null
        val project = repository.getProjectById(projectId) ?: return null
        if (project.budget <= 0.0) return null

        val totalExpenses = repository.getProjectTotalExpenses(projectId).first() ?: 0.0
        val utilization = ((totalExpenses / project.budget) * 100.0).roundToInt()

        return when {
            utilization >= 100 -> ExpenseSaveResult.BudgetWarning(
                utilizationPercent = 100,
                projectName = project.name,
                totalExpenses = totalExpenses,
                budget = project.budget
            )
            utilization >= 80 -> ExpenseSaveResult.BudgetWarning(
                utilizationPercent = 80,
                projectName = project.name,
                totalExpenses = totalExpenses,
                budget = project.budget
            )
            else -> null
        }
    }

    private fun recalculateLaborDependentFields(input: ExpenseFormUiState): ExpenseFormUiState {
        if (input.category != ExpenseCategory.LABOR) {
            return input.copy(
                isAmountReadOnly = false,
                laborTypeSnapshot = null,
                canSave = input.description.isNotBlank() && (parseAmountInput(input.amount) ?: 0.0) > 0.0,
                selectedDates = emptyList()
            )
        }

        val workerOption = input.workers.firstOrNull { it.worker.id == input.workerId }
        if (workerOption == null) {
            return input.copy(
                isAmountReadOnly = false,
                laborTypeSnapshot = null,
                canSave = false,
                selectedDates = emptyList()
            )
        }

        var amount = input.amount
        var description = input.description
        var readOnly = false
        var selectedLaborType: LaborType? = input.laborTypeSnapshot

        if (description.isBlank()) {
            description = workerOption.worker.workerName
        }

        if (workerOption.laborType == LaborType.SUBCONTRACTOR) {
            selectedLaborType = workerOption.laborType
            val contractRate = workerOption.contractPrice ?: 0.0
            amount = formatAmountInput(contractRate.toLong().toString())
            readOnly = true
        } else {
            val hasHourly = workerOption.hourlyRate != null
            val hasDaily = workerOption.dailyRate != null
            selectedLaborType = when {
                hasHourly && hasDaily && (selectedLaborType == LaborType.HOURLY || selectedLaborType == LaborType.DAILY) -> selectedLaborType
                hasHourly && hasDaily -> null
                hasHourly -> LaborType.HOURLY
                hasDaily -> LaborType.DAILY
                workerOption.laborType == LaborType.HOURLY || workerOption.laborType == LaborType.DAILY -> workerOption.laborType
                else -> null
            }

            val rate = when (selectedLaborType) {
                LaborType.HOURLY -> workerOption.hourlyRate
                LaborType.DAILY -> workerOption.dailyRate
                else -> null
            }

            if (selectedLaborType == LaborType.DAILY && input.selectedDates.isNotEmpty()) {
                // Multi-day: total = rate * dayCount
                if (rate != null && rate > 0.0) {
                    val totalAmount = rate * input.selectedDates.size
                    amount = formatAmountInput(totalAmount.toLong().toString())
                }
            } else {
                // Single day or hourly: standard calculation
                val units = input.unitsWorked.toDoubleOrNull() ?: 0.0
                if (rate != null && units > 0.0) {
                    amount = formatAmountInput((rate * units).toLong().toString())
                }
            }
        }

        val requiresLaborModeSelection =
            workerOption.hourlyRate != null && workerOption.dailyRate != null &&
                (selectedLaborType != LaborType.HOURLY && selectedLaborType != LaborType.DAILY)

        // For daily with multi-date, require at least one selected date
        val multiDateValid = if (selectedLaborType == LaborType.DAILY && input.useMultiDatePicker) {
            input.selectedDates.isNotEmpty()
        } else {
            true
        }

        val canSave = !requiresLaborModeSelection && description.isNotBlank() && (parseAmountInput(amount) ?: 0.0) > 0.0 && multiDateValid

        return input.copy(
            amount = amount,
            description = description,
            isAmountReadOnly = readOnly,
            laborTypeSnapshot = selectedLaborType,
            canSave = canSave
        )
    }
}

