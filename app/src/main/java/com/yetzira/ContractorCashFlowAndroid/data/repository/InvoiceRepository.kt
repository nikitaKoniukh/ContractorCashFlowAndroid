package com.yetzira.ContractorCashFlowAndroid.data.repository

import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ClientDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.InvoiceDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ProjectDao
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.sync.FirestoreSyncService
import kotlinx.coroutines.flow.Flow

class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val clientDao: ClientDao,
    private val projectDao: ProjectDao,
    private val syncService: FirestoreSyncService
) {
    fun getAllInvoices(): Flow<List<InvoiceEntity>> = invoiceDao.getAll()

    suspend fun getInvoiceById(id: String): InvoiceEntity? = invoiceDao.getById(id)

    suspend fun insertInvoice(invoice: InvoiceEntity) {
        val stamped = invoice.copy(lastModified = System.currentTimeMillis())
        invoiceDao.insert(stamped)
        syncService.syncInvoice(stamped)
    }

    suspend fun updateInvoice(invoice: InvoiceEntity) {
        val stamped = invoice.copy(lastModified = System.currentTimeMillis())
        invoiceDao.update(stamped)
        syncService.syncInvoice(stamped)
    }

    suspend fun deleteInvoice(invoice: InvoiceEntity) {
        invoiceDao.delete(invoice)
        syncService.deleteInvoice(invoice.id)
    }

    fun getAllClients(): Flow<List<ClientEntity>> = clientDao.getAll()

    suspend fun insertClient(client: ClientEntity) {
        val stamped = client.copy(lastModified = System.currentTimeMillis())
        clientDao.insert(stamped)
        syncService.syncClient(stamped)
    }

    fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAll()
}

