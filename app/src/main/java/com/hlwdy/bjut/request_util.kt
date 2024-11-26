package com.hlwdy.bjut

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class HttpUtils {
    private var params = mutableMapOf<String, String>()
    private var headers = mutableMapOf<String, String>()

    fun get(url: String,is_redirect:Boolean,callBack: Callback) {
        val okHttpClient = OkHttpClient().newBuilder().followRedirects(is_redirect).build()
        val request: Request = Request.Builder().apply {
            headers.forEach {
                addHeader(it.key, it.value)
            }
            url(url)
            get()
        }.build()
        okHttpClient.newCall(request).enqueue(callBack)
    }

    fun post(url: String, callBack: Callback) {
        val okHttpClient = OkHttpClient()
        val formBody: FormBody.Builder = FormBody.Builder()
        params.forEach(formBody::add)
        val request: Request = Request.Builder()
            .apply {
                headers.forEach {
                    addHeader(it.key, it.value)
                }
                url(url)
                post(formBody.build())
            }.build()
        okHttpClient.newCall(request).enqueue(callBack)
    }

    fun postJson(url: String, jsonString: String, callBack: Callback) {
        val okHttpClient = OkHttpClient()
        val stringBody = jsonString.toRequestBody("application/json;charset=utf-8".toMediaType())
        val request: Request = Request.Builder().apply {
            headers.forEach {
                addHeader(it.key, it.value)
            }
            url(url)
            post(stringBody)
        }.build()

        okHttpClient.newCall(request).enqueue(callBack)

    }

    fun addParam(key: String, value: String): HttpUtils {
        params[key] = value
        return this
    }

    fun addHeader(key: String, value: String): HttpUtils {
        headers[key] = value
        return this
    }


}