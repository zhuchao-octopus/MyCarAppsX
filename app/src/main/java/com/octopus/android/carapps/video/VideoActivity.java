/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.octopus.android.carapps.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.common.util.BroadcastUtil;
import com.common.util.Kernel;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.adapter.MyListViewAdapter;
import com.octopus.android.carapps.common.player.ComMediaPlayer;
import com.octopus.android.carapps.common.ui.GlobalDef;
import com.octopus.android.carapps.common.utils.ResourceUtil;
import com.zhuchao.android.fbase.MMLog;

import java.util.Objects;

/**
 * This activity plays a video from a specified URI.
 */

public class VideoActivity extends Activity {
    private static final String TAG = "VideoActivity";
    private VideoUI mVideoUI;
    private static VideoActivity mThis;
    private WakeLock mWakeLock;
    private MotionEvent mMotionEventDelayed = null;
    private boolean mFor3Finger = false;
    public boolean mUpdateUIResource = false;
    private ComMediaPlayer mMediaPlayer;

    private final int[] LIST_ID = new int[]{
            R.id.tv_all_list, R.id.tv_sd_list, R.id.tv_sd2_list, R.id.tv_usb_list, R.id.tv_usb2_list, R.id.tv_usb3_list, R.id.tv_usb4_list
    };
    private Configuration resumeConfiguration = null;
    private int recreateScreenWidthDp = 0;
    private final Handler mHandler = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (mMotionEventDelayed != null) {
                dispatchTouchEvent(mMotionEventDelayed);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ResourceUtil.updateAppUi(this);
        boolean multiWindow = ResourceUtil.isMultiWindow(this);
        if (multiWindow) {
            GlobalDef.updateMultiWindowActivity(this);
        }
        if (Util.isSufaceFlashInWallpaperApp()) {
            setTheme(R.style.TranslucentTheme2);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen0_video_layout);

        View mainView = findViewById(R.id.screen1_main);
        if (Util.isSufaceFlashInWallpaperApp()) {
            Drawable d = mainView.getBackground();
            if (d == null) {
                GlobalDef.updateScreen0Background(mainView);
            }
        }

        mVideoUI = VideoUI.getInstance(this, mainView, 0);
        updateIntent(this.getIntent());
        mVideoUI.onCreate();
        mThis = this;

        MMLog.i(TAG, "VideoActivity.onCreate(),multiWindow=" + multiWindow + ",tag=" + mainView.getTag());
    }

    @Override
    protected void onNewIntent(Intent it) {
        updateIntent(it);
        setIntent(it);
    }

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
    protected void onPause() {
        MMLog.d(TAG, "onPause");
        // TODO Auto-generated method stub
        super.onPause();
        if (!ResourceUtil.isInSplitScreen(this)) {
            doUIPause();
        }

    }

    @Override
    protected void onResume() {
        MMLog.d(TAG, "onResume");
        // TODO Auto-generated method stub
        if (mUpdateUIResource) {
            ResourceUtil.updateAppUi(this);
            mUpdateUIResource = false;
        }

        super.onResume();
        if (mVideoUI != null) mVideoUI.onResume();

        GlobalDef.openGps(this, getIntent());
        setIntent(null);
        PowerManager pManager = ((PowerManager) getSystemService(POWER_SERVICE));
        mWakeLock = pManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, this.getPackageName());
        mWakeLock.acquire();

