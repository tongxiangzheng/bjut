package com.hlwdy.bjut.ui.otp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hlwdy.bjut.account_session_util
import com.hlwdy.bjut.databinding.FragmentOtpBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import net.arraynetworks.vpn.OTPManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.security.SecureRandom
import java.util.UUID

/*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


fun readOtpData(context:Context): ByteArray? {
    var otpData: ByteArray?=null
    var database: SQLiteDatabase? = null
    try {
        val dbPath = context.getDatabasePath("otp.db").path
        database = SQLiteDatabase.openDatabase(
            dbPath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
        database.query(
            "servers",  // 表名
            null,      // 所有列
            null,      // 无条件
            null,
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                otpData = cursor.getBlob(cursor.getColumnIndexOrThrow("data"))
            }
        }
    } catch (e: Exception) {

    } finally {
        database?.close()
    }
    return otpData
}

class AESCrypto {
    // IV (Initialization Vector)
    private val initVector: ByteArray = byteArrayOf(
        97, 114, 114, 97, 121, 100, 101, 118, 73, 86,
        99, 108, 105, 99, 107, 49
    )

    private val ivSpec: IvParameterSpec = IvParameterSpec(initVector)

    // Encryption Key
    private val secretKey: ByteArray = byteArrayOf(
        97, 114, 114, 97, 121, 110, 101, 116, 119, 111,
        114, 107, 115, 57, 50, 48, 106, 83, 100, 56,
        102, 42, 35, 57, 42, 100, 45, 35, 106, 48,
        46, 72
    )

    private val keySpec: SecretKeySpec = SecretKeySpec(secretKey, "AES")

    private var cipher: Cipher? = null

    init {
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    }

    /**
     * 解密数据
     * @param encryptedData 加密的数据
     * @return 解密后的数据
     * @throws Exception 如果解密失败
     */
    @Throws(Exception::class)
    fun decrypt(encryptedData: ByteArray): ByteArray {
        try {
            cipher?.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            return cipher?.doFinal(encryptedData) ?: throw Exception("Cipher not initialized")
        } catch (e: Exception) {
            val message = "[decrypt] ${e.message}"
            throw Exception(message)
        }
    }

    /**
     * 加密数据
     * @param plainData 原始数据
     * @return 加密后的数据
     * @throws Exception 如果加密失败
     */
    @Throws(Exception::class)
    fun encrypt(plainData: ByteArray): ByteArray {
        try {
            cipher?.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            return cipher?.doFinal(plainData) ?: throw Exception("Cipher not initialized")
        } catch (e: Exception) {
            val message = "[encrypt] ${e.message}"
            throw Exception(message)
        }
    }

    companion object {
        /**
         * 静态解密方法
         * @param data 要解密的数据
         * @return 解密后的数据，如果解密失败返回null
         */
        @JvmStatic
        fun decryptStatic(data: ByteArray?): ByteArray? {
            if (data != null && data.isNotEmpty()) {
                try {
                    return AESCrypto().decrypt(data)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return null
        }
    }
}*/

fun generateSM():ByteArray{
    val uuid = UUID.randomUUID().toString()
    val secureRandom = SecureRandom.getInstance("SHA1PRNG")
    secureRandom.setSeed(uuid.toByteArray())
    val randomBytes = ByteArray(10)
    secureRandom.nextBytes(randomBytes)
    return randomBytes
}

class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    fun showToast(message: String) {
        activity?.let { fragmentActivity ->
            Handler(Looper.getMainLooper()).post {
                if (isAdded) {
                    Toast.makeText(fragmentActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private var otpD:String?=null
    fun refreshOtp(){
        if(otpD!=null){
            val otpManager = OTPManager()
            val secret = otpD.toString().chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            val otp = otpManager.generateOTP(secret)
            binding.textOtp.text=String.format("%06d",otp)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        val root: View = binding.root

        otpD=account_session_util(requireContext()).getUserDetails()[account_session_util.KEY_OTPDATA]
        refreshOtp()
        /*var otpData=readOtpData(requireContext())
        if(otpData!=null){
            val otpManager = OTPManager()
            val secret = AESCrypto().decrypt(otpData)
            showToast(String(net.arraynetworks.vpn.NativeLib().encodeOptData(secret)))
            val otp = otpManager.generateOTP(secret)
            textView.text=String.format("%06d",otp)
        }*/

        binding.btnLook.setOnClickListener {
            val editText = EditText(context).apply {
                setText(otpD.toString())
                isFocusable = false
                isFocusableInTouchMode = false
                isLongClickable = true
                keyListener=null
                setTextIsSelectable(true)  // 允许选择文本
                setPadding(32, 32, 32, 32)
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("OTP data")
                .setView(editText)
                .setPositiveButton("复制") { _, _ ->
                    // 复制文本到剪贴板
                    val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("text", otpD.toString())
                    clipboard.setPrimaryClip(clip)
                    showToast("已复制")
                }
                .setNegativeButton("取消", null)
                .show()
        }

        binding.btnRegister.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setTitle("登录到校园VPN")
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
            builder.setPositiveButton("设备注册") { dialog, which ->
                val userAccount = username.text.toString()
                val userPassword = password.text.toString()
                val data=generateSM()
                showToast("请稍后")
                OTPManager().registerOTP(userAccount,userPassword,String(net.arraynetworks.vpn.NativeLib().encodeOptData(data)),
                    object :
                        Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            showToast("network error")
                        }
                        override fun onResponse(call: Call, response: Response) {
                            val res=response.body?.string().toString()
                            activity?.let { fragmentActivity ->
                                Handler(Looper.getMainLooper()).post {
                                    if (isAdded) {
                                        if(res.contains("登录失败")){
                                            showToast("登录失败，请检查账号密码")
                                        }else if(res.contains("重复绑定")){
                                            showToast("已经绑定过，无法重复绑定")
                                        }else {
                                            showToast("绑定完成")
                                            otpD=data.joinToString("") { "%02x".format(it) }
                                            account_session_util(requireContext()).editOTPData(otpD.toString())
                                            refreshOtp()
                                        }
                                    }
                                }
                            }
                        }})
            }

            builder.setNegativeButton("取消") { dialog, which ->
                dialog.cancel()
            }

            val dialog = builder.create()
            dialog.show()
        }

        binding.btnAdd.setOnClickListener {
            val editText = EditText(context).apply {
                setTextIsSelectable(true)  // 允许选择文本
                setPadding(32, 32, 32, 32)
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("导入OTP data")
                .setView(editText)
                .setPositiveButton("导入") { _, _ ->
                    if(!editText.text.toString().matches(Regex("^[a-zA-Z0-9]*$")) ||editText.text.toString().length!=20){//20位
                        showToast("数据格式无效")
                        return@setPositiveButton
                    }
                    if(otpD!=null){
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("这将覆盖当前data，是否确认操作?")
                            .setPositiveButton("确认") { _, _ ->
                                otpD=editText.text.toString()
                                account_session_util(requireContext()).editOTPData(otpD.toString())
                                refreshOtp()
                            }
                            .setNegativeButton("取消", null)
                            .show()
                    }else{
                        otpD=editText.text.toString()
                        account_session_util(requireContext()).editOTPData(otpD.toString())
                        refreshOtp()
                    }
                }
                .setNegativeButton("取消", null)
                .show()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            // 当Fragment处于STARTED状态（可见）时才会执行
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                refreshOtp()
                flow {
                    while (true) {
                        // 每次都基于当前系统时间计算，所以恢复时会显示正确的时间
                        val remainingSeconds = 60 - (System.currentTimeMillis() / 1000).toInt() % 60
                        emit(remainingSeconds)
                        delay(1000)
                    }
                }.collect { remainingSeconds ->
                    binding.textOtptime.text = "$remainingSeconds 秒后更新"
                    if (remainingSeconds == 60) {
                        // 处理刷新逻辑
                        refreshOtp()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}