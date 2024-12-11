package com.hlwdy.bjut.ui.library

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hlwdy.bjut.BaseFragment
import com.hlwdy.bjut.BjutAPI
import com.hlwdy.bjut.account_session_util
import com.hlwdy.bjut.appLogger
import com.hlwdy.bjut.databinding.FragmentLibraryBorrowBinding
import com.hlwdy.bjut.databinding.ItemBorrowbookBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Date
import java.util.Locale

data class BorrowBookInfo(
    val bookName: String,
    val bookId: String,
    val BorrowTime: Long,
    val BorrowEndTime: Long,
    val bookStatus: String
)
class LibraryBorrowFragment : BaseFragment() {
    private var _binding: FragmentLibraryBorrowBinding? = null
    private val binding get() = _binding!!
    private val bookAdapter = BorrowBookAdapter()

    fun showToast(message: String) {
        activity?.let { fragmentActivity ->
            Handler(Looper.getMainLooper()).post {
                if (isAdded) {
                    Toast.makeText(fragmentActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBorrowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadBookData()
        binding.swipeRefresh.apply {
            setOnRefreshListener {
                loadBookData()
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun setupRecyclerView() {
        binding.borrowBookListView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bookAdapter
        }
    }

    private fun loadBookData() {
        showLoading()
        BjutAPI().getBookBorrow(
            account_session_util(requireContext()).getUserDetails()[account_session_util.KEY_SESS].toString()
            ,object :
                Callback {
                override fun onFailure(call: Call, e: IOException) {
                    showToast("network error")
                }
                override fun onResponse(call: Call, response: Response) {
                    val res = JSONObject(response.body?.string().toString())
                    if(res.getString("e")=="0"){
                        val tmp=res.getJSONObject("d").getJSONArray("list")
                        val l=mutableListOf<BorrowBookInfo>()
                        for (i in 0 until tmp.length()) {
                            val classObject = tmp.getJSONObject(i)
                            l.add(
                                BorrowBookInfo(
                                    bookName = classObject.getString("book_name"),
                                    bookId = classObject.getString("book_id"),
                                    BorrowTime = classObject.getLong("lendtime"),
                                    BorrowEndTime = classObject.getLong("lendendtime"),
                                    bookStatus = classObject.getString("book_status")
                                )
                            )
                        }
                        activity?.let { fragmentActivity ->
                            Handler(Looper.getMainLooper()).post {
                                if (isAdded) {
                                    if (l.isEmpty()) {
                                        // 显示空
                                        binding.borrowBookListView.visibility = View.GONE
                                        binding.emptyView.visibility = View.VISIBLE
                                    } else {
                                        // 显示列表
                                        binding.borrowBookListView.visibility = View.VISIBLE
                                        binding.emptyView.visibility = View.GONE
                                        bookAdapter.submitList(l)
                                    }
                                    hideLoading()
                                }
                            }
                        }
                    }else{
                        showToast("error")
                        appLogger.e("Error", "BorrowBookList error")
                    }
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class BorrowBookAdapter : ListAdapter<BorrowBookInfo, BorrowBookAdapter.BookViewHolder>(BorrowBookDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBorrowbookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookViewHolder(
        private val binding: ItemBorrowbookBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: BorrowBookInfo) {
            binding.apply {
                tvBookName.text = book.bookName
                tvBookId.text = "图书编号: ${book.bookId}"
                tvBorrowTime.text = "借阅时间: ${formatDate(book.BorrowTime)}"
                tvEndTime.text = "应还时间: ${formatDate(book.BorrowEndTime)}"
                tvStatus.text = "状态: ${book.bookStatus}"
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date(timestamp * 1000))
        }
    }
}
class BorrowBookDiffCallback : DiffUtil.ItemCallback<BorrowBookInfo>() {
    override fun areItemsTheSame(oldItem: BorrowBookInfo, newItem: BorrowBookInfo): Boolean {
        return oldItem.bookId == newItem.bookId
    }

    override fun areContentsTheSame(oldItem: BorrowBookInfo, newItem: BorrowBookInfo): Boolean {
        return oldItem == newItem
    }
}