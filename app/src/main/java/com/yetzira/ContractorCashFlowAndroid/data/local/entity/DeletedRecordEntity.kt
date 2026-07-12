package com.yetzira.ContractorCashFlowAndroid.data.local.entity

import androidx.room.Entity

/**
 * Local outbox of deleted record IDs so a failed/offline remote delete
 * cannot be resurrected by the next fullSync pull.
 */
@Entity(
    tableName = "deleted_records",
    primaryKeys = ["collection", "recordId"]
)
data class DeletedRecordEntity(
    val collection: String,
    val recordId: String,
    val deletedAt: Long = System.currentTimeMillis()
)
