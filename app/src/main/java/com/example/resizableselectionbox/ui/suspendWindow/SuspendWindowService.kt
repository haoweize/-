package com.example.resizableselectionbox.ui.suspendWindow

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer


import com.example.resizableselectionbox.logic.network.SearchBodyData
import com.example.resizableselectionbox.ui.SearchViewModel
import com.example.resizableselectionbox.ui.SelectionBox.DraggableResizableRectView
import com.example.resizableselectionbox.ui.SuspendViewModel
import com.example.resizableselectionbox.R
import java.util.Objects

class SuspendWindowService : LifecycleService(){

    private lateinit var windowManager: WindowManager
    private var floatRootView: View? = null//悬浮窗View
    private var translationView: View? = null//选择框View
    private var translateButton: View? = null//确定框View

    private lateinit var draggableResizableRectView: DraggableResizableRectView
    private lateinit var layoutParam2:WindowManager.LayoutParams

    //接收Activity中传递的MediaProjection对象
    private val mMediaProjectionManager = SuspendViewModel.mMpMgr

    @SuppressLint("ResourceAsColor")
    override fun onCreate() {
        super.onCreate()
        //开启前台服务
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("my_service", "前台Service通知",
                NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }
        //点击前台service通知，回到UI
        val intent = Intent(this, FirstFragment::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, "my_service")
            .setContentTitle("This is content title")
            .setContentText("This is content text")
            .setSmallIcon(android.R.drawable.spinner_background)
            .setLargeIcon(BitmapFactory.decodeResource(resources, android.R.drawable.spinner_background))
            .setContentIntent(pi)
            .build()
        startForeground(1, notification)
        //创建悬浮球与选择框
        initObserve()
        //弹出确定框按钮
        SuspendViewModel.popTranslate.observe(this, Observer {
            translateButton = LayoutInflater.from(this).inflate(R.layout.translate_button, null)
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Window 类型（需要悬浮窗权限）
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                // 设置窗口不获取焦点，并让视图布局到屏幕中
                PixelFormat.TRANSLUCENT
            )
            //设置确定框位置
            params.gravity = Gravity.TOP or Gravity.START
            if (SuspendViewModel.right > SuspendViewModel.screenWidth -650f){
                params.x = (SuspendViewModel.left +200f).toInt()
            }else{
                params.x = (SuspendViewModel.right - 200f).toInt()
            }

            if (SuspendViewModel.top < 150f){
                params.y = (SuspendViewModel.bottom).toInt()
            }else{
                params.y = (SuspendViewModel.top -50).toInt()
            }
            windowManager.addView(translateButton,params)
            translateButton?.findViewById<Button>(R.id.translate)?.setOnClickListener {
                val searchBodyData = SearchBodyData(SuspendViewModel.source!!, "en2zh")
                SearchViewModel.searchTranslation(searchBodyData)
                Log.d("Dragglable", "开始翻译")
                it.visibility = View.INVISIBLE
            }
        })
        //根据确定框状态更新其是否可见
        SuspendViewModel.statusPopTranslate.observe(this, Observer{
            if(!it){
                windowManager.removeView(translateButton)
            }
        })
        //从网络上查找框住的内容
        SearchViewModel.translateLiveData.observe(this, Observer {
            //创建翻译框视图
            translationView = LayoutInflater.from(this).inflate(R.layout.translation_box,null)
            //根据翻译结果确定其内容
            translationView?.findViewById<TextView>(R.id.translation)?.apply {
                text = it.getOrNull()?:""
                textSize = 20f
                background = getDrawable(R.color.translation)

            }
            //设置翻译框在窗口中的属性
            val translationParam = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Window 类型（需要悬浮窗权限）
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                // 设置窗口不获取焦点，并让视图布局到屏幕中
                PixelFormat.TRANSLUCENT
            )
            //设置翻译框初始位置
            translationParam.gravity = Gravity.TOP or Gravity.START
            if (SuspendViewModel.top > SuspendViewModel.screenHeight-SuspendViewModel.bottom){
                translationParam.y = 0
            }else{
                translationParam.y = (SuspendViewModel.bottom+ 100f).toInt()
            }
            translationParam.x = 0
            //添加翻译框
            windowManager.addView(translationView,translationParam)
            SuspendViewModel.statusTranslation.value = true
            SuspendViewModel.hasTranslation = true
        })
        //根据翻译框状态更新其是否可见
        SuspendViewModel.statusTranslation.observe(this, Observer {
            if(!it){
                windowManager.removeView(translationView)
            }
        })
    }

    private fun initObserve() {
        SuspendViewModel.apply {
            /**
             * 悬浮窗按钮的显示和隐藏
             */
            isVisible.observe(this@SuspendWindowService, Observer{
                floatRootView?.visibility = if (it) View.VISIBLE else View.GONE
            })
            /**
             * 悬浮窗按钮的创建和移除
             */
            isShowSuspendWindow.observe(this@SuspendWindowService, Observer{
                if (it) {
                    if(floatRootView != null){
                        windowManager?.removeView(floatRootView)
                    }else{
                        showWindow()
                    }
                } else {
                    windowManager?.removeView(floatRootView)
                }
            })
        }
    }

    private fun showWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        //获取屏幕的宽高
        windowManager.defaultDisplay.getMetrics(outMetrics)
        SuspendViewModel.screenWidth = windowManager.defaultDisplay.width
        SuspendViewModel.screenHeight = windowManager.defaultDisplay.height

        var layoutParam = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.RGBA_8888
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            //位置大小设置
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            gravity = Gravity.LEFT or Gravity.TOP
            //设置剧中屏幕显示
            x = outMetrics.widthPixels / 2 - width / 2
            y = outMetrics.heightPixels / 2 - height / 2
        }
        // 新建悬浮窗控件
        floatRootView = LayoutInflater.from(this).inflate(R.layout.suspendwindow, null)
        // 将悬浮窗控件添加到WindowManager
        windowManager.addView(floatRootView, layoutParam)
        floatRootView?.setOnTouchListener(ItemViewTouchListener(layoutParam, windowManager))
        floatRootView?.setOnClickListener {

            //获取屏幕截图
            getShotScreen()
            //创建选择框实例
            draggableResizableRectView = DraggableResizableRectView(this)
            //重新设置自定义视图在窗口中的位置。
            layoutParam2 = WindowManager.LayoutParams().apply {
                type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                format = PixelFormat.RGBA_8888
                flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                //视图宽度、高度设置
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.MATCH_PARENT
                //设置视图初始化时的位置。
                gravity = Gravity.LEFT or Gravity.TOP
//                x = 0
//                y = 0
            }
            //添加到windowManager中
            windowManager.addView(draggableResizableRectView,layoutParam2)
        }
    }
    @SuppressLint("WrongConstant")
    fun getShotScreen(){
        val mResultCode = SuspendViewModel.resultCode
        val mMediaProjection = mMediaProjectionManager?.getMediaProjection(
            mResultCode,
            Objects.requireNonNull(SuspendViewModel.intent!!)
        )
        val metrics = resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val screenDensity = metrics.densityDpi
        //创建一个图像读取器
        val imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 1);
        val virtualDisplay = mMediaProjection?.createVirtualDisplay("ScreenCapture",
            screenWidth, screenHeight, screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.getSurface(), null, null);
        Thread.sleep(100)
        val image = imageReader.acquireNextImage()

        //将image转换成bitmap
        val bitmap = imageToBitmap(image)

        SuspendViewModel.hasGet.value = SuspendViewModel.hasGet.value
        SuspendViewModel.screen = bitmap
    }
    //imageToBitmap
    fun imageToBitmap(image: Image): Bitmap? {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val bitmap = Bitmap.createBitmap(image.width + rowPadding / pixelStride, image.height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)

        return bitmap
    }
    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(floatRootView)
        Log.d("FirstFragment", "移除了float")
    }

}