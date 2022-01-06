package com.example.myapplication.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.models.Profile
import com.example.myapplication.models.Steps

@Dao
interface StepsDao {
    @Query("SELECT * FROM StepsTable")
    fun getAll(): LiveData<MutableList<Steps>>

    @Insert
    fun insert(steps: Steps)

    @Delete
    fun delete(steps: Steps)

    @Query("UPDATE StepsTable SET steps = :steps WHERE date =:date")
    fun update(steps: Int, date: String)

    @Query("SELECT * from StepsTable WHERE date =:date LIMIT 1")
    fun getSingleStepsRecord(date: String): Steps?

    @Insert
    fun insertProfile(profile: Profile)

    @Query("SELECT * from Profile WHERE name =:name LIMIT 1")
    fun getProfile(name: String): Profile?



}