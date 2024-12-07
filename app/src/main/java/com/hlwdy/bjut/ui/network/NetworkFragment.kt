package com.hlwdy.bjut.ui.network

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.lifecycleScope

import java.net.HttpURLConnection
import java.net.URL


import com.hlwdy.bjut.databinding.FragmentNetworkBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import org.json.JSONObject


fun isInternalIp(ip: String): Boolean {
    val privateIpPatterns = listOf(
        "^10\\..*".toRegex(),
        "^172\\.(1[6-9]|2[0-9]|3[0-1])\\..*".toRegex(),
        "^192\\.168\\..*".toRegex()
    )
    return privateIpPatterns.any { it.matches(ip) }
}
fun checkWebsiteAccessibility(urlString: String): Boolean {
    return try {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout=1000
        connection.readTimeout=1000
        connection.instanceFollowRedirects=false
        connection.connect()

        val responseCode = connection.responseCode
        if(responseCode == HttpURLConnection.HTTP_OK){
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            Log.d("normal","response: "+response)
            response.isNotEmpty()
        }else{
            connection.disconnect()
            false
        }
    } catch (e: Exception) {
        false
    }
}

fun getJsonFromUrl(urlString: String): String? {
    try {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = connection.inputStream
            inputStream.bufferedReader().use { reader ->
                return reader.readText()
            }
        } else {
            // 处理错误响应
            println("Error: ${connection.responseCode}")
            return null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }

}



class NetworkFragment : Fragment() {

    private var _binding: FragmentNetworkBinding? = null
    private var networkStates="unknown"
    private var networkLoginStates=false
    val handler = Handler(Looper.getMainLooper())
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private fun updateShow(){
        when(networkStates){
            "unknown"->binding.networkStateMonitor.setText("网络状态未知")
            "wifi"->binding.networkStateMonitor.setText("bjut_wifi")
            "dorm"->binding.networkStateMonitor.setText("宿舍光猫")
            "bjut"->binding.networkStateMonitor.setText("有线网（你是怎么做到的？）")
            "outBJUT"->binding.networkStateMonitor.setText("校外")
            "unknownType"->binding.networkStateMonitor.setText("未知校内网络")
            else->binding.networkStateMonitor.setText("error: $networkStates")
        }
        if(networkLoginStates){
            binding.networkLoginStateMonitor.setText("已登录")
        }else{
            binding.networkLoginStateMonitor.setText("未登录")
        }
    }
    private fun checkBjutLocation(){
        //测试是否为bjut_wifi
        val wres= checkWebsiteAccessibility("http://wlgn.bjut.edu.cn")
        if(wres){
            networkStates="wifi"
            return
        }
        //测试是否为宿舍光猫
        val bres= checkWebsiteAccessibility("http://10.21.221.98")
        if(bres){
            networkStates="dorm"
            return
        }
        //测试是否为其他情况（例如有线网）
        val res= checkWebsiteAccessibility("http://lgn.bjut.edu.cn")
        if(res){
            networkStates="bjut"
            return
        }
        networkStates="unknownType"
    }
    private fun checkLoginStatus() {
        //val host = getServiceHost()
        //val timestamp = Instant.now().epochSecond
        //val urlString = "http://${host}/drcom/chkstatus?callback=dr${timestamp}"123"
        val urlString=when(networkStates){
            "wifi"->"https://wlgn.bjut.edu.cn/drcom/chkstatus?callback=dr1002"
            "dorm"->"http://10.21.221.98:801/eportal/portal/online_list"
            "bjut"->"lgn.bjut.edu.cn"
            else->""
        }
        if(urlString==""){
            networkLoginStates=false
            return
        }
        val resString = getJsonFromUrl(urlString)
        if(resString == null){
            networkLoginStates=false
            return
        }
        try {
            val jsonString=resString.substringAfter("(").substringBeforeLast(")")
            if (jsonString != "") {
                val jsonObject = JSONObject(jsonString)
                networkLoginStates = (jsonObject.getInt("result") == 1)
            } else {
                networkLoginStates = false
            }
        }catch (e:Exception){
            networkLoginStates = false
        }

    }
    private fun checkNetworkStates() {
        val domain="bjut.edu.cn"
        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val address = InetAddress.getByName(domain)
                    if (isInternalIp(address.hostAddress!!)) {
                        checkBjutLocation()
                        checkLoginStatus()
                    }else {
                        networkStates = "outBJUT"
                        networkLoginStates = false
                    }
                    true
                } catch (e: Exception) {
                    false
                }
            }
            if(success){
                updateShow()
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNetworkBinding.inflate(inflater, container, false)
        checkNetworkStates()
        val root: View = binding.root
        binding.btnLogin.setOnClickListener {

        }

        binding.btnSetPwd.setOnClickListener {

        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}