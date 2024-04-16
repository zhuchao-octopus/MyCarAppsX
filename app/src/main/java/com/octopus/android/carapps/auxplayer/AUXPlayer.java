package com.octopus.android.carapps.auxplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.utils.ResourceUtil;

public class AUXPlayer extends Activity {

    AuxInUI mUI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        boolean multiWindow = ResourceUtil.updateAppUi(this);
        if (multiWindow) {
            GlobalDef.updateMultiWindownActivity(this);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aux_player);

        mUI = AuxInUI.getInstanse(this, findViewById(R.id.screen1_main), 0);
        mUI.onCreate();
    }

    @Override
    protected void onNewIntent(Intent it) {
        setIntent(it);
    }

    WakeLock mWakeLock;

    @Override
    protected void onStart() {
        super.onStart();

        if (mUI != null) mUI.onResume();

        GlobalDef.openGps(this, getIntent());
        setIntent(null);
        PowerManager pManager = ((PowerManager) getSystemService(POWER_SERVICE));
        mWakeLock = pManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, this.getPackageName());
        mWakeLock.acquire();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mUI != null) mUI.onPause();

        if (null != mWakeLock) {
            mWakeLock.release();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUI != null) mUI.onDestroy();

        if (GlobalDef.mSource == AuxInUI.SOURCE) {
            BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mUI != null) {
            mUI.mWillDestory = true;
        }
        BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
    }
    //	@Override
    //	public boolean onKeyDown(int keyCode, KeyEvent event) {
    //		switch (keyCode) {
    //
    //		case KeyEvent.KEYCODE_BACK: {
    //			if (mUI != null) {
    //				mUI.mWillDestory = true;
    //			}
    //			BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
    //			break;
    //		}
    //
    //		}
    //		return super.onKeyDown(keyCode, event);
    //	}
}