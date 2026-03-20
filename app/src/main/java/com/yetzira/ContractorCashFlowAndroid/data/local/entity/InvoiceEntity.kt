package com.yetzira.ContractorCashFlowAndroid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("projectId"),
        Index("dueDate"),
        Index("createdDate")
    ]
)
data class InvoiceEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val dueDate: Long,
    val isPaid: Boolean = false,
    val clientName: String,
    val createdDate: Long = System.currentTimeMillis(),
    val projectId: String? = null,
    val lastModified: Long = System.currentTimeMillis()
)

