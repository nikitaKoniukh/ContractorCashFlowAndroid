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

private const val TAG = "LaborRepository"

interface LaborRepositoryContract {
    fun getAllWorkers(): Flow<List<LaborDetailsEntity>>
    suspend fun getWorkerById(id: String): LaborDetailsEntity?
    suspend fun insertWorker(worker: LaborDetailsEntity)
    suspend fun updateWorker(worker: LaborDetailsEntity)
    suspend fun deleteWorker(worker: LaborDetailsEntity)
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    fun getAllProjects(): Flow<List<ProjectEntity>>
}

class LaborRepository(
    private val laborDetailsDao: LaborDetailsDao,
    private val expenseDao: ExpenseDao,
    private val projectDao: ProjectDao,
    private val syncService: FirestoreSyncService
) : LaborRepositoryContract {
    // Fire-and-forget scope: sync failures never block the caller
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun getAllWorkers(): Flow<List<LaborDetailsEntity>> = laborDetailsDao.getAll()

    override suspend fun getWorkerById(id: String): LaborDetailsEntity? = laborDetailsDao.getById(id)

    override suspend fun insertWorker(worker: LaborDetailsEntity) {
        val stamped = worker.copy(lastModified = System.currentTimeMillis())
        laborDetailsDao.insert(stamped)
        syncScope.launch {
            runCatching { syncService.syncLaborDetails(stamped) }
                .onFailure { Log.w(TAG, "Labor sync failed: ${it.message}") }
        }
    }

    override suspend fun updateWorker(worker: LaborDetailsEntity) {
        val stamped = worker.copy(lastModified = System.currentTimeMillis())
        laborDetailsDao.update(stamped)
        syncScope.launch {
            runCatching { syncService.syncLaborDetails(stamped) }
                .onFailure { Log.w(TAG, "Labor sync failed: ${it.message}") }
        }
    }

    override suspend fun deleteWorker(worker: LaborDetailsEntity) {
        laborDetailsDao.delete(worker)
        syncScope.launch {
            runCatching { syncService.deleteLaborDetails(worker.id) }
                .onFailure { Log.w(TAG, "Labor delete sync failed: ${it.message}") }
        }
    }

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAll()

    override fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAll()
}
