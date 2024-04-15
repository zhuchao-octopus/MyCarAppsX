package com.octopus.android.carapps.radio;


import com.common.util.MyCmd;
import com.common.util.Util;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.service.ServiceBase;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Handler;
import android.util.Log;

public class RadioService extends ServiceBase {
    public static final String TAG = "RadioService";

    private static AkRadio mAkRadio;

    public static AkRadio getRadioHadware() {
        return mAkRadio;
    }

    private static RadioService mThis;

    public static RadioService getInstanse(Context context) {
        if (mThis == null) {
            mThis = new RadioService(context);

            mThis.onCreate();
        }
        return mThis;
    }

    public RadioService(Context context) {
        super(context);
    }

    public void onDestroy() {
    }

    private static Handler[] mHandlerPresentation = new Handler[2];

    public static void setHandler(Handler h, int index) {
        if (index < mHandlerPresentation.length) {
            mHandlerPresentation[index] = h;
        }
    }

    // IMRDCallback mMRDCallback = new IMRDCallback() {
    // public void mrdCallback(int value) {
    // for (int i = 0; i < mHandlerPresentation.length; ++i) {
    // if (mHandlerPresentation[i] != null) {
    // mHandlerPresentation[i].sendEmptyMessage(value);
    // }
    // }
    // }
    // };

    private void returnInfoToUI(int value) {
        for (int i = 0; i < mHandlerPresentation.length; ++i) {
            if (mHandlerPresentation[i] != null) {
                mHandlerPresentation[i].sendEmptyMessage(value);
            }
        }
    }

    public void onCreate() {

        mAkRadio = new AkRadio(mContext);

        mAkRadio.sendMRDCommand(AkRadio.MRD_OPEN);

        mAkRadio.sendMRDCommand(AkRadio.MRD_QUERY_RADIO_INFO);
        // mAkRadio.sendMRDCommand(AkRadio.MRD_QUERY_RDS_INFO);
        returnInfoToUI(AkRadio.MRD_INIT);

        // getAllSaveData();
    }

    public void doCmd(int cmd, Intent intent) {
        switch (cmd) {
            case MyCmd.Cmd.MCU_RADIO_RECEIVE_DATA:
                byte[] buf = intent.getByteArrayExtra(MyCmd.EXTRA_COMMON_DATA);
                int ret = mAkRadio.doMcuData(buf);
                if (ret != 0) {
                    returnInfoToUI(ret);
                    if (AkRadio.MRD_RDS_INFO == ret) {
                        if (((mAkRadio.mMRDRdsFlag & 0x2) != 0)
                                && ((mAkRadio.mMRDRdsFlag & 0x20) != 0)) {
                            showTA();
                        } else {
                            hideTA();
                        }
                    }
                }
                break;
            case MyCmd.Cmd.RETURN_ABANDON_FOCUS:
                GlobalDef.abandonAudioFocus(mAudioFocusListener);
                break;
        }
    }

    private TAUI mTAUI;

    private void showTA() {
        if (mTAUI == null) {
            mTAUI = new TAUI(mContext, mAkRadio);
        }
        mTAUI.show();
    }

    private void hideTA() {
        if (mTAUI == null) {
            mTAUI = new TAUI(mContext, mAkRadio);
        }
        mTAUI.hide();
    }


    public void doEQResule() {
        returnInfoToUI(GlobalDef.MSG_UPDATE_EQ_MODE);
    }

