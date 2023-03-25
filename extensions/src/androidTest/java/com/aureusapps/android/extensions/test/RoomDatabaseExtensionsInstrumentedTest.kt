package com.aureusapps.android.extensions.test

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aureusapps.android.extensions.resetDatabase
import com.aureusapps.android.extensions.test.db.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDatabaseExtensionsInstrumentedTest {

    private lateinit var db: UserDatabase
    private lateinit var userDao: UserDao
    private lateinit var phoneDao: PhoneDao
    private lateinit var writableDatabase: SupportSQLiteDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = UserDatabase.getInstance(context)
        userDao = db.userDao()
        phoneDao = db.phoneDao()
        db.clearAllTables()
        writableDatabase = db.openHelper.writableDatabase
        writableDatabase.delete("sqlite_sequence", null, null)
    }

    @After
    fun tearDown() {
        db.close()
        writableDatabase.close()
    }

    @Test
    fun testResetDatabase() {
        userDao.insertAll(
            listOf(
                User(0, "John", "john@example.com"),
                User(0, "Sam", "sam@example.com")
            )
        )
        phoneDao.insertAll(
            listOf(
                Phone(0, 1, "1234567890"),
                Phone(0, 1, "2345678901"),
                Phone(0, 2, "0987654321"),
                Phone(0, 2, "9876543210")
            )
        )
        // check size after insert
        Assert.assertEquals(2, userDao.getAll().size)
        Assert.assertEquals(4, phoneDao.getAll().size)

        // test current sequence values
        testSequenceValue("User", 2)
        testSequenceValue("Phone", 4)

        // reset database
        db.resetDatabase()

        // test values after reset
        Assert.assertEquals(0, userDao.getAll().size)
        Assert.assertEquals(0, phoneDao.getAll().size)
        writableDatabase.query("SELECT seq FROM sqlite_sequence").use {
            Assert.assertEquals(0, it.count)
        }
    }

    private fun testSequenceValue(table: String, expectedValue: Int) {
        val cursor = writableDatabase.query("SELECT seq FROM sqlite_sequence WHERE name = '$table'")
        cursor.moveToFirst()
        val nextSeq = cursor.getInt(0)
        Assert.assertEquals(expectedValue, nextSeq)
        cursor.close()
    }

}