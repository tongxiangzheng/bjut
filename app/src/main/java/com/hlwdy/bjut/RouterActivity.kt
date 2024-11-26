package com.hlwdy.bjut

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.hlwdy.bjut.ui.card.CardFragment
import com.hlwdy.bjut.ui.otp.OtpFragment
import com.hlwdy.bjut.ui.schedule.ScheduleFragment

class RouterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_router)

        appLogger.init(this)

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
        }
    }
}