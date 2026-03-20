package com.yetzira.ContractorCashFlowAndroid.data.repository

import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ExpenseDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.LaborDetailsDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ProjectDao
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborDetailsEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.sync.FirestoreSyncService
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val projectDao: ProjectDao,
    private val laborDetailsDao: LaborDetailsDao,
    private val syncService: FirestoreSyncService
) {
    fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAll()

    suspend fun getExpenseById(id: String): ExpenseEntity? = expenseDao.getById(id)

    suspend fun insertExpense(expense: ExpenseEntity) {
        val stamped = expense.copy(lastModified = System.currentTimeMillis())
        expenseDao.insert(stamped)
        syncService.syncExpense(stamped)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        val stamped = expense.copy(lastModified = System.currentTimeMillis())
        expenseDao.update(stamped)
        syncService.syncExpense(stamped)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.delete(expense)
        syncService.deleteExpense(expense.id)
    }

    fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAll()

    suspend fun getProjectById(id: String): ProjectEntity? = projectDao.getById(id)

    fun getAllWorkers(): Flow<List<LaborDetailsEntity>> = laborDetailsDao.getAll()

    fun getProjectTotalExpenses(projectId: String): Flow<Double?> = expenseDao.getTotalForProject(projectId)
}

