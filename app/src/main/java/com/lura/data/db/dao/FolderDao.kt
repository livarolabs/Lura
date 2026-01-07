package com.lura.data.db.dao

import androidx.room.*
import com.lura.data.db.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY isDefault DESC, name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>
    
    @Query("SELECT * FROM folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: String): FolderEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>)
    
    @Update
    suspend fun updateFolder(folder: FolderEntity)
    
    @Delete
    suspend fun deleteFolder(folder: FolderEntity)
    
    @Query("DELETE FROM folders WHERE id = :folderId AND isDefault = 0")
    suspend fun deleteFolderById(folderId: String)
    
    @Query("SELECT COUNT(*) FROM books WHERE folderId = :folderId")
    suspend fun getBookCountInFolder(folderId: String): Int
}
