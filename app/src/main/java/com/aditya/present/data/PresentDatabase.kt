package com.aditya.present.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 3,
    exportSchema = false,
)
abstract class PresentDatabase : RoomDatabase() {
    abstract fun dao(): PresentDao

    companion object {
        @Volatile
        private var INSTANCE: PresentDatabase? = null

        // Migration v2 → v3: add index on attendance.slotId for faster slot-based queries
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_slotId` ON `attendance` (`slotId`)")
            }
        }

        fun get(context: Context): PresentDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PresentDatabase::class.java,
                    "present.db",
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build().also { INSTANCE = it }
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PresentDatabase =
        PresentDatabase.get(context)

    @Provides
    fun provideDao(db: PresentDatabase): PresentDao = db.dao()
}
