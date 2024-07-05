package com.example.resizableselectionbox.logic.network

import com.example.resizableselectionbox.HookApplication
import com.example.resizableselectionbox.logic.model.SearchResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface searchService {
    @Headers("content-type:application/json","x-authorization:token ${HookApplication.TOKEN}")
    @POST("v1/translator")
    fun searchTranslation(@Body data: SearchBodyData): Call<SearchResponse>
}