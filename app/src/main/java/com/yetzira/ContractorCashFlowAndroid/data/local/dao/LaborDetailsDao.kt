package com.yetzira.ContractorCashFlowAndroid.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LaborDetailsDao {
    @Query("SELECT * FROM labor_details ORDER BY createdDate DESC")
    fun getAll(): Flow<List<LaborDetailsEntity>>

    @Query("SELECT * FROM labor_details WHERE id = :id")
    suspend fun getById(id: String): LaborDetailsEntity?

    @Query("SELECT * FROM labor_details WHERE workerName LIKE '%' || :query || '%' ORDER BY createdDate DESC")
    fun search(query: String): Flow<List<LaborDetailsEntity>>

    @Insert
    suspend fun insert(laborDetails: LaborDetailsEntity)

    @Update
    suspend fun update(laborDetails: LaborDetailsEntity)

    @Delete
    suspend fun delete(laborDetails: LaborDetailsEntity)
}

