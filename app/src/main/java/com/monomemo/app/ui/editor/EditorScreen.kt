package com.monomemo.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monomemo.app.MonoMemoApp
import com.monomemo.app.ui.theme.D2CodingFamily
import com.monomemo.app.ui.theme.HighlightAllDark
import com.monomemo.app.ui.theme.HighlightAllLight
import com.monomemo.app.ui.theme.HighlightCurrentDark
import com.monomemo.app.ui.theme.HighlightCurrentLight
import com.monomemo.app.ui.components.PencilEmptyState
import com.monomemo.app.ui.theme.AccentOrange

@Composable
fun EditorScreen(
    noteId: Long?,
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier,
) {
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val editorMode by viewModel.editorMode.collectAsState()
    val findResult by viewModel.findResult.collectAsState()
    val currentMatchIndex by viewModel.currentMatchIndex.collectAsState()
    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val app = LocalContext.current.applicationContext as MonoMemoApp
    val lineNumbersEnabled by app.settingsDataStore.lineNumbersEnabled.collectAsState(initial = false)

    LaunchedEffect(noteId) {
        noteId?.let { viewModel.loadNote(it) }
    }

    LaunchedEffect(currentMatchIndex, findResult.matches) {
        if (currentMatchIndex >= 0 && currentMatchIndex < findResult.matches.size) {
            val layout = textLayoutResult ?: return@LaunchedEffect
            val matchRange = findResult.matches[currentMatchIndex]
            val rect = layout.getBoundingBox(matchRange.first.coerceAtMost(layout.layoutInput.text.length - 1))
            val targetY = (rect.top - scrollState.viewportSize / 2).toInt().coerceAtLeast(0)
            scrollState.animateScrollTo(targetY)
        }
    }

    if (noteId == null) {
        return
    }

    val isDark = isSystemInDarkTheme()
    val highlightAll = if (isDark) HighlightAllDark else HighlightAllLight
    val highlightCurrent = if (isDark) HighlightCurrentDark else HighlightCurrentLight

    val highlightTransformation = remember(findResult, currentMatchIndex, editorMode, isDark) {
        if (editorMode == EditorMode.Search && findResult.matches.isNotEmpty()) {
            HighlightVisualTransformation(findResult.matches, currentMatchIndex, highlightAll, highlightCurrent)
        } else {
            VisualTransformation.None
        }
    }

    val text = content.text
    val lineCount = if (text.isEmpty()) 1 else text.count { it == '\n' } + 1
    val charCount = text.length

    Column(modifier = modifier.fillMaxSize()) {
        // Title with accent left border
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )
            TextField(
                value = title,
                onValueChange = { viewModel.onTitleChange(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("제목") },
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
        }

        // Editor body
        Row(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
        ) {
            // Line numbers
            if (lineNumbersEnabled) {
                val lineNumWidth = (lineCount.toString().length * 10 + 16).dp
                Column(
                    modifier = Modifier
                        .width(lineNumWidth)
                        .padding(top = 0.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    for (i in 1..lineCount) {
                        Text(
                            text = "$i",
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                fontFamily = D2CodingFamily,
                                textAlign = TextAlign.End,
                            ),
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                }
            }

            // Text editor
            BasicTextField(
                value = content,
                onValueChange = { viewModel.onContentChange(it) },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(horizontal = if (lineNumbersEnabled) 4.dp else 16.dp),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontFamily = D2CodingFamily,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = highlightTransformation,
                onTextLayout = { textLayoutResult = it },
                decorationBox = { innerTextField ->
                    Box {
                        if (content.text.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                PencilEmptyState("메모를 시작하세요")
                            }
                        }
                        innerTextField()
                    }
                },
            )
        }

        // Bottom bar: tab button + counter (warm frame tint)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { viewModel.insertTab() }) {
                Text(
                    "TAB",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Box(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${lineCount}줄",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Canvas(modifier = Modifier.padding(horizontal = 6.dp).size(4.dp)) {
                    drawCircle(AccentOrange)
                }
                Text(
                    text = "${charCount}자",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private class HighlightVisualTransformation(
    private val matches: List<IntRange>,
    private val currentIndex: Int,
    private val colorAll: Color,
    private val colorCurrent: Color,
) : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): TransformedText {
        val annotated = buildAnnotatedString {
            append(text)
            matches.forEachIndexed { index, range ->
                val safeStart = range.first.coerceIn(0, text.length)
                val safeEnd = range.last.coerceIn(0, text.length) + 1
                if (safeStart < safeEnd) {
                    addStyle(
                        SpanStyle(
                            background = if (index == currentIndex) colorCurrent else colorAll,
                        ),
                        safeStart,
                        safeEnd.coerceAtMost(text.length),
                    )
                }
            }
        }
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}
