package com.aureusapps.android.extensions.test.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {

    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Insert
    fun insertAll(users: List<User>): List<Long>

    @Query("DELETE FROM user")
    fun deleteAll(): Int

}