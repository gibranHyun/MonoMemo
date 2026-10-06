package com.monomemo.app.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.monomemo.app.data.NoteRepository
import com.monomemo.app.data.db.NoteEntity
import com.monomemo.app.domain.find.FindEngine
import com.monomemo.app.domain.find.FindOptions
import com.monomemo.app.domain.find.FindResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class EditorMode { Normal, Search }

class EditorViewModel(
    private val noteRepository: NoteRepository,
    private val defaultTitle: String = "New memo",
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _content = MutableStateFlow(TextFieldValue(""))
    val content: StateFlow<TextFieldValue> = _content

    private val _titleManuallyEdited = MutableStateFlow(false)
    val titleManuallyEdited: StateFlow<Boolean> = _titleManuallyEdited

    // Search state
    private val _editorMode = MutableStateFlow(EditorMode.Normal)
    val editorMode: StateFlow<EditorMode> = _editorMode

    private val _findQuery = MutableStateFlow("")
    val findQuery: StateFlow<String> = _findQuery

    private val _findOptions = MutableStateFlow(FindOptions())
    val findOptions: StateFlow<FindOptions> = _findOptions

    private val _findResult = MutableStateFlow(FindResult(emptyList(), false))
    val findResult: StateFlow<FindResult> = _findResult

    private val _currentMatchIndex = MutableStateFlow(-1)
    val currentMatchIndex: StateFlow<Int> = _currentMatchIndex

    private val _replaceQuery = MutableStateFlow("")
    val replaceQuery: StateFlow<String> = _replaceQuery

    private val _showReplace = MutableStateFlow(false)
    val showReplace: StateFlow<Boolean> = _showReplace

    // Replace All undo
    private val _undoContent = MutableStateFlow<String?>(null)
    val undoContent: StateFlow<String?> = _undoContent

    private val _replaceAllCount = MutableStateFlow(0)
    val replaceAllCount: StateFlow<Int> = _replaceAllCount

    // Undo/Redo
    private val undoStack = ArrayDeque<String>()
    private val redoStack = ArrayDeque<String>()
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo
    private var lastSnapshotText: String = ""
    private var snapshotJob: Job? = null

    private var currentNoteId: Long? = null
    private var saveJob: Job? = null
    private var findJob: Job? = null
    private var currentNote: NoteEntity? = null

    fun loadNote(noteId: Long) {
        if (noteId == currentNoteId) return
        flushSave()
        exitSearch()
        currentNoteId = noteId
        viewModelScope.launch {
            val note = noteRepository.getById(noteId) ?: return@launch
            currentNote = note
            _title.value = note.title
            _content.value = TextFieldValue(note.content)
            _titleManuallyEdited.value = note.titleManuallyEdited
            undoStack.clear()
            redoStack.clear()
            lastSnapshotText = note.content
            _canUndo.value = false
            _canRedo.value = false
        }
    }

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
        if (!_titleManuallyEdited.value) {
            _titleManuallyEdited.value = true
        }
        scheduleSave()
    }

    fun onContentChange(newContent: TextFieldValue) {
        _content.value = newContent
        if (!_titleManuallyEdited.value) {
            updateAutoTitle(newContent.text)
        }
        scheduleSnapshot(newContent.text)
        scheduleSave()
        if (_editorMode.value == EditorMode.Search) {
            scheduleFind()
        }
    }

    // Search mode
    fun enterSearch() {
        _editorMode.value = EditorMode.Search
    }

    fun exitSearch() {
        _editorMode.value = EditorMode.Normal
        _findQuery.value = ""
        _replaceQuery.value = ""
        _findResult.value = FindResult(emptyList(), false)
        _currentMatchIndex.value = -1
        _showReplace.value = false
        findJob?.cancel()
    }

    fun onFindQueryChange(query: String) {
        _findQuery.value = query
        scheduleFind()
    }

    fun onReplaceQueryChange(query: String) {
        _replaceQuery.value = query
    }

    fun toggleCaseSensitive() {
        _findOptions.value = _findOptions.value.copy(
            caseSensitive = !_findOptions.value.caseSensitive,
        )
        scheduleFind()
    }

    fun toggleWholeWord() {
        _findOptions.value = _findOptions.value.copy(
            wholeWord = !_findOptions.value.wholeWord,
        )
        scheduleFind()
    }

    fun toggleShowReplace() {
        _showReplace.value = !_showReplace.value
    }

    fun nextMatch() {
        val matches = _findResult.value.matches
        if (matches.isEmpty()) return
        _currentMatchIndex.value = (_currentMatchIndex.value + 1) % matches.size
    }

    fun prevMatch() {
        val matches = _findResult.value.matches
        if (matches.isEmpty()) return
        _currentMatchIndex.value = if (_currentMatchIndex.value <= 0) {
            matches.size - 1
        } else {
            _currentMatchIndex.value - 1
        }
    }

    fun replaceOne() {
        val matches = _findResult.value.matches
        val idx = _currentMatchIndex.value
        if (idx < 0 || idx >= matches.size) return

        val range = matches[idx]
        val text = _content.value.text
        // matches는 최대 FIND_DEBOUNCE_MS 만큼 지연되어 재계산되므로, 편집 직후 바로
        // 치환을 누르면 range가 현재 text 길이를 벗어난 stale 값일 수 있다. 그 경우
        // 크래시 대신 최신 상태로 다시 찾기만 수행한다.
        if (range.first < 0 || range.last >= text.length) {
            viewModelScope.launch { performFind() }
            return
        }
        val newText = text.substring(0, range.first) + _replaceQuery.value + text.substring(range.last + 1)
        val cursor = (range.first + _replaceQuery.value.length).coerceAtMost(newText.length)
        _content.value = TextFieldValue(newText, TextRange(cursor))
        if (!_titleManuallyEdited.value) updateAutoTitle(newText)
        scheduleSave()
        viewModelScope.launch { performFind() }
    }

    fun replaceAll() {
        val matches = _findResult.value.matches
        if (matches.isEmpty()) return

        val oldText = _content.value.text
        // replaceOne()과 동일한 이유로, stale한 match 범위가 현재 텍스트를 벗어나면
        // 치환을 건너뛰고 다시 찾기만 수행한다.
        if (matches.any { it.first < 0 || it.last >= oldText.length }) {
            viewModelScope.launch { performFind() }
            return
        }
        _undoContent.value = oldText
        _replaceAllCount.value = matches.size

        val sb = StringBuilder(oldText)
        val replacement = _replaceQuery.value
        // 뒤에서부터 치환하여 인덱스 유지
        for (i in matches.indices.reversed()) {
            val range = matches[i]
            sb.replace(range.first, range.last + 1, replacement)
        }

        val newText = sb.toString()
        val cursor = _content.value.selection.start.coerceAtMost(newText.length)
        _content.value = TextFieldValue(newText, TextRange(cursor))
        if (!_titleManuallyEdited.value) updateAutoTitle(newText)
        scheduleSave()
        viewModelScope.launch { performFind() }
    }

    fun undoReplaceAll() {
        val oldText = _undoContent.value ?: return
        val cursor = _content.value.selection.start.coerceAtMost(oldText.length)
        _content.value = TextFieldValue(oldText, TextRange(cursor))
        if (!_titleManuallyEdited.value) updateAutoTitle(oldText)
        _undoContent.value = null
        _replaceAllCount.value = 0
        scheduleSave()
        viewModelScope.launch { performFind() }
    }

    fun dismissUndo() {
        _undoContent.value = null
        _replaceAllCount.value = 0
    }

    // Undo/Redo
    private fun scheduleSnapshot(newText: String) {
        snapshotJob?.cancel()
        snapshotJob = viewModelScope.launch {
            delay(SNAPSHOT_DEBOUNCE_MS)
            if (newText != lastSnapshotText) {
                undoStack.addLast(lastSnapshotText)
                if (undoStack.size > MAX_UNDO) undoStack.removeFirst()
                redoStack.clear()
                lastSnapshotText = newText
                _canUndo.value = undoStack.isNotEmpty()
                _canRedo.value = false
            }
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        // flush pending snapshot
        snapshotJob?.cancel()
        val currentText = _content.value.text
        if (currentText != lastSnapshotText) {
            redoStack.addLast(currentText)
        }
        val prev = undoStack.removeLast()
        redoStack.addLast(lastSnapshotText)
        lastSnapshotText = prev
        val cursor = _content.value.selection.start.coerceAtMost(prev.length)
        _content.value = TextFieldValue(prev, TextRange(cursor))
        if (!_titleManuallyEdited.value) updateAutoTitle(prev)
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
        scheduleSave()
        if (_editorMode.value == EditorMode.Search) viewModelScope.launch { performFind() }
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        snapshotJob?.cancel()
        undoStack.addLast(lastSnapshotText)
        val next = redoStack.removeLast()
        lastSnapshotText = next
        val cursor = _content.value.selection.start.coerceAtMost(next.length)
        _content.value = TextFieldValue(next, TextRange(cursor))
        if (!_titleManuallyEdited.value) updateAutoTitle(next)
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
        scheduleSave()
        if (_editorMode.value == EditorMode.Search) viewModelScope.launch { performFind() }
    }

    // Tab insertion
    fun insertTab() {
        val current = _content.value
        val sel = current.selection
        val text = current.text
        val newText = text.substring(0, sel.start) + "    " + text.substring(sel.end)
        val newSelection = androidx.compose.ui.text.TextRange(sel.start + 4)
        _content.value = TextFieldValue(newText, newSelection)
        if (!_titleManuallyEdited.value) updateAutoTitle(newText)
        scheduleSnapshot(newText)
        scheduleSave()
        if (_editorMode.value == EditorMode.Search) scheduleFind()
    }

    private fun scheduleFind() {
        findJob?.cancel()
        findJob = viewModelScope.launch {
            delay(FIND_DEBOUNCE_MS)
            performFind()
        }
    }

    // FindEngine.findMatches는 문서 전체를 훑는 연산이라, 긴 노트에서 메인 스레드를
    // 막지 않도록 Dispatchers.Default에서 실행한다.
    private suspend fun performFind() {
        val result = withContext(Dispatchers.Default) {
            FindEngine.findMatches(
                text = _content.value.text,
                query = _findQuery.value,
                options = _findOptions.value,
            )
        }
        _findResult.value = result
        if (result.matches.isEmpty()) {
            _currentMatchIndex.value = -1
        } else {
            _currentMatchIndex.value = _currentMatchIndex.value.coerceIn(0, result.matches.size - 1)
        }
    }

    private fun updateAutoTitle(content: String) {
        val firstLine = content.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() }
            ?: defaultTitle
        _title.value = if (firstLine.length > 40) firstLine.take(40) + "…" else firstLine
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(SAVE_DEBOUNCE_MS)
            performSave()
        }
    }

    // AppScaffold가 ON_STOP 시점에 호출해 백그라운드 전환 직전 편집 내용을 즉시 저장한다.
    internal fun flushSave() {
        saveJob?.cancel()
        saveJob = null
        val note = currentNote ?: return
        viewModelScope.launch {
            noteRepository.updateContent(
                id = note.id,
                title = _title.value,
                content = _content.value.text,
                titleManuallyEdited = _titleManuallyEdited.value,
            )
        }
    }

    private suspend fun performSave() {
        val note = currentNote ?: return
        noteRepository.updateContent(
            id = note.id,
            title = _title.value,
            content = _content.value.text,
            titleManuallyEdited = _titleManuallyEdited.value,
        )
    }

    override fun onCleared() {
        super.onCleared()
        flushSave()
    }

    class Factory(
        private val noteRepository: NoteRepository,
        private val defaultTitle: String = "New memo",
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditorViewModel(noteRepository, defaultTitle) as T
        }
    }

    companion object {
        private const val SAVE_DEBOUNCE_MS = 800L
        private const val FIND_DEBOUNCE_MS = 200L
        private const val SNAPSHOT_DEBOUNCE_MS = 500L
        private const val MAX_UNDO = 50
    }
}
