package com.hlwdy.bjut.ui.news

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.hlwdy.bjut.BaseFragment
import com.hlwdy.bjut.BjutAPI
import com.hlwdy.bjut.BjutHttpRsa
import com.hlwdy.bjut.account_session_util
import com.hlwdy.bjut.appLogger
import com.hlwdy.bjut.databinding.FragmentNewsBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class NewsFragment : BaseFragment() {
    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: NewsAdapter
    private val newsList = mutableListOf<NewsItem>()

    private val viewModel: NewsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun showToast(message: String) {
        activity?.let { fragmentActivity ->
            Handler(Looper.getMainLooper()).post {
                if (isAdded) {
                    Toast.makeText(fragmentActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun refreshNewsList(cid:String,page:Int=1){
        isLoading=true
        showLoading()
        BjutAPI().getNewsList(account_session_util(requireContext()).getUserDetails()[account_session_util.KEY_SESS].toString(),cid,page.toString()
            ,object :
                Callback {
                override fun onFailure(call: Call, e: IOException) {
                    showToast("network error")
                }
                override fun onResponse(call: Call, response: Response) {
                    val res = JSONObject(
                        BjutHttpRsa.requestDecrypt(
                            response.body?.string().toString()
                        )
                    )
                    if(res.getString("e")=="0"){
                        val tmp=res.getJSONObject("d").getJSONObject("list").getJSONArray("data")
                        val l=mutableListOf<NewsItem>()
                        for (i in 0 until tmp.length()) {
                            val classObject = tmp.getJSONObject(i)
                            l.add(NewsItem(classObject.getString("id"),classObject.getString("title"),
                                classObject.getString("summary"), classObject.getString("createdate")))
                        }
                        activity?.let {
                            Handler(Looper.getMainLooper()).post {
                                if (isAdded) {
                                    if(page==1){//first
                                        updateNewsList(l)
                                    }else{
                                        addNewsItems(l)
                                    }
                                    Curpage=page
                                    Curcid=cid
                                    hideLoading()
                                }
                            }
                        }
                        isLoading=false
                    }else{
                        showToast("error")
                        appLogger.e("Error", "Try NewsList $cid-$page error")
                    }
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        BjutAPI().WebVpnLoginMy(account_session_util(requireContext()).getUserDetails()[account_session_util.KEY_WEBVPNTK].toString()
            ,object :
                Callback {
                override fun onFailure(call: Call, e: IOException) {
                    showToast("network error")
                }
                override fun onResponse(call: Call, response: Response) {}
            })

        val tabLayout = binding.tabLayout
        val tabTitles = listOf("校发通知", "会议通知", "公示公告", "教学通告","学校工作信息","院部处通知","院部处工作信息","学术海报")
        tabTitles.forEach { title ->
            tabLayout.addTab(tabLayout.newTab().apply {
                text = title
                contentDescription = title
            })
        }
        viewModel.selectedTabPosition.observe(viewLifecycleOwner) { position ->
            tabLayout.selectTab(tabLayout.getTabAt(position))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.setSelectedTab(tab?.position ?: 0)
                refreshNewsList((tab?.position?.plus(1)).toString())
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        if (viewModel.isFirstLoad||viewModel.selectedTabPosition.value==0) {
            refreshNewsList("1")
            viewModel.isFirstLoad = false
        }
    }

    private var isLoading = false
    private var Curpage=0
    private var Curcid=""

    private fun setupRecyclerView() {
        adapter = NewsAdapter(newsList) { newsItem ->
            openNewsDetail(newsItem)
        }
        binding.NewsListView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@NewsFragment.adapter
        }
        binding.NewsListView.layoutManager = LinearLayoutManager(requireContext())
        binding.NewsListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount
                if (!isLoading&&lastVisibleItem >= totalItemCount - 1) {
                    refreshNewsList(Curcid,Curpage+1)
                    //showToast("next")
                }
            }
        })
    }

    // 添加新闻项的方法
    private fun addNewsItem(id:String,title: String, content: String, date:String) {
        newsList.add(NewsItem(id,title, content,date))
        adapter.notifyItemInserted(newsList.size - 1)
    }

    private fun addNewsItems(newItems: List<NewsItem>) {
        val cnt=newsList.size
        newsList.addAll(newItems)
        adapter.notifyItemRangeInserted(cnt, newItems.size)
    }

    private fun updateNewsList(newItems: List<NewsItem>) {
        clearNewsList()
        newsList.addAll(newItems)
        adapter.notifyItemRangeInserted(0, newItems.size)
    }

    private fun clearNewsList() {
        val size = newsList.size
        newsList.clear()
        adapter.notifyItemRangeRemoved(0, size)
    }

    private fun openNewsDetail(newsItem: NewsItem) {
        NewsDetailActivity.start(requireContext(), newsItem)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}