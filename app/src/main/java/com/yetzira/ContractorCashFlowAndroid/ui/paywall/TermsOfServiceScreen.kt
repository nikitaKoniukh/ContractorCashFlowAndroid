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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val TERMS_OF_SERVICE_TEXT = """
KablanPro Terms of Use

Last updated: July 12, 2026

1. Acceptance of Terms
By downloading, accessing, or using KablanPro (“the App”), you agree to these Terms of Use. If you do not agree, do not use the App.

2. Description of the Service
KablanPro is a contractor cash-flow management application that lets you manage projects, workers, expenses, invoices, clients, analytics, receipt scanning, and optional cloud backup. The App is offered under a Free plan and paid KablanPro Monthly / Yearly subscriptions.

3. Free and Paid Plans
• Free plan: limited projects and workers as shown in the App.
• KablanPro Monthly and Yearly: subscription plans billed through Google Play that unlock unlimited projects and workers (and any other Pro features shown in the App).
Subscription prices, billing periods, renewal, and cancellation are handled by Google Play. You can manage or cancel subscriptions in your Google Play account settings. Auto-renewal continues until you cancel according to Google Play’s policies.

4. User Responsibilities
You are responsible for:
• the accuracy of data you enter;
• keeping your device and Google account secure;
• complying with applicable laws when recording financial or business information;
• not misusing the App, attempting unauthorized access, or interfering with cloud sync or billing systems.

5. Accounts and Cloud Sync
Optional Google Sign-In enables Firebase Authentication and Firestore sync. You remain responsible for data you choose to sync. Sign out to stop associating new sync activity with your account on that device.

6. Intellectual Property
The App, branding, and related materials are owned by Yetzira / the developer. You receive a limited, non-exclusive, non-transferable license to use the App for your own business purposes.

7. Disclaimer
The App is provided “as is” for organizational and informational purposes. It is not professional accounting, tax, or legal advice. We do not warrant uninterrupted or error-free operation.

8. Limitation of Liability
To the maximum extent permitted by law, the developer is not liable for indirect, incidental, special, consequential, or lost-profit damages arising from use of the App, including data loss, sync conflicts, or billing issues handled by Google Play.

9. Termination
We may suspend or terminate access if you violate these terms. You may stop using the App at any time and cancel subscriptions via Google Play.

10. Governing Law
These terms are governed by the laws applicable in the developer’s jurisdiction, without regard to conflict-of-law rules. Courts in that jurisdiction have exclusive venue, except where consumer law requires otherwise.

11. Contact
For questions about these Terms, contact the developer using the support email listed on the Google Play Store listing for KablanPro.

12. Changes
We may update these Terms. Continued use after changes means you accept the updated Terms. Material changes will be reflected by updating the “Last updated” date above.
""".trimIndent()

@Composable
fun TermsOfServiceScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Button(onClick = onBack, Modifier.padding(16.dp)) {
            Text(text = "Back")
        }

        Text(
            text = "Terms of Use",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Text(
            text = TERMS_OF_SERVICE_TEXT,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTermsOfServiceScreen() {
    TermsOfServiceScreen(onBack = {})
}
