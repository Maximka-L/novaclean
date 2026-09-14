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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.novaclean.app.R
import com.novaclean.app.domain.model.VaultItem
import com.novaclean.app.presentation.components.GradientButton
import com.novaclean.app.presentation.components.ProBadge
import com.novaclean.app.presentation.theme.ElectricBlue
import com.novaclean.app.presentation.theme.MintGreen
import com.novaclean.app.presentation.theme.NeonPurple
import com.novaclean.app.presentation.viewmodel.VaultStep
import com.novaclean.app.presentation.viewmodel.VaultViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: VaultViewModel,
    onBack: () -> Unit,
    onNavigatePaywall: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            viewModel.addPhoto(it.toString(), "Vault_Photo_${System.currentTimeMillis()}.jpg")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkAccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.vault_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (state.step == VaultStep.UNLOCKED) {
                        IconButton(onClick = { viewModel.lockVault() }) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ElectricBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (state.step == VaultStep.UNLOCKED) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.activeTab == 0) {
                        FloatingActionButton(
                            onClick = {
                                pickPhotoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            containerColor = ElectricBlue,
                            contentColor = Color.Black
                        ) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.vault_add_photo))
                        }
                    } else {
                        FloatingActionButton(
                            onClick = { viewModel.showNoteDialog(true) },
                            containerColor = MintGreen,
                            contentColor = Color.Black
                        ) {
                            Icon(Icons.Default.NoteAdd, contentDescription = stringResource(R.string.vault_add_note))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.step) {
                VaultStep.PRO_REQUIRED -> {
                    VaultProGateView(onNavigatePaywall = onNavigatePaywall)
                }
                VaultStep.SETUP_PIN, VaultStep.CONFIRM_PIN, VaultStep.ENTER_PIN -> {
                    VaultPinEntryView(
                        step = state.step,
                        enteredPin = state.enteredPin,
                        errorMessage = state.errorMessage,
                        onDigit = { viewModel.onPinDigit(it) },
                        onBackspace = { viewModel.onPinBackspace() }
                    )
                }
                VaultStep.UNLOCKED -> {
                    VaultContentView(
                        state = state,
                        onTabSelect = { viewModel.setTab(it) },
                        onDelete = { viewModel.deleteItem(it) },
                        onRestore = { viewModel.restoreItem(it) }
                    )
                }
            }

            if (state.showNoteDialog) {
                CreateNoteDialog(
                    onDismiss = { viewModel.showNoteDialog(false) },
                    onSave = { title, content -> viewModel.saveNote(title, content) }
                )
            }
        }
    }
}

@Composable
private fun VaultProGateView(onNavigatePaywall: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(NeonPurple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.size(54.dp),
                tint = NeonPurple
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        ProBadge(onClick = onNavigatePaywall)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.vault_pro_gate_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.vault_pro_gate_desc),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        GradientButton(
            text = stringResource(R.string.vault_action_unlock),
            icon = Icons.Default.Lock,
            onClick = onNavigatePaywall,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun VaultPinEntryView(
    step: VaultStep,
    enteredPin: String,
    errorMessage: String?,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = ElectricBlue
        )

        Spacer(modifier = Modifier.height(16.dp))

        val promptText = when (step) {
            VaultStep.SETUP_PIN -> stringResource(R.string.vault_setup_pin)
            VaultStep.CONFIRM_PIN -> stringResource(R.string.vault_confirm_pin)
            else -> stringResource(R.string.vault_enter_pin)
        }

        Text(
            text = promptText,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // PIN Indicator Dots
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (i in 0 until 4) {
                val isFilled = i < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (isFilled) ElectricBlue else Color.Transparent)
                        .border(2.dp, if (isFilled) ElectricBlue else MaterialTheme.colorScheme.outline, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            val errorText = when (errorMessage) {
                "PIN_MISMATCH" -> stringResource(R.string.vault_pin_mismatch)
                else -> stringResource(R.string.vault_wrong_pin)
            }
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        } else {
            Spacer(modifier = Modifier.height(18.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Keypad (3x4)
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "DEL")
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            for (row in keys) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (key in row) {
                        when (key) {
                            "" -> Spacer(modifier = Modifier.size(72.dp))
                            "DEL" -> {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .clickable { onBackspace() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .clickable { onDigit(key) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultContentView(
    state: com.novaclean.app.presentation.viewmodel.VaultUiState,
    onTabSelect: (Int) -> Unit,
    onDelete: (VaultItem) -> Unit,
    onRestore: (VaultItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = state.activeTab) {
            Tab(
                selected = state.activeTab == 0,
                onClick = { onTabSelect(0) },
                text = { Text(stringResource(R.string.vault_tab_photos) + " (${state.photoItems.size})") }
            )
            Tab(
                selected = state.activeTab == 1,
                onClick = { onTabSelect(1) },
                text = { Text(stringResource(R.string.vault_tab_notes) + " (${state.noteItems.size})") }
            )
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ElectricBlue)
            }
        } else if (state.activeTab == 0) {
            // Photos Tab
            if (state.photoItems.isEmpty()) {
                VaultEmptyPlaceholder()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.photoItems, key = { it.id }) { item ->
                        VaultPhotoCard(item = item, onDelete = { onDelete(item) }, onRestore = { onRestore(item) })
                    }
                }
            }
        } else {
            // Notes Tab
            if (state.noteItems.isEmpty()) {
                VaultEmptyPlaceholder()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.noteItems, key = { it.id }) { note ->
                        VaultNoteCard(note = note, onDelete = { onDelete(note) })
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultPhotoCard(
    item: VaultItem,
    onDelete: () -> Unit,
    onRestore: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = File(item.internalPath),
            contentDescription = item.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(2.dp)
        ) {
            IconButton(onClick = onRestore, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Restore, contentDescription = stringResource(R.string.vault_restore_item), tint = Color.White, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.vault_delete_item), tint = Color.Red, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun VaultNoteCard(
    note: VaultItem,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = note.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.noteContent ?: "",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun VaultEmptyPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(48.dp), tint = ElectricBlue)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = stringResource(R.string.vault_empty_title), fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.vault_empty_desc),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CreateNoteDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.vault_dialog_note_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Заголовок") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(stringResource(R.string.vault_dialog_note_hint)) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isNotBlank()) onSave(title, content)
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
