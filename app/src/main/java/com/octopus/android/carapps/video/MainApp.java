package com.octopus.android.carapps.video;

import android.app.Application;
import android.content.Intent;

import com.zhuchao.android.fbase.MMLog;

public class MainApp extends Application {
    private static final String TAG = "MainApplication";
    @Override
    public void onCreate() {
        super.onCreate();
        MMLog.i(TAG, "MainApplication.onCreate() and start VideoService");
        startService(new Intent(this, VideoService.class));
    }
}
