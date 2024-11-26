package com.hlwdy.bjut

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.hlwdy.bjut.ui.LoadingDialog

abstract class BaseFragment : Fragment() {
    private var loadingView: View? = null

    protected fun showLoading() {
        if (loadingView == null) {
            // 获取Fragment的根视图
            val rootView = view as? ViewGroup ?: return

            // 创建加载视图
            loadingView = layoutInflater.inflate(R.layout.dialog_loading, null)

            // 创建与父容器相同大小的LayoutParams
            val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // 找到Fragment内容的实际位置
            val location = IntArray(2)
            rootView.getLocationInWindow(location)

            // 设置loadingView的位置和大小
            loadingView?.layoutParams = params

            // 添加到根视图
            rootView.addView(loadingView)

            // 确保loadingView在最上层
            loadingView?.bringToFront()
        }
        loadingView?.visibility = View.VISIBLE
    }

    fun hideLoading() {
        loadingView?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
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