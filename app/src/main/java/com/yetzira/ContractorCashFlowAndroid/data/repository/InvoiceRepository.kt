package com.yetzira.ContractorCashFlowAndroid.data.repository

import android.util.Log
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ClientDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.InvoiceDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ProjectDao
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.sync.FirestoreSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private const val TAG = "InvoiceRepository"

interface InvoiceRepositoryContract {
    fun getAllInvoices(): Flow<List<InvoiceEntity>>
    suspend fun getInvoiceById(id: String): InvoiceEntity?
    suspend fun insertInvoice(invoice: InvoiceEntity)
    suspend fun updateInvoice(invoice: InvoiceEntity)
    suspend fun deleteInvoice(invoice: InvoiceEntity)
    fun getAllClients(): Flow<List<ClientEntity>>
    suspend fun insertClient(client: ClientEntity)
    fun getAllProjects(): Flow<List<ProjectEntity>>
}

class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val clientDao: ClientDao,
    private val projectDao: ProjectDao,
    private val syncService: FirestoreSyncService
) : InvoiceRepositoryContract {
    // Fire-and-forget scope: sync failures never block the caller
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun getAllInvoices(): Flow<List<InvoiceEntity>> = invoiceDao.getAll()

    override suspend fun getInvoiceById(id: String): InvoiceEntity? = invoiceDao.getById(id)

    override suspend fun insertInvoice(invoice: InvoiceEntity) {
        val stamped = invoice.copy(lastModified = System.currentTimeMillis())
        invoiceDao.insert(stamped)
        syncScope.launch {
            runCatching { syncService.syncInvoice(stamped) }
                .onFailure { Log.w(TAG, "Invoice sync failed: ${it.message}") }
        }
    }

    override suspend fun updateInvoice(invoice: InvoiceEntity) {
        val stamped = invoice.copy(lastModified = System.currentTimeMillis())
        invoiceDao.update(stamped)
        syncScope.launch {
            runCatching { syncService.syncInvoice(stamped) }
                .onFailure { Log.w(TAG, "Invoice sync failed: ${it.message}") }
        }
    }

    override suspend fun deleteInvoice(invoice: InvoiceEntity) {
        invoiceDao.delete(invoice)
        syncScope.launch {
            runCatching { syncService.deleteInvoice(invoice.id) }
                .onFailure { Log.w(TAG, "Invoice delete sync failed: ${it.message}") }
        }
    }

    override fun getAllClients(): Flow<List<ClientEntity>> = clientDao.getAll()

    override suspend fun insertClient(client: ClientEntity) {
        val stamped = client.copy(lastModified = System.currentTimeMillis())
        clientDao.insert(stamped)
        syncScope.launch {
            runCatching { syncService.syncClient(stamped) }
                .onFailure { Log.w(TAG, "Client sync failed: ${it.message}") }
        }
    }

    override fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAll()
}
