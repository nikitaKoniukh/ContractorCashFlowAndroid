package com.yetzira.ContractorCashFlowAndroid.data.repository

import android.util.Log
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ClientDao
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import com.yetzira.ContractorCashFlowAndroid.sync.FirestoreSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ClientRepository(
    private val clientDao: ClientDao,
    private val syncService: FirestoreSyncService
) {
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun getAllClients(): Flow<List<ClientEntity>> = clientDao.getAll()

    fun searchClients(query: String): Flow<List<ClientEntity>> = clientDao.search(query)

    suspend fun getClientById(id: String): ClientEntity? = clientDao.getById(id)

    suspend fun insertClient(client: ClientEntity) {
        val stamped = client.copy(lastModified = System.currentTimeMillis())
        clientDao.insert(stamped)
        syncScope.launch {
            syncService.syncClient(stamped).onFailure { throwable ->
                Log.w(TAG, "Client cloud sync failed after local insert: ${throwable.message}")
            }
        }
    }

    suspend fun updateClient(client: ClientEntity) {
        val stamped = client.copy(lastModified = System.currentTimeMillis())
        clientDao.update(stamped)
        syncScope.launch {
            syncService.syncClient(stamped).onFailure { throwable ->
                Log.w(TAG, "Client cloud sync failed after local update: ${throwable.message}")
            }
        }
    }

    suspend fun deleteClient(client: ClientEntity) {
        clientDao.delete(client)
        syncScope.launch {
            syncService.deleteClient(client.id).onFailure { throwable ->
                Log.w(TAG, "Client cloud delete failed after local delete: ${throwable.message}")
            }
        }
    }

    private companion object {
        const val TAG = "KablanProClientRepo"
    }
}

