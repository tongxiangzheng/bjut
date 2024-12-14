package com.hlwdy.bjut

import android.content.Context
import android.content.SharedPreferences

class settings_util(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        const val PREF_NAME = "AppSettings"
        const val KEY_AUTOUPDATE = "autoupdate"
        const val KEY_UPDATEIGNORE = "updateignore"
        const val KEY_BIOENCRYPT = "bioencrypt"
    }

    fun getSettingBool(key: String): Boolean? {
        return if (prefs.contains(key)) {
            prefs.getBoolean(key, false)
        } else {
            null
        }
    }

    fun getSettingStr(key:String): String {
        return prefs.getString(key, "").toString()
    }

    fun editSettingBool(key:String,value:Boolean){
        editor.putBoolean(key, value)
        editor.commit()
    }

    fun editSettingStr(key:String,value:String){
        editor.putString(key, value)
        editor.commit()
    }

}