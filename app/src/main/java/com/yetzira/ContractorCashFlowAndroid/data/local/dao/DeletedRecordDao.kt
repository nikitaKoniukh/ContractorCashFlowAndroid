package com.yetzira.ContractorCashFlowAndroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.DeletedRecordEntity

@Dao
interface DeletedRecordDao {
    @Query("SELECT * FROM deleted_records")
    suspend fun getAll(): List<DeletedRecordEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM deleted_records WHERE collection = :collection AND recordId = :recordId)")
    suspend fun exists(collection: String, recordId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: DeletedRecordEntity)

    @Query("DELETE FROM deleted_records WHERE collection = :collection AND recordId = :recordId")
    suspend fun delete(collection: String, recordId: String)
}
