package com.yetzira.ContractorCashFlowAndroid.export

import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborDetailsEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity

data class AppDataSnapshot(
    val exportedAt: Long,
    val projects: List<ProjectEntity>,
    val expenses: List<ExpenseEntity>,
    val invoices: List<InvoiceEntity>,
    val clients: List<ClientEntity>,
    val labor: List<LaborDetailsEntity>
)

