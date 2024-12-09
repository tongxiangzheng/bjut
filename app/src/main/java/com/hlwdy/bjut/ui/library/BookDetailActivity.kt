package com.hlwdy.bjut.ui.library

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.hlwdy.bjut.BaseActivity
import com.hlwdy.bjut.R
import java.net.URL

class BookDetailActivity : BaseActivity() {
    fun showToast(message: String) {
        android.os.Handler(this.mainLooper).post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    private fun showContent(id:String,base:String){
        showLoading()
        val webview=findViewById<WebView>(R.id.book_webview)
        webview.settings.javaScriptEnabled=true

        webview.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                //拦截html
                val url = request?.url.toString()
                if (url.contains("detail.action")) {
                    try {
                        // 获取原始响应
                        val connection = URL(url).openConnection()
                        val input = connection.getInputStream()
                        val originalHtml = input.bufferedReader().use { it.readText() }
                        // 注入的 CSS
                        val darkModeCss = "<style>@media (prefers-color-scheme: dark) {body{background:#121212;color:white;}.resoult .article .tableLib .titBox{background:#2d2d2d;border: 1px solid #4a4a4a;}.resoult .article .tit{border-bottom: 1px solid #4d4d4d;}.resoult .article .tableLib table th, .resoult .article .tableLib td{border: 1px solid #4d4d4d;}}</style>"
                        // 在 </head> 标签前插入 CSS
                        val modifiedHtml = if (originalHtml.contains("</head>")) {
                            originalHtml.replace("</head>", "$darkModeCss</head>")
                        } else {
                            // 如果没有 head 标签，在开头插入
                            "$darkModeCss$originalHtml"
                        }

                        // 创建新的响应
                        val modifiedInputStream = modifiedHtml.byteInputStream()
                        return WebResourceResponse(
                            "text/html",
                            "UTF-8",
                            modifiedInputStream
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return super.shouldInterceptRequest(view, request)
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