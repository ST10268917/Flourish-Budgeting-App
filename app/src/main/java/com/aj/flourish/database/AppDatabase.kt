package com.aj.flourish.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aj.flourish.database.dao.CategoryDao
import com.aj.flourish.database.dao.ExpenseDao
import com.aj.flourish.database.entities.Category
import com.aj.flourish.database.entities.Expense

@Database(
    entities = [Category::class, Expense::class],
    version = 1,

)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flourish_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
