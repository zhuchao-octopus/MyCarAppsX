package com.octopus.android.carapps.car.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BR extends BroadcastReceiver {

	public static long mBootTime = 0;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
//			Intent it = new Intent(Intent.ACTION_RUN);
//			it.setClass(context, UIService.class);
//			context.startService(it);
//			Log.d("allen", "CarUI BroadcastReceiver");
			mBootTime = System.currentTimeMillis();
		}
	}

}