    public void doKeyControl(int code) {
        switch (code) {
            case MyCmd.Keycode.NUMBER1:
            case MyCmd.Keycode.NUMBER2:
            case MyCmd.Keycode.NUMBER3:
            case MyCmd.Keycode.NUMBER4:
            case MyCmd.Keycode.NUMBER5:
            case MyCmd.Keycode.NUMBER6:
            case MyCmd.Keycode.NUMBER7:
            case MyCmd.Keycode.NUMBER8:
            case MyCmd.Keycode.NUMBER9:
                mAkRadio.sendMRDCommand(AkRadio.MRD_LIST_INDEX, (code - MyCmd.Keycode.NUMBER1));
                break;

            case MyCmd.Keycode.KEY_SEEK_NEXT:
                mAkRadio.sendMRDCommand(AkRadio.MRD_SEEK_DOWN);
                break;
            case MyCmd.Keycode.KEY_TURN_A:
            case MyCmd.Keycode.KEY_DVD_RIGHT:
                mAkRadio.sendMRDCommand(AkRadio.MRD_STEP_DOWN);
                break;
            case MyCmd.Keycode.CH_UP:
            case MyCmd.Keycode.NEXT:
            case MyCmd.Keycode.KEY_DVD_UP:
                mAkRadio.sendMRDCommand(AkRadio.MRD_NEXT_LIST_CHANNEL);
                break;

            case MyCmd.Keycode.KEY_SEEK_PREV:
                mAkRadio.sendMRDCommand(AkRadio.MRD_SEEK_UP);
                break;

            case MyCmd.Keycode.KEY_TURN_D:
            case MyCmd.Keycode.KEY_DVD_LEFT:
                mAkRadio.sendMRDCommand(AkRadio.MRD_STEP_UP);
                break;

            case MyCmd.Keycode.CH_DOWN:
            case MyCmd.Keycode.PREVIOUS:
            case MyCmd.Keycode.KEY_DVD_DOWN:
                mAkRadio.sendMRDCommand(AkRadio.MRD_PREV_LIST_CHANNEL);
                break;
            case MyCmd.Keycode.FAST_F:
                mAkRadio.sendMRDCommand(AkRadio.MRD_SEEK_DOWN);
                break;
            case MyCmd.Keycode.FAST_R:
                mAkRadio.sendMRDCommand(AkRadio.MRD_SEEK_UP);
                break;
            case MyCmd.Keycode.AS:
                mAkRadio.sendMRDCommand(AkRadio.MRD_AS);
                break;
            case MyCmd.Keycode.RDS_TA_SWITCH:
                if ((mAkRadio.mMRDRdsFlag & 0x20) != 0) {
                    mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_TA_OFF);
                } else {
                    mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_TA_ON);
                }
                break;
            case MyCmd.Keycode.RADIO:
                mAkRadio.doKeyBaud();
                break;
            case MyCmd.Keycode.KEY_ST_PROG:
                if ((mAkRadio.mMRDFlag & 0x2) != 0) {
                    mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_ST_OFF);
                } else {
                    mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_ST_ON);
                }
                break;

            case MyCmd.Keycode.KEY_RADIO_PS:
                mAkRadio.sendMRDCommand(AkRadio.MRD_PS);
                break;
            case MyCmd.Keycode.KEY_RADIO_SCAN:
                mAkRadio.sendMRDCommand(AkRadio.MRD_SCAN);
                break;
            case MyCmd.Keycode.RADIO_POWER: {
                if (mAkRadio != null) {
                    mAkRadio.reportCanboxPlayStatus();
                }
                break;
            }
        }
    }

    //	public static OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
