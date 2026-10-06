package com.monomemo.app.ui.trash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.monomemo.app.R
import com.monomemo.app.data.db.NoteEntity
import com.monomemo.app.ui.components.EmptyTrashState
import com.monomemo.app.ui.components.RestoreIcon
import com.monomemo.app.ui.components.TrashCanIcon
import com.monomemo.app.ui.theme.AccentOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    trashNotes: List<NoteEntity>,
    onBack: () -> Unit,
    onRestore: (Long) -> Unit,
    onDeletePermanently: (Long) -> Unit,
    onEmptyAll: () -> Unit,
) {
    var confirmEmptyAll by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.trash_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.trash_back),
                        )
                    }
                },
                actions = {
                    if (trashNotes.isNotEmpty()) {
                        TextButton(onClick = { confirmEmptyAll = true }) {
                            Text(
                                stringResource(R.string.trash_empty_all),
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Info banner
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "\u24D8",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.trash_info_message))
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(stringResource(R.string.trash_info_days))
                        }
                        append(".")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (trashNotes.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    EmptyTrashState(text = stringResource(R.string.trash_empty))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    items(trashNotes, key = { it.id }) { note ->
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        TrashNoteItem(
                            note = note,
                            onRestore = { onRestore(note.id) },
                            onDelete = { pendingDeleteId = note.id },
                        )
                    }
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }

    // 개별 영구삭제 확인
    pendingDeleteId?.let { id ->
        ConfirmDeleteDialog(
            title = stringResource(R.string.trash_confirm_delete_title),
            message = stringResource(R.string.trash_confirm_delete_message),
            onConfirm = {
                onDeletePermanently(id)
                pendingDeleteId = null
            },
            onDismiss = { pendingDeleteId = null },
        )
    }

    // 전체 비우기 확인
    if (confirmEmptyAll) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.trash_confirm_empty_title),
            message = stringResource(R.string.trash_confirm_empty_message),
            onConfirm = {
                onEmptyAll()
                confirmEmptyAll = false
            },
            onDismiss = { confirmEmptyAll = false },
        )
    }
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(R.string.trash_confirm_delete),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.trash_confirm_cancel))
            }
        },
    )
}

@Composable
private fun TrashNoteItem(
    note: NoteEntity,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    val relativeTime = note.deletedAt?.let { formatRelativeTime(it) } ?: ""
    val restoreDescription = stringResource(R.string.trash_restore)
    val deleteDescription = stringResource(R.string.trash_delete)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = note.title.ifEmpty { stringResource(R.string.untitled) },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = relativeTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(
            onClick = onRestore,
            modifier = Modifier.semantics {
                contentDescription = restoreDescription
            },
        ) {
            RestoreIcon(color = AccentOrange)
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.semantics {
                contentDescription = deleteDescription
            },
        ) {
            TrashCanIcon()
        }
    }
}

@Composable
private fun formatRelativeTime(deletedAt: Long): String {
    val now = System.currentTimeMillis()
    val diffMs = now - deletedAt
    val diffHours = diffMs / (1000 * 60 * 60)
    val diffDays = diffHours / 24
    val diffWeeks = diffDays / 7

    val timeStr = when {
        diffDays < 1 -> {
            if (diffHours < 1) stringResource(R.string.trash_just_now)
            else stringResource(R.string.trash_hours, diffHours.toInt())
        }
        diffDays < 7 -> stringResource(R.string.trash_days, diffDays.toInt())
        else -> stringResource(R.string.trash_weeks, diffWeeks.toInt())
    }

    return if (diffHours < 1) {
        timeStr
    } else {
        stringResource(R.string.trash_deleted_ago, timeStr)
    }
}
