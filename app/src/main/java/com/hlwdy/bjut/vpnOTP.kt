//package net.arraynetworks.vpn
package com.hlwdy.bjut

import okhttp3.Callback
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/*
class NativeLib {
    init {
        System.loadLibrary("vpn")  // 加载libvpn.so
    }

    // 声明native方法
    external fun getOptValue(timestamp: Int, secret: ByteArray, interval: Int): Int
    external fun encodeOptData(data: ByteArray): ByteArray
}*/

class Base32Encoder {//无填充base32
    companion object {
        private const val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

        fun toBase32(bytes: ByteArray): String {
            // Base32编码
            val bits = bytes.flatMap { byte ->
                (7 downTo 0).map { (byte.toInt() shr it) and 1 }
            }

            return bits.chunked(5)
                .map { group ->
                    if (group.size < 5) {
                        group + List(5 - group.size) { 0 }
                    } else {
                        group
                    }
                }
                .map { group ->
                    val value = group.fold(0) { acc, bit -> (acc shl 1) or bit }
                    BASE32_CHARS[value]
                }
                .joinToString("")
        }
    }
}

class OTPManager {
    private var timeOffset: Long = 0  // 时间偏移量(毫秒)

    // 获取校准后的时间戳(秒)
    private fun getAdjustedTimestamp(): Int {
        return ((System.currentTimeMillis() + timeOffset) / 1000L).toInt()
    }

    /*链接库otp生成 String.format("%06d",otp)
    fun generateOTP(secret: ByteArray, interval: Int = 60): Int {
        val timestamp = getAdjustedTimestamp()
        return try {
            NativeLib().getOptValue(timestamp, secret, interval)
        } catch (e: Exception) {
            -1
        }
    }*/

    fun generateOTP(secret: ByteArray, interval: Int = 60, digits: Int = 6): String {
        // 获取当前时间戳并计算时间步数
        val currentTime = getAdjustedTimestamp() / interval

        // 将时间步数转换为字节数组
        val msg = ByteBuffer.allocate(8).putLong(currentTime.toLong()).array()

        // 使用HMAC-SHA1计算哈希值
        val hmac = Mac.getInstance("HmacSHA1")
        val keySpec = SecretKeySpec(secret, "HmacSHA1")
        hmac.init(keySpec)
        val hash = hmac.doFinal(msg)

        // 获取偏移量
        val offset = (hash[hash.size - 1].toInt() and 0xf)

        // 生成OTP值
        val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                ((hash[offset + 1].toInt() and 0xff) shl 16) or
                ((hash[offset + 2].toInt() and 0xff) shl 8) or
                (hash[offset + 3].toInt() and 0xff)

        val otp = binary % Math.pow(10.0, digits.toDouble()).toInt()

        // 格式化输出，确保位数正确
        return String.format("%0${digits}d", otp)
    }

    // 设置时间偏移
    fun setTimeOffset(offset: Long) {
        timeOffset = offset
    }

    fun registerOTP(usname:String,pwd:String,sm:String,callback: Callback){
        HttpUtils().addHeader("User-Agent","BjutApp")
            .addHeader("Cookie","ANStandalone=true;;ANStandalone=true")
            .addParam("method","otp")
            .addParam("uname",usname)
            .addParam("pwd",pwd)
            .addParam("sm",sm)
            .post("https://vpn.bjut.edu.cn/prx/000/http/localhost/register",callback)
    }
}