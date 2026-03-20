package com.yetzira.ContractorCashFlowAndroid.data.repository

import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ExpenseDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.LaborDetailsDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ProjectDao
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborDetailsEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

class LaborRepository(
    private val laborDetailsDao: LaborDetailsDao,
    private val expenseDao: ExpenseDao,
    private val projectDao: ProjectDao
) {
    fun getAllWorkers(): Flow<List<LaborDetailsEntity>> = laborDetailsDao.getAll()

    suspend fun getWorkerById(id: String): LaborDetailsEntity? = laborDetailsDao.getById(id)

    suspend fun insertWorker(worker: LaborDetailsEntity) = laborDetailsDao.insert(worker)

    suspend fun updateWorker(worker: LaborDetailsEntity) = laborDetailsDao.update(worker)

    suspend fun deleteWorker(worker: LaborDetailsEntity) = laborDetailsDao.delete(worker)

    fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAll()

    fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAll()
}

