package com.aj.flourish

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * This is the main database class for the Flourish application, using Room Persistence Library.
 * It defines the entities that will be stored in the database and provides access
 * to the Data Access Objects (DAOs) for interacting with those entities.
 */


@Database(
    // Defines the list of entities (tables) in the databases.
    entities = [Category::class, Expense::class],
    version = 1,

)
abstract class AppDatabase : RoomDatabase() {
    // Abstract methods to access the DAOs (Data Access Objects)
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        // Volatile ensures visibility of changes to INSTANCE across threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Singleton pattern to ensure only one instance of the database exists
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Prevents memory leaks
                    AppDatabase::class.java,   // The database class
                    "flourish_database"   // Name of the DB file
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
