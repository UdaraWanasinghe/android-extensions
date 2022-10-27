package com.aureusapps.android.extensions.test.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [User::class, Phone::class],
    version = 1,
    exportSchema = false
)
abstract class UserDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun phoneDao(): PhoneDao

    companion object {

        fun getInstance(context: Context): UserDatabase {
            return Room.databaseBuilder(
                context,
                UserDatabase::class.java,
                "user_database"
            )
                .fallbackToDestructiveMigration()
                .build()
        }

    }

}