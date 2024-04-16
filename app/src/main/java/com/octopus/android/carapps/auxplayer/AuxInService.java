package com.octopus.android.carapps.auxplayer;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Handler;
import android.util.Log;

import com.common.util.MyCmd;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.service.ServiceBase;

public class AuxInService extends ServiceBase {
    public static final String TAG = "RadioService";

    private static AuxInService mThis;

    public static AuxInService getInstanse(Context context) {
        if (mThis == null) {
            mThis = new AuxInService(context);

            mThis.onCreate();
        }
        return mThis;
    }

    public AuxInService(Context context) {
        super(context);
    }

    public void onDestroy() {
    }

    public void onCreate() {

    }

    public void doKeyControl(int code) {

    }

    private static Handler[] mHandlerUICallBack = new Handler[2];

    public static void setUICallBack(Handler cb, int index) {
        mHandlerUICallBack[index] = cb;
    }

    private static void callBackToUI(int what, int status, Object obj) {
        for (int i = 0; i < mHandlerUICallBack.length; ++i) {
            if (mHandlerUICallBack[i] != null) {
                mHandlerUICallBack[i].sendMessageDelayed(mHandlerUICallBack[i].obtainMessage(what, status, 0, obj), 20);
            }
        }
    }

    private int mReoverSource = -1;

    public void doCmd(int cmd, Intent intent) {
        switch (cmd) {
            case MyCmd.Cmd.REVERSE_STATUS:
                int status = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);

                if (status == 1) {
                    AuxInUI aui;
                    for (int i = 0; i < 2; i++) {
                        aui = AuxInUI.mUI[i];

                        //					Log.d("tt", "aux:"+aui);

                        if (aui != null && !aui.mPause) {

                            //						Log.d("tt", "aux:"+aui.mPause);

                            mReoverSource = i;
                            aui.reverseStart();

                            break;
                        }
                    }
                } else {
                    if (mReoverSource >= 0 && mReoverSource < 2) {
                        AuxInUI.mUI[mReoverSource].reverseStop();
                        mReoverSource = -1;
                    }
                }
                break;
            case MyCmd.Cmd.MCU_BRAK_CAR_STATUS:
                int brake = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                callBackToUI(GlobalDef.MSG_PARK_BRAKE, brake, null);
                break;
        }

    }

    private static boolean mPausedByTransientLossOfFocus = false;
    public static OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            // AudioFocus is a new feature: focus updates are made verbose on
            // purpose

            if (GlobalDef.mContext == null) {
                return;
            }
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (!GlobalDef.isAudioFocusGPS()) {
                        if (GlobalDef.mSource == AuxInUI.SOURCE) {
                            //				if (mServiceBase.mPlayStatus == BTMusicService.A2DP_INFO_PLAY) {
                            mPausedByTransientLossOfFocus = true;
                            GlobalDef.setSource(GlobalDef.mContext, MyCmd.SOURCE_OTHERS_APPS);
                            //				}
                        }
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.v(TAG, "AudioFocus: received AUDIOFOCUS_GAIN");
                    if (mPausedByTransientLossOfFocus) {
                        mPausedByTransientLossOfFocus = false;
                        GlobalDef.setSource(GlobalDef.mContext, AuxInUI.SOURCE);
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
        return AuxInUI.SOURCE;
    }
}
