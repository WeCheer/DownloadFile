package com.wyc.downloadfile.observer

import android.util.Log
import com.wyc.downloadfile.bean.DownloadInfo
import com.wyc.downloadfile.manager.DownloadManager
import com.wyc.downloadfile.utils.DownloadCallback
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 *作者： wyc
 * <p>
 * 创建时间： 2021/6/28 15:08
 * <p>
 * 文件名字： com.wyc.downloadfile.observer
 * <p>
 * 类的介绍：
 */
class DownloadObserver(private var mCallback: DownloadCallback?) : Observer<DownloadInfo> {

    private companion object {
        private const val TAG = "DownloadObserver"
        private var mDisposable: Disposable? = null
        private var mDownloadInfo: DownloadInfo? = null
    }

    override fun onSubscribe(d: Disposable) {
        mDisposable = d
    }

    override fun onNext(t: DownloadInfo) {
        mDownloadInfo = t
        mDownloadInfo?.downloadStatus = DownloadInfo.DOWNLOAD
        mCallback?.onDownload(mDownloadInfo)
    }

    override fun onError(e: Throwable) {
        Log.e(TAG, "onError", e)
        mDownloadInfo?.let {
            if (DownloadManager.getInstance().getDownloadUrl(it.url)) {
                DownloadManager.getInstance().pauseDownload(it)
                it.downloadStatus = DownloadInfo.DOWNLOAD_ERROR
                mCallback?.onDownload(mDownloadInfo)
            }
        }

    }

    override fun onComplete() {
        Log.d(TAG, "onComplete")
        mDownloadInfo?.downloadStatus = DownloadInfo.DOWNLOAD_OVER
        mCallback?.onDownload(mDownloadInfo)
    }
}