// PHASE 6: Updated to use entities from :common module

package com.cascadesim.core.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cascadesim.common.entity.CountryEntity
import com.cascadesim.common.entity.EventEntity
import com.cascadesim.common.entity.NpcEntity
import com.cascadesim.core.db.converters.JsonConverters
import com.cascadesim.core.db.dao.WorldDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Room database singleton for CascadeSim.
 * Stores all persistent world state including countries, NPCs, and events.
 */
@Database(
    entities = [
        CountryEntity::class,
        NpcEntity::class,
        EventEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(JsonConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun worldDao(): WorldDao

    companion object {
        private const val DATABASE_NAME = "cascadesim_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Hilt module providing database dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideWorldDao(database: AppDatabase): WorldDao {
        return database.worldDao()
    }

    @Provides
    @Singleton
    fun provideJsonConverters(): JsonConverters {
        return JsonConverters()
    }
}
