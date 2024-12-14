package com.hlwdy.bjut

import BiometricHelper
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.hlwdy.bjut.ui.LoginActivity
import com.hlwdy.bjut.ui.card.CardFragment
import com.hlwdy.bjut.ui.network.NetworkFragment
import com.hlwdy.bjut.ui.otp.OtpFragment
import com.hlwdy.bjut.ui.schedule.ScheduleFragment

class RouterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_router)

        if(account_session_util(this).getUserDetails()[account_session_util.KEY_SESS].toString()=="null"){
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        appLogger.init(this)

        BiometricHelper(this).authenticate({
            if (intent?.action == "openCardCode") {
                val fragment = CardFragment().apply {
                    arguments = Bundle().apply {
                        putBoolean("jump_code", true)
                    }
                }

                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit()
            }else if (intent?.action == "openSchedule") {
                val fragment = ScheduleFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit()
            }else if (intent?.action == "openOTP") {
                val fragment = OtpFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit()
            }else if (intent?.action == "openNetwork") {
                val fragment = NetworkFragment()
                val bundle=Bundle()
                bundle.putBoolean("loginNow",true)
                fragment.arguments = bundle
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit()

            }
        })

    }
}