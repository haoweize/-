package com.example.resizableselectionbox.logic

import androidx.lifecycle.liveData
import com.example.resizableselectionbox.logic.network.HookNetwork
import com.example.resizableselectionbox.logic.network.SearchBodyData
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

//仓库层的主要工作就是判断调用方请求的数据应该是从本地数据源中获取还是从网络数据源中获取，
// 并将获得的数据返回给调用方。
object Repository {

    fun searchTranslation(bodyData: SearchBodyData) = fire(Dispatchers.IO) {
        val searchResponse = HookNetwork.searchTranslation(bodyData)
        if (searchResponse.target != null) {
            when(searchResponse.target){
                is String ->Result.success(searchResponse.target.toString())
                is List<*> ->Result.success(searchResponse.target[0].toString())
                else ->Result.success("对不起，无法翻译您选中的内容")
            }
        } else {
            Result.failure(RuntimeException("response status is ${searchResponse.isdict}"))
        }
    }

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }
}