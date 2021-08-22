package com.wyc.downloadfile

import android.app.Application
import android.content.Context

/**
 *作者： wyc
 * <p>
 * 创建时间： 2021/6/28 17:59
 * <p>
 * 文件名字： com.wyc.downloadfile
 * <p>
 * 类的介绍：
 */
class App : Application() {

    companion object {
        private var sInstance: App? = null
        private var sContent: Context? = null

        @JvmStatic
        fun getInstance(): App {
            return sInstance!!
        }

        @JvmStatic
        fun getContext(): Context {
            return sContent!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        sInstance = this
        sContent = this.applicationContext
    }

}