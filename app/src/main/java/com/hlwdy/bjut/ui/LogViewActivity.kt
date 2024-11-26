package com.hlwdy.bjut.ui

import android.database.Cursor
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hlwdy.bjut.LogDbHelper
import com.hlwdy.bjut.appLogger
import com.hlwdy.bjut.R
import java.util.Date
import java.util.Locale

class LogAdapter : RecyclerView.Adapter<LogAdapter.ViewHolder>() {
    private var cursor: Cursor? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun swapCursor(newCursor: Cursor?) {
        cursor?.close()
        cursor = newCursor
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        cursor?.let { cursor ->
            if (cursor.moveToPosition(position)) {
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(LogDbHelper.COLUMN_TIMESTAMP))
                val tag = cursor.getString(cursor.getColumnIndexOrThrow(LogDbHelper.COLUMN_TAG))
                val message = cursor.getString(cursor.getColumnIndexOrThrow(LogDbHelper.COLUMN_MESSAGE))
                val stackTrace = cursor.getString(cursor.getColumnIndexOrThrow(LogDbHelper.COLUMN_STACK_TRACE))

                holder.bind(timestamp, tag, message, stackTrace)
            }
        }
    }

    override fun getItemCount(): Int = cursor?.count ?: 0

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)
        private val tvTag: TextView = view.findViewById(R.id.tvTag)
        private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        private val tvStackTrace: TextView = view.findViewById(R.id.tvStackTrace)

        fun bind(timestamp: Long, tag: String, message: String, stackTrace: String?) {
            tvTimestamp.text = dateFormat.format(Date(timestamp))
            tvTag.text = tag
            if(tag=="Error")tvTag.setTextColor(Color.RED)
            tvMessage.text = message

            if (stackTrace != null) {
                tvStackTrace.visibility = View.VISIBLE
                tvStackTrace.text = stackTrace
            } else {
                tvStackTrace.visibility = View.GONE
            }
        }
    }
}

class LogViewActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LogAdapter
    private var cursor: Cursor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_view)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = LogAdapter()
        recyclerView.adapter = adapter

        cursor = appLogger.getLogs()
        adapter.swapCursor(cursor)
    }

    override fun onDestroy() {
        cursor?.close()
        super.onDestroy()
    }
}  