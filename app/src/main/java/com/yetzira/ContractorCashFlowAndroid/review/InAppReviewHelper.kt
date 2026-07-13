package com.yetzira.ContractorCashFlowAndroid.review

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

/**
 * Mirrors iOS review prompt rules:
 * - After the first project is saved, or after the 5th expense is saved
 * - Show a soft ask; on accept launch Play In-App Review
 */
object InAppReviewHelper {
    private const val EXPENSE_THRESHOLD = 5L

    suspend fun onProjectCreated(context: Context) {
        val prefs = UserPreferencesRepository(context)
        if (prefs.hasRated.first()) return
        prefs.setPendingReviewPrompt(true)
    }

    suspend fun onExpenseCreated(context: Context) {
        val prefs = UserPreferencesRepository(context)
        if (prefs.hasRated.first() || prefs.declinedReviewAfterExpenses.first()) return
        val count = (prefs.savedExpenseCount.first() ?: 0L) + 1L
        prefs.setSavedExpenseCount(count)
        if (count >= EXPENSE_THRESHOLD) {
            prefs.setPendingReviewPrompt(true)
        }
    }

    suspend fun decline(context: Context) {
        val prefs = UserPreferencesRepository(context)
        prefs.setPendingReviewPrompt(false)
        prefs.setDeclinedReviewAfterExpenses(true)
    }

    suspend fun acceptAndLaunch(activity: Activity) {
        val prefs = UserPreferencesRepository(activity)
        prefs.setPendingReviewPrompt(false)
        prefs.setHasRated(true)
        runCatching {
            val manager = ReviewManagerFactory.create(activity)
            val request = manager.requestReviewFlow().await()
            manager.launchReviewFlow(activity, request).await()
        }
    }
}
