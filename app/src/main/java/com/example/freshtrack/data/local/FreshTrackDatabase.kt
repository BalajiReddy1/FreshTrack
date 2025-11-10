package com.example.freshtrack.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.freshtrack.data.local.dao.CategoryDao
import com.example.freshtrack.data.local.dao.ProductDao
import com.example.freshtrack.data.local.entities.CategoryEntity
import com.example.freshtrack.data.local.entities.DefaultCategories
import com.example.freshtrack.data.local.entities.ProductEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Room Database for FreshTrack
 * Manages local data persistence with automatic migration support
 */
@Database(
    entities = [ProductEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class FreshTrackDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: FreshTrackDatabase? = null

        private const val DATABASE_NAME = "freshtrack_database"

        fun getInstance(context: Context): FreshTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FreshTrackDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration() // For development; remove in production
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * Callback to populate database with default categories on first creation
         */
        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // Populate default categories when database is created
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultCategories(database.categoryDao())
                    }
                }
            }
        }

        /**
         * Insert default categories into the database
         */
        private suspend fun populateDefaultCategories(categoryDao: CategoryDao) {
            val defaultCategories = DefaultCategories.getAll()
            categoryDao.insertCategories(defaultCategories)
        }
    }
}

/**
 * Extension function to provide database instance
 * Makes it easier to access database in dependency injection
 */
fun Context.getFreshTrackDatabase(): FreshTrackDatabase {
    return FreshTrackDatabase.getInstance(this)
}