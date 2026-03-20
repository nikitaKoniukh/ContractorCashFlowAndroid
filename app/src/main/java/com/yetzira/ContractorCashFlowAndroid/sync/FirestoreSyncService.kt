package com.yetzira.ContractorCashFlowAndroid.sync

import android.content.Context
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.delay

class FirestoreSyncService(
    private val context: Context
) {
    suspend fun pullAllData(): Result<Unit> {
        delay(900)
        return if (FirebaseApp.getApps(context).isNotEmpty()) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("Firebase is not configured in this build."))
        }
    }
}

