package com.yetzira.ContractorCashFlowAndroid.data.repository

import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ExpenseDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.LaborDetailsDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ProjectDao
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborDetailsEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val projectDao: ProjectDao,
    private val laborDetailsDao: LaborDetailsDao
) {
    fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAll()

    suspend fun getExpenseById(id: String): ExpenseEntity? = expenseDao.getById(id)

    suspend fun insertExpense(expense: ExpenseEntity) = expenseDao.insert(expense)

    suspend fun updateExpense(expense: ExpenseEntity) = expenseDao.update(expense)

    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.delete(expense)

    fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAll()

    suspend fun getProjectById(id: String): ProjectEntity? = projectDao.getById(id)

    fun getAllWorkers(): Flow<List<LaborDetailsEntity>> = laborDetailsDao.getAll()

    fun getProjectTotalExpenses(projectId: String): Flow<Double?> = expenseDao.getTotalForProject(projectId)
}

