@file:Suppress("DEPRECATION")

package com.yetzira.ContractorCashFlowAndroid.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.yetzira.ContractorCashFlowAndroid.data.preferences.AppLanguageOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.ui.components.AnalyticsCard
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProColors
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
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
    }
    val appVersionLabel = remember(packageInfo) {
        "KablanPro ${packageInfo.versionName}.${PackageInfoCompat.getLongVersionCode(packageInfo)}"
    }
    val webClientId = remember {
        val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (id != 0) context.getString(id) else null
    }
    val googleSignInSetupError = remember(webClientId) {
        if (webClientId.isNullOrBlank()) {
            "Google sign-in setup is incomplete: missing default_web_client_id in resources. Regenerate google-services.json with a Web OAuth client."
        } else {
            null
        }
    }
    val googleSignInClient = remember {
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
        if (!webClientId.isNullOrBlank()) {
            builder.requestIdToken(webClientId)
        }
        val options = builder.build()
        GoogleSignIn.getClient(context, options)
    }
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(
            SETTINGS_AUTH_LOG_TAG,
            "Google launcher callback resultCode=${result.resultCode} hasData=${result.data != null}"
        )
        val fallbackMessage = if (result.resultCode == Activity.RESULT_OK) {
            "Google sign-in failed"
        } else {
            "Google sign-in was cancelled or did not complete"
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        runCatching { task.getResult(ApiException::class.java) }
            .onSuccess { account ->
                val idToken = account.idToken
                Log.d(
                    SETTINGS_AUTH_LOG_TAG,
                    "Google account resolved email=${account.email.orEmpty()} idTokenPresent=${!idToken.isNullOrBlank()}"
                )
                if (!idToken.isNullOrBlank()) {
                    viewModel.signInWithGoogleIdToken(idToken)
                } else {
                    viewModel.onGoogleSignInFailed("Google sign-in is missing ID token. Check Firebase OAuth client setup.")
                }
            }
            .onFailure { throwable ->
                val message = googleSignInErrorMessage(throwable, fallbackMessage)
                Log.w(SETTINGS_AUTH_LOG_TAG, "Google account task failed: $message", throwable)
                viewModel.onGoogleSignInFailed(message)
            }
    }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let(viewModel::exportData)
    }

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
                SectionTitle("Account")
                if (state.isAuthenticated) {
                    Text(
                        text = state.userEmail ?: "Signed in",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    OutlinedButton(
                        onClick = viewModel::signOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Text("Sign out")
                    }
                } else {
                    Text(
                        text = "Sign in to enable user-scoped cloud sync.",
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
                        onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        enabled = googleSignInSetupError == null
                    ) {
                        Text("Sign in with Google")
                    }
                }
            }

            SettingsDropdownCard(
                title = "Language",
                selectedLabel = state.selectedLanguage.displayName,
                options = AppLanguageOption.entries.toList(),
                optionLabel = { it.displayName },
                onOptionSelected = viewModel::setLanguage
            )

            SettingsDropdownCard(
                title = "Currency",
                selectedLabel = "${state.selectedCurrency.code} (${state.selectedCurrency.symbol})",
                options = CurrencyOption.entries.toList(),
                optionLabel = { "${it.code} (${it.symbol})" },
                onOptionSelected = viewModel::setCurrency
            )

            AnalyticsCard {
                SectionTitle("Notifications")
                SettingsSwitchRow(
                    title = "Invoice Reminders",
                    checked = state.invoiceRemindersEnabled,
                    onCheckedChange = viewModel::setInvoiceRemindersEnabled
                )
                SettingsSwitchRow(
                    title = "Overdue Alerts",
                    checked = state.overdueAlertsEnabled,
                    onCheckedChange = viewModel::setOverdueAlertsEnabled
                )
                SettingsSwitchRow(
                    title = "Budget Warnings",
                    checked = state.budgetWarningsEnabled,
                    onCheckedChange = viewModel::setBudgetWarningsEnabled
                )
            }

            AnalyticsCard {
                SectionTitle("Subscription")
                if (state.subscription.isPro) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "👑", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.subscription.planName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Renews ${formatDate(state.subscription.renewalDate)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://play.google.com/store/account/subscriptions".toUri()
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Manage Subscription")
                    }
                } else {
                    Text(
                        text = "Free Plan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Upgrade to KablanPro Pro to unlock premium features.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onOpenPaywall,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Upgrade to Pro")
                    }
                }
            }

            AnalyticsCard {
                SectionTitle("Cloud Sync")
                SyncActionButton(
                    state = state.cloudSyncState,
                    onClick = viewModel::runCloudSync,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnalyticsCard {
                SectionTitle("Export Data")
                Text(
                    text = "Create a JSON snapshot of all local KablanPro data.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { exportLauncher.launch(viewModel.suggestedExportFileName()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export Data")
                }
            }

            AnalyticsCard {
                SectionTitle("About")
                Text(
                    text = appVersionLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
private fun <T> SettingsDropdownCard(
    title: String,
    selectedLabel: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    AnalyticsCard {
        SectionTitle(title)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionLabel(option)) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
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
        CloudSyncState.IDLE -> "Sync Now"
        CloudSyncState.SYNCING -> "Syncing..."
        CloudSyncState.DONE -> "Synced"
        CloudSyncState.FAILED -> "Sync Failed"
    }
    val color = when (state) {
        CloudSyncState.IDLE -> MaterialTheme.colorScheme.primary
        CloudSyncState.SYNCING -> MaterialTheme.colorScheme.primary
        CloudSyncState.DONE -> KablanProColors.IncomeGreen
        CloudSyncState.FAILED -> KablanProColors.ExpenseRed
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
                painter = painterResource(android.R.drawable.stat_notify_sync),
                contentDescription = label,
                tint = color
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

private fun googleSignInErrorMessage(throwable: Throwable, fallback: String): String {
    val apiException = throwable as? ApiException
    val code = apiException?.statusCode ?: return throwable.message ?: fallback
    val message = when (code) {
        GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Google sign-in was cancelled"
        GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Google sign-in failed"
        GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS -> "Google sign-in is already in progress"
        CommonStatusCodes.NETWORK_ERROR -> "Network error during Google sign-in"
        CommonStatusCodes.DEVELOPER_ERROR -> "Google sign-in setup error (OAuth client/SHA mismatch)"
        CommonStatusCodes.INTERNAL_ERROR -> "Internal Google Play services error"
        else -> apiException.status.statusMessage ?: fallback
    }
    return "$message (code=$code)"
}

private const val SETTINGS_AUTH_LOG_TAG = "KablanProAuth"

