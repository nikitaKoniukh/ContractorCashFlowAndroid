package com.yetzira.ContractorCashFlowAndroid.data.repository

import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ClientDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.InvoiceDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ProjectDao
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val clientDao: ClientDao,
    private val projectDao: ProjectDao
) {
    fun getAllInvoices(): Flow<List<InvoiceEntity>> = invoiceDao.getAll()

    suspend fun getInvoiceById(id: String): InvoiceEntity? = invoiceDao.getById(id)

    suspend fun insertInvoice(invoice: InvoiceEntity) = invoiceDao.insert(invoice)

    suspend fun updateInvoice(invoice: InvoiceEntity) = invoiceDao.update(invoice)

    suspend fun deleteInvoice(invoice: InvoiceEntity) = invoiceDao.delete(invoice)

    fun getAllClients(): Flow<List<ClientEntity>> = clientDao.getAll()

    suspend fun insertClient(client: ClientEntity) = clientDao.insert(client)

    fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAll()
}

