package com.yetzira.ContractorCashFlowAndroid.ui.paywall

import android.app.Activity
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.billingclient.api.ProductDetails
import com.yetzira.ContractorCashFlowAndroid.billing.BillingProduct
import com.yetzira.ContractorCashFlowAndroid.billing.PurchaseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallSheet(
    viewModel: PurchaseViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    limitReachedMessage: String? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null,
        modifier = modifier,
        sheetState = sheetState
    ) {
        PaywallScreen(
            viewModel = viewModel,
            onDismiss = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            limitReachedMessage = limitReachedMessage
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    viewModel: PurchaseViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    limitReachedMessage: String? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isProUser by viewModel.isProUser.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isPurchasing by viewModel.isPurchasing.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val yearlyProduct = products.firstOrNull { it.productId == BillingProduct.PRO_YEARLY }
    val monthlyProduct = products.firstOrNull { it.productId == BillingProduct.PRO_MONTHLY }

    val plans = remember(monthlyProduct, yearlyProduct) {
        val dynamicSavings = computeSavingsBadge(monthlyProduct, yearlyProduct)
        listOf(
            PaywallPlanOption(
                id = BillingProduct.PRO_MONTHLY,
                title = "KablanPro Monthly",
                periodLabel = "/ month",
                basePlanId = BillingProduct.MONTHLY_BASE_PLAN,
                savingsBadge = null,
                productDetails = monthlyProduct
            ),
            PaywallPlanOption(
                id = BillingProduct.PRO_YEARLY,
                title = "KablanPro Yearly",
                periodLabel = "/ year",
                basePlanId = BillingProduct.YEARLY_BASE_PLAN,
                savingsBadge = dynamicSavings ?: "SAVE 58%",
                productDetails = yearlyProduct
            )
        )
    }
    var selectedPlanId by remember { mutableStateOf(BillingProduct.PRO_YEARLY) }

    val selectedPlan = plans.firstOrNull { it.id == selectedPlanId }
    val selectedProduct = selectedPlan?.productDetails

    LaunchedEffect(plans) {
        if (plans.none { it.id == selectedPlanId }) {
            selectedPlanId = BillingProduct.PRO_YEARLY
        }
    }

    var showErrorDialog by remember { mutableStateOf(false) }
    var legalUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isProUser) { if (isProUser) onDismiss() }
    LaunchedEffect(errorMessage) { showErrorDialog = errorMessage != null }

    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false; viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(errorMessage.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false; viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Close button ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Hero icon (crown) ──────────────────────────────────────────
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = com.yetzira.ContractorCashFlowAndroid.ui.theme.ProGold,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Upgrade to Pro",
                style = com.yetzira.ContractorCashFlowAndroid.ui.theme.BalanceTextStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp),
                fontSize = 34.sp,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = limitReachedMessage
                    ?: "Run more projects and manage your full crew — no limits holding you back.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Feature comparison table ───────────────────────────────────
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    // Column headers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Features",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Free",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(52.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Pro",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(52.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    PaywallFeatureRow(
                        icon = Icons.Default.Folder,
                        title = "Unlimited Projects",
                        freeValue = "1",
                        proValue = "Unlimited"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    PaywallFeatureRow(
                        icon = Icons.Default.Group,
                        title = "Unlimited Workers",
                        freeValue = "2",
                        proValue = "Unlimited"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    PaywallFeatureRow(
                        icon = Icons.Default.AttachMoney,
                        title = "Expenses",
                        subtitle = "(always free)",
                        freeValue = "check",
                        proValue = "check"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    PaywallFeatureRow(
                        icon = Icons.Default.Description,
                        title = "Invoices",
                        subtitle = "(always free)",
                        freeValue = "check",
                        proValue = "check"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Plan cards ─────────────────────────────────────────────────
            if (isLoading && products.isEmpty()) {
                CircularProgressIndicator()
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    plans.forEach { plan ->
                        PaywallProductCard(
                            plan = plan,
                            isSelected = selectedPlanId == plan.id,
                            onClick = { selectedPlanId = plan.id }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Subscribe button ───────────────────────────────────────────
            Button(
                onClick = {
                    val product = selectedProduct
                    val basePlanId = selectedPlan?.basePlanId
                    if (product != null && basePlanId != null) {
                        activity?.let { viewModel.launchPurchaseFlow(it, product, basePlanId) }
                    }
                },
                enabled = !isPurchasing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isPurchasing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Subscribe Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { viewModel.restorePurchases() }) {
                Text(
                    text = "Restore Purchases",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { legalUrl = PAYWALL_TERMS_OF_USE_URL }) {
                    Text(
                        text = "Terms of Use",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
                Text(
                    text = "·",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                TextButton(onClick = { legalUrl = PAYWALL_PRIVACY_POLICY_URL }) {
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    legalUrl?.let { url ->
        LegalWebDialog(
            url = url,
            onDismiss = { legalUrl = null }
        )
    }
}

@Composable
private fun LegalWebDialog(
    url: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                    Text(
                        text = if (url.contains("privacy")) "Privacy Policy" else "Terms of Use",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    factory = { context ->
                        WebView(context).apply {
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView,
                                    request: WebResourceRequest
                                ): Boolean = false
                            }
                            loadUrl(url)
                        }
                    },
                    update = { webView ->
                        if (webView.url != url) webView.loadUrl(url)
                    }
                )
            }
        }
    }
}

// ── Feature row ────────────────────────────────────────────────────────────────

@Composable
private fun PaywallFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    freeValue: String,
    proValue: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon in soft circle
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Title + optional subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Free value cell
        Box(modifier = Modifier.width(52.dp), contentAlignment = Alignment.Center) {
            if (freeValue == "check") {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF8E8E93),
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = freeValue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Pro value cell
        Box(modifier = Modifier.width(52.dp), contentAlignment = Alignment.Center) {
            if (proValue == "check") {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF59B865),
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = proValue,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF59B865),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ── Plan card ──────────────────────────────────────────────────────────────────

@Composable
private fun PaywallProductCard(
    plan: PaywallPlanOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Get the recurring (non-trial) price — last phase in the list
    val displayPrice = recurringPricingPhase(plan.productDetails, plan.basePlanId)
        ?.formattedPrice ?: "—"

    // Detect a free-trial intro offer (priceAmountMicros == 0)
    val trialLabel = plan.productDetails
        ?.subscriptionOfferDetails
        ?.firstOrNull { it.basePlanId == plan.basePlanId && it.offerId != null }
        ?.pricingPhases
        ?.pricingPhaseList
        ?.firstOrNull { it.priceAmountMicros == 0L }
        ?.billingPeriod
        ?.let { parsePeriodLabel(it) }
        ?.let { "$it free trial" }

    OutlinedCard(
        onClick = onClick,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
                    else Color.Transparent
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (plan.savingsBadge != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF34C759)
                        ) {
                            Text(
                                text = plan.savingsBadge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "$displayPrice ${plan.periodLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (trialLabel != null) {
                    Text(
                        text = trialLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF34C759)
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(3.dp),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f))
                ) {}
            }
        }
    }
}

// ── Billing helpers ────────────────────────────────────────────────────────────

/**
 * Returns the recurring (non-trial) pricing phase for [basePlanId].
 * Prefers the base-plan-only offer (offerId == null); falls back to any offer.
 * Uses the LAST phase in the list because Google returns phases in order:
 * [free-trial?, intro?, recurring], so last == the ongoing charge.
 */
private fun recurringPricingPhase(
    productDetails: ProductDetails?,
    basePlanId: String
): ProductDetails.PricingPhase? {
    val offers = productDetails?.subscriptionOfferDetails ?: return null
    val offer = offers.firstOrNull { it.basePlanId == basePlanId && it.offerId == null }
        ?: offers.firstOrNull { it.basePlanId == basePlanId }
    return offer?.pricingPhases?.pricingPhaseList?.lastOrNull()
}

/**
 * Calculates "SAVE X%" from real Play Store micros prices.
 * Returns null when either product isn't loaded yet (UI will use the hardcoded fallback).
 */
private fun computeSavingsBadge(
    monthlyProduct: ProductDetails?,
    yearlyProduct: ProductDetails?
): String? {
    val monthlyMicros = recurringPricingPhase(monthlyProduct, BillingProduct.MONTHLY_BASE_PLAN)
        ?.priceAmountMicros ?: return null
    val yearlyMicros = recurringPricingPhase(yearlyProduct, BillingProduct.YEARLY_BASE_PLAN)
        ?.priceAmountMicros ?: return null
    val monthlyAnnual = monthlyMicros * 12
    if (monthlyAnnual <= 0) return null
    val savingsPct = ((monthlyAnnual - yearlyMicros) * 100L / monthlyAnnual).toInt()
    return if (savingsPct > 0) "SAVE $savingsPct%" else null
}

/**
 * Parses an ISO 8601 duration like "P7D", "P1W", "P1M", "P1Y"
 * into a human-readable label such as "7-day", "1-week", "1-month".
 */
private fun parsePeriodLabel(isoPeriod: String): String? {
    if (isoPeriod.isBlank()) return null
    Regex("P(\\d+)D").find(isoPeriod)?.let { return "${it.groupValues[1]}-day" }
    Regex("P(\\d+)W").find(isoPeriod)?.let { return "${it.groupValues[1]}-week" }
    Regex("P(\\d+)M").find(isoPeriod)?.let { return "${it.groupValues[1]}-month" }
    Regex("P(\\d+)Y").find(isoPeriod)?.let { return "${it.groupValues[1]}-year" }
    return null
}

// ── Data class ─────────────────────────────────────────────────────────────────

private data class PaywallPlanOption(
    val id: String,
    val title: String,
    val periodLabel: String,
    val basePlanId: String,
    val savingsBadge: String?,
    val productDetails: ProductDetails?
)

private const val PAYWALL_TERMS_OF_USE_URL =
    "https://nikitakoniukh.github.io/KablanProAndroid/terms-of-use.html"
private const val PAYWALL_PRIVACY_POLICY_URL =
    "https://nikitakoniukh.github.io/KablanProAndroid/privacy.html"

