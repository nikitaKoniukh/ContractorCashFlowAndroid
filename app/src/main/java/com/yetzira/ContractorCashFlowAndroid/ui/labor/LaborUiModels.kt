package com.yetzira.ContractorCashFlowAndroid.ui.labor

import androidx.annotation.StringRes
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborDetailsEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborType

enum class LaborSortOption(@StringRes val labelResId: Int) {
    RECENTLY_ADDED(R.string.labor_sort_recently_added),
    WORKER_NAME(R.string.labor_sort_worker_name),
    TOTAL_EARNED_HIGH_TO_LOW(R.string.labor_sort_total_earned_high_to_low),
    TOTAL_EARNED_LOW_TO_HIGH(R.string.labor_sort_total_earned_low_to_high)
}

data class LaborMonthOption(
    val label: String,
    val startMillis: Long,
    val endMillis: Long
)

data class LaborFiltersState(
    val laborType: LaborType? = null,
    val projectName: String? = null,
    val monthEnabled: Boolean = false,
    val month: LaborMonthOption? = null
) {
    val isActive: Boolean
        get() = laborType != null || projectName != null || (monthEnabled && month != null)
}

data class WorkerMetricsUi(
    val worker: LaborDetailsEntity,
    val laborType: LaborType?,
    val totalAmountEarned: Double,
    val totalUnitsWorked: Double,
    val totalDaysWorked: Int,
    val associatedProjects: List<String>,
    val linkedExpenseCount: Int,
    val hourlyUnitsWorked: Double = 0.0,
    val dailyUnitsWorked: Double = 0.0,
    val projectBreakdown: List<ProjectCostUi> = emptyList()
) {
    val rateLabel: String
        get() = when (laborType) {
            LaborType.HOURLY -> "${worker.hourlyRate ?: 0.0}${laborType.rateSuffix}"
            LaborType.DAILY -> "${worker.dailyRate ?: 0.0}${laborType.rateSuffix}"
            LaborType.CONTRACT, LaborType.SUBCONTRACTOR -> "${worker.contractPrice ?: 0.0}"
            null -> "-"
        }
}

data class ProjectCostUi(
    val projectName: String,
    val amount: Double
)

data class LaborSummaryUi(
    val totalLaborCost: Double = 0.0,
    val workerCount: Int = 0,
    val daysWorked: Int = 0,
    val avgDailyCost: Double = 0.0,
    val totalHours: Double = 0.0,
    val periodLabel: String = "All Time"
)

data class LaborListUiState(
    val query: String = "",
    val filters: LaborFiltersState = LaborFiltersState(),
    val sort: LaborSortOption = LaborSortOption.RECENTLY_ADDED,
    val availableProjects: List<String> = emptyList(),
    val availableMonths: List<LaborMonthOption> = emptyList(),
    val workers: List<WorkerMetricsUi> = emptyList(),
    val summary: LaborSummaryUi = LaborSummaryUi()
)

data class LaborFormUiState(
    val id: String? = null,
    val workerName: String = "",
    val laborType: LaborType = LaborType.HOURLY,
    val hourlyRate: String = "",
    val dailyRate: String = "",
    val contractPrice: String = "",
    val notes: String = "",
    val duplicateWarning: Boolean = false,
    val canSave: Boolean = false,
    val hasChanges: Boolean = false
)

data class LaborDetailUiState(
    val worker: LaborDetailsEntity? = null,
    val metrics: WorkerMetricsUi? = null
)

