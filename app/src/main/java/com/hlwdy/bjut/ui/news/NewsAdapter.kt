package com.hlwdy.bjut.ui.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hlwdy.bjut.R

class NewsAdapter(
    private val items: List<NewsItem>,
    private val onItemClick: (NewsItem) -> Unit
) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val TitleText: TextView = view.findViewById(R.id.newsItemText)
        val DateText: TextView = view.findViewById(R.id.newsItemDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.news_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val newsItem = items[position]
        holder.TitleText.text = newsItem.title
        holder.DateText.text = newsItem.date
        holder.itemView.setOnClickListener {
            onItemClick(newsItem)
        }
    }

    override fun getItemCount() = items.size
}