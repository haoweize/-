package com.example.resizableselectionbox.ui.suspendWindow

import android.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.resizableselectionbox.logic.network.SearchBodyData
import com.example.resizableselectionbox.ui.SearchViewModel
import com.example.resizableselectionbox.ui.SuspendViewModel
import com.example.resizableselectionbox.databinding.FragmentMainpageBinding


class FirstFragment : Fragment() {
    val TAG = "FirstFragment"
    //创建搜索相关的viewModel

    private var _binding: FragmentMainpageBinding? = null
    private val binding get() = _binding!!

    //创建媒体投影
    private lateinit var mMediaProjectionManager: MediaProjectionManager



    lateinit var source:String
    lateinit var trans_type:String
    lateinit var suspendIntent:Intent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainpageBinding.inflate(inflater, container, false)


        return binding.root
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode === Activity.RESULT_CANCELED) {
            Log.e(TAG, "User cancel")
        } else {
            try {
                val mWindowManager = context?.getSystemService(Activity.WINDOW_SERVICE) as WindowManager
                val metrics = DisplayMetrics()
                mWindowManager.defaultDisplay.getMetrics(metrics)
            } catch (e: Exception) {
                Log.e(TAG, "MediaProjection error")
            }
            SuspendViewModel.resultCode = resultCode
            SuspendViewModel.intent = data
            val service = Intent(context, SuspendWindowService::class.java)
            service.putExtra("code", resultCode)
            service.putExtra("data", R.attr.data)
            //开启前台服务，在服务中进行截图。
            context?.startService(service)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //开启选词翻译
        binding.btnChose.setOnClickListener {
            mMediaProjectionManager = context?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

            SuspendViewModel.mMpMgr = mMediaProjectionManager
            val captureIntent: Intent = mMediaProjectionManager.createScreenCaptureIntent()
            startActivityForResult(captureIntent, 1)
            // 检查是否已经拥有悬浮窗权限
            if (!Settings.canDrawOverlays(context)) {
                // 如果没有权限，跳转到授权页面
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                startActivity(intent)
            } else {
                suspendIntent = Intent(context, SuspendWindowService::class.java)
                context?.startService(suspendIntent)
                SuspendViewModel.isShowSuspendWindow.value =true
                SuspendViewModel.isVisible.value = true
            }
        }
        Log.d("FirstFragment", "Fragment被创建")

        //点击按钮更换翻译模式，先把方向换成en2zh
        trans_type = "en2zh"
        binding.zh2en.setOnClickListener {
            trans_type = "zh2en"
        }
        binding.en2zh.setOnClickListener {
            trans_type = "en2zh"
        }
        binding.searchBtn.setOnClickListener {
            val text = binding.editText.text.toString()
            val source = listOf<String>(text, "")
            val searchBodyData = SearchBodyData(source, trans_type)
            SearchViewModel.searchTranslation(searchBodyData)
            SearchViewModel.translateLiveData.observe(viewLifecycleOwner, Observer {
                binding.showInfo.text = it.getOrNull() ?: ""
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        Log.d(TAG, "destroyview被调用")
    }
    override fun onDestroy() {
        super.onDestroy()
//        if(suspendIntent != null)
//            context?.stopService(suspendIntent)
        Log.d("FirstFragment", "Fragment被摧毁，且Service被关闭")
    }
}