package net.arraynetworks.vpn

import android.os.Handler
import android.os.Looper
import com.hlwdy.bjut.BjutHttpRsa
import com.hlwdy.bjut.HttpUtils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class NativeLib {
    init {
        System.loadLibrary("vpn")  // 加载libvpn.so
    }

    // 声明native方法
    external fun getOptValue(timestamp: Int, secret: ByteArray, interval: Int): Int
    external fun encodeOptData(data: ByteArray): ByteArray
}
class OTPManager {
    private var timeOffset: Long = 0  // 时间偏移量(毫秒)

    // 获取校准后的时间戳(秒)
    private fun getAdjustedTimestamp(): Int {
        return ((System.currentTimeMillis() + timeOffset) / 1000L).toInt()
    }

    // 生成OTP
    fun generateOTP(secret: ByteArray, interval: Int = 60): Int {
        val timestamp = getAdjustedTimestamp()
        return try {
            NativeLib().getOptValue(timestamp, secret, interval)
        } catch (e: Exception) {
            -1
        }
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