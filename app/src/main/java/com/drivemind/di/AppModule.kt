package com.drivemind.di

import android.content.Context
import androidx.room.Room
import com.drivemind.data.local.AppDatabase
import com.drivemind.data.local.dao.VoiceNoteDao
import com.drivemind.data.repository.VoiceNoteRepositoryImpl
import com.drivemind.domain.repository.VoiceNoteRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideVoiceNoteDao(db: AppDatabase): VoiceNoteDao = db.voiceNoteDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVoiceNoteRepository(
        impl: VoiceNoteRepositoryImpl
    ): VoiceNoteRepository
}
