package com.octopus.android.carapps.frontcamera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.util.BroadcastUtil;
import com.common.util.Kernel;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.common.ui.GlobalDef;
import com.octopus.android.carapps.common.camera.CameraHolder;
import com.octopus.android.carapps.common.ui.UIBase;
import com.octopus.android.carapps.common.utils.ParkBrake;
import com.rockchip.gl.GLSurfaceView;

import java.util.List;
import java.util.Timer;

public class FrontCameraUI extends UIBase implements View.OnClickListener, SurfaceHolder.Callback {

    private static final String TAG = "FrontCameraUI";
    public static final int SOURCE = MyCmd.SOURCE_FRONT_CAMERA;

    public static FrontCameraUI[] mUI = new FrontCameraUI[MAX_DISPLAY];

    public static FrontCameraUI getInstanse(Context context, View view, int index) {
        if (index >= MAX_DISPLAY) {
            return null;
        }

        mUI[index] = new FrontCameraUI(context, view, index);

        return mUI[index];
    }

    public FrontCameraUI(Context context, View view, int index) {
        super(context, view, index);
        // mSource = SOURCE;
    }

    private static final int[] BUTTON_ON_CLICK = new int[]{
            R.id.camera_surfaceview, R.id.switch_camera, R.id.switch_camera_lf, R.id.cam1, R.id.cam2, R.id.cam3, R.id.cam4, R.id.switch_camera_mirror, R.id.switch_camera_f, R.id.switch_camera_m,
            R.id.switch_camera_b
    };

    // private static final int[][] VIEW_HIDE = new int[][] {
    // {0 },
    // { R.id.eq } };

    private static final int[] VIEW_HIDE2 = new int[]{R.id.bottom_button_layout};

    public void updateFullUI() {
        if (mDisplayIndex == SCREEN1) {

            boolean fullFunction = true;
            if (mUI[0] != null && !mUI[0].mPause) {
                fullFunction = false;
            }

            for (int j : VIEW_HIDE2) {
                View v = mMainView.findViewById(j);
                if (v != null) {
                    v.setVisibility(fullFunction ? View.VISIBLE : View.GONE);
                }
            }

        } else {
            if (mUI[1] != null && !mUI[1].mPause) {
                mUI[1].updateFullUI();
            }
        }
    }

    private void initPresentationUI() {
        for (int i : BUTTON_ON_CLICK) {
            View v = mMainView.findViewById(i);
            if (v != null) {
                v.setOnClickListener(this);
                if (i == R.id.cam1 || i == R.id.cam2 || i == R.id.cam3 || i == R.id.cam4) {
                    // v.setOnFocusChangeListener(l);
                    v.setOnLongClickListener(mOnLongClickListener);
                    ((EditText) v).setOnEditorActionListener(mOnEditorActionListener);
                }
            }
        }
    }

