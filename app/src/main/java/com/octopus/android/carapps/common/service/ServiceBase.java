package com.octopus.android.carapps.common.service;

import android.content.Context;
import android.content.Intent;

import com.common.util.MyCmd;

public abstract class ServiceBase {
    public Context mContext;

    public ServiceBase(Context context) {
        mContext = context;
    }

    public abstract void onCreate();

    public void onDestroy() {
    }

    public void doCmd(int cmd, Intent intent) {

    }

    public void doKeyControl(int code) {

    }

    public void doEQResule() {

    }

    public void abandonAudioFocus() {

    }

    //	public OnAudioFocusChangeListener getAudioFocusChangeListener() {
    //		return null;
    //	}

    public int getSource() {
        return MyCmd.SOURCE_NONE;
    }

}
