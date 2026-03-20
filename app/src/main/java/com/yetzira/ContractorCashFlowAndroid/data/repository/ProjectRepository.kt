package com.yetzira.ContractorCashFlowAndroid.data.repository

import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ProjectDao
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

class ProjectRepository(
    private val projectDao: ProjectDao
) {
    fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAll()

    fun searchProjects(query: String): Flow<List<ProjectEntity>> = projectDao.search(query)

    suspend fun getProjectById(id: String): ProjectEntity? = projectDao.getById(id)

    suspend fun insertProject(project: ProjectEntity) = projectDao.insert(project)

    suspend fun updateProject(project: ProjectEntity) = projectDao.update(project)

    suspend fun deleteProject(project: ProjectEntity) = projectDao.delete(project)
}

