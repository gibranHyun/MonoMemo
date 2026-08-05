package com.monomemo.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monomemo.app.MonoMemoApp
import com.monomemo.app.R
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
    val content by viewModel.content.collectAsState()
    val editorMode by viewModel.editorMode.collectAsState()
    val findResult by viewModel.findResult.collectAsState()
    val currentMatchIndex by viewModel.currentMatchIndex.collectAsState()
    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val app = LocalContext.current.applicationContext as MonoMemoApp
    val lineNumbersEnabled by app.settingsDataStore.lineNumbersEnabled.collectAsState(initial = false)
    val fontSizeSp by app.settingsDataStore.fontSizeSp.collectAsState(initial = 16)
    val wrapEnabled by app.settingsDataStore.wrapEnabled.collectAsState(initial = true)

    // 본문·줄번호 공통: D2Coding 기본 행간이 넓어 세로로 늘어져 보이므로 폰트 상하 여백(includeFontPadding)을
    // 제거하고 줄 간격을 1.1배로 압축. Trim.None + 중앙정렬로 각 줄이 동일 높이를 차지해 줄번호와 정확히 정렬됨.
    val editorLineHeight = (fontSizeSp * 1.05f).sp
    // 자간 축소: D2Coding 고정폭 셀이 넓어 가로로 벌어져 보이므로 글자 폭의 5%만큼 좁혀 세로 강조를 완화
    val editorLetterSpacing = (fontSizeSp * -0.05f).sp
    val editorPlatformStyle = remember { PlatformTextStyle(includeFontPadding = false) }
    val editorLineHeightStyle = remember {
        LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None,
        )
    }

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
        // Editor body
        Box(modifier = Modifier.weight(1f)) {
            val hScrollState = rememberScrollState()
            val lineNumWidth = if (lineNumbersEnabled) (lineCount.toString().length * 10 + 16).dp else 0.dp
            // Editor with scrollable content (always visible)
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val availableWidth = maxWidth
                val availableHeight = maxHeight
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp)
                        .verticalScroll(scrollState)
                        .then(if (!wrapEnabled) Modifier.horizontalScroll(hScrollState) else Modifier),
                ) {
                    // Line numbers
                    if (lineNumbersEnabled) {
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
                                        fontSize = fontSizeSp.sp,
                                        fontFamily = D2CodingFamily,
                                        textAlign = TextAlign.End,
                                        lineHeight = editorLineHeight,
                                        platformStyle = editorPlatformStyle,
                                        lineHeightStyle = editorLineHeightStyle,
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
                            .defaultMinSize(minHeight = availableHeight)
                            .then(
                                if (wrapEnabled)
                                    Modifier.weight(1f)
                                else
                                    Modifier.defaultMinSize(minWidth = availableWidth - lineNumWidth)
                            )
                            .padding(horizontal = if (lineNumbersEnabled) 4.dp else 16.dp),
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = fontSizeSp.sp,
                            fontFamily = D2CodingFamily,
                            letterSpacing = editorLetterSpacing,
                            lineHeight = editorLineHeight,
                            platformStyle = editorPlatformStyle,
                            lineHeightStyle = editorLineHeightStyle,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        visualTransformation = highlightTransformation,
                        onTextLayout = { textLayoutResult = it },
                    )
                }
            }

            // Empty state overlay (non-interactive, behind cursor)
            if (content.text.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PencilEmptyState(stringResource(R.string.start_writing))
                }
            }
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
            TextButton(
                onClick = { viewModel.insertTab() },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(
                    stringResource(R.string.tab_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Box(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.line_count, lineCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Canvas(modifier = Modifier.padding(horizontal = 6.dp).size(4.dp)) {
                    drawCircle(AccentOrange)
                }
                Text(
                    text = stringResource(R.string.char_count, charCount),
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
