package com.wyc.downloadfile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.wyc.downloadfile.App
import com.wyc.downloadfile.R
import com.wyc.downloadfile.bean.DownloadInfo
import com.wyc.downloadfile.manager.DownloadLimitManager
import com.wyc.downloadfile.utils.DownloadCallback
import com.wyc.downloadfile.utils.Log

/**
 *作者： wyc
 * <p>
 * 创建时间： 2021/6/29 14:32
 * <p>
 * 文件名字： com.wyc.downloadfile.adapter
 * <p>
 * 类的介绍：
 */
class LimitDownloadAdapter(private var mDatas: MutableList<DownloadInfo>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG = "LimitDownloadAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_download_layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val info = mDatas[position]
        if (holder is ViewHolder) {
            if (DownloadLimitManager.getInstance().getWaitUrl(info.url)) {
                holder.mainBtnDown.text = "等待"
            } else {
                when (info.downloadStatus) {
                    DownloadInfo.DOWNLOAD_CANCEL -> {
                        holder.mainProgress.progress = 0
                        holder.mainBtnDown.text = "下载"
                    }
                    DownloadInfo.DOWNLOAD_PAUSE -> {
                        holder.mainBtnDown.text = "等待"
                    }
                    DownloadInfo.DOWNLOAD_OVER -> {
                        holder.mainProgress.progress = holder.mainProgress.max
                        holder.mainBtnDown.text = "完成"
                    }
                    else -> {
                        if (info.total == 0L) {
                            holder.mainProgress.progress = 0
                        } else {
                            val progress = info.progress * holder.mainProgress.max / info.total
                            holder.mainProgress.progress = progress.toInt()
                            holder.mainBtnDown.text = "下载中"
                        }
                    }
                }
            }

            holder.mainBtnDown.setOnClickListener {
                DownloadLimitManager.getInstance().download(info.url, mDownloadCallback)
            }

            holder.mainBtnPause.setOnClickListener {
                DownloadLimitManager.getInstance().pauseDownload(info)
            }
            holder.mainBtnCancel.setOnClickListener {
                DownloadLimitManager.getInstance().cancelDownload(info)
            }
        }
    }

    override fun getItemCount(): Int {
        return mDatas.size
    }

    private fun updateProgress(info: DownloadInfo) {
        mDatas.forEachIndexed { index, downloadInfo ->
            if (downloadInfo.url == info.url) {
                mDatas[index] = info
                notifyItemChanged(index)
                return@forEachIndexed
            }
        }
    }

    private val mDownloadCallback = object : DownloadCallback {
        override fun onDownload(downloadInfo: DownloadInfo?) {
            if (downloadInfo == null) {
                return
            }
            when (downloadInfo.downloadStatus) {
                DownloadInfo.DOWNLOAD -> {
                    updateProgress(downloadInfo)
                }
                DownloadInfo.DOWNLOAD_OVER -> {
                    Log.d(TAG, "下载完成--${downloadInfo.fileName}")
                    updateProgress(downloadInfo)
                }
                DownloadInfo.DOWNLOAD_PAUSE -> {
                    Log.d(TAG, "下载暂停--${downloadInfo.fileName}")
                    Toast.makeText(App.getContext(), "下载暂停--${downloadInfo.fileName}", Toast.LENGTH_SHORT).show()
                }
                DownloadInfo.DOWNLOAD_CANCEL -> {
                    Log.d(TAG, "下载取消--${downloadInfo.fileName}")
                    updateProgress(downloadInfo)
                    Toast.makeText(App.getContext(), "下载取消--${downloadInfo.fileName}", Toast.LENGTH_SHORT).show()
                }
                DownloadInfo.DOWNLOAD_ERROR -> {
                    Log.d(TAG, "下载出错--${downloadInfo.fileName}")
                    Toast.makeText(App.getContext(), "下载出错--${downloadInfo.fileName}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainProgress: ProgressBar = itemView.findViewById(R.id.main_progress)
        val mainBtnDown: Button = itemView.findViewById(R.id.main_btn_down)
        val mainBtnPause: Button = itemView.findViewById(R.id.main_btn_pause)
        val mainBtnCancel: Button = itemView.findViewById(R.id.main_btn_cancel)
    }
}