package com.yetzira.ContractorCashFlowAndroid.data.repository

import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ClientDao
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

class ClientRepository(
    private val clientDao: ClientDao
) {
    fun getAllClients(): Flow<List<ClientEntity>> = clientDao.getAll()

    fun searchClients(query: String): Flow<List<ClientEntity>> = clientDao.search(query)

    suspend fun getClientById(id: String): ClientEntity? = clientDao.getById(id)

    suspend fun insertClient(client: ClientEntity) = clientDao.insert(client)

    suspend fun updateClient(client: ClientEntity) = clientDao.update(client)

    suspend fun deleteClient(client: ClientEntity) = clientDao.delete(client)
}

