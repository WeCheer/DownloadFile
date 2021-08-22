package com.wyc.downloadfile.utils

import java.io.Closeable
import java.io.File
import java.io.IOException

/**
 *作者： wyc
 * <p>
 * 创建时间： 2021/6/29 10:25
 * <p>
 * 文件名字： com.wyc.downloadfile.utils
 * <p>
 * 类的介绍：
 */
object IOUtils {

    private const val TAG = "IOUtils"

    @JvmStatic
    fun closeAll(vararg closeables: Closeable?) {
        for (closeable in closeables) {
            if (closeable != null) {
                try {
                    closeable.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @JvmStatic
    fun deleteFile(file: File) {
        deleteFileImpl(file)
    }

    @JvmStatic
    fun deleteFile(filePath: String) {
        deleteFile(File(filePath))
    }

    private fun deleteFileImpl(file: File) {
        if (file.exists()) {
            if (file.isFile) {
                Log.i(TAG, "[deleteFileImpl] delete file(" + file.absolutePath + ") succeed ? "
                        + file.delete())
            } else if (file.isDirectory) {
                val files = file.listFiles()
                if (files != null) {
                    for (file1 in files) {
                        deleteFile(file1)
                    }
                }
                Log.i(TAG, "[deleteFileImpl] delete file(" + file.absolutePath + ") succeed ? "
                        + file.delete())
            }
        } else {
            Log.w(TAG, "file not exists")
        }
    }
}