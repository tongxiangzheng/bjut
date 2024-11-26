package com.hlwdy.bjut.ui.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NewsViewModel: ViewModel() {
    private val _selectedTabPosition = MutableLiveData<Int>(0)
    val selectedTabPosition: LiveData<Int> = _selectedTabPosition

    fun setSelectedTab(position: Int) {
        _selectedTabPosition.value = position
    }

    var isFirstLoad = true
}