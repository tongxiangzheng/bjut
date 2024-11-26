package com.hlwdy.bjut.ui.card

import android.os.Bundle
import android.webkit.WebView
import androidx.lifecycle.ViewModel

class CardViewModel : ViewModel() {
    var webView: WebView? = null
    var webViewState: Bundle? = null
    var isWebViewInitialized = false
}