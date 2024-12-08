package com.hlwdy.bjut.ui.network

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hlwdy.bjut.account_session_util

import java.net.HttpURLConnection
import java.net.URL


import com.hlwdy.bjut.databinding.FragmentNetworkBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import org.json.JSONObject
import com.hlwdy.bjut.network_account_util

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
        connection.connectTimeout=500
        connection.readTimeout=500
        connection.instanceFollowRedirects=false
        connection.connect()

        val responseCode = connection.responseCode
        if(responseCode == HttpURLConnection.HTTP_OK){
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            //Log.d("normal","response: "+response)
            response.isNotEmpty()
        }else{
            connection.disconnect()
            false
        }
    } catch (e: Exception) {
        false
    }
}

fun getDataFromUrl(urlString: String): String? {
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
private fun checkBjutLocation():String{
    //测试是否为bjut_wifi
    val wres= checkWebsiteAccessibility("http://wlgn.bjut.edu.cn")
    if(wres){
        return "wifi"
    }
    //测试是否为宿舍光猫
    val bres= checkWebsiteAccessibility("http://10.21.221.98")
    if(bres){
        return "dorm"
    }
    //测试是否为其他情况（例如有线网）
    val res= checkWebsiteAccessibility("http://lgn.bjut.edu.cn")
    if(res){
        return "bjut"
    }
    return "unknownType"
}
fun checkLoginStatusBjut():Boolean {
    val urlString="https://lgn.bjut.edu.cn"
    val htmlData=getDataFromUrl(urlString)?:return false
    Log.d("normal", "html: $htmlData")
    return htmlData.contains("<!--Dr.COMWebLoginID_1.htm-->")
}
fun checkLoginStatusDormOrWifi(networkStates:String):Boolean {
    val urlString=when(networkStates){
        "wifi"->"https://wlgn.bjut.edu.cn/drcom/chkstatus?callback=dr1002"
        "dorm"->"http://10.21.221.98:801/eportal/portal/online_list"
        else->""
    }
    val resString = getDataFromUrl(urlString) ?: return false
    try {
        val jsonString=resString.substringAfter("(").substringBeforeLast(")")
        if (jsonString != "") {
            val jsonObject = JSONObject(jsonString)
            return (jsonObject.getInt("result") == 1)
        } else {
            return false
        }
    }catch (e:Exception){
        return false
    }
}
fun checkLoginStatus(networkStates:String):Boolean {
    return when(networkStates){
        "wifi"->checkLoginStatusDormOrWifi(networkStates)
        "dorm"->checkLoginStatusDormOrWifi(networkStates)
        "bjut"->checkLoginStatusBjut()
        else->false
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
    private fun showToast(message: String) {
        activity?.let { fragmentActivity ->
            Handler(Looper.getMainLooper()).post {
                if (isAdded) {
                    Toast.makeText(fragmentActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun updateShow(){
        when(networkStates){
            "unknown"->binding.networkStateMonitor.setText("网络状态未知")
            "wifi"->binding.networkStateMonitor.setText("bjut_wifi")
            "dorm"->binding.networkStateMonitor.setText("宿舍光猫")
            "bjut"->binding.networkStateMonitor.setText("有线网")
            "unknownType"->binding.networkStateMonitor.setText("未知校内网络")
            "outBJUT"->binding.networkStateMonitor.setText("校外")
            "noNetwork"->binding.networkStateMonitor.setText("无网络")
            else->binding.networkStateMonitor.setText("error: $networkStates")
        }
        if(networkLoginStates){
            binding.networkLoginStateMonitor.setText("已登录")
        }else{
            binding.networkLoginStateMonitor.setText("未登录")
        }
    }


    private fun checkNetworkStates() {
        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val address = InetAddress.getByName("bjut.edu.cn")
                    if (isInternalIp(address.hostAddress!!)) {
                        networkStates=checkBjutLocation()
                        networkLoginStates=checkLoginStatus(networkStates)
                    }else {
                        networkStates = "outBJUT"
                        networkLoginStates = false
                    }
                    true
                } catch (e: Exception) {
                    false
                }
            }
            if(!success) {
                networkStates = "noNetwork"
                networkLoginStates = false
            }
            updateShow()
        }
    }
    private fun setPassword(networkAccount: network_account_util){
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("保存密码")
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        val usernameLayout = TextInputLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            hint = "账号"
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE // 设置外边框样式
        }
        val username = TextInputEditText(requireContext()).apply { setText(account_session_util(requireContext()).getUserDetails()[account_session_util.KEY_USERNAME]) }
        usernameLayout.addView(username)

        val passwordLayout = TextInputLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            hint = "密码"
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            // 添加密码可见性开关
            endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        }
        val password = TextInputEditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        passwordLayout.addView(password)

        layout.addView(usernameLayout)
        layout.addView(passwordLayout)
        builder.setView(layout)
        builder.setPositiveButton("保存") { dialog, _ ->
            val userAccount = username.text.toString()
            val userPassword = password.text.toString()
            networkAccount.editUserData(userAccount, userPassword)
            showToast("保存成功")
            dialog.cancel()
        }

        builder.setNegativeButton("取消") { dialog, _ ->
            dialog.cancel()
        }

        val dialog = builder.create()
        dialog.show()
    }
    private fun loginNetwork(networkAccount: network_account_util){
        if(!networkAccount.haveData()){
            setPassword(networkAccount)
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNetworkBinding.inflate(inflater, container, false)

        val networkAccount=network_account_util(requireContext())
        checkNetworkStates()
        val root: View = binding.root
        binding.btnRefresh.setOnClickListener {
            checkNetworkStates()
        }
        binding.btnLogin.setOnClickListener {
            loginNetwork(networkAccount)
        }
        binding.btnSetPwd.setOnClickListener {
            setPassword(networkAccount)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}