package com.aureusapps.android.extensions.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val email: String,
)