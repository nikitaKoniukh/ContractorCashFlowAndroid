package com.yetzira.ContractorCashFlowAndroid.ui.paywall

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

private val PRIVACY_POLICY_TEXT = """
KablanPro Privacy Policy

Last updated: July 12, 2026

1. Who we are
KablanPro (“the App”) is published by Yetzira / the developer listed on Google Play. This policy explains what data we collect, how it is used, and your choices.

2. Data we collect
Depending on how you use the App, we may process:
• Google account identifiers (email / Firebase Auth UID) when you sign in for cloud sync
• Business data you enter: projects, expenses, invoices, clients, workers/labor details, notes, and preferences (language, currency, theme, notification settings)
• Receipt images you capture or select for OCR (processed on-device with ML Kit; images may be stored locally and referenced in synced expense records)
• Subscription entitlement status via Google Play Billing
• Diagnostic information needed to operate sync and billing (for example connectivity checks)

We do not sell your personal data.

3. How data is stored
• Locally on your device using Room (SQLite) and DataStore preferences
• Optionally in Google Firebase Firestore under your authenticated user path when cloud sync is enabled
• Authentication is handled by Firebase Auth / Google Sign-In

4. How we use data
We use data to:
• provide project, expense, invoice, labor, client, and analytics features
• sync your data across devices when you sign in
• enforce Free vs Pro limits and process subscriptions through Google Play
• schedule invoice reminders and budget warnings you enable
• improve reliability of OCR and sync error handling

5. Third-party services
The App uses:
• Firebase / Google (Auth, Firestore) — https://policies.google.com/privacy
• Google Play Billing — https://payments.google.com/payments/apis-secure/get_legal_document?ldo=0&ldt=privacynotice
• ML Kit Text Recognition — https://developers.google.com/ml-kit/terms

Their privacy terms apply to data they process on our behalf or as independent controllers for their services.

6. Retention and deletion
Local data remains on your device until you delete it in the App or uninstall the App. Synced Firestore data remains associated with your account until you delete records in the App (and sync completes) or request account/data deletion from the developer. Uninstalling alone may not delete cloud data.

7. Your rights
Subject to applicable law, you may request access, correction, or deletion of your personal data, and you may sign out to stop further cloud sync from that device. Contact the developer via the support email on the Google Play listing.

8. Children
The App is intended for business users and is not directed to children under 13 (or the minimum age required in your country).

9. International transfers
If you enable cloud sync, Google/Firebase may process data in data centers outside your country under their terms and safeguards.

10. Changes
We may update this policy. Continued use after updates means you accept the revised policy. The “Last updated” date above will change when material updates are published.

11. Contact
For privacy requests, contact the developer using the support email on the KablanPro Google Play Store listing.
""".trimIndent()

@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Button(onClick = { navController.popBackStack() }) {
            Text(text = "Back")
        }

        Text(
            text = "Privacy Policy",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 12.dp)
        )

        Text(
            text = PRIVACY_POLICY_TEXT,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
