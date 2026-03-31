
package com.yetzira.ContractorCashFlowAndroid.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.credentials.CustomCredential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.yetzira.ContractorCashFlowAndroid.data.preferences.AppLanguageOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.ThemeModeOption
import com.yetzira.ContractorCashFlowAndroid.locale.LocaleHelper
import com.yetzira.ContractorCashFlowAndroid.ui.components.AnalyticsCard
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernDropdown
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenPaywall: () -> Unit,
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
        "KablanPro ${packageInfo.versionName}.${PackageInfoCompat.getLongVersionCode(packageInfo)}"
    }
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val googleSignInSetupIncompleteMessage = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_google_sign_in_setup_incomplete)
    val googleSignInSetupMissingClientIdMessage = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_google_sign_in_setup_missing_client_id)
    val googleSignInUnsupportedCredentialMessage = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_google_sign_in_unsupported_credential)
    val googleSignInParseTokenFailedMessage = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_google_sign_in_parse_token_failed)
    val googleSignInFailedMessage = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_google_sign_in_failed)

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
        ThemeModeOption.SYSTEM to stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_theme_system),
        ThemeModeOption.LIGHT to stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_theme_light),
        ThemeModeOption.DARK to stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_theme_dark)
    )

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
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
                SectionTitle(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_section_account))
                if (state.isAuthenticated) {
                    Text(
                        text = state.userEmail ?: stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_signed_in),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    OutlinedButton(
                        onClick = viewModel::signOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Text(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_sign_out))
                    }
                } else {
                    Text(
                        text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_sign_in_prompt),
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
                        Text(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_sign_in_with_google))
                    }
                }
            }

            SettingsDropdownCard(
                title = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_section_language),
                selectedLabel = state.selectedLanguage.displayName,
                options = AppLanguageOption.entries.toList(),
                optionLabel = { it.displayName },
                onOptionSelected = { language ->
                    // 1. Persist to SharedPreferences synchronously (commit())
                    //    so attachBaseContext reads the new code on recreate.
                    LocaleHelper.saveLanguage(context, language.code)
                    // 2. Persist to DataStore + notify ViewModel (async is fine here).
                    viewModel.setLanguage(language)
                    // 3. Recreate Activity — attachBaseContext will now apply new locale.
                    (context as? Activity)?.recreate()
                }
            )

            SettingsDropdownCard(
                title = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_section_currency),
                selectedLabel = "${state.selectedCurrency.code} (${state.selectedCurrency.symbol})",
                options = CurrencyOption.entries.toList(),
                optionLabel = { "${it.code} (${it.symbol})" },
                onOptionSelected = viewModel::setCurrency
            )

            SettingsDropdownCard(
                title = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_section_theme),
                selectedLabel = themeLabels[state.selectedThemeMode]
                    ?: stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_theme_system),
                options = ThemeModeOption.entries.toList(),
                optionLabel = { themeLabels[it] ?: it.name },
                onOptionSelected = viewModel::setThemeMode
            )

            AnalyticsCard {
                SectionTitle(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_section_notifications))
                SettingsSwitchRow(
                    title = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_invoice_reminders),
                    checked = state.invoiceRemindersEnabled,
                    onCheckedChange = viewModel::setInvoiceRemindersEnabled
                )
                SettingsSwitchRow(
                    title = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_overdue_alerts),
                    checked = state.overdueAlertsEnabled,
                    onCheckedChange = viewModel::setOverdueAlertsEnabled
                )
                SettingsSwitchRow(
                    title = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_budget_warnings),
                    checked = state.budgetWarningsEnabled,
                    onCheckedChange = viewModel::setBudgetWarningsEnabled
                )
            }

            AnalyticsCard {
                SectionTitle(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_section_subscription))
                if (state.subscription.isPro) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_subscription_pro_badge), style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.subscription.planName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            state.subscription.renewalDate?.let { renewalDate ->
                                Text(
                                    text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_subscription_renews, formatDate(renewalDate)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/account/subscriptions")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_manage_subscription))
                    }
                } else {
                    Text(
                        text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_subscription_free),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_subscription_upgrade_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onOpenPaywall,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_upgrade_pro))
                    }
                }
            }

            AnalyticsCard {
                SectionTitle(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_section_cloud_sync))
                SyncActionButton(
                    state = state.cloudSyncState,
                    onClick = viewModel::runCloudSync,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnalyticsCard {
                SectionTitle(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_section_export))
                Text(
                    text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_export_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { exportLauncher.launch(viewModel.suggestedExportFileName()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_export_button))
                }
            }

            AnalyticsCard {
                SectionTitle(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_section_about))
                Text(
                    text = appVersionLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
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
        CloudSyncState.IDLE -> stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_sync_button_idle)
        CloudSyncState.SYNCING -> stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_sync_button_syncing)
        CloudSyncState.DONE -> stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_sync_button_done)
        CloudSyncState.FAILED -> stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.settings_sync_button_failed)
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

