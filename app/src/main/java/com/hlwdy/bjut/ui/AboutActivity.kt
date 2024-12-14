package com.hlwdy.bjut.ui

import BiometricHelper
import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import com.hlwdy.bjut.R
import com.hlwdy.bjut.settings_util

class AboutActivity : AppCompatActivity() {

    fun showToast(message: String) {
        android.os.Handler(this.mainLooper).post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            "unknown"
        }

        // 设置版本号文本
        findViewById<TextView>(R.id.tvVersion).text = "Version $versionName"

        val checkbox1=findViewById<CheckBox>(R.id.autoUpdateCheckBox)
        var autoCheck=settings_util(this).getSettingBool(settings_util.KEY_AUTOUPDATE)
        if(autoCheck==null){
            autoCheck=true
        }
        checkbox1.isChecked=autoCheck
        checkbox1.setOnCheckedChangeListener { buttonView, isChecked ->
            // isChecked 是新的状态
            settings_util(this).editSettingBool(settings_util.KEY_AUTOUPDATE,isChecked)
        }

        val checkbox2=findViewById<CheckBox>(R.id.bioEncryptCheckBox)
        var bioEncrypt=settings_util(this).getSettingBool(settings_util.KEY_BIOENCRYPT)
        if(bioEncrypt==null){
            bioEncrypt=false
        }
        checkbox2.isChecked=bioEncrypt
        var isUserAction = true
        checkbox2.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isUserAction) {
                when (BiometricManager.from(this)
                    .canAuthenticate(BIOMETRIC_STRONG)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> {
                        // 已经录入了生物识别
                        BiometricHelper(this).authenticate(
                            onSuccess = {
                                settings_util(this).editSettingBool(settings_util.KEY_BIOENCRYPT,isChecked)
                            },
                            onFailed = {
                                if (isUserAction) {
                                    isUserAction = false
                                    buttonView.isChecked = !isChecked
                                    isUserAction = true
                                }
                            }
                        )
                        return@setOnCheckedChangeListener
                    }
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                        // 设备支持但未录入，显示提示对话框
                        AlertDialog.Builder(this)
                            .setTitle("设置生物识别")
                            .setMessage("需要系统设置完成生物识别录入才能使用此功能。")
                            .setNegativeButton("好的", null)
                            .show()
                    }
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                        showToast("此设备不支持生物识别")
                    }
                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                        showToast("生物识别硬件当前不可用")
                    }
                    else ->{
                        showToast("生物验证不可用")
                    }
                }

                //不可用复位
                isUserAction = false
                // 恢复之前的状态
                buttonView.isChecked = !isChecked
                isUserAction = true
            }
        }
    }
}