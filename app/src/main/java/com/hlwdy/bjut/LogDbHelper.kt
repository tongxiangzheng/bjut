package com.hlwdy.bjut

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LogDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "AppLogs.db"
        private const val DATABASE_VERSION = 1

        // 表结构定义
        private const val TABLE_LOGS = "logs"
        const val COLUMN_ID = "_id"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_TAG = "tag"
        const val COLUMN_MESSAGE = "message"
        const val COLUMN_STACK_TRACE = "stack_trace"

        // 建表SQL
        private const val SQL_CREATE_LOGS_TABLE = """  
            CREATE TABLE $TABLE_LOGS (  
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,  
                $COLUMN_TIMESTAMP INTEGER NOT NULL,  
                $COLUMN_TAG TEXT NOT NULL,  
                $COLUMN_MESSAGE TEXT NOT NULL,  
                $COLUMN_STACK_TRACE TEXT  
            )  
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_LOGS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 简单处理，直接删除旧表并创建新表
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOGS")
        onCreate(db)
    }

    fun addLog(tag: String, message: String, throwable: Throwable? = null) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_TIMESTAMP, System.currentTimeMillis())
            put(COLUMN_TAG, tag)
            put(COLUMN_MESSAGE, message)
            put(COLUMN_STACK_TRACE, throwable?.stackTraceToString())
        }

        db.insert(TABLE_LOGS, null, values)

        // 保持最新的1000条记录
        db.execSQL("""  
            DELETE FROM $TABLE_LOGS   
            WHERE $COLUMN_ID NOT IN (  
                SELECT $COLUMN_ID FROM $TABLE_LOGS   
                ORDER BY $COLUMN_TIMESTAMP DESC   
                LIMIT 200
            )  
        """)
    }

    fun getLogs(): Cursor {
        return readableDatabase.query(
            TABLE_LOGS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_TIMESTAMP DESC"
        )
    }

    fun clearLogs() {
        writableDatabase.delete(TABLE_LOGS, null, null)
    }
}

object appLogger {
    private lateinit var dbHelper: LogDbHelper

    fun init(context: Context) {
        dbHelper = LogDbHelper(context.applicationContext)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        dbHelper.addLog(tag, message, throwable)
    }

    fun getLogs(): Cursor = dbHelper.getLogs()

    fun clearLogs() = dbHelper.clearLogs()
}