package com.yetzira.ContractorCashFlowAndroid.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
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

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases.orEmpty().forEach { purchase ->
                    scope.launch { handlePurchase(purchase) }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            else -> _errorMessage.value = "Purchase failed: ${billingResult.debugMessage}"
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
        if (billingClient.isReady) {
            scope.launch {
                checkCurrentEntitlements()
                loadProducts()
            }
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: com.android.billingclient.api.BillingResult) {
                isConnected = billingResult.responseCode == BillingClient.BillingResponseCode.OK
                if (isConnected) {
                    scope.launch {
                        checkCurrentEntitlements()
                        loadProducts()
                    }
                } else {
                    _errorMessage.value = "Billing unavailable: ${billingResult.debugMessage}"
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnected = false
            }
        })
    }

    suspend fun loadProducts() {
        _isLoading.value = true
        try {
            reconnectIfNeeded()
            val products = BillingProduct.ALL_IDS.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }

            val result = suspendCancellableCoroutine<Pair<com.android.billingclient.api.BillingResult, List<ProductDetails>>> { continuation ->
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

            if (result.first.responseCode == BillingClient.BillingResponseCode.OK) {
                _products.value = result.second.sortedBy { details ->
                    if (details.productId == BillingProduct.PRO_MONTHLY) 0 else 1
                }
            } else {
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
        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull { it.basePlanId == basePlanId }
            ?.offerToken

        if (offerToken == null) {
            _errorMessage.value = "No active offer found for ${productDetails.productId}"
            return
        }

        _isPurchasing.value = true

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

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            return
        }

        if (!purchase.isAcknowledged) {
            val result = suspendCancellableCoroutine<com.android.billingclient.api.BillingResult> { continuation ->
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
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                _errorMessage.value = "Acknowledgment failed: ${result.debugMessage}"
                return
            }
        }

        checkCurrentEntitlements()
    }

    suspend fun checkCurrentEntitlements() {
        reconnectIfNeeded()

        val result = suspendCancellableCoroutine<Pair<com.android.billingclient.api.BillingResult, List<Purchase>>> { continuation ->
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

        if (result.first.responseCode != BillingClient.BillingResponseCode.OK) {
            _errorMessage.value = "Failed to refresh purchases: ${result.first.debugMessage}"
            return
        }

        val activeProPurchase = result.second.firstOrNull { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                purchase.products.any { it in BillingProduct.ALL_IDS }
        }

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
                handlePurchase(purchase)
            }
    }

    suspend fun restorePurchases() {
        checkCurrentEntitlements()
    }

    fun canCreateProject(currentCount: Int): Boolean =
        _isProUser.value || currentCount < FreeTierLimit.MAX_PROJECTS

    fun canCreateExpense(currentCount: Int): Boolean =
        _isProUser.value || currentCount < FreeTierLimit.MAX_EXPENSES

    fun canCreateInvoice(currentCount: Int): Boolean =
        _isProUser.value || currentCount < FreeTierLimit.MAX_INVOICES

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
        billingClient.endConnection()
        scope.cancel()
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


