package com.octopus.android.carapps.video;

import android.app.Application;
import android.content.Intent;

public class MainApp extends Application {
    @Override
    public void onCreate() {

        startService(new Intent(this, VideoService.class));
    }
}
