package com.hlwdy.bjut

import android.content.Context
import android.content.SharedPreferences

class network_account_util(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        const val PREF_NAME = "AppNetworkAccount"
        const val KEY_HAVEDATA = "havedata"
        const val KEY_USERNAME = "username"
        const val KEY_PASSWORD = "userpwd"
    }

    fun haveData(): Boolean {
        return prefs.getBoolean(KEY_HAVEDATA, false)
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USERNAME, "")
    }

    fun getUserPwd(): String? {
        return prefs.getString(KEY_PASSWORD, "")
    }

    fun editUserData(username:String,userpwd:String){
        editor.putBoolean(KEY_HAVEDATA, true)
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_PASSWORD, userpwd)
        editor.commit()
    }


}