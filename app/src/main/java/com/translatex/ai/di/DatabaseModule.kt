package com.translatex.ai.di

import android.content.Context
import androidx.room.Room
import com.translatex.ai.data.local.TranslateXDatabase
import com.translatex.ai.data.local.dao.TranslationDao
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
    fun provideDatabase(@ApplicationContext ctx: Context): TranslateXDatabase =
        Room.databaseBuilder(ctx, TranslateXDatabase::class.java, "translatex_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTranslationDao(db: TranslateXDatabase): TranslationDao =
        db.translationDao()
}