//		public void onAudioFocusChange(int focusChange) {
//			MyCmd.SOURCE_OTHERS_APPS
//		}
//	};
    private static boolean mPausedByTransientLossOfFocus = false;
    public static OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            // AudioFocus is a new feature: focus updates are made verbose on
            // purpose

            Log.v(TAG, "onAudioFocusChange " + focusChange);

            if (GlobalDef.mContext == null) {
                return;
            }
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (!GlobalDef.isAudioFocusGPS())
                    {
                        if (GlobalDef.mSource == RadioUI.SOURCE)
                        {
                            //if (mServiceBase.mPlayStatus == BTMusicService.A2DP_INFO_PLAY) {
                            mPausedByTransientLossOfFocus = true;
                            GlobalDef.setSource(GlobalDef.mContext, MyCmd.SOURCE_OTHERS_APPS);
                            //}
                        }
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mPausedByTransientLossOfFocus) {
                        mPausedByTransientLossOfFocus = false;
                        GlobalDef.setSource(GlobalDef.mContext, RadioUI.SOURCE);
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


    //	public  OnAudioFocusChangeListener getAudioFocusChangeListener(){
//		return mAudioFocusListener;
//	}
    public int getSource() {
        return RadioUI.SOURCE;
    }
    // private boolean mSaveRadioStatus = false;

    // public void saveRadioStatus() {
    // if (!mSaveRadioStatus) {
    // // if (AkRadio.mPower) {
    // mSaveRadioStatus = true;
    // mAkRadio.mMRDNumber = 0;
    // mAkRadio.sendMRDCommand(AkRadio.MRD_SAVE);
    // // }
    // }
    // }
    //
    // public void resetRadioStatus() {
    // if (mSaveRadioStatus) {
    // mAkRadio.mMRDNumber = 1;
    // mAkRadio.sendMRDCommand(AkRadio.MRD_SAVE);
    // mSaveRadioStatus = false;
    // }
    // }

    public static void doPlayPauseKey() {
        // AkRadio.mPower = !AkRadio.mPower;
        // if (AkRadio.mPower) {
        // mAkRadio.mMRDNumber = 1;
        // mAkRadio.sendMRDCommand(AkRadio.MRD_SAVE);
        // } else {
        // mAkRadio.mMRDNumber = 0;
        // mAkRadio.sendMRDCommand(AkRadio.MRD_SAVE);
        // }
        // mAkRadio.sendBroadcastRadioInformation(mThis);
        // if (RadioActivity.mThis != null){
        // RadioActivity.mThis.doPower(AkRadio.mPower);
        // }
    }

    // for save data
    // private static String SAVE_DATA = "radio_data";
    // private static String SAVE_FM_LIST_FREQ = "fm_list1";
    // private static String SAVE_AM_LIST_FREQ = "am_list1";
    // private static String SAVE_FM_LIST_FAVORITE = "fm_list2";
    // private static String SAVE_AM_LIST_FAVORITE = "am_list2";
    // private static String SAVE_FM_NUM = "fm_num";
    // private static String SAVE_AM_NUM = "am_num";
    // private static String SAVE_CUR_FREQ_AM = "freqa";
    // private static String SAVE_CUR_FREQ_FM = "freqf";
    // private static String SAVE_CUR_BAUD = "baud";
    //
    // private void saveFreqListData() {
    // int i = 0;
    // if (mAkRadio.mMRDBand == AkRadio.MRD_AM) {
    // for (i = 0; i < mAkRadio.mMRDFreqs.length; ++i) {
    // if (mAkRadio.mMRDFreqs[i] != 0) {
    // saveData(SAVE_AM_LIST_FREQ + i, mAkRadio.mMRDFreqs[i]);
    // } else {
    // break;
    // }
    // }
    // saveData(SAVE_AM_NUM, i);
    // } else {
    // for (i = 0; i < mAkRadio.mMRDFreqs.length; ++i) {
    // if (mAkRadio.mMRDFreqs[i] != 0) {
    // saveData(SAVE_FM_LIST_FREQ + i, mAkRadio.mMRDFreqs[i]);
    // } else {
    // break;
    // }
    // }
    // saveData(SAVE_FM_NUM, i);
    // }
    //
    // }
    //
    // public void saveCurFreq() {
    // saveData(SAVE_CUR_BAUD, mAkRadio.mMRDBand);
    //
    // if (mAkRadio.mMRDBand == AkRadio.MRD_AM) {
    // mAkRadio.mMRDAmFreqency = mAkRadio.mMRDFreqency;
    // saveData(SAVE_CUR_FREQ_AM, mAkRadio.mMRDAmFreqency);
    // } else {
    // mAkRadio.mMRDFmFreqency = mAkRadio.mMRDFreqency;
    // saveData(SAVE_CUR_FREQ_FM, mAkRadio.mMRDFmFreqency);
    // }
    // }
    //
    // private void getAllSaveData() {
    //
    // mAkRadio.mMRDAmFreqency = (short) getData(SAVE_CUR_FREQ_AM, 531);
    // mAkRadio.mMRDFmFreqency = (short) getData(SAVE_CUR_FREQ_FM, 8750);
    // mAkRadio.mMRDBand = (byte) getData(SAVE_CUR_BAUD);
    // if (mAkRadio.mMRDBand == AkRadio.MRD_AM) {
    // mAkRadio.mMRDFreqency = mAkRadio.mMRDAmFreqency;
    // } else {
    // mAkRadio.mMRDFreqency = mAkRadio.mMRDFmFreqency;
    // }
    //
    // int num;
    // int i;
    //
    // i = 0;
    // num = getData(SAVE_AM_NUM);
    // if (num > 0 && num < AkRadio.MAX_FREQ_LIST) {
    // for (i = 0; i < num; ++i) {
    // mAkRadio.mMRDFreqsAM[i] = (short) getData(SAVE_AM_LIST_FREQ + i);
    // }
    // }
    //
    // i = 0;
    // num = getData(SAVE_FM_NUM);
    // if (num > 0 && num < AkRadio.MAX_FREQ_LIST) {
    // for (i = 0; i < num; ++i) {
    // mAkRadio.mMRDFreqsFM[i] = (short) getData(SAVE_FM_LIST_FREQ + i);
    // }
    // }
    //
    // if (mAkRadio.mMRDBand == AkRadio.MRD_AM) { // am
    // for (i = 0; i < num; ++i) {
    // mAkRadio.mMRDFreqs[i] = mAkRadio.mMRDFreqsAM[i];
    // }
    // } else {
    // for (i = 0; i < num; ++i) {
    // mAkRadio.mMRDFreqs[i] = mAkRadio.mMRDFreqsFM[i];
    // }
    // }
    //
    // }
    //
    // private int getData(String s) {
    // SharedPreferences sharedata = mContext.getSharedPreferences(SAVE_DATA,
    // 0);
    // return sharedata.getInt(s, 0);
    // }
    //
    // private int getData(String s, int def) {
    // SharedPreferences sharedata = mContext.getSharedPreferences(SAVE_DATA,
    // 0);
    // return sharedata.getInt(s, def);
    // }
    //
    // private void saveData(String s, int v) {
    // SharedPreferences.Editor sharedata = mContext.getSharedPreferences(
    // SAVE_DATA, 0).edit();
    // sharedata.putInt(s, v);
    //
    // sharedata.commit();
    // }

}
