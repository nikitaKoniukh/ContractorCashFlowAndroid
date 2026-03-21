package com.yetzira.ContractorCashFlowAndroid.ui.paywall

import android.app.Activity
import com.yetzira.ContractorCashFlowAndroid.billing.BillingActionState
import com.yetzira.ContractorCashFlowAndroid.billing.BillingEntitlementState
import com.yetzira.ContractorCashFlowAndroid.billing.BillingProductState
import com.yetzira.ContractorCashFlowAndroid.billing.BillingRepositoryContract
import com.yetzira.ContractorCashFlowAndroid.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaywallViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `uiState reflects billing product and entitlement state`() = runTest {
        val repository = FakeBillingRepository().apply {
            product.value = BillingProductState(
                isLoading = false,
                title = "KablanPro Pro",
                description = "Premium plan",
                priceText = "$9.99",
                isAvailable = true
            )
            entitlement.value = BillingEntitlementState(
                isLoading = false,
                isPro = true,
                planName = "KablanPro Pro"
            )
            action.value = BillingActionState.Purchased
        }
        val viewModel = PaywallViewModel(repository)
        val collectJob = launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isPro)
        assertEquals("KablanPro Pro", viewModel.uiState.value.productTitle)
        assertEquals(PaywallStatus.PURCHASED, viewModel.uiState.value.status)

        collectJob.cancel()
    }

    @Test
    fun `purchase delegates to billing repository`() = runTest {
        val repository = FakeBillingRepository()
        val viewModel = PaywallViewModel(repository)
        val activity = FakeActivity()

        viewModel.purchase(activity)
        advanceUntilIdle()

        assertEquals(1, repository.purchaseLaunchCount)
    }

    @Test
    fun `restorePurchases delegates to refresh`() = runTest {
        val repository = FakeBillingRepository()
        val viewModel = PaywallViewModel(repository)

        viewModel.restorePurchases()
        advanceUntilIdle()

        assertTrue(repository.refreshCount >= 2)
    }

    private class FakeBillingRepository : BillingRepositoryContract {
        val product = MutableStateFlow(BillingProductState(isLoading = false))
        val entitlement = MutableStateFlow(BillingEntitlementState(isLoading = false))
        val action = MutableStateFlow<BillingActionState>(BillingActionState.Idle)

        var refreshCount = 0
        var purchaseLaunchCount = 0

        override val productState: StateFlow<BillingProductState> = product
        override val entitlementState: StateFlow<BillingEntitlementState> = entitlement
        override val actionState: StateFlow<BillingActionState> = action

        override suspend fun refresh(): Result<Unit> {
            refreshCount += 1
            return Result.success(Unit)
        }

        override suspend fun launchPurchase(activity: Activity): Result<Unit> {
            purchaseLaunchCount += 1
            return Result.success(Unit)
        }

        override fun clearActionState() {
            action.value = BillingActionState.Idle
        }

        override fun close() = Unit
    }

    private class FakeActivity : Activity()
}

