package com.novaclean.app.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.novaclean.app.R
import com.novaclean.app.domain.model.CompressionPreset
import com.novaclean.app.domain.model.formatFileSize
import com.novaclean.app.presentation.components.GradientButton
import com.novaclean.app.presentation.components.ProBadge
import com.novaclean.app.presentation.theme.ElectricBlue
import com.novaclean.app.presentation.theme.MintGreen
import com.novaclean.app.presentation.viewmodel.MediaCompressorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaCompressorScreen(
    viewModel: MediaCompressorViewModel,
    onBack: () -> Unit,
    onNavigatePaywall: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onImageSelected(it.toString()) }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshStatus()
    }

    if (state.showLimitDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLimitDialog() },
            icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ElectricBlue) },
            title = { Text(stringResource(R.string.category_compressor_title)) },
            text = { Text(stringResource(R.string.compressor_freemium_limit_reached)) },
            confirmButton = {
                Button(onClick = {
                    viewModel.dismissLimitDialog()
                    onNavigatePaywall()
                }) {
                    Text(stringResource(R.string.profile_upgrade_to_pro))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLimitDialog() }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.compressor_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (!state.isPro) {
                        Box(modifier = Modifier.padding(end = 12.dp)) {
                            ProBadge(onClick = onNavigatePaywall)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Photo picker / Preview Card
            if (state.selectedUri == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .clickable {
                            pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = ElectricBlue
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.compressor_select_photo),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    AsyncImage(
                        model = state.selectedUri,
                        contentDescription = stringResource(R.string.compressor_selected_image),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Image Info Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.compressor_original_size, formatFileSize(state.originalSizeBytes)),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        val estimatedSavedBytes = (state.originalSizeBytes * state.selectedPreset.estimatedSavingsPercent) / 100
                        val estimatedResultBytes = state.originalSizeBytes - estimatedSavedBytes
                        Text(
                            text = stringResource(
                                R.string.compressor_estimated_size,
                                formatFileSize(estimatedResultBytes),
                                state.selectedPreset.estimatedSavingsPercent
                            ),
                            fontSize = 13.sp,
                            color = MintGreen
                        )
                    }

                    TextButton(onClick = {
                        pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Text(stringResource(R.string.compressor_pick_another), color = ElectricBlue)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Presets Selector
            Text(
                text = stringResource(R.string.compressor_quality_title),
                modifier = Modifier.fillMaxWidth(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            PresetOptionRow(
                title = stringResource(R.string.compressor_preset_balanced_title),
                description = stringResource(R.string.compressor_preset_balanced_desc),
                isSelected = state.selectedPreset == CompressionPreset.BALANCED,
                onClick = { viewModel.onPresetSelected(CompressionPreset.BALANCED) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            PresetOptionRow(
                title = stringResource(R.string.compressor_preset_high_title),
                description = stringResource(R.string.compressor_preset_high_desc),
                isSelected = state.selectedPreset == CompressionPreset.HIGH_SAVINGS,
                onClick = { viewModel.onPresetSelected(CompressionPreset.HIGH_SAVINGS) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            PresetOptionRow(
                title = stringResource(R.string.compressor_preset_low_title),
                description = stringResource(R.string.compressor_preset_low_desc),
                isSelected = state.selectedPreset == CompressionPreset.LOW_COMPRESSION,
                onClick = { viewModel.onPresetSelected(CompressionPreset.LOW_COMPRESSION) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Result or Action Button
            if (state.compressedResult != null) {
                val result = state.compressedResult!!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MintGreen.copy(alpha = 0.12f))
                        .border(1.dp, MintGreen, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MintGreen, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.compressor_success_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MintGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.compressor_success_desc, formatFileSize(result.savedBytes)),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.isCompressing) {
                CircularProgressIndicator(color = ElectricBlue)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(R.string.compressor_progress), fontSize = 13.sp)
            } else if (state.selectedUri != null && state.compressedResult == null) {
                val estSaved = (state.originalSizeBytes * state.selectedPreset.estimatedSavingsPercent) / 100
                GradientButton(
                    text = stringResource(R.string.compressor_action_compress, formatFileSize(estSaved)),
                    icon = Icons.Default.Compress,
                    onClick = { viewModel.compressSelectedImage() },
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (state.selectedUri == null) {
                GradientButton(
                    text = stringResource(R.string.compressor_btn_pick),
                    icon = Icons.Default.AddPhotoAlternate,
                    onClick = {
                        pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PresetOptionRow(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) ElectricBlue.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) ElectricBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) ElectricBlue else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) ElectricBlue else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
