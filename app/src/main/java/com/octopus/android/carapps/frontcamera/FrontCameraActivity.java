package com.octopus.android.carapps.frontcamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.octopus.android.carapps.R;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.utils.ResourceUtil;

public class FrontCameraActivity extends Activity {

	FrontCameraUI mUI;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		boolean multiWindow = ResourceUtil.updateAppUi(this);
		if (multiWindow) {
			GlobalDef.updateMultiWindownActivity(this);
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.front_camera);

		
		
		mUI = FrontCameraUI.getInstanse(this, findViewById(R.id.screen1_main), 0);
		updateIntent(this.getIntent());
		mUI.onCreate();		 
		
	}

	private void updateIntent(Intent it) {
		if (it != null) {
			int page = it.getIntExtra("camera", 0);
			if (page != 0) {
				mUI.mCameraIndex = page;
			}
			page = it.getIntExtra("finish", 0);
			if (page == 1){
				finish();
			}
			
			page = it.getIntExtra("style", 0);
			if (page != 0 && mUI != null) {
				mUI.mStyle = page;
			}
			
//			if ("com.my.frontcamera.BackCameraActivity".equals(
//					it.getComponent().getClassName())){
//				mUI.mCameraIndex = 1;
//			}
		}
	}

	@Override
	protected void onNewIntent(Intent it) {
		updateIntent(it);
		setIntent(it);
	}
	WakeLock mWakeLock;
	FrontCameraService mFrontCameraService;
	@Override
	protected void onStart() {
		super.onStart();
		mFrontCameraService = FrontCameraService.getInstanse(this);
		
		if (mUI != null)
			mUI.onResume();
		
		PowerManager pManager = ((PowerManager) getSystemService(POWER_SERVICE));
		mWakeLock = pManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
				| PowerManager.ON_AFTER_RELEASE, this.getPackageName());
		mWakeLock.acquire();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mUI != null)
			mUI.onPause();

		if (mFrontCameraService != null){
			mFrontCameraService.release();
		}
		
		if (null != mWakeLock) {
            mWakeLock.release();
        }

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mUI != null)
			mUI.onDestroy();

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (mUI != null) {
			mUI.mWillDestory = true;
		}
	}
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		switch (keyCode) {
//
//		case KeyEvent.KEYCODE_BACK: {
//			if (mUI != null) {
//				mUI.mWillDestory = true;
//			}
////			BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
//			break;
//		}
//
//		}
//		return super.onKeyDown(keyCode, event);
//	}
}