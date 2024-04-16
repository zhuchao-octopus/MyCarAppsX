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
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import com.common.util.BroadcastUtil;
import com.common.util.Kernel;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.adapter.MyListViewAdapter;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.player.ComMediaPlayer;
import com.octopus.android.carapps.common.utils.ResourceUtil;

/**
 * This activity plays a video from a specified URI.
 */

public class VideoActivity extends Activity {
    private VideoUI mVideoUI;
    private static VideoActivity mThis;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        boolean multiWindow = ResourceUtil.updateAppUi(this);
        multiWindow = ResourceUtil.isMultiWindow(this);
        if (multiWindow) {
            GlobalDef.updateMultiWindownActivity(this);
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

        mVideoUI = VideoUI.getInstanse(this, mainView, 0);

        updateIntent(this.getIntent());
        mVideoUI.onCreate();
        mThis = this;

    }

    private void updateIntent(Intent it) {
        if (it != null) {
            int page;
            page = it.getIntExtra("first_poweron", 0);
            if (page != 0) {
                mVideoUI.mFirstPowerOn = page;
            }
        }
    }

    @Override
    protected void onNewIntent(Intent it) {
        updateIntent(it);
        setIntent(it);
    }

    WakeLock mWakeLock;

    private MotionEvent mMotionEventDelayed = null;
    private final Handler mHandler = new Handler(Looper.myLooper()) {

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
    protected void onPause() {
        Log.d("ffk1", "onPause");
        // TODO Auto-generated method stub
        super.onPause();
        if (!ResourceUtil.isInSplitScreen(this)) {
            doUIPause();
        }

    }

    public boolean mUpdateUIResource = false;

    @Override
    protected void onResume() {
        Log.d("ffk1", "onResume");
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
        Log.d("ffk1", "onStop");
        super.onStop();
        doUIPause();
        // if (mVideoUI != null)
        // mVideoUI.onPause();
        //
        // if (Util.isRKSystem()) {
        // // finish();
        // }
        //
        // if (null != mWakeLock) {
        // mWakeLock.release();
        // }
    }

    //
    @Override
    protected void onStart() {
        Log.d("ffk1", "onStart");
        super.onStart();
        // if (mVideoUI != null)
        // mVideoUI.onResume();
        //
        // GlobalDef.openGps(this, getIntent());
        // setIntent(null);
        // PowerManager pManager = ((PowerManager)
        // getSystemService(POWER_SERVICE));
        // mWakeLock = pManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
        // | PowerManager.ON_AFTER_RELEASE, this.getPackageName());
        // mWakeLock.acquire();
    }

    protected void onDestroy() {
        Log.d("ffk1", "onDestroy");
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

    public static void finishActivity(Context context) {
        if (mThis != null) {
            mThis.finish();
        }
    }

    public static void updateBySystemConfig(boolean f) {
        if (mThis != null) {
            ResourceUtil.updateAppUi(mThis);
            // boolean restart = false;
            // if (mThis.mVideoUI != null)
            // restart = !mThis.mVideoUI.mPause;
            //
            if (f) {
                mThis.finish();
            } else {
                if (mThis.mVideoUI != null) {
                    mThis.mUpdateUIResource = mThis.mVideoUI.mPause;
                }
            }
            // if(restart){
            // UtilSystem.doRunActivity(mThis, "com.car.ui",
            // "com.my.video.VideoActivity");
            // }
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
        Log.d("ccdd", arg0.getAction() + "   dispatchKeyEvent:" + getCurrentFocus());

        int keyCode = arg0.getKeyCode();

        if (arg0.getAction() == KeyEvent.ACTION_UP) {

            Log.d("ccdd", "down:" + getCurrentFocus());
            switch (keyCode) {
                // case KeyEvent.KEYCODE_DPAD_DOWN:
                // case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (keyevntTranslate(keyCode, arg0.getAction())) {
                        return true;
                    }
            }
        } else if (arg0.getAction() == KeyEvent.ACTION_DOWN) {
            Log.d("ccdd", "down:" + getCurrentFocus());
            switch (keyCode) {
                // case KeyEvent.KEYCODE_DPAD_DOWN:
                // case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (keyevntTranslate(keyCode, arg0.getAction())) {
                        return true;
                    }
            }
        }

        return super.dispatchKeyEvent(arg0);
    }

    private ComMediaPlayer mMediaPlayer;
    // public boolean onKeyDown(int keyCode, KeyEvent event) {
    //
    // switch (keyCode) {
    // case KeyEvent.KEYCODE_DPAD_UP:
    // case KeyEvent.KEYCODE_DPAD_DOWN:
    // case KeyEvent.KEYCODE_DPAD_RIGHT:
    // case KeyEvent.KEYCODE_DPAD_LEFT:
    // case KeyEvent.KEYCODE_MEDIA_NEXT:
    // case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
    // // Log.d("dd", ""+getCurrentFocus());
    // if (mVideoUI != null) {
    // if (mVideoUI.mIsFullScrean) {
    // mVideoUI.changeToSmallScreen();
    // }
    // mVideoUI.prepareFullScreen();
    // }
    // break;
    // }
    //
    // switch (keyCode) {
    // // case KeyEvent.KEYCODE_DPAD_UP: {
    // // return doButtonUp();
    // //
    // // }
    // // case KeyEvent.KEYCODE_DPAD_RIGHT:
    // case KeyEvent.KEYCODE_F7:
    // return rollKeyTranslate2(MyCmd.Keycode.KEY_ROOL_LEFT);
    // case KeyEvent.KEYCODE_F8:
    // return rollKeyTranslate2(MyCmd.Keycode.KEY_ROOL_RIGHT);
    // case KeyEvent.KEYCODE_DPAD_UP:
    // case KeyEvent.KEYCODE_DPAD_DOWN:
    // return keyevntTranslate(keyCode);
    // case KeyEvent.KEYCODE_TAB:
    // if (mVideoUI != null) {
    // if (mVideoUI.mIsFullScrean) {
    // mVideoUI.changeToSmallScreen();
    // }
    // mVideoUI.prepareFullScreen();
    // }
    // return
    // rollKeyTranslate(event.isShiftPressed()?MyCmd.Keycode.KEY_ROOL_LEFT:MyCmd.Keycode.KEY_ROOL_RIGHT);
    // default:
    // return super.onKeyDown(keyCode, event);
    // }
    // // return true;
    // }

    private int[] LIST_ID = new int[]{
            R.id.tv_all_list, R.id.tv_sd_list, R.id.tv_sd2_list, R.id.tv_usb_list, R.id.tv_usb2_list, R.id.tv_usb3_list, R.id.tv_usb4_list
    };

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

    private boolean keyevntTranslate(int keyCode, int action) {
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
        if (v != null) {
            id = v.getId();
        }
        if (id == R.id.tv_all_list || id == R.id.tv_local_list || id == R.id.tv_sd_list || id == R.id.tv_sd2_list || id == R.id.tv_usb_list || id == R.id.tv_usb2_list || id == R.id.tv_usb3_list || id == R.id.tv_usb4_list) {
            int key = 0;

            View child = ((ListView) v).getSelectedView();
            if (child != null) {
                MyListViewAdapter adapter = (MyListViewAdapter) (((ListView) v).getAdapter());
                MyListViewAdapter.ViewHolder vh = (MyListViewAdapter.ViewHolder) child.getTag();

                if (child != null) {
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
                    // case R.id.home:
                    // case R.id.prev:
                    // case R.id.pp:
                    // case R.id.next:
                    // case R.id.repeat:
                    // case R.id.shuffle:
                    // key = Kernel.KEY_RIGHT;
                    // break;
                    // case R.id.up:
                    // nextFocus = R.id.down;
                    // break;
                    if (id == R.id.eq) {
                        key = Kernel.KEY_MAX;
                    }
                    break;
                case MyCmd.Keycode.KEY_ROOL_LEFT:
                    // case R.id.back:
                    // case R.id.pp:
                    // case R.id.next:
                    // case R.id.repeat:
                    // case R.id.shuffle:
                    // key = Kernel.KEY_LEFT;
                    // break;
                    // case R.id.down:
                    // nextFocus = R.id.up;
                    // break;
                    if (id == R.id.btn_all) {
                        key = Kernel.KEY_MAX;
                    }
                    break;
            }

			/*switch (id) {
			case R.id.tv_all_list:
			case R.id.tv_sd_list:
			case R.id.tv_sd2_list:
			case R.id.tv_usb_list:
			case R.id.tv_usb2_list:
			case R.id.tv_usb3_list:
			case R.id.tv_usb4_list:
				if (mMediaPlayer == null) {
					mMediaPlayer = VideoService.getMediaPlayer();
					if (mMediaPlayer == null) {
						break;
					}
				}
				View child = ((ListView) v).getSelectedView();
				if (child != null) {
					MyListViewAdapter.ViewHolder vh = (MyListViewAdapter.ViewHolder) child
							.getTag();
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
				break;
			}*/

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
        Log.d("ffk1", "doUIPause:" + mVideoUI.mPause);
        if (!mVideoUI.mPause) {
            if (mVideoUI != null) mVideoUI.onPause();

            if (Util.isRKSystem()) {
                // finish();
            }

            if (null != mWakeLock) {
                mWakeLock.release();
            }
        }
    }

    private Configuration resumeConfiguration = null;
    private int recreateScreenWidthDp = 0;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //			Log.d("VideoActivity", "onConfigurationChanged\n" + newConfig + "\n" + resumeConfiguration);
        if (resumeConfiguration != null && newConfig != null && resumeConfiguration.screenWidthDp != newConfig.screenWidthDp) {
            Log.w("VideoActivity", "onConfigurationChanged restart activity " + resumeConfiguration.screenWidthDp + ", " + newConfig.screenWidthDp + ", " + recreateScreenWidthDp + ", " + ResourceUtil.isMultiWindow(this));
            if (recreateScreenWidthDp != newConfig.screenWidthDp && !ResourceUtil.isMultiWindow(this)) {
                mHandler.postDelayed(recreateRunnable, 500);
            }
            recreateScreenWidthDp = newConfig.screenWidthDp;
        }
    }

    private Runnable recreateRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("VideoActivity", "do recreate");
            recreate();
        }
    };
}
