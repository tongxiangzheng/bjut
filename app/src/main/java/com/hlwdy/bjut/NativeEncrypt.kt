package com.hlwdy.bjut

class NativeEncrypthlwdyck {
    init {
        System.loadLibrary("encrypt")
    }

    external fun keypr(): String
    external fun keypu(): String
}