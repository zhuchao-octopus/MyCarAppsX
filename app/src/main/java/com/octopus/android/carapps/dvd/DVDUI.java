package com.octopus.android.carapps.dvd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.adapter.MyListViewAdapterDvd;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.camera.CameraHolder;
import com.octopus.android.carapps.common.ui.UIBase;
import com.octopus.android.carapps.common.utils.ParkBrake;
import com.octopus.android.carapps.common.view.MusicRollImage;
import com.octopus.android.carapps.hardware.dvs.DVideoSpec;

import java.util.List;
import java.util.Timer;

public class DVDUI extends UIBase implements View.OnClickListener, SurfaceHolder.Callback {

    private final static String TAG = "DVDUI";
    public static final int SOURCE = MyCmd.SOURCE_DVD;

    public static DVDUI[] mUI = new DVDUI[MAX_DISPLAY];

    private DVideoSpec mDvd;

    public static DVDUI getInstanse(Context context, View view, int index) {
        if (index >= MAX_DISPLAY) {
            return null;
        }

        mUI[index] = new DVDUI(context, view, index);

        return mUI[index];
    }

    public DVDUI(Context context, View view, int index) {
        super(context, view, index);
        mSource = SOURCE;
    }

    private static final int[] BUTTON_ON_CLICK = new int[]{
            R.id.camera_surfaceview, R.id.dvd_play_pause_button, R.id.dvd_stop_button, R.id.dvd_next_button, R.id.dvd_ff_button, R.id.dvd_shuffle, R.id.cd_shuffle, R.id.dvd_keyboard, R.id.up,
            R.id.down, R.id.left, R.id.right, R.id.ok, R.id.dvd_fr_button, R.id.cd_ff_button, R.id.cd_fr_button, R.id.dvd_prev_button, R.id.dvd_next_button, R.id.dvd_title_button, R.id.dvd_eq_button,
            R.id.dvd_subt_button, R.id.dvd_volume_button, R.id.dvd_play_order_button, R.id.dvd_next_page_button, R.id.dvd_prev_page_button, R.id.cd_prev_button, R.id.cd_play_pause_button,
            R.id.cd_next_button, R.id.cd_play_order_button, R.id.cd_eq_button, R.id.repeat_dvd,

            R.id.cd_stop_button, R.id.cd_repeat2, R.id.cd_eq_mode_switch, R.id.dvd_eq_mode_switch, R.id.num0, R.id.num1, R.id.num2, R.id.num3, R.id.num4, R.id.num5, R.id.num6, R.id.num7, R.id.num8,
            R.id.num9, R.id.num_ok
    };

    // private static final int[][] VIEW_HIDE = new int[][] {
    // {0 },
    // { R.id.eq } };

    private static final int[] VIEW_HIDE2 = new int[]{
            R.id.bottom_button_layout, R.id.list_content
    };

    private MusicRollImage mMusicRollImageCD;

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

