package com.example.tongxiwen.photogathertest;

import android.app.Application;

import com.tencent.bugly.Bugly;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        String appID = "2e3bf1811a";
        Bugly.init(getApplicationContext(), appID, true);
    }
}