    private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View arg0, boolean arg1) {
            // TODO Auto-generated method stub
            if (!arg1) {
                switch (arg0.getId()) {
                }
            }
        }
    };

    // private String []mCamString = new String[4];

    private int mActionId;
    TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            mActionId = actionId;
            if (actionId == EditorInfo.IME_ACTION_DONE) {// 点击软键盘完成控件时触发的行为
                // 关闭光标并且关闭软键盘
                ((EditText) v).setCursorVisible(false);
                ((EditText) v).setFocusable(false);
                ((EditText) v).setFocusableInTouchMode(false);

                InputMethodManager im = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                saveData("" + v.getId(), v.getText().toString());

            }
            return true;// 消费掉该行为
        }
    };

    @SuppressLint("WrongViewCast")
    public void onCreate() {
        super.onCreate();

        mSurfaceView = (SurfaceView) mMainView.findViewById(R.id.camera_surfaceview);
        if (!(com.common.util.Util.isAndroidP() || com.common.util.Util.isAndroidQ())) {
            SurfaceHolder holder = mSurfaceView.getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        } else {
            mSurfaceView.setBackgroundColor(mContext.getResources().getColor(R.color.vehicle_camera_transparent));
        }

        initPresentationUI();

        mBrakeCarView = mMainView.findViewById(R.id.brake_warning_text);
        GlobalDef.updateBrakeCarText((TextView) mBrakeCarView, MachineConfig.VALUE_SYSTEM_UI_KLD7_1992, R.string.warning_driving_1992);
        mSignalView = mMainView.findViewById(R.id.no_signal);
        if (Util.isGLCamera()) {
            FrameLayout v = (FrameLayout) mMainView.findViewById(R.id.glsuface_main);
            if (com.common.util.Util.isAndroidP() || com.common.util.Util.isAndroidQ()) {
                v.setBackgroundColor(mContext.getResources().getColor(R.color.vehicle_camera_transparent));
                //mGLSufaceView = new GLSurfaceView(mContext, (AttributeSet) mMainView.findViewById(R.id.screen1_main));
            } else {
                //mGLSufaceView = new GLSurfaceView(mContext, (AttributeSet) v);
            }
        }
        initCustomUI();
    }

    FrameLayout mGlFrameLayout;

    private void initCustomUI() {
        if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef.getSystemUI())) {

            View v = mMainView.findViewById(R.id.no_signal_text);
            if (v != null) {
                v.setVisibility(View.VISIBLE);
            }
        }

        mIsShowUSBCamera = MachineConfig.getPropertyIntReadOnly(MachineConfig.KEY_FCAMERA_SHOW_USBCMAERA);
        if (mIsShowUSBCamera == 1) {
            View v = mMainView.findViewById(R.id.camera_3);
            if (v != null) {
                v.setVisibility(View.VISIBLE);
            }
            v = mMainView.findViewById(R.id.switch_camera_parent);
            if (v != null) {
                v.setAlpha(0.0f);
                v.setVisibility(View.GONE);
            }


            v = mMainView.findViewById(R.id.switch_camera_f);
            if (v != null) {
                ((ImageView) v).setImageResource(R.drawable.f_camera_f2);
            }

            v = mMainView.findViewById(R.id.switch_camera_b);
            if (v != null) {
                ((ImageView) v).setImageResource(R.drawable.f_camera_b2);
            }
            v = mMainView.findViewById(R.id.switch_camera_m);
            if (v != null) {
                ((ImageView) v).setImageResource(R.drawable.f_camera_m2);
            }

        }

        if (mStyle == 1) {
            View v = mMainView.findViewById(R.id.switch_camera_parent);
            if (v != null) {
                v.setVisibility(View.GONE);
            }
            v = mMainView.findViewById(R.id.camera_hide_buttons);
            if (v != null) {
                v.setVisibility(View.GONE);
            }
        }

    }

    private GLSurfaceView mGLSufaceView;

    private long mLockClickSwitch = 0;

    private OnLongClickListener mOnLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View arg0) {

            arg0.setFocusableInTouchMode(true);
            arg0.setFocusable(true);
            arg0.requestFocus();
            quitFullScreen();

            return false;
        }
    };

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.camera_surfaceview) {
            toggleFullScreen();
        } else if (id == R.id.switch_camera_f) {
            prepareFullScreen();
            showCameraWithUSB(4);
        } else if (id == R.id.switch_camera_b) {
            prepareFullScreen();
            showCameraWithUSB(1);
        } else if (id == R.id.switch_camera_m) {
            prepareFullScreen();
            showCameraWithUSB(-1);
        } else if (id == R.id.switch_camera) {
            if ((System.currentTimeMillis() - mLockClickSwitch) > 1000) {
                mLockClickSwitch = System.currentTimeMillis();
                if (mIsShowUSBCamera == 1) {
                    toggleCameraWithUSB();
                } else {
                    toggleCamera();
                }
            }
        } else if (id == R.id.switch_camera_lf) {
            if ((System.currentTimeMillis() - mLockClickSwitch) > 1000) {
                mLockClickSwitch = System.currentTimeMillis();
                toggleCameraLR();
            }
        } else if (id == R.id.switch_camera_mirror) {
            prepareFullScreen();
            if ((System.currentTimeMillis() - mLockClickSwitch) > 1000) {
                mLockClickSwitch = System.currentTimeMillis();
                toggleMirror();
            }
        } else if (id == R.id.cam1) {
            setCamera(1);
        } else if (id == R.id.cam2) {
            setCamera(4);
        } else if (id == R.id.cam3) {
            setCamera(5);
        } else if (id == R.id.cam4) {
            setCamera(6);
        }
    }

    private int mCameraType = MachineConfig.VAULE_CAMERA_FRONT;

    private void updateCamerType() {
        String s = MachineConfig.getPropertyOnce(MachineConfig.KEY_CAMERA_TYPE);
        if (s != null) {
            mCameraType = Integer.valueOf(s);
        }

        if (mCameraType == MachineConfig.VAULE_CAMERA_FRONT) {
            if (mCameraIndex == 1) {

                ((ImageView) mMainView.findViewById(R.id.switch_camera)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.f_camera_b));
            } else {

                ((ImageView) mMainView.findViewById(R.id.switch_camera)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.f_camera_f));
            }

            ((ImageView) mMainView.findViewById(R.id.switch_camera_lf)).setAlpha(0.3f);
            ((ImageView) mMainView.findViewById(R.id.switch_camera_lf)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.f_camera_l));

            mMainView.findViewById(R.id.camera4).setVisibility(View.GONE);
            mMainView.findViewById(R.id.switch_camera).setVisibility(View.VISIBLE);
            // mMainView.findViewById(R.id.switch_camera_lf).setVisibility(View.VISIBLE);
        } else {

            mMainView.findViewById(R.id.camera4).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.switch_camera).setVisibility(View.GONE);
            // mMainView.findViewById(R.id.switch_camera_lf).setVisibility(View.GONE);
            updateCamera4Icon(mCameraIndex);

            s = getData("" + R.id.cam1);
            if (s != null) {
                ((EditText) mMainView.findViewById(R.id.cam1)).setText(s);
            }
            s = getData("" + R.id.cam2);
            if (s != null) {
                ((EditText) mMainView.findViewById(R.id.cam2)).setText(s);
            }
            s = getData("" + R.id.cam3);
            if (s != null) {
                ((EditText) mMainView.findViewById(R.id.cam3)).setText(s);
            }
            s = getData("" + R.id.cam4);
            if (s != null) {
                ((EditText) mMainView.findViewById(R.id.cam4)).setText(s);
            }
        }
    }

    private void saveData(String s, String v) {
        SharedPreferences.Editor sharedata = mContext.getSharedPreferences(FrontCameraService.SAVE_DATA, 0).edit();

        sharedata.putString(s, v);
        sharedata.commit();
    }

    private String getData(String s) {
        SharedPreferences sharedata = mContext.getSharedPreferences(FrontCameraService.SAVE_DATA, 0);
        return sharedata.getString(s, null);
    }

    private void updateCamera4Icon(int index) {

        int id = 0;
        switch (index) {
            case 1:
                id = R.id.cam1;
                break;
            case 4:
                id = R.id.cam2;
                break;
            case 5:
                id = R.id.cam3;
                break;
            case 6:
                id = R.id.cam4;
                break;
        }

        if (id != 0) {
            mMainView.findViewById(R.id.cam1).setBackground(mContext.getDrawable(R.drawable.button_click_back_camera));
            mMainView.findViewById(R.id.cam2).setBackground(mContext.getDrawable(R.drawable.button_click_back_camera));
            mMainView.findViewById(R.id.cam3).setBackground(mContext.getDrawable(R.drawable.button_click_back_camera));
            mMainView.findViewById(R.id.cam4).setBackground(mContext.getDrawable(R.drawable.button_click_back_camera));

            mMainView.findViewById(id).setBackground(mContext.getDrawable(R.drawable.com_button14));
        }
    }

    private long mUpdateCameraTime = 0;

    private void setCamera(int index) {
        if (mCameraIndex != index) {
            if ((System.currentTimeMillis() - mUpdateCameraTime) < 1200) {
                return;
            }
            mUpdateCameraTime = System.currentTimeMillis();
            mCameraIndex = index;
            mPreSignal = mSignal = -1;
            mSignalView.setVisibility(View.GONE);
            startCheckSignal(false);
            showBlackEx(true, 800);
            GlobalDef.setCameraSource(mCameraIndex);
            updateCamera4Icon(index);
        }
    }

    public static int mCameraIndex = 4; // 4 is front
    public int mStyle = 0;

    private int mMirrorPreview = 0;

    private void toggleMirror() {
        if (Util.isRKSystem()) {
            //if (ParkBrake.isSignal() != 1 && !mShowUSBCamera)
            //{
            //    return;
            //}
            if (mMirrorPreview == 0) {
                mMirrorPreview = 1;
            } else {
                mMirrorPreview = 0;
            }

            //mGLSufaceView.setMirror(mMirrorPreview);

            mPreSignal = mSignal = -1;
            mSignalView.setVisibility(View.GONE);
            startCheckSignal(false);
            if (Util.isGLCamera()) {
                showBlackEx(true, 1400);
                closeCamera();
                restartPreview();
            } else {
                showBlackEx(true, 800);
            }
        }
    }

    private void toggleCamera() {
        if (mCameraIndex == 4) {
            mCameraIndex = 1;
            ((ImageView) mMainView.findViewById(R.id.switch_camera)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.f_camera_b));
        } else {
            mCameraIndex = 4;
            ((ImageView) mMainView.findViewById(R.id.switch_camera)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.f_camera_f));
        }

        ((ImageView) mMainView.findViewById(R.id.switch_camera)).setAlpha(1.0f);
        ((ImageView) mMainView.findViewById(R.id.switch_camera_lf)).setAlpha(0.3f);
        ((ImageView) mMainView.findViewById(R.id.switch_camera_lf)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.f_camera_l));

        mPreSignal = mSignal = -1;
        mSignalView.setVisibility(View.GONE);
        startCheckSignal(false);
        if (Util.isGLCamera()) {
            showBlackEx(true, 1400);
            closeCamera();
            restartPreview();
        } else {
            showBlackEx(true, 800);
        }
        GlobalDef.setCameraSource(mCameraIndex);
    }

    private void toggleCameraLR() {
        if (mCameraIndex == 5) {
            mCameraIndex = 6;
            ((ImageView) mMainView.findViewById(R.id.switch_camera_lf)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.f_camera_r));
        } else {
            mCameraIndex = 5;
            ((ImageView) mMainView.findViewById(R.id.switch_camera_lf)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.f_camera_l));
        }

        ((ImageView) mMainView.findViewById(R.id.switch_camera_lf)).setAlpha(1.0f);
        ((ImageView) mMainView.findViewById(R.id.switch_camera)).setAlpha(0.3f);
        ((ImageView) mMainView.findViewById(R.id.switch_camera)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.f_camera_f));

        mPreSignal = mSignal = -1;
        mSignalView.setVisibility(View.GONE);
        startCheckSignal(false);
        showBlackEx(true, 800);
        GlobalDef.setCameraSource(mCameraIndex);
    }

    public boolean mWillDestory = false;

    private boolean mCloseByReverse = false;

    public void reverseStart() {

        if (mPrearePreview || mPreviewing) {
            mCloseByReverse = true;

            mMainView.findViewById(R.id.only_black).setVisibility(View.VISIBLE);

            releaseCamera();
        }

        stopCheckSignal();
        stopCheckBrakeCar();
    }

    public void reverseStop() {
        if (mCloseByReverse) {
            showBlackEx(true, 1500);
            restartPreview();
            mCloseByReverse = false;

        }

        startCheckBrakeCar();
        startCheckSignal(false);
        GlobalDef.setCameraSource(mCameraIndex);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mCameraIndex == MyCmd.CAMERA_SOURCE_FRONT_CAMERA) {
            BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.SET_FRONT_CAMERA_POWER, 0);
        }

        mSignal = -1;
        mPreSignal = -1;
        mHandler.removeMessages(MSG_DELAY_RESTART_CAMERA);
        mHandler.removeMessages(MSG_DELAY_RESTART_CAMERA_HALF_BOTTOM);
        mMainView.findViewById(R.id.only_black).setVisibility(View.VISIBLE);
        FrontCameraService.setUICallBack(null, mDisplayIndex);
        stopCheckSignal();
        stopCheckBrakeCar();
        // if (mDisplayIndex == SCREEN0) {
        //
        // if (GlobalDef.getScreenNum(mContext) > 1
        // /* && ParkBrake.isBrake() */) {
        // if (GlobalDef.mReverseStatus == 1) {
        // releaseCamera();
        // } else {
        // if (!mWillDestory) {
        GlobalDef.notifyUIScreen0Change(SOURCE, 0);
        //
        // if (mUI[1] != null && !mUI[1].mPause) {
        // mUI[1].updateFullUI();
        // } else {
        // releaseCamera();
        // }
        // }
        // }
        // } else {
        // releaseCamera();
        // }
        // } else {
        releaseCamera();
        // }

        Util.setFileValue(MCU_VIDEO_OUT_L_NODE, getVideoOutLSetting());
        Util.setFileValue(MCU_VIDEO_OUT_R_NODE, getVideoOutRSetting());
        if (mCameraType == MachineConfig.VAULE_CAMERA_FRONT) {
            if (mIsShowUSBCamera != 1) {
                mCameraIndex = 4;
            }
        }

        if (mIsShowUSBCamera == 1) {
            mHandler.removeMessages(MSG_AUTO_FULLSCREEN);
        }


    }

    private static final String MCU_VIDEO_OUT_L_NODE = "/sys/class/ak/source/lindex";
    private static final String MCU_VIDEO_OUT_R_NODE = "/sys/class/ak/source/rindex";

    private int getVideoOutRSetting() {
        return Util.getFileValue(MCU_VIDEO_OUT_R_NODE);
    }

    private int getVideoOutLSetting() {
        return Util.getFileValue(MCU_VIDEO_OUT_L_NODE);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCameraIndex == MyCmd.CAMERA_SOURCE_FRONT_CAMERA) {
            BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.SET_FRONT_CAMERA_POWER, 1);
        }

        showBlackEx(true, 800);


        if (mGLSufaceView != null) {
            //mGLSufaceView.setMirror(mMirrorPreview);
        }

        GlobalDef.setCameraSource(mCameraIndex);
        Log.d(TAG, "setCameraSource" + mCameraIndex);

        updateCamerType();
        if (MachineConfig.VALUE_SYSTEM_UI22_1050.equals(GlobalDef.getSystemUI())) {
            View v = mMainView.findViewById(R.id.switch_camera);
            if (v != null) {
                v.setVisibility(View.GONE);
            }
        }


        startCheckBrakeCar();

        mSignalView.setVisibility(View.GONE);
        startCheckSignal(false);

        FrontCameraService.setUICallBack(mHandler, mDisplayIndex);

        updateFullUI();

        // GlobalDef.requestAudioFocus();
        // BroadcastUtil.sendToCarServiceSetSource(mContext, SOURCE);

        if (mDisplayIndex == SCREEN0) {

            setFullScreen();

            //if (ParkBrake.isSignal() == 1) {
            //    restartPreview();
            //} else {
            //    showBlackEx(true, 1800);
            //}

            GlobalDef.setCurrentScreen0(this);
            GlobalDef.notifyUIScreen0Change(SOURCE, 1);
        } else {
            if (mUI[0] == null || mUI[0].mPause) {
                // mHandler.postDelayed(new Runnable() {
                // public void run() {

                if (ParkBrake.isSignal() == 1) {
                    restartPreview();
                }
                // }
                // }, 200);

            }
            if (mUI[0] != null && !mUI[0].mPause) {
                Kernel.doKeyEvent(Kernel.KEY_HOMEPAGE);
            }

        }

        if (mIsShowUSBCamera == 1) {
            //			String s = SystemConfig.getProperty(mContext,
            //					SystemConfig.KEY_DVR_RECORDING);

            quitFullScreen();
            prepareFullScreen();

            if ("1".equals(SystemConfig.getProperty(mContext, SystemConfig.KEY_DVR_RECORDING))) {
                mUSBCameraRecording = true;
            } else {
                mUSBCameraRecording = false;
            }


            if (mShowUSBCamera) {

                if (!mUSBCameraRecording) {
                    ((ImageView) mMainView.findViewById(R.id.switch_camera)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.f_camera_m));
                } else {
                    //					toggleCameraWithUSB();
                    showCameraWithUSB(-1);
                }
            }

        }

    }

    private static android.hardware.Camera mCameraDevice;
    private static boolean mStartPreviewFail = false;
    private static boolean mPreviewing;

    private int mCameraOpenIndex = 0;
    private static boolean mPrearePreview = false;
    private SurfaceHolder mSurfaceHolder = null;
    private SurfaceView mSurfaceView;
    private Timer mTimer;

    private void setFullScreen() {

        if (this.mDisplayIndex == SCREEN0) {
            ((Activity) mContext).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            if (mIsShowUSBCamera == 1) {
                View v = mMainView.findViewById(R.id.camera_hide_buttons);
                v.setVisibility(View.GONE);
            }
        }
    }

    private boolean mIsFullScrean = true;

    private void quitFullScreen() {

        if (mDisplayIndex == SCREEN1) {

            mMainView.findViewById(R.id.common_status_bar_main).setVisibility(View.VISIBLE);

        } else {

            final WindowManager.LayoutParams attrs = ((Activity) mContext).getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            ((Activity) mContext).getWindow().setAttributes(attrs);
            ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            if (mIsShowUSBCamera == 1) {
                View v = mMainView.findViewById(R.id.camera_hide_buttons);
                v.setVisibility(View.VISIBLE);
            }
            prepareFullScreen();

        }

    }

    private void toggleFullScreen() {
        mIsFullScrean = !mIsFullScrean;
        if (mIsFullScrean) {
            setFullScreen();
        } else {
            quitFullScreen();
        }
    }

    private void showBlackEx(boolean on, int time) {
        //		Log.d("abc", time+"showBlackEx:"+on);
        if (on) {
            mMainView.findViewById(R.id.only_black).setVisibility(View.VISIBLE);
            mHandler.removeMessages(MSG_REMOVE_BLACK);
            mHandler.sendEmptyMessageDelayed(MSG_REMOVE_BLACK, time);
        } else {
            mMainView.findViewById(R.id.only_black).setVisibility(View.GONE);
        }
    }

    private void showBlack(boolean on) {
        showBlackEx(on, 0);
    }

    private static final int MSG_CHECK_BRAKE = 13;

    private static final int MSG_CHECK_SIGNAL = 14;

    private static final int MSG_REMOVE_BLACK = 15;
    private static final int TIME_CHECK_BRAKE = 1000;

    private static final int MSG_DELAY_RESTART_CAMERA = 16;
    private static final int MSG_DELAY_RESTART_CAMERA_HALF_BOTTOM = 17;

    private static final int TIME_CHECK_SIGNAL = 900;
    private static final int MSG_AUTO_FULLSCREEN = 18;
    private static final int TIME_AUTO_FULLSCREEN = 5000;

    public void prepareFullScreen() {
        if (mIsShowUSBCamera == 1) {
            mHandler.removeMessages(MSG_AUTO_FULLSCREEN);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_AUTO_FULLSCREEN, 0, 0), TIME_AUTO_FULLSCREEN);
        }
    }

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK_BRAKE:
                    startCheckBrakeCar();
                    break;
                case MSG_CHECK_SIGNAL:
                    startCheckSignal(true);
                    break;
                case MSG_REMOVE_BLACK:
                    showBlack(false);
                    break;
                case MSG_DELAY_RESTART_CAMERA:
                    restartPreviewEx();
                    break;
                case MSG_DELAY_RESTART_CAMERA_HALF_BOTTOM:
                    restartPreviewExHalfBottom();
                    break;
                case GlobalDef.MSG_PARK_BRAKE:
                    if (msg.arg1 != mBrake) {
                        boolean brake = (msg.arg1 == 1);
                        brakeCarShowText(brake);

                        mBrake = msg.arg1;
                    }
                    break;
                case MSG_AUTO_FULLSCREEN:
                    mIsFullScrean = true;
                    setFullScreen();
                    break;
            }
        }
    };

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null) {
            return;
        }
        mSurfaceHolder = holder;
        if (mCameraDevice == null) return;
        if (mPause) return;
        if (mPreviewing && holder.isCreating()) {
            setPreviewDisplay(holder);
            Camera.Parameters parameters = mCameraDevice.getParameters();
            List<Size> sizes = parameters.getSupportedPreviewSizes();
            Size optimalSize = getOptimalPreviewSize(sizes, width, height);
            parameters.setPreviewSize(optimalSize.width, optimalSize.height);
            mCameraDevice.setParameters(parameters);
        } else {
            restartPreview();
        }
    }

    private void restartPreview() {
        // if (mDisplayIndex == 0

        // BroadcastUtil.sendToCarServiceSetSource(mContext, SOURCE);
        // || (mDisplayIndex == 1 && mScreen1Type == TYPE_FULL_FUNCTION)) {
        // mHandler.postDelayed(new Runnable() {
        // public void run() {
        // restartPreviewEx();
        // }
        // }, 250);
        mPrearePreview = true;
        mHandler.removeMessages(MSG_DELAY_RESTART_CAMERA);
        mHandler.removeMessages(MSG_DELAY_RESTART_CAMERA_HALF_BOTTOM);
        mHandler.sendEmptyMessageDelayed(MSG_DELAY_RESTART_CAMERA, 200);

        // }
    }

    private void restartPreviewEx() {

        try {

            releaseCamera();

            //mGLSufaceView.setMirror(mMirrorPreview);

            mHandler.removeMessages(MSG_DELAY_RESTART_CAMERA_HALF_BOTTOM);
            mHandler.sendEmptyMessageDelayed(MSG_DELAY_RESTART_CAMERA_HALF_BOTTOM, (com.common.util.Util.isAndroidP() || com.common.util.Util.isAndroidQ()) ? 200 : 0);
        } catch (Exception e) {
            // Log.e(TAG, "" + e);
            mStartPreviewFail = true;
        }
    }

    private void restartPreviewExHalfBottom() {
        try {
            mStartPreviewFail = false;
            startPreview(mSurfaceHolder);
            mPrearePreview = false;
            if (Util.isGLCamera()) {
                if (mShowUSBCamera) {
                    showBlackEx(true, 2700);
                } else {
                    showBlackEx(true, 400);
                }
            } else {
                showBlackEx(true, 100);
            }
            if (mCloseByReverse) {

                // Log.d(TAG,
                // "mCloseByReverse!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                releaseCamera();
            }
        } catch (Exception e) {
            // Log.e(TAG, "" + e);
            mStartPreviewFail = true;
        }
    }

    private void releaseCamera() {
        mHandler.removeMessages(MSG_DELAY_RESTART_CAMERA);
        mHandler.removeMessages(MSG_DELAY_RESTART_CAMERA_HALF_BOTTOM);
        Log.d(TAG, ">>releaseCamera!!" + mPreviewing);
        if (GlobalDef.isGLCamera()) {
            if (mGLSufaceView != null) {
                if (mPreviewing && GlobalDef.getCameraPreview() == mCameraOpenIndex) {
                    //mGLSufaceView.stoptPreview();

                    mPreviewing = false;
                    GlobalDef.setCameraPreview(0);
                    Log.d(TAG, ">>releaseCamera ret:" + mPreviewing + ":" + GlobalDef.getCameraPreview());
                }
                //				mGLSufaceView.release();
                //				mGLSufaceView = null;
            }

            return;
        }
        if (mPreviewing) {
            stopPreview();
            closeCamera();
            Log.d("camera", "Aux releaseCamera");
            mPreviewing = false;
        }

        // stopCheckSignal();
    }

    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {

        if (mDisplayIndex == SCREEN0) {
            if (mUI[1] != null && mUI[1].mPause) {
                stopPreview();
            }
        } else {
            if (mUI[0] != null && mUI[0].mPause) {
                stopPreview();
            }
        }

        // mSurfaceHolder = null;
    }

    private void startPreview(SurfaceHolder surfaceHolder) throws Exception { //
        // if (mPausing)
        // return;

        // if (!checkBrakeCar() && mNoVideo) {
        // return;
        // }
        Log.d(TAG, "startPreview");

        if (GlobalDef.mReverseStatus == 1) {
            releaseCamera();
            Log.d("Camera", "fc startPreview reverse1");
            return;
        }

        if (GlobalDef.isGLCamera()) {
            //			if (mGLSufaceView == null) {
            //				FrameLayout v = (FrameLayout) mMainView
            //						.findViewById(R.id.glsuface_main);
            //				mGLSufaceView = new GLSufaceView(mContext, v);
            //			}

            Log.d(TAG, ">>startPreview ret:" + mPreviewing + ":" + GlobalDef.getCameraPreview() + ":" + GlobalDef.getCameramOpenCameraPreview());
            if (mPause) {
                Log.d(TAG, "<<startPreview pause:");
                return;
            }
            if ((GlobalDef.getCameraPreview() != 0) || GlobalDef.getCameramOpenCameraPreview()) {
                Log.d(TAG, "<<startPreview ret22 :" + GlobalDef.isCameraTryNumMax());
                if (!GlobalDef.isCameraTryNumMax()) {
                    showBlackEx(true, 500);
                    mHandler.removeMessages(MSG_DELAY_RESTART_CAMERA);
                    mHandler.removeMessages(MSG_DELAY_RESTART_CAMERA_HALF_BOTTOM);
                    mHandler.sendEmptyMessageDelayed(MSG_DELAY_RESTART_CAMERA, 500);
                }
                return;
            }


            GlobalDef.setCameramOpenCameraPreview(true);
            if (!mShowUSBCamera) {
                //mPreviewing = mGLSufaceView.startPreview(mCameraIndex);
            } else {
                //mPreviewing = mGLSufaceView.startPreviewEx(1);
            }

            GlobalDef.setCameramOpenCameraPreview(false);
            if (mPreviewing) {
                mCameraOpenIndex++;
                GlobalDef.setCameraPreview(mCameraOpenIndex);
            } else {
                GlobalDef.setCameraPreview(0);
            }
            Log.d(TAG, "<<startPreview ret:" + mPreviewing + ":" + GlobalDef.getCameraPreview());

            return;
        }

        if (GlobalDef.getCameraSource() != mCameraIndex) {
            GlobalDef.setCameraSource(mCameraIndex);
        }

        ensureCameraDevice();
        if (mPreviewing) return;// stopPreview(); //why stop?

        setPreviewDisplay(surfaceHolder);
        try {
            Camera.Parameters parameters = mCameraDevice.getParameters();
            List<Size> sizes = parameters.getSupportedPreviewSizes();
            Size s = sizes.get(0);
            parameters.setPreviewSize(s.width, s.height);
            //			parameters.setPictureSize(s.width, s.height);
            if (Util.isPX5()) {
                parameters.set("soc_camera_channel", mCameraIndex);
            }
            mCameraDevice.setParameters(parameters);
            mCameraDevice.startPreview();
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("startPreview failed", ex);
        }
        if (GlobalDef.mReverseStatus == 1) {
            closeCamera();
            Log.d("Camera", "fc startPreview reverse2");
            return;
        }

        mPreviewing = true;
    }

    private static void ensureCameraDevice() throws Exception {
        if (mCameraDevice == null) {
            mCameraDevice = CameraHolder.instance().open();
        }
    }

    @SuppressWarnings("deprecation")
    private static void stopPreview() {
        if (mCameraDevice != null && mPreviewing) {
            mCameraDevice.stopPreview();
        }
        mPreviewing = false;
    }

    private static void closeCamera() {
        if (mCameraDevice != null) {

            try {
                CameraHolder.instance().release();
            } catch (Exception e) {

            }
            mCameraDevice = null;
            mPreviewing = false;
        }
    }

    private static void setPreviewDisplay(SurfaceHolder holder) {
        try {
            mCameraDevice.setPreviewDisplay(holder);
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("setPreviewDisplay failed", ex);
        }
    }

    private View mBrakeCarView;
    private View mSignalView;

    private int mBrake = -1;

    private int mSignal = -1;
    private int mPreSignal = -1;

    private void noSignalShowText(int s) {
        if (mSignalView != null) {
            if (s == 1) {
                if (!mPreviewing) {
                    restartPreview();
                }
                mSignalView.setVisibility(View.GONE);
                // showBlack(false);
            } else {
                releaseCamera();
                mSignalView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void stopCheckSignal() {
        mHandler.removeMessages(MSG_CHECK_SIGNAL);
    }

    private void startCheckSignal(boolean check) {
        int time = TIME_CHECK_SIGNAL;
        if (check) {
            if (GlobalDef.mReverseStatus != 1) {
                int s = 0;

                if (!mShowUSBCamera) {
                    s = ParkBrake.isSignal();
                } else {
                    if (android.hardware.Camera.getNumberOfCameras() > 1 && !mUSBCameraRecording) {
                        s = 1;
                    }
                }
                if (s == mPreSignal) {
                    if (s != mSignal) {

                        noSignalShowText(s);
                        mSignal = s;
                    }
                    if (GlobalDef.mAutoTest) {
                        if (mDisplayIndex == 0) {
                            BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.AUTO_TEST_RESULT, mCameraIndex == 4 ? MyCmd.SOURCE_FRONT_CAMERA : MyCmd.SOURCE_REVERSE, s == 1 ? 0 : 1);
                        }
                    }
                } else {
                    mPreSignal = s;
                    time = 200;
                }
            }
        } else {

            // mSignalView.setVisibility(View.GONE);
            time = 200;
        }

        stopCheckSignal();
        mHandler.sendEmptyMessageDelayed(MSG_CHECK_SIGNAL, time);

    }

    private void brakeCarShowText(boolean brake) {
        //		if (mBrakeCarView != null) {
        //			if (brake) {
        //				mBrakeCarView.setVisibility(View.GONE);
        //			} else {
        //				mBrakeCarView.setVisibility(View.VISIBLE);
        //			}
        //		}
    }

    private void stopCheckBrakeCar() {
        mHandler.removeMessages(MSG_CHECK_BRAKE);
    }

    private void startCheckBrakeCar() {//no brake in camera
        // stopCheckBrakeCar();
        //		boolean brake = ParkBrake.isBrake();
        //		int brake1 = brake?1:0;
        //		if (brake1 != mBrake) {
        //			brakeCarShowText(brake);
        //
        //			mBrake = brake1;
        //		}
        // mHandler.sendEmptyMessageDelayed(MSG_CHECK_BRAKE, TIME_CHECK_BRAKE);

    }

    private int mIsShowUSBCamera = 0;
    private boolean mShowUSBCamera = false;
    private boolean mUSBCameraRecording = false;

    private void toggleCameraWithUSB() {

        TextView v = (TextView) mMainView.findViewById(R.id.no_signal_text);


        if (mShowUSBCamera) {
            mCameraIndex = 1;
            ((ImageView) mMainView.findViewById(R.id.switch_camera)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.f_camera_b));

            mShowUSBCamera = false;

            //mGLSufaceView.release();
            //mGLSufaceView = new GLSufaceView(mContext, mGlFrameLayout, 0);
            v.setVisibility(View.GONE);

        } else {
            if (mCameraIndex == 4) {
                mShowUSBCamera = true;
                ((ImageView) mMainView.findViewById(R.id.switch_camera)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.f_camera_m));

                //mGLSufaceView.release();
                //mGLSufaceView = new GLSufaceView(mContext, mGlFrameLayout, 1);

                if (mUSBCameraRecording) {
                    v.setVisibility(View.VISIBLE);
                    v.setText(R.string.dvr_recording_notice);
                } else {
                    v.setVisibility(View.GONE);
                }

            } else {
                mCameraIndex = 4;
                ((ImageView) mMainView.findViewById(R.id.switch_camera)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.f_camera_f));
                v.setVisibility(View.GONE);
            }
        }

        ((ImageView) mMainView.findViewById(R.id.switch_camera)).setAlpha(1.0f);
        ((ImageView) mMainView.findViewById(R.id.switch_camera_lf)).setAlpha(0.3f);
        ((ImageView) mMainView.findViewById(R.id.switch_camera_lf)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.f_camera_l));

        mPreSignal = mSignal = -1;
        mSignalView.setVisibility(View.GONE);
        startCheckSignal(false);
        if (Util.isGLCamera()) {
            if (mShowUSBCamera) {
                showBlackEx(true, 3100);
            } else {
                showBlackEx(true, 1400);
            }
            closeCamera();
            restartPreview();
        } else {
            showBlackEx(true, 800);
        }
        GlobalDef.setCameraSource(mCameraIndex);
    }

    private void showCameraWithUSB(int index) {

        switch (index) {
            case -1: // mid
                if (mShowUSBCamera) {
                    return;
                }
                mShowUSBCamera = true;
                break;
            default:
                if (mCameraIndex == index && !mShowUSBCamera) {
                    return;
                }
                mCameraIndex = index;
                mShowUSBCamera = false;
                break;
        }
        TextView v = (TextView) mMainView.findViewById(R.id.no_signal_text);


        if (mShowUSBCamera) {

            /*if (mGLSufaceView.getCameraIndex() != 1) {
                releaseCamera();
                closeCamera();
                mGLSufaceView.release();

                mGLSufaceView = new GLSufaceView(mContext, mGlFrameLayout, 1);
                if (GlobalDef.mDvrMirror == 1) {
                    mMirrorPreview = 1;
                }
                mGLSufaceView.setMirror(mMirrorPreview);
            }*/
            if (mUSBCameraRecording) {
                v.setVisibility(View.VISIBLE);
                v.setText(R.string.dvr_recording_notice);
            } else {
                v.setVisibility(View.GONE);
            }

        } else {

            v.setVisibility(View.GONE);

            v.setVisibility(View.GONE);

            if (mGLSufaceView.getCameraIndex() != 0) {
                releaseCamera();
                closeCamera();
                mGLSufaceView.release();
                //mGLSufaceView = new GLSufaceView(mContext, mGlFrameLayout, 0);
            }

        }


        mPreSignal = mSignal = -1;
        mSignalView.setVisibility(View.GONE);
        startCheckSignal(false);
        if (Util.isGLCamera()) {
            if (mShowUSBCamera) {
                showBlackEx(true, 3100);
            } else {
                showBlackEx(true, 1400);
            }
            closeCamera();
            restartPreview();
        } else {
            showBlackEx(true, 800);
        }
        GlobalDef.setCameraSource(mCameraIndex);

    }

}
