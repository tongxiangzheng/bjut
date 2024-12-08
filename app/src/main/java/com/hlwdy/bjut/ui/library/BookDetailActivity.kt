package com.hlwdy.bjut.ui.library

import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.hlwdy.bjut.BaseActivity
import com.hlwdy.bjut.R

class BookDetailActivity : BaseActivity() {
    fun showToast(message: String) {
        android.os.Handler(this.mainLooper).post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    private fun showContent(id:String,base:String){
        showLoading()
        var webview=findViewById<WebView>(R.id.book_webview)
        webview.settings.javaScriptEnabled=true

        webview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // 注入CSS
                val css = "@media (prefers-color-scheme: dark) {body{background:#121212;color:white;}.resoult .article .tableLib .titBox{background:#2d2d2d;border: 1px solid #4a4a4a;}.resoult .article .tit{border-bottom: 1px solid #4d4d4d;}.resoult .article .tableLib table th, .resoult .article .tableLib td{border: 1px solid #4d4d4d;}}"
                webview.evaluateJavascript(
                    """  
            (function() {  
                var style = document.createElement('style');  
                style.type = 'text/css';  
                style.innerHTML = '$css';  
                document.head.appendChild(style);  
            })();  
            """, null
                )
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                hideLoading()
            }
        }
        webview.loadUrl("https://libyt.bjut.edu.cn/search/detail.action?searchtype=wrd&searchbase=$base&doc_number=$id&page=&xc=3")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.book_detail)

        val id = intent.getStringExtra("book_id").toString()
        val base = intent.getStringExtra("base").toString()
        showContent(id,base)
    }

}