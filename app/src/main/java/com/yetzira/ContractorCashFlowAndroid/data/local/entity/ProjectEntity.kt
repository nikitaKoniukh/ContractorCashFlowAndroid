package com.yetzira.ContractorCashFlowAndroid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val clientName: String,
    val budget: Double,
    val createdDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val lastModified: Long = System.currentTimeMillis()
)

