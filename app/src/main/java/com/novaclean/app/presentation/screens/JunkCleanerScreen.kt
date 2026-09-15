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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaclean.app.R
import com.novaclean.app.domain.model.JunkCategory
import com.novaclean.app.domain.model.formatFileSize
import com.novaclean.app.presentation.components.GradientButton
import com.novaclean.app.presentation.theme.ElectricBlue
import com.novaclean.app.presentation.theme.MintGreen
import com.novaclean.app.presentation.theme.NeonCyan
import com.novaclean.app.presentation.theme.OrangeFlame
import com.novaclean.app.presentation.viewmodel.JunkCleanerViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JunkCleanerScreen(
    viewModel: JunkCleanerViewModel,
    onBack: () -> Unit,
    isProUser: Boolean = false,
    interstitialAdManager: com.novaclean.app.ads.InterstitialAdManager? = null
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val isScanning by viewModel.isScanning.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cleanSuccessEvent.collectLatest { (count, freed) ->
            snackbarHostState.showSnackbar(
                context.getString(R.string.junk_cleaned_toast, count, formatFileSize(freed))
            )
            (context as? android.app.Activity)?.let { act ->
                interstitialAdManager?.showAdIfAvailable(act, isProUser)
            }
        }
    }

    val totalSelectedBytes = scanResult?.let { res ->
        res.items.filter { selectedCategories[it.category] == true }.sumOf { it.size }
    } ?: 0L

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.junk_screen_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.scanJunk() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.action_refresh))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            if (!isScanning && (scanResult?.items?.isNotEmpty() == true)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.junk_will_free_label),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatFileSize(totalSelectedBytes),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrangeFlame
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        GradientButton(
                            text = stringResource(R.string.action_clean_junk, formatFileSize(totalSelectedBytes)),
                            icon = Icons.Default.CleaningServices,
                            gradient = listOf(OrangeFlame, Color(0xFFFF1744)),
                            onClick = { viewModel.cleanJunk() }
                        )
                        if (!isProUser) {
                            Spacer(modifier = Modifier.height(8.dp))
                            com.novaclean.app.presentation.components.YandexBannerAd(isProUser = isProUser)
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isScanning) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = OrangeFlame)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.junk_scanning_progress), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else if (scanResult == null || scanResult!!.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MintGreen,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.junk_empty_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.junk_empty_desc),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val res = scanResult!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.junk_total_found, formatFileSize(res.totalSize)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(JunkCategory.values().size) { idx ->
                        val cat = JunkCategory.values()[idx]
                        val count = res.categoryCount[cat] ?: 0
                        val size = res.categorySizes[cat] ?: 0L
                        val isChecked = selectedCategories[cat] ?: true

                        JunkCategoryRow(
                            category = cat,
                            count = count,
                            size = size,
                            isChecked = isChecked,
                            onToggle = { viewModel.toggleCategory(cat) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun JunkCategoryRow(
    category: JunkCategory,
    count: Int,
    size: Long,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    val titleRes = when (category) {
        JunkCategory.ZERO_BYTE_FILES -> R.string.junk_cat_zero_byte_title
        JunkCategory.EMPTY_FOLDERS -> R.string.junk_cat_empty_folders_title
        JunkCategory.OLD_APKS -> R.string.junk_cat_old_apks_title
        JunkCategory.TEMP_FILES -> R.string.junk_cat_temp_files_title
        JunkCategory.APP_CACHE -> R.string.junk_cat_app_cache_title
    }

    val icon: ImageVector = when (category) {
        JunkCategory.ZERO_BYTE_FILES -> Icons.Default.Warning
        JunkCategory.EMPTY_FOLDERS -> Icons.Default.FolderOpen
        JunkCategory.OLD_APKS -> Icons.Default.Android
        JunkCategory.TEMP_FILES -> Icons.Default.Delete
        JunkCategory.APP_CACHE -> Icons.Default.Layers
    }

    val iconColor: Color = when (category) {
        JunkCategory.ZERO_BYTE_FILES -> OrangeFlame
        JunkCategory.EMPTY_FOLDERS -> ElectricBlue
        JunkCategory.OLD_APKS -> MintGreen
        JunkCategory.TEMP_FILES -> NeonCyan
        JunkCategory.APP_CACHE -> Color(0xFFAB47BC)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onToggle)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(titleRes),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.junk_category_files_count, count, formatFileSize(size)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isChecked) OrangeFlame else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}
