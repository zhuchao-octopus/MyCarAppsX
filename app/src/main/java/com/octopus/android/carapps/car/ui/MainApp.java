package com.octopus.android.carapps.car.ui;

import android.app.Application;
import android.content.Intent;

import com.octopus.android.carapps.car.service.UIService;

public class MainApp extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		startService(new Intent(this, UIService.class));
	}
}
