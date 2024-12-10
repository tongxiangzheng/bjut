package com.hlwdy.bjut.ui

import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hlwdy.bjut.R
import com.hlwdy.bjut.settings_util

class AboutActivity : AppCompatActivity() {

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

        val checkbox=findViewById<CheckBox>(R.id.autoUpdateCheckBox)
        var autoCheck=settings_util(this).getSettingBool(settings_util.KEY_AUTOUPDATE)
        if(autoCheck==null){
            autoCheck=true
        }
        checkbox.isChecked=autoCheck
        checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            // isChecked 是新的状态
            settings_util(this).editSettingBool(settings_util.KEY_AUTOUPDATE,isChecked)
        }
    }
}