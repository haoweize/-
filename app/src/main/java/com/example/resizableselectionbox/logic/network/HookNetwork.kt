package com.example.resizableselectionbox.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object HookNetwork {
    //动态获取搜索代理
    private val searchService = ServiceCreator.create<searchService>()

    suspend fun searchTranslation(body:SearchBodyData) = searchService.searchTranslation(body).await()

    //实现挂起函数解挂后的动作。
    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            //在回调函数中开启子线程将从网络获取的数据返回给调用者。 返回的形式是 classT 这里主要是model文件下的几个Response类。
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(
                        RuntimeException("response body is null"))

                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}