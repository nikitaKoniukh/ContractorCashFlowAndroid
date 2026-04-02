package com.yetzira.ContractorCashFlowAndroid.ui.paywall

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.billingclient.api.ProductDetails
import com.yetzira.ContractorCashFlowAndroid.billing.BillingProduct
import com.yetzira.ContractorCashFlowAndroid.billing.PurchaseViewModel

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
        listOf(
            PaywallPlanOption(
                id = BillingProduct.PRO_MONTHLY,
                title = "KablanPro Monthly",
                periodLabel = "/ month",
                hardcodedPrice = "₪69.90",
                basePlanId = BillingProduct.MONTHLY_BASE_PLAN,
                savingsBadge = null,
                productDetails = monthlyProduct
            ),
            PaywallPlanOption(
                id = BillingProduct.PRO_YEARLY,
                title = "KablanPro Yearly",
                periodLabel = "/ year",
                hardcodedPrice = "₪349.90",
                basePlanId = BillingProduct.YEARLY_BASE_PLAN,
                savingsBadge = "SAVE 17%",
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
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Sheet drag handle ──────────────────────────────────────────
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )

            // ── Close button ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
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

            // ── Hero icon ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        color = com.yetzira.ContractorCashFlowAndroid.ui.theme.ProGold.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = com.yetzira.ContractorCashFlowAndroid.ui.theme.ProGold,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Upgrade to Pro",
                style = com.yetzira.ContractorCashFlowAndroid.ui.theme.BalanceTextStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = limitReachedMessage
                    ?: "Run more projects and manage your full crew — no limits holding you back.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Feature comparison table ───────────────────────────────────
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // Column headers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Free",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(52.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Pro",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(52.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    PaywallFeatureRow(
                        icon = Icons.Default.Folder,
                        title = "Projects",
                        freeValue = "1",
                        proValue = "∞"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    PaywallFeatureRow(
                        icon = Icons.Default.Group,
                        title = "Workers",
                        freeValue = "2",
                        proValue = "∞"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    PaywallFeatureRow(
                        icon = Icons.Default.AttachMoney,
                        title = "Expenses",
                        subtitle = "always free",
                        freeValue = "check",
                        proValue = "check"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    PaywallFeatureRow(
                        icon = Icons.Default.Description,
                        title = "Invoices",
                        subtitle = "always free",
                        freeValue = "check",
                        proValue = "check"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Plan cards ─────────────────────────────────────────────────
            if (isLoading && products.isEmpty()) {
                CircularProgressIndicator()
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

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
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Terms of Use",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "·",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
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
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon in soft circle
        Box(
            modifier = Modifier
                .size(36.dp)
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
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title + optional subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
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
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF8E8E93),
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = freeValue,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Pro value cell
        Box(modifier = Modifier.width(52.dp), contentAlignment = Alignment.Center) {
            if (proValue == "check") {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF59B865),
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = proValue,
                    style = MaterialTheme.typography.titleLarge,
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
    val pricingPhase = plan.productDetails
        ?.subscriptionOfferDetails
        ?.firstOrNull { it.basePlanId == plan.basePlanId }
        ?.pricingPhases
        ?.pricingPhaseList
        ?.firstOrNull()
    val displayPrice = pricingPhase?.formattedPrice ?: plan.hardcodedPrice

    OutlinedCard(
        onClick = onClick,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    else Color.Transparent
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleSmall,
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
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "$displayPrice ${plan.periodLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

// ── Data class ─────────────────────────────────────────────────────────────────

private data class PaywallPlanOption(
    val id: String,
    val title: String,
    val periodLabel: String,
    val hardcodedPrice: String,
    val basePlanId: String,
    val savingsBadge: String?,
    val productDetails: ProductDetails?
)
