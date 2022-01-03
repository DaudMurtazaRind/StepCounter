package com.example.myapplication.database

import android.content.Context
import androidx.room.Room


class DatabaseClient private constructor(mCtx: Context) {
    private val mCtx: Context

    val appDatabase: AppDatabase

    companion object {
        private var mInstance: DatabaseClient? = null
        @Synchronized
        fun getInstance(mCtx: Context): DatabaseClient {
            if (mInstance == null) {
                mInstance = DatabaseClient(mCtx)
            }
            return mInstance as DatabaseClient
        }
    }

    init {
        this.mCtx = mCtx
        appDatabase = Room.databaseBuilder(mCtx, AppDatabase::class.java, "MyStepsHistory").build()
    }
}
