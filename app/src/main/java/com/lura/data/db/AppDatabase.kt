package com.lura.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lura.data.db.dao.HighlightDao
import com.lura.data.db.dao.BookDao
import com.lura.data.db.dao.FolderDao
import com.lura.data.db.entity.Highlight
import com.lura.data.db.entity.BookEntity
import com.lura.data.db.entity.FolderEntity

@Database(
    entities = [BookEntity::class, Highlight::class, FolderEntity::class], 
    version = 3, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun highlightDao(): HighlightDao
    abstract fun folderDao(): FolderDao
}
