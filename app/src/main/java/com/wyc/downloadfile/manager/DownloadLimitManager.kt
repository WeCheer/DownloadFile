package com.wyc.downloadfile.manager

import android.text.TextUtils
import com.wyc.downloadfile.bean.DownloadInfo
import com.wyc.downloadfile.config.Constant
import com.wyc.downloadfile.observer.DownloadObserver
import com.wyc.downloadfile.observer.DownloadSubscribe
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
 * 创建时间： 2021/6/29 14:35
 * <p>
 * 文件名字： com.wyc.downloadfile.manager
 * <p>
 * 类的介绍：
 */
class DownloadLimitManager private constructor() : DownloadSubscribe.LimitDownloadCallback {

    private val mClient: OkHttpClient = OkHttpClient.Builder().build()

    //存放各个下载中的请求
    private val mDownloadCalls = HashMap<String, Call>()

    //存放等待下载的请求
    private val mDownloadWait = mutableListOf<String>()

    //同时下载的最大个数
    private var mMaxCount = 2

    private var mCallback: DownloadCallback? = null

    companion object {

        private const val TAG = "DownloadLimitManager"
        private val INSTANCE = AtomicReference<DownloadLimitManager>()

        @JvmStatic
        fun getInstance(): DownloadLimitManager {
            while (true) {
                var current = INSTANCE.get()
                if (current != null) {
                    return current
                }
                current = DownloadLimitManager()
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
        return mDownloadCalls.containsKey(url)
    }

    /**
     * 查看是否在等待任务中
     */
    fun getWaitUrl(url: String?): Boolean {
        for (item in mDownloadWait) {
            if (item == url) {
                return true
            }
        }
        return false
    }

    fun download(url: String?, callback: DownloadCallback?) {
        if (TextUtils.isEmpty(url)) {
            Log.w(TAG, "download url is null")
            return
        }
        Log.d(TAG, "download url = $url")
        this.mCallback = callback
        Observable.just(url)
            .filter {
                val flag = mDownloadCalls.containsKey(it)
                if (flag) {
                    //如果已经在下载，查找下一个文件进行下载
                    downloadNext()
                    false
                } else {
                    //判断如果正在下载的个数达到最大限制，存到等待下载列表中
                    if (mDownloadCalls.size >= mMaxCount) {
                        if (!getWaitUrl(it)) {
                            mDownloadWait.add(it)
                            val info = DownloadInfo(it, DownloadInfo.DOWNLOAD_WAIT)
                            //回调接口
                            mCallback?.onDownload(info)
                        }
                        false
                    } else {
                        true
                    }
                }

            }
            .map {
                //生成DownloadInfo对象
                createDownInfo(it)
            }
            .map {
                //如果已经下载重新命名
                getRealFileName(it)
            }
            .flatMap {
                //下载
                Observable.create(DownloadSubscribe(it, mClient, mDownloadCalls, this))
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(DownloadObserver(mCallback))
    }

    /**
     * 下载等待中的第一条
     */
    private fun downloadNext() {
        if (mDownloadCalls.size < mMaxCount && mDownloadWait.size > 0) {
            download(mDownloadWait[0], mCallback)
            mDownloadWait.removeAt(0)
        }
    }

    /**
     * 构建DownloadInfo
     */
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

    /**
     * 下载取消或暂停
     */
    fun pauseDownload(info: DownloadInfo?) {
        info?.apply {
            Log.d(TAG, "pauseDownload url = $url")
            if (TextUtils.isEmpty(url)) {
                return
            }
            val call = mDownloadCalls[url]
            call?.cancel()
            mDownloadCalls.remove(url)
            downloadStatus = DownloadInfo.DOWNLOAD_PAUSE
            mCallback?.onDownload(info)
            downloadNext()
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

    override fun callback() {
        //回调进行下一个下载任务
        Log.d(TAG, "callback")
        downloadNext()
    }
}