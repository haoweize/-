package com.example.resizableselectionbox.logic.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//这个单例类是一般流程。
object ServiceCreator {

    private const val BASE_URL = "http://api.interpreter.caiyunai.com/"
    //创建retrofit实例
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    //创建接口动态代理对象
    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    inline fun <reified T> create(): T = create(T::class.java)

}