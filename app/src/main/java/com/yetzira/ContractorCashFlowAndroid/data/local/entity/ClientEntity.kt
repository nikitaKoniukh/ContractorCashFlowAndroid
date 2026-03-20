package com.yetzira.ContractorCashFlowAndroid.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "clients",
    indices = [
        Index("name")
    ]
)
data class ClientEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val notes: String? = null
)

