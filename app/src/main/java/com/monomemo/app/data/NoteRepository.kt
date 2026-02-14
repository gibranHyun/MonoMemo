package com.monomemo.app.data

import com.monomemo.app.data.db.NoteDao
import com.monomemo.app.data.db.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val dao: NoteDao) {

    fun getActiveNotes(): Flow<List<NoteEntity>> = dao.getActiveNotes()

    fun getTrashNotes(): Flow<List<NoteEntity>> = dao.getTrashNotes()

    suspend fun getById(id: Long): NoteEntity? = dao.getById(id)

    fun getByIdFlow(id: Long): Flow<NoteEntity?> = dao.getByIdFlow(id)

    suspend fun insert(note: NoteEntity): Long = dao.insert(note)

    suspend fun update(note: NoteEntity) = dao.update(note)

    suspend fun softDelete(id: Long) = dao.softDelete(id)

    suspend fun restore(id: Long) = dao.restore(id)

    suspend fun deletePermanently(id: Long) = dao.deletepermanently(id)

    suspend fun deleteOlderThan(threshold: Long): Int = dao.deleteOlderThan(threshold)

    suspend fun getActiveCount(): Int = dao.getActiveCount()
}
