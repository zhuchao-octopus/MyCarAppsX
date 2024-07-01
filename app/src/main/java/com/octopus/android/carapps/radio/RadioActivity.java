package com.octopus.android.carapps.radio;

import android.app.Activity;
import android.content.Context;
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
import android.widget.GridView;
import android.widget.ListView;

import com.common.util.BroadcastUtil;
import com.common.util.Kernel;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.common.ui.GlobalDef;
import com.octopus.android.carapps.common.utils.ResourceUtil;

public class RadioActivity extends Activity {

    private RadioUI mRadioUI;
    static RadioActivity mThis;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ResourceUtil.updateAppUi(this);
        boolean multiWindow = ResourceUtil.isMultiWindow(this);
        if (multiWindow) {
            GlobalDef.updateMultiWindowActivity(this);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen0_radio_layout);
        // Log.d("aa","onCreate");
        mRadioUI = RadioUI.getInstanse(this, findViewById(R.id.screen1_main), 0);

        mRadioUI.onCreate();

        mThis = this;
    }

    @Override
    protected void onNewIntent(Intent it) {
        setIntent(it);
        udpateAMFM();
    }

    private void udpateAMFM() {

        if (mRadioUI != null) {
            Intent it = getIntent();
            if (it != null) {
                // adapt WAC&TALK
                String band = it.getStringExtra("type");
                if (band != null) {
                    Log.d("radio", "band = " + band);
                    if (band.equalsIgnoreCase("am")) {
                        mRadioUI.updateAmFm((byte) 3);
                    } else {
                        mRadioUI.updateAmFm((byte) 0);
                    }
                }
                float freq = it.getFloatExtra("frep", -1.0f);
                if (freq != -1.0f) {
                    Log.d("radio", "frep = " + freq);
                    mRadioUI.updateFreq(freq);
                }

                byte mAmFm = it.getByteExtra("amfm", (byte) -1);
                if (mAmFm != -1) {
                    mRadioUI.updateAmFm(mAmFm);
                }

                freq = it.getFloatExtra("freq", -1.0f);
                if (freq != -1.0f) {
                    mRadioUI.updateFreq(freq);
                }
            }
        }
    }

    public boolean mUpdateUIResource = false;

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        resumeConfiguration = getResources().getConfiguration();
    }

    @Override
    protected void onStart() {

        if (mUpdateUIResource) {
            ResourceUtil.updateAppUi(this);
            mUpdateUIResource = false;
        }

        super.onStart();

        if (mRadioUI != null) mRadioUI.onResume();

        udpateAMFM();
        GlobalDef.openGps(this, getIntent());

        setIntent(null);
    }

    protected void onStop() {
        super.onStop();
        if (mRadioUI != null) mRadioUI.onPause();

    }

    public static void finishActivity(Context context) {
        if (mThis != null) {
            mThis.finish();
        }
    }

    public static void updateBySystemConfig(boolean f) {
        // Log.d("ddc", "radio updateBySystemConfig:"+mThis);
        if (mThis != null) {
            ResourceUtil.updateAppUi(mThis);
            // boolean restart = false;
            // if (mThis.mRadioUI != null)
            // restart = !mThis.mRadioUI.mPause;
            //
            if (f) {
                mThis.finish();
            } else {
                if (mThis.mRadioUI != null) {
                    mThis.mUpdateUIResource = mThis.mRadioUI.mPause;
                }
            }
            // if(restart){
            // UtilSystem.doRunActivity(mThis, "com.car.ui",
            // "com.my.radio.RadioActivity");
            // }
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(recreateRunnable);
        if (mRadioUI != null) mRadioUI.onDestroy();

        if (mThis == this) {
            mThis = null;
        }

        if (GlobalDef.mSource == RadioUI.SOURCE) {
            BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mRadioUI != null) {
            mRadioUI.mWillDestory = true;
        }
        BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
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

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Util.isRKSystem()) {
            return super.onKeyDown(keyCode, event);
        }
        switch (keyCode) {
            // case KeyEvent.KEYCODE_DPAD_UP: {
            // return doButtonUp();
            //
            // }
            case KeyEvent.KEYCODE_F7:
                return rollKeyTranslate2(MyCmd.Keycode.KEY_ROOL_LEFT);
            case KeyEvent.KEYCODE_F8:
                return rollKeyTranslate2(MyCmd.Keycode.KEY_ROOL_RIGHT);

            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_UP:
                return keyevntTranslate(keyCode);
            case KeyEvent.KEYCODE_TAB:
                return rollKeyTranslate(event.isShiftPressed() ? MyCmd.Keycode.KEY_ROOL_LEFT : MyCmd.Keycode.KEY_ROOL_RIGHT);
            default:
                return super.onKeyDown(keyCode, event);
        }
        // return true;
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

        int nextFocus = 0;
        int id = 0;
        if (v != null) {
            id = v.getId();
        }
        if (id == R.id.radio_frequency_list || id == R.id.radio_pty_list) {
            int key = 0;

            View child = null;
            if (v instanceof ListView) {
                child = ((ListView) v).getSelectedView();
            } else if (v instanceof GridView) {
                child = ((GridView) v).getSelectedView();
            }

            if (child != null) {

                int maxnum = 0;
                int index = 0;
                if (id == R.id.radio_frequency_list) {
                    maxnum = AkRadio.mFreqListSize;

                    MyListViewAdapter.ViewHolder vh = (MyListViewAdapter.ViewHolder) child.getTag();
                    index = vh.index;
                } else {
                    maxnum = PTYListViewAdapter.mPty_data.length;

                    index = (int) child.getTag();
                }

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
            id = R.id.radio_frequency_list;
            if (findViewById(R.id.radio_pty_list).getVisibility() == View.VISIBLE) {
                id = R.id.radio_pty_list;
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
                    if (id == R.id.radio_function_button_pty) {
                        key = Kernel.KEY_MAX;
                    }
                    break;
            }

            if (id == R.id.radio_frequency_list || id == R.id.radio_pty_list) {
                View child = ((ListView) v).getSelectedView();
                if (child != null) {

                    int maxnum = 0;
                    int index = 0;
                    if (id == R.id.radio_frequency_list) {
                        maxnum = AkRadio.mFreqListSize;

                        MyListViewAdapter.ViewHolder vh = (MyListViewAdapter.ViewHolder) child.getTag();
                        index = vh.index;
                    } else {
                        maxnum = PTYListViewAdapter.mPty_data.length;

                        index = (int) child.getTag();
                    }

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

    private Configuration resumeConfiguration = null;
    private int recreateScreenWidthDp = 0;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Log.d("RadioActivity", "onConfigurationChanged\n" + newConfig + "\n" + resumeConfiguration);
        if (resumeConfiguration != null && newConfig != null && resumeConfiguration.screenWidthDp != newConfig.screenWidthDp) {
            Log.w("RadioActivity", "onConfigurationChanged " + resumeConfiguration.screenWidthDp + ", " + newConfig.screenWidthDp + ", " + recreateScreenWidthDp + ", " + ResourceUtil.isMultiWindow(this));
            if (recreateScreenWidthDp != newConfig.screenWidthDp && !ResourceUtil.isMultiWindow(this)) {
                mHandler.postDelayed(recreateRunnable, 500);
            }
            recreateScreenWidthDp = newConfig.screenWidthDp;
        }
    }

    private final Runnable recreateRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("RadioActivity", "do recreate");
            recreate();
        }
    };
}
