package com.yetzira.ContractorCashFlowAndroid.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.yetzira.ContractorCashFlowAndroid.data.preferences.SubscriptionPreferencesRepositoryContract
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

class PurchaseManager(
    context: Context,
    private val preferencesRepository: SubscriptionPreferencesRepositoryContract
) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _isProUser = MutableStateFlow(false)
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _activePurchase = MutableStateFlow<Purchase?>(null)
    val activePurchase: StateFlow<Purchase?> = _activePurchase.asStateFlow()

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        Log.d(TAG, "purchasesUpdated: code=${billingResult.responseCode} msg='${billingResult.debugMessage}' purchases=${purchases?.size ?: 0}")
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases.orEmpty().forEach { purchase ->
                    Log.d(TAG, "  purchase: products=${purchase.products} state=${purchase.purchaseState} acknowledged=${purchase.isAcknowledged} token=${purchase.purchaseToken.take(20)}…")
                    scope.launch { handlePurchase(purchase) }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> Log.d(TAG, "  user cancelled purchase")
            else -> {
                Log.w(TAG, "  purchase FAILED: code=${billingResult.responseCode} msg='${billingResult.debugMessage}'")
                _errorMessage.value = "Purchase failed: ${billingResult.debugMessage}"
            }
        }
        _isPurchasing.value = false
    }

    private val billingClient = BillingClient.newBuilder(appContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .build()

    @Volatile
    private var isConnected = false
    private val connectionMutex = Mutex()
    private var reconnectJob: Job? = null

    init {
        connectAndLoad()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun reconnectIfNeeded() {
        if (!isConnected || !billingClient.isReady) {
            connectAndLoad()
        }
    }

    fun connectAndLoad() {
        Log.d(TAG, "connectAndLoad: starting (isReady=${billingClient.isReady})")
        scope.launch {
            if (ensureConnected()) {
                checkCurrentEntitlements()
                loadProducts()
            } else {
                Log.w(TAG, "connectAndLoad: billing not connected, skipping load")
            }
        }
    }

    /** Suspends until the BillingClient is ready (or returns immediately if already connected). */
    private suspend fun ensureConnected(): Boolean {
        if (billingClient.isReady) {
            isConnected = true
            Log.d(TAG, "ensureConnected: already ready")
            return true
        }

        return connectionMutex.withLock {
            if (billingClient.isReady) {
                isConnected = true
                return@withLock true
            }

            Log.d(TAG, "ensureConnected: starting connection…")
            suspendCancellableCoroutine { continuation ->
                billingClient.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        isConnected = result.responseCode == BillingClient.BillingResponseCode.OK
                        Log.d(TAG, "onBillingSetupFinished: code=${result.responseCode} connected=$isConnected msg='${result.debugMessage}'")
                        if (!isConnected && result.responseCode != BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                            _errorMessage.value = "Billing unavailable: ${result.debugMessage}"
                        }
                        if (continuation.isActive) continuation.resume(isConnected)
                    }

                    override fun onBillingServiceDisconnected() {
                        Log.w(TAG, "onBillingServiceDisconnected")
                        isConnected = false
                        scheduleReconnect()
                    }
                })
            }
        }
    }

    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            delay(RECONNECT_DELAY_MS)
            ensureConnected()
        }
    }

    suspend fun loadProducts() {
        Log.d(TAG, "loadProducts: querying ${BillingProduct.ALL_IDS}")
        _isLoading.value = true
        try {
            if (!ensureConnected()) {
                Log.w(TAG, "loadProducts: not connected, aborting")
                return
            }
            val products = BillingProduct.ALL_IDS.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }

            suspend fun queryProducts(): Pair<BillingResult, List<ProductDetails>> =
                suspendCancellableCoroutine { continuation ->
                    billingClient.queryProductDetailsAsync(
                        QueryProductDetailsParams.newBuilder()
                            .setProductList(products)
                            .build()
                    ) { billingResult, productDetailsList ->
                        if (continuation.isActive) {
                            continuation.resume(billingResult to productDetailsList)
                        }
                    }
                }

            var result = queryProducts()
            if (result.first.responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                Log.w(TAG, "loadProducts: SERVICE_DISCONNECTED, reconnecting and retrying once")
                isConnected = false
                if (ensureConnected()) {
                    result = queryProducts()
                }
            }

            Log.d(TAG, "loadProducts: response code=${result.first.responseCode} msg='${result.first.debugMessage}' count=${result.second.size}")
            if (result.first.responseCode == BillingClient.BillingResponseCode.OK) {
                result.second.forEach { details ->
                    Log.d(TAG, "  product: id=${details.productId} name='${details.name}'")
                    details.subscriptionOfferDetails?.forEach { offer ->
                        Log.d(TAG, "    offer: basePlanId=${offer.basePlanId} offerId=${offer.offerId} token=${offer.offerToken.take(20)}…")
                        offer.pricingPhases.pricingPhaseList.forEach { phase ->
                            Log.d(TAG, "      phase: price=${phase.formattedPrice} micros=${phase.priceAmountMicros} period=${phase.billingPeriod} cycles=${phase.billingCycleCount} mode=${phase.recurrenceMode}")
                        }
                    }
                }
                _products.value = result.second.sortedBy { details ->
                    if (details.productId == BillingProduct.PRO_MONTHLY) 0 else 1
                }
            } else {
                Log.w(TAG, "loadProducts: FAILED code=${result.first.responseCode} msg='${result.first.debugMessage}'")
                _errorMessage.value = "Failed to load products: ${result.first.debugMessage}"
            }
        } finally {
            _isLoading.value = false
        }
    }

    fun launchPurchaseFlow(
        activity: Activity,
        productDetails: ProductDetails,
        basePlanId: String
    ) {
        Log.d(TAG, "launchPurchaseFlow: productId=${productDetails.productId} basePlanId=$basePlanId")
        Log.d(TAG, "  available offers: ${productDetails.subscriptionOfferDetails?.map { "${it.basePlanId}/${it.offerId}" }}")

        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull { it.basePlanId == basePlanId }
            ?.offerToken

        if (offerToken == null) {
            Log.e(TAG, "launchPurchaseFlow: no offer token found for basePlanId=$basePlanId")
            _errorMessage.value = "No active offer found for ${productDetails.productId}"
            return
        }

        Log.d(TAG, "launchPurchaseFlow: using offerToken=${offerToken.take(20)}…")
        _isPurchasing.value = true

        scope.launch {
            if (!ensureConnected()) {
                Log.w(TAG, "launchPurchaseFlow: not connected, aborting")
                _errorMessage.value = "Billing not available. Please try again."
                _isPurchasing.value = false
                return@launch
            }

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken)
                            .build()
                    )
                )
                .build()

            val result = billingClient.launchBillingFlow(activity, billingFlowParams)
            Log.d(TAG, "launchBillingFlow result: code=${result.responseCode} msg='${result.debugMessage}'")
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "launchBillingFlow: non-OK response, resetting isPurchasing")
                when (result.responseCode) {
                    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                        isConnected = false
                        scheduleReconnect()
                        _errorMessage.value = "Billing temporarily disconnected. Please try again in a moment."
                    }
                    BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                        _errorMessage.value = "Purchase unavailable: app must be installed from Google Play (internal testing track)."
                    }
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                        _errorMessage.value = "You already own this subscription."
                        checkCurrentEntitlements()
                    }
                    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                        _errorMessage.value = "This subscription is currently unavailable."
                    }
                    else -> {
                        _errorMessage.value = "Purchase failed (code ${result.responseCode}): ${result.debugMessage}"
                    }
                }
                _isPurchasing.value = false
            }
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        Log.d(TAG, "handlePurchase: products=${purchase.products} state=${purchase.purchaseState} acknowledged=${purchase.isAcknowledged}")
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            Log.d(TAG, "handlePurchase: skipping — state is ${purchase.purchaseState}")
            return
        }

        if (!purchase.isAcknowledged) {
            Log.d(TAG, "handlePurchase: acknowledging purchase token=${purchase.purchaseToken.take(20)}…")
            suspend fun acknowledge(): BillingResult =
                suspendCancellableCoroutine { continuation ->
                    billingClient.acknowledgePurchase(
                        AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                    ) { ackResult ->
                        if (continuation.isActive) {
                            continuation.resume(ackResult)
                        }
                    }
                }

            var result = acknowledge()
            if (result.responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                Log.w(TAG, "handlePurchase: SERVICE_DISCONNECTED during acknowledge, reconnecting and retrying once")
                isConnected = false
                if (ensureConnected()) {
                    result = acknowledge()
                }
            }

            Log.d(TAG, "handlePurchase: ack result code=${result.responseCode} msg='${result.debugMessage}'")
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.e(TAG, "handlePurchase: acknowledgment FAILED")
                _errorMessage.value = "Acknowledgment failed: ${result.debugMessage}"
                return
            }
        } else {
            Log.d(TAG, "handlePurchase: already acknowledged")
        }

        checkCurrentEntitlements()
    }

    suspend fun checkCurrentEntitlements() {
        Log.d(TAG, "checkCurrentEntitlements: querying active subscriptions")
        if (!ensureConnected()) {
            Log.w(TAG, "checkCurrentEntitlements: not connected, aborting")
            return
        }

        suspend fun queryEntitlements(): Pair<BillingResult, List<Purchase>> =
            suspendCancellableCoroutine { continuation ->
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

        var result = queryEntitlements()
        if (result.first.responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
            Log.w(TAG, "checkCurrentEntitlements: SERVICE_DISCONNECTED, reconnecting and retrying once")
            isConnected = false
            if (ensureConnected()) {
                result = queryEntitlements()
            }
        }

        Log.d(TAG, "checkCurrentEntitlements: response code=${result.first.responseCode} total purchases=${result.second.size}")
        if (result.first.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "checkCurrentEntitlements: FAILED msg='${result.first.debugMessage}'")
            _errorMessage.value = "Failed to refresh purchases: ${result.first.debugMessage}"
            return
        }

        result.second.forEach { p ->
            Log.d(TAG, "  purchase: products=${p.products} state=${p.purchaseState} acknowledged=${p.isAcknowledged} token=${p.purchaseToken.take(20)}…")
        }

        val activeProPurchase = result.second.firstOrNull { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                purchase.products.any { it in BillingProduct.ALL_IDS }
        }

        Log.d(TAG, "checkCurrentEntitlements: activeProPurchase=${activeProPurchase?.products} → isProUser=${activeProPurchase != null}")
        _activePurchase.value = activeProPurchase
        _isProUser.value = activeProPurchase != null

        val planName = when {
            activeProPurchase?.products?.contains(BillingProduct.PRO_YEARLY) == true -> "Pro Yearly"
            activeProPurchase?.products?.contains(BillingProduct.PRO_MONTHLY) == true -> "Pro Monthly"
            else -> null
        }
        preferencesRepository.setSubscription(
            isPro = activeProPurchase != null,
            planName = planName,
            renewalDate = null
        )

        result.second
            .filter { !it.isAcknowledged && it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .forEach { purchase ->
                Log.d(TAG, "checkCurrentEntitlements: found unacknowledged purchase, handling…")
                handlePurchase(purchase)
            }
    }

    suspend fun restorePurchases() {
        Log.d(TAG, "restorePurchases: delegating to checkCurrentEntitlements")
        _isRestoring.value = true
        try {
            checkCurrentEntitlements()
        } finally {
            _isRestoring.value = false
        }
    }

    fun canCreateProject(currentCount: Int): Boolean =
        _isProUser.value || currentCount < FreeTierLimit.MAX_PROJECTS

    fun canCreateExpense(currentCount: Int): Boolean =
        true

    fun canCreateInvoice(currentCount: Int): Boolean =
        true

    fun canCreateWorker(currentCount: Int): Boolean =
        _isProUser.value || currentCount < FreeTierLimit.MAX_WORKERS

    fun openManageSubscriptions(context: Context) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/account/subscriptions")
        )
        context.startActivity(intent)
    }

    fun destroy() {
        Log.d(TAG, "destroy: ending billing connection")
        billingClient.endConnection()
        scope.cancel()
    }

    companion object {
        private const val TAG = "BillingDebug"
        private const val RECONNECT_DELAY_MS = 750L
    }
}

object PurchaseManagerProvider {
    @Volatile
    private var instance: PurchaseManager? = null

    fun getInstance(context: Context): PurchaseManager {
        return instance ?: synchronized(this) {
            instance ?: PurchaseManager(
                context = context.applicationContext,
                preferencesRepository = UserPreferencesRepository(context.applicationContext)
            ).also { instance = it }
        }
    }
}

