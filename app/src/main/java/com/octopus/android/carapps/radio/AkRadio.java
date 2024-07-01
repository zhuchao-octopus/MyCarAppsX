package com.octopus.android.carapps.radio;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.ProtocolAk47;
import com.common.util.Util;
import com.octopus.android.carapps.common.ui.GlobalDef;

import java.util.ArrayList;

public class AkRadio {

    private Context mContext;

    public static final int MRD_FM1 = 0;
    public static final int MRD_FM2 = 1;
    public static final int MRD_FM3 = 2;
    public static final int MRD_AM = 3;
    public static final int MRD_AM2 = 4;

    public static final int MAX_FREQ_LIST = 50;
    public static final int MIN_FREQ_LIST = 30;

    public static final int AM_FREQ_LIST = 12;
    public static final int FM_FREQ_LIST = MIN_FREQ_LIST;

    public static int mFMNum = 1;

    public AkRadio(Context c) {
        mContext = c;
        mFreqListSize = MIN_FREQ_LIST;

        // default
        mMRDBand = MRD_FM1;
        // for (int i = 0; i < MIN_FREQ_LIST; ++i) {
        // mMRDFreqs[i] = 8750;
        // }
        initSaveData();

        if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI35_KLD813_2.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI21_RM12.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI45_8702_2.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI44_KLD007.equals(GlobalDef.getSystemUI())) {
            mFMNum = 3;
        }
    }

    private static final String TAG = "AkRadio";

    public static final int MRD_QUERY_RADIO_INFO = 0x1;
    public static final int MRD_QUERY_RDS_INFO = 0x2;

    public static final int MRD_NEXT_LIST_CHANNEL = 0x3;
    public static final int MRD_PREV_LIST_CHANNEL = 0x4;

    public static final int MRD_LIST_INDEX = 0x5;

    public static final int MRD_READ_FREQS = 0x6;
    public static final int MRD_READ_REGION = 0x7;
    public static final int MRD_READ_PARAM = 0x8;
    public static final int MRD_WRITE_BAND = 0x10;
    public static final int MRD_AS = 0x11;
    public static final int MRD_PS = 0x12;
    public static final int MRD_SCAN = 0x13;
    public static final int MRD_SEEK_UP = 0x14;
    public static final int MRD_SEEK_DOWN = 0x15;
    public static final int MRD_STEP_UP = 0x16;
    public static final int MRD_STEP_DOWN = 0x17;
    public static final int MRD_SCAN_STOP = 0x18;
    public static final int MRD_WRITE_LOCDX = 0x19;
    public static final int MRD_PLAY = 0x1a;
    public static final int MRD_SAVE = 0x1b;
    public static final int MRD_WRITE_REGION = 0x1c;
    public static final int MRD_WRITE_FREQENCY = 0x1d;
    public static final int MRD_WRITE_PARAM = 0x1e;
    public static final int MRD_INFO = 0x1f;

    public static final int MRD_READ_RDS = 0x20;
    public static final int MRD_READ_RDS_FLAG = 0x21;
    public static final int MRD_READ_RDS_PTY = 0x22;
    public static final int MRD_READ_RDS_PS = 0x23;
    public static final int MRD_READ_RDS_RT = 0x24;
    public static final int MRD_READ_RDS_PSLIST = 0x25;
    public static final int MRD_RDS_AF_OFF = 0x30;
    public static final int MRD_RDS_AF_ON = 0x31;
    public static final int MRD_RDS_TA_OFF = 0x32;
    public static final int MRD_RDS_TA_ON = 0x33;
    public static final int MRD_RDS_EON_OFF = 0x34;
    public static final int MRD_RDS_EON_ON = 0x35;
    public static final int MRD_RDS_REG_OFF = 0x36;
    public static final int MRD_RDS_REG_ON = 0x37;
    public static final int MRD_RDS_PTY_OFF = 0x38;
    public static final int MRD_RDS_PTY_ON = 0x39;
    public static final int MRD_RDS_PTY_TYPE = 0x3a;
    public static final int MRD_RDS_PTY_SEEK_UP = 0x3b;
    public static final int MRD_RDS_PTY_SEEK_DOWN = 0x3c;
    public static final int MRD_RDS_INFO = 0x3f;
    public static final int MRD_RDS_READ_FREQ_RANGE = 0x40;
    public static final int MRD_RDS_PTY = 0x41;
    public static final int MRD_RDS_PS = 0x42;

    public static final int MRD_RDS_ST_OFF = 0x43;
    public static final int MRD_RDS_ST_ON = 0x44;

    public static final int MRD_WRITE_BAND2 = 0x45;

    public static final int MRD_OPEN = 0x80;
    public static final int MRD_CLOSE = 0xff;
    public static final int SHOW_TA = 0x1000;
    public static final int HIDE_TA = 0x1001;

    public static final int MRD_INFO_BAND = 0x00000001;
    public static final int MRD_INFO_NUMBER = 0x00000002;
    public static final int MRD_INFO_FREQENCY = 0x00000004;
    public static final int MRD_INFO_STRENGTH = 0x00000008;
    public static final int MRD_INFO_FLAG = 0x00000010;
    public static final int MRD_INFO_FREQS = 0x00000020;
    public static final int MRD_INFO_REGION = 0x00000040;
    public static final int MRD_INFO_PARAM = 0x00000080;
    public static final int MRD_INFO_PS = 0x00040000;
    public static final int MRD_INFO_LOCDX = 0x00080000;
    public static final int MRD_INFO_STEREOMONO = 0x00100000;
    public static final int MRD_INFO_SCAN = 0x00200000;
    public static final int MRD_INFO_AS = 0x00400000;
    public static final int MRD_INFO_SEEK = 0x00800000;
    public static final int MRD_INFO_FREQ_RANGE = 0x01000000;
    public static final int MRD_INIT = 0x80000000;

    public static final int MRD_INFO_FLAG_PS = 0x04;
    public static final int MRD_INFO_FLAG_LOCDX = 0x08;
    public static final int MRD_INFO_FLAG_STEREOMONO = 0x10;
    public static final int MRD_INFO_FLAG_SCAN = 0x20;
    public static final int MRD_INFO_FLAG_AS = 0x40;
    public static final int MRD_INFO_FLAG_SEEK = 0x80;

    public static final int MRD_RDS_FLAG = 0x00000200;

    public static final int MRD_RDS_RT = 0x00001000;
    public static final int MRD_RDS_PSLIST = 0x00002000;
    public static final int MRD_RDS_DEBUG_INFO = 0x00008000;
    public static final int MRD_RDS_TA = 0x02000000;
    public static final int MRD_RDS_REG_SW = 0x040000000;
    public static final int MRD_RDS_EON_SW = 0x08000000;
    public static final int MRD_RDS_PTY_SW = 0x10000000;
    public static final int MRD_RDS_TA_SW = 0x20000000;
    public static final int MRD_RDS_AF_SW = 0x40000000;
    public static final int MRD_RDS_TP = 0x80000000;

    public static final int MRD_RDS_FLAG_TA = 0x02;
    public static final int MRD_RDS_FLAG_REG_SW = 0x04;
    public static final int MRD_RDS_FLAG_EON_SW = 0x08;
    public static final int MRD_RDS_FLAG_PTY_SW = 0x10;
    public static final int MRD_RDS_FLAG_TA_SW = 0x20;
    public static final int MRD_RDS_FLAG_AF_SW = 0x40;
    public static final int MRD_RDS_FLAG_TP = 0x80;

    public static final int MRD_READ_DEBUG_INFO = 0xf0;
    public static final int MRD_READ_RDS_DEBUG_INFO = 0xf1;

    private native final int nativeSendMRDCommand(int value);

    public int sendMRDCommand(int value, int param) {
        switch (value) {

            case MRD_WRITE_FREQENCY:
                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_SET_CURRENT_FREQUENCY, (param & 0xff00) >> 8, param & 0xff);
                GlobalDef.setSource(mContext, RadioUI.SOURCE);
                break;
            case MRD_RDS_PTY_TYPE:
                BroadcastUtil.sendToCarServiceMcuRds(mContext, ProtocolAk47.SEND_RDS_SUB_OPT, 0x1, param);
                break;
            case MRD_SAVE:
                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 0x4, param);

                break;

            case MRD_LIST_INDEX:
                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 0x3, param);

                break;
        }
        return 0;
    }

    public int sendMRDCommand(int value) {
        boolean cmd = true;
        // Log.d(TAG, "sendMRDCommand:" + value);
        switch (value) {

            case MRD_WRITE_FREQENCY:
                sendMRDCommand(value, mMRDFreqency);
                break;
            case MRD_SEEK_DOWN:
                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 1, 4);
                break;
            case MRD_SEEK_UP:
                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 1, 3);
                break;

            case MRD_AS:
                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 1, 0);
                break;
            case MRD_PS:
                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 1, 1);
                break;
            case MRD_SCAN:
                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 1, 2);
                break;
            case MRD_STEP_UP:
                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 1, 5);
                break;
            case MRD_STEP_DOWN:
                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 1, 6);
                break;
            case MRD_PREV_LIST_CHANNEL:
                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 1, 8);
                break;
            case MRD_NEXT_LIST_CHANNEL:
                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 1, 9);
                break;
            // BroadcastUtil.sendToCarServiceMcuRadio(mContext,
            // ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 1, 6);
            // break;
            case MRD_WRITE_BAND:
                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 0, mMRDBand);
                break;
            case MRD_WRITE_BAND2:
                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 0, 0xff);
                break;
            case MRD_WRITE_LOCDX:

                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 2, (mMRDFlag & MRD_INFO_FLAG_LOCDX) != 0 ? 1 : 0);

                break;
            case MRD_QUERY_RADIO_INFO:
                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_QUERY_RADIO_INFO, 0);
                break;
            case MRD_RDS_AF_ON:
                BroadcastUtil.sendToCarServiceMcuRds(mContext, ProtocolAk47.SEND_RDS_SUB_OPT, 0, 1);
                break;
            case MRD_RDS_AF_OFF:
                BroadcastUtil.sendToCarServiceMcuRds(mContext, ProtocolAk47.SEND_RDS_SUB_OPT, 0, 0);
                break;
            case MRD_QUERY_RDS_INFO:
                BroadcastUtil.sendToCarServiceMcuRds(mContext, ProtocolAk47.SEND_RDS_SUB_QUERY, 0);
                break;
            case MRD_RDS_TA_ON:
                BroadcastUtil.sendToCarServiceMcuRds(mContext, ProtocolAk47.SEND_RDS_SUB_OPT, 0, 3);
                break;
            case MRD_RDS_TA_OFF:
                BroadcastUtil.sendToCarServiceMcuRds(mContext, ProtocolAk47.SEND_RDS_SUB_OPT, 0, 2);
                break;
            case MRD_RDS_ST_ON:
                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 0x5);
                break;
            case MRD_RDS_ST_OFF:
                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 0x6);
                break;
            default:
                cmd = false;
                break;
        }
        if (cmd) {
            // GlobalDef.setSource(mContext, RadioUI.SOURCE);
        }
        return 0;// nativeSendMRDCommand(value);
    }

    private IMRDCallback mMRDCallback;

    private void mrdCallback(int value) {
        if (mMRDCallback != null) {
            mMRDCallback.mrdCallback(value);
        }
    }

    public void setMRDCallback(IMRDCallback mrdCallback) {
        mMRDCallback = mrdCallback;
    }

    // private int mMRDMask = 0xffffffff;

    // public void setMRDMask(int value) {
    // if (value == 0) {
    // value = 0xffffffff;
    // }
    // mMRDMask = value;
    // }

    public static final int MY_KEY_BAUD = 0x1;
    public static final int MY_BIND_SERVICE_FINISH = 0x2;

    public static final int MY_KEY_AS = 0x5;
    public static final int MY_KEY_NUM1 = 0x6;
    public static final int MY_KEY_NUM2 = 0x7;
    public static final int MY_KEY_NUM3 = 0x8;
    public static final int MY_KEY_NUM4 = 0x9;
    public static final int MY_KEY_NUM5 = 0xa;
    public static final int MY_KEY_NUM6 = 0xb;

    public static final int MY_KEY_LONG_NUM1 = 0x16;
    public static final int MY_KEY_LONG_NUM2 = 0x17;
    public static final int MY_KEY_LONG_NUM3 = 0x18;
    public static final int MY_KEY_LONG_NUM4 = 0x19;
    public static final int MY_KEY_LONG_NUM5 = 0x1a;
    public static final int MY_KEY_LONG_NUM6 = 0x1b;

    public byte mMRDBand;
    public byte mMRDNumber;
    public short mMRDFreqency;
    public byte mMRDStrength;
    public byte mMRDFlag;
    public short[] mMRDFreqs = new short[MAX_FREQ_LIST];
    public byte mMRDRegion;
    public byte[] mMRDParam = new byte[4];

    public byte mMRDRdsFlag;
    public byte[] mMRDRdsPty = new byte[2];
    public byte[] mMRDRdsPs = new byte[8];
    public byte[] mMRDRdsRt = new byte[64];
    public byte[][] mMRDRdsPslist = new byte[12][8];
    public byte[] mMRDRdsDinfo = new byte[32];
    public byte mMRDRdsPtyType;
    public short mFreqMax;
    public short mFreqMin;

    public String mStrRdsPs = null;

    public int mCurPlayIndex = 0;
    // for save
    // public short mMRDFmFreqency;
    // public short mMRDAmFreqency;
    //
    // public short[] mMRDFreqsFM = new short[MAX_FREQ_LIST];
    // public short[] mMRDFreqsAM = new short[MAX_FREQ_LIST];

    // /
    public static int mFreqListSize = MIN_FREQ_LIST;

    // ///////////
    public int doMcuData(byte[] buf) {
        int ret = 0;
        if (buf != null && buf.length > 1) {
            switch (buf[0]) {
                case ProtocolAk47.TYPE_RADIO_RECEIVE: {
                    switch (buf[1]) {
                        case ProtocolAk47.RECEVE_RADIO_SUB_RADIO_CURRENT_INFO:
                            if (buf.length > 11) {
                                mMRDBand = buf[2];
                                mMRDFreqency = (short) ((((int) buf[4] & 0xff) << 8) | ((int) buf[5]) & 0xff);
                                mMRDStrength = buf[6];
                                mMRDFlag = buf[7];
                                mFreqMin = (short) ((((int) buf[8] & 0xff) << 8) | ((int) buf[9]) & 0xff);
                                mFreqMax = (short) ((((int) buf[10] & 0xff) << 8) | ((int) buf[11]) & 0xff);
                                mCurPlayIndex = (int) (buf[3] & 0xff);

                                ret = MRD_INFO_FREQENCY;
                                // Log.d(TAG,
                                // "doMcuData RECEVE_RADIO_SUB_RADIO_CURRENT_INFO");
                                Log.d(TAG, Integer.toHexString(((int) mMRDFlag & 0xff)) + ":" + mMRDFreqs[0] + ":" + mFreqMin);

                                reportCanboxPlayStatus();
                            }
                            break;
                        case ProtocolAk47.RECEVE_RADIO_SUB_PRESET_LIST_FREQUENCY_INFO: {
                            int len = buf.length - 2;
                            if (len > (mMRDFreqs.length * 2)) {
                                len = mMRDFreqs.length;
                            } else {
                                len /= 2;
                            }

                            for (int i = 0; i < mMRDFreqs.length; ++i) {
                                mMRDFreqs[i] = 0;
                            }

                            for (int i = 0; i < len; ++i) {
                                mMRDFreqs[i] = (short) ((((int) buf[2 * i + 2] & 0xff) << 8) | ((int) buf[2 * i + 3]) & 0xff);
                                //						Log.d("kkk", i + ":" + mMRDFreqs[i]);
                            }
                            ret = MRD_INFO_FREQS;
                            // Log.d(TAG, "RECEVE_RADIO_SUB_RADIO_CURRENT_INFO");
                            // Log.d(TAG, mMRDBand + ":" + mMRDFreqs[0]);
                        }
                        break;

                    }
                }
                break;
                case ProtocolAk47.TYPE_RDS_RECEIVE: {
                    switch (buf[1]) {
                        case ProtocolAk47.RECEVE_RDS_SUB_RDS_FLAGS:
                            if (buf.length >= 3) {
                                mMRDRdsFlag = buf[2];
                            }
                            ret = MRD_RDS_INFO;
                            break;
                        case ProtocolAk47.RECEVE_RDS_SUB_PTY_INFO:
                            if (buf.length >= 4) {
                                mMRDRdsPty[0] = buf[2];
                                mMRDRdsPty[1] = buf[3];
                            }
                            ret = MRD_RDS_PTY;
                            break;
                        case ProtocolAk47.RECEVE_RDS_SUB_PS_INFO:
                            if (buf.length >= 10) {
                                Util.byteArrayCopy(mMRDRdsPs, buf, 0, 2, mMRDRdsPs.length);
                                if (mMRDRdsPs[0] != 0) {
                                    mStrRdsPs = new String(mMRDRdsPs);
                                } else {
                                    mStrRdsPs = null;
                                }
                            }
                            ret = MRD_RDS_PS;
                            break;
                        case ProtocolAk47.RECEVE_RDS_SUB_RT_INFO:
                            Util.clearBuf(mMRDRdsRt);
                            // test
                            // buf = new byte[]{0x03, 0x05, 0x57, 0x65, 0x6c, 0x63,
                            // 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x00};
                            Util.byteArrayCopy(mMRDRdsRt, buf, 0, 2, (buf.length - 2));
                            // mStrRdsPs = new String(mMRDRdsPs);

                            ret = MRD_RDS_RT;
                            break;

                    }
                }
                break;
            }

        }
        return ret;
    }

    public void reportCanboxPlayStatus() {
        int curChannel = -1;
        if (mCurPlayIndex >= 0 && mCurPlayIndex < mMRDFreqs.length) {
            if (mMRDFreqs[mCurPlayIndex] == mMRDFreqency) {
                curChannel = mCurPlayIndex;
            }
        }
        BroadcastUtil.sendCanboxInfo(mContext, MyCmd.RADIO_SOURCE_CHANGE, (int) mMRDFreqency, mMRDBand, curChannel);

        // if (GlobalDef.mIs8600) {
        // if (GlobalDef.mSource == MyCmd.SOURCE_RADIO){
        // String s = null;
        // if (mMRDBand < MRD_AM) { // fm
        // s = "FM ";
        // if ((mMRDFreqency / 10000) <= 0){
        // s += " ";
        // }
        // String sf = (mMRDFreqency / 100)+"";
        // if ((mMRDFreqency % 100) < 10) {
        // sf += "0";
        // }
        // sf += (mMRDFreqency % 100);
        // s += sf;
        //
        // GlobalDef.setSmallLcdIcon("0x00080000");
        // } else {
        // s = "AM  ";
        // if ((mMRDFreqency / 1000) <= 0){
        // s += " ";
        // s += mMRDFreqency;
        // } else {
        // s += mMRDFreqency;
        // }
        //
        // GlobalDef.setSmallLcdIcon("0");
        // }
        //
        // if (s != null) {
        // GlobalDef.setSmallLcd(s);
        // }
        // }
        // }

    }

    // add rds ps name list
    private ArrayList<FreqAndPS> mPsList = new ArrayList<FreqAndPS>();

    public class FreqAndPS {
        public int mFreq;
        public String mPS;

        public FreqAndPS(int f, String s) {
            mFreq = f;
            mPS = s;
        }
    }

    public String getFreqPS(int freq) {
        String s = null;
        for (int i = 0; i < mPsList.size(); ++i) {
            FreqAndPS fp = mPsList.get(i);
            if (fp.mFreq == freq) {
                s = fp.mPS;
                break;
            }
        }
        return s;
    }

    public int addFreqPS(int freq, String ps) {
        int ret = 0;
        for (int i = 0; i < mPsList.size(); ++i) {
            FreqAndPS fp = mPsList.get(i);
            if (fp.mFreq == freq) {
                ret = 1;
                fp.mPS = ps;
                break;
            }
        }

        if (ret == 0) {
            mPsList.add(new FreqAndPS(freq, ps));
            saveAllData();
        }
        return ret;
    }

    public void removeFreqPS(int freq) {
        for (int i = 0; i < mPsList.size(); ++i) {
            FreqAndPS fp = mPsList.get(i);
            if (fp.mFreq == freq) {
                mPsList.remove(i);
                saveAllData();
                return;
            }
        }
    }

    private void initSaveData() {
        int num = getDataInt(SAVE_RADIO_NUM);
        for (int i = 0; i < num; ++i) {
            int freq = getDataInt("" + i);
            String ps = getData("a" + i);
            mPsList.add(new FreqAndPS(freq, ps));
        }
    }

    private void saveAllData() {
        // SharedPreferences.Editor sharedata = mContext.getSharedPreferences(
        // SAVE_RADIO_FREQ, 0).edit();
        // sharedata.clear();
        // sharedata.commit();

        saveData(SAVE_RADIO_NUM, mPsList.size());

        for (int i = 0; i < mPsList.size(); ++i) {
            FreqAndPS fp = mPsList.get(i);
            saveData("" + i, fp.mFreq);
            saveData("a" + i, fp.mPS);
        }
    }

    private static String SAVE_RADIO_FREQ = "save_radio_freq";
    private static String SAVE_RADIO_NUM = "save_radio_freq_num";

    private void saveData(String s, String v) {
        saveDataEx(SAVE_RADIO_FREQ, s, v);
    }

    private void removeData(String s) {
        SharedPreferences.Editor sharedata = mContext.getSharedPreferences(SAVE_RADIO_FREQ, 0).edit();
        sharedata.remove(s);
        sharedata.commit();
    }

    private void saveDataEx(String data, String s, String v) {
        SharedPreferences.Editor sharedata = mContext.getSharedPreferences(data, 0).edit();
        sharedata.putString(s, v);
        sharedata.commit();
    }

    private void saveData(String s, int v) {
        SharedPreferences.Editor sharedata = mContext.getSharedPreferences(SAVE_RADIO_FREQ, 0).edit();
        sharedata.putInt(s, v);
        sharedata.commit();
    }

    private int getDataInt(String s) {
        SharedPreferences sharedata = mContext.getSharedPreferences(SAVE_RADIO_FREQ, 0);
        return sharedata.getInt(s, 0);
    }

    private String getData(String s) {
        return getDataEx(SAVE_RADIO_FREQ, s);
    }

    private String getDataEx(String data, String s) {
        SharedPreferences sharedata = mContext.getSharedPreferences(data, 0);
        return sharedata.getString(s, null);
    }

    //
    public void doKeyBaud() {
        if (AkRadio.mFMNum == 1) {
            mMRDBand = (byte) ((mMRDBand == 0) ? 3 : 0);
        } else {
            mMRDBand = (byte) ((mMRDBand + 1) % 5);
        }
        sendMRDCommand(AkRadio.MRD_WRITE_BAND);
    }

    public void doKeyBaudEx(boolean forward) {
        if (AkRadio.mFMNum == 1) {
            doKeyBaud();
        } else {
            if (forward) {
                mMRDBand = (byte) ((mMRDBand + 1) % 5);
            } else {
                mMRDBand = (byte) ((mMRDBand + 5 - 1) % 5);
            }

            sendMRDCommand(AkRadio.MRD_WRITE_BAND);
        }
    }

    // public void doKeyAMFMSwitch(boolean forward) {
    //
    // if (mMRDBand <= 2) {
    //
    // if (forward) {
    // mMRDBand = (byte) ((mMRDBand + 1) % 3);
    // } else {
    // mMRDBand = (byte) ((mMRDBand + 3 - 1) % 3);
    // }
    //
    // } else {
    // int index = mMRDBand - 3;
    //
    // if (forward) {
    // index = (byte) ((index + 1) % 2);
    // } else {
    // index = (byte) ((index + 2 - 1) % 2);
    // }
    // mMRDBand = (byte) (3 + index);
    // }
    //
    // sendMRDCommand(AkRadio.MRD_WRITE_BAND);
    //
    // }

    // public String getCurBaundText(){
    // String s;
    // int index = 1;
    // if (mMRDBand >= MRD_AM){
    // s = "AM";
    // index = mMRDBand - MRD_AM;
    // } else {
    // s = "FM";
    // index = mMRDBand - MRD_FM1;
    // }
    //
    // if (AkRadio.mFMNum == 3){
    // s += (index+1);
    // }
    // return s;
    // }

    public void doKeyAMFMSwitch(boolean forward) {
        int index = 0;
        if (forward) {
            if (mMRDBand >= MRD_AM) {
                index = (mCurPlayIndex + 6) % 12;
            } else {
                index = (mCurPlayIndex + 6) % 30;
            }
        } else {
            if (mMRDBand >= MRD_AM) {
                index = (mCurPlayIndex - 6 + 12) % 12;
            } else {
                index = (mCurPlayIndex - 6 + 30) % 30;
            }

        }

        sendMRDCommand(AkRadio.MRD_LIST_INDEX, index);

        // if (mMRDBand <= 2) {
        //
        // if (forward) {
        // mMRDBand = (byte) ((mMRDBand + 1) % 3);
        // } else {
        // mMRDBand = (byte) ((mMRDBand + 3 - 1) % 3);
        // }
        //
        // } else {
        // int index = mMRDBand - 3;
        //
        // if (forward) {
        // index = (byte) ((index + 1) % 2);
        // } else {
        // index = (byte) ((index + 2 - 1) % 2);
        // }
        // mMRDBand = (byte) (3 + index);
        // }
        //
        // sendMRDCommand(AkRadio.MRD_WRITE_BAND);

    }

    public String getCurBaundText() {
        String s;
        int index = 1;

        if (MachineConfig.VALUE_SYSTEM_UI22_1050.equals(GlobalDef.getSystemUI())) {
            index = mCurPlayIndex / 6;
            if (mMRDBand >= MRD_AM) {
                s = "AM";

                if (index < 0 || index > 2) {
                    index = 0;
                }
            } else {
                s = "FM";
                if (index < 0 || index > 5) {
                    index = 0;
                }
            }

            s += (index + 1);
        } else {
            if (mMRDBand >= MRD_AM) {
                s = "AM";
                index = mMRDBand - MRD_AM;
            } else {
                s = "FM";
                index = mMRDBand - MRD_FM1;
            }

            if (AkRadio.mFMNum == 3) {
                s += (index + 1);
            }
        }
        return s;
    }
}