        mMainView.findViewById(R.id.cd_prev_button).setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                // TODO Auto-generated method stub
                onClick(R.id.cd_fr_button);
                return true;
            }
        });
        mMainView.findViewById(R.id.cd_next_button).setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                // TODO Auto-generated method stub
                onClick(R.id.cd_ff_button);
                return true;
            }
        });

        View v = mMainView.findViewById(R.id.disc);
        if ((v instanceof MusicRollImage)) {
            mMusicRollImageCD = (MusicRollImage) v;
        }
    }

    MyListViewAdapterDvd mMyListViewAdapter;
    ListView mListViewCD;
    TextView mTitleCD;

    public void onCreate() {
        super.onCreate();
        // initAnimation();
        mProgress = (SeekBar) mMainView.findViewById(R.id.progress);

        if (mProgress != null) {
            mProgress.setMax(1000);
        }

        mEndTime = (TextView) mMainView.findViewById(R.id.totaltime);
        mCurrentTime = (TextView) mMainView.findViewById(R.id.currenttime);
        mCDSongNum = (TextView) mMainView.findViewById(R.id.cd_song_num);

        mTitleCD = (TextView) mMainView.findViewById(R.id.cd_title);
        mListViewCD = (ListView) mMainView.findViewById(R.id.list_view);

        mListViewCD.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int postion, long id) {
                mDvd.sendDVDControlSelect(postion);
            }
        });

        mSurfaceView = (SurfaceView) mMainView.findViewById(R.id.camera_surfaceview);

        if (!(com.common.util.Util.isAndroidP() || com.common.util.Util.isAndroidQ())) {
            SurfaceHolder holder = mSurfaceView.getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        } else {
            mSurfaceView.setBackgroundColor(mContext.getResources().getColor(R.color.vehicle_camera_transparent));
        }

        mSurfaceView.setOnTouchListener(mOnTouchCameraView);
        initPresentationUI();

        mBrakeCarView = mMainView.findViewById(R.id.brake_warning_text);
        GlobalDef.updateBrakeCarText((TextView) mBrakeCarView, MachineConfig.VALUE_SYSTEM_UI_KLD7_1992, R.string.warning_driving_1992);
        mSignalView = mMainView.findViewById(R.id.no_signal);

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        mDvd = DVDService.getDVideoSpec();
        mDvd.updateScreen(width, height);


        if (Util.isGLCamera()) {
            FrameLayout v = (FrameLayout) mMainView.findViewById(R.id.glsuface_main);
            if (com.common.util.Util.isAndroidP() || com.common.util.Util.isAndroidQ()) {
                v.setBackgroundColor(mContext.getResources().getColor(R.color.vehicle_camera_transparent));
                //RelativeLayout rl = (RelativeLayout)mMainView.findViewById(R.id.screen1_main);
                //mGLSufaceView = new GLSurfaceView(mContext, rl);
                //rl.setOnTouchListener(mOnTouchCameraView);
            } else {
                //mGLSufaceView = new GLSurfaceView(mContext, v);
            }
        }
        loadBackground();
    }

    private static GLSurfaceView mGLSufaceView;

    // public void onClick(View v) {
    // switch(v.getId()){
    // case R.id.dvd_next_button:
    // mDvd.sendDVDControlCommand(DVideoSpec.DVS_COMMON_CMD_1_PARAM, 0x020D);
    //
    //
    // break;
    // case R.id.camera_surfaceview:
    //
    // toggleFullScreen();
    // break;
    // }
    // }

    public void onClick(View view) {
        onClick(view.getId());
    }

    public void onClick(int id) {
        if (id == R.id.cd_ff_button || id == R.id.dvd_ff_button) {
            mDvd.sendDVDControlCommand(DVideoSpec.DVS_SF);

            GlobalDef.setSource(mContext, SOURCE);
            // mDvd.sendDVDControlCommand(DVideoSpec.DVS_STOP);
        } else if (id == R.id.cd_fr_button || id == R.id.dvd_fr_button) {
            mDvd.sendDVDControlCommand(DVideoSpec.DVS_SR);

            GlobalDef.setSource(mContext, SOURCE);
            // mDvd.sendDVDControlCommand(DVideoSpec.DVS_EJECT);
        } else if (id == R.id.dvd_prev_button || id == R.id.cd_prev_button) {
            mDvd.sendDVDControlCommand(DVideoSpec.DVS_PREVIOUS);
            // startAnimation(false);

            GlobalDef.setSource(mContext, SOURCE);
        } else if (id == R.id.dvd_next_button || id == R.id.cd_next_button) {// startAnimation(true);
            // mDvd.sendDVDControlCommand(DVideoSpec.DVS_EJECT);
            mDvd.sendDVDControlCommand(DVideoSpec.DVS_NEXT);

            GlobalDef.setSource(mContext, SOURCE);
            // mDvd.sendDVDControlCommand(DVideoSpec.DVS_VERSION_REQ, 0x0A,
            // 0x80);
        } else if (id == R.id.dvd_play_pause_button || id == R.id.cd_play_pause_button) {
            if (mDvd.isPlaying()) {
                mDvd.sendDVDControlCommand(DVideoSpec.DVS_PAUSE);
            } else {
                mDvd.sendDVDControlCommand(DVideoSpec.DVS_PLAY);

                GlobalDef.setSource(mContext, SOURCE);
            }

            // mDvd.sendDVDControlCommand(DVideoSpec.DVS_PLAY);
        } else if (id == R.id.cd_stop_button || id == R.id.dvd_stop_button) {
            mDvd.sendDVDControlCommand(DVideoSpec.DVS_STOP);
        } else if (id == R.id.dvd_title_button) {
            mDvd.sendDVDControlCommand(DVideoSpec.DVS_TITLE);
        } else if (id == R.id.cd_eq_button || id == R.id.dvd_eq_button) {
            try {
                Intent it = new Intent("com.eqset.intent.action.EQActivity");
                mContext.startActivity(it);
            } catch (Exception e) {

            }
        } else if (id == R.id.dvd_subt_button) {
            mDvd.sendDVDControlCommand(DVideoSpec.DVS_SUBTITLE);
        } else if (id == R.id.dvd_volume_button) {
            mDvd.sendDVDControlCommand(DVideoSpec.DVS_AUDIO);
        } else if (id == R.id.repeat_dvd || id == R.id.cd_repeat2 || id == R.id.cd_play_order_button || id == R.id.dvd_play_order_button) {
            mDvd.sendDVDControlCommand(DVideoSpec.DVS_REPEAT);
        } else if (id == R.id.cd_shuffle || id == R.id.dvd_shuffle) {
            mDvd.sendDVDControlCommand(DVideoSpec.DVS_SHUFFLERANDOM);
        } else if (id == R.id.dvd_keyboard) {
            toggleKeyBoard();
        } else if (id == R.id.camera_surfaceview) {// touchVideo();
        } else if (id == R.id.left) {
            DVDService.doKeyControlPublic(MyCmd.Keycode.KEY_DVD_LEFT);
        } else if (id == R.id.right) {
            DVDService.doKeyControlPublic(MyCmd.Keycode.KEY_DVD_RIGHT);
        } else if (id == R.id.up) {
            DVDService.doKeyControlPublic(MyCmd.Keycode.KEY_DVD_UP);
        } else if (id == R.id.down) {
            DVDService.doKeyControlPublic(MyCmd.Keycode.KEY_DVD_DOWN);
        } else if (id == R.id.ok || id == R.id.num_ok) {
            DVDService.doKeyControlPublic(MyCmd.Keycode.KEY_DVD_ENTER);
        } else if (id == R.id.num0) {
            DVDService.doKeyControlPublic(MyCmd.Keycode.NUMBER0);
        } else if (id == R.id.num1) {
            DVDService.doKeyControlPublic(MyCmd.Keycode.NUMBER1);
        } else if (id == R.id.num2) {
            DVDService.doKeyControlPublic(MyCmd.Keycode.NUMBER2);
        } else if (id == R.id.num3) {
            DVDService.doKeyControlPublic(MyCmd.Keycode.NUMBER3);
        } else if (id == R.id.num4) {
            DVDService.doKeyControlPublic(MyCmd.Keycode.NUMBER4);
        } else if (id == R.id.num5) {
            DVDService.doKeyControlPublic(MyCmd.Keycode.NUMBER5);
        } else if (id == R.id.num6) {
            DVDService.doKeyControlPublic(MyCmd.Keycode.NUMBER6);
        } else if (id == R.id.num7) {
            DVDService.doKeyControlPublic(MyCmd.Keycode.NUMBER7);
        } else if (id == R.id.num8) {
            DVDService.doKeyControlPublic(MyCmd.Keycode.NUMBER8);
        } else if (id == R.id.num9) {
            DVDService.doKeyControlPublic(MyCmd.Keycode.NUMBER9);
        } else if (id == R.id.dvd_next_page_button) {// LinearLayout tmp_page1_linearLayout = (LinearLayout) mMainView
            // .findViewById(R.id.dvd_page1_layout);
            // LinearLayout tmp_page2_linearLayout = (LinearLayout) mMainView
            // .findViewById(R.id.dvd_page2_layout);
            // // tmp_page1_linearLayout.setVisibility(View.GONE);
            // // tmp_page2_linearLayout.setVisibility(View.VISIBLE);
            // // tmp_page1_linearLayout.startAnimation(AnimationUtils
            // // .loadAnimation(this, R.anim.push_left_out));
            // tmp_page1_linearLayout.setVisibility(View.GONE);
            // tmp_page2_linearLayout.setVisibility(View.VISIBLE);
            // mMainView.findViewById(R.id.dvd_prev_page_button).setVisibility(
            // View.GONE);
            // // tmp_page2_linearLayout.startAnimation(AnimationUtils
            // // .loadAnimation(this, R.anim.push_left_in));
            showMenuPage(2);

            showKeyBoard(false);
        } else if (id == R.id.dvd_prev_page_button) {
            showMenuPage(1);
            showKeyBoard(false);
            // LinearLayout tmp_page1_linearLayout = (LinearLayout)mMainView
            // . findViewById(R.id.dvd_page1_layout);
            // LinearLayout tmp_page2_linearLayout = (LinearLayout)mMainView
            // . findViewById(R.id.dvd_page2_layout);
            // tmp_page1_linearLayout.setVisibility(View.VISIBLE);
            // // tmp_page1_linearLayout.startAnimation(AnimationUtils
            // // .loadAnimation(this, R.anim.push_left_in));
            // tmp_page2_linearLayout.setVisibility(View.GONE);
            // // tmp_page2_linearLayout.startAnimation(AnimationUtils
            // // .loadAnimation(this, R.anim.push_left_out));
        } else if (id == R.id.dvd_eq_mode_switch || id == R.id.cd_eq_mode_switch) {
            GlobalDef.swtichEQMode();
        }
        // showMenu(true);

        if (id == R.id.dvd_ff_button || id == R.id.dvd_fr_button || id == R.id.cd_ff_button || id == R.id.cd_fr_button || id == R.id.dvd_prev_button || id == R.id.dvd_next_button || id == R.id.dvd_play_pause_button || id == R.id.dvd_stop_button || id == R.id.dvd_title_button || id == R.id.dvd_subt_button || id == R.id.dvd_volume_button || id == R.id.dvd_play_order_button || id == R.id.dvd_next_page_button || id == R.id.dvd_prev_page_button || id == R.id.cd_shuffle || id == R.id.dvd_shuffle || id == R.id.dvd_keyboard || id == R.id.left || id == R.id.right || id == R.id.up || id == R.id.down || id == R.id.ok) {
            prepareFullScreen();
        }
        // showMenu(true);
    }

    private void toggleKeyBoard() {
        View v = mMainView.findViewById(R.id.keyboard2);
        View v3 = mMainView.findViewById(R.id.keyboard3);
        View v_all = mMainView.findViewById(R.id.dvd_keyboard_all);
        if (v != null) {
            if (v.getVisibility() == View.VISIBLE) {
                if (v3 != null) {
                    if (v3.getVisibility() == View.VISIBLE) {
                        v3.setVisibility(View.GONE);
                    } else {
                        v3.setVisibility(View.VISIBLE);
                        if (v_all != null) {
                            v_all.setVisibility(View.VISIBLE);
                        }
                    }
                }
                v.setVisibility(View.GONE);
            } else {
                // setFullScreen();
                if (v3 != null) {
                    if (v3.getVisibility() == View.VISIBLE) {
                        v3.setVisibility(View.GONE);
                    } else {
                        v.setVisibility(View.VISIBLE);
                        if (v_all != null) {
                            v_all.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    v.setVisibility(View.VISIBLE);
                    if (v_all != null) {
                        v_all.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    private void showKeyBoard(boolean b) {
        View v = mMainView.findViewById(R.id.keyboard2);
        if (v != null) {
            v.setVisibility(b ? View.VISIBLE : View.GONE);
        }
        v = mMainView.findViewById(R.id.dvd_keyboard_all);
        if (v != null) {
            v.setVisibility(b ? View.VISIBLE : View.GONE);
        }
    }

    OnTouchListener mOnTouchCameraView = new OnTouchListener() {
        public boolean onTouch(View arg0, MotionEvent arg1) {
            if (arg1.getAction() == KeyEvent.ACTION_DOWN) {
                if (mDvd.mDiskType != DVideoSpec.DVD_DISK_TYPE_CD) {
                    touchVideo((int) arg1.getX(), (int) arg1.getY());
                }
                return true;
            }
            return false;
        }
    };

    public int getStatusHeight(Activity activity) {
        int statusHeight = 0;
        Rect localRect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        statusHeight = localRect.top;
        return statusHeight;
    }

    private void touchVideo(int x, int y) {

        if (mDvd.mTouchMode == 1 || mDvd.mTouchMode == 2) {
            mDvd.sendDVDTouchCommand(x, y);

            if (mIsFullScrean) {
                if (y <= 60) {
                    mIsFullScrean = false;
                    quitFullScreen();
                }
                // mHandler.removeMessages(MSG_MONITOR_SYSTEM_UI);
                // mHandler.sendEmptyMessageDelayed(MSG_MONITOR_SYSTEM_UI, 300);
            }
        } else {
            toggleFullScreen();
        }
    }

    private void showMenuPage(int index) {
        switch (index) {
            case 1:
                mMainView.findViewById(R.id.dvd_page2_layout).setVisibility(View.GONE);
                mMainView.findViewById(R.id.dvd_page1_layout).setVisibility(View.VISIBLE);

                mMainView.findViewById(R.id.dvd_prev_page_button).setVisibility(View.GONE);

                mMainView.findViewById(R.id.dvd_next_page_button).setVisibility(View.VISIBLE);
                break;
            case 2:
                mMainView.findViewById(R.id.dvd_page1_layout).setVisibility(View.GONE);
                mMainView.findViewById(R.id.dvd_page2_layout).setVisibility(View.VISIBLE);

                mMainView.findViewById(R.id.dvd_prev_page_button).setVisibility(View.VISIBLE);

                mMainView.findViewById(R.id.dvd_next_page_button).setVisibility(View.GONE);
                break;

        }
    }

    public boolean mWillDestory = false;

    @Override
    public void onPause() {
        super.onPause();

        mHandler.removeMessages(MSG_AUTO_FULLSCREEN);
        mHandler.removeMessages(MSG_LOADING_TIMEOUT);
        DVDService.setUICallBack(null, mDisplayIndex);
        stopCheckSignal();
        stopCheckBrakeCar();
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
        mMainView.findViewById(R.id.only_black).setVisibility(View.VISIBLE);
        showKeyBoard(false);

        deinitAnimation();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mDisplayIndex == SCREEN0) {
            mDvd.sendDVSSetMemoryCommand();
        }
    }

    private final static String DVD_POWER = "/sys/class/ak/source/dvd_power";
    private final static String DVD_STATUS = "/sys/class/ak/source/dvd_status";

    @Override
    public void onResume() {
        super.onResume();
        if (mGLSufaceView != null) {
            //mGLSufaceView.setMirror(0);
        }
        BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.MCU_DVD_SEND_CMD, 2, 0);
        GlobalDef.setCameraSource(MyCmd.CAMERA_SOURCE_DVD);
        if (GlobalDef.mReverseStatus == 1) {
            saveStatusReverseStart();
            return;
        }

        showBlack(true);
        updateDiskStatus(Util.getFileValue(DVD_STATUS), 1200);
        startCheckBrakeCar();
        // startCheckSignal();
        DVDService.setUICallBack(mHandler, mDisplayIndex);

        updateFullUI();

        // mDvd.mDiskType = DVideoSpec.DVD_DISK_TYPE_CD;
        // setDiskTypeView();
        updateLoading();

        // GlobalDef.requestAudioFocus( mAudioFocusListener);
        // BroadcastUtil.sendToCarServiceSetSource(mContext, SOURCE);

        if (mDisplayIndex == SCREEN0) {

            // setFullScreen();

            // restartPreview();

            GlobalDef.setCurrentScreen0(this);
            GlobalDef.notifyUIScreen0Change(SOURCE, 1);
        } else {
            if (mUI[0] == null || mUI[0].mPause) {
                // mHandler.postDelayed(new Runnable() {
                // public void run() {
                restartPreview();
                // }
                // }, 200);

            }
            // if (mUI[0] != null && !mUI[0].mPause) {
            // Kernel.doKeyEvent(Kernel.KEY_HOMEPAGE);
            // }

        }
        updatePlayStatus();

        GlobalDef.requestEQInfo();
    }

    private static android.hardware.Camera mCameraDevice;
    private static boolean mStartPreviewFail = false;
    private static boolean mPreviewing;
    private int mCameraOpenIndex = 0;

    private SurfaceHolder mSurfaceHolder = null;
    private SurfaceView mSurfaceView;
    private Timer mTimer;

    private void setFullScreen() {

        if (this.mDisplayIndex == SCREEN0) {

            if (mDvd.mDiskType != DVideoSpec.DVD_DISK_TYPE_CD) {
                ((Activity) mContext).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            // mMainView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            mMainView.findViewById(R.id.common_status_bar_main).setVisibility(View.GONE);
        }

        mMainView.findViewById(R.id.dvd_menu).setVisibility(View.GONE);
        showKeyBoard(false);
    }

    public boolean mIsFullScrean = false;

    public void quitFullScreen() {

        if (mDisplayIndex == SCREEN1) {

            mMainView.findViewById(R.id.common_status_bar_main).setVisibility(View.VISIBLE);

        } else {

            final WindowManager.LayoutParams attrs = ((Activity) mContext).getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            ((Activity) mContext).getWindow().setAttributes(attrs);
            ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            // mMainView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        }

        mMainView.findViewById(R.id.dvd_menu).setVisibility(View.VISIBLE);
        prepareFullScreen();
    }

    private void toggleFullScreen() {
        if (mMainView.findViewById(R.id.loading).getVisibility() == View.VISIBLE) {
            return;
        }
        mIsFullScrean = !mIsFullScrean;
        if (mIsFullScrean) {
            setFullScreen();
        } else {
            quitFullScreen();
        }
    }

    long mTimeShowBlack = 0;
    final static int TIME_HIDE_BLACK = 300;

    private void showBlack(boolean on) {
        if (on) {

            mMainView.findViewById(R.id.only_black).setVisibility(View.VISIBLE);
            mTimeShowBlack = System.currentTimeMillis();
            mHandler.removeMessages(MSG_REMOVE_BLACK);
            mHandler.sendEmptyMessageDelayed(MSG_REMOVE_BLACK, TIME_HIDE_BLACK);
        } else {
            if ((System.currentTimeMillis() - mTimeShowBlack) >= (TIME_HIDE_BLACK - 50)) {
                mMainView.findViewById(R.id.only_black).setVisibility(View.GONE);
                mHandler.removeMessages(MSG_REMOVE_BLACK);
            }
        }
    }

    private static final int MSG_CHECK_BRAKE = 13;

    private static final int MSG_CHECK_SIGNAL = 14;

    private static final int MSG_REMOVE_BLACK = 15;

    private static final int MSG_TO_HOME = 17;
    private static final int MSG_DELAY_RESTART_CAMERA = 16;
    private static final int MSG_AUTO_FULLSCREEN = 5;

    private static final int MSG_MONITOR_SYSTEM_UI = 6;
    private static final int MSG_LOADING_TIMEOUT = 7;

    private static final int TIME_CHECK_BRAKE = 1000;

    private static final int TIME_CHECK_SIGNAL = 1000;

    private static final int TIME_AUTO_FULLSCREEN = 6000;

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK_BRAKE:
                    startCheckBrakeCar();
                    break;
                case MSG_CHECK_SIGNAL:
                    startCheckSignal();
                    break;
                case MSG_REMOVE_BLACK:
                    showBlack(false);
                    break;
                case MSG_DELAY_RESTART_CAMERA:
                    restartPreviewEx();
                    break;
                case GlobalDef.MSG_PARK_BRAKE:
                    if (msg.arg1 != mBrake) {
                        boolean brake = (msg.arg1 == 1);
                        brakeCarShowText(brake);

                        mBrake = msg.arg1;
                    }
                    break;
                case MyCmd.Cmd.APP_USER_PRIVATE: {

                    updateDVDCmd(msg.arg1, msg.arg2);
                }

                break;
                case MSG_TO_HOME:
                    mIsFullScrean = false;
                    quitFullScreen();
                    if (mDisplayIndex == 0) {
                        mContext.startActivity(new Intent(Intent.ACTION_MAIN).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP).addCategory(Intent.CATEGORY_HOME));
                    } else {
                        BroadcastUtil.sendToCarServiceSetSource(mContext, MyCmd.SOURCE_MX51);

                    }
                    break;

                case MSG_AUTO_FULLSCREEN: {
                    if (!mPause && GlobalDef.mSource == SOURCE) {
                        autoToFullScreen();
                    }
                    break;
                }
                case MSG_MONITOR_SYSTEM_UI:
                    if (mDisplayIndex == SCREEN0 && mIsFullScrean && !mPause) {

                        // if ((systemui & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        //
                        // mIsFullScrean = false;
                        // quitFullScreen();
                        // }
                    }
                    break;
                case MSG_LOADING_TIMEOUT:
                    Log.d(TAG, "MSG_LOADING_TIMEOUT:!!!!!!!!" + mDvd.mDiskType);
                    if (!mPause) {
                        mDvd.mDiskType = DVideoSpec.DVD_DISK_TYPE_DVD;// force show
                        // dvd video
                        updateLoading();
                    }
                    break;
                case GlobalDef.MSG_UPDATE_EQ_MODE:
                    GlobalDef.updateEQModeButton(mMainView, R.id.cd_eq_mode_switch);
                    GlobalDef.updateEQModeButton(mMainView, R.id.dvd_eq_mode_switch);

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

        Log.d(TAG, "surfaceChanged:" + holder);
    }

    private void restartPreview() {
        // if (mDisplayIndex == 0

        // GlobalDef.reactiveSource(mContext,
        // SOURCE,DVDService.mAudioFocusListener);
        if (mPause) {
            return;
        }

        GlobalDef.requestAudioFocus(DVDService.mAudioFocusListener);
        BroadcastUtil.sendToCarServiceSetSource(mContext, SOURCE);

        // || (mDisplayIndex == 1 && mScreen1Type == TYPE_FULL_FUNCTION)) {
        // mHandler.postDelayed(new Runnable() {
        // public void run() {
        // restartPreviewEx();
        // }
        // }, 250);

        // mPrearePreview = true;
        mHandler.removeMessages(MSG_DELAY_RESTART_CAMERA);
        mHandler.sendEmptyMessageDelayed(MSG_DELAY_RESTART_CAMERA, 250);

        // }
    }

    private void restartPreviewEx() {

        try {

            releaseCamera();

            mStartPreviewFail = false;
            startPreview(mSurfaceHolder);
        } catch (Exception e) {
            // Log.e(TAG, "" + e);
            mStartPreviewFail = true;

        }
    }

    private void releaseCamera() {
        mHandler.removeMessages(MSG_DELAY_RESTART_CAMERA);
        if (GlobalDef.isGLCamera()) {
            if (mGLSufaceView != null) {
                if (mPreviewing && GlobalDef.getCameraPreview() == mCameraOpenIndex) {
                    //mGLSufaceView.stoptPreview();
                    mPreviewing = false;
                    GlobalDef.setCameraPreview(0);
                    Log.d(TAG, ">>releaseCamera ret:" + mPreviewing + ":" + GlobalDef.getCameraPreview());
                }
                // mGLSufaceView.release();
                // mGLSufaceView = null;
            }
            return;
        }

        if (mPreviewing) {
            stopPreview();
            closeCamera();
            Log.d("acamera", "dvd releaseCamera");
            mPreviewing = false;
        }

        // stopCheckSignal();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;

        Log.d(TAG, "surfaceCreated:" + holder);

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

        Log.d(TAG, "surfaceDestroyed:" + holder);

        // mSurfaceHolder = null;
    }

    private void startPreview(SurfaceHolder surfaceHolder) throws Exception { //
        // if (mPausing)
        // return;

        // if (!checkBrakeCar() && mNoVideo) {
        // return;
        // }

        if (GlobalDef.mReverseStatus == 1) {
            releaseCamera();
            Log.d(TAG, "dvd startPreview reverse1");
            return;
        }

        if (GlobalDef.isGLCamera()) {
            // if (mGLSufaceView == null) {
            // FrameLayout v = (FrameLayout) mMainView
            // .findViewById(R.id.glsuface_main);
            // mGLSufaceView = new GLSufaceView(mContext, v);
            // }


            Log.d(TAG, ">>startPreview ret:" + mPreviewing + ":" + GlobalDef.getCameramOpenCameraPreview());
            if (mPause) {
                Log.d(TAG, "<<startPreview pause:");
                return;
            }
            if ((GlobalDef.getCameraPreview() != 0) || GlobalDef.getCameramOpenCameraPreview()) {
                Log.d(TAG, "<<startPreview 22:" + GlobalDef.isCameraTryNumMax());
                if (!GlobalDef.isCameraTryNumMax()) {
                    mHandler.removeMessages(MSG_DELAY_RESTART_CAMERA);
                    mHandler.sendEmptyMessageDelayed(MSG_DELAY_RESTART_CAMERA, 500);
                }
                return;
            }
            GlobalDef.setCameramOpenCameraPreview(true);
            //mPreviewing = mGLSufaceView.startPreview(MyCmd.CAMERA_SOURCE_DVD);
            if (mPreviewing) {
                mCameraOpenIndex++;
                GlobalDef.setCameraPreview(mCameraOpenIndex);
            } else {
                GlobalDef.setCameraPreview(0);
                Log.d(TAG, "<<startPreview fail restart:");
                mHandler.removeMessages(MSG_DELAY_RESTART_CAMERA);
                mHandler.sendEmptyMessageDelayed(MSG_DELAY_RESTART_CAMERA, 500);
            }
            GlobalDef.setCameramOpenCameraPreview(false);
            Log.d(TAG, "<<startPreview ret:" + mPreviewing + ":" + GlobalDef.getCameraPreview());

            return;
        }

        Log.d(TAG, "startPreview");
        ensureCameraDevice();
        if (mPreviewing) return;// stopPreview(); //why stop?
        Log.d(TAG, "dvd:" + surfaceHolder);
        setPreviewDisplay(surfaceHolder);
        try {
            Camera.Parameters parameters = mCameraDevice.getParameters();
            List<Size> sizes = parameters.getSupportedPreviewSizes();
            Size s = sizes.get(0);
            parameters.setPreviewSize(s.width, s.height);
            // parameters.setPictureSize(s.width, s.height);
            if (Util.isPX5()) {
                parameters.set("soc_camera_channel", MyCmd.CAMERA_SOURCE_DVD);
            }
            mCameraDevice.setParameters(parameters);
            mCameraDevice.startPreview();
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("startPreview failed", ex);
        }

        if (GlobalDef.mReverseStatus == 1) {
            closeCamera();
            Log.d("Camera", "dvd startPreview reverse2");
            return;
        }

        mPreviewing = true;
    }

    private static void ensureCameraDevice() throws Exception {
        if (mCameraDevice == null) {
            //mCameraDevice = CameraHolder.instance().open();
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

    private int mBrake = -1;

    private int mSignal = -1;

    private void noSignalShowText(int s) {
        if (mSignalView != null) {
            if (s == 1) {
                restartPreview();
                mSignalView.setVisibility(View.GONE);
                showBlack(false);
            } else {
                releaseCamera();
                mSignalView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void stopCheckSignal() {
        mHandler.removeMessages(MSG_CHECK_SIGNAL);
    }

    private void startCheckSignal() {
        stopCheckSignal();
        if (GlobalDef.mReverseStatus != 1) {
            int s = ParkBrake.isSignal();
            if (s != mSignal) {
                noSignalShowText(s);
                mSignal = s;
            }
        }
        mHandler.sendEmptyMessageDelayed(MSG_CHECK_SIGNAL, TIME_CHECK_SIGNAL);

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

    private void updateDVDCmd(int cmd, int param) {
        switch (cmd) {
            case DVideoSpec.DVD_RETURN_DISK_MODE:

                Log.d(TAG, "DVideoSpec.DVD_RETURN_DISK_MODE:!!!!!!!!" + mDvd.mDiskType);
                updateLoading();
                break;
            case DVideoSpec.DVD_RETURN_TRACK_NUM:
                updateCDList();
                break;
            case DVideoSpec.DVD_RETURN_CUR_TRACK_NUM:
                updateCDPlayingList();
                break;
            case DVideoSpec.DVD_DISK_STATUS:
                updateDiskStatus(param, 1);
                break;

            case DVideoSpec.DVD_RETURN_PLAY_STATUS:
                updatePlayStatus();
                break;
            case DVideoSpec.DVD_RETURN_CUR_TIME:
                setProgress();
                break;
            case DVideoSpec.DVD_RETURN_PLAY_RATE:
                updatePlayRate();
                break;
            case DVideoSpec.DVD_RETURN_TOTAL_TIME:
                break;
            case DVideoSpec.DVD_RETURN_SHUFFLE_MODE:
            case DVideoSpec.DVD_RETURN_REPEAT_MODE:
                setPlayRepeat(mDvd.mRepeatMode, mDvd.mShuffleMode);
                break;
            case DVideoSpec.DVD_RETURN_TOUCH_MODE:
                if (param == 0x1) {
                    if (!mIsFullScrean) {
                        setFullScreen();
                    }
                }
                break;
        }
    }

    private String getRateString() {
        String s = "";
        if (mDvd.mDiskType == DVideoSpec.DVD_DISK_TYPE_CD) {
            if (mDvd.mPlayRate != 0) {
                s = "   X" + mDvd.mPlayRate;
            }
        }
        // else if (mDvd.mPlayRate < 0){
        // s = "   x" + mDvd.mPlayRate;
        // }
        return s;
    }

    private void updatePlayRate() {

        if (mDvd.mDiskType == DVideoSpec.DVD_DISK_TYPE_CD) {
            mEndTime.setText(stringForTime(mDvd.mTotalTime) + getRateString());
            // mTitleCD.setText(CD_TITLE + (mDvd.mTrackCurrent) +
            // getRateString());
        }
    }

    private void updateDiskStatus(int param, int time) {

        if (GlobalDef.mTestAll) {
            //			return;
            param = MyCmd.Dvd.DVD_STATUS_DISK_INSIDE;
        }
        mHandler.removeMessages(MSG_TO_HOME);
        if (param != MyCmd.Dvd.DVD_STATUS_DISK_INSIDE && param != MyCmd.Dvd.DVD_STATUS_IN_ING) {
            showBlack(true);
            mHandler.sendEmptyMessageDelayed(MSG_TO_HOME, time);
            if (param != MyCmd.Dvd.DVD_STATUS_OUT_ING) {
                mMainView.findViewById(R.id.no_disk).setVisibility(View.VISIBLE);

            }

            //			if (GlobalDef.mAutoTest && mDisplayIndex == 0) {
            //				BroadcastUtil.sendToCarService(mContext,
            //						MyCmd.Cmd.AUTO_TEST_RESULT, MyCmd.SOURCE_DVD, 1);
            //			}

        } else {
            mMainView.findViewById(R.id.no_disk).setVisibility(View.GONE);

            showBlack(false);

            GlobalDef.reactiveSource(mContext, SOURCE, DVDService.mAudioFocusListener);

            BroadcastUtil.sendToCarServiceSetSource(mContext, SOURCE);
            //			if (GlobalDef.mAutoTest && mDisplayIndex == 0) {
            //				BroadcastUtil.sendToCarService(mContext,
            //						MyCmd.Cmd.AUTO_TEST_RESULT, MyCmd.SOURCE_DVD, 0);
            //			}

            // BroadcastUtil.sendToCarServiceSetSource(mContext, SOURCE);
        }

        if (param == MyCmd.Dvd.DVD_STATUS_OUT_ING) {
            clearAllView();
        }
    }

    private void updateCDPlayingList() {
        if (mDvd.mTrackNum > 0 && mDvd.mTrackCurrent > 0 && mDvd.mTrackCurrent <= mDvd.mTrackNum) {
            if (mCDSongNum != null) {
                mCDSongNum.setText(mDvd.mTrackCurrent + "/" + mDvd.mTrackNum);
            }
            if (mMyListViewAdapter == null) {
                updateCDList();
            }
            if (mMyListViewAdapter != null) {
                if (mMyListViewAdapter.getCount() != mDvd.mTrackNum) {
                    updateCDList();
                }
                int track = mDvd.mTrackCurrent - 1;
                mMyListViewAdapter.setSelectItem(track);
                mTitleCD.setText(CD_TITLE + (track + 1));
                if (((mListViewCD.getFirstVisiblePosition() + 6) < track) || (mListViewCD.getFirstVisiblePosition() > track)) {
                    mListViewCD.setSelection(track);
                }

            }
        }
    }

    private final static String CD_TITLE = "    CD   Track    ";

    private void updateCDList() {
        if (mDvd.mTrackNum > 0) {

            int id = 0;
            if (mDisplayIndex == 0) {
                id = R.layout.tl_list;
            } else {
                if (GlobalDef.mScreen1Size == 0) {
                    id = R.layout.tl_list_1024;
                } else {
                    id = R.layout.tl_list_800;
                }
            }

            mMyListViewAdapter = new MyListViewAdapterDvd(mContext, id, CD_TITLE, mDvd.mTrackNum);
            mListViewCD.setAdapter(mMyListViewAdapter);
            // updateCDPlayingList();
        }
    }

    private void setDiskTypeView() {
        mHandler.removeMessages(MSG_AUTO_FULLSCREEN);
        DVDService.initDVDLang();

        if (mDvd.mDiskType == DVideoSpec.DVD_DISK_TYPE_CD) {
            mMainView.findViewById(R.id.cd_player).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.dvd_player).setVisibility(View.GONE);

            quitFullScreen();
            updateCDPlayingList();
            updatePlayStatus();
            initAnimation();

            GlobalDef.notifyUIScreen0Change(SOURCE, 1);

            if (DVDPlayer.mWakeLock != null) {
                DVDPlayer.mWakeLock.release();
                DVDPlayer.mWakeLock = null;
            }

        } else {
            mMainView.findViewById(R.id.cd_player).setVisibility(View.GONE);
            mMainView.findViewById(R.id.dvd_player).setVisibility(View.VISIBLE);

            restartPreview();

            showBlack(false);
            prepareFullScreen();
            deinitAnimation();
        }
    }

    private Bitmap mDvdLogo;

    private void loadBackground() {
        if (mDvdLogo == null) {
            try {
                mDvdLogo = BitmapFactory.decodeFile(MachineConfig.getParameterPath() + "dvd_logo.png");
            } catch (Exception e) {

            }
        }

        //		if (MachineConfig.VALUE_SYSTEM_UI45_8702_2.equals(GlobalDef
        //				.getSystemUI())) {
        //
        //			// load machine logo2
        //			if (mDvdLogo == null) {
        //				String s = MachineConfig
        //						.getProperty(MachineConfig.KEY_LOGO_PATH);
        //				if (s != null) {
        //					try {
        //						mDvdLogo = BitmapFactory.decodeFile(s);
        //					} catch (Exception e) {
        //
        //					}
        //				}
        //			}
        //
        //			if (mDvdLogo == null) {
        //				try {
        //					mDvdLogo = BitmapFactory.decodeFile(MachineConfig
        //							.getParamterPath() + "/logo/default_logo2.png");
        //				} catch (Exception e) {
        //
        //				}
        //			}
        //			if (mDvdLogo == null) {
        //				try {
        //					mDvdLogo = BitmapFactory.decodeFile(MachineConfig
        //							.getParamterPath() + "/logo/android.png");
        //				} catch (Exception e) {
        //
        //				}
        //			}
        //		}
    }

    private void updateLoading() {

        mHandler.removeMessages(MSG_LOADING_TIMEOUT);
        if (GlobalDef.mTestAll) {
            //			return;
            mDvd.mDiskType = DVideoSpec.DVD_DISK_TYPE_CD;
        }

        if (mDvd.mDiskType == 0) {
            mHandler.sendEmptyMessageDelayed(MSG_LOADING_TIMEOUT, 24000);

            mMainView.findViewById(R.id.loading).setVisibility(View.VISIBLE);

            mMainView.findViewById(R.id.cd_player).setVisibility(View.GONE);
            mMainView.findViewById(R.id.dvd_player).setVisibility(View.GONE);

            if (mDvdLogo != null) {
                ((ImageView) mMainView.findViewById(R.id.loading_image)).setImageBitmap(mDvdLogo);
                ((ImageView) mMainView.findViewById(R.id.loading_image)).setScaleType(ScaleType.CENTER_INSIDE);
            }
            if (AppConfig.mWallpaperDrawable != null) {
                mMainView.findViewById(R.id.loading).setBackground(AppConfig.mWallpaperDrawable);
            }
            setProgress();

            mTitleCD.setText("");
        } else {
            mMainView.findViewById(R.id.loading).setVisibility(View.GONE);
            setDiskTypeView();

        }

    }

    private void updatePlayStatus() {
        if (mDvd != null) {
            setPlayButtonStatus(!(mDvd.mPlayStatus == 3 || mDvd.mPlayStatus == 4 || mDvd.mPlayStatus == 5 || mDvd.mPlayStatus == 13));
        }
    }

    private void setPlayButtonStatus(boolean playing) {
        if (mDvd.mDiskType == DVideoSpec.DVD_DISK_TYPE_CD) {
            startAnimation(playing);
        }
        if (playing) {
            ((ImageView) mMainView.findViewById(R.id.cd_play_pause_button)).getDrawable().setLevel(1);
            ((ImageView) mMainView.findViewById(R.id.dvd_play_pause_button)).getDrawable().setLevel(1);
        } else {
            ((ImageView) mMainView.findViewById(R.id.cd_play_pause_button)).getDrawable().setLevel(0);
            ((ImageView) mMainView.findViewById(R.id.dvd_play_pause_button)).getDrawable().setLevel(0);
        }

        if (mMusicRollImageCD != null) {
            if (playing) {
                mMusicRollImageCD.play();
            } else {
                mMusicRollImageCD.pause();
            }
        }
    }

    private SeekBar mProgress;
    private TextView mCurrentTime;
    private TextView mEndTime;

    private TextView mCDSongNum;

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return String.format("%02d:%02d", minutes, seconds).toString();
        }
        // return str;
    }

    private void setPlayRepeat(int repeat, int shuffle) {

        // Button repeatButton = ((Button) mMainView.findViewById(R.id.repeat));
        // Drawable repeatButtonDrawable[] =
        // repeatButton.getCompoundDrawables();

        // ImageView view = (ImageView) mMainView.findViewById(R.id.repeat);
        //
        // if (repeat == MusicPlayer.REPEAT_NONE) {
        // ((ImageView) view).getDrawable().setLevel(0);
        // } else if (repeat == MusicPlayer.REPEAT_ALL) {
        // ((ImageView) view).getDrawable().setLevel(1);
        // } else {
        // ((ImageView) view).getDrawable().setLevel(2);
        // }
        int level = 0;
        if (repeat != 0) {
            level = mDvd.mRepeatMode;
        } else if (shuffle != 0) {
            level = 2;
        }

        View v = mMainView.findViewById(R.id.play_repeat_tag);
        if (v != null) {
            Drawable repeatTag = ((ImageView) v).getDrawable();
            //
            if (level == 0) {
                // repeatButtonDrawable[0].setLevel(1);
                // repeatButton.setText(R.string.text_single);
                repeatTag.setLevel(0);
            } else if (level == 3) {
                // repeatButtonDrawable[0].setLevel(2);
                // repeatButton.setText(R.string.text_random);
                repeatTag.setLevel(1);
            } else if (level == 1) {
                // repeatButtonDrawable[0].setLevel(2);
                // repeatButton.setText(R.string.text_random);
                repeatTag.setLevel(2);
            } else if (level == 2) {
                // repeatButtonDrawable[0].setLevel(2);
                // repeatButton.setText(R.string.text_random);
                repeatTag.setLevel(3);
            }
        }
        v = mMainView.findViewById(R.id.repeat_dvd);
        if (v != null) {
            Drawable repeatTag = ((ImageView) v).getDrawable();
            if (repeat == 0) {
                repeatTag.setLevel(0);
            } else if (repeat == 3 || repeat == 4) {
                repeatTag.setLevel(1);
            } else {

                repeatTag.setLevel(2);
            }
        }
        v = mMainView.findViewById(R.id.cd_repeat2);
        if (v != null) {
            Drawable repeatTag = ((ImageView) v).getDrawable();
            if (repeat == 0) {
                repeatTag.setLevel(0);
            } else if (repeat == 3 || repeat == 4) {
                repeatTag.setLevel(1);
            } else {

                repeatTag.setLevel(2);
            }
        }

    }

    private void setProgress() {
        if (mDvd.mTotalTime <= 0) {
            mCurrentTime.setText("");
            mEndTime.setText("");
            mProgress.setProgress(0);
            return;
        }
        String s;
        if (mDvd.mDvdHour > 0) {
            s = String.format("%d:%02d:%02d", mDvd.mDvdHour, mDvd.mDvdMinute, mDvd.mDvdSecond).toString();
        } else {
            s = String.format("%02d:%02d", mDvd.mDvdMinute, mDvd.mDvdSecond).toString();
        }

        mCurrentTime.setText(s);
        mDvd.mCurTime = mDvd.mDvdHour * 3600 + mDvd.mDvdMinute * 60 + mDvd.mDvdSecond;

        long pos = 0;
        // use long to avoid overflow
        pos = 1000L * mDvd.mCurTime / mDvd.mTotalTime;

        mProgress.setProgress((int) pos);

        mEndTime.setText(stringForTime(mDvd.mTotalTime) + getRateString());
        // if (mDvd.mDiskType) {
        // mProgress.setEnabled(false);
        // if (mMediaPlayer.getCurSongNum() <= 0) {
        // mProgress.setProgress(0);
        // mCurrentTime.setText("");
        // mEndTime.setText("");
        // }
        // } else {
        // mProgress.setEnabled(true);
        // position = mMediaPlayer.getCurrentPosition();
        // duration = mMediaPlayer.getDuration();
        // if (mProgress != null && position >= 0 && duration > 0
        // /* && position <= duration */) {
        // long pos = 0;
        // if (duration > 0) {
        // // use long to avoid overflow
        // pos = 1000L * position / duration;
        // }
        //
        // mProgress.setProgress((int) pos);
        //
        // // if (position > 3000) {
        // // mMediaPlayer.savePlayTime();
        // // }
        // }
        //
        // if (mEndTime != null)
        // mEndTime.setText(stringForTime(duration));
        // if ((mMediaPlayer.getCurSongNum() > 0)) {
        // mCurrentTime.setText(stringForTime(position));
        // }
        // // if (mLrcview != null) {
        // // if (mLrcview.getVisibility() == View.VISIBLE) {
        // // if (mCurLyricFile != null && mCurLyricFile.exists()) {
        // // mLrcview.initLrcIndex(position);
        // // } else {
        // // mLrcview.clearView();
        // // mCurLyricFile = null;
        // // }
        // // }
        // // }
        // }

    }

    // Animation animation;
    // ImageView mViewDisc;
    void initAnimation() {
        // if(animation==null){
        // animation = (AnimationSet) AnimationUtils.loadAnimation(mContext,
        // R.anim.mic_rotate);
        // mViewDisc = (ImageView) mMainView.findViewById(R.id.disc);
        // mViewDisc.setAnimation(animation);
        // startAnimation(true);
        // }

        // mViewDisc.setVisibility(View.GONE);
    }

    void deinitAnimation() {
        // if(animation!=null){
        // animation.cancel();
        // animation = null;
        // }
        //
        // if(mViewDisc!=null){
        // mViewDisc.setAnimation(null);
        // }

    }

    void startAnimation(boolean b) {
        // if (b) {
        // // if(mViewDisc.getAnimation()==null){
        // // animation = (AnimationSet) AnimationUtils.loadAnimation(mContext,
        // // R.anim.mic_rotate);
        //
        // mViewDisc.setVisibility(View.GONE);
        // mViewDisc.setVisibility(View.VISIBLE);
        // mViewDisc.setAnimation(animation);
        // animation.start();
        //
        // // }
        // } else {
        // mViewDisc.clearAnimation();
        // }
    }

    public int getScreen0Type() {
        int ret = SCREEN0_HIDE;
        if (mUI[0] != null) {
            if (!mUI[0].mPause) {
                // if(mDvd.mDiskType == DVideoSpec.DVD_DISK_TYPE_CD){
                // ret = SCREEN0_SHOW_CD_PLAYER;
                // } else {
                if (mIsFullScrean) {
                    ret = SCREEN0_SHOW_FULLSCREEN;
                } else {
                    ret = SCREEN0_SHOW_VIDEO;
                }
                // }
            }
        }
        return ret;
    }

    public void prepareFullScreen() {

        Log.d("aa", "dvd prepareFullScreen");

        // if (mDisplayIndex == SCREEN1) {
        mHandler.removeMessages(MSG_AUTO_FULLSCREEN);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_AUTO_FULLSCREEN, 0, 0), TIME_AUTO_FULLSCREEN);
        // }
    }

    private void autoToFullScreen() {
        if ((mBrake == 1) && mDvd.mDiskType != DVideoSpec.DVD_DISK_TYPE_CD && mMainView.findViewById(R.id.loading).getVisibility() != View.VISIBLE) {
            if (!mIsFullScrean) {
                mIsFullScrean = true;
                setFullScreen();
            }
        }
    }

    private void clearAllView() {
        if (mCDSongNum != null) {
            mCDSongNum.setText("");
        }
    }

    private boolean mResetStatusReverse = false;

    public void saveStatusReverseStart() {
        mResetStatusReverse = true;
        onPause();
        Log.d(TAG, "saveStatusReverseStart");
    }

    public void recoverStatusReverseStop() {

        Log.d(TAG, "recoverStatusReverseStop:" + mResetStatusReverse);
        if (mResetStatusReverse) {
            mResetStatusReverse = false;
            if (mIsFullScrean) {
                if (!Util.isPX5()) { // ??
                    mMainView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                }
            }
            onResume();

        }

    }

    // private static String SAVE_DATA = "DvdUI";
    // private static String SAVE_DATA_DISK_TYPE = "DISK_TYPE";
    // private void saveData(String s, long v) {
    // if(mContext!=null){
    // SharedPreferences.Editor sharedata =
    // mContext.getSharedPreferences(SAVE_DATA, 0)
    // .edit();
    //
    // sharedata.putLong(s, v);
    // sharedata.commit();
    // }
    // }
    //
    // private int getData(String s) {
    // if(mContext!=null){
    // SharedPreferences sharedata = mContext.getSharedPreferences(SAVE_DATA,
    // 0);
    // return sharedata.getInt(s, 0);
    // }
    // return 0;
    // }

}
