package com.hlwdy.bjut.ui.library

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hlwdy.bjut.BaseFragment
import com.hlwdy.bjut.BjutAPI
import com.hlwdy.bjut.databinding.FragmentLibrarySearchBinding
import com.hlwdy.bjut.databinding.ItemBookBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.regex.Pattern

data class Book(
    var title: String? = null,
    var isbn: String? = null,
    var author: String? = null,
    var publisher: String? = null,
    var year: String? = null,
    var number: String? = null,
    var id: String? = null,
)

private fun decodeHtml(text: String?): String? {
    if (text == null) return null
    return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
}

fun extractBookInfo(htmlContent: String): List<Book> {
    val books = mutableListOf<Book>()

    // 匹配每个book条目
    val bookPattern = """(?s)DOC-NUMBER\s*\(\d+\)\s*=\s*(\d+).*?<table class=items[^>]*?>.*?</table>""".toRegex()
    val bookMatcher = bookPattern.findAll(htmlContent)

    bookMatcher.forEach { matchResult ->
        val docNumber = matchResult.groupValues[1] // 获取DOC-NUMBER
        val tableContent = matchResult.value
        val book = Book()
        book.id=docNumber

        // 提取标题
        val titlePattern = Pattern.compile("<div class=itemtitle><a[^>]*?>([^<]+?)(?:\\s*:?\\s*&nbsp;)*</a>")
        titlePattern.matcher(tableContent).also { matcher ->
            if (matcher.find()) {
                book.title = decodeHtml(matcher.group(1)?.trim())
            }
        }

        // 提取ISBN
        val isbnPattern = Pattern.compile("isbn=([0-9-]+)|fmt_issn\\(\"([0-9-]+)\"\\)")
        isbnPattern.matcher(tableContent).also { matcher ->
            if (matcher.find()) {
                book.isbn = matcher.group(1) ?: matcher.group(2)
            }
        }

        // 提取作者
        val authorPattern = Pattern.compile("作者：<td class=content[^>]*?>(.*?)</td>")
        authorPattern.matcher(tableContent).also { matcher ->
            if (matcher.find()) {
                book.author = decodeHtml(matcher.group(1))?.split("\\s+".toRegex())
                    ?.filter { it.isNotBlank() }
                    ?.joinToString(" ")
            }
        }

        // 提取出版社
        val publisherPattern = Pattern.compile("出版社：<td class=content[^>]*?>(.*?)</td>")
        publisherPattern.matcher(tableContent).also { matcher ->
            if (matcher.find()) {
                book.publisher = decodeHtml(matcher.group(1)?.trim())
            }
        }

        // 提取出版年份
        val yearPattern = Pattern.compile("出版年：<td class=content[^>]*?>(.*?)</td>")
        yearPattern.matcher(tableContent).also { matcher ->
            if (matcher.find()) {
                book.year = decodeHtml(matcher.group(1)?.trim())
            }
        }

        // 提取索书号
        val callNumberPattern = Pattern.compile("索书号：<td class=content[^>]*?>(.*?)</td>")
        callNumberPattern.matcher(tableContent).also { matcher ->
            if (matcher.find()) {
                book.number = decodeHtml(matcher.group(1)?.trim())
            }
        }

        books.add(book)
    }

    return books
}


class LibrarySearchFragment : BaseFragment() {
    private var _binding: FragmentLibrarySearchBinding? = null
    private val binding get() = _binding!!
    private val bookAdapter = BookAdapter { book ->
        //跳转图书详情
        val intent = Intent(context, BookDetailActivity::class.java).apply {
            putExtra("book_id", book.id)
            putExtra("base", selectedType.id)
        }
        requireContext().startActivity(intent)
    }

    // 数据类定义
    data class SearchOption(
        val id: String,        // 用于API调用的值
        val displayName: String // 显示给用户看的文本
    )

    // 定义选项数据
    private val typeOptions = listOf(
        SearchOption("bgd01", "中文文献"),
        SearchOption("bgd09", "外文文献")
    )

    private val searchTypeOptions = listOf(
        SearchOption("WRD", "任意字段"),
        SearchOption("WTI", "题名"),
        SearchOption("WAU", "著者"),
        SearchOption("WPU", "出版社")
    )
    private var selectedType: SearchOption = typeOptions[0]  // 默认选中中文
    private var selectedSearchType: SearchOption = searchTypeOptions[0]  // 默认选中任意

