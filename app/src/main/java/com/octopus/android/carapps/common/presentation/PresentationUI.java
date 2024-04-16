package com.octopus.android.carapps.common.presentation;

import android.app.Presentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.Kernel;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.audio.MusicUI;
import com.octopus.android.carapps.auxplayer.AuxInUI;
import com.octopus.android.carapps.btmusic.BTMusicUI;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.ui.UIBase;
import com.octopus.android.carapps.common.utils.ResourceUtil;
import com.octopus.android.carapps.dvd.DVDService;
import com.octopus.android.carapps.dvd.DVDUI;
import com.octopus.android.carapps.hardware.dvs.DVideoSpec;
import com.octopus.android.carapps.radio.RadioUI;
import com.octopus.android.carapps.screen1.launcher.Screen1LauncherUI;
import com.octopus.android.carapps.screen1.launcher.Screen1SaverUI;
import com.octopus.android.carapps.settings.SettingsUI;
import com.octopus.android.carapps.video.VideoUI;
import com.octopus.android.carapps.wallpaper.WallpaperUI;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PresentationUI extends Presentation implements OnTouchListener, OnGestureListener {

    public final static int SOURCE_NONE = MyCmd.SOURCE_NONE;
    public final static int SOURCE_SCREEN_SAVER = MyCmd.SOURCE_SCREEN1_SAVER;
    ;
    public final static int SOURCE_LAUCNHER = MyCmd.SOURCE_SCREEN1_LAUNCHER;
    ;
    public final static int SOURCE_RADIO = MyCmd.SOURCE_RADIO;
    public final static int SOURCE_MUSIC = MyCmd.SOURCE_MUSIC;
    public final static int SOURCE_VIDEO = MyCmd.SOURCE_VIDEO;
    public final static int SOURCE_BT_MUSIC = MyCmd.SOURCE_BT_MUSIC;

    public final static int SOURCE_AUX = MyCmd.SOURCE_AUX;

    public final static int SOURCE_DVD = MyCmd.SOURCE_DVD; // to do

    public final static int SOURCE_SETTINGS = MyCmd.SOURCE_SCREEN1_SETTINGS;
    public final static int SOURCE_WALLPAPER = MyCmd.SOURCE_SCREEN1_WALLPAPER;
    protected Context mContext;

    protected UIBase mUIBase; // 副屏 screen1
    protected UIBase mUIBase0;// 主屏
    private static PresentationUI mPresentationUI;

    private static boolean mUseScreen0Test = false;

    public static PresentationUI getInstanse(Context context) {
        if (Util.isRKSystem()) {
            return null;
        }

        if (mPresentationUI == null) {
            DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            Display[] display = displayManager.getDisplays();

            if (!mUseScreen0Test) {
                if (1 >= display.length) {
                    return null;
                }

                mPresentationUI = new PresentationUI(context, display[1], R.style.TranslucentTheme);
            } else {
                mPresentationUI = new PresentationUI(context, display[0], R.style.TranslucentTheme);
            }

        }

        return mPresentationUI;
    }

    public static PresentationUI getInstanse() {
        return mPresentationUI;
    }

    public void release() {
        unregisterListener();
        mPresentationUI = null;
    }

    public PresentationUI(Context context, Display display, int style) {
        super(context, display, style);
        // ResourceUtil.updateSingleUi(getContext());
        mContext = context;
        // if ((context instanceof Service)) {
        getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        // }
        registerListener();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        return false;
    }

    ;

    // @Override
    // public void show() {
    // if (mUIBase != null) {
    // mUIBase.onCreate();
    // mUIBase.onResume();
    //
    // }
    // initTime();
    // if (!isShowing()) {
    // super.show();
    // }
    // }

    @Override
    public void dismiss() {
        // if (mUIBase != null) {
        // mUIBase.onPause();
        // mUIBase.onDestroy();
        // }
        // if (isShowing()) {
        // super.dismiss();
        // }
    }

    public void updateOtherUI(int source) {
        View v = findViewById(R.id.screen1_eject);
        if (v != null) {
            if (source == SOURCE_DVD) {
                v.setVisibility(View.VISIBLE);
            } else {
                v.setVisibility(View.GONE);
            }
        }
    }

    private void initUI() {
        if (mUIBase != null) {
            mUIBase.onCreate();
            mUIBase.onResume();
            updateStatusBarIcon();
        }
    }

    private void deinitUI() {
        if (mUIBase != null) {
            mUIBase.onPause();
            mUIBase.onDestroy();
        }

    }

    private void updateStatusBarIcon() {
        String s = AppConfig.getCanboxSetting();
        View v;
        v = findViewById(R.id.screen1_air_control1);
        if (v != null) {
            if (MachineConfig.VALUE_CANBOX_TOYOTA_BINARYTEK.equals(s)) {
                v.setVisibility(View.VISIBLE);
            } else {
                v.setVisibility(View.GONE);
            }
        }
    }

    private static final int[] BUTTON_ON_CLICK = new int[]{
            R.id.home, R.id.back, R.id.screen1_settings, R.id.set_wallpaper, R.id.screen_save_click, R.id.screen1_eject, R.id.button_dvr, R.id.screen1_air_control, R.id.screen1_air_add,
            R.id.screen1_air_minus, R.id.screen1_air_rear
    };

    private void initClick() {
        for (int i : BUTTON_ON_CLICK) {
            View v = findViewById(i);
            if (v != null) {
                v.setOnClickListener(mViewListener);
            }
        }

        View v = findViewById(R.id.screen1_air_control1);
        if (v != null) {
            v.setOnTouchListener(this);
        }
        v = findViewById(R.id.screen1_air_control);
        if (v != null) {
            v.setOnTouchListener(this);
        }
    }

    private boolean mPrepareToLacher = false;
    private View.OnClickListener mViewListener = new View.OnClickListener() {
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.home) {
                if (SOURCE_LAUCNHER != mSource) {
                    doUpdate(SOURCE_LAUCNHER);
                } else {
                    doUpdate(SOURCE_SCREEN_SAVER);
                }
            } else if (id == R.id.back) {
                if (mSource == SOURCE_SETTINGS || mSource == SOURCE_WALLPAPER) {
                    doUpdate(mPreSource);
                } else if (mSource != SOURCE_LAUCNHER) {
                    mPrepareToLacher = true;
                    mUIBase0 = GlobalDef.getCurrentScreen0();
                    if (mUIBase0 != null && mUIBase0.mSource == mSource && !mUIBase0.mPause) {
                        Kernel.doKeyEvent(Kernel.KEY_BACK);
                    } else {
                        BroadcastUtil.sendToCarServiceSetSource(mContext, MyCmd.SOURCE_MX51);
                    }
                }
            } else if (id == R.id.screen_save_click) {
                doUpdate(SOURCE_LAUCNHER);
            } else if (id == R.id.set_wallpaper) {
                doUpdate(SOURCE_WALLPAPER);
            } else if (id == R.id.screen1_settings) {
                doUpdate(SOURCE_SETTINGS);
            } else if (id == R.id.screen1_air_control) {
                BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.SHOW_AIR_CONTROL, 0);
            } else if (id == R.id.screen1_air_add) {
                BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.SHOW_AIR_CONTROL, 0xa);
            } else if (id == R.id.screen1_air_minus) {
                BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.SHOW_AIR_CONTROL, 0x9);
            } else if (id == R.id.screen1_air_rear) {
                BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.SHOW_AIR_CONTROL, 0x2a);
            } else if (id == R.id.screen1_eject) {
                BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.APP_REQUEST_SEND_KEY, MyCmd.Keycode.EJECT);
            } else if (id == R.id.button_dvr) {// Intent it = new Intent(MyCmd.BROADCAST_DVR_SCREEN1_UPDATE);
                // mContext.sendBroadcast(it);
                Intent it = new Intent(Intent.ACTION_RUN);
                try {
                    it.setClassName("com.my.dvr", "com.my.dvr.DvrService");
                    it.putExtra("cmd", 1);
                    mContext.startService(it);
                } catch (Exception e) {
                    Log.d("abc", "startUIService err");
                }
            }

        }
    };

    public int mSource = SOURCE_NONE;
    private int mPreSource;

    public void update(int source) {
        update(source, false);
    }

    public void update(int source, boolean force) {
        switch (source) {
            case MyCmd.SOURCE_MX51:
            case MyCmd.SOURCE_AV_OFF:
                // case MyCmd.SOURCE_DVD:
                if (!mPrepareToLacher) {
                    source = SOURCE_SCREEN_SAVER;
                } else {
                    source = SOURCE_LAUCNHER;
                    mPrepareToLacher = false;
                }
                break;
        }

        doUpdateEx(source, force);
    }

    private final static int LAYOUT_ID[][] = new int[][]{
            {
                    SOURCE_AUX, R.layout.aux_player, R.layout.aux_player, R.layout.aux_player
            },

            {
                    SOURCE_SCREEN_SAVER, R.layout.screen1_saver, R.layout.screen1_saver_800, R.layout.screen1_saver_800
            },

            {
                    SOURCE_LAUCNHER, R.layout.screen1_launcher, R.layout.screen1_launcher_800, R.layout.screen1_launcher_1600_480
            },

            {
                    SOURCE_RADIO, R.layout.screen1_radio_layout, R.layout.screen1_radio_layout_800, R.layout.screen1_radio_layout_1600_480
            },

            {
                    SOURCE_MUSIC, R.layout.screen1_music_layout, R.layout.screen1_music_layout_800, R.layout.screen1_music_layout_1600_480
            },

            {
                    SOURCE_VIDEO, R.layout.screen1_video_layout, R.layout.screen1_video_layout_800, R.layout.screen1_video_layout_1600_480
            },

            {
                    SOURCE_BT_MUSIC, R.layout.screen1_bt_music, R.layout.screen1_bt_music_800, R.layout.screen1_bt_music_1600_480
            },

            {
                    SOURCE_DVD, R.layout.screen1_dvd_player, R.layout.screen1_dvd_player_800, R.layout.screen1_dvd_player_1600_480
            },

            {
                    SOURCE_WALLPAPER, R.layout.wallpaper_chooser, R.layout.wallpaper_chooser, R.layout.wallpaper_chooser
            },

            {
                    SOURCE_SETTINGS, R.layout.settings, R.layout.settings, R.layout.settings
            },

    };

    private int getLayoutId(int source) {
        int id = 0;
        for (int i = 0; i < LAYOUT_ID.length; ++i) {
            if (LAYOUT_ID[i][0] == source) {
                if (GlobalDef.mScreen1Size == MyCmd.Screen1.SIZE_800_480) {
                    id = LAYOUT_ID[i][2];
                } else if (GlobalDef.mScreen1Size == MyCmd.Screen1.SIZE_1600_480) {
                    id = LAYOUT_ID[i][3];
                } else {
                    id = LAYOUT_ID[i][1];
                }
                break;
            }
        }
        return id;
    }

    private void doUpdate(int source) {
        doUpdateEx(source, false);
    }

    private void doUpdateEx(int source, boolean force) {
        if (!isShowing()) {
            show();
        }

        // if (source == SOURCE_VIDEO && !mVideoShow) {
        // source = SOURCE_SCREEN_SAVER;
        // }

        if (source == SOURCE_NONE || source == 6) {
            return;
        }
        if (!force) {
            source = updateScreen1Status(source);
        }

        if (mSource == source) {
            return;
        }
        mPreSource = mSource;
        mSource = source;
        int layout = 0;
        layout = getLayoutId(source);
        // switch (source) {
        // case SOURCE_SCREEN_SAVER:
        // layout = R.layout.screen1_saver;
        //
        // break;
        // case SOURCE_LAUCNHER:
        // layout = R.layout.screen1_launcher;
        //
        // break;
        // case SOURCE_RADIO:
        // layout = R.layout.screen1_radio_layout;
        //
        // break;
        // case SOURCE_MUSIC:
        // layout = R.layout.screen1_music_layout;
        //
        // break;
        // case SOURCE_VIDEO:
        // layout = R.layout.screen1_video_layout;
        //
        // break;
        // case SOURCE_BT_MUSIC:
        // layout = R.layout.screen1_bt_music;
        // break;
        // case SOURCE_AUX:
        // layout = R.layout.aux_player;
        // break;
        // default:
        // break;
        // }

        if (layout != 0) {
            deinitUI();
            getLayoutId(source);
            ResourceUtil.updateSingleUi(getContext());
            setContentView(layout);

            switch (source) {
                case SOURCE_SCREEN_SAVER:
                    mUIBase = Screen1SaverUI.getInstanse(mContext, findViewById(R.id.screen1_main), 1);
                    break;
                case SOURCE_LAUCNHER:

                    mUIBase = Screen1LauncherUI.getInstanse(mContext, findViewById(R.id.screen1_main), 1);
                    break;
                case SOURCE_RADIO:

                    mUIBase = RadioUI.getInstanse(mContext, findViewById(R.id.screen1_main), 1);
                    // hideDvd();
                    break;
                case SOURCE_MUSIC:

                    mUIBase = MusicUI.getInstance(mContext, findViewById(R.id.screen1_main), 1);
                    // hideDvd();
                    break;
                case SOURCE_VIDEO:

                    mUIBase = VideoUI.getInstanse(mContext, findViewById(R.id.screen1_main), 1);
                    // hideDvd();
                    break;
                case SOURCE_BT_MUSIC:

                    mUIBase = BTMusicUI.getInstanse(mContext, findViewById(R.id.screen1_main), 1);
                    // hideDvd();
                    break;
                case SOURCE_AUX:
                    mUIBase = AuxInUI.getInstanse(mContext, findViewById(R.id.screen1_main), 1);
                    // hideScreen0Camera();
                    break;
                case SOURCE_DVD:
                    mUIBase = DVDUI.getInstanse(mContext, findViewById(R.id.screen1_main), 1);
                    // hideAuxIn();
                    // hideFrontCamera();
                    // hideDvd();
                    break;
                case SOURCE_WALLPAPER:
                    mUIBase = WallpaperUI.getInstanse(mContext, findViewById(R.id.screen1_main), 1);
                    break;
                case SOURCE_SETTINGS:
                    mUIBase = SettingsUI.getInstanse(getContext(), findViewById(R.id.screen1_main), 1);
                    break;
                default:
                    break;
            }
            hideSreen0Apps(source, force);
            // show();
            updateOtherUI(source);
            initUI();
            if (source != SOURCE_WALLPAPER /* && source!=SOURCE_SETTINGS */) {
                initClick();
                initTime();
                BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.SET_SCREEN1_SOURCE, source);
            }
        }
    }

    private void hideSreen0Apps(int source, boolean force) {
        String s = AppConfig.getTopActivity();
        boolean toHome = false;
        if (s != null) {
            if (force) {
                switch (source) {
                    case SOURCE_RADIO:
                        if ((AppConfig.CAR_UI_DVD.equals(s)) || (AppConfig.CAR_UI_RADIO.equals(s))) {
                            toHome = true;
                        }
                        break;
                    case SOURCE_MUSIC:
                        if ((AppConfig.CAR_UI_DVD.equals(s)) || (AppConfig.CAR_UI_AUDIO.equals(s))) {
                            toHome = true;
                        }
                        break;
                    case SOURCE_VIDEO:
                        if ((AppConfig.CAR_UI_DVD.equals(s)) || (AppConfig.CAR_UI_VIDEO.equals(s))) {
                            toHome = true;
                        }
                        break;
                    case SOURCE_BT_MUSIC:
                        if ((AppConfig.CAR_UI_DVD.equals(s)) || (AppConfig.CAR_UI_BT_MUSIC.equals(s))) {
                            toHome = true;
                        }
                        break;
                    case SOURCE_AUX:
                        if ((AppConfig.CAR_UI_DVD.equals(s)) || AppConfig.CAR_UI_FRONT_CAMERA.equals(s) || (AppConfig.CAR_UI_AUX_IN.equals(s))) {
                            toHome = true;
                        }
                        break;
                    case SOURCE_DVD:
                        if ((AppConfig.CAR_UI_DVD.equals(s)) || AppConfig.CAR_UI_FRONT_CAMERA.equals(s) || (AppConfig.CAR_UI_AUX_IN.equals(s))) {
                            toHome = true;
                        }
                        break;
                    default:
                        break;
                }
            } else {
                switch (source) {
                    case SOURCE_AUX:
                        if ((AppConfig.CAR_UI_DVD.equals(s)) || AppConfig.CAR_UI_FRONT_CAMERA.equals(s) || (AppConfig.CAR_UI_AUX_IN.equals(s))) {
                            toHome = true;
                        }
                        break;
                    case SOURCE_DVD:
                        if (mDvd == null) {
                            mDvd = DVDService.getDVideoSpec();
                        }

                        if (AppConfig.CAR_UI_FRONT_CAMERA.equals(s) || (AppConfig.CAR_UI_AUX_IN.equals(s))) {
                            toHome = true;
                        } else if ((AppConfig.CAR_UI_DVD.equals(s))) {
                            if (mDvd.mDiskType != DVideoSpec.DVD_DISK_TYPE_CD) {
                                toHome = true;
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        if (toHome) {
            mContext.startActivity(new Intent(Intent.ACTION_MAIN).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP).addCategory(Intent.CATEGORY_HOME));
            Util.doSleep(5);
        }
    }

    private void hideScreen0Camera() {
        String s = AppConfig.getTopActivity();
        if ((s != null) && (AppConfig.CAR_UI_FRONT_CAMERA.equals(s) || AppConfig.CAR_UI_DVD.equals(s))) {

            mContext.startActivity(new Intent(Intent.ACTION_MAIN).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP).addCategory(Intent.CATEGORY_HOME));
        }

    }

    private void hideDvd() {
        String s = AppConfig.getTopActivity();
        if ((s != null) && (AppConfig.CAR_UI_DVD.equals(s))) {

            mContext.startActivity(new Intent(Intent.ACTION_MAIN).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP).addCategory(Intent.CATEGORY_HOME));
        }

    }

    private void hideFrontCamera() {
        String s = AppConfig.getTopActivity();
        if ((s != null) && (AppConfig.CAR_UI_FRONT_CAMERA.equals(s))) {

            mContext.startActivity(new Intent(Intent.ACTION_MAIN).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP).addCategory(Intent.CATEGORY_HOME));
        }

    }

    private void hideAuxIn() {
        String s = AppConfig.getTopActivity();
        if ((s != null) && (AppConfig.CAR_UI_AUX_IN.equals(s))) {

            mContext.startActivity(new Intent(Intent.ACTION_MAIN).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP).addCategory(Intent.CATEGORY_HOME));
        }

    }

    public void updateScreen1Change(int source, int show) {
        if (mUIBase == null) {
            return;
        }

        switch (source) {
            case MyCmd.SOURCE_AUX:
                if (show == 0) {
                    if (mSource == SOURCE_SCREEN_SAVER || mSource == SOURCE_LAUCNHER) {
                        doUpdate(source);
                    }
                } else {
                    doUpdate(SOURCE_SCREEN_SAVER);
                }
                break;
            case MyCmd.SOURCE_VIDEO:
                if (show == 0) {
                    if (mSource == SOURCE_SCREEN_SAVER || mSource == SOURCE_LAUCNHER) {
                        doUpdate(source);
                    }
                } else {
                    // if (mUIBase0 != null
                    // && mUIBase0.mSource == SOURCE_VIDEO
                    // && mUIBase0.getScreen0Type() ==
                    // UIBase.SCREEN0_SHOW_FULLSCREEN) {
                    // doUpdate(source);
                    // } else {
                    doUpdate(SOURCE_SCREEN_SAVER);
                    // }
                }
                break;
            case MyCmd.SOURCE_FRONT_CAMERA:
                if (show == 1) {
                    if (mSource == SOURCE_AUX || mSource == SOURCE_DVD) {
                        doUpdate(SOURCE_SCREEN_SAVER);
                    }
                }
                break;
            case MyCmd.SOURCE_DVD:
                if (mDvd == null) {
                    mDvd = DVDService.getDVideoSpec();
                }

                if (show == 1) {
                    if (mSource == SOURCE_AUX) {
                        doUpdate(SOURCE_SCREEN_SAVER);
                    } else if (mSource == SOURCE_DVD) {
                        if (mDvd.mDiskType != DVideoSpec.DVD_DISK_TYPE_CD) {
                            doUpdate(SOURCE_SCREEN_SAVER);
                        } else {
                            doUpdate(source);
                        }
                    }
                    if (mSource == SOURCE_SCREEN_SAVER || mSource == SOURCE_LAUCNHER) {
                        if (mDvd.mDiskType == DVideoSpec.DVD_DISK_TYPE_CD) {
                            doUpdate(source);
                        }
                    }
                } else if (show == 0) {
                    if (mSource == SOURCE_SCREEN_SAVER || mSource == SOURCE_LAUCNHER) {

                        if (mDvd.mDiskType != DVideoSpec.DVD_DISK_TYPE_CD) {
                            doUpdate(source);
                        }
                    }
                }
                break;
        }

    }

    private DVideoSpec mDvd;// = DVDService.getDVideoSpec();
    // time
    Handler mHandler = new Handler(Looper.myLooper()) {
        public void handleMessage(Message msg) {
            initTime();
            super.handleMessage(msg);
        }
    };

    TextView mClock;

    private void initTime() {
        setTime();
        mHandler.removeMessages(0);
        mHandler.sendEmptyMessageDelayed(0, 30000);

    }

    private void setTime() { // 24

        mClock = (TextView) findViewById(R.id.clock);
        if (mClock != null) {
            // Date curDate = new Date(System.currentTimeMillis());
            // int h = curDate.getHours();
            //
            // String strTimeFormat = Settings.System.getString(
            // mContext.getContentResolver(),
            // android.provider.Settings.System.TIME_12_24);
            //
            // if ("12".equals(strTimeFormat)) {
            // if (h > 12) {
            // h -= 12;
            // }
            // }
            //
            // mClock.setText(String.format("%02d:%02d", h,
            // curDate.getMinutes()));

            long time = System.currentTimeMillis();
            Date d1 = new Date(time);
            SimpleDateFormat format;

            boolean is24 = DateFormat.is24HourFormat(mContext);
            if (is24) {
                format = new SimpleDateFormat("HH:mm");
            } else {
                format = new SimpleDateFormat("hh:mm a");
            }
            String t2 = format.format(d1);

            mClock.setText(t2);
        }
    }

    private int updateScreen1Status(int source) {
        if (source == SOURCE_AUX || source == SOURCE_VIDEO || source == SOURCE_DVD) {
            mUIBase0 = GlobalDef.getCurrentScreen0();
            if (source == SOURCE_AUX) {
                if (mUIBase0 != null && mUIBase0.mSource == SOURCE_AUX && !mUIBase0.mPause) {
                    source = SOURCE_SCREEN_SAVER;
                }
            } else if (source == SOURCE_VIDEO) {
                if (mUIBase0 != null && mUIBase0.mSource == SOURCE_VIDEO && !mUIBase0.mPause
                    /* && mUIBase0.getScreen0Type() == UIBase.SCREEN0_SHOW_VIDEO */) {
                    source = SOURCE_SCREEN_SAVER;
                }
            } else if (source == SOURCE_DVD) {
                if (mDvd == null) {
                    mDvd = DVDService.getDVideoSpec();
                }

                if (mUIBase0 != null && mUIBase0.mSource == SOURCE_DVD && !mUIBase0.mPause && (mDvd.mDiskType != DVideoSpec.DVD_DISK_TYPE_CD)
                    /* && mUIBase0.getScreen0Type() == UIBase.SCREEN0_SHOW_VIDEO */) {
                    source = SOURCE_SCREEN_SAVER;
                }
            }
        }
        return source;
    }

    // for test
    // private static boolean mVideoShow = false;
    //
    // public static void updateVideoShow(boolean b) {
    // mVideoShow = b;
    // }
    //
    // public static void updateVideoQuit() {
    // if (mPresentationUI != null) {
    // mPresentationUI.updateVideoQuitTest();
    // }
    // }
    //
    // public void updateVideoQuitTest() {
    // if (mSource == SOURCE_LAUCNHER || SOURCE_SCREEN_SAVER == mSource) {
    // mVideoShow = true;
    // doUpdate(SOURCE_VIDEO);
    // }
    // }
    //
    // public static void updateVideoResume() {
    // if (mPresentationUI != null) {
    // mPresentationUI.updateVideoResumeTest();
    // }
    // }
    //
    // public void updateVideoResumeTest() {
    // if (mSource == SOURCE_VIDEO) {
    //
    // mVideoShow = false;
    // doUpdate(SOURCE_SCREEN_SAVER);
    // }
    // }

    private void udpateWallPaper() {
        if (GlobalDef.updateScreen1Wallpaper() && mUIBase != null) {
            mUIBase.udpateWallPaper();
        }
    }

    private BroadcastReceiver mReceiver = null;

    private void registerListener() {
        if (mReceiver == null) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    // Log.d("ff", action);
                    if (action.equals(WallpaperUI.BROADCAST_SCREEN1_WALLPAPER_CHANGE)) {
                        udpateWallPaper();
                        if (mSource == SOURCE_WALLPAPER) {
                            doUpdate(mPreSource);
                        }
                    } else if (action.equals(Intent.ACTION_TIME_CHANGED)) {
                        initTime();
                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(WallpaperUI.BROADCAST_SCREEN1_WALLPAPER_CHANGE);
            iFilter.addAction(Intent.ACTION_TIME_CHANGED);
            mContext.registerReceiver(mReceiver, iFilter);
        }
        if (mContentOb == null) {
            mContentOb = new SettingsValueChangeContentObserver();
            mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(android.provider.Settings.System.TIME_12_24), true, mContentOb);// 注册监听
        }

    }

    SettingsValueChangeContentObserver mContentOb;

    private void unregisterListener() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        if (mContentOb != null) {
            mContext.getContentResolver().unregisterContentObserver(mContentOb);
            mContentOb = null;
        }
    }

    class SettingsValueChangeContentObserver extends ContentObserver {

        public SettingsValueChangeContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // String strTimeFormat = Settings.System.getString(
            // mContext.getContentResolver(),
            // android.provider.Settings.System.TIME_12_24);
            initTime();
            // Log.d("aa", strTimeFormat);

        }
    }

    GestureDetector mygesture = new GestureDetector(this);

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        return mygesture.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // TODO Auto-generated method stub
        //		Log.d("tt", (e2.getY() - e1.getY())+"::"+distanceY);
        if (e2.getY() - e1.getY() > 50 && Math.abs(distanceY) > 0) {

            BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.SHOW_AIR_CONTROL, 0);
            return true;
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //		Log.d("tt", (e2.getY() - e1.getY())+"::"+velocityY);


        return false;
    }

}
