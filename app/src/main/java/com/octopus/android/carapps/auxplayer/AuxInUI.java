package com.octopus.android.carapps.auxplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.util.BroadcastUtil;
import com.common.util.Kernel;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.camera.CameraHolder;
import com.octopus.android.carapps.common.ui.UIBase;
import com.octopus.android.carapps.common.utils.ParkBrake;

import java.util.List;
import java.util.Timer;

public class AuxInUI extends UIBase implements View.OnClickListener, SurfaceHolder.Callback {

    private static final String TAG = "AuxInUI";
    public static final int SOURCE = MyCmd.SOURCE_AUX;

    public static AuxInUI[] mUI = new AuxInUI[MAX_DISPLAY];

    public static AuxInUI getInstanse(Context context, View view, int index) {
        if (index >= MAX_DISPLAY) {
            return null;
        }

        mUI[index] = new AuxInUI(context, view, index);

        return mUI[index];
    }

    public AuxInUI(Context context, View view, int index) {
        super(context, view, index);
        mSource = SOURCE;
    }

    private static final int[] BUTTON_ON_CLICK = new int[]{R.id.camera_surfaceview};

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

            for (int i = 0; i < VIEW_HIDE2.length; ++i) {
                View v = mMainView.findViewById(VIEW_HIDE2[i]);
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
            }
        }

    }

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
                //mGLSufaceView = new GLSurfaceView(mContext, (LinearLayout)mMainView.findViewById(R.id.screen1_main));
            } else {
                //mGLSufaceView = new GLSurfaceView(mContext, v);
            }
        }
        initCustomUI();
    }

    private void initCustomUI() {
        String value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);
        if (MachineConfig.VALUE_SYSTEM_UI_KLD7_1992.equals(value)) {

            View v = mMainView.findViewById(R.id.no_signal_image);
            if (v instanceof ImageView) {
                ImageView new_name = (ImageView) v;
                Drawable d = Drawable.createFromPath("/mnt/paramter/auxin_nosignal.png");
                if (d != null) {
                    new_name.setImageDrawable(d);
                }
            }
        } else if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef.getSystemUI())) {

            View v = mMainView.findViewById(R.id.no_signal_text);
            if (v != null) {
                v.setVisibility(View.VISIBLE);
            }
        }

    }

    private static GLSurfaceView mGLSufaceView;

    public void onClick(View v) {
        toggleFullScreen();
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

        Log.d(TAG, "reverseStop setCameraSource" + MyCmd.CAMERA_SOURCE_AUX);
        GlobalDef.setCameraSource(MyCmd.CAMERA_SOURCE_AUX);
    }

    @Override
    public void onPause() {
        super.onPause();

        mMainView.findViewById(R.id.only_black).setVisibility(View.VISIBLE);
        AuxInService.setUICallBack(null, mDisplayIndex);
        mHandler.removeMessages(MSG_DELAY_RESTART_CAMERA);
        stopCheckSignal();
        stopCheckBrakeCar();
        mPreSignal = -1;
        mSignal = -1;
        if (mDisplayIndex == SCREEN0) {

            if (GlobalDef.getScreenNum(mContext) > 1
                /* && ParkBrake.isBrake() */) {
                if (GlobalDef.mReverseStatus == 1) {
                    releaseCamera();
                } else {
                    if (!mWillDestory) {
                        GlobalDef.notifyUIScreen0Change(SOURCE, 0);

                        if (mUI[1] != null && !mUI[1].mPause) {
                            mUI[1].updateFullUI();
                        } else {
                            releaseCamera();
                        }
                    } else {
                        releaseCamera();
                    }
                }
            } else {
                releaseCamera();
            }
        } else {
            releaseCamera();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGLSufaceView != null) {
            //mGLSufaceView.setMirror(0);
        }
        startCheckBrakeCar();

        mSignalView.setVisibility(View.GONE);
        startCheckSignal(false);
        showBlackEx(true, 800);
        AuxInService.setUICallBack(mHandler, mDisplayIndex);

        updateFullUI();

        GlobalDef.setCameraSource(MyCmd.CAMERA_SOURCE_AUX);
        Log.d(TAG, "setCameraSource" + MyCmd.CAMERA_SOURCE_AUX);
        GlobalDef.reactiveSource(mContext, SOURCE, AuxInService.mAudioFocusListener);

        BroadcastUtil.sendToCarServiceSetSource(mContext, SOURCE);
        if (mDisplayIndex == SCREEN0) {

            setFullScreen();
            if (ParkBrake.isSignal() == 1) {
                restartPreview();
            } else {
                showBlackEx(true, 1800);
            }

            GlobalDef.setCurrentScreen0(this);
            GlobalDef.notifyUIScreen0Change(SOURCE, 1);
        } else {
            if (mUI[0] == null || mUI[0].mPause) {
                // mHandler.postDelayed(new Runnable() {
                // public void run() {

                if (ParkBrake.isSignal() == 1) {
                    restartPreview();
                } else {
                    showBlackEx(true, 1800);
                }
                // }
                // }, 200);

            }
            if (mUI[0] != null && !mUI[0].mPause) {
                Kernel.doKeyEvent(Kernel.KEY_HOMEPAGE);
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
        } else {
            mMainView.findViewById(R.id.common_status_bar_main).setVisibility(View.GONE);
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

    private static final int MSG_DELAY_RESTART_CAMERA = 16;
    private static final int MSG_DELAY_RESTART_CAMERA_FOR_FAIL = 17;

    private static final int TIME_CHECK_BRAKE = 1000;

    private static final int TIME_CHECK_SIGNAL = 900;

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
                case MSG_DELAY_RESTART_CAMERA_FOR_FAIL:
                    break;
                case GlobalDef.MSG_PARK_BRAKE:
                    if (msg.arg1 != mBrake) {
                        boolean brake = (msg.arg1 == 1);
                        brakeCarShowText(brake);

                        mBrake = msg.arg1;
                    }
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
        mHandler.sendEmptyMessageDelayed(MSG_DELAY_RESTART_CAMERA, 200);

        // }
    }

    private void restartPreviewEx() {

        try {

            releaseCamera();

            mStartPreviewFail = false;
            startPreview(mSurfaceHolder);
            mPrearePreview = false;
            showBlackEx(true, 100);
            if (mCloseByReverse) {

                Log.d(TAG, "mCloseByReverse!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                releaseCamera();
            }
        } catch (Exception e) {
            // Log.e(TAG, "" + e);
            mStartPreviewFail = true;

        }
    }

    private void releaseCamera() {
        mHandler.removeMessages(MSG_DELAY_RESTART_CAMERA);
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

        //		Log.d(TAG, "startPreview");

        if (GlobalDef.mReverseStatus == 1) {
            releaseCamera();
            Log.d("Camera", "av startPreview reverse1");
            return;
        }

        if (GlobalDef.isGLCamera()) {
            //			if (mGLSufaceView == null) {
            //				FrameLayout v = (FrameLayout) mMainView
            //						.findViewById(R.id.glsuface_main);
            //				mGLSufaceView = new GLSufaceView(mContext, v);
            //			}

            Log.d(TAG, ">>startPreview ret:" + mPreviewing + ":" + GlobalDef.getCameramOpenCameraPreview());
            if (mPause) {
                Log.d(TAG, "<<startPreview pause:");
                return;
            }
            if ((GlobalDef.getCameraPreview() != 0) || GlobalDef.getCameramOpenCameraPreview()) {
                Log.d(TAG, "<<startPreview 22:" + GlobalDef.isCameraTryNumMax());
                if (!GlobalDef.isCameraTryNumMax()) {
                    showBlackEx(true, 500);
                    mHandler.removeMessages(MSG_DELAY_RESTART_CAMERA);
                    mHandler.sendEmptyMessageDelayed(MSG_DELAY_RESTART_CAMERA, 500);
                }
                return;
            }
            GlobalDef.setCameramOpenCameraPreview(true);
            //mPreviewing = mGLSufaceView.startPreview();
            mCameraOpenIndex++;
            if (mPreviewing) {
                mCameraOpenIndex++;
                GlobalDef.setCameraPreview(mCameraOpenIndex);
            } else {
                GlobalDef.setCameraPreview(0);
            }
            GlobalDef.setCameramOpenCameraPreview(false);
            Log.d(TAG, "<<startPreview ret:" + mPreviewing + ":" + GlobalDef.getCameraPreview());
            return;
        }

        // if (mPausing)
        // return;

        // if (!checkBrakeCar() && mNoVideo) {
        // return;
        // }
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
                parameters.set("soc_camera_channel", MyCmd.CAMERA_SOURCE_AUX);
            }
            mCameraDevice.setParameters(parameters);
            mCameraDevice.startPreview();
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("startPreview failed", ex);
        }
        if (GlobalDef.mReverseStatus == 1) {
            closeCamera();
            Log.d("Camera", "av startPreview reverse2");
            return;
        }
        mPreviewing = true;

    }

    private static void ensureCameraDevice() throws Exception {
        if (mCameraDevice == null) {
            mCameraDevice = CameraHolder.instance().open();
        }
    }

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

    private int mBrake;

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
        //		Log.d("dddd", "stopCheckSignal");
        mHandler.removeMessages(MSG_CHECK_SIGNAL);
    }

    private void startCheckSignal(boolean check) {

        //		Log.d("dddd", "startCheckSignal");
        int time = TIME_CHECK_SIGNAL;
        if (check) {
            if (GlobalDef.mReverseStatus != 1) {
                int s = ParkBrake.isSignal();
                if (s == mPreSignal) {
                    if (s != mSignal) {

                        noSignalShowText(s);
                        mSignal = s;
                    }
                } else {
                    mPreSignal = s;
                    time = 200;
                }

                if (GlobalDef.mAutoTest) {
                    if (mDisplayIndex == 0) {
                        BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.AUTO_TEST_RESULT, MyCmd.SOURCE_AUX, s == 1 ? 0 : 1);
                    }
                }
            }
        } else {

            time = 200;
        }

        stopCheckSignal();
        mHandler.sendEmptyMessageDelayed(MSG_CHECK_SIGNAL, time);

    }

    private void brakeCarShowText(boolean brake) {
        if (mBrakeCarView != null) {
            if (brake) {
                mBrakeCarView.setVisibility(View.GONE);
            } else {
                mBrakeCarView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void stopCheckBrakeCar() {
        mHandler.removeMessages(MSG_CHECK_BRAKE);
    }

    private void startCheckBrakeCar() {
        // stopCheckBrakeCar();
        boolean brake = ParkBrake.isBrake();
        int brake1 = brake ? 1 : 0;
        if (brake1 != mBrake) {
            brakeCarShowText(brake);

            mBrake = brake1;
        }
        // mHandler.sendEmptyMessageDelayed(MSG_CHECK_BRAKE, TIME_CHECK_BRAKE);

    }
}
