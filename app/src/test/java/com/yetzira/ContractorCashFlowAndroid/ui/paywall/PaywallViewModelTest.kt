package com.yetzira.ContractorCashFlowAndroid.ui.paywall

import com.yetzira.ContractorCashFlowAndroid.data.preferences.SubscriptionPreferencesRepositoryContract
import com.yetzira.ContractorCashFlowAndroid.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaywallViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `activatePro stores pro subscription and invokes callback`() = runTest {
        val repository = FakeSubscriptionPreferencesRepository()
        val viewModel = PaywallViewModel(repository)
        val start = System.currentTimeMillis()
        var onDoneCalled = false

        viewModel.activatePro {
            onDoneCalled = true
        }
        advanceUntilIdle()
        val end = System.currentTimeMillis()

        assertEquals(true, repository.isPro)
        assertEquals("KablanPro Pro", repository.planName)
        assertNotNull(repository.renewalDate)
        val expectedMin = start + 30L * 24 * 60 * 60 * 1000
        val expectedMax = end + 30L * 24 * 60 * 60 * 1000
        assertTrue(repository.renewalDate!! in expectedMin..expectedMax)
        assertTrue(onDoneCalled)
    }

    private class FakeSubscriptionPreferencesRepository : SubscriptionPreferencesRepositoryContract {
        var isPro: Boolean? = null
        var planName: String? = null
        var renewalDate: Long? = null

        override suspend fun setSubscription(
            isPro: Boolean,
            planName: String?,
            renewalDate: Long?
        ) {
            this.isPro = isPro
            this.planName = planName
            this.renewalDate = renewalDate
        }
    }
}

