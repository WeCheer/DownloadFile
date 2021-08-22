package com.wyc.downloadfile.observer

import com.wyc.downloadfile.bean.DownloadInfo
import com.wyc.downloadfile.config.Constant
import com.wyc.downloadfile.utils.IOUtils
import com.wyc.downloadfile.utils.Log
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.cacheGet
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 *作者： wyc
 * <p>
 * 创建时间： 2021/6/28 18:40
 * <p>
 * 文件名字： com.wyc.downloadfile.observer
 * <p>
 * 类的介绍：
 */
class DownloadSubscribe @JvmOverloads constructor(private var mDownloadInfo: DownloadInfo,
                                                  private var mClient: OkHttpClient,
                                                  private var mDownCalls: HashMap<String, Call>,
                                                  private var mCallback: LimitDownloadCallback? = null
) : ObservableOnSubscribe<DownloadInfo> {

    private companion object {
        private const val TAG = "DownloadSubscribe"
    }

    interface LimitDownloadCallback {
        fun callback()
    }

    override fun subscribe(emitter: ObservableEmitter<DownloadInfo>) {
        val url = mDownloadInfo.url
        //已下载长度
        val downloadLength = mDownloadInfo.progress
        //文件总长度
        val contentLength = mDownloadInfo.total
        emitter.onNext(mDownloadInfo)
        if (url == null) {
            return
        }
        val request = Request.Builder()
            .addHeader("RANGE", "bytes=$downloadLength-$contentLength")
            .url(url)
            .build()
        val call = mClient.newCall(request)
        //把这个添加到call里,方便取消
        mDownCalls[url] = call
        val response = call.execute()
        val file = File(Constant.FILE_PATH, mDownloadInfo.fileName!!)
        saveFile(response, file, downloadLength, url, emitter)
        //限制下载个数需要回调进行下载另一个操作
        mCallback?.callback()

        //如果下载长度和文件长度相等，即为下载完成
        if (downloadLength == contentLength) {
            Log.d(TAG, "download complete")
            emitter.onComplete()
        }
    }

    private fun saveFile(response: Response, file: File, downloadLength: Long, url: String, emitter: ObservableEmitter<DownloadInfo>) {
        var currentLength = downloadLength
        var inputStream: InputStream? = null
        var fileOutputStream: FileOutputStream? = null

        try {
            inputStream = response.body?.byteStream()
            fileOutputStream = FileOutputStream(file, true)
            val buffer = ByteArray(2048)
            var len: Int
            if (inputStream == null) {
                Log.w(TAG, "inputStream is null")
                return
            }
            while (inputStream.read(buffer).also { len = it } != -1) {
                fileOutputStream.write(buffer, 0, len)
                currentLength += len.toLong()
                mDownloadInfo.progress = currentLength
                emitter.onNext(mDownloadInfo)
            }
            fileOutputStream.flush()
            mDownCalls.remove(url)
        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
        } finally {
            IOUtils.closeAll(inputStream, fileOutputStream)
        }
    }

}