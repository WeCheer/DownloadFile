package com.wyc.downloadfile.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.wyc.downloadfile.R
import com.wyc.downloadfile.bean.DownloadInfo
import com.wyc.downloadfile.observer.DownloadObserver
import com.wyc.downloadfile.config.Constant
import com.wyc.downloadfile.manager.DownloadManager
import com.wyc.downloadfile.utils.DownloadCallback
import kotlinx.android.synthetic.main.activity_single.*

class SingleActivity : AppCompatActivity() {

    private var mDownloadInfo: DownloadInfo? = null
    private val TAG = "SingleActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single)
        main_btn_down.setOnClickListener {
            DownloadManager.getInstance().download(Constant.URL_1, mDownloadCallback)
        }

        main_btn_pause.setOnClickListener {
            DownloadManager.getInstance().pauseDownload(mDownloadInfo)
        }

        main_btn_cancel.setOnClickListener {
            DownloadManager.getInstance().cancelDownload(mDownloadInfo)
        }
    }

    private val mDownloadCallback = object : DownloadCallback {
        override fun onDownload(downloadInfo: DownloadInfo?) {
            if (downloadInfo == null) {
                return
            }
            if (downloadInfo.url != Constant.URL_1) {
                return
            }
            when (downloadInfo.downloadStatus) {
                DownloadInfo.DOWNLOAD -> {
                    mDownloadInfo = downloadInfo
                    if (downloadInfo.total == 0L) {
                        main_progress.progress = 0
                    } else {
                        val progress = downloadInfo.progress * main_progress.max / downloadInfo.total
                        main_progress.progress = progress.toInt()
                    }
                }
                DownloadInfo.DOWNLOAD_OVER -> {
                    main_progress.progress = main_progress.max
                    Toast.makeText(this@SingleActivity, "下载完成", Toast.LENGTH_SHORT).show()
                }
                DownloadInfo.DOWNLOAD_PAUSE -> {
                    Toast.makeText(this@SingleActivity, "下载暂停", Toast.LENGTH_SHORT).show()
                }
                DownloadInfo.DOWNLOAD_CANCEL -> {
                    main_progress.progress = 0
                    Toast.makeText(this@SingleActivity, "下载取消", Toast.LENGTH_SHORT).show()
                }
                DownloadInfo.DOWNLOAD_ERROR -> {
                    Toast.makeText(this@SingleActivity, "下载出错", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
