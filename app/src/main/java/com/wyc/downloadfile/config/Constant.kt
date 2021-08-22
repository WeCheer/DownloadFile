package com.wyc.downloadfile.config

import com.wyc.downloadfile.App

/**
 *作者： wyc
 * <p>
 * 创建时间： 2021/6/28 15:12
 * <p>
 * 文件名字： com.wyc.downloadfile.config
 * <p>
 * 类的介绍：
 */
object Constant {
    /**
     * 下载路径
     */
    val FILE_PATH: String = App.getContext().getExternalFilesDir("")!!.absolutePath

    /**
     * 若文件下载不下来，更换网址
     */
    const val URL_1 = "http://files.ibaodian.com/v2/teamfile/1ca447a600580cdcb575ab9348536f38/CM10086_android_V4.8.0_20180708_A0001.apk"
    const val URL_2 = "http://files.ibaodian.com/v2/teamfile/f063d3c2c4a32a8143fc4f36be39cfd9/jtyh.patch"
    const val URL_3 = "http://files.ibaodian.com/v2/teamfile/482fd8d425d25f3c3fbdb83156a85af1/IMG_20160508_184212.jpg"
    const val URL_4 = "http://files.ibaodian.com/v2/teamfile/c0ab1e924a99738a268c137f60f3a6db/IMG_20160525_115133.jpg"
    const val URL_5 = "http://files.ibaodian.com/v2/teamfile/5fe13f1385a0112fb75fceed364088e7/IMG_20180818_132629.jpg"
    const val URL_6 = "http://files.ibaodian.com/v2/teamfile/da43a96fde668d4c3fd6f89b8da7e20c/5b726910e7bce766b218d0ee.jpg"
    const val URL_7 = "http://files.ibaodian.com/v2/teamfile/ac43a96d0f21e83cd3967e60e6775d1d/sf_updata.apk"
    const val URL_8 = "http://files.ibaodian.com/v2/teamfile/长城金禧利年金保险菁华版（A计划）.pdf"
    const val URL_9 = "http://files.ibaodian.com/v2/teamfile/2b1d7f518fbcf467ec9bf748743bea80/D90B2EA927372212B33BB673318AA1A1361024EB20B8493A9B23E8178DF3D001"
}