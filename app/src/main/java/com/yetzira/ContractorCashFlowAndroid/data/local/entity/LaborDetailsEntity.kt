package com.yetzira.ContractorCashFlowAndroid.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "labor_details",
    indices = [
        Index("workerName"),
        Index("createdDate")
    ]
)
data class LaborDetailsEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val workerName: String,
    val laborType: String,
    val hourlyRate: Double? = null,
    val dailyRate: Double? = null,
    val contractPrice: Double? = null,
    val notes: String? = null,
    val createdDate: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)

