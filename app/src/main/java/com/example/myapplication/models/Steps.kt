package com.example.myapplication.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "StepsTable")
data class Steps(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "steps")
    var steps: Int,
    @ColumnInfo(name = "date")
    var date: String
)