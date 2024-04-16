package com.octopus.android.carapps.dvd;

import android.app.Activity;
import android.content.Intent;
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

import com.common.util.BroadcastUtil;
import com.common.util.Kernel;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.adapter.MyListViewAdapterDvd;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.utils.ResourceUtil;
import com.octopus.android.carapps.hardware.dvs.DVideoSpec;

public class DVDPlayer extends Activity {

    DVDUI mUI;
    private static DVDPlayer mThis;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        boolean multiWindow = ResourceUtil.updateAppUi(this);
        if (multiWindow) {
            GlobalDef.updateMultiWindownActivity(this);
        }
        if (Util.isSufaceFlashInWallpaperApp()) {
            setTheme(R.style.TranslucentTheme2);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dvd_player);

        if (Util.isSufaceFlashInWallpaperApp()) {
            GlobalDef.updateScreen0Background(this, findViewById(R.id.screen1_main));
        }
        mUI = DVDUI.getInstanse(this, findViewById(R.id.screen1_main), 0);
        mUI.onCreate();
        mDvd = DVDService.getDVideoSpec();
        mThis = this;
    }

    @Override
    protected void onNewIntent(Intent it) {
        setIntent(it);
    }

    public static WakeLock mWakeLock;

    public boolean mUpdateUIResource = false;

    @Override
    protected void onResume() {
        //		Log.d("ffk", "onResume!");
        mHandlerDestroy.removeMessages(0);
        mPaused = false;
        if (mUpdateUIResource) {
            ResourceUtil.updateAppUi(this);
            mUpdateUIResource = false;
        }
        super.onResume();

        PowerManager pManager = ((PowerManager) getSystemService(POWER_SERVICE));
        mWakeLock = pManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, this.getPackageName());
        mWakeLock.acquire();

        if (mUI != null) mUI.onResume();
        GlobalDef.openGps(this, getIntent());
        setIntent(null);


    }

    @Override
    protected void onPause() {
        mPaused = true;
        super.onPause();
        if (mUI != null) mUI.onPause();

        if (null != mWakeLock) {
            mWakeLock.release();
            mWakeLock = null;
        }

    }

    private final Handler mHandlerDestroy = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {

            //			Log.d("ffk", "mHandlerDestroy!!!!!!!");
            if (GlobalDef.mSource == DVDUI.SOURCE) {
                if (!mPaused) {
                    BroadcastUtil.sendToCarServiceSetSource(mThis, MyCmd.SOURCE_MX51);
                }
            }
        }
    };

    private static boolean mPaused = true;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //		Log.d("ffk", "onDestroy!");
        if (mUI != null) mUI.onDestroy();
        if (mThis == this) {
            mThis = null;
        }
        mHandlerDestroy.removeMessages(0);
        mHandlerDestroy.sendEmptyMessageDelayed(0, 800);
    }

    public static void finishActivity() {
        if (mThis != null) {
            mThis.finish();
        }
    }


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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mUI != null) {
            mUI.mWillDestory = true;
        }

        BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
    }

    // @Override
    // public boolean onKeyDown(int keyCode, KeyEvent event) {
    //
    // switch (keyCode) {
    //
    // case KeyEvent.KEYCODE_BACK: {
    // // if (mUI != null) {
    // // mUI.mWillDestory = true;
    // // }
    // // BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
    // return super.onKeyDown(keyCode, event);
    // }
    // case KeyEvent.KEYCODE_DPAD_LEFT:
    // DVDService.doKeyControlPublic(MyCmd.Keycode.LEFT);
    // break;
    // case KeyEvent.KEYCODE_DPAD_RIGHT:
    // DVDService.doKeyControlPublic(MyCmd.Keycode.RIGHT);
    // break;
    // case KeyEvent.KEYCODE_DPAD_UP:
    // DVDService.doKeyControlPublic(MyCmd.Keycode.UP);
    // break;
    // case KeyEvent.KEYCODE_DPAD_DOWN:
    // DVDService.doKeyControlPublic(MyCmd.Keycode.DOWN);
    // break;
    // case KeyEvent.KEYCODE_ENTER:
    // DVDService.doKeyControlPublic(MyCmd.Keycode.ENTER);
    // break;
    // default:
    // return super.onKeyDown(keyCode, event);
    //
    // }
    // return true;
    // }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            // case KeyEvent.KEYCODE_DPAD_UP: {
            // return doButtonUp();
            //
            // }
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_TAB:
                // Log.d("dd", ""+getCurrentFocus());
                if (mUI != null) {
                    if (mUI.mIsFullScrean) {
                        mUI.quitFullScreen();
                    }
                    mUI.mIsFullScrean = false;
                    mUI.prepareFullScreen();
                }
                break;
        }

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

    }

    private int isNeedToRoll(int id, int keyCode) {
        int ret = 0;

        // if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
        // switch (id) {
        // case R.id.prev:
        // case R.id.pp:
        // ret = getVisibleListView();
        // break;
        // }
        // } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
        // switch (id) {
        //
        // case R.id.radio_frequency_list:
        // case R.id.radio_pty_list:
        // ret = R.id.prev;
        // break;
        // }
        // }

        return ret;
    }

    private boolean keyevntTranslate(int keyCode) {
        boolean ret = false;
        View v = getCurrentFocus();
        if (v != null) {

            int id = isNeedToRoll(v.getId(), keyCode);
            if (id != 0) {
                v.clearFocus();
                findViewById(id).requestFocus();

                ret = true;
            }

        }
        return ret;

    }

    private boolean rollKeyTranslate2(int keyCode) {
        boolean ret = false;
        View v = getCurrentFocus();
        if (mUI != null) {
            if (mUI.mIsFullScrean) {
                mUI.quitFullScreen();
            }
            mUI.mIsFullScrean = false;
            mUI.prepareFullScreen();
        }

        int nextFocus = 0;

        int id = 0;
        if (v != null) {
            id = v.getId();
        }

        if (mDvd.mDiskType != DVideoSpec.DVD_DISK_TYPE_CD) {
            if (id != 0) {
                int key = 0;

                switch (keyCode) {
                    case MyCmd.Keycode.KEY_ROOL_RIGHT:
                        if (id == R.id.cd_eq_button || id == R.id.eq || id == R.id.dvd_next_page_button || id == R.id.dvd_prev_page_button) {
                            key = Kernel.KEY_MAX;
                        }
                        if (key != Kernel.KEY_MAX) {
                            key = Kernel.KEY_RIGHT;
                        }
                        break;
                    case MyCmd.Keycode.KEY_ROOL_LEFT:
                        if (id == R.id.cd_prev_button || id == R.id.dvd_prev_button || id == R.id.dvd_title_button) {
                            key = Kernel.KEY_MAX;
                        }
                        if (key != Kernel.KEY_MAX) {
                            key = Kernel.KEY_LEFT;
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
            } else {
                if (findViewById(R.id.dvd_page1_layout).getVisibility() == View.VISIBLE) {
                    id = R.id.dvd_prev_button;
                } else {
                    id = R.id.dvd_title_button;
                }
                findViewById(id).requestFocus();
                findViewById(id).requestFocusFromTouch();
            }
        } else {
            if (id == R.id.list_view) {
                int key = 0;
                View child = ((ListView) v).getSelectedView();
                if (child != null) {

                    int maxnum = 0;
                    int index = 0;
                    maxnum = mDvd.mTrackNum;
                    MyListViewAdapterDvd.ViewHolder vh = (MyListViewAdapterDvd.ViewHolder) child.getTag();
                    index = vh.index;

                    if (child != null) {
                        switch (keyCode) {
                            case MyCmd.Keycode.KEY_ROOL_RIGHT:

                                if (index < maxnum - 1) {
                                    key = Kernel.KEY_DOWN;
                                }
                                break;
                            case MyCmd.Keycode.KEY_ROOL_LEFT:
                                if (index > 0) {
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
                findViewById(R.id.list_view).requestFocus();
            }

        }

        return ret;

    }

    private DVideoSpec mDvd;

    private boolean rollKeyTranslate(int keyCode) {
        boolean ret = false;
        View v = getCurrentFocus();
        if (mUI != null) {
            if (mUI.mIsFullScrean) {
                mUI.quitFullScreen();
            }
            mUI.mIsFullScrean = false;
            mUI.prepareFullScreen();
        }
        int nextFocus = 0;
        if (v != null) {
            int id = v.getId();// isNeedToRoll(v.getId(), keyCode);
            int key = 0;

            switch (keyCode) {
                case MyCmd.Keycode.KEY_ROOL_RIGHT:
                    if (id == R.id.cd_eq_button || id == R.id.eq || id == R.id.dvd_next_page_button || id == R.id.dvd_prev_page_button) {
                        key = Kernel.KEY_MAX;
                    }
                    break;
                case MyCmd.Keycode.KEY_ROOL_LEFT:
                    if (id == R.id.cd_prev_button || id == R.id.dvd_prev_button || id == R.id.dvd_title_button) {
                        key = Kernel.KEY_MAX;
                    }
                    break;
            }

            if (id == R.id.list_view) {
                View child = ((ListView) v).getSelectedView();
                if (child != null) {

                    int maxnum = 0;
                    int index = 0;
                    maxnum = mDvd.mTrackNum;
                    MyListViewAdapterDvd.ViewHolder vh = (MyListViewAdapterDvd.ViewHolder) child.getTag();
                    index = vh.index;

                    if (child != null) {
                        switch (keyCode) {
                            case MyCmd.Keycode.KEY_ROOL_RIGHT:

                                if (index < maxnum - 1) {
                                    key = Kernel.KEY_DOWN;
                                }
                                break;
                            case MyCmd.Keycode.KEY_ROOL_LEFT:
                                if (index > 0) {
                                    key = Kernel.KEY_UP;
                                }
                                break;
                        }
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

}