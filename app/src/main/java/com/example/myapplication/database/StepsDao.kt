package com.example.myapplication.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.models.Steps

@Dao
interface StepsDao {
    @Query("SELECT * FROM StepsTable")
    fun getAll(): LiveData<List<Steps>>

    @Insert
    fun insert(steps: Steps)

    @Delete
    fun delete(steps: Steps)
}