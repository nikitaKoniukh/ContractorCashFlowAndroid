package com.yetzira.ContractorCashFlowAndroid.ui.paywall

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState

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
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTermsOfServiceScreen() {
    TermsOfServiceScreen(onBack = {})
}