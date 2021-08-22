package com.wyc.downloadfile.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.wyc.downloadfile.R
import com.wyc.downloadfile.adapter.MultipleDownloadAdapter
import com.wyc.downloadfile.bean.DownloadInfo
import com.wyc.downloadfile.config.Constant
import kotlinx.android.synthetic.main.activity_multiple_activity.*

class MultipleActivity : AppCompatActivity() {

    private var mAdapter: MultipleDownloadAdapter? = null
    private val mDatas = mutableListOf<DownloadInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_activity)
        getDatas()

        mAdapter = MultipleDownloadAdapter(mDatas)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = mAdapter
        //取消item刷新动画
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    private fun getDatas() {
        mDatas.add(DownloadInfo(Constant.URL_1))
        mDatas.add(DownloadInfo(Constant.URL_2))
        mDatas.add(DownloadInfo(Constant.URL_3))
        mDatas.add(DownloadInfo(Constant.URL_4))
        mDatas.add(DownloadInfo(Constant.URL_5))
        mDatas.add(DownloadInfo(Constant.URL_6))
        mDatas.add(DownloadInfo(Constant.URL_7))
        mDatas.add(DownloadInfo(Constant.URL_8))
        mDatas.add(DownloadInfo(Constant.URL_9))
    }
}
