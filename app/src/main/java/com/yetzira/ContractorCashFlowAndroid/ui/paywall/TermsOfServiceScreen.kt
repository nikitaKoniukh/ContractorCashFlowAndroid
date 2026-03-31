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

@Composable
fun TermsOfServiceScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)) {
        // Back Button
        Button(onClick = { onBack() }, Modifier.padding(16.dp)) {
            Text(text = "Back")
        }

        // Scrollable Content
        Text(
            text = "Terms of Service Content goes here...",
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