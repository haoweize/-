package com.example.resizableselectionbox.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.resizableselectionbox.logic.Repository
import com.example.resizableselectionbox.logic.network.SearchBodyData

object SearchViewModel : ViewModel() {
    private val searchLiveData = MutableLiveData<SearchBodyData>()

    //使用searchLiveData中的数据传递到Repository中去获得翻译结果，并将该结果转换成translateLiveData。
    val translateLiveData = Transformations.switchMap(searchLiveData) { searchBodyData ->
        Repository.searchTranslation(searchBodyData)
    }

    fun searchTranslation(searchBodyData: SearchBodyData) {
        searchLiveData.value = searchBodyData
    }
}