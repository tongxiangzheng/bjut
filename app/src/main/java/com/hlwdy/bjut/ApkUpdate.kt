package com.hlwdy.bjut

import okhttp3.Callback

class ApkUpdate {
    fun getLatest(callback: Callback){
        HttpUtils()
            .get("https://api.github.com/repos/bjutapp/bjut/releases/latest",true,callback)
    }
}