package com.hlwdy.bjut.ui.news

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hlwdy.bjut.BjutAPI
import com.hlwdy.bjut.R
import com.hlwdy.bjut.account_session_util
import com.hlwdy.bjut.ui.schedule.Course
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class NewsDetailActivity : AppCompatActivity() {
    fun showToast(message: String) {
        android.os.Handler(this.mainLooper).post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    private fun showContent(content:String){
        android.os.Handler(this.mainLooper).post {
            var webview=findViewById<WebView>(R.id.news_webview)
            webview.settings.javaScriptEnabled=true
            webview.loadDataWithBaseURL(null,
                "$content <script type=\"text/javascript\">var tables = document.getElementsByTagName(\"img\");for(var i = 0; i<tables.length; i++){tables[i].style.width = \"100%\";tables[i].style.height = \"auto\";}</script>"
                +"<style>@media (prefers-color-scheme: dark) { body,span{ background-color: #121212 !important; color: #cfcfcf  !important; } p{ background-color: #121212 !important; } }table { border-color: #333 !important; } th, td { border-color: #333 !important; }</style>",
                "text/html", "UTF-8", null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_detail)

        val title = intent.getStringExtra(EXTRA_TITLE)
        val id = intent.getStringExtra(EXTRA_ID)
        
        BjutAPI().getNewsDetail(account_session_util(this).getUserDetails()[account_session_util.KEY_WEBVPNTK].toString(),id.toString(),object :
            Callback {
            override fun onFailure(call: Call, e: IOException) {
                showToast("network error")
            }
            override fun onResponse(call: Call, response: Response) {
                try{
                    val res=JSONObject(response.body?.string().toString())
                    showContent(res.toString())
                    if(res.getString("e")=="0"){
                        showContent(res.getJSONObject("d").getString("content"))
                    }else{
                        showToast("error")
                    }
                }catch (e:Exception){
                    showToast("error")
                }
            }
        })

        findViewById<TextView>(R.id.news_title).text = title
    }

    companion object {
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_ID = "extra_id"

        fun start(context: Context, newsItem: NewsItem) {
            val intent = Intent(context, NewsDetailActivity::class.java).apply {
                putExtra(EXTRA_TITLE, newsItem.title)
                putExtra(EXTRA_ID, newsItem.id)
            }
            context.startActivity(intent)
        }
    }
}