package com.developermind.focuslock.ui.donation

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.developermind.focuslock.FocusLockApplication
import com.developermind.focuslock.R
import com.developermind.focuslock.data.billing.model.DonationProduct
import com.google.firebase.analytics.FirebaseAnalytics

private val BackgroundColor = Color(0xFF0D0D0D)
private val CardColor = Color(0xFF1A1A1A)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF888888)
private val AccentColor = Color(0xFF64B5F6)

@Composable
fun DonationScreen(
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as FocusLockApplication
    val factory = DonationViewModel.Factory(
        repository = app.donationRepository,
        analytics = FirebaseAnalytics.getInstance(context),
    )
    val viewModel: DonationViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = context as Activity

    LaunchedEffect(Unit) { viewModel.onScreenViewed() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 20.dp, top = 44.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onNavigateBack) {
                Text(
                    text = stringResource(R.string.donation_back),
                    color = AccentColor,
                    fontSize = 14.sp,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.donation_screen_title),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.donation_intro),
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp,
            )

            Spacer(modifier = Modifier.height(32.dp))

            when (val state = uiState) {
                is DonationUiState.Loading -> LoadingContent()
                is DonationUiState.Ready -> ProductsContent(
                    products = state.products,
                    isPurchasing = false,
                    onPurchase = { product -> viewModel.onPurchaseClick(product, activity) },
                )
                is DonationUiState.Purchasing -> ProductsContent(
                    products = state.products,
                    isPurchasing = true,
                    onPurchase = {},
                )
                is DonationUiState.Success -> SuccessContent(onNavigateBack = onNavigateBack)
                is DonationUiState.Pending -> PendingContent(onNavigateBack = onNavigateBack)
                is DonationUiState.Error -> ErrorContent(
                    canRetry = state.canRetry,
                    onRetry = viewModel::onRetry,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            strokeWidth = 2.dp,
            color = AccentColor,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.donation_loading),
            fontSize = 14.sp,
            color = TextSecondary,
        )
    }
}

@Composable
private fun ProductsContent(
    products: List<DonationProduct>,
    isPurchasing: Boolean,
    onPurchase: (DonationProduct) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        products.forEach { product ->
            DonationProductCard(
                product = product,
                enabled = !isPurchasing,
                onClick = { onPurchase(product) },
            )
        }
    }
}

@Composable
private fun DonationProductCard(
    product: DonationProduct,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = product.emoji,
            fontSize = 26.sp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) TextPrimary else TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (product.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.description,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(
            text = product.formattedPrice,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) AccentColor else TextSecondary,
        )
    }
}

@Composable
private fun SuccessContent(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.donation_success_title),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.donation_success_message),
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 20.sp,
        )
        Spacer(modifier = Modifier.height(28.dp))
        TextButton(onClick = onNavigateBack) {
            Text(
                text = stringResource(R.string.donation_success_action),
                color = AccentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun PendingContent(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.donation_pending_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.donation_pending_message),
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 20.sp,
        )
        Spacer(modifier = Modifier.height(28.dp))
        TextButton(onClick = onNavigateBack) {
            Text(
                text = stringResource(R.string.donation_back),
                color = AccentColor,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun ErrorContent(canRetry: Boolean, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.donation_error_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(
                if (canRetry) R.string.donation_error_message else R.string.donation_error_launch
            ),
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 20.sp,
        )
        if (canRetry) {
            Spacer(modifier = Modifier.height(20.dp))
            TextButton(onClick = onRetry) {
                Text(
                    text = stringResource(R.string.donation_retry),
                    color = AccentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
