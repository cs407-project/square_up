package com.cs407.square_up

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "users")
data class User (
    @PrimaryKey(autoGenerate = true)
    val userID: Int = 0,
    val userName: String
)
