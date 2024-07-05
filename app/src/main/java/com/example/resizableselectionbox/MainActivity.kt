package com.example.resizableselectionbox

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.resizableselectionbox.ui.SuspendViewModel


class MainActivity : AppCompatActivity() {
    //安卓状态栏高度
    private var barHeight :Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SuspendViewModel.hasGet.observe(this, Observer {
//            SuspendViewModel.rootView = screenShot()
            findViewById<ImageView>(R.id.imgView).setImageBitmap(SuspendViewModel.screen)
        })
        barHeight = getStatusBarHeight(this)
        SuspendViewModel.barHeight = barHeight
    }
    //确保杀死后台时，所有相关操作已经完成。
    override fun onDestroy() {
        super.onDestroy()
        SuspendViewModel.isShowSuspendWindow.value = false
        Log.d("FirstFragment", "Activity被销毁")
    }
    //获取安卓状态栏的高度
    fun getStatusBarHeight(context: Context): Int {
        var statusBarHeight = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }
}

