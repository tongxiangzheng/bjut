package com.hlwdy.bjut

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    private var loadingView: View? = null

    protected fun showLoading() {
        if (loadingView == null) {
            // 获取Activity的根视图
            val rootView = findViewById<ViewGroup>(android.R.id.content)

            // 创建加载视图
            loadingView = layoutInflater.inflate(R.layout.dialog_loading, null)

            // 创建与父容器相同大小的LayoutParams
            val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // 设置loadingView的位置和大小
            loadingView?.layoutParams = params

            // 添加到根视图
            rootView.addView(loadingView)

            // 确保loadingView在最上层
            loadingView?.bringToFront()
        }
        loadingView?.visibility = View.VISIBLE
    }

    protected fun hideLoading() {
        loadingView?.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        (loadingView?.parent as? ViewGroup)?.removeView(loadingView)
        loadingView = null
    }

    // 协程扩展函数
    protected suspend fun <T> withLoading(block: suspend () -> T): T {
        try {
            showLoading()
            return block()
        } finally {
            hideLoading()
        }
    }
}