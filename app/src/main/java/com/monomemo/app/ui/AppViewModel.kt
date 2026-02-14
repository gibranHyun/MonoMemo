package com.monomemo.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.monomemo.app.data.NoteRepository
import com.monomemo.app.data.db.NoteEntity
import com.monomemo.app.data.settings.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen { Editor, Trash, Settings, About }

class AppViewModel(
    private val noteRepository: NoteRepository,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _currentNoteId = MutableStateFlow<Long?>(null)
    val currentNoteId: StateFlow<Long?> = _currentNoteId

    private val _currentScreen = MutableStateFlow(AppScreen.Editor)
    val currentScreen: StateFlow<AppScreen> = _currentScreen

    val activeNotes: StateFlow<List<NoteEntity>> = noteRepository.getActiveNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashNotes: StateFlow<List<NoteEntity>> = noteRepository.getTrashNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadInitialNote()
        cleanupOldTrash()
    }

    private fun loadInitialNote() {
        viewModelScope.launch {
            if (noteRepository.getActiveCount() == 0) {
                val id = noteRepository.insert(
                    NoteEntity(title = "새 메모", content = ""),
                )
                openNote(id)
                return@launch
            }

            val lastId = settingsDataStore.lastOpenedNoteId.first()
            if (lastId != null) {
                val note = noteRepository.getById(lastId)
                if (note != null && note.deletedAt == null) {
                    _currentNoteId.value = lastId
                    return@launch
                }
            }

            val notes = noteRepository.getActiveNotes().first()
            if (notes.isNotEmpty()) {
                openNote(notes.first().id)
            }
        }
    }

    private fun cleanupOldTrash() {
        viewModelScope.launch {
            val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            noteRepository.deleteOlderThan(thirtyDaysAgo)
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun openNote(id: Long) {
        _currentNoteId.value = id
        _currentScreen.value = AppScreen.Editor
        viewModelScope.launch {
            settingsDataStore.setLastOpenedNoteId(id)
        }
    }

    fun createNewNote() {
        viewModelScope.launch {
            val id = noteRepository.insert(
                NoteEntity(title = "새 메모", content = ""),
            )
            openNote(id)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            noteRepository.softDelete(id)
            if (_currentNoteId.value == id) {
                val notes = noteRepository.getActiveNotes().first()
                if (notes.isNotEmpty()) {
                    openNote(notes.first().id)
                } else {
                    createNewNote()
                }
            }
        }
    }

    fun restoreNote(id: Long) {
        viewModelScope.launch {
            noteRepository.restore(id)
        }
    }

    fun importNote(content: String) {
        viewModelScope.launch {
            val firstLine = content.lineSequence()
                .map { it.trim() }
                .firstOrNull { it.isNotEmpty() }
                ?: "가져온 메모"
            val title = if (firstLine.length > 40) firstLine.take(40) + "…" else firstLine
            val id = noteRepository.insert(
                NoteEntity(title = title, content = content),
            )
            openNote(id)
        }
    }

    fun deletePermanently(id: Long) {
        viewModelScope.launch {
            noteRepository.deletePermanently(id)
        }
    }

    class Factory(
        private val noteRepository: NoteRepository,
        private val settingsDataStore: SettingsDataStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppViewModel(noteRepository, settingsDataStore) as T
        }
    }
}
