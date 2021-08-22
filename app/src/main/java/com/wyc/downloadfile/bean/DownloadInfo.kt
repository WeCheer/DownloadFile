package com.wyc.downloadfile.bean

import android.os.Parcel
import android.os.Parcelable

/**
 *作者： wyc
 * <p>
 * 创建时间： 2021/6/28 14:54
 * <p>
 * 文件名字： com.wyc.downloadfile.bean
 * <p>
 * 类的介绍：
 */
data class DownloadInfo constructor(
    var url: String?,
    var downloadStatus: String?,
    var fileName: String?,
    var total: Long = 0L,
    var progress: Long = 0L
) : Parcelable {

    constructor(url: String?, downloadStatus: String) : this(url, downloadStatus, null, 0L, 0L)

    constructor(url: String?) : this(url, null, null, 0L, 0L)

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readLong()) {
    }

    companion object {
        /**
         * 下载状态
         */
        const val DOWNLOAD = "download" // 下载中

        const val DOWNLOAD_PAUSE = "pause" // 下载暂停

        const val DOWNLOAD_WAIT = "wait" // 等待下载

        const val DOWNLOAD_CANCEL = "cancel" // 下载取消

        const val DOWNLOAD_OVER = "over" // 下载结束

        const val DOWNLOAD_ERROR = "error" // 下载出错

        const val TOTAL_ERROR: Long = -1 //获取进度失败

        @JvmField
        val CREATOR = object : Parcelable.Creator<DownloadInfo> {
            override fun createFromParcel(parcel: Parcel): DownloadInfo {
                return DownloadInfo(parcel)
            }

            override fun newArray(size: Int): Array<DownloadInfo?> {
                return arrayOfNulls(size)
            }

        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeString(downloadStatus)
        parcel.writeString(fileName)
        parcel.writeLong(total)
        parcel.writeLong(progress)
    }

    override fun describeContents(): Int {
        return 0
    }

}