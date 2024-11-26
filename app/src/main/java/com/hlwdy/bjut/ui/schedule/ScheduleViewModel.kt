package com.hlwdy.bjut.ui.schedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class ScheduleViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    val days = listOf("周一", "周二", "周三", "周四", "周五","周六","周日")
    val timeSlots = listOf(
        "8:00-8:45", "8:50-9:35", "9:55-10:40", "10:45-11:30",
        "13:30-14:15", "14:20-15:05", "15:25-16:10", "16:15-17:00",
        "18:00-18:45", "18:50-19:35", "19:55-20:40", "20:45-21:30"
    )
    var mp=mapOf("1" to "周一", "2" to "周二","3" to "周三","4" to "周四","5" to "周五","6" to "周六","7" to "周日")

    var selectedWeek: Int?
        get() = savedStateHandle.get<Int>("selected_week")
        set(value) {
            savedStateHandle["selected_week"] = value
        }
}

data class Course(
    val name: String,
    val day: String,
    val timeCode: String, // 例如 "0102" 表示第1-2节课
    val location: String,
    var teacher: String,
    var id: String,
    var weeks: String
)
