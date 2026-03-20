package com.yetzira.ContractorCashFlowAndroid.data.repository

import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ExpenseDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.LaborDetailsDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ProjectDao
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborDetailsEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.sync.FirestoreSyncService
import kotlinx.coroutines.flow.Flow

class LaborRepository(
    private val laborDetailsDao: LaborDetailsDao,
    private val expenseDao: ExpenseDao,
    private val projectDao: ProjectDao,
    private val syncService: FirestoreSyncService
) {
    fun getAllWorkers(): Flow<List<LaborDetailsEntity>> = laborDetailsDao.getAll()

    suspend fun getWorkerById(id: String): LaborDetailsEntity? = laborDetailsDao.getById(id)

    suspend fun insertWorker(worker: LaborDetailsEntity) {
        val stamped = worker.copy(lastModified = System.currentTimeMillis())
        laborDetailsDao.insert(stamped)
        syncService.syncLaborDetails(stamped)
    }

    suspend fun updateWorker(worker: LaborDetailsEntity) {
        val stamped = worker.copy(lastModified = System.currentTimeMillis())
        laborDetailsDao.update(stamped)
        syncService.syncLaborDetails(stamped)
    }

    suspend fun deleteWorker(worker: LaborDetailsEntity) {
        laborDetailsDao.delete(worker)
        syncService.deleteLaborDetails(worker.id)
    }

    fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAll()

    fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAll()
}

