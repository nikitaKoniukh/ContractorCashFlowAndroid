package com.yetzira.ContractorCashFlowAndroid.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yetzira.ContractorCashFlowAndroid.R
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val emoji: String,
    val titleRes: Int,
    val descriptionRes: Int
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = listOf(
        OnboardingPage(
            emoji = "📊",
            titleRes = R.string.onboarding_track_projects_title,
            descriptionRes = R.string.onboarding_track_projects_description
        ),
        OnboardingPage(
            emoji = "💸",
            titleRes = R.string.onboarding_log_expenses_title,
            descriptionRes = R.string.onboarding_log_expenses_description
        ),
        OnboardingPage(
            emoji = "🧾",
            titleRes = R.string.onboarding_create_invoices_title,
            descriptionRes = R.string.onboarding_create_invoices_description
        ),
        OnboardingPage(
            emoji = "👷",
            titleRes = R.string.onboarding_manage_workers_title,
            descriptionRes = R.string.onboarding_manage_workers_description
        ),
        OnboardingPage(
            emoji = "📈",
            titleRes = R.string.onboarding_financial_analytics_title,
            descriptionRes = R.string.onboarding_financial_analytics_description
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex
    val accent = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnboardingPageContent(page = pages[page])
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                val pageIndicatorLabel = stringResource(
                    R.string.onboarding_page_indicator,
                    pagerState.currentPage + 1,
                    pages.size
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = pageIndicatorLabel },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pages.indices.forEach { index ->
                        val selected = index == pagerState.currentPage
                        val width by animateDpAsState(
                            targetValue = if (selected) 20.dp else 8.dp,
                            animationSpec = tween(200),
                            label = "dotWidth"
                        )
                        val color by animateColorAsState(
                            targetValue = if (selected) accent else Color.Gray.copy(alpha = 0.25f),
                            animationSpec = tween(200),
                            label = "dotColor"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(width)
                                .background(color, RoundedCornerShape(4.dp))
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = {
                            if (pagerState.currentPage > 0) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            } else {
                                onFinished()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = stringResource(
                                if (pagerState.currentPage > 0) {
                                    R.string.onboarding_back
                                } else {
                                    R.string.onboarding_skip
                                }
                            ),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            if (isLastPage) {
                                onFinished()
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = stringResource(
                                if (isLastPage) {
                                    R.string.onboarding_get_started
                                } else {
                                    R.string.onboarding_next
                                }
                            ),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.15f))

        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .shadow(12.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black.copy(alpha = 0.05f))
                .padding(28.dp)
        ) {
            Text(
                text = page.emoji,
                fontSize = 72.sp,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(page.descriptionRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.92f)
        )

        Spacer(modifier = Modifier.weight(0.35f))
    }
}
