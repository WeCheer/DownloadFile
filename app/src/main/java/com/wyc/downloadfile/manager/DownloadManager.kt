package com.wyc.downloadfile.manager

import android.text.TextUtils
import com.wyc.downloadfile.bean.DownloadInfo
import com.wyc.downloadfile.observer.DownloadObserver
import com.wyc.downloadfile.observer.DownloadSubscribe
import com.wyc.downloadfile.config.Constant
import com.wyc.downloadfile.utils.DownloadCallback
import com.wyc.downloadfile.utils.IOUtils
import com.wyc.downloadfile.utils.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.atomic.AtomicReference


/**
 *作者： wyc
 * <p>
 * 创建时间： 2021/6/28 14:42
 * <p>
 * 文件名字： com.wyc.downloadfile.manager
 * <p>
 * 类的介绍：
 */
class DownloadManager private constructor() {

    private var mClient: OkHttpClient = OkHttpClient.Builder().build()
    private var mDownCalls = HashMap<String, Call>()
    private var mCallback: DownloadCallback? = null

    companion object {

        private const val TAG = "DownloadManager"
        private val INSTANCE = AtomicReference<DownloadManager>()

        @JvmStatic
        fun getInstance(): DownloadManager {
            while (true) {
                var current = INSTANCE.get()
                if (current != null) {
                    return current
                }
                current = DownloadManager()
                if (INSTANCE.compareAndSet(null, current)) {
                    return current
                }
            }
        }
    }

    /**
     * 查看是否在下载任务中
     */
    fun getDownloadUrl(url: String?): Boolean {
        Log.d(TAG, "getDownloadUrl url = $url")
        if (TextUtils.isEmpty(url)) {
            return false
        }
        return mDownCalls.containsKey(url)
    }

    fun download(url: String?, callback: DownloadCallback) {
        if (TextUtils.isEmpty(url)) {
            Log.w(TAG, "download url is null")
            return
        }
        mCallback = callback
        Observable.just(url)
            .filter {
                !mDownCalls.containsKey(it)
            }
            .map {
                createDownInfo(it)
            }
            .map {
                getRealFileName(it)
            }
            .flatMap {
                Observable.create(DownloadSubscribe(it, mClient, mDownCalls))
            }
            .subscribeOn(Schedulers.io())//子线程执行
            .observeOn(AndroidSchedulers.mainThread())//主线程回调
            .subscribe(DownloadObserver(callback))//添加观察者，监听下载进度
    }

    /**
     * 下载取消或暂停
     */
    fun pauseDownload(info: DownloadInfo?) {
        info?.apply {
            Log.d(TAG, "pauseDownload url = $url")
            if (TextUtils.isEmpty(url)) {
                return
            }
            val call = mDownCalls[url]
            call?.cancel()
            mDownCalls.remove(url)
            downloadStatus = DownloadInfo.DOWNLOAD_PAUSE
            mCallback?.onDownload(info)
        }
    }

    /**
     * 取消下载，并删除本地文件
     */
    fun cancelDownload(info: DownloadInfo?) {
        info?.apply {
            pauseDownload(info)
            progress = 0
            downloadStatus = DownloadInfo.DOWNLOAD_CANCEL
            mCallback?.onDownload(info)
            fileName?.let {
                val file = File(Constant.FILE_PATH, it)
                IOUtils.deleteFile(file)
            }
        }
    }


    private fun createDownInfo(url: String): DownloadInfo {
        val downloadInfo = DownloadInfo(url)
        //获取文件大小
        val contentLength = getContentLength(url)
        downloadInfo.total = contentLength
        val fileName = url.substring(url.lastIndexOf("/"))
        downloadInfo.fileName = fileName
        return downloadInfo
    }

    private fun getContentLength(downloadUrl: String): Long {
        val request = Request.Builder()
            .url(downloadUrl)
            .build()

        try {
            val response = mClient.newCall(request).execute()
            if (response.isSuccessful) {
                val contentLength = response.body?.contentLength() ?: 0
                response.close()
                return if (contentLength == 0L) DownloadInfo.TOTAL_ERROR else contentLength
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
        }
        return DownloadInfo.TOTAL_ERROR
    }

    /**
     * 如果文件已下载重新命名新的文件名
     * */
    private fun getRealFileName(downloadInfo: DownloadInfo): DownloadInfo {
        val fileName = downloadInfo.fileName
        var downloadLength = 0L
        val contentLength = downloadInfo.total
        val path = File(Constant.FILE_PATH)
        if (!path.exists()) {
            path.mkdirs()
        }
        fileName?.let {
            var file = File(Constant.FILE_PATH, it)
            if (file.exists()) {
                downloadLength = file.length()
            }
            //之前下载过，需要重新来一个文件
            var i = 1
            while (downloadLength >= contentLength) {
                val dotIndex = it.lastIndexOf(".")
                val fileNameOther = if (dotIndex == -1) {
                    "$it($i)"
                } else {
                    it.substring(0, dotIndex) + "(" + i + ")" + it.substring(dotIndex)
                }
                val newFile = File(Constant.FILE_PATH, fileNameOther)
                file = newFile
                downloadLength = newFile.length()
                i++
            }
            //设置改变过的文件名
            downloadInfo.progress = downloadLength
            downloadInfo.fileName = file.name
        }
        return downloadInfo
    }

}


