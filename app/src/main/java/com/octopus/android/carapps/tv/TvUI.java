package com.octopus.android.carapps.tv;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.view.WindowManager;

import com.common.util.BroadcastUtil;
import com.common.util.Kernel;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.common.ui.GlobalDef;
import com.octopus.android.carapps.common.ui.UIBase;

public class TvUI extends UIBase {

    private static final String TAG = "TvUI";
    public static final int SOURCE = MyCmd.SOURCE_DTV;
    public static TvUI[] mUI = new TvUI[MAX_DISPLAY];

    public static TvUI getInstanse(Context context, View view, int index) {
        if (index >= MAX_DISPLAY) {
            return null;
        }

        mUI[index] = new TvUI(context, view, index);
        return mUI[index];
    }

    public TvUI(Context context, View view, int index) {
        super(context, view, index);
        mSource = SOURCE;
    }

    private static final int[] BUTTON_ON_CLICK = new int[]{R.id.camera_surfaceview};

    // private static final int[][] VIEW_HIDE = new int[][] {
    // {0 },
    // { R.id.eq } };

    private static final int[] VIEW_HIDE2 = new int[]{R.id.bottom_button_layout};

    public void updateFullUI() {

    }

    private void initPresentationUI() {
        for (int i : BUTTON_ON_CLICK) {
            View v = mMainView.findViewById(i);
            if (v != null) {
                // v.setOnClickListener(this);
            }
        }

    }

    private GestureDetector detector;

    public void onCreate() {
        super.onCreate();
        // detector = new GestureDetector(mContext, this);
        // mMainView.setOnTouchListener(this);
    }

    public boolean mWillDestory = false;

    @Override
    public void onPause() {
        super.onPause();
        recoverVideoOutForHDMITV();
        mHandler.removeMessages(MSG_CHECK_TV);
        BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.SET_SCREEN0_SOURCE, 0);
    }

    @Override
    public void onResume() {
        super.onResume();

        mTimeToCheck = 0;
        mLockVideo = 0;

        mMainView.findViewById(R.id.no_tv).setVisibility(View.GONE);
        Util.setFileValue(DTV_SWITCH, 1);// open dtv
        checkDTVStatus();
        //		setFullScreen();

        mLeftVideo = Util.getFileValue(MCU_VIDEO_OUT_L_NODE);
        mRightVideo = Util.getFileValue(MCU_VIDEO_OUT_R_NODE);
    }

    private void setFullScreen() {

        if (this.mDisplayIndex == SCREEN0) {
            ((Activity) mContext).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private boolean mIsFullScrean = true;

    private void quitFullScreen() {

        final WindowManager.LayoutParams attrs = ((Activity) mContext).getWindow().getAttributes();
        attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ((Activity) mContext).getWindow().setAttributes(attrs);
        ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

    }

    public void onClick(View v) {
    }

    private final static String DTV_SWITCH = "/sys/class/misc/lt8619b/device/enable_dtv";
    private final static String DTV_LOCK = "/sys/class/misc/lt8619b/device/lock";
    private final static int MSG_CHECK_TV = 1;

    private final static int MSG_SHOW_NO_SINGAL = 2;

    private final static int MSG_WILL_QUIT = 10;

    private int mTimeToCheck = 0;
    private Handler mHandler = new Handler(Looper.myLooper()) {
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_CHECK_TV:
                    checkDTVStatus();
                    break;
                case MSG_SHOW_NO_SINGAL:
                    mMainView.findViewById(R.id.no_signal).setVisibility(View.VISIBLE);
                    break;
                case MSG_WILL_QUIT:
                    Kernel.doKeyEvent(Kernel.KEY_BACK);
                    break;
            }
        }
    };

    private static final int MAX_CHECKTIME = 13;
    private int mLockVideo = 0;


    private static final String MCU_VIDEO_OUT_L_NODE = "/sys/class/ak/source/lindex";
    private static final String MCU_VIDEO_OUT_R_NODE = "/sys/class/ak/source/rindex";
    private static final int MCU_VIDEO_OUT_SOURCE_ARM = 7;
    private static final int MCU_VIDEO_OUT_SOURCE_TV = 3;

    private int mLeftVideo;
    private int mRightVideo;

    private void checkVideoOutForHDMITV() {
        if (mDisplayIndex != 0) {
            return;
        }
        if (mLeftVideo == MCU_VIDEO_OUT_SOURCE_ARM) {
            Util.setFileValue(MCU_VIDEO_OUT_L_NODE, MCU_VIDEO_OUT_SOURCE_TV);
        }

        if (mRightVideo == MCU_VIDEO_OUT_SOURCE_ARM) {
            Util.setFileValue(MCU_VIDEO_OUT_R_NODE, MCU_VIDEO_OUT_SOURCE_TV);
        }
    }

    private void recoverVideoOutForHDMITV() {

        if (mDisplayIndex != 0) {
            return;
        }
        if (mLeftVideo == MCU_VIDEO_OUT_SOURCE_ARM) {
            Util.setFileValue(MCU_VIDEO_OUT_L_NODE, MCU_VIDEO_OUT_SOURCE_ARM);
        }
        if (mRightVideo == MCU_VIDEO_OUT_SOURCE_ARM) {
            Util.setFileValue(MCU_VIDEO_OUT_R_NODE, MCU_VIDEO_OUT_SOURCE_ARM);
        }
        mLeftVideo = 0;
        mRightVideo = 0;
    }

    private void checkDTVStatus() {
        int lock = Util.getFileValue(DTV_LOCK);
        Log.d(TAG, "checkDTVStatus:" + lock);

        mHandler.removeMessages(MSG_CHECK_TV);

        if (lock == 1) {
            if (mLockVideo != lock && !mPause) {
                mLockVideo = lock;
                mMainView.findViewById(R.id.loading).setVisibility(View.GONE);
                // mMainView.findViewById(R.id.no_signal).setVisibility(View.VISIBLE);

                GlobalDef.reactiveSource(mContext, SOURCE, TVService.mAudioFocusListener);

                BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.SET_SCREEN0_SOURCE, 1);
                setFullScreen();


                mHandler.removeMessages(MSG_SHOW_NO_SINGAL);
                mHandler.sendEmptyMessageDelayed(MSG_SHOW_NO_SINGAL, 1000);

                mTimeToCheck = MAX_CHECKTIME;

                checkVideoOutForHDMITV();

            }
        } else {
            if (mTimeToCheck < MAX_CHECKTIME) {
                mMainView.findViewById(R.id.loading).setVisibility(View.VISIBLE);
                mMainView.findViewById(R.id.no_signal).setVisibility(View.GONE);

                ++mTimeToCheck;
            } else {
                //				 ((Activity)mContext).finish();
                mMainView.findViewById(R.id.loading).setVisibility(View.GONE);
                mMainView.findViewById(R.id.no_tv).setVisibility(View.VISIBLE);
                //				quitFullScreen();

                mHandler.removeMessages(MSG_WILL_QUIT);
                mHandler.sendEmptyMessageDelayed(MSG_WILL_QUIT, 1000);

                return;
            }
        }


        mHandler.sendEmptyMessageDelayed(MSG_CHECK_TV, 800);
    }
}
