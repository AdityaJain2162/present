package com.aditya.present.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(
    entities = [
        AcademicSessionEntity::class,
        SubjectEntity::class,
        ClassSlotEntity::class,
        AttendanceEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class PresentDatabase : RoomDatabase() {
    abstract fun dao(): PresentDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PresentDatabase =
        Room.databaseBuilder(context, PresentDatabase::class.java, "present.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDao(db: PresentDatabase): PresentDao = db.dao()
}
