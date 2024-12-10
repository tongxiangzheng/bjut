package com.hlwdy.bjut

class NativeEncrypthlwdyck {
    init {
        System.loadLibrary("encrypt")
    }

    fun getPackageName():String{
        return "com.hlwdy.bjut"
    }

    external fun keypr(): String
    external fun keypu(): String

    private val cachedPrivateKey by lazy { keypr() }
    private val cachedPublicKey by lazy { keypu() }

    fun getPrKey(): String = cachedPrivateKey
    fun getPuKey(): String = cachedPublicKey
}