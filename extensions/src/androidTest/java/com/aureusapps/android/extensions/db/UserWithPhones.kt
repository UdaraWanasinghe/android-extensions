package com.aureusapps.android.extensions.db

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithPhones(
    @Embedded
    val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val userId: Long,
)