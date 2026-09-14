package com.novaclean.app.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaclean.app.R
import com.novaclean.app.domain.model.PasswordCategory
import com.novaclean.app.domain.model.PasswordStrengthLevel
import com.novaclean.app.presentation.components.GradientButton
import com.novaclean.app.presentation.theme.DangerRed
import com.novaclean.app.presentation.theme.MintGreen
import com.novaclean.app.presentation.theme.NeonCyan
import com.novaclean.app.presentation.theme.WarningYellow
import com.novaclean.app.presentation.viewmodel.PasswordGeneratorViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGeneratorScreen(
    viewModel: PasswordGeneratorViewModel,
    onBack: () -> Unit,
    onSavePassword: ((service: String, login: String, pass: String, category: PasswordCategory, notes: String?) -> Unit)? = null,
    onNavigateToManager: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showSaveDialog by remember { mutableStateOf(false) }

    val password by viewModel.password.collectAsState()
    val options by viewModel.options.collectAsState()
    val evaluation by viewModel.evaluation.collectAsState()

    fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Generated Password", password)
        clipboard.setPrimaryClip(clip)
        viewModel.onCopyTriggered()
    }

    LaunchedEffect(Unit) {
        viewModel.copyEvent.collectLatest {
            snackbarHostState.showSnackbar(context.getString(R.string.password_gen_copied_toast))
        }
    }

    val strengthColor by animateColorAsState(
        targetValue = when (evaluation.level) {
            PasswordStrengthLevel.WEAK -> DangerRed
            PasswordStrengthLevel.MEDIUM -> WarningYellow
            PasswordStrengthLevel.STRONG -> MintGreen
            PasswordStrengthLevel.EXCELLENT -> NeonCyan
        },
        label = "StrengthColor"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = evaluation.scoreRatio,
        animationSpec = tween(500),
        label = "StrengthProgress"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.password_gen_screen_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (onNavigateToManager != null) {
                        IconButton(onClick = onNavigateToManager) {
                            Icon(Icons.Default.VpnKey, contentDescription = stringResource(R.string.passwords_screen_title), tint = NeonCyan)
                        }
                    }
                    IconButton(onClick = { viewModel.generateNewPassword() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.password_gen_regenerate))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                GradientButton(
                    text = stringResource(R.string.password_gen_copy_action),
                    icon = Icons.Default.ContentCopy,
                    onClick = { copyToClipboard() }
                )
                if (onSavePassword != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.passwords_save_from_generator), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Password Display Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, strengthColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = strengthColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.password_gen_screen_title),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { viewModel.generateNewPassword() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = password,
                        fontFamily = FontFamily.Monospace,
                        fontSize = if (password.length > 20) 18.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { copyToClipboard() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Strength Progress Bar (Kaspersky Style)
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = strengthColor,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Strength description and crack time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(evaluation.titleRes),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = strengthColor
                        )
                        Text(
                            text = stringResource(R.string.password_gen_entropy_bits, evaluation.entropyBits),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val timeToCrackText = if (evaluation.timeToCrackArg != null) {
                        stringResource(evaluation.timeToCrackRes, evaluation.timeToCrackArg!!)
                    } else {
                        stringResource(evaluation.timeToCrackRes)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = strengthColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = timeToCrackText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Settings Header
            Text(
                text = stringResource(R.string.password_gen_settings_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Length Slider Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.password_gen_length_label, options.length),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = options.length.toFloat(),
                        onValueChange = { viewModel.updateLength(it.toInt()) },
                        valueRange = 6f..32f,
                        steps = 25,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Toggles
            ToggleOptionCard(
                title = stringResource(R.string.password_gen_opt_uppercase),
                isChecked = options.includeUppercase,
                onToggle = { viewModel.toggleUppercase() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ToggleOptionCard(
                title = stringResource(R.string.password_gen_opt_lowercase),
                isChecked = options.includeLowercase,
                onToggle = { viewModel.toggleLowercase() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ToggleOptionCard(
                title = stringResource(R.string.password_gen_opt_digits),
                isChecked = options.includeDigits,
                onToggle = { viewModel.toggleDigits() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ToggleOptionCard(
                title = stringResource(R.string.password_gen_opt_symbols),
                isChecked = options.includeSymbols,
                onToggle = { viewModel.toggleSymbols() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ToggleOptionCard(
                title = stringResource(R.string.password_gen_opt_exclude_similar),
                isChecked = options.excludeSimilar,
                onToggle = { viewModel.toggleExcludeSimilar() }
            )

            Spacer(modifier = Modifier.height(30.dp))
        }

        if (showSaveDialog && onSavePassword != null) {
            AddPasswordDialog(
                prefilledPassword = password,
                onDismiss = { showSaveDialog = false },
                onSave = { service, login, pass, cat, notes ->
                    onSavePassword(service, login, pass, cat, notes)
                    showSaveDialog = false
                    Toast.makeText(context, context.getString(R.string.passwords_saved_toast), Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun ToggleOptionCard(
    title: String,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Icon(
            imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isChecked) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
    }
}
