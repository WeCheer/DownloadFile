package com.wyc.downloadfile.utils

import com.wyc.downloadfile.bean.DownloadInfo

/**
 *作者： wyc
 * <p>
 * 创建时间： 2021/6/29 15:03
 * <p>
 * 文件名字： com.wyc.downloadfile.utils
 * <p>
 * 类的介绍：
 */
interface DownloadCallback {
    fun onDownload(downloadInfo: DownloadInfo?)
}