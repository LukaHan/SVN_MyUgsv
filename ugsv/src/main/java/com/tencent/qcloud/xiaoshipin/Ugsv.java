package com.tencent.qcloud.xiaoshipin;

import android.app.Application;

import com.tencent.qcloud.xiaoshipin.common.utils.TCHttpEngine;
import com.tencent.qcloud.xiaoshipin.login.TCUserMgr;
import com.tencent.ugc.TXUGCBase;

/**
 * 小视频应用类，用于全局的操作，如
 * sdk初始化,全局提示框
 */
public class Ugsv {
    private static Application instance;

    private static String ugcKey = "73b0df85d8449c2c9289e76117ba7df1";
    private static String ugcLicenceUrl = "http://license.vod2.myqcloud.com/license/v1/c0de286aba01c989cefd54b66d75c168/TXUgcSDK.licence";

    public static void init(Application application) {
        instance = application;
        TCUserMgr.getInstance().initContext(instance);
        TCHttpEngine.getInstance().initContext(instance);
        // 短视频licence设置
        TXUGCBase.getInstance().setLicence(instance, ugcLicenceUrl, ugcKey);
    }

    public static Application getApplication() {
        return instance;
    }

    public static void setUserInfo(String userid, String nickname, String headpic) {
        TCUserMgr.getInstance().setUserId(userid);
        TCUserMgr.getInstance().setNickName(nickname, null);
        TCUserMgr.getInstance().setHeadPic(headpic, null);
    }
}
