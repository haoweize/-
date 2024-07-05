package com.example.resizableselectionbox.ui.SelectionBox

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.resizableselectionbox.ui.SuspendViewModel
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer

class DraggableResizableRectView(context: Context) : View(context) {

    //创建选择框
    private var rect: RectF = RectF()
    //创建控制点
    private var resizeControlPointRadius = 40f
    //创建右控制点
    private var resizeControlPointRect: RectF = RectF()
    //创建左控制点
    private var resizeControlPointRect2: RectF = RectF()

    private val tag = "Draggable"

    //初始化
    private var isDragging = false
    private var isRightResizing = false
    private var isLeftResizing = false
    private var lastX = 0f
    private var lastY = 0f

    //设置画笔属性
    private var paint: Paint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }
    private var paint2: Paint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.FILL
    }

    init {
        rect.set(400f, 900f, 800f, 1000f)
        resizeControlPointRect.set(
            rect.right - resizeControlPointRadius,
            rect.bottom - resizeControlPointRadius,
            rect.right + resizeControlPointRadius,
            rect.bottom + resizeControlPointRadius
        )
        resizeControlPointRect2.set(
            rect.left - resizeControlPointRadius,
            rect.top - resizeControlPointRadius,
            rect.left + resizeControlPointRadius,
            rect.top + resizeControlPointRadius
        )
    }
    //
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(rect, paint)
        canvas.drawOval(resizeControlPointRect, paint2)
        canvas.drawOval(resizeControlPointRect2, paint2)
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (SuspendViewModel.statusPopTranslate.value == true){
                    SuspendViewModel.statusPopTranslate.value = false
                }
                //更新翻译框状态，若存在则取消，若不存在则跳过此步骤
                if (SuspendViewModel.statusTranslation.value == true && SuspendViewModel.hasTranslation){
                    SuspendViewModel.statusTranslation.value = false
                }
                // 判断是否点击到了控制点
                if (resizeControlPointRect.contains(x, y)) {
                    isRightResizing = true
                    lastX = x
                    lastY = y
                    Log.d(tag, "点击到了右控制点")
                } else if (rect.contains(x, y)) {
                    isDragging = true
                    lastX = x
                    lastY = y
//                    Toast.makeText(context, "点击到了拖动框", Toast.LENGTH_SHORT).show()
                    Log.d(tag, "点击到了拖动框")
                }else if (resizeControlPointRect2.contains(x, y)){
                    isLeftResizing = true
                    lastX = x
                    lastY = y
                    Log.d(tag, "点击到了左控制点")
                }else{
                    //返回上个界面
                    this.visibility = GONE
                    //更新翻译框状态，若存在则取消，若不存在则跳过此步骤
                    if (SuspendViewModel.statusTranslation.value == true && SuspendViewModel.hasTranslation){
                        SuspendViewModel.statusTranslation.value = false
                    }
                    Log.d(tag, "返回上一个界面")
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (SuspendViewModel.statusPopTranslate.value == true){
                    SuspendViewModel.statusPopTranslate.value = false
                }
                //更新翻译框状态，若存在则取消，若不存在则跳过此步骤
                if (SuspendViewModel.statusTranslation.value == true && SuspendViewModel.hasTranslation){
                    SuspendViewModel.statusTranslation.value = false
                }
                if (isDragging) {
                    val offsetX = x - lastX
                    val offsetY = y - lastY
                    //重绘视图并且控制其不超出屏幕范围
                    if (rect.left +offsetX >= 0 && rect.top +offsetY >= 0 &&
                        rect.right +offsetX <= SuspendViewModel.screenWidth && rect.bottom +offsetY <= SuspendViewModel.screenHeight){
                        rect.offset(offsetX, offsetY)
                        resizeControlPointRect.offset(offsetX, offsetY)
                        resizeControlPointRect2.offset(offsetX, offsetY)
                        lastX = x
                        lastY = y
                        invalidate()
                    }else{

                    }
                } else if (isRightResizing) {
                    val newRight = rect.right + (x - lastX)
                    val newBottom = rect.bottom + (y - lastY)
                    rect.right = newRight.coerceAtLeast(rect.left + 2 * resizeControlPointRadius)
                    rect.bottom = newBottom.coerceAtLeast(rect.top + 2 * resizeControlPointRadius)
                    resizeControlPointRect.set(
                        rect.right - resizeControlPointRadius,
                        rect.bottom - resizeControlPointRadius,
                        rect.right + resizeControlPointRadius,
                        rect.bottom + resizeControlPointRadius
                    )
                    lastX = x
                    lastY = y
                    invalidate()
                }else if(isLeftResizing){
                    val newLeft = rect.left + (x - lastX)
                    val newTop = rect.top + (y - lastY)
                    rect.left = newLeft.coerceAtMost(rect.right - 2 * resizeControlPointRadius)
                    rect.top = newTop.coerceAtMost(rect.bottom - 2 * resizeControlPointRadius)
                    resizeControlPointRect2.set(
                        rect.left - resizeControlPointRadius,
                        rect.top - resizeControlPointRadius,
                        rect.left + resizeControlPointRadius,
                        rect.top + resizeControlPointRadius
                    )
                    lastX = x
                    lastY = y
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {

                isDragging = false
                isRightResizing = false
                isLeftResizing = false

                //传递悬浮框位置，方便放置翻译框
                SuspendViewModel.left = rect.left
                SuspendViewModel.top = rect.top
                SuspendViewModel.bottom = rect.bottom
                SuspendViewModel.right = rect.right

                //根据rect创建bitmap
                val rootView = SuspendViewModel.screen
                val tmpRect = RectF()
                tmpRect.set(rect.left+25,rect.top +25, rect.right, rect.bottom+50)
                val resBitmap = getBitmapFromRect(rootView!!, tmpRect)
//                Thread.sleep(150)

                //图转文
                val textRecognizer = TextRecognizer.Builder(context).build()
                if (!textRecognizer.isOperational) {
                    Toast.makeText(context, "OCR调用失败", Toast.LENGTH_SHORT).show()
                } else {
                    // Text recognizer is ready for use
                    val frame = Frame.Builder().setBitmap(resBitmap).build()
                    val textBlocks = textRecognizer.detect(frame)

                    val stringBuilder = StringBuilder()
                    for (i in 0 until textBlocks.size()) {
                        val textBlock = textBlocks.valueAt(i)
                        stringBuilder.append(textBlock.value)
                        stringBuilder.append("\n")
                    }

                    val recognizedText = stringBuilder.toString()
                    val source = listOf<String>(recognizedText, "")
                    //先将source存入SuspendViewModel中，点击翻译按钮后再进行翻译
                    SuspendViewModel.source = source
                    //设置SuspendViewModel中的变量，用于弹出确定框
                    SuspendViewModel.popTranslate.value = true
                    SuspendViewModel.statusPopTranslate.value = true


                    Log.d(tag, recognizedText)
                    //Toast只会在应用内起作用
//                    Toast.makeText(context, recognizedText, Toast.LENGTH_SHORT).show()
                }
            }
        }
        return true
    }

    // 在 onActivityResult 中处理权限请求结果
    fun getBitmapFromRect(bitmap: Bitmap, rect: RectF): Bitmap {
        // 将 RectF 转换为整数值，用于截取矩形区域
        val left = rect.left.toInt()
        val top = rect.top.toInt()
        val right = rect.right.toInt()
        val bottom = rect.bottom.toInt()

        // 截取 Bitmap 中的指定区域
        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
    }
}