package com.hlwdy.bjut

import android.content.Context
import android.content.SharedPreferences

class account_session_util(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        const val PREF_NAME = "AppPrefs"
        const val IS_LOGIN = "IsLogin"
        const val KEY_USERNAME = "usname"
        const val KEY_NAME = "name"
        const val KEY_TK = "ticket"
        const val KEY_SESS = "ses"
        const val KEY_LGTIME = "lgtime"
        const val KEY_WEBVPNTK="webvpn"
        const val KEY_WEBVPNTKTIME="webvpntktime"
        const val KEY_CARDID="cardid"
        const val KEY_OTPDATA="otpdata"
    }

    fun createLoginSession(usname: String,name: String,tk: String,ses: String) {
        editor.putBoolean(IS_LOGIN, true)
        editor.putString(KEY_USERNAME, usname)
        editor.putString(KEY_NAME, name)
        editor.putString(KEY_SESS, ses)
        editor.putString(KEY_TK, tk)
        editor.putString(KEY_LGTIME, System.currentTimeMillis().toString())
        editor.commit()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGIN, false)
    }

    fun getUserDetails(): HashMap<String, String?> {
        val user = HashMap<String, String?>()
        user[KEY_USERNAME] = prefs.getString(KEY_USERNAME, null)
        user[KEY_NAME] = prefs.getString(KEY_NAME, null)
        user[KEY_SESS] = prefs.getString(KEY_SESS, null)
        user[KEY_TK] = prefs.getString(KEY_TK, null)
        user[KEY_LGTIME] = prefs.getString(KEY_LGTIME, null)
        user[KEY_WEBVPNTK] = prefs.getString(KEY_WEBVPNTK, null)
        user[KEY_WEBVPNTKTIME] = prefs.getString(KEY_WEBVPNTKTIME, "0")
        user[KEY_CARDID] = prefs.getString(KEY_CARDID, null)
        user[KEY_OTPDATA] = prefs.getString(KEY_OTPDATA, null)
        return user
    }

    fun logoutUser() {
        editor.clear()
        editor.commit()
    }

    fun editWebVpnTK(tk: String,time:String) {
        editor.putString(KEY_WEBVPNTK, tk)
        editor.putString(KEY_WEBVPNTKTIME, time)
        editor.commit()
    }

    fun editCardID(id: String) {
        editor.putString(KEY_CARDID, id)
        editor.commit()
    }

    fun editOTPData(data: String) {
        editor.putString(KEY_OTPDATA, data)
        editor.commit()
    }
}