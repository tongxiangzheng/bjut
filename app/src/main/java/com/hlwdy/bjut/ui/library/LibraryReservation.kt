package com.hlwdy.bjut.ui.library

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import com.hlwdy.bjut.BaseFragment
import com.hlwdy.bjut.account_session_util
import com.hlwdy.bjut.appLogger
import com.hlwdy.bjut.databinding.FragmentLibraryReservationBinding

class LibraryReservationViewModel : ViewModel() {
    var webView: WebView? = null
    var webViewState: Bundle? = null
    var isWebViewInitialized = false
}

class LibraryReservationFragment : BaseFragment() {

    private var _binding: FragmentLibraryReservationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: LibraryReservationViewModel by viewModels()

    fun showToast(message: String) {
        activity?.let { fragmentActivity ->
            Handler(Looper.getMainLooper()).post {
                if (isAdded) {
                    Toast.makeText(fragmentActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun loadUrl(url: String) {
        activity?.let {
            Handler(Looper.getMainLooper()).post {
                if (isAdded) {
                    viewModel.webView?.settings?.javaScriptEnabled=true
                    viewModel.webView?.settings?.domStorageEnabled=true//localstorage
                    viewModel.webView?.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            return false
                        }
                        override fun onPageFinished(view: WebView, url: String) {
                            hideLoading()
                        }
                    }
                    viewModel.webView?.loadUrl(url)
                    viewModel.isWebViewInitialized=true
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLibraryReservationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if (viewModel.webView == null) {
            viewModel.webView = binding.libraryReservationView
        } else {
            // 如果WebView已存在，从旧的父视图中移除
            (viewModel.webView?.parent as? ViewGroup)?.removeView(viewModel.webView)
            // 将已存在的WebView添加到新的布局中
            (binding.root as? ViewGroup)?.addView(viewModel.webView)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoading()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.webView?.canGoBack() == true) {
                    viewModel.webView?.goBack()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        if(!viewModel.isWebViewInitialized){
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setCookie(".webvpn.bjut.edu.cn", "wengine_vpn_ticketwebvpn_bjut_edu_cn="+
                    account_session_util(requireContext()).getUserDetails()[account_session_util.KEY_WEBVPNTK].toString()+"; path=/")
            cookieManager.flush()

            loadUrl("https://webvpn.bjut.edu.cn/https/77726476706e69737468656265737421fcfe438f223d615e7f1a9ba397586d3708b09bf11aa570982732/")
        }else{
            viewModel.webViewState?.let { viewModel.webView?.restoreState(it) }
            hideLoading()
        }

    }
    override fun onPause() {
        super.onPause()
        // 保存WebView状态
        viewModel.webView?.let { webView ->
            val state = Bundle()
            webView.saveState(state)
            viewModel.webViewState = state
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (viewModel.webView?.parent as? ViewGroup)?.removeView(viewModel.webView)
        _binding = null
    }


}