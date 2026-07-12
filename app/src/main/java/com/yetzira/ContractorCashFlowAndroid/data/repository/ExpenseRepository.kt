package com.yetzira.ContractorCashFlowAndroid.data.repository

import android.util.Log
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ExpenseDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.LaborDetailsDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ProjectDao
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborDetailsEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.sync.FirestoreSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface ExpenseRepositoryContract {
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    suspend fun getExpenseById(id: String): ExpenseEntity?
    suspend fun insertExpense(expense: ExpenseEntity)
    suspend fun updateExpense(expense: ExpenseEntity)
    suspend fun deleteExpense(expense: ExpenseEntity)
    fun getAllProjects(): Flow<List<ProjectEntity>>
    suspend fun getProjectById(id: String): ProjectEntity?
    fun getAllWorkers(): Flow<List<LaborDetailsEntity>>
    fun getProjectTotalExpenses(projectId: String): Flow<Double?>
}

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val projectDao: ProjectDao,
    private val laborDetailsDao: LaborDetailsDao,
    private val syncService: FirestoreSyncService
) : ExpenseRepositoryContract {
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAll()

    override suspend fun getExpenseById(id: String): ExpenseEntity? = expenseDao.getById(id)

    override suspend fun insertExpense(expense: ExpenseEntity) {
        val stamped = expense.copy(lastModified = System.currentTimeMillis())
        syncService.clearExpenseTombstone(stamped.id)
        expenseDao.insert(stamped)
        syncScope.launch {
            syncService.syncExpense(stamped).onFailure { throwable ->
                Log.w(TAG, "Expense cloud sync failed after local insert: ${throwable.message}")
            }
        }
    }

    override suspend fun updateExpense(expense: ExpenseEntity) {
        val stamped = expense.copy(lastModified = System.currentTimeMillis())
        expenseDao.update(stamped)
        syncScope.launch {
            syncService.syncExpense(stamped).onFailure { throwable ->
                Log.w(TAG, "Expense cloud sync failed after local update: ${throwable.message}")
            }
        }
    }

    override suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.delete(expense)
        syncScope.launch {
            syncService.deleteExpense(expense.id).onFailure { throwable ->
                Log.w(TAG, "Expense cloud delete failed after local delete: ${throwable.message}")
            }
        }
    }

    override fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAll()

    override suspend fun getProjectById(id: String): ProjectEntity? = projectDao.getById(id)

    override fun getAllWorkers(): Flow<List<LaborDetailsEntity>> = laborDetailsDao.getAll()

    override fun getProjectTotalExpenses(projectId: String): Flow<Double?> =
        expenseDao.getTotalForProject(projectId)

    private companion object {
        const val TAG = "KablanProExpenseRepo"
    }
}
