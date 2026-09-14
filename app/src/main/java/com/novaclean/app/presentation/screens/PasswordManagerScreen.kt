package com.novaclean.app.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaclean.app.R
import com.novaclean.app.domain.model.PasswordCategory
import com.novaclean.app.domain.model.SavedPassword
import com.novaclean.app.presentation.theme.ElectricBlue
import com.novaclean.app.presentation.theme.MintGreen
import com.novaclean.app.presentation.theme.NeonCyan
import com.novaclean.app.presentation.theme.NeonPurple
import com.novaclean.app.presentation.viewmodel.PasswordManagerViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordManagerScreen(
    viewModel: PasswordManagerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { event ->
            val message = when (event) {
                "SAVED" -> context.getString(R.string.passwords_saved_toast)
                "DELETED" -> context.getString(R.string.passwords_deleted_toast)
                else -> event
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.passwords_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog("") },
                containerColor = NeonCyan,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.passwords_add_action))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.query,
                onValueChange = { viewModel.onQueryChanged(it) },
                placeholder = { Text(stringResource(R.string.passwords_search_hint), fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElectricBlue) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Category Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PasswordCategory.entries.toTypedArray()) { cat ->
                    val label = when (cat) {
                        PasswordCategory.ALL -> stringResource(R.string.passwords_cat_all)
                        PasswordCategory.SOCIAL -> stringResource(R.string.passwords_cat_social)
                        PasswordCategory.MAIL -> stringResource(R.string.passwords_cat_mail)
                        PasswordCategory.FINANCE -> stringResource(R.string.passwords_cat_finance)
                        PasswordCategory.WORK -> stringResource(R.string.passwords_cat_work)
                        PasswordCategory.OTHER -> stringResource(R.string.passwords_cat_other)
                    }
                    val isSelected = state.activeCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCategorySelected(cat) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricBlue
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ElectricBlue)
                }
            } else if (state.passwords.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(54.dp), tint = ElectricBlue)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = stringResource(R.string.passwords_empty_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.passwords_empty_desc),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.passwords, key = { it.id }) { item ->
                        val isVisible = state.visiblePasswordIds.contains(item.id)
                        PasswordItemCard(
                            item = item,
                            isPasswordVisible = isVisible,
                            onToggleVisibility = { viewModel.toggleVisibility(item.id) },
                            onDelete = { viewModel.deletePassword(item.id) },
                            onCopy = { text ->
                                copyToClipboard(context, text)
                                Toast.makeText(context, context.getString(R.string.passwords_copied_toast, text), Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        if (state.showAddDialog) {
            AddPasswordDialog(
                prefilledPassword = state.prefilledPassword,
                onDismiss = { viewModel.dismissAddDialog() },
                onSave = { service, login, pass, cat, notes ->
                    viewModel.savePassword(service, login, pass, cat, notes)
                }
            )
        }
    }
}

@Composable
private fun PasswordItemCard(
    item: SavedPassword,
    isPasswordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onDelete: () -> Unit,
    onCopy: (String) -> Unit
) {
    val categoryLabel = when (item.category) {
        PasswordCategory.SOCIAL -> stringResource(R.string.passwords_cat_social)
        PasswordCategory.MAIL -> stringResource(R.string.passwords_cat_mail)
        PasswordCategory.FINANCE -> stringResource(R.string.passwords_cat_finance)
        PasswordCategory.WORK -> stringResource(R.string.passwords_cat_work)
        else -> stringResource(R.string.passwords_cat_other)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NeonPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.serviceName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = NeonPurple,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.serviceName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = categoryLabel,
                        fontSize = 11.sp,
                        color = MintGreen
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Login Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.passwords_login_label), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = item.login, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            IconButton(onClick = { onCopy(item.login) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Password Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.passwords_password_label), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = if (isPasswordVisible) item.password else "•".repeat(item.password.length.coerceIn(8, 16)),
                    fontSize = 14.sp,
                    fontFamily = if (isPasswordVisible) FontFamily.Monospace else FontFamily.Default,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPasswordVisible) NeonCyan else MaterialTheme.colorScheme.onSurface
                )
            }

            Row {
                IconButton(onClick = onToggleVisibility, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = { onCopy(item.password) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (!item.notes.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.notes,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun AddPasswordDialog(
    prefilledPassword: String,
    onDismiss: () -> Unit,
    onSave: (service: String, login: String, pass: String, category: PasswordCategory, notes: String?) -> Unit
) {
    var service by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf(prefilledPassword) }
    var notes by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(PasswordCategory.OTHER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.passwords_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = service,
                    onValueChange = { service = it },
                    label = { Text(stringResource(R.string.passwords_service_label), fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = login,
                    onValueChange = { login = it },
                    label = { Text(stringResource(R.string.passwords_login_label), fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.passwords_password_label), fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.passwords_notes_label), fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (service.isNotBlank() && password.isNotBlank()) {
                        onSave(service, login, password, selectedCategory, notes)
                    }
                }
            ) {
                Text(stringResource(R.string.vault_dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.vault_dialog_cancel))
            }
        }
    )
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("NovaClean", text)
    clipboard.setPrimaryClip(clip)
}
