package com.octopus.android.carapps.frontcamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;

import com.common.util.MyCmd;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.common.ui.GlobalDef;
import com.octopus.android.carapps.common.service.ServiceBase;

public class FrontCameraService extends ServiceBase {
    public static final String TAG = "RadioService";

    private static FrontCameraService mThis;

    public static FrontCameraService getInstanse(Context context) {
        //		if (mThis == null) {
        mThis = new FrontCameraService(context);

        mThis.onCreate();
        //		}
        return mThis;
    }

    public static String SAVE_DATA = "FrontCameraUI";

    private String getData(String s) {
        SharedPreferences sharedata = mContext.getSharedPreferences(SAVE_DATA, 0);
        return sharedata.getString(s, null);
    }

    public void release() {
        onDestroy();
    }

    public FrontCameraService(Context context) {
        super(context);
    }

    public void onDestroy() {
        unregisterListener();
    }

    public void onCreate() {
        registerListener();
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
                    FrontCameraUI aui;
                    for (int i = 0; i < 2; i++) {
                        aui = FrontCameraUI.mUI[i];
                        if (aui != null && !aui.mPause) {
                            mReoverSource = i;
                            aui.reverseStart();
                            break;
                        }
                    }
                } else {
                    if (mReoverSource >= 0 && mReoverSource < 2) {
                        FrontCameraUI.mUI[mReoverSource].reverseStop();
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

    public static void reverseCome(int cmd, Intent intent) {
        if (mThis != null) {
            mThis.doCmd(cmd, intent);
        }
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

                    if (action.equals(MyCmd.BROADCAST_CMD_TO_CARUI_CAMERA)) {
                        String s;
                        s = getData("" + R.id.cam1);
                        sendCamName(s, 1);
                        s = getData("" + R.id.cam2);
                        sendCamName(s, 2);
                        s = getData("" + R.id.cam3);
                        sendCamName(s, 3);
                        s = getData("" + R.id.cam4);
                        sendCamName(s, 4);
                    }

                }
            };
            IntentFilter iFilter = new IntentFilter();

            iFilter.addAction(MyCmd.BROADCAST_CMD_TO_CARUI_CAMERA);

            mContext.registerReceiver(mReceiver, iFilter);
        }
    }

    private void sendCamName(String s, int index) {
        if (s == null) {
            return;
        }
        Intent it = new Intent(MyCmd.BROADCAST_CMD_FROM_CARUI_CAMERA);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, 0);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, index);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA2, s);

        mContext.sendBroadcast(it);
    }
}
