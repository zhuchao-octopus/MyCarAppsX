package com.octopus.android.carapps.btmusic;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.common.util.BroadcastUtil;
import com.common.util.Kernel;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.common.util.UtilCarKey;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.common.ui.GlobalDef;
import com.octopus.android.carapps.common.utils.ResourceUtil;

import java.util.Objects;

public class BTMusicActivity extends Activity {

    BTMusicUI mUI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ResourceUtil.updateAppUi(this);
        boolean multiWindow = ResourceUtil.isMultiWindow(this);
        if (multiWindow) {
            GlobalDef.updateMultiWindowActivity(this);
        }
        super.onCreate(savedInstanceState);

        if (UtilCarKey.mBtMusicInBTapk) {
            UtilCarKey.doKeyBTMusicInBT(this);
            finish();
            return;
        }

        setContentView(R.layout.screen0_bt_music);
        mUI = BTMusicUI.getInstanse(this, findViewById(R.id.screen1_main), 0);
        mUI.onCreate();
        mThis = this;
    }

    @Override
    protected void onNewIntent(Intent it) {
        setIntent(it);
    }

    public boolean mUpdateUIResource = false;

    @Override
    protected void onStart() {
        if (mUpdateUIResource) {
            ResourceUtil.updateAppUi(this);
            mUpdateUIResource = false;
        }
        super.onStart();
        if (mUI != null) mUI.onResume();
        BTMusicService.mGpsRunAfter = GlobalDef.openGps(this, getIntent());
        setIntent(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mUI != null) mUI.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(recreateRunnable);
        if (mUI != null) mUI.onDestroy();

        if (UtilCarKey.mBtMusicInBTapk) {
            return;
        }

        if (mThis == this) {
            mThis = null;
        }

        if (GlobalDef.mSource == BTMusicUI.SOURCE) {
            BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
        }

        // BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mUI != null) {
            mUI.mWillDestory = true;
        }
        if (GlobalDef.mSource == MyCmd.SOURCE_BT_MUSIC /*
         * && BTMusicService.
         * mPlayStatus >=
         * BTMusicService
         * .A2DP_INFO_CONNECTED
         */) {
            BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Util.isRKSystem()) {
            return super.onKeyDown(keyCode, event);
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_F7:
                return rollKeyTranslate2(MyCmd.Keycode.KEY_ROOL_LEFT);
            case KeyEvent.KEYCODE_F8:
                return rollKeyTranslate2(MyCmd.Keycode.KEY_ROOL_RIGHT);
            case KeyEvent.KEYCODE_TAB:
                return rollKeyTranslate(event.isShiftPressed() ? MyCmd.Keycode.KEY_ROOL_LEFT : MyCmd.Keycode.KEY_ROOL_RIGHT);
            default:
                return super.onKeyDown(keyCode, event);
        }
        // return true;
    }

    private boolean rollKeyTranslate2(int keyCode) {
        boolean ret = false;
        View v = getCurrentFocus();
        int id = 0;
        if (v != null) {
            id = v.getId();
        }
        if (id != 0) {
            // isNeedToRoll(v.getId(), keyCode);
            int key = 0;

            switch (keyCode) {
                case MyCmd.Keycode.KEY_ROOL_RIGHT:
                    key = Kernel.KEY_RIGHT;

                    break;
                case MyCmd.Keycode.KEY_ROOL_LEFT:
                    key = Kernel.KEY_LEFT;
                    break;
            }

            if (key != 0) {
                if (key < Kernel.KEY_MAX) {
                    Kernel.doKeyEvent(key);
                    ret = true;
                }
            }
        } else {
            findViewById(R.id.prev).requestFocus();
            findViewById(R.id.prev).requestFocusFromTouch();
        }

        return ret;

    }

    private boolean rollKeyTranslate(int keyCode) {
        boolean ret = false;
        View v = getCurrentFocus();

        int nextFocus = 0;
        if (v != null) {
            int id = v.getId();// isNeedToRoll(v.getId(), keyCode);
            int key = 0;

            switch (keyCode) {
                case MyCmd.Keycode.KEY_ROOL_RIGHT:
                    if (id == R.id.bt) {
                        key = Kernel.KEY_MAX;
                    }
                    break;
                case MyCmd.Keycode.KEY_ROOL_LEFT:
                    if (id == R.id.prev) {
                        key = Kernel.KEY_MAX;
                    }
                    break;
            }

            if (key != 0) {
                if (key < Kernel.KEY_MAX) {
                    Kernel.doKeyEvent(key);
                    ret = true;
                }
            } else if (nextFocus != 0) {
                v.clearFocus();
                findViewById(nextFocus).requestFocus();
                ret = true;
            }
        }

        return ret;

    }

    private static BTMusicActivity mThis;

    public static void updateBySystemConfig(boolean f) {
        if (mThis != null) {
            ResourceUtil.updateAppUi(mThis);

            //			boolean restart = false;
            //			if (mThis.mUI != null)
            //				restart = !mThis.mUI.mPause;
            //
            if (f) {
                mThis.finish();
            } else {
                if (mThis.mUI != null) {
                    mThis.mUpdateUIResource = mThis.mUI.mPause;
                }
            }
            //			if(restart){
            //				UtilSystem.doRunActivity(mThis, "com.car.ui", "com.my.dvd.DVDPlayer");
            //			}
        }
    }

    private MotionEvent mMotionEventDelayed = null;
    private final Handler mHandler = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        @Override
        public void handleMessage(Message msg) {
            if (mMotionEventDelayed != null) {
                dispatchTouchEvent(mMotionEventDelayed);
            }
        }
    };

    private boolean mFor3Finger = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        if (GlobalDef.mTouch3Switch) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                if (mMotionEventDelayed == null) {
                    mMotionEventDelayed = ev;//.copy();
                    mHandler.removeMessages(0);
                    mHandler.sendEmptyMessageDelayed(0, 60);
                    return true;
                } else {
                    mMotionEventDelayed = null;
                }
            } else if (ev.getAction() == MotionEvent.ACTION_POINTER_3_DOWN) {
                mHandler.removeMessages(0);
                mFor3Finger = true;
                ev.setAction(MotionEvent.ACTION_UP);
                return super.dispatchTouchEvent(ev);
            } else if (ev.getAction() == MotionEvent.ACTION_UP) {
                mMotionEventDelayed = null;
                mHandler.removeMessages(0);
                if (mFor3Finger) {
                    mFor3Finger = false;
                    return true;
                }
            }

            if (mFor3Finger || mMotionEventDelayed != null) {
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeConfiguration = getResources().getConfiguration();
    }

    private Configuration resumeConfiguration = null;
    private int recreateScreenWidthDp = 0;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Log.d("BTMusicActivity", "onConfigurationChanged\n" + newConfig + "\n" + resumeConfiguration);
        if (resumeConfiguration != null && newConfig != null && resumeConfiguration.screenWidthDp != newConfig.screenWidthDp) {
            Log.w("BTMusicActivity", "onConfigurationChanged restart activity " + resumeConfiguration.screenWidthDp + ", " + newConfig.screenWidthDp + ", " + recreateScreenWidthDp + ", " + ResourceUtil.isMultiWindow(this));
            if (recreateScreenWidthDp != newConfig.screenWidthDp && !ResourceUtil.isMultiWindow(this)) {
                mHandler.postDelayed(recreateRunnable, 500);
            }
            recreateScreenWidthDp = newConfig.screenWidthDp;
        }
    }

    private final Runnable recreateRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("BTMusicActivity", "do recreate");
            recreate();
        }
    };
}
