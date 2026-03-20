package com.yetzira.ContractorCashFlowAndroid.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY createdDate DESC")
    fun getAll(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getById(id: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE clientName LIKE '%' || :query || '%' ORDER BY createdDate DESC")
    fun search(query: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE projectId = :projectId ORDER BY createdDate DESC")
    fun getForProject(projectId: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE isPaid = 0 ORDER BY dueDate ASC")
    fun getUnpaid(): Flow<List<InvoiceEntity>>

    @Insert
    suspend fun insert(invoice: InvoiceEntity)

    @Update
    suspend fun update(invoice: InvoiceEntity)

    @Delete
    suspend fun delete(invoice: InvoiceEntity)
}

