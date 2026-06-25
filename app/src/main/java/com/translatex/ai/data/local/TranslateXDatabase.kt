package com.translatex.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.translatex.ai.data.local.dao.TranslationDao
import com.translatex.ai.data.local.entity.TranslationEntity

@Database(
    entities = [TranslationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TranslateXDatabase : RoomDatabase() {
    abstract fun translationDao(): TranslationDao
}
