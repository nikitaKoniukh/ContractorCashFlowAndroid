package com.yetzira.ContractorCashFlowAndroid.ui.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.credentials.CustomCredential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.billing.BillingProduct
import com.yetzira.ContractorCashFlowAndroid.billing.PurchaseViewModel
import com.yetzira.ContractorCashFlowAndroid.billing.PurchaseViewModelFactory
import com.yetzira.ContractorCashFlowAndroid.data.preferences.AppLanguageOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.ThemeModeOption
import com.yetzira.ContractorCashFlowAndroid.locale.LocaleHelper
import com.yetzira.ContractorCashFlowAndroid.ui.components.AnalyticsCard
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernDropdown
import com.yetzira.ContractorCashFlowAndroid.ui.paywall.PaywallSheet
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Identifies which notification toggle the user is trying to enable,
 * so we can show the correct explanation dialog.
 */
private enum class NotificationToggleType {
    INVOICE_REMINDERS,
    OVERDUE_ALERTS,
    BUDGET_WARNINGS
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val packageInfo = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                android.content.pm.PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
    }
    val appVersionLabel = remember(packageInfo) {
        "KablanPro ${packageInfo.versionName}.${androidx.core.content.pm.PackageInfoCompat.getLongVersionCode(packageInfo)}"
    }
    val coroutineScope = rememberCoroutineScope()
    val purchaseViewModel: PurchaseViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = remember { PurchaseViewModelFactory(context) }
    )
    val isProUser by purchaseViewModel.isProUser.collectAsState()
    val activePurchase by purchaseViewModel.activePurchase.collectAsState()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var showPaywall by remember { mutableStateOf(false) }
    val googleSignInSetupIncompleteMessage = stringResource(R.string.settings_google_sign_in_setup_incomplete)
    val googleSignInSetupMissingClientIdMessage = stringResource(R.string.settings_google_sign_in_setup_missing_client_id)
    val googleSignInUnsupportedCredentialMessage = stringResource(R.string.settings_google_sign_in_unsupported_credential)
    val googleSignInParseTokenFailedMessage = stringResource(R.string.settings_google_sign_in_parse_token_failed)
    val googleSignInFailedMessage = stringResource(R.string.settings_google_sign_in_failed)

    val webClientId = remember {
        @Suppress("DiscouragedApi")
        val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (id != 0) context.getString(id) else null
    }
    val googleSignInSetupError = remember(webClientId) {
        if (webClientId.isNullOrBlank()) {
            googleSignInSetupIncompleteMessage
        } else {
            null
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let(viewModel::exportData)
    }
    val themeLabels = mapOf(
        ThemeModeOption.SYSTEM to stringResource(R.string.settings_theme_system),
        ThemeModeOption.LIGHT to stringResource(R.string.settings_theme_light),
        ThemeModeOption.DARK to stringResource(R.string.settings_theme_dark)
    )

    // --- Notification explanation dialog state ---
    var pendingNotificationToggle by remember { mutableStateOf<NotificationToggleType?>(null) }

    // Permission launcher for POST_NOTIFICATIONS (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted — enable the toggle the user originally flipped
            when (pendingNotificationToggle) {
                NotificationToggleType.INVOICE_REMINDERS -> viewModel.setInvoiceRemindersEnabled(true)
                NotificationToggleType.OVERDUE_ALERTS -> viewModel.setOverdueAlertsEnabled(true)
                NotificationToggleType.BUDGET_WARNINGS -> viewModel.setBudgetWarningsEnabled(true)
                null -> { /* shouldn't happen */ }
            }
        }
        pendingNotificationToggle = null
    }

    /**
     * Called when the user taps "Allow" in the explanation dialog.
     * Checks system notification permission and either enables the toggle,
     * requests permission, or redirects to Android Settings.
     */
    fun onDialogAllow() {
        val toggleType = pendingNotificationToggle ?: return

        // On Android < 13, notifications are allowed by default — just enable the toggle
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            when (toggleType) {
                NotificationToggleType.INVOICE_REMINDERS -> viewModel.setInvoiceRemindersEnabled(true)
                NotificationToggleType.OVERDUE_ALERTS -> viewModel.setOverdueAlertsEnabled(true)
                NotificationToggleType.BUDGET_WARNINGS -> viewModel.setBudgetWarningsEnabled(true)
            }
            pendingNotificationToggle = null
            return
        }

        // Android 13+: check current permission state
        val permissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            // Already have permission — enable the toggle
            when (toggleType) {
                NotificationToggleType.INVOICE_REMINDERS -> viewModel.setInvoiceRemindersEnabled(true)
                NotificationToggleType.OVERDUE_ALERTS -> viewModel.setOverdueAlertsEnabled(true)
                NotificationToggleType.BUDGET_WARNINGS -> viewModel.setBudgetWarningsEnabled(true)
            }
            pendingNotificationToggle = null
            return
        }

        // Check if we can show the rationale (user hasn't permanently denied)
        val activity = context as? Activity
        val shouldShowRationale = activity?.shouldShowRequestPermissionRationale(
            Manifest.permission.POST_NOTIFICATIONS
        ) ?: false

        // If the system has NOT shown the permission dialog before, or rationale is available — request permission
        val areNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!areNotificationsEnabled && !shouldShowRationale && activity != null) {
            // User previously denied and selected "Don't ask again" → redirect to Settings
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
            pendingNotificationToggle = null
        } else {
            // Request the permission — result will be handled by notificationPermissionLauncher
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun onDialogCancel() {
        pendingNotificationToggle = null
    }

    // --- Show explanation dialog if a toggle is pending ---
    pendingNotificationToggle?.let { toggleType ->
        val (dialogTitle, dialogMessage) = when (toggleType) {
            NotificationToggleType.INVOICE_REMINDERS -> Pair(
                stringResource(R.string.notif_dialog_invoice_reminders_title),
                stringResource(R.string.notif_dialog_invoice_reminders_message)
            )
            NotificationToggleType.OVERDUE_ALERTS -> Pair(
                stringResource(R.string.notif_dialog_overdue_alerts_title),
                stringResource(R.string.notif_dialog_overdue_alerts_message)
            )
            NotificationToggleType.BUDGET_WARNINGS -> Pair(
                stringResource(R.string.notif_dialog_budget_warnings_title),
                stringResource(R.string.notif_dialog_budget_warnings_message)
            )
        }

        AlertDialog(
            onDismissRequest = { onDialogCancel() },
            title = { Text(dialogTitle) },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(onClick = { onDialogAllow() }) {
                    Text(stringResource(R.string.notif_dialog_allow))
                }
            },
            dismissButton = {
                TextButton(onClick = { onDialogCancel() }) {
                    Text(stringResource(R.string.notif_dialog_cancel))
                }
            }
        )
    }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsCard {
                SectionTitle(stringResource(R.string.settings_section_account))
                if (state.isAuthenticated) {
                    Text(
                        text = state.userEmail ?: stringResource(R.string.settings_signed_in),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    OutlinedButton(
                        onClick = viewModel::signOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Text(stringResource(R.string.settings_sign_out))
                    }
                } else {
                    Text(
                        text = stringResource(R.string.settings_sign_in_prompt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    googleSignInSetupError?.let { setupError ->
                        Text(
                            text = setupError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Button(
                        onClick = {
                            val serverClientId = webClientId
                            if (serverClientId.isNullOrBlank()) {
                                viewModel.onGoogleSignInFailed(
                                    googleSignInSetupMissingClientIdMessage
                                )
                                return@Button
                            }
                            coroutineScope.launch {
                                runCatching {
                                    val googleIdOption = GetGoogleIdOption.Builder()
                                        .setServerClientId(serverClientId)
                                        .setFilterByAuthorizedAccounts(false)
                                        .setAutoSelectEnabled(false)
                                        .build()
                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()
                                    credentialManager.getCredential(
                                        context = context,
                                        request = request
                                    )
                                }.onSuccess { response ->
                                    val credential = response.credential
                                    if (credential is CustomCredential) {
                                        val isGoogleCredentialType = credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ||
                                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL
                                        if (isGoogleCredentialType) {
                                            val token = runCatching {
                                                GoogleIdTokenCredential.createFrom(credential.data).idToken
                                            }.getOrElse { parseError ->
                                                throw parseError
                                            }
                                            Log.d(
                                                SETTINGS_AUTH_LOG_TAG,
                                                "CredentialManager sign-in success tokenLength=${token.length}"
                                            )
                                            viewModel.signInWithGoogleIdToken(token)
                                        } else {
                                            viewModel.onGoogleSignInFailed(googleSignInUnsupportedCredentialMessage)
                                        }
                                    } else {
                                        viewModel.onGoogleSignInFailed(googleSignInUnsupportedCredentialMessage)
                                    }
                                }.onFailure { throwable ->
                                    val message = googleSignInErrorMessage(
                                        throwable = throwable,
                                        parseTokenFailedMessage = googleSignInParseTokenFailedMessage,
                                        fallbackFailedMessage = googleSignInFailedMessage
                                    )
                                    Log.w(SETTINGS_AUTH_LOG_TAG, "CredentialManager sign-in failed: $message", throwable)
                                    viewModel.onGoogleSignInFailed(message)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        enabled = googleSignInSetupError == null
                    ) {
                        Text(stringResource(R.string.settings_sign_in_with_google))
                    }
                }
            }

            SettingsDropdownCard(
                title = stringResource(R.string.settings_section_language),
                selectedLabel = state.selectedLanguage.displayName,
                options = AppLanguageOption.entries.toList(),
                optionLabel = { it.displayName },
                onOptionSelected = { language ->
                    LocaleHelper.saveLanguage(context, language.code)
                    viewModel.setLanguage(language)
                    (context as? Activity)?.recreate()
                }
            )

            SettingsDropdownCard(
                title = stringResource(R.string.settings_section_currency),
                selectedLabel = "${state.selectedCurrency.code} (${state.selectedCurrency.symbol})",
                options = CurrencyOption.entries.toList(),
                optionLabel = { "${it.code} (${it.symbol})" },
                onOptionSelected = viewModel::setCurrency
            )

            SettingsDropdownCard(
                title = stringResource(R.string.settings_section_theme),
                selectedLabel = themeLabels[state.selectedThemeMode]
                    ?: stringResource(R.string.settings_theme_system),
                options = ThemeModeOption.entries.toList(),
                optionLabel = { themeLabels[it] ?: it.name },
                onOptionSelected = viewModel::setThemeMode
            )

            // ── Notifications section with explanation-dialog-gated toggles ──
            AnalyticsCard {
                SectionTitle(stringResource(R.string.settings_section_notifications))
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_invoice_reminders),
                    checked = state.invoiceRemindersEnabled,
                    onCheckedChange = { wantsEnabled ->
                        if (wantsEnabled) {
                            pendingNotificationToggle = NotificationToggleType.INVOICE_REMINDERS
                        } else {
                            viewModel.setInvoiceRemindersEnabled(false)
                        }
                    }
                )
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_overdue_alerts),
                    checked = state.overdueAlertsEnabled,
                    onCheckedChange = { wantsEnabled ->
                        if (wantsEnabled) {
                            pendingNotificationToggle = NotificationToggleType.OVERDUE_ALERTS
                        } else {
                            viewModel.setOverdueAlertsEnabled(false)
                        }
                    }
                )
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_budget_warnings),
                    checked = state.budgetWarningsEnabled,
                    onCheckedChange = { wantsEnabled ->
                        if (wantsEnabled) {
                            pendingNotificationToggle = NotificationToggleType.BUDGET_WARNINGS
                        } else {
                            viewModel.setBudgetWarningsEnabled(false)
                        }
                    }
                )
                Text(
                    text = stringResource(R.string.settings_notifications_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            AnalyticsCard {
                SectionTitle(stringResource(R.string.settings_section_subscription))
                if (isProUser) {
                    val planLabel = when {
                        activePurchase?.products?.contains(BillingProduct.PRO_YEARLY) == true -> "Pro Yearly"
                        activePurchase?.products?.contains(BillingProduct.PRO_MONTHLY) == true -> "Pro Monthly"
                        else -> state.subscription.planName
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.settings_subscription_pro_badge), style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = planLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            state.subscription.renewalDate?.let { renewalDate ->
                                Text(
                                    text = stringResource(R.string.settings_subscription_renews, formatDate(renewalDate)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { purchaseViewModel.openManageSubscriptions(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.settings_manage_subscription))
                    }
                } else {
                    Text(
                        text = stringResource(R.string.settings_subscription_free),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.settings_subscription_upgrade_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showPaywall = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.settings_upgrade_pro))
                    }
                }
            }

            AnalyticsCard {
                SectionTitle(stringResource(R.string.settings_section_cloud_sync))
                SyncActionButton(
                    state = state.cloudSyncState,
                    onClick = viewModel::runCloudSync,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnalyticsCard {
                SectionTitle(stringResource(R.string.settings_section_export))
                Text(
                    text = stringResource(R.string.settings_export_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { exportLauncher.launch(viewModel.suggestedExportFileName()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_export_button))
                }
            }

            AnalyticsCard {
                SectionTitle(stringResource(R.string.settings_section_about))
                Text(
                    text = appVersionLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    if (showPaywall) {
        PaywallSheet(
            viewModel = purchaseViewModel,
            onDismiss = { showPaywall = false }
        )
    }
}

@Composable
private fun <T> SettingsDropdownCard(
    title: String,
    selectedLabel: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit
) {
    AnalyticsCard {
        SectionTitle(title)
        ModernDropdown(
            label = title,
            options = options.map { optionLabel(it) },
            selected = selectedLabel,
            onSelected = { selectedOptionLabel ->
                options.find { optionLabel(it) == selectedOptionLabel }?.let { onOptionSelected(it) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SyncActionButton(
    state: CloudSyncState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = when (state) {
        CloudSyncState.IDLE -> stringResource(R.string.settings_sync_button_idle)
        CloudSyncState.SYNCING -> stringResource(R.string.settings_sync_button_syncing)
        CloudSyncState.DONE -> stringResource(R.string.settings_sync_button_done)
        CloudSyncState.FAILED -> stringResource(R.string.settings_sync_button_failed)
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = state != CloudSyncState.SYNCING
    ) {
        if (state == CloudSyncState.SYNCING) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(18.dp)
                    .height(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

private fun formatDate(timestamp: Long?): String {
    if (timestamp == null) return "—"
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
}

private fun googleSignInErrorMessage(
    throwable: Throwable,
    parseTokenFailedMessage: String,
    fallbackFailedMessage: String
): String {
    return when (throwable) {
        is GoogleIdTokenParsingException -> parseTokenFailedMessage
        is NoCredentialException -> throwable.message ?: fallbackFailedMessage
        is GetCredentialException -> throwable.errorMessage?.toString()
            ?: throwable.message
            ?: fallbackFailedMessage
        else -> throwable.message ?: fallbackFailedMessage
    }
}

private const val SETTINGS_AUTH_LOG_TAG = "KablanProAuth"
