package com.novaclean.app.presentation.screens

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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaclean.app.R
import com.novaclean.app.domain.model.formatFileSize
import com.novaclean.app.presentation.components.CategoryCard
import com.novaclean.app.presentation.components.CircularGauge
import com.novaclean.app.presentation.components.GradientButton
import com.novaclean.app.presentation.components.ProBadge
import com.novaclean.app.presentation.theme.ElectricBlue
import com.novaclean.app.presentation.theme.MintGreen
import com.novaclean.app.presentation.theme.NeonCyan
import com.novaclean.app.presentation.theme.OrangeFlame
import com.novaclean.app.presentation.theme.ProGoldEnd
import com.novaclean.app.presentation.theme.ProGoldStart
import com.novaclean.app.presentation.viewmodel.DashboardViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToPhotos: () -> Unit,
    onNavigateToAudio: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToJunk: () -> Unit,
    onNavigateToCompressor: () -> Unit,
    onNavigateToLargeFiles: () -> Unit,
    onNavigateToVault: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val isPro by viewModel.isProUser.collectAsState()
    val ramInfo by viewModel.ramInfo.collectAsState()
    val isOptimizing by viewModel.isOptimizing.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshRamInfo()
        viewModel.freedBytesEvent.collectLatest { freed ->
            val msg = context.getString(R.string.dashboard_ram_freed_toast, formatFileSize(freed))
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.app_name_nova),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.app_name_clean),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonCyan
                        )
                        if (isPro) {
                            Spacer(modifier = Modifier.width(8.dp))
                            ProBadge()
                        }
                    }
                    Text(
                        text = stringResource(R.string.app_tagline),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isPro) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(ProGoldStart.copy(alpha = 0.2f), ProGoldEnd.copy(alpha = 0.2f))
                                    )
                                )
                                .border(
                                    1.dp,
                                    Brush.horizontalGradient(listOf(ProGoldStart, ProGoldEnd)),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(onClick = onNavigateToPaywall)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = ProGoldStart,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.badge_pro),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ProGoldStart
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Profile Icon Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                            .clickable(onClick = onNavigateToProfile)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.action_profile),
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // RAM Gauge
            CircularGauge(
                percentage = ramInfo.usedPercentage,
                title = stringResource(R.string.dashboard_ram_title),
                subtitle = stringResource(R.string.dashboard_ram_subtitle, ramInfo.formattedUsed, ramInfo.formattedTotal),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Optimize Button
            GradientButton(
                text = if (isOptimizing) stringResource(R.string.action_optimizing) else stringResource(R.string.action_accelerate_ram),
                icon = Icons.Default.Bolt,
                isPulsing = ramInfo.usedPercentage > 75,
                onClick = { viewModel.optimizeRam() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Pro Banner
            if (!isPro) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF2C1F03), Color(0xFF1E1708))
                            )
                        )
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(ProGoldStart.copy(alpha = 0.5f), ProGoldEnd.copy(alpha = 0.5f))),
                            RoundedCornerShape(18.dp)
                        )
                        .clickable(onClick = onNavigateToPaywall)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.dashboard_pro_banner_title),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = ProGoldStart
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                ProBadge()
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.dashboard_pro_banner_desc),
                                fontSize = 12.sp,
                                color = Color(0xFFD4AF37)
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.horizontalGradient(listOf(ProGoldStart, ProGoldEnd)))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.action_open),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Clean Modules Title
            Text(
                text = stringResource(R.string.dashboard_modules_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Categories
            CategoryCard(
                title = stringResource(R.string.category_photos_title),
                subtitle = stringResource(R.string.category_photos_desc),
                icon = Icons.Default.PhotoLibrary,
                iconColor = NeonCyan,
                onClick = onNavigateToPhotos
            )

            Spacer(modifier = Modifier.height(12.dp))

            CategoryCard(
                title = stringResource(R.string.category_audio_title),
                subtitle = stringResource(R.string.category_audio_desc),
                icon = Icons.Default.Audiotrack,
                iconColor = ElectricBlue,
                onClick = onNavigateToAudio
            )

            Spacer(modifier = Modifier.height(12.dp))

            CategoryCard(
                title = stringResource(R.string.category_contacts_title),
                subtitle = stringResource(R.string.category_contacts_desc),
                icon = Icons.Default.Contacts,
                iconColor = MintGreen,
                onClick = onNavigateToContacts
            )

            Spacer(modifier = Modifier.height(12.dp))

            CategoryCard(
                title = stringResource(R.string.category_junk_title),
                subtitle = stringResource(R.string.category_junk_desc),
                icon = Icons.Default.DeleteSweep,
                iconColor = OrangeFlame,
                onClick = onNavigateToJunk
            )

            Spacer(modifier = Modifier.height(12.dp))

            CategoryCard(
                title = stringResource(R.string.category_compressor_title),
                subtitle = stringResource(R.string.category_compressor_desc),
                icon = Icons.Default.Compress,
                iconColor = NeonCyan,
                onClick = onNavigateToCompressor
            )

            Spacer(modifier = Modifier.height(12.dp))

            CategoryCard(
                title = stringResource(R.string.category_large_files_title),
                subtitle = stringResource(R.string.category_large_files_desc),
                icon = Icons.Default.FolderZip,
                iconColor = ElectricBlue,
                onClick = onNavigateToLargeFiles
            )

            Spacer(modifier = Modifier.height(12.dp))

            CategoryCard(
                title = stringResource(R.string.category_vault_title),
                subtitle = stringResource(R.string.category_vault_desc),
                icon = Icons.Default.Lock,
                iconColor = com.novaclean.app.presentation.theme.NeonPurple,
                onClick = onNavigateToVault
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
