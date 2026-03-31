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
			text = """
				Insert privacy policy details here, explaining how user data is collected, used, and shared.
				Please ensure you comply with relevant privacy regulations and include clear information about user rights.
			""".trimIndent(),
			style = MaterialTheme.typography.bodyMedium,
			modifier = Modifier.padding(top = 12.dp)
		)
	}
}