        resumeConfiguration = getResources().getConfiguration();
    }

    protected void onStop() {
        MMLog.d(TAG, "onStop");
        super.onStop();
        doUIPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MMLog.d(TAG, "onStart");
    }

    protected void onDestroy() {
        MMLog.d(TAG, "onDestroy");
        super.onDestroy();
        mHandler.removeCallbacks(recreateRunnable);
        if (mVideoUI != null) mVideoUI.onDestroy();
        if (mThis == this) {
            mThis = null;
        }

        if (GlobalDef.mSource == VideoUI.SOURCE) {
            BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mVideoUI != null) {
            mVideoUI.mWillDestory = true;
        }
        BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent arg0) {
        // TODO Auto-generated method stub
        MMLog.d(TAG, arg0.getAction() + ",dispatchKeyEvent:" + getCurrentFocus());
        int keyCode = arg0.getKeyCode();
        if (arg0.getAction() == KeyEvent.ACTION_UP) {
            MMLog.d(TAG, "down:" + getCurrentFocus());
            switch (keyCode) {
                // case KeyEvent.KEYCODE_DPAD_DOWN:
                // case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (keyEventTranslate(keyCode, arg0.getAction())) {
                        return true;
                    }
            }
        } else if (arg0.getAction() == KeyEvent.ACTION_DOWN) {
            MMLog.d(TAG, "down:" + getCurrentFocus());
            switch (keyCode) {
                // case KeyEvent.KEYCODE_DPAD_DOWN:
                // case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (keyEventTranslate(keyCode, arg0.getAction())) {
                        return true;
                    }
            }
        }
        return super.dispatchKeyEvent(arg0);
    }

    private void updateIntent(Intent it) {
        if (it != null) {
            int page = it.getIntExtra("first_poweron", 0);
            if (page != 0) {
                mVideoUI.mFirstPowerOn = page;
            }
        }
    }

    public static void finishActivity(Context context) {
        if (mThis != null) {
            mThis.finish();
        }
    }

    public static void updateBySystemConfig(boolean f) {
        if (mThis != null) {
            ResourceUtil.updateAppUi(mThis);
            if (f) {
                mThis.finish();
            } else {
                if (mThis.mVideoUI != null) {
                    mThis.mUpdateUIResource = mThis.mVideoUI.mPause;
                }
            }
        }
    }

    private int getVisibleListView() {
        for (int i : LIST_ID) {
            View v = findViewById(i);
            if (v != null) {
                if (v.getVisibility() == View.VISIBLE) {
                    return i;
                }
            }
        }
        return 0;
    }

    private int isNeedToRoll(int id, int keyCode) {
        int ret = 0;

        if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef.getSystemUI())) {

            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (id == R.id.btn_local || id == R.id.btn_usb || id == R.id.btn_usb2 || id == R.id.btn_usb3 || id == R.id.btn_usb4 || id == R.id.btn_sd || id == R.id.btn_sd2) {
                    ret = R.id.prev;
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (id == R.id.tv_all_list) {
                    ret = R.id.next;
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (id == R.id.tv_all_list) {
                    ret = R.id.tv_all_list;
                }
            }

        } else {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (id == R.id.prev || id == R.id.pp) {
                    ret = getVisibleListView();
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (id == R.id.tv_all_list || id == R.id.tv_sd_list || id == R.id.tv_sd2_list || id == R.id.tv_usb_list || id == R.id.tv_usb2_list || id == R.id.tv_usb3_list || id == R.id.tv_usb4_list) {
                    ret = R.id.prev;
                }
            }

        }
        return ret;
    }

    private boolean keyEventTranslate(int keyCode, int action) {
        boolean ret = false;
        View v = getCurrentFocus();
        if (v != null) {
            int id = isNeedToRoll(v.getId(), keyCode);
            if (id != 0) {
                if (action == KeyEvent.ACTION_DOWN) {
                    v.clearFocus();
                    findViewById(id).requestFocus();
                }
                ret = true;
            }
        }
        return ret;
    }

    private boolean rollKeyTranslate2(int keyCode) {
        boolean ret = false;
        View v = getCurrentFocus();
        mVideoUI.prepareFullScreen();
        int nextFocus = 0;
        int id = 0;

        if (v != null)
            id = v.getId();
        else return false;

        if (id == R.id.tv_all_list || id == R.id.tv_local_list || id == R.id.tv_sd_list || id == R.id.tv_sd2_list || id == R.id.tv_usb_list || id == R.id.tv_usb2_list || id == R.id.tv_usb3_list || id == R.id.tv_usb4_list) {
            int key = 0;

            View child = ((ListView) v).getSelectedView();
            if (child != null) {
                MyListViewAdapter adapter = (MyListViewAdapter) (((ListView) v).getAdapter());
                MyListViewAdapter.ViewHolder vh = (MyListViewAdapter.ViewHolder) child.getTag();

                switch (keyCode) {
                    case MyCmd.Keycode.KEY_ROOL_RIGHT:

                        if (vh.index < adapter.getCount() - 1) {
                            key = Kernel.KEY_DOWN;
                        }
                        break;
                    case MyCmd.Keycode.KEY_ROOL_LEFT:
                        if (vh.index > 0) {
                            key = Kernel.KEY_UP;
                        }
                        break;
                }
                ret = true;
            } else {
                key = Kernel.KEY_DOWN;
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
        } else {
            id = R.id.tv_all_list;
            if (findViewById(R.id.tv_all_list).getVisibility() == View.VISIBLE) {
                id = R.id.tv_all_list;
            } else if (findViewById(R.id.tv_local_list).getVisibility() == View.VISIBLE) {
                id = R.id.tv_local_list;
            } else if (findViewById(R.id.tv_sd_list).getVisibility() == View.VISIBLE) {
                id = R.id.tv_sd_list;
            } else if (findViewById(R.id.tv_sd2_list).getVisibility() == View.VISIBLE) {
                id = R.id.tv_sd2_list;
            } else if (findViewById(R.id.tv_usb_list).getVisibility() == View.VISIBLE) {
                id = R.id.tv_usb_list;
            } else if (findViewById(R.id.tv_usb2_list).getVisibility() == View.VISIBLE) {
                id = R.id.tv_usb2_list;
            } else if (findViewById(R.id.tv_usb3_list).getVisibility() == View.VISIBLE) {
                id = R.id.tv_usb3_list;
            } else if (findViewById(R.id.tv_usb4_list).getVisibility() == View.VISIBLE) {
                id = R.id.tv_usb4_list;
            }
            findViewById(id).requestFocus();
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
                    if (id == R.id.eq) {
                        key = Kernel.KEY_MAX;
                    }
                    break;
                case MyCmd.Keycode.KEY_ROOL_LEFT:
                    if (id == R.id.btn_all) {
                        key = Kernel.KEY_MAX;
                    }
                    break;
            }

            if (id == R.id.tv_all_list || id == R.id.tv_sd_list || id == R.id.tv_sd2_list || id == R.id.tv_usb_list || id == R.id.tv_usb2_list || id == R.id.tv_usb3_list || id == R.id.tv_usb4_list) {
                if (mMediaPlayer == null) {
                    mMediaPlayer = VideoService.getMediaPlayer();
                }
                View child = ((ListView) v).getSelectedView();
                if (child != null) {
                    MyListViewAdapter.ViewHolder vh = (MyListViewAdapter.ViewHolder) child.getTag();
                    switch (keyCode) {
                        case MyCmd.Keycode.KEY_ROOL_RIGHT:
                            if (vh.index < mMediaPlayer.getCurSongNum() - 1) {
                                key = Kernel.KEY_DOWN;
                            }
                            break;
                        case MyCmd.Keycode.KEY_ROOL_LEFT:
                            if (vh.index > 0) {
                                key = Kernel.KEY_UP;
                            }
                            break;
                    }
                    ret = true;
                } else {
                    key = Kernel.KEY_DOWN;
                }
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


    private void doUIPause() {
        MMLog.d(TAG, "doUIPause:" + mVideoUI.mPause);
        if (!mVideoUI.mPause) {
            mVideoUI.onPause();
            //if (Util.isRKSystem()) {
            // finish();
            //}
            if (null != mWakeLock) {
                mWakeLock.release();
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Log.d("VideoActivity", "onConfigurationChanged\n" + newConfig + "\n" + resumeConfiguration);
        if (resumeConfiguration != null && resumeConfiguration.screenWidthDp != newConfig.screenWidthDp) {
            MMLog.w(TAG, "onConfigurationChanged restart activity " + resumeConfiguration.screenWidthDp + ", " + newConfig.screenWidthDp + ", " + recreateScreenWidthDp + ", " + ResourceUtil.isMultiWindow(this));
            if (recreateScreenWidthDp != newConfig.screenWidthDp && !ResourceUtil.isMultiWindow(this)) {
                mHandler.postDelayed(recreateRunnable, 500);
            }
            recreateScreenWidthDp = newConfig.screenWidthDp;
        }
    }

    private final Runnable recreateRunnable = new Runnable() {
        @Override
        public void run() {
            MMLog.i(TAG, "do recreate");
            recreate();
        }
    };
}
