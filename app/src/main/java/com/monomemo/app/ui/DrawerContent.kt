package com.monomemo.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.monomemo.app.data.db.NoteEntity

@Composable
fun DrawerContent(
    notes: List<NoteEntity>,
    currentNoteId: Long?,
    onNewNote: () -> Unit,
    onNoteSelect: (Long) -> Unit,
    onTrashClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCloseDrawer: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredNotes = remember(notes, searchQuery) {
        if (searchQuery.isBlank()) {
            notes
        } else {
            val q = searchQuery.lowercase()
            notes.filter { note ->
                note.title.lowercase().contains(q) || note.content.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "MonoMemo",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = {
                onNewNote()
                onCloseDrawer()
            }) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }

        // Search
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("노트 검색", style = MaterialTheme.typography.bodyMedium)
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
            ),
        )

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(4.dp))

        // Note list
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredNotes, key = { it.id }) { note ->
                val isSelected = note.id == currentNoteId
                Text(
                    text = note.title.ifEmpty { "제목 없음" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onNoteSelect(note.id)
                            onCloseDrawer()
                        }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(8.dp))

        // Footer
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = {
                onTrashClick()
                onCloseDrawer()
            }) {
                Text(
                    "휴지통",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = {
                onSettingsClick()
                onCloseDrawer()
            }) {
                Text(
                    "설정",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
