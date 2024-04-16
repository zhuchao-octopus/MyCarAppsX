package com.octopus.android.carapps.radio;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import com.octopus.android.carapps.R;

public class TAUI {
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View mView;
    private boolean isShow = false;
    AkRadio mAkRadio;

    public TAUI(Context c, AkRadio radio) {
        mView = ((LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.radio_ta_layout, null);
        mAkRadio = radio;
        ((ImageView) mView.findViewById(R.id.radio_ta_switch)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_TA_OFF);
                //						hide();
            }
        });

        mWindowManager = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0, LayoutParams.TYPE_PHONE, LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.RGBA_8888);

    }

    public void show() {
        if (!isShow) {
            isShow = true;

            mWindowManager.addView(mView, mLayoutParams);
        }
    }

    // public void show() {
    // show(true);
    // }

    public void hide() {
        if (isShow) {
            mWindowManager.removeView(mView);
            isShow = false;

        }
    }
}
