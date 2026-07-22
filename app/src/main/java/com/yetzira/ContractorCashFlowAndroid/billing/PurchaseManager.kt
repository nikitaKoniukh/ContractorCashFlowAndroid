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
import com.yetzira.ContractorCashFlowAndroid.BuildConfig
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.preferences.SubscriptionPreferencesRepositoryContract
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import kotlin.coroutines.resume

class PurchaseManager(
    context: Context,
    private val preferencesRepository: SubscriptionPreferencesRepositoryContract
) : FreeTierGate {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val devPrefs = appContext.getSharedPreferences("dev_prefs", Context.MODE_PRIVATE)
    private val _devProOverride = MutableStateFlow(
        if (BuildConfig.DEBUG) devPrefs.getBoolean("dev_pro_override", false) else false
    )
    val devProOverride: StateFlow<Boolean> = _devProOverride.asStateFlow()

    fun toggleDevProOverride() {
        if (!BuildConfig.DEBUG) {
            Log.w(TAG, "toggleDevProOverride ignored outside DEBUG builds")
            return
        }
        val newValue = !_devProOverride.value
        _devProOverride.value = newValue
        devPrefs.edit().putBoolean("dev_pro_override", newValue).apply()
        if (newValue) {
            _isProUser.value = true
        } else {
            scope.launch { checkCurrentEntitlements() }
        }
    }

    private val _isProUser = MutableStateFlow(_devProOverride.value)
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _activePurchase = MutableStateFlow<Purchase?>(null)
    val activePurchase: StateFlow<Purchase?> = _activePurchase.asStateFlow()

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()

    private val _hasPendingPurchase = MutableStateFlow(false)
    val hasPendingPurchase: StateFlow<Boolean> = _hasPendingPurchase.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        Log.d(TAG, "purchasesUpdated: code=${billingResult.responseCode} purchases=${purchases?.size ?: 0}")
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases.orEmpty().forEach { purchase ->
                    Log.d(
                        TAG,
                        "  purchase: products=${purchase.products} state=${purchase.purchaseState} ack=${purchase.isAcknowledged}"
                    )
                    scope.launch { handlePurchase(purchase) }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "  user cancelled purchase")
                _statusMessage.value = appContext.getString(R.string.paywall_status_cancelled)
            }
            else -> {
                Log.w(TAG, "  purchase FAILED: code=${billingResult.responseCode}")
                _errorMessage.value = appContext.getString(
                    R.string.billing_purchase_failed,
                    billingResult.debugMessage
                )
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
        .enableAutoServiceReconnection()
        .build()

    @Volatile
    private var isConnected = false
    private val connectionMutex = Mutex()
    private var pendingConnection: CompletableDeferred<Boolean>? = null
    private var reconnectJob: Job? = null

    init {
        if (!BuildConfig.DEBUG && devPrefs.getBoolean("dev_pro_override", false)) {
            devPrefs.edit().putBoolean("dev_pro_override", false).apply()
        }
        scope.launch {
            hydrateFromCache()
            connectAndLoad()
        }
    }

    private suspend fun hydrateFromCache() {
        val cachedPro = preferencesRepository.subscriptionIsPro.first()
        if (_devProOverride.value) {
            _isProUser.value = true
        } else if (cachedPro) {
            Log.d(TAG, "hydrateFromCache: restoring Pro from DataStore until Play confirms")
            _isProUser.value = true
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
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
                Log.w(TAG, "connectAndLoad: billing not connected — keeping cached Pro if any")
            }
        }
    }

    private suspend fun ensureConnected(): Boolean {
        if (billingClient.isReady) {
            isConnected = true
            return true
        }

        return connectionMutex.withLock {
            if (billingClient.isReady) {
                isConnected = true
                return@withLock true
            }

            pendingConnection?.let { pending ->
                if (pending.isActive) {
                    return@withLock pending.await()
                }
            }

            val deferred = CompletableDeferred<Boolean>()
            pendingConnection = deferred

            try {
                billingClient.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        val connected = result.responseCode == BillingClient.BillingResponseCode.OK
                        isConnected = connected
                        if (!connected &&
                            result.responseCode != BillingClient.BillingResponseCode.SERVICE_DISCONNECTED
                        ) {
                            _errorMessage.value = appContext.getString(
                                R.string.billing_unavailable,
                                result.debugMessage
                            )
                        }
                        deferred.complete(connected)
                    }

                    override fun onBillingServiceDisconnected() {
                        isConnected = false
                        if (deferred.isActive) deferred.complete(false)
                        scheduleReconnect()
                    }
                })
                deferred.await()
            } catch (e: Exception) {
                Log.e(TAG, "ensureConnected: startConnection threw", e)
                if (deferred.isActive) deferred.complete(false)
                false
            }
        }
    }

    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            delay(RECONNECT_DELAY_MS)
            if (!billingClient.isReady) {
                ensureConnected()
            }
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

            val productParams = BillingProduct.ALL_IDS.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }

            var result = queryProductDetails(productParams)
            if (result.first.responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                isConnected = false
                if (ensureConnected()) {
                    result = queryProductDetails(productParams)
                }
            }

            if (result.first.responseCode == BillingClient.BillingResponseCode.OK) {
                _products.value = result.second.sortedBy { details ->
                    if (details.productId == BillingProduct.PRO_MONTHLY) 0 else 1
                }
                if (result.second.isEmpty()) {
                    Log.w(TAG, "loadProducts: OK but no product details returned")
                }
            } else {
                _errorMessage.value = appContext.getString(
                    R.string.billing_failed_load_products,
                    result.first.debugMessage
                )
            }
        } finally {
            _isLoading.value = false
        }
    }

    private suspend fun queryProductDetails(
        productParams: List<QueryProductDetailsParams.Product>
    ): Pair<BillingResult, List<ProductDetails>> =
        suspendCancellableCoroutine { continuation ->
            billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder()
                    .setProductList(productParams)
                    .build()
            ) { billingResult, productDetailsResult ->
                if (!continuation.isActive) return@queryProductDetailsAsync

                productDetailsResult.unfetchedProductList.forEach { unfetched ->
                    Log.w(
                        TAG,
                        "Unfetched product id=${unfetched.productId} status=${unfetched.statusCode}"
                    )
                }
                continuation.resume(
                    billingResult to productDetailsResult.productDetailsList
                )
            }
        }

    fun launchPurchaseFlow(
        activity: Activity,
        productDetails: ProductDetails,
        basePlanId: String
    ) {
        Log.d(TAG, "launchPurchaseFlow: productId=${productDetails.productId} basePlanId=$basePlanId")

        val offerToken = resolveOfferToken(productDetails, basePlanId)
        if (offerToken == null) {
            _errorMessage.value = appContext.getString(
                R.string.billing_no_active_offer,
                productDetails.productId
            )
            return
        }

        _isPurchasing.value = true
        _statusMessage.value = null

        scope.launch {
            if (!ensureConnected()) {
                _errorMessage.value = appContext.getString(R.string.billing_not_available)
                _isPurchasing.value = false
                return@launch
            }

            val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()

            val flowBuilder = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productParams))

            val current = _activePurchase.value
            if (current != null &&
                current.products.none { it == productDetails.productId } &&
                current.products.any { it in BillingProduct.ALL_IDS }
            ) {
                Log.d(TAG, "launchPurchaseFlow: subscription replacement from ${current.products}")
                flowBuilder.setSubscriptionUpdateParams(
                    BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                        .setOldPurchaseToken(current.purchaseToken)
                        .setSubscriptionReplacementMode(
                            BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.WITH_TIME_PRORATION
                        )
                        .build()
                )
            }

            val result = billingClient.launchBillingFlow(activity, flowBuilder.build())
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                when (result.responseCode) {
                    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                        isConnected = false
                        scheduleReconnect()
                        _errorMessage.value =
                            appContext.getString(R.string.billing_temporarily_disconnected)
                    }
                    BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                        _errorMessage.value =
                            appContext.getString(R.string.billing_must_install_from_play)
                    }
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                        _errorMessage.value = appContext.getString(R.string.billing_already_owned)
                        checkCurrentEntitlements()
                    }
                    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                        _errorMessage.value =
                            appContext.getString(R.string.billing_unavailable_product)
                    }
                    else -> {
                        _errorMessage.value = appContext.getString(
                            R.string.billing_purchase_failed_code,
                            result.responseCode
                        )
                    }
                }
                _isPurchasing.value = false
            }
        }
    }

    private fun resolveOfferToken(productDetails: ProductDetails, basePlanId: String): String? {
        val offers = productDetails.subscriptionOfferDetails.orEmpty()
        val baseOffer = offers.firstOrNull { it.basePlanId == basePlanId && it.offerId == null }
            ?: offers.firstOrNull { it.basePlanId == basePlanId }
        return baseOffer?.offerToken
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PENDING -> {
                Log.d(TAG, "handlePurchase: PENDING")
                _hasPendingPurchase.value = true
                _statusMessage.value = appContext.getString(R.string.paywall_status_pending)
                return
            }
            Purchase.PurchaseState.PURCHASED -> Unit
            else -> {
                Log.d(TAG, "handlePurchase: skipping state=${purchase.purchaseState}")
                return
            }
        }

        _hasPendingPurchase.value = false
        if (!purchase.isAcknowledged) {
            val ackOk = acknowledgePurchaseWithRetry(purchase.purchaseToken)
            if (!ackOk) return
        }

        refreshEntitlements()
        _statusMessage.value = appContext.getString(R.string.paywall_status_purchased)
    }

    private suspend fun acknowledgePurchaseWithRetry(purchaseToken: String): Boolean {
        var result = doAcknowledge(purchaseToken)
        if (result.responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
            isConnected = false
            if (ensureConnected()) {
                result = doAcknowledge(purchaseToken)
            }
        }
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _errorMessage.value = appContext.getString(R.string.billing_ack_failed, result.debugMessage)
            return false
        }
        return true
    }

    private suspend fun doAcknowledge(purchaseToken: String): BillingResult =
        suspendCancellableCoroutine { continuation ->
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchaseToken)
                    .build()
            ) { ackResult ->
                if (continuation.isActive) continuation.resume(ackResult)
            }
        }

    /**
     * Queries Google Play for active subscriptions and updates local state.
     * On connection failure, keeps any cached Pro entitlement.
     */
    suspend fun checkCurrentEntitlements() {
        Log.d(TAG, "checkCurrentEntitlements: querying active subscriptions")
        if (!ensureConnected()) {
            Log.w(TAG, "checkCurrentEntitlements: not connected — retaining cached Pro")
            return
        }

        var result = queryPurchases()
        if (result.first.responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
            isConnected = false
            if (ensureConnected()) {
                result = queryPurchases()
            }
        }

        if (result.first.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "checkCurrentEntitlements: FAILED — retaining cached Pro")
            _errorMessage.value = appContext.getString(
                R.string.billing_refresh_failed,
                result.first.debugMessage
            )
            return
        }

        applyPurchases(result.second)

        result.second
            .filter { !it.isAcknowledged && it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .forEach { purchase ->
                acknowledgePurchaseWithRetry(purchase.purchaseToken)
            }
    }

    private suspend fun queryPurchases(): Pair<BillingResult, List<Purchase>> =
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

    private suspend fun refreshEntitlements() {
        if (!ensureConnected()) return
        val result = queryPurchases()
        if (result.first.responseCode != BillingClient.BillingResponseCode.OK) return
        applyPurchases(result.second)
    }

    private suspend fun applyPurchases(purchases: List<Purchase>) {
        val pending = purchases.any { it.purchaseState == Purchase.PurchaseState.PENDING }
        _hasPendingPurchase.value = pending

        val activeProPurchase = purchases.firstOrNull { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                purchase.products.any { it in BillingProduct.ALL_IDS }
        }

        _activePurchase.value = activeProPurchase
        _isProUser.value = activeProPurchase != null || _devProOverride.value

        val planName = when {
            activeProPurchase?.products?.contains(BillingProduct.PRO_YEARLY) == true -> "Pro Yearly"
            activeProPurchase?.products?.contains(BillingProduct.PRO_MONTHLY) == true -> "Pro Monthly"
            else -> null
        }
        val renewalDate = activeProPurchase?.let { resolveRenewalDate(it) }

        preferencesRepository.setSubscription(
            isPro = activeProPurchase != null,
            planName = planName,
            renewalDate = renewalDate
        )
    }

    suspend fun restorePurchases() {
        Log.d(TAG, "restorePurchases")
        _isRestoring.value = true
        _statusMessage.value = null
        try {
            if (!ensureConnected()) {
                _errorMessage.value = appContext.getString(R.string.billing_not_available)
                return
            }
            checkCurrentEntitlements()
            if (!_isProUser.value && !_devProOverride.value) {
                _errorMessage.value = appContext.getString(R.string.billing_restore_none_found)
            } else if (_isProUser.value) {
                _statusMessage.value = appContext.getString(R.string.paywall_status_restored)
            }
        } finally {
            _isRestoring.value = false
        }
    }

    override fun canCreateProject(currentCount: Int): Boolean =
        FreeTierPolicy.canCreateProject(_isProUser.value, currentCount)

    override fun canCreateWorker(currentCount: Int): Boolean =
        FreeTierPolicy.canCreateWorker(_isProUser.value, currentCount)

    fun openManageSubscriptions(context: Context) {
        val sku = _activePurchase.value?.products
            ?.firstOrNull { it in BillingProduct.ALL_IDS }
            ?: BillingProduct.PRO_YEARLY
        val uri = Uri.parse(
            "https://play.google.com/store/account/subscriptions" +
                "?sku=$sku&package=${appContext.packageName}"
        )
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    fun destroy() {
        billingClient.endConnection()
        scope.cancel()
    }

    companion object {
        private const val TAG = "BillingDebug"
        private const val RECONNECT_DELAY_MS = 1500L
        private const val DAY_MS = 24L * 60 * 60 * 1000

        fun resolveRenewalDate(purchase: Purchase): Long? {
            try {
                val json = JSONObject(purchase.originalJson)
                when {
                    json.has("expiryTimeMillis") ->
                        json.getString("expiryTimeMillis").toLongOrNull()?.let { return it }
                    json.has("expiryTime") -> {
                        val value = json.optLong("expiryTime", 0L)
                        if (value > 0L) return value
                    }
                }
            } catch (_: Exception) {
                // Fall through to estimate
            }
            val periodMs = when {
                purchase.products.any { it == BillingProduct.PRO_YEARLY } -> 365L * DAY_MS
                purchase.products.any { it == BillingProduct.PRO_MONTHLY } -> 30L * DAY_MS
                else -> return null
            }
            return purchase.purchaseTime + periodMs
        }
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
