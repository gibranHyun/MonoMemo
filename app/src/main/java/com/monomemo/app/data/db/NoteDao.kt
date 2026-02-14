package com.monomemo.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE deletedAt IS NULL ORDER BY updatedAt DESC")
    fun getActiveNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun getTrashNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<NoteEntity?>

    @Query("UPDATE notes SET title = :title, content = :content, titleManuallyEdited = :titleManuallyEdited, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun updateContent(id: Long, title: String, content: String, titleManuallyEdited: Boolean, updatedAt: Long)

    @Query("UPDATE notes SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deletepermanently(id: Long)

    @Query("DELETE FROM notes WHERE deletedAt IS NOT NULL AND deletedAt < :threshold")
    suspend fun deleteOlderThan(threshold: Long): Int

    @Query("SELECT COUNT(*) FROM notes WHERE deletedAt IS NULL")
    suspend fun getActiveCount(): Int
}
