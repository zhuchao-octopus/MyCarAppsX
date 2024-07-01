package com.octopus.android.carapps.common.view;

import static com.octopus.android.carapps.common.ui.GlobalDef.mContext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.octopus.android.carapps.R;

import java.util.Date;

public class SplitWidgetClockView extends LinearLayout {
    public SplitWidgetClockView(Context context) {
        this(context, null);
    }

    public SplitWidgetClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SplitWidgetClockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDetachedFromWindow() {
        // TODO Auto-generated method stub
        super.onDetachedFromWindow();
        Log.d("eec", "onDetachedFromWindow:");
        deinit();
    }

    @Override
    protected void onAttachedToWindow() {
        // TODO Auto-generated method stub
        super.onAttachedToWindow();
        Log.d("eec", "onAttachedToWindow:" + findViewById(R.id.clock_hour));
        init();
    }

    private View mViewHour;
    private View mViewMinutus;

    private void init() {
        mViewHour = findViewById(R.id.clock_hour);
        mViewMinutus = findViewById(R.id.clock_minute);
        registerReceiver();
        updateTime();
    }

    private void updateTime() {
        if (mViewHour != null) {

            Date curDate = new Date(System.currentTimeMillis());
            int h = curDate.getHours();

            String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

            if ("12".equals(strTimeFormat)) {
                if (h > 12) {
                    h -= 12;
                }
            }

            int m = curDate.getMinutes();
            int s = curDate.getSeconds();

            float rotate;

            rotate = ((h * 60 + m) / 720.0f) * 360.0f;// (h * 360.0f) / 12.0f;

            mViewHour.setRotation(rotate);

            rotate = (m * 360.0f) / 60.0f;

            mViewMinutus.setRotation(rotate);

            int second = ((60 - s) % 60);
            if (second == 0) {
                second = 60;
            }
        }

    }

    private void deinit() {
        unregisterReceiver();
    }

    private BroadcastReceiver mBroadcastReceiver;

    private void unregisterReceiver() {
        if (mBroadcastReceiver != null) {
            mContext.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    private void registerReceiver() {
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    Log.d("eec", "" + intent.getAction());
                    if (intent.getAction().equals(Intent.ACTION_TIME_CHANGED) || intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                        updateTime();
                    }
                }
            };
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);

        getContext().registerReceiver(mBroadcastReceiver, intentFilter);
    }
}
