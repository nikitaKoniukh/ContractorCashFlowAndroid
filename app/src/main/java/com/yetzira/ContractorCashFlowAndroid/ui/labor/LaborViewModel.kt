package com.yetzira.ContractorCashFlowAndroid.ui.labor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborDetailsEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborType
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.data.repository.LaborRepositoryContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LaborViewModel(
    private val repository: LaborRepositoryContract
) : ViewModel() {

    private var workers: List<LaborDetailsEntity> = emptyList()
    private var expenses: List<ExpenseEntity> = emptyList()
    private var projects: List<ProjectEntity> = emptyList()
    private var query: String = ""
    private var filters: LaborFiltersState = LaborFiltersState()
    private var sort: LaborSortOption = LaborSortOption.RECENTLY_ADDED
    private var selectedWorkerId: String? = null
    private var originalWorker: LaborDetailsEntity? = null

    private val _listUiState = MutableStateFlow(LaborListUiState())
    val listUiState: StateFlow<LaborListUiState> = _listUiState.asStateFlow()

    private val _detailUiState = MutableStateFlow(LaborDetailUiState())
    val detailUiState: StateFlow<LaborDetailUiState> = _detailUiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllWorkers().collect {
                workers = it
                recompute()
            }
        }
        viewModelScope.launch {
            repository.getAllExpenses().collect {
                expenses = it
                recompute()
            }
        }
        viewModelScope.launch {
            repository.getAllProjects().collect {
                projects = it
                recompute()
            }
        }
    }

    fun setSearchQuery(value: String) {
        query = value
        recompute()
    }

    fun applyFilters(value: LaborFiltersState) {
        filters = value
        recompute()
    }

    fun setSort(value: LaborSortOption) {
        sort = value
        recompute()
    }

    fun selectWorker(workerId: String) {
        selectedWorkerId = workerId
        recomputeDetail()
    }

    fun setOriginalWorker(worker: LaborDetailsEntity?) {
        originalWorker = worker
    }

    fun buildFormState(worker: LaborDetailsEntity?): LaborFormUiState {
        return if (worker == null) {
            LaborFormUiState()
        } else {
            LaborFormUiState(
                id = worker.id,
                workerName = worker.workerName,
                laborType = LaborType.fromString(worker.laborType) ?: LaborType.HOURLY,
                hourlyRate = worker.hourlyRate?.toString().orEmpty(),
                dailyRate = worker.dailyRate?.toString().orEmpty(),
                contractPrice = worker.contractPrice?.toString().orEmpty(),
                notes = worker.notes.orEmpty(),
                canSave = worker.workerName.isNotBlank(),
                hasChanges = false
            )
        }
    }

    fun updateForm(
        state: LaborFormUiState,
        allWorkers: List<LaborDetailsEntity> = workers
    ): LaborFormUiState {
        val trimmedName = state.workerName.trim()
        val duplicate = allWorkers.any { it.workerName.equals(trimmedName, ignoreCase = true) && it.id != state.id }
        val original = originalWorker
        val hasChanges = if (original == null) {
            trimmedName.isNotBlank() ||
                state.hourlyRate.isNotBlank() ||
                state.dailyRate.isNotBlank() ||
                state.contractPrice.isNotBlank() ||
                state.notes.isNotBlank()
        } else {
            trimmedName != original.workerName ||
                state.laborType.name != original.laborType ||
                state.hourlyRate.trim().ifBlank { null } != original.hourlyRate?.toString() ||
                state.dailyRate.trim().ifBlank { null } != original.dailyRate?.toString() ||
                state.contractPrice.trim().ifBlank { null } != original.contractPrice?.toString() ||
                state.notes.trim().ifBlank { null } != original.notes
        }
        return state.copy(
            workerName = trimmedName,
            duplicateWarning = duplicate,
            canSave = trimmedName.isNotBlank() && !duplicate && (original == null || hasChanges),
            hasChanges = hasChanges
        )
    }

    fun saveWorker(state: LaborFormUiState, onDone: () -> Unit) {
        viewModelScope.launch {
            if (state.workerName.isBlank()) return@launch
            val entity = LaborDetailsEntity(
                id = state.id ?: java.util.UUID.randomUUID().toString(),
                workerName = state.workerName.trim(),
                laborType = state.laborType.name,
                hourlyRate = state.hourlyRate.trim().toDoubleOrNull(),
                dailyRate = state.dailyRate.trim().toDoubleOrNull(),
                contractPrice = state.contractPrice.trim().toDoubleOrNull(),
                notes = state.notes.trim().ifBlank { null },
                createdDate = originalWorker?.createdDate ?: System.currentTimeMillis()
            )
            if (state.id == null) repository.insertWorker(entity) else repository.updateWorker(entity)
            onDone()
        }
    }

    fun deleteWorker(worker: LaborDetailsEntity, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteWorker(worker)
            onDone?.invoke()
        }
    }

    private fun recompute() {
        _listUiState.value = buildListUiState(
            q = query,
            filterState = filters,
            sortOption = sort,
            workers = workers,
            expenses = expenses,
            projects = projects
        )
        recomputeDetail()
    }

    private fun recomputeDetail() {
        val worker = workers.firstOrNull { it.id == selectedWorkerId }
        _detailUiState.value = LaborDetailUiState(
            worker = worker,
            metrics = worker?.let { computeMetrics(it, expenses.filter { expense -> expense.workerId == worker.id }, projects) }
        )
    }

    private fun buildListUiState(
        q: String,
        filterState: LaborFiltersState,
        sortOption: LaborSortOption,
        workers: List<LaborDetailsEntity>,
        expenses: List<ExpenseEntity>,
        projects: List<ProjectEntity>
    ): LaborListUiState {
        val availableMonths = buildMonthOptions()
        val availableProjects = expenses.mapNotNull { expense ->
            projects.firstOrNull { it.id == expense.projectId }?.name
        }.distinct().sorted()

        val workerMetrics = workers
            .filter { worker ->
                q.isBlank() || worker.workerName.contains(q, ignoreCase = true) || worker.notes.orEmpty().contains(q, ignoreCase = true)
            }
            .map { worker ->
                val relevantExpenses = if (filterState.monthEnabled && filterState.month != null) {
                    expenses.filter {
                        it.workerId == worker.id && it.date in filterState.month.startMillis..filterState.month.endMillis
                    }
                } else {
                    expenses.filter { it.workerId == worker.id }
                }
                computeMetrics(worker, relevantExpenses, projects)
            }
            .filter { metric ->
                (filterState.laborType == null || metric.laborType == filterState.laborType) &&
                    (filterState.projectName == null || metric.associatedProjects.contains(filterState.projectName)) &&
                    (!filterState.monthEnabled || filterState.month == null || metric.linkedExpenseCount > 0)
            }

        val sortedWorkers = when (sortOption) {
            LaborSortOption.RECENTLY_ADDED -> workerMetrics.sortedByDescending { it.worker.createdDate }
            LaborSortOption.WORKER_NAME -> workerMetrics.sortedBy { it.worker.workerName.lowercase() }
            LaborSortOption.TOTAL_EARNED_HIGH_TO_LOW -> workerMetrics.sortedByDescending { it.totalAmountEarned }
            LaborSortOption.TOTAL_EARNED_LOW_TO_HIGH -> workerMetrics.sortedBy { it.totalAmountEarned }
        }

        val visibleWorkerIds = sortedWorkers.map { it.worker.id }.toSet()
        val summaryExpenses = expenses.filter { expense ->
            expense.workerId in visibleWorkerIds &&
                (!filterState.monthEnabled || filterState.month == null || expense.date in filterState.month.startMillis..filterState.month.endMillis)
        }

        val uniqueDays = summaryExpenses.map { normalizeDay(it.date) }.distinct().size
        val totalLaborCost = summaryExpenses.sumOf { it.amount }
        val totalHours = summaryExpenses.filter {
            LaborType.fromString(it.laborTypeSnapshot) == LaborType.HOURLY
        }.sumOf { it.unitsWorked ?: 0.0 }

        return LaborListUiState(
            query = q,
            filters = filterState,
            sort = sortOption,
            availableProjects = availableProjects,
            availableMonths = availableMonths,
            workers = sortedWorkers,
            summary = LaborSummaryUi(
                totalLaborCost = totalLaborCost,
                workerCount = sortedWorkers.size,
                daysWorked = uniqueDays,
                avgDailyCost = if (uniqueDays > 0) totalLaborCost / uniqueDays else 0.0,
                totalHours = totalHours,
                periodLabel = if (filterState.monthEnabled) filterState.month?.label ?: "Selected Month" else "All Time"
            )
        )
    }

    private fun computeMetrics(
        worker: LaborDetailsEntity,
        sourceExpenses: List<ExpenseEntity>,
        projects: List<ProjectEntity>
    ): WorkerMetricsUi {
        val fallbackType = LaborType.fromString(worker.laborType)
        val hourlyUnitsWorked = sourceExpenses
            .filter { (LaborType.fromString(it.laborTypeSnapshot) ?: fallbackType) == LaborType.HOURLY }
            .sumOf { it.unitsWorked ?: 0.0 }
        val dailyUnitsWorked = sourceExpenses
            .filter { (LaborType.fromString(it.laborTypeSnapshot) ?: fallbackType) == LaborType.DAILY }
            .sumOf { it.unitsWorked ?: 0.0 }

        val projectBreakdown = sourceExpenses
            .groupBy { expense ->
                projects.firstOrNull { it.id == expense.projectId }?.name ?: ""
            }
            .filterKeys { it.isNotBlank() }
            .map { (projectName, expensesForProject) ->
                ProjectCostUi(
                    projectName = projectName,
                    amount = expensesForProject.sumOf { it.amount }
                )
            }
            .sortedBy { it.projectName.lowercase() }

        return WorkerMetricsUi(
            worker = worker,
            laborType = fallbackType,
            totalAmountEarned = sourceExpenses.sumOf { it.amount },
            totalUnitsWorked = sourceExpenses.sumOf { it.unitsWorked ?: 0.0 },
            totalDaysWorked = sourceExpenses.map { normalizeDay(it.date) }.distinct().size,
            associatedProjects = sourceExpenses.mapNotNull { expense ->
                projects.firstOrNull { it.id == expense.projectId }?.name
            }.distinct().sorted(),
            linkedExpenseCount = sourceExpenses.size,
            hourlyUnitsWorked = hourlyUnitsWorked,
            dailyUnitsWorked = dailyUnitsWorked,
            projectBreakdown = projectBreakdown
        )
    }

    private fun buildMonthOptions(): List<LaborMonthOption> {
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return buildList {
            repeat(12) {
                val start = calendar.timeInMillis
                val label = formatter.format(Date(start))
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                val end = calendar.timeInMillis
                add(LaborMonthOption(label, start, end))
                calendar.add(Calendar.MILLISECOND, 1)
                calendar.add(Calendar.MONTH, -2)
            }
        }
    }

    private fun normalizeDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
