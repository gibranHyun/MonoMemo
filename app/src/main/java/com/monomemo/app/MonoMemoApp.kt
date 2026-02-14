package com.monomemo.app

import android.app.Application
import com.monomemo.app.data.NoteRepository
import com.monomemo.app.data.db.AppDatabase
import com.monomemo.app.data.settings.SettingsDataStore

class MonoMemoApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val noteRepository: NoteRepository by lazy { NoteRepository(database.noteDao()) }
    val settingsDataStore: SettingsDataStore by lazy { SettingsDataStore(this) }
}
