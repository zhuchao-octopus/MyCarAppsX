package com.octopus.android.carapps.dvd;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.service.ServiceBase;
import com.octopus.android.carapps.common.ui.UIInterface;
import com.octopus.android.carapps.hardware.dvs.DVideoSpec;
import com.octopus.android.carapps.hardware.dvs.IDVSCallback;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DVDService extends ServiceBase {
    public static final String TAG = "DVDService";

    @SuppressLint("StaticFieldLeak")
    private static DVDService mThis;

    private static DVideoSpec mDvd = new DVideoSpec();

    public static DVideoSpec getDVideoSpec() {
        return mDvd;
    }

    public static DVDService getInstanse(Context context) {
        if (mThis == null) {
            mThis = new DVDService(context);

            mThis.onCreate();
        }
        return mThis;
    }

    public DVDService(Context context) {
        super(context);
    }

    public void onDestroy() {
        unregisterListener();
    }

    public void onCreate() {
        mDvd.sendDVSCommand(DVideoSpec.DVS_OPEN);
        mDvd.setServiceCallback(mIDVSCallback);
        registerListener();
    }

    public void doKeyControl(int code) {
        switch (code) {


            case MyCmd.Keycode.CH_UP:
            case MyCmd.Keycode.KEY_SEEK_NEXT:
            case MyCmd.Keycode.KEY_TURN_A:
            case MyCmd.Keycode.NEXT:
                mDvd.sendDVDControlCommand(DVideoSpec.DVS_NEXT);

                break;


            case MyCmd.Keycode.CH_DOWN:
            case MyCmd.Keycode.KEY_SEEK_PREV:
            case MyCmd.Keycode.KEY_TURN_D:
            case MyCmd.Keycode.PREVIOUS:
                mDvd.sendDVDControlCommand(DVideoSpec.DVS_PREVIOUS);
                break;
            case MyCmd.Keycode.FAST_F:
                mDvd.sendDVDControlCommand(DVideoSpec.DVS_SF);
                break;
            case MyCmd.Keycode.FAST_R:
                mDvd.sendDVDControlCommand(DVideoSpec.DVS_SR);
                break;
            case MyCmd.Keycode.STOP:
                mDvd.sendDVDControlCommand(DVideoSpec.DVS_STOP);
                break;
            case MyCmd.Keycode.PLAY:
                mDvd.sendDVDControlCommand(DVideoSpec.DVS_PLAY);
                break;
            case MyCmd.Keycode.PAUSE:
                mDvd.sendDVDControlCommand(DVideoSpec.DVS_PAUSE);
                break;

            case MyCmd.Keycode.KEY_SUB_T:
                mDvd.sendDVDControlCommand(DVideoSpec.DVS_SUBTITLE);
                break;

            case MyCmd.Keycode.KEY_PBC:
            case MyCmd.Keycode.KEY_TITLE:
                mDvd.sendDVDControlCommand(DVideoSpec.DVS_TITLE);
                break;

            case MyCmd.Keycode.KEYAMS_RPT:
                mDvd.sendDVDControlCommand(DVideoSpec.DVS_REPEAT);
                break;
            case MyCmd.Keycode.KEY_LDC_RDM:
                mDvd.sendDVDControlCommand(DVideoSpec.DVS_SHUFFLERANDOM);
                break;
            case MyCmd.Keycode.KEY_ST_PROG:
                mDvd.sendDVDControlCommand(27);
                break;
            case MyCmd.Keycode.KEY_ZOOM:
                mDvd.sendDVDControlCommand(43);

                break;
            case MyCmd.Keycode.KEY_ANGLE:
                mDvd.sendDVDControlCommand(44);
                break;

            case MyCmd.Keycode.KEY_OSD:
                mDvd.sendDVDControlCommand(37);
                break;

            case MyCmd.Keycode.KEY_GOTO:
                mDvd.sendDVDControlCommand(25);
                break;

            case MyCmd.Keycode.NUMBER0:
                mDvd.sendDVDControlCommand(1);
                break;
            case MyCmd.Keycode.NUMBER1:
                mDvd.sendDVDControlCommand(2);
                break;
            case MyCmd.Keycode.NUMBER2:
                mDvd.sendDVDControlCommand(3);
                break;
            case MyCmd.Keycode.NUMBER3:
                mDvd.sendDVDControlCommand(4);
                break;
            case MyCmd.Keycode.NUMBER4:
                mDvd.sendDVDControlCommand(5);
                break;
            case MyCmd.Keycode.NUMBER5:
                mDvd.sendDVDControlCommand(6);
                break;
            case MyCmd.Keycode.NUMBER6:
                mDvd.sendDVDControlCommand(7);
                break;
            case MyCmd.Keycode.NUMBER7:
                mDvd.sendDVDControlCommand(8);
                break;
            case MyCmd.Keycode.NUMBER8:
                mDvd.sendDVDControlCommand(9);
                break;
            case MyCmd.Keycode.NUMBER9:
                mDvd.sendDVDControlCommand(10);
                break;
            case MyCmd.Keycode.KEY_NUM10:
                mDvd.sendDVDControlCommand(11);
                break;

            case MyCmd.Keycode.KEY_DVD_LEFT:
                mDvd.sendDVDControlCommand(38);
                break;
            case MyCmd.Keycode.KEY_DVD_RIGHT:
                mDvd.sendDVDControlCommand(39);
                break;
            case MyCmd.Keycode.KEY_DVD_UP:
                mDvd.sendDVDControlCommand(40);
                break;
            case MyCmd.Keycode.KEY_DVD_DOWN:
                mDvd.sendDVDControlCommand(41);
                break;
            case MyCmd.Keycode.KEY_DVD_ENTER:
                mDvd.sendDVDControlCommand(36);
                break;

            case MyCmd.Keycode.KEY_DVD_SETUP:
                mDvd.sendDVDControlCommand(46);
                break;

            case MyCmd.Keycode.PLAY_PAUSE:
                if (mDvd.isPlaying()) {
                    mDvd.sendDVDControlCommand(DVideoSpec.DVS_PAUSE);
                } else {
                    mDvd.sendDVDControlCommand(DVideoSpec.DVS_PLAY);
                }
                break;

        }
    }

    public static void doKeyControlPublic(int code) {
        if (mThis != null) {
            mThis.doKeyControl(code);
        }
    }

    private IDVSCallback mIDVSCallback = new IDVSCallback() {

        public void dvsCallback(int value, int param) {
            returnInfoToUI(MyCmd.Cmd.APP_USER_PRIVATE, value, param, null);
        }
    };

    public void doEQResule() {
        returnInfoToUI(GlobalDef.MSG_UPDATE_EQ_MODE);
    }

    private void returnInfoToUI(int value) {
        returnInfoToUI(value, 0, 0, null);
    }

    private static Handler[] mHandlerUICallBack = new Handler[2];

    public static void setUICallBack(Handler cb, int index) {
        mHandlerUICallBack[index] = cb;
    }

    private static void returnInfoToUI(int what, int status, int param, Object obj) {

        //		Log.d(TAG, mHandlerUICallBack[0] +"returnInfoToUI:"+what );
        for (int i = 0; i < mHandlerUICallBack.length; ++i) {
            if (mHandlerUICallBack[i] != null) {
                mHandlerUICallBack[i].sendMessageDelayed(mHandlerUICallBack[i].obtainMessage(what, status, param, obj), 20);
            }
        }
    }

    private int mReoverSource = -1;

    private void doDvdStatusChange(byte s) {
        if (mDvd.mDVDStatus != s) {
            if (s != MyCmd.Dvd.DVD_STATUS_DISK_INSIDE && s != MyCmd.Dvd.DVD_STATUS_IN_ING) {
                if (DVDUI.mUI[0] != null && !DVDUI.mUI[0].mPause) {// screen 0

                    // mContext.startActivity(new
                    // Intent(Intent.ACTION_MAIN).addFlags(
                    // Intent.FLAG_ACTIVITY_NEW_TASK
                    // | Intent.FLAG_ACTIVITY_CLEAR_TOP).addCategory(
                    // Intent.CATEGORY_HOME));
                    if (GlobalDef.mSource == DVDUI.SOURCE) {
                        GlobalDef.setSource(mContext, MyCmd.SOURCE_MX51);
                        mDvd.clearAllData();
                    }

                } else { // screen 1

                }
            } else if (s == MyCmd.Dvd.DVD_STATUS_OUT_ING) {
                Log.d(TAG, "doDvdStatusChange:" + s);
                mDvd.clearAllData();

            }
        }
        mDvd.mDVDStatus = s;
    }

    public void doCmd(int cmd, Intent intent) {
        switch (cmd) {
            case MyCmd.Cmd.SOURCE_CHANGE:
                int source = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                if (source != DVDUI.SOURCE) {
                    savePlayStatus(); //car service do this now
                    stopReportCanbox();
                } else {
                    startReportCanbox();
                }
                break;

            case MyCmd.Cmd.REVERSE_STATUS:

                DVDUI aui;
                if (GlobalDef.mReverseStatus == 1) {
                    for (int i = 0; i < UIInterface.MAX_DISPLAY; i++) {
                        aui = DVDUI.mUI[i];
                        if (aui != null && !aui.mPause) {
                            //						mReoverSource = i;
                            aui.saveStatusReverseStart();
                        }
                    }
                } else {
                    for (int i = 0; i < UIInterface.MAX_DISPLAY; i++) {
                        aui = DVDUI.mUI[i];
                        if (aui != null) {
                            //						mReoverSource = i;
                            aui.recoverStatusReverseStop();
                        }
                    }
                }
                break;
            case MyCmd.Cmd.MCU_BRAK_CAR_STATUS:
                int brake = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                returnInfoToUI(GlobalDef.MSG_PARK_BRAKE, brake, 0, null);
                break;
            case MyCmd.Cmd.MCU_DVD_RECEIVE_DATA:

                byte[] param = intent.getByteArrayExtra(MyCmd.EXTRA_COMMON_DATA);
                if (param != null && param.length >= 2) {
                    switch (param[1]) {
                        case 1:
                            Log.d(TAG, "power:" + param[2]);
                            if (mDvd.mDVDPower == 0 && param[2] == 1) {
                                mHandler.removeMessages(0);
                                mHandler.sendEmptyMessageDelayed(0, 5000);
                            }

                            mDvd.mDVDPower = param[2];
                            if (param[2] == 0) {
                                mDvd.clearAllData();
                            }

                            returnInfoToUI(MyCmd.Cmd.APP_USER_PRIVATE, DVideoSpec.DVD_POWER_STATUS, mDvd.mDVDPower, null);
                            break;
                        case 2:
                            doDvdStatusChange(param[2]);

                            returnInfoToUI(MyCmd.Cmd.APP_USER_PRIVATE, DVideoSpec.DVD_DISK_STATUS, mDvd.mDVDStatus, null);
                            break;
                    }

                }
                break;
            case MyCmd.Cmd.ACC_POWER_OFF:
                if (Util.isRKSystem()) {
                    Log.d(TAG, "dvd ACC_POWER_OFF");
                    mDvd.clearAllData();
                }
                break;
        }

    }

    // for canbox
    private Timer mTimerCanbox;

    private void startReportCanbox() {
        // stopReportCanbox();
        if (mTimerCanbox == null) {
            TimerTask task = new TimerTask() {
                public void run() {
                    if (mDvd != null) {
                        if (GlobalDef.mCanboxNeedPlayInfo) {
                            //BroadcastUtil.sendCanboxInfo(mContext,MyCmd.DVD_SOURCE_CHANGE,mDvd.mTrackCurrent, mDvd.mCurTime,mDvd.mTrackNum, mDvd.mTotalTime);
                        }

                        //						if (GlobalDef.mIs8600) {
                        //							if (GlobalDef.mSource == MyCmd.SOURCE_DVD){
                        //							String s = String
                        //									.format("%03d-%02d%02d",
                        //											mDvd.mTrackCurrent,
                        //											(int) mDvd.mCurTime/60,
                        //
                        //											(int) mDvd.mCurTime%60);
                        //							GlobalDef.setSmallLcd(s);
                        //							}
                        //						}

                    }
                    // sendBroadcastInt(AUDIO_SOURCE_CHANGE, mCurrentPlayPos,
                    // (int) (position() / 1000L), mCurrentTotal);
                    // Log.d("canbox", "dvd report");

                }
            };
            mTimerCanbox = new Timer();
            mTimerCanbox.schedule(task, 1, 1000);
        }
    }

    private void stopReportCanbox() {
        if (mTimerCanbox != null) {
            mTimerCanbox.cancel();
            mTimerCanbox.purge();
            mTimerCanbox = null;
        }
    }

    private static boolean mPausedByTransientLossOfFocus = false;
    public static OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            // AudioFocus is a new feature: focus updates are made verbose on
            // purpose

            Log.e(TAG, "onAudioFocusChange " + focusChange);

            if (mDvd == null) {
                return;
            }
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (!GlobalDef.isAudioFocusGPS()) {
                        if (GlobalDef.mSource == DVDUI.SOURCE) {
                            if (mDvd.isPlaying()) {
                                mDvd.sendDVDControlCommand(DVideoSpec.DVS_PAUSE);
                                mPausedByTransientLossOfFocus = true;
                            }

                            GlobalDef.setSource(GlobalDef.mContext, MyCmd.SOURCE_OTHERS_APPS);
                        }


                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    // Log.v(TAG, "AudioFocus: received AUDIOFOCUS_GAIN");
                    if (mPausedByTransientLossOfFocus) {
                        mPausedByTransientLossOfFocus = false;
                        mDvd.sendDVDControlCommand(DVideoSpec.DVS_PLAY);

                        GlobalDef.setSource(GlobalDef.mContext, DVDUI.SOURCE);
                    }

                    break;
                default:
                    Log.e(TAG, "Unknown audio focus change code");
            }
        }
    };

    public void abandonAudioFocus() {
        mPausedByTransientLossOfFocus = false;
        GlobalDef.abandonAudioFocus(mAudioFocusListener);
    }

    public int getSource() {
        return DVDUI.SOURCE;
    }

    private void savePlayStatus() {
        mDvd.sendDVSSetMemoryCommand();
    }

    public void systemPowerOff() {
        //		Log.d("dd", "dvd poweroff");
        savePlayStatus();
    }

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    initDVDLang();
                    break;
            }
        }
    };

    public static void initDVDLang() {
        int lang = 0;
        String locale = Locale.getDefault().getLanguage();
        if (locale != null) {
            if (locale.equals("en")) {
                lang = 0;
            } else if (locale.equals("zh")) {
                lang = 1;
            } else if (locale.equals("zh")) {
                lang = 2;
            } else if (locale.equals("ar")) {
                lang = 3;
            } else if (locale.equals("iw")) {
                lang = 4;
            } else if (locale.equals("ru")) {
                lang = 5;
            } else if (locale.equals("zh")) {
                lang = 6;
            } else if (locale.equals("zh")) {
                lang = 7;
            } else if (locale.equals("pt")) {
                lang = 8;
            } else if (locale.equals("tr")) {
                lang = 9;
            } else if (locale.equals("fr")) {
                lang = 10;
            } else if (locale.equals("de")) {
                lang = 11;
            } else if (locale.equals("it")) {
                lang = 12;
            } else if (locale.equals("es")) {
                lang = 13;
            } else if (locale.equals("zh")) {
                lang = 14;
            } else if (locale.equals("pt")) {
                lang = 15;
            } else if (locale.equals("zh")) {
                lang = 16;
            } else if (locale.equals("cs")) {
                lang = 17;
            }
        }

        Log.d(TAG, lang + ":initDVDLang:" + locale);
        //		if (lang != -1) {
        mDvd.sendDVDLangCommand(lang);
        //		}
    }

    private BroadcastReceiver mReceiver = null;

    private void unregisterListener() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private void registerListener() {
        if (mReceiver == null) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();

                    if (action.equals(Intent.ACTION_LOCALE_CHANGED)) {

                        mHandler.removeMessages(0);
                        initDVDLang();
                    }

                }
            };
            IntentFilter iFilter = new IntentFilter();

            iFilter.addAction(Intent.ACTION_LOCALE_CHANGED);

            mContext.registerReceiver(mReceiver, iFilter);
        }
    }
}
