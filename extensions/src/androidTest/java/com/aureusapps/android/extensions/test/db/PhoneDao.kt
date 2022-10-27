package com.aureusapps.android.extensions.test.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PhoneDao {

    @Query("SELECT * FROM phone")
    fun getAll(): List<Phone>

    @Insert
    fun insertAll(phones: List<Phone>): List<Long>

    @Query("DELETE FROM phone")
    fun deleteAll(): Int

}