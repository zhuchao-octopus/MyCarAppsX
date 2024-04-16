package com.octopus.android.carapps.common.ui;

import android.content.Context;
import android.view.View;

import com.octopus.android.carapps.car.ui.GlobalDef;

public class UIBase implements UIInterface {

    protected Context mContext;
    protected View mMainView;

    public boolean mPause = false;
    public int mSource;

    protected int mDisplayIndex; // 0 is main screen, 1 is second screen

    public UIBase(Context context, View view, int displayIndex) {
        mContext = context;
        mMainView = view;
        mDisplayIndex = displayIndex;

    }

    public void onCreate() {
        mPause = false;
    }

    public void onDestroy() {
        mPause = true;
    }

    public void onPause() {
        mPause = true;
    }

    public void onResume() {
        if (mDisplayIndex == 1) {
            udpateWallPaper();
            // AppConfig.updateSystemBackground(mContext, mMainView);
        }
        mPause = false;
    }

    public final static int SCREEN0_HIDE = 0;
    public final static int SCREEN0_SHOW_NORMAL = 1;
    public final static int SCREEN0_SHOW_VIDEO = 2;
    public final static int SCREEN0_SHOW_FULLSCREEN = 3;


    public final static int SCREEN0_SHOW_CD_PLAYER = 4;

    public int getScreen0Type() {
        return SCREEN0_HIDE;
    }

    public void udpateWallPaper() {
        if (mMainView != null && GlobalDef.mDrawableScreen1Wallpaper != null) {
            mMainView.setBackground(GlobalDef.mDrawableScreen1Wallpaper);
        }
    }


}
