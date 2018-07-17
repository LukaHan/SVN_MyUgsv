package com.dzh.ugsv;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.tencent.ugc.TXUGCBase;

public class BaseApplication extends MultiDexApplication {
//    private String ugcKey = "00535820dd2a02f1a2db1dec30e7b040";
//    private String ugcLicenceUrl = "http://license.vod2.myqcloud.com/license/v1/a56cb408e549c339f47dbede98e225ed/TXUgcSDK.licence";
    private String ugcKey = "09bb91939d9ef9669f7ff16a850c92e5";
    private String ugcLicenceUrl = "http://download-1252463788.cossh.myqcloud.com/xiaoshipin/licence_xsp/TXUgcSDK.licence";

    @Override
    public void onCreate() {
        super.onCreate();


        // 短视频licence设置
        TXUGCBase.getInstance().setLicence(this, ugcLicenceUrl, ugcKey);
        String licenseInfo = TXUGCBase.getInstance().getLicenceInfo(this);
        Log.d("luka","licenseInfo,Base:"+licenseInfo);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
