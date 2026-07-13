package com.yetzira.ContractorCashFlowAndroid.data.repository

import android.util.Log
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ExpenseDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ProjectDao
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.sync.FirestoreSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

interface ProjectRepositoryContract {
    fun getAllProjects(): Flow<List<ProjectEntity>>
    fun searchProjects(query: String): Flow<List<ProjectEntity>>
    suspend fun getProjectById(id: String): ProjectEntity?
    suspend fun insertProject(project: ProjectEntity)
    suspend fun updateProject(project: ProjectEntity)
    suspend fun deleteProject(project: ProjectEntity)
}

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val expenseDao: ExpenseDao,
    private val syncService: FirestoreSyncService
) : ProjectRepositoryContract {
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAll()

    override fun searchProjects(query: String): Flow<List<ProjectEntity>> = projectDao.search(query)

    override suspend fun getProjectById(id: String): ProjectEntity? = projectDao.getById(id)

    override suspend fun insertProject(project: ProjectEntity) {
        val stamped = project.copy(lastModified = System.currentTimeMillis())
        syncService.clearProjectTombstone(stamped.id)
        projectDao.insert(stamped)
        syncScope.launch {
            syncService.syncProject(stamped).onFailure { throwable ->
                Log.w(TAG, "Project cloud sync failed after local insert: ${throwable.message}")
            }
        }
    }

    override suspend fun updateProject(project: ProjectEntity) {
        val stamped = project.copy(lastModified = System.currentTimeMillis())
        projectDao.update(stamped)
        syncScope.launch {
            syncService.syncProject(stamped).onFailure { throwable ->
                Log.w(TAG, "Project cloud sync failed after local update: ${throwable.message}")
            }
        }
    }

    override suspend fun deleteProject(project: ProjectEntity) {
        // Capture child expenses before Room CASCADE removes them locally.
        val childExpenseIds = expenseDao.getForProject(project.id).first().map { it.id }
        projectDao.delete(project)
        syncScope.launch {
            childExpenseIds.forEach { expenseId ->
                syncService.deleteExpense(expenseId).onFailure { throwable ->
                    Log.w(TAG, "Cascaded expense cloud delete failed: ${throwable.message}")
                }
            }
            syncService.deleteProject(project.id).onFailure { throwable ->
                Log.w(TAG, "Project cloud delete failed after local delete: ${throwable.message}")
            }
        }
    }

    private companion object {
        const val TAG = "KablanProProjectRepo"
    }
}
