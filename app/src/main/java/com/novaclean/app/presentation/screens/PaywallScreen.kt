package com.novaclean.app.presentation.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaclean.app.R
import com.novaclean.app.domain.model.BillingResultState
import com.novaclean.app.domain.model.PlanType
import com.novaclean.app.domain.model.SubscriptionPlan
import com.novaclean.app.presentation.components.GradientButton
import com.novaclean.app.presentation.theme.DarkBackground
import com.novaclean.app.presentation.theme.MintGreen
import com.novaclean.app.presentation.theme.ProGoldEnd
import com.novaclean.app.presentation.theme.ProGoldStart
import com.novaclean.app.presentation.viewmodel.PaywallViewModel

@Composable
fun PaywallScreen(
    viewModel: PaywallViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val snackbarHostState = remember { SnackbarHostState() }

    val plans = viewModel.plans
    val selectedPlanId by viewModel.selectedPlanId.collectAsState()
    val billingState by viewModel.billingState.collectAsState()

    val selectedPlan = plans.first { it.id == selectedPlanId }

    LaunchedEffect(billingState) {
        when (val state = billingState) {
            is BillingResultState.Success -> {
                snackbarHostState.showSnackbar(context.getString(state.messageRes))
                viewModel.resetBillingState()
                onClose()
            }
            is BillingResultState.Error -> {
                snackbarHostState.showSnackbar(context.getString(R.string.paywall_error_prefix, state.errorMessage))
                viewModel.resetBillingState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.action_close),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Crown / Sparkles
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(ProGoldStart.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = ProGoldStart,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.paywall_screen_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = stringResource(R.string.paywall_screen_subtitle),
                fontSize = 14.sp,
                color = Color(0xFFB0B8C4),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Benefits
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF161B26))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BenefitItem(stringResource(R.string.paywall_benefit_unlimited_clean))
                BenefitItem(stringResource(R.string.paywall_benefit_ai_similar_photos))
                BenefitItem(stringResource(R.string.paywall_benefit_merge_contacts))
                BenefitItem(stringResource(R.string.paywall_benefit_deep_ram))
                BenefitItem(stringResource(R.string.paywall_benefit_no_ads))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Plan Cards
            plans.forEach { plan ->
                val isSelected = plan.id == selectedPlanId
                PlanCard(
                    plan = plan,
                    isSelected = isSelected,
                    onClick = { viewModel.selectPlan(plan.id) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CTA Button
            val ctaText = if (selectedPlan.hasFreeTrial) {
                stringResource(R.string.paywall_cta_trial, selectedPlan.trialDays)
            } else if (selectedPlan.type == PlanType.LIFETIME) {
                stringResource(R.string.paywall_cta_lifetime, stringResource(selectedPlan.priceTextRes))
            } else {
                stringResource(R.string.paywall_cta_subscribe)
            }

            if (billingState is BillingResultState.Loading) {
                CircularProgressIndicator(color = ProGoldStart)
            } else {
                GradientButton(
                    text = ctaText,
                    icon = Icons.Default.Star,
                    gradient = listOf(ProGoldStart, ProGoldEnd),
                    isPulsing = true,
                    onClick = { viewModel.purchaseSelectedPlan(activity) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Restore Purchases & Terms
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.paywall_restore_action),
                    fontSize = 12.sp,
                    color = Color(0xFF8A99AD),
                    modifier = Modifier.clickable { viewModel.restorePurchases() }
                )
                Text(
                    text = "  •  ",
                    fontSize = 12.sp,
                    color = Color(0xFF556070)
                )
                Text(
                    text = stringResource(R.string.paywall_terms_privacy),
                    fontSize = 12.sp,
                    color = Color(0xFF8A99AD)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MintGreen.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MintGreen,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
fun PlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) ProGoldStart else Color(0xFF2E384D)
    val bgColor = if (isSelected) Color(0xFF231F14) else Color(0xFF161B26)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(plan.titleRes),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    if (plan.badgeRes != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Brush.horizontalGradient(listOf(ProGoldStart, ProGoldEnd)))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(plan.badgeRes),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(plan.subtitleRes),
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(plan.priceTextRes),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = if (isSelected) ProGoldStart else Color.White
                )
                Text(
                    text = stringResource(plan.periodTextRes),
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}
