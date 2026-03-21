package com.yetzira.ContractorCashFlowAndroid.ui.paywall

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yetzira.ContractorCashFlowAndroid.R

@Composable
fun PaywallScreen(
    viewModel: PaywallViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findActivity()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.paywall_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.paywall_description),
            style = MaterialTheme.typography.bodyLarge
        )

        uiState.productTitle?.takeIf { it.isNotBlank() }?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        uiState.priceText?.let { price ->
            Text(
                text = stringResource(R.string.paywall_price_label, price),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        uiState.productDescription?.takeIf { it.isNotBlank() }?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val statusText = when (uiState.status) {
            PaywallStatus.IDLE -> null
            PaywallStatus.LOADING -> stringResource(R.string.paywall_status_loading)
            PaywallStatus.PURCHASED -> stringResource(R.string.paywall_status_purchased)
            PaywallStatus.RESTORED -> stringResource(R.string.paywall_status_restored)
            PaywallStatus.PENDING -> uiState.statusDetail ?: stringResource(R.string.paywall_status_pending)
            PaywallStatus.USER_CANCELLED -> stringResource(R.string.paywall_status_cancelled)
            PaywallStatus.ERROR -> uiState.statusDetail ?: stringResource(R.string.paywall_status_error)
        }

        statusText?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (uiState.status == PaywallStatus.ERROR) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        Button(
            onClick = {
                activity?.let(viewModel::purchase)
                if (activity == null) {
                    viewModel.clearStatus()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canPurchase && activity != null
        ) {
            Text(
                text = when {
                    uiState.isPro -> stringResource(R.string.paywall_already_subscribed)
                    uiState.isProcessingAction -> stringResource(R.string.paywall_button_processing)
                    uiState.priceText != null -> stringResource(R.string.paywall_button_subscribe_with_price, uiState.priceText!!)
                    else -> stringResource(R.string.paywall_button_subscribe)
                }
            )
        }

        OutlinedButton(
            onClick = viewModel::restorePurchases,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isProcessingAction
        ) {
            Text(stringResource(R.string.paywall_button_restore))
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.paywall_button_not_now))
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

