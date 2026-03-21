package com.yetzira.ContractorCashFlowAndroid.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.yetzira.ContractorCashFlowAndroid.BuildConfig
import com.yetzira.ContractorCashFlowAndroid.data.preferences.SubscriptionPreferencesRepositoryContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

class PlayBillingRepository(
    context: Context,
    private val preferencesRepository: SubscriptionPreferencesRepositoryContract
) : BillingRepositoryContract {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectionMutex = Mutex()

    private val _productState = MutableStateFlow(BillingProductState())
    override val productState: StateFlow<BillingProductState> = _productState.asStateFlow()

    private val _entitlementState = MutableStateFlow(BillingEntitlementState())
    override val entitlementState: StateFlow<BillingEntitlementState> = _entitlementState.asStateFlow()

    private val _actionState = MutableStateFlow<BillingActionState>(BillingActionState.Idle)
    override val actionState: StateFlow<BillingActionState> = _actionState.asStateFlow()

    @Volatile
    private var isConnected: Boolean = false
    private var cachedProductDetails: ProductDetails? = null
    private var cachedOfferToken: String? = null

    init {
        scope.launch {
            refresh()
        }
    }

    override suspend fun refresh(): Result<Unit> = runCatching {
        _actionState.value = BillingActionState.Loading
        ensureConnected()
        querySubscriptionProduct()
        syncExistingPurchases(restored = true)
        if (_actionState.value == BillingActionState.Loading) {
            _actionState.value = BillingActionState.Idle
        }
    }

    override suspend fun launchPurchase(activity: Activity): Result<Unit> = runCatching {
        ensureConnected()
        if (!productState.value.isAvailable || cachedProductDetails == null) {
            querySubscriptionProduct()
        }

        val details = cachedProductDetails ?: error("Subscription product details are unavailable")
        val offerToken = cachedOfferToken ?: error("No eligible subscription offer token was found")

        _actionState.value = BillingActionState.LaunchingPurchase

        val productDetailsParams = BillingFlowParams.ProductDetailsParams
            .newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()

        val billingResult = billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()
        )

        if (billingResult.responseCode != BillingResponseCode.OK) {
            _actionState.value = when (billingResult.responseCode) {
                BillingResponseCode.USER_CANCELED -> BillingActionState.UserCancelled
                else -> BillingActionState.Error(billingResult.debugMessage.ifBlank { "Unable to launch purchase flow" })
            }
            error(billingResult.debugMessage.ifBlank { "Unable to launch purchase flow" })
        }
    }

    override fun clearActionState() {
        _actionState.value = BillingActionState.Idle
    }

    override fun close() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
        isConnected = false
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                scope.launch {
                    processPurchases(purchases.orEmpty(), restored = false)
                }
            }
            BillingResponseCode.USER_CANCELED -> {
                _actionState.value = BillingActionState.UserCancelled
            }
            BillingResponseCode.ITEM_ALREADY_OWNED -> {
                scope.launch {
                    syncExistingPurchases(restored = true)
                }
            }
            else -> {
                _actionState.value = BillingActionState.Error(
                    billingResult.debugMessage.ifBlank { "Billing request failed" }
                )
            }
        }
    }

    private val billingClient: BillingClient = BillingClient
        .newBuilder(appContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    private suspend fun ensureConnected() {
        if (isConnected && billingClient.isReady) return

        connectionMutex.withLock {
            if (isConnected && billingClient.isReady) return@withLock
            val result = suspendCancellableCoroutine<BillingResult> { continuation ->
                billingClient.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        if (billingResult.responseCode == BillingResponseCode.OK) {
                            isConnected = true
                        }
                        if (continuation.isActive) {
                            continuation.resume(billingResult)
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        isConnected = false
                    }
                })
            }

            if (result.responseCode != BillingResponseCode.OK) {
                throw IllegalStateException(result.debugMessage.ifBlank { "Google Play Billing is unavailable" })
            }
        }
    }

    private suspend fun querySubscriptionProduct() {
        _productState.value = _productState.value.copy(isLoading = true, errorMessage = null)

        val queryResult = suspendCancellableCoroutine<Pair<BillingResult, List<ProductDetails>>> { continuation ->
            val product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(subscriptionProductId())
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder()
                    .setProductList(listOf(product))
                    .build()
            ) { billingResult, productDetailsList ->
                if (continuation.isActive) {
                    continuation.resume(billingResult to productDetailsList)
                }
            }
        }

        val billingResult = queryResult.first
        val productDetailsList = queryResult.second
        if (billingResult.responseCode != BillingResponseCode.OK) {
            cachedProductDetails = null
            cachedOfferToken = null
            _productState.value = BillingProductState(
                isLoading = false,
                errorMessage = billingResult.debugMessage.ifBlank { "Unable to load subscription details" }
            )
            throw IllegalStateException(_productState.value.errorMessage.orEmpty())
        }

        val productDetails = productDetailsList.firstOrNull { it.productId == subscriptionProductId() }
        if (productDetails == null) {
            cachedProductDetails = null
            cachedOfferToken = null
            _productState.value = BillingProductState(
                isLoading = false,
                errorMessage = "Subscription product '${subscriptionProductId()}' was not found in Google Play"
            )
            return
        }

        val selectedOffer = selectOffer(productDetails)
        cachedProductDetails = productDetails
        cachedOfferToken = selectedOffer?.offerToken

        val pricingPhase = selectedOffer?.pricingPhases?.pricingPhaseList?.lastOrNull()
        _productState.value = BillingProductState(
            isLoading = false,
            title = productDetails.name.takeIf { it.isNotBlank() } ?: productDetails.title,
            description = productDetails.description,
            priceText = pricingPhase?.formattedPrice,
            isAvailable = selectedOffer != null,
            errorMessage = if (selectedOffer == null) "No active base plan/offer is available for ${subscriptionProductId()}" else null
        )
    }

    private suspend fun syncExistingPurchases(restored: Boolean) {
        _entitlementState.value = _entitlementState.value.copy(isLoading = true)

        val queryResult = suspendCancellableCoroutine<Pair<BillingResult, List<Purchase>>> { continuation ->
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            ) { billingResult, purchases ->
                if (continuation.isActive) {
                    continuation.resume(billingResult to purchases)
                }
            }
        }

        val billingResult = queryResult.first
        if (billingResult.responseCode != BillingResponseCode.OK) {
            _entitlementState.value = _entitlementState.value.copy(isLoading = false)
            _actionState.value = BillingActionState.Error(
                billingResult.debugMessage.ifBlank { "Unable to refresh subscription" }
            )
            return
        }

        processPurchases(queryResult.second, restored)
    }

    private suspend fun processPurchases(purchases: List<Purchase>, restored: Boolean) {
        val relevantPurchases = purchases.filter { purchase ->
            purchase.products.contains(subscriptionProductId())
        }
        val purchased = relevantPurchases.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        val pending = relevantPurchases.firstOrNull { it.purchaseState == Purchase.PurchaseState.PENDING }

        when {
            purchased != null -> {
                acknowledgeIfNeeded(purchased)
                val resolvedPlanName = productState.value.title
                    ?: BuildConfig.PLAY_BILLING_SUBSCRIPTION_PRODUCT_ID
                    .ifBlank { DEFAULT_PLAN_NAME }
                preferencesRepository.setSubscription(
                    isPro = true,
                    planName = resolvedPlanName,
                    renewalDate = null
                )
                _entitlementState.value = BillingEntitlementState(
                    isLoading = false,
                    isPro = true,
                    planName = resolvedPlanName,
                    renewalDate = null,
                    autoRenewing = purchased.isAutoRenewing,
                    purchaseToken = purchased.purchaseToken
                )
                _actionState.value = if (restored) BillingActionState.Restored else BillingActionState.Purchased
            }
            pending != null -> {
                preferencesRepository.setSubscription(
                    isPro = false,
                    planName = null,
                    renewalDate = null
                )
                _entitlementState.value = BillingEntitlementState(
                    isLoading = false,
                    isPro = false,
                    planName = null,
                    renewalDate = null,
                    autoRenewing = false,
                    purchaseToken = pending.purchaseToken
                )
                _actionState.value = BillingActionState.Pending(
                    "Your purchase is pending approval in Google Play"
                )
            }
            else -> {
                preferencesRepository.setSubscription(
                    isPro = false,
                    planName = null,
                    renewalDate = null
                )
                _entitlementState.value = BillingEntitlementState(
                    isLoading = false,
                    isPro = false,
                    planName = null,
                    renewalDate = null,
                    autoRenewing = false,
                    purchaseToken = null
                )
                _actionState.value = if (restored) BillingActionState.Idle else _actionState.value
            }
        }
    }

    private suspend fun acknowledgeIfNeeded(purchase: Purchase) {
        if (purchase.isAcknowledged) return

        val billingResult = suspendCancellableCoroutine<BillingResult> { continuation ->
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            ) { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
        }

        if (billingResult.responseCode != BillingResponseCode.OK) {
            throw IllegalStateException(
                billingResult.debugMessage.ifBlank { "Unable to acknowledge subscription purchase" }
            )
        }
    }

    private fun selectOffer(productDetails: ProductDetails): ProductDetails.SubscriptionOfferDetails? {
        val offers = productDetails.subscriptionOfferDetails.orEmpty()
        if (offers.isEmpty()) return null

        val configuredBasePlanId = BuildConfig.PLAY_BILLING_SUBSCRIPTION_BASE_PLAN_ID
        val configuredOfferId = BuildConfig.PLAY_BILLING_SUBSCRIPTION_OFFER_ID

        return when {
            configuredBasePlanId.isNotBlank() && configuredOfferId.isNotBlank() -> {
                offers.firstOrNull { it.basePlanId == configuredBasePlanId && it.offerId == configuredOfferId }
            }
            configuredBasePlanId.isNotBlank() -> {
                offers.firstOrNull { it.basePlanId == configuredBasePlanId }
            }
            else -> offers.firstOrNull()
        }
    }

    private fun subscriptionProductId(): String =
        BuildConfig.PLAY_BILLING_SUBSCRIPTION_PRODUCT_ID.ifBlank { DEFAULT_PRODUCT_ID }

    private companion object {
        const val DEFAULT_PRODUCT_ID = "kablanpro_pro"
        const val DEFAULT_PLAN_NAME = "KablanPro Pro"
    }
}

