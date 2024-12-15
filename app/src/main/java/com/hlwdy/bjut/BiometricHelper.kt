import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.hlwdy.bjut.settings_util

class BiometricHelper(private val activity: AppCompatActivity) {
    private lateinit var biometricPrompt: BiometricPrompt

    init {
        setupBiometric()
    }

    private fun setupBiometric() {
        val executor = ContextCompat.getMainExecutor(activity)

        biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    pendingSuccess?.invoke()
                    clearCallbacks()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    //Toast.makeText(activity, "认证错误：$errString", Toast.LENGTH_SHORT).show()
                    Toast.makeText(activity, "认证失败", Toast.LENGTH_SHORT).show()
                    pendingFailed?.invoke()
                    clearCallbacks()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(activity, "尝试验证失败", Toast.LENGTH_SHORT).show()
                    //pendingAction = null
                    //允许继续尝试
                }
            })
    }

    private var pendingSuccess: (() -> Unit)? = null
    private var pendingFailed: (() -> Unit)? = null

    private fun clearCallbacks() {
        pendingSuccess = null
        pendingFailed = null
    }

    fun authenticate(onSuccess: () -> Unit, onFailed: () -> Unit = {}) {
        var bioEncrypt=settings_util(activity).getSettingBool(settings_util.KEY_BIOENCRYPT)
        if(bioEncrypt==null){
            bioEncrypt=false
        }
        if(!bioEncrypt){
            onSuccess.invoke()
            return
        }

        pendingSuccess = onSuccess
        pendingFailed = onFailed

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("安全验证")
            .setSubtitle("请验证您的身份")
            .setNegativeButtonText("取消")
            .setConfirmationRequired(false)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}