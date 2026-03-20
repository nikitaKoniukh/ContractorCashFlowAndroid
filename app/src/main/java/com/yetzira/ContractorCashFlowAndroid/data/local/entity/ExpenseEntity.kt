package com.yetzira.ContractorCashFlowAndroid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LaborDetailsEntity::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("projectId"),
        Index("workerId"),
        Index("date")
    ]
)
data class ExpenseEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val category: String,
    val amount: Double,
    val descriptionText: String,
    val date: Long = System.currentTimeMillis(),
    val projectId: String? = null,
    val workerId: String? = null,
    val unitsWorked: Double? = null,
    val laborTypeSnapshot: String? = null,
    val lastModified: Long = System.currentTimeMillis()
)