    private var isSearchBarVisible = true

    private fun setupRecyclerView() {
        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bookAdapter
        }
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLibrarySearchBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 设置语言选项
        val typeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            typeOptions.map { it.displayName }
        )
        binding.typeSelector.post {
            (binding.typeSelector.editText as? AutoCompleteTextView)?.apply {
                setAdapter(typeAdapter)
                setText(selectedType.displayName, false)

                setOnItemClickListener { _, _, position, _ ->
                    selectedType = typeOptions[position]
                }
            }
        }

        // 设置搜索类型选项
        val searchTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            searchTypeOptions.map { it.displayName }
        )
        binding.searchTypeSelector.post {
            (binding.searchTypeSelector.editText as? AutoCompleteTextView)?.apply {
                setAdapter(searchTypeAdapter)
                setText(selectedSearchType.displayName, false)

                setOnItemClickListener { _, _, position, _ ->
                    selectedSearchType = searchTypeOptions[position]
                }
            }
        }

        setupRecyclerView()
        setupRecyclerViewScroll()

        binding.searchButton.setOnClickListener {
            val searchQuery = binding.searchInput.text.toString()
            if(searchQuery==""){
                showToast("搜索内容不可为空")
            }else{
                setSearchControlsEnabled(false)
                bookAdapter.submitList(emptyList())
                binding.noResultsText.visibility = View.GONE
                binding.loadingIndicator.visibility = View.VISIBLE
                BjutAPI().getBookList(searchQuery,selectedSearchType.id,selectedType.id,object :
                    Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        showToast("network error")
                    }
                    override fun onResponse(call: Call, response: Response) {
                        val res=response.body?.string().toString()
                        val results=extractBookInfo(res)
                        activity?.let { fragmentActivity ->
                            Handler(Looper.getMainLooper()).post {
                                if (isAdded) {
                                    if (results.isEmpty()) {
                                        binding.noResultsText.visibility = View.VISIBLE
                                    } else {
                                        bookAdapter.submitList(results)
                                    }
                                    binding.loadingIndicator.visibility = View.GONE
                                    setSearchControlsEnabled(true)
                                }
                            }
                        }
                    }
                })
            }

        }

        return root
    }

    private fun setSearchControlsEnabled(enabled: Boolean) {
        binding.apply {
            searchButton.isEnabled = enabled
            searchInput.isEnabled = enabled
        }
    }

    private fun setupRecyclerViewScroll() {
        var lastScrollY = 0
        binding.searchResultsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // dy > 0 表示向上滚动，dy < 0 表示向下滚动
                if (dy > 30 && isSearchBarVisible) {  // 向上滚动，隐藏搜索栏
                    hideSearchBar()
                } else if (dy < -30 && !isSearchBarVisible) {  // 向下滚动，显示搜索栏
                    showSearchBar()
                }
                lastScrollY = dy
            }
        })
    }

    private fun hideSearchBar() {
        isSearchBarVisible = false
        binding.searchCard.animate()
            .translationY(-binding.searchCard.height.toFloat()-30)
            .setDuration(200)
            .start()
    }

    private fun showSearchBar() {
        isSearchBarVisible = true
        binding.searchCard.animate()
            .translationY(0f)
            .setDuration(200)
            .start()
    }
}


class BookAdapter(
    private val onItemClick: (Book) -> Unit  // 添加点击回调
) : ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookViewHolder(private val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }
        fun bind(book: Book) {
            binding.apply {
                bookTitle.text = book.title
                bookAuthor.text = "作者：${book.author}"
                bookPublisher.text = "出版社：${book.publisher}"
                bookYear.text = "出版年：${book.year}"
                bookISBN.text = "ISBN：${book.isbn}"
                bookNumber.text = "索书号：${book.number}"
                bookID.text="编号：${book.id}"
            }
        }
    }
}

class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
    override fun areItemsTheSame(oldItem: Book, newItem: Book) =
        oldItem.number == newItem.number

    override fun areContentsTheSame(oldItem: Book, newItem: Book) =
        oldItem == newItem
}