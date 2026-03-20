package com.yetzira.ContractorCashFlowAndroid.data.repository

import android.util.Log
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ProjectDao
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.sync.FirestoreSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val syncService: FirestoreSyncService
) {
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAll()

    fun searchProjects(query: String): Flow<List<ProjectEntity>> = projectDao.search(query)

    suspend fun getProjectById(id: String): ProjectEntity? = projectDao.getById(id)

    suspend fun insertProject(project: ProjectEntity) {
        val stamped = project.copy(lastModified = System.currentTimeMillis())
        projectDao.insert(stamped)
        syncScope.launch {
            syncService.syncProject(stamped).onFailure { throwable ->
                Log.w(TAG, "Project cloud sync failed after local insert: ${throwable.message}")
            }
        }
    }

    suspend fun updateProject(project: ProjectEntity) {
        val stamped = project.copy(lastModified = System.currentTimeMillis())
        projectDao.update(stamped)
        syncScope.launch {
            syncService.syncProject(stamped).onFailure { throwable ->
                Log.w(TAG, "Project cloud sync failed after local update: ${throwable.message}")
            }
        }
    }

    suspend fun deleteProject(project: ProjectEntity) {
        projectDao.delete(project)
        syncScope.launch {
            syncService.deleteProject(project.id).onFailure { throwable ->
                Log.w(TAG, "Project cloud delete failed after local delete: ${throwable.message}")
            }
        }
    }

    private companion object {
        const val TAG = "KablanProProjectRepo"
    }
}

