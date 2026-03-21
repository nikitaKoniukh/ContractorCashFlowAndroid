package com.yetzira.ContractorCashFlowAndroid.ui.analytics

import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ExpenseDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.InvoiceDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ProjectDao
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.data.preferences.AppLanguageOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.SettingsPreferencesRepositoryContract
import com.yetzira.ContractorCashFlowAndroid.data.preferences.ThemeModeOption
import com.yetzira.ContractorCashFlowAndroid.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `uiState reflects current currency and computed totals`() = runTest {
        val now = System.currentTimeMillis()
        val expenses = listOf(
            ExpenseEntity(
                id = "e1",
                category = "LABOR",
                amount = 200.0,
                descriptionText = "Work",
                date = now - 2 * 86_400_000L,
                projectId = "p1"
            )
        )
        val invoices = listOf(
            InvoiceEntity(
                id = "i1",
                amount = 800.0,
                dueDate = now + 5 * 86_400_000L,
                isPaid = true,
                clientName = "Client A",
                createdDate = now - 2 * 86_400_000L,
                projectId = "p1"
            )
        )
        val projects = listOf(
            ProjectEntity(id = "p1", name = "Project A", clientName = "Client A", budget = 1_000.0)
        )
        val preferences = FakeSettingsPreferencesRepository(currency = CurrencyOption.USD)
        val viewModel = AnalyticsViewModel(
            expenseDao = FakeExpenseDao(expenses),
            invoiceDao = FakeInvoiceDao(invoices),
            projectDao = FakeProjectDao(projects),
            userPreferencesRepository = preferences
        )
        val collectJob = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(CurrencyOption.USD, state.currency)
        assertEquals(800.0, state.totalIncome, 0.0)
        assertEquals(200.0, state.totalExpenses, 0.0)
        assertEquals(600.0, state.netBalance, 0.0)

        collectJob.cancel()
    }

    @Test
    fun `setSelectedPeriod updates uiState and recomputes visible range`() = runTest {
        val now = System.currentTimeMillis()
        val expenses = listOf(
            ExpenseEntity(
                id = "recent",
                category = "MATERIALS",
                amount = 100.0,
                descriptionText = "Recent",
                date = now - 5 * 86_400_000L,
                projectId = "p1"
            ),
            ExpenseEntity(
                id = "old",
                category = "MATERIALS",
                amount = 300.0,
                descriptionText = "Old",
                date = now - 200 * 86_400_000L,
                projectId = "p1"
            )
        )
        val viewModel = AnalyticsViewModel(
            expenseDao = FakeExpenseDao(expenses),
            invoiceDao = FakeInvoiceDao(emptyList()),
            projectDao = FakeProjectDao(listOf(ProjectEntity(id = "p1", name = "P", clientName = "C", budget = 1000.0))),
            userPreferencesRepository = FakeSettingsPreferencesRepository()
        )
        val collectJob = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertEquals(AnalyticsPeriod.MONTH, viewModel.uiState.value.selectedPeriod)
        assertEquals(100.0, viewModel.uiState.value.totalExpenses, 0.0)

        viewModel.setSelectedPeriod(AnalyticsPeriod.ALL)
        advanceUntilIdle()

        assertEquals(AnalyticsPeriod.ALL, viewModel.uiState.value.selectedPeriod)
        assertEquals(400.0, viewModel.uiState.value.totalExpenses, 0.0)
        assertTrue(viewModel.uiState.value.expensesByCategory.isNotEmpty())

        collectJob.cancel()
    }

    private class FakeSettingsPreferencesRepository(
        currency: CurrencyOption = CurrencyOption.ILS
    ) : SettingsPreferencesRepositoryContract {
        override val appLanguage: Flow<AppLanguageOption> = MutableStateFlow(AppLanguageOption.HEBREW)
        override val themeMode: Flow<ThemeModeOption> = MutableStateFlow(ThemeModeOption.SYSTEM)
        override val selectedCurrencyCode: Flow<CurrencyOption> = MutableStateFlow(currency)
        override val invoiceRemindersEnabled: Flow<Boolean> = MutableStateFlow(true)
        override val overdueAlertsEnabled: Flow<Boolean> = MutableStateFlow(true)
        override val budgetWarningsEnabled: Flow<Boolean> = MutableStateFlow(true)
        override val subscriptionIsPro: Flow<Boolean> = MutableStateFlow(false)
        override val subscriptionPlanName: Flow<String?> = MutableStateFlow(null)
        override val subscriptionRenewalDate: Flow<Long?> = MutableStateFlow(null)

        override suspend fun setAppLanguage(language: AppLanguageOption) = Unit
        override suspend fun setThemeMode(themeMode: ThemeModeOption) = Unit
        override suspend fun setSelectedCurrency(currency: CurrencyOption) = Unit
        override suspend fun setInvoiceRemindersEnabled(enabled: Boolean) = Unit
        override suspend fun setOverdueAlertsEnabled(enabled: Boolean) = Unit
        override suspend fun setBudgetWarningsEnabled(enabled: Boolean) = Unit
    }

    private class FakeExpenseDao(initial: List<ExpenseEntity>) : ExpenseDao {
        private val flow = MutableStateFlow(initial)
        override fun getAll(): Flow<List<ExpenseEntity>> = flow
        override suspend fun getById(id: String): ExpenseEntity? = flow.value.firstOrNull { it.id == id }
        override fun search(query: String): Flow<List<ExpenseEntity>> = flow.map { list ->
            list.filter { it.descriptionText.contains(query, true) || it.category.contains(query, true) }
        }
        override fun getForProject(projectId: String): Flow<List<ExpenseEntity>> = flow.map { list -> list.filter { it.projectId == projectId } }
        override fun getForWorker(workerId: String): Flow<List<ExpenseEntity>> = flow.map { list -> list.filter { it.workerId == workerId } }
        override fun getTotalForProject(projectId: String): Flow<Double?> = flow.map { list -> list.filter { it.projectId == projectId }.sumOf { it.amount } }
        override suspend fun insert(expense: ExpenseEntity) { flow.value = flow.value + expense }
        override suspend fun update(expense: ExpenseEntity) { flow.value = flow.value.map { if (it.id == expense.id) expense else it } }
        override suspend fun delete(expense: ExpenseEntity) { flow.value = flow.value.filterNot { it.id == expense.id } }
    }

    private class FakeInvoiceDao(initial: List<InvoiceEntity>) : InvoiceDao {
        private val flow = MutableStateFlow(initial)
        override fun getAll(): Flow<List<InvoiceEntity>> = flow
        override suspend fun getById(id: String): InvoiceEntity? = flow.value.firstOrNull { it.id == id }
        override fun search(query: String): Flow<List<InvoiceEntity>> = flow.map { list -> list.filter { it.clientName.contains(query, true) } }
        override fun getForProject(projectId: String): Flow<List<InvoiceEntity>> = flow.map { list -> list.filter { it.projectId == projectId } }
        override fun getUnpaid(): Flow<List<InvoiceEntity>> = flow.map { list -> list.filter { !it.isPaid } }
        override suspend fun insert(invoice: InvoiceEntity) { flow.value = flow.value + invoice }
        override suspend fun update(invoice: InvoiceEntity) { flow.value = flow.value.map { if (it.id == invoice.id) invoice else it } }
        override suspend fun delete(invoice: InvoiceEntity) { flow.value = flow.value.filterNot { it.id == invoice.id } }
    }

    private class FakeProjectDao(initial: List<ProjectEntity>) : ProjectDao {
        private val flow = MutableStateFlow(initial)
        override fun getAll(): Flow<List<ProjectEntity>> = flow
        override suspend fun getById(id: String): ProjectEntity? = flow.value.firstOrNull { it.id == id }
        override fun search(query: String): Flow<List<ProjectEntity>> = flow.map { list ->
            list.filter { it.name.contains(query, true) || it.clientName.contains(query, true) }
        }
        override suspend fun insert(project: ProjectEntity) { flow.value = flow.value + project }
        override suspend fun update(project: ProjectEntity) { flow.value = flow.value.map { if (it.id == project.id) project else it } }
        override suspend fun delete(project: ProjectEntity) { flow.value = flow.value.filterNot { it.id == project.id } }
    }
}

