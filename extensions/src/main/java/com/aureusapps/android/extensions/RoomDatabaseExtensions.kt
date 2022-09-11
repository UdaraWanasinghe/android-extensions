package com.aureusapps.android.extensions

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

fun RoomDatabase.resetDatabase(tables: List<String>? = null): Boolean {
    val db = openHelper.writableDatabase
    val tableNames = db.getTableNames()
    val hasForeignKeys = db.hasForeignKeys(tables ?: tableNames.minus("sqlite_sequence"))
    val supportsDeferForeignKeys = db.supportsDeferForeignKeys()

    return try {
        if (hasForeignKeys && !supportsDeferForeignKeys) {
            // clear enforcement of foreign key constraints.
            db.execSQL("PRAGMA foreign_keys = FALSE")
        }
        db.beginTransaction()
        if (hasForeignKeys && supportsDeferForeignKeys) {
            // enforce foreign key constraints after outermost transaction is committed.
            db.execSQL("PRAGMA defer_foreign_keys = TRUE")
        }

        // clear all tables including sqlite_sequence table.
        // deleting sqlite_sequence table is required to reset autoincrement value.
        val tablesToClear = tables?.let {
            if (tableNames.contains("sqlite_sequence")) {
                it.plus("sqlite_sequence")
            } else {
                it
            }
        } ?: tableNames
        for (tableName in tablesToClear) {
            db.execSQL("DELETE FROM $tableName")
        }

        db.setTransactionSuccessful()
        true
    } catch (e: Exception) {
        false
    } finally {
        db.endTransaction()
        if (hasForeignKeys && !supportsDeferForeignKeys) {
            // restore enforcement of foreign key constraints.
            db.execSQL("PRAGMA foreign_keys = TRUE")
        }
        // blocks until there is no database writer and all are reading from the most recent database snapshot.
        db.query("PRAGMA wal_checkpoint(FULL)").close()
        if (!db.inTransaction()) {
            db.execSQL("VACUUM")
        }
    }

}

fun SupportSQLiteDatabase.getTableNames(
    exclude: List<String> = listOf("android_metadata", "room_master_table")
): List<String> {
    val cursor = query("SELECT DISTINCT tbl_name FROM sqlite_master WHERE type='table'")
    val tables = mutableListOf<String>()
    while (cursor.moveToNext()) {
        tables.add(cursor.getString(0))
    }
    cursor.close()
    tables.removeAll(exclude)
    return tables
}

fun SupportSQLiteDatabase.hasForeignKeys(tables: List<String>? = null): Boolean {
    val tableNames = tables ?: getTableNames(exclude = listOf("android_metadata", "room_master_table", "sqlite_sequence"))
    for (tableName in tableNames) {
        val cursor = query("PRAGMA foreign_key_list($tableName)")
        if (cursor.count > 0) {
            cursor.close()
            return true
        }
        cursor.close()
    }
    return false
}

fun SupportSQLiteDatabase.supportsDeferForeignKeys(): Boolean {
    // defer_foreign_keys is only supported on API 21+
    // Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    val cursor = query("PRAGMA defer_foreign_keys")
    return cursor.use { it.count > 0 }
}