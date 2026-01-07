package com.lura.di

import android.content.Context
import androidx.room.Room
import com.lura.data.db.AppDatabase
import com.lura.data.db.dao.BookDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lura_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideBookDao(db: AppDatabase): BookDao {
        return db.bookDao()
    }

    @Provides
    @Singleton
    fun provideHighlightDao(db: AppDatabase): com.lura.data.db.dao.HighlightDao {
        return db.highlightDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @dagger.Binds
    @Singleton
    abstract fun bindLibraryRepository(
        impl: com.lura.data.repository.LibraryRepositoryImpl
    ): com.lura.domain.repository.LibraryRepository

    @dagger.Binds
    @Singleton
    abstract fun bindEpubParser(
        impl: com.lura.data.engine.RealEpubParser
    ): com.lura.domain.engine.EpubParser
}
