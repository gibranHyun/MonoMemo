package com.monomemo.app.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.monomemo.app.R

@Composable
fun SearchTopBar(
    findQuery: String,
    onFindQueryChange: (String) -> Unit,
    replaceQuery: String,
    onReplaceQueryChange: (String) -> Unit,
    matchCount: Int,
    currentIndex: Int,
    limitReached: Boolean,
    caseSensitive: Boolean,
    wholeWord: Boolean,
    showReplace: Boolean,
    onToggleCaseSensitive: () -> Unit,
    onToggleWholeWord: () -> Unit,
    onToggleShowReplace: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onReplaceOne: () -> Unit,
    onReplaceAll: () -> Unit,
    onClose: () -> Unit,
) {
    val underlineColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
    ) {
        // Find row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close_search))
            }

            TextField(
                value = findQuery,
                onValueChange = onFindQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.find_placeholder), style = MaterialTheme.typography.bodyMedium) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = underlineColors,
            )

            Spacer(modifier = Modifier.width(8.dp))

            val counterText = if (matchCount == 0) "0" else "${currentIndex + 1}/$matchCount"
            Text(
                text = if (limitReached) "$counterText+" else counterText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            IconButton(onClick = onPrev, enabled = matchCount > 0) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.previous_match))
            }
            IconButton(onClick = onNext, enabled = matchCount > 0) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.next_match))
            }
        }

        // Options row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val activeColor = MaterialTheme.colorScheme.onSurface
            val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

            TextButton(onClick = onToggleCaseSensitive) {
                Text(
                    "Aa",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (caseSensitive) FontWeight.Bold else FontWeight.Normal,
                    color = if (caseSensitive) activeColor else inactiveColor,
                )
            }
            TextButton(onClick = onToggleWholeWord) {
                Text(
                    "W",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (wholeWord) FontWeight.Bold else FontWeight.Normal,
                    color = if (wholeWord) activeColor else inactiveColor,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onToggleShowReplace) {
                Text(
                    stringResource(if (showReplace) R.string.hide_replace else R.string.show_replace),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        // Replace row
        if (showReplace) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
            ) {
                TextField(
                    value = replaceQuery,
                    onValueChange = onReplaceQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.replace_placeholder), style = MaterialTheme.typography.bodyMedium) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = underlineColors,
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onReplaceOne, enabled = matchCount > 0) {
                    Text(stringResource(R.string.replace_one), style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = onReplaceAll, enabled = matchCount > 0) {
                    Text(stringResource(R.string.replace_all), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        if (limitReached) {
            Text(
                text = stringResource(R.string.match_limit_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 48.dp, vertical = 2.dp),
            )
        }
    }
}
