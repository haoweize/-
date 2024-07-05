package com.example.resizableselectionbox.ui

import android.content.Intent
import android.graphics.Bitmap
import android.media.projection.MediaProjectionManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

object SuspendViewModel : ViewModel() {

    //悬浮窗口创建 移除
    var isShowSuspendWindow = MutableLiveData<Boolean>()
    //悬浮窗口显示 隐藏
    var isVisible = MutableLiveData<Boolean>()


    //媒体投影相关
    var resultCode: Int = 0
    var intent: Intent? = null
    var mMpMgr: MediaProjectionManager? = null
    var screen:Bitmap? = null
    var hasGet = MutableLiveData<Boolean>()

    //状态栏高度
    var barHeight:Int = 0
    //根据选择框位置设置翻译悬浮窗位置
    var left: Float = 0f
    var top: Float = 0f
    var right: Float = 0f
    var bottom: Float = 0f
    //屏幕宽度、高度
    var screenWidth:Int = 0
    var screenHeight:Int = 0

    //待查寻内容
    var source:List<String>? = null
    //弹出确定框
    var popTranslate = MutableLiveData<Boolean>()
    //确定框状态
    var statusPopTranslate = MutableLiveData<Boolean>()
    //翻译框状态
    var statusTranslation = MutableLiveData<Boolean>()
    var hasTranslation: Boolean = false

}