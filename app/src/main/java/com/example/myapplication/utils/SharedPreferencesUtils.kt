package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesUtils {
    private var context: Context? = null

    private var FILE_NAME = "share_date"

    constructor(FILE_NAME: String) {
        this.FILE_NAME = FILE_NAME
    }

    constructor(context: Context?) {
        this.context = context
    }

    fun setParam(key: String?, `object`: Any) {
        val type = `object`.javaClass.simpleName
        val sp = context!!.getSharedPreferences(
            FILE_NAME,
            Context.MODE_MULTI_PROCESS
        )
        val editor = sp.edit()
        if ("String" == type) {
            editor.putString(key, `object`.toString())
        } else if ("Integer" == type) {
            editor.putInt(key, (`object` as Int))
        } else if ("Boolean" == type) {
            editor.putBoolean(key, (`object` as Boolean))
        } else if ("Float" == type) {
            editor.putFloat(key, (`object` as Float))
        } else if ("Long" == type) {
            editor.putLong(key, (`object` as Long))
        }
        editor.commit()
    }

    fun getParam(key: String?, defaultObject: Any): Any? {
        val type = defaultObject.javaClass.simpleName
        val sp = context!!.getSharedPreferences(
            FILE_NAME,
            Context.MODE_PRIVATE
        )
        if ("String" == type) {
            return sp.getString(key, defaultObject as String)
        } else if ("Integer" == type) {
            return sp.getInt(key, (defaultObject as Int))
        } else if ("Boolean" == type) {
            return sp.getBoolean(key, (defaultObject as Boolean))
        } else if ("Float" == type) {
            return sp.getFloat(key, (defaultObject as Float))
        } else if ("Long" == type) {
            return sp.getLong(key, (defaultObject as Long))
        }
        return null
    }

    fun remove(key: String?) {
        val sp = context!!.getSharedPreferences(
            FILE_NAME,
            Context.MODE_PRIVATE
        )
        val editor = sp.edit()
        editor.remove(key)
        editor.commit()
    }

    fun clear() {
        val sp = context!!.getSharedPreferences(
            FILE_NAME,
            Context.MODE_PRIVATE
        )
        val editor = sp.edit()
        editor.clear()
        editor.commit()
    }
}