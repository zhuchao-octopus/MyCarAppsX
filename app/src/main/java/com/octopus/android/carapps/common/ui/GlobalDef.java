package com.octopus.android.carapps.common.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.ProtocolAk47;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.common.util.UtilCarKey;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.audio.MusicUI;
import com.octopus.android.carapps.video.VideoUI;
import com.octopus.android.carapps.wallpaper.WallpaperUI;
import com.zhuchao.android.fbase.MMLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class GlobalDef {

    public static final String TAG = "GlobalDef";
    public static int mSource = MyCmd.SOURCE_NONE;
    public static int mSourceWillUpdate = MyCmd.SOURCE_NONE;

    public static int mReverseStatus = 0;

    @SuppressLint("StaticFieldLeak")
    public static Context mContext;
    private static AudioManager mAudioManager;
    public static boolean mAutoPlayMusicWhenStorageMounted = true;

    public final static int MSG_PARK_BRAKE = 0x1000010;

    public final static int TIME_ONE_SLEEP_MOUNT = 12000;

    public static int mScreen1Size = MyCmd.Screen1.SIZE_1024_600;

    public static long mSleepOnTime = 0;

    public static long mPowerOffTime = 0;

    public static boolean mAutoMountByPowerOn = false;

    public static boolean mTestAll = false;

    public static boolean mIs8600 = false;
    public static final String SCREEN_8600_MAIN = "/sys/class/misc/ak-lcd/device/main_area";
    public static final String SCREEN_8600_ICON = "/sys/class/misc/ak-lcd/device/icon";
    public static final String SCREEN_8600_ANIM = "/sys/class/misc/ak-lcd/device/anim";

    public static int mDvrMirror = 0;

    public static int mListCommonColor = 0;
    public static int mRadioButtonType = 0;

    public static final int RADIO_BUTTON_TYPE_LONG_PRESS_SCAN = 1;

    public static boolean mIsMediaWidget = false;
    public static boolean mCanboxNeedPlayInfo = false;
    private final static boolean USE_2SCREEN = false;

    static {
        mSystemUI = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);

        mDvrMirror = MachineConfig.getPropertyIntReadOnly(MachineConfig.KEY_DVR_MIRROR);
        updateCanBox();
    }

    public static void updateCanBox() {
        String s = MachineConfig.getProperty(MachineConfig.KEY_CAN_BOX);
        updateCanBox(s);
    }

    public static void updateCanBox(String can) {
        if (can != null) {
            mCanboxNeedPlayInfo = true;
        } else {
            mCanboxNeedPlayInfo = false;
        }
    }

    public static void initCustomUI(Context c) {
        mSystemUI = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);
        //mSystemUI = MachineConfig.VALUE_SYSTEM_UI32_KLD8;//this default is VALUE_SYSTEM_UI45_8702_2
        String value = mSystemUI;
        switch (value) {
            case MachineConfig.VALUE_SYSTEM_UI20_RM10_1:
            case MachineConfig.VALUE_SYSTEM_UI21_RM10_2: {
                String s = SystemConfig.getProperty(c, SystemConfig.KEY_LAUNCHER_UI_RM10);
                if (s != null) {
                    if ("1".equals(s)) {
                        mSystemUI = MachineConfig.VALUE_SYSTEM_UI21_RM10_2;
                    } else { // 0
                        mSystemUI = MachineConfig.VALUE_SYSTEM_UI20_RM10_1;
                    }
                }
                break;
            }
            case MachineConfig.VALUE_SYSTEM_UI21_RM12: {
                String s = SystemConfig.getProperty(c, SystemConfig.KEY_LAUNCHER_UI_RM10);
                if (s != null) {
                    if ("1".equals(s)) {
                        mSystemUI = MachineConfig.VALUE_SYSTEM_UI21_RM10_2;
                    } else { // 0
                        mSystemUI = MachineConfig.VALUE_SYSTEM_UI21_RM12;
                    }
                }
                break;
            }
        }

        if (MachineConfig.VALUE_SYSTEM_UI_KLD7_1992.equals(mSystemUI)) {// KEY_1992_LIST_COLOR
            mListCommonColor = MachineConfig.getPropertyIntReadOnly(MachineConfig.KEY_1992_LIST_COLOR);
        }

        File f = new File("/system/app/MyWidget/MyWidget.apk");
        if (!f.exists()) {
            f = new File("/product/app/MyWidget/MyWidget.apk");
        }
        if (f.exists()) {
            mIsMediaWidget = true;
        }

        mRadioButtonType = MachineConfig.getPropertyIntReadOnly(MachineConfig.KEY_RADIO_BUTTON_TYPE);
    }

    public static void init(Context c) {
        mContext = c;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (mOnAudioFocusChangeListenerDelayed != null) {
            requestAudioFocus(mOnAudioFocusChangeListenerDelayed);
            mOnAudioFocusChangeListenerDelayed = null;
        }
        updateScreen1Wallpaper();
        updateScreen1Size();
        updateCustomConfig(c);
        requestEQInfo();
        if (Util.isRKSystem()) {
            CAMERA_INDEX = CAMERA_INDEX_PX5;
            CAMERA_SIGNAL = CAMERA_SIGNAL_PX5;
        }

        initCustomUI(c);
        getTouch3ConfigValue();
        // File f = new File(SCREEN_8600_MAIN);
        // // Log.d(TAG, "check 8600:" + f.exists());
        // if (f.exists()){
        // mIs8600 = true;
        // }
    }

    public static void setSmallLcd(String s) {
        Log.d(TAG, mIs8600 + "setSmallLcd:" + s);
        Util.setFileValue(SCREEN_8600_MAIN, s);
    }

    public static void setSmallLcdIcon(String s) {
        Log.d(TAG, mIs8600 + "setSmallLcdIcon:" + s);
        Util.setFileValue(SCREEN_8600_ICON, s);
    }

    public static void setSmallLcdAinm(String s) {
        // Log.d(TAG, mIs8600 + "setSmallLcdIcon:" + s);
        Util.setFileValue(SCREEN_8600_ANIM, s);
    }

    public static void checkBrakeImage(View v) {

    }

    public static boolean isOneSleepRemountTime() {
        boolean ret = true;
        if ((SystemClock.uptimeMillis() - GlobalDef.mSleepOnTime) > TIME_ONE_SLEEP_MOUNT) {
            ret = false;
        }
        // Log.d("allen", ret
        // +"isOneSleepRemountTime:"+(SystemClock.uptimeMillis() -
        // GlobalDef.mSleepOnTime));
        return ret;
    }

    public static boolean USE_OLD_CAMER_IF_NO_DVR = false;

    private static int mCameraPreview = 0;
    private static boolean mOpenCameraPreview = false;
    public static int mCameraTryNum = 0;

    public static boolean isCameraTryNumMax() {
        mCameraTryNum++;
        return (mCameraTryNum > 4);
    }

    public static int getCameraPreview() {
        return mCameraPreview;
    }

    public static void setCameraPreview(int preview) {
        mCameraPreview = preview;
        if (preview > 0) {
            mCameraTryNum = 0;
        }
    }

    public static boolean getCameramOpenCameraPreview() {
        return mOpenCameraPreview;
    }

    public static void setCameramOpenCameraPreview(boolean preview) {
        mOpenCameraPreview = preview;
        if (preview) {
            mCameraTryNum = 0;
        }
    }

    public static boolean isGLCamera() {
        boolean ret = false;
        if (Util.isGLCamera()) {
            if (USE_OLD_CAMER_IF_NO_DVR) {
                if ("1".equals(SystemConfig.getProperty(mContext, SystemConfig.KEY_DVR_RECORDING)) && "1".equals(SystemConfig.getProperty(mContext, SystemConfig.KEY_DVR_ACTITUL_RECORDING))) {
                    ret = true;
                }
            } else {
                ret = true;
            }
        }
        Log.d(TAG, "isGLCamera" + ret);
        return ret;
    }

    private static String mSystemUI;

    public static String getSystemUI() {
        return mSystemUI;
    }

    public static int mAudioUIType;
    public static final int AUDIO_TYPE_ALL_FODER = 1;
    public static final int AUDIO_TYPE_2_PAGE = 2;

    public static void updateCustomConfig(Context c) {
        mAudioUIType = c.getResources().getInteger(R.integer.audio_ui_type);
    }

    public static void updateBrakeCarText(TextView v, String ui, int string_id) {
        if (mSystemUI != null && mSystemUI.equals(ui)) {
            v.setText(string_id);
        } else {
            String s = "/mnt/paramter/brake.png";
            File f = new File(s);
            if (f.exists()) {

                Drawable d = Drawable.createFromPath(s);
                if (d != null) {
                    v.setText("");
                    v.setBackground(d);
                }
            }
        }
    }

    public static int getScreenNum(Context context) {
        if (USE_2SCREEN) {
            DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            Display[] display = displayManager.getDisplays();
            return display.length;
        } else {
            return 1;
        }
    }

    public static void setSource(Context context, int source) {
        int armSound = Util.getFileValue("/sys/class/ak/source/arm_sound");
        if ((mSource != source) || ((armSound == 1) && source != MusicUI.SOURCE && source != VideoUI.SOURCE)) {
            BroadcastUtil.sendToCarServiceSetSource(context, source);
            mSourceWillUpdate = source;
        }
    }

    public static void reactiveSource(Context context, int source, OnAudioFocusChangeListener listener) {
        MMLog.i(TAG, "reactiveSource=" + source +",listener="+ listener);
        OnAudioFocusChangeListener preListener = mOnAudioFocusChangeListenerNow;

        requestAudioFocus(listener);
        if (preListener != null && preListener != listener) {
            abandonAudioFocus(preListener);
        }
        setSource(context, source);
    }

    @SuppressLint("StaticFieldLeak")
    protected static UIBase mUIBase0;

    public static void setCurrentScreen0(UIBase ui) {
        mUIBase0 = ui;
    }

    public static UIBase getCurrentScreen0() {
        return mUIBase0;
    }

    // public final static int mSource = MyCmd.SOURCE_NONE;

    public static Handler mUIScreen0ChangeMonitor;

    public static void setUIScreen0Monitor(Handler h) {
        mUIScreen0ChangeMonitor = h;
    }

    public static void notifyUIScreen0Change(int source, int show) {
        if (mUIScreen0ChangeMonitor != null) {
            mUIScreen0ChangeMonitor.removeMessages(0);
            int delay = 0;
            if (show == 0) {
                delay = 300;
            }
            mUIScreen0ChangeMonitor.sendMessageDelayed(mUIScreen0ChangeMonitor.obtainMessage(0, source, show), delay);
        }
    }

    public static void removeUIScreen0Change(int source, int show) {
        if (mUIScreen0ChangeMonitor != null) {
            mUIScreen0ChangeMonitor.removeMessages(0);
        }
    }

    public static boolean checkStorageIsMounted(String path) {
        if (path == null || path.length() <= 0) {
            return false;
        }
        File sdcardDir = new File(path);
        if (sdcardDir.exists()) {
            String state = Environment.getExternalStorageState(sdcardDir);
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                return true;
            }
        }
        return false;
    }

    public static void abandonAudioFocus(OnAudioFocusChangeListener listener) {
        if (mAudioManager != null && listener != null) {
            Log.e(TAG, "abandonAudioFocus" + listener);

            mAudioManager.abandonAudioFocus(listener);

            if (mOnAudioFocusChangeListenerNow == listener) {
                mOnAudioFocusChangeListenerNow = null;
            }
        }
    }

    public static void rerequestAudioFocusByCarPlayOff() { // by sb suding logic
        Log.d(TAG, "rerequestAudioFocusByCarPlayOff mSource:" + mSource);
        if (mSource == MyCmd.SOURCE_AUX || mSource == MyCmd.SOURCE_MUSIC || mSource == MyCmd.SOURCE_VIDEO || mSource == MyCmd.SOURCE_RADIO || mSource == MyCmd.SOURCE_DTV || mSource == MyCmd.SOURCE_DVD || mSource == MyCmd.SOURCE_BT_MUSIC) {

            requestAudioFocus(mOnAudioFocusChangeListenerNow);
        }
    }

    private static OnAudioFocusChangeListener mOnAudioFocusChangeListenerNow;
    private static OnAudioFocusChangeListener mOnAudioFocusChangeListenerDelayed;

    public static void requestAudioFocus(OnAudioFocusChangeListener listener) {
        if (mAudioManager != null) {
            Log.e(TAG, "requestAudioFocus" + listener);
            if (listener != null) {
                mAudioManager.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                mOnAudioFocusChangeListenerNow = listener;
            }
        } else {
            mOnAudioFocusChangeListenerDelayed = listener;
        }
    }

    // public static void requestAudioFocus() {
    // if (mAudioManager != null) {
    // }
    // }
    public final static String CAMERA_SIGNAL_701 = "/sys/class/misc/mst701/device/lock";
    public final static String CAMERA_SIGNAL_PX5 = "/sys/class/ak/source/cvbs_status";

    public static String CAMERA_SIGNAL = CAMERA_SIGNAL_701;

    private final static String CAMERA_INDEX_701 = "/sys/class/misc/mst701/device/source";
    private final static String CAMERA_INDEX_PX5 = "/sys/class/ak/source/cam_ch";

    private static String CAMERA_INDEX = CAMERA_INDEX_701;

    public static void setCameraSource(int source) {
        Util.setFileValue(CAMERA_INDEX, source);

        // Log.d("acccd", source+":"+getCameraSource());

    }

    public static int getCameraSource() {
        return Util.getFileValue(CAMERA_INDEX);
    }

    public static boolean openGps(Context c, Intent it) {
        if (it != null) {
            int gps = it.getIntExtra("gps", 0);
            if (gps != 0) {
                // Kernel.doKeyEvent(Kernel.KEY_HOMEPAGE);
                UtilCarKey.doKeyGps(c);
                return true;
            }
        }
        return false;
    }

    public static void updateScreen0Background(View v) {
        if (mContext != null && v != null) {
            updateScreen0Background(mContext, v);
        }
    }

    public static void updateScreen0Background(Context c, View v) {
        if (c != null) {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(c);
            Drawable d = wallpaperManager.getDrawable();
            if (d != null) {
                v.setBackground(d);
            }
        }
    }

    public static String mScreen1Wallpaper;
    public static Drawable mDrawableScreen1Wallpaper;

    public static boolean updateScreen1Wallpaper() {
        String s = SystemConfig.getProperty(mContext, SystemConfig.KEY_SCREEN1_WALLPAPER_NAME);
        if (s == null) {
            mScreen1Wallpaper = WallpaperUI.PATH_DEFAULT_WALLPAPER1;
            mDrawableScreen1Wallpaper = Drawable.createFromPath(WallpaperUI.PATH_WALLPAPER + mScreen1Wallpaper);
            if (mDrawableScreen1Wallpaper == null) {
                mScreen1Wallpaper = WallpaperUI.PATH_DEFAULT_WALLPAPER1;
                mDrawableScreen1Wallpaper = Drawable.createFromPath(WallpaperUI.PATH_WALLPAPER + mScreen1Wallpaper);
            }
            if (mDrawableScreen1Wallpaper == null) {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
                mDrawableScreen1Wallpaper = wallpaperManager.getDrawable();
            }
            return true;
        } else if (s != null && !s.equals(mScreen1Wallpaper)) {
            mScreen1Wallpaper = s;
            mDrawableScreen1Wallpaper = Drawable.createFromPath(WallpaperUI.PATH_WALLPAPER + mScreen1Wallpaper);
            return true;
        }
        return false;
    }

    public static void updateScreen1Size() {
        DisplayManager displayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        Display[] display = displayManager.getDisplays();

        if (display.length > 1) {
            if (display[1].getWidth() == 800) {
                mScreen1Size = MyCmd.Screen1.SIZE_800_480;
            } else if (display[1].getWidth() == 1600) {
                mScreen1Size = MyCmd.Screen1.SIZE_1600_480;
            }
        }

    }

    private static final int EQ_MODE_USER = 0;
    private static final int EQ_MODE_NORMAL = 1;
    private static final int EQ_MODE_JAZZ = 2;
    private static final int EQ_MODE_POP = 3;
    private static final int EQ_MODE_CLASSICAL = 4;
    private static final int EQ_MODE_DBB = 5;

    public static int mEQModeParam;

    public final static int MSG_UPDATE_EQ_MODE = 0xf0000001;
    public final static int MSG_REVERSE_COME = 0xf0000002;

    public static void requestEQInfo() {
        if (mContext != null) {
            BroadcastUtil.sendToCarServiceMcuEQ(mContext, ProtocolAk47.SEND_AUDIO_SUB_QUERYAUDIO_INFO, 0x2);
        }
    }

    public static void updateEQModeButton(View main, int id) {
        View v = main.findViewById(id);
        Log.d("ddd", mEQModeParam + ":" + v);
        if (v instanceof Button) {
            Button new_name = (Button) v;
            int string_id = 0;
            switch (mEQModeParam) {
                case EQ_MODE_NORMAL:
                    string_id = R.string.eq_text_normal;
                    break;
                case EQ_MODE_JAZZ:
                    string_id = R.string.eq_text_jazz;
                    break;
                case EQ_MODE_POP:
                    string_id = R.string.eq_text_pop;
                    break;
                case EQ_MODE_CLASSICAL:
                    string_id = R.string.eq_text_classic;
                    break;
                case EQ_MODE_DBB:
                    string_id = R.string.eq_text_rock;
                    break;

                case EQ_MODE_USER:
                default:
                    string_id = R.string.eq_text_user;
                    break;
            }
            if (string_id != 0) {
                new_name.setText(string_id);
            }
        }
    }

    public static int swtichEQMode() {
        int i = 0;
        for (i = 0; i < EQ_MODE.length; ++i) {
            if (EQ_MODE[i] == mEQModeParam) {
                break;
            }
        }

        if (i < EQ_MODE.length) {
            i = (i + 1) % EQ_MODE.length;
        } else {
            i = 0;
        }

        mEQModeParam = EQ_MODE[i];

        BroadcastUtil.sendToCarServiceMcuEQ(mContext, ProtocolAk47.SEND_AUDIO_CMD_SET, 0x0, mEQModeParam);
        return i;
    }

    public static String getFocusPackage() {

        String topPackageName = null;

        FileReader fr = null;
        try {
            fr = new FileReader("/sys/class/ak/mcu/public_audio_focus");
            BufferedReader reader = new BufferedReader(fr, 250);
            topPackageName = reader.readLine();
            reader.close();
            fr.close();
        } catch (Exception e) {
        }

        return topPackageName;

    }

    public static boolean isAudioFocusGPS() {
        String focus = getFocusPackage();
        if (focus != null) {
            String gps = SystemConfig.getProperty(mContext, MachineConfig.KEY_GPS_PACKAGE);

            Log.d(TAG, focus + ":" + gps);
            if (focus.equals(gps)) {
                return true;
            }
        }
        return false;
    }

    private final static byte[] EQ_MODE = {
            EQ_MODE_CLASSICAL, EQ_MODE_POP, EQ_MODE_DBB, EQ_MODE_JAZZ, EQ_MODE_USER, EQ_MODE_NORMAL
    };

    public static boolean mAutoTest = false;

    private static Activity mMultiWindowModeActivity;

    public static void updateMultiWindowActivity(Activity ac) {
        if (mMultiWindowModeActivity != ac) {
            if (mMultiWindowModeActivity != null) {
                CharSequence title = mMultiWindowModeActivity.getTitle();
                if (title != null && !title.equals(ac.getTitle())) {
                    mMultiWindowModeActivity.finish();
                    mMultiWindowModeActivity = null;
                }
            }
            mMultiWindowModeActivity = ac;
        }

    }

    public static void wakeLockOnce() {
        if (mContext != null) {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            WakeLock mWakeLockOne = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
            if (null != mWakeLockOne) {
                mWakeLockOne.acquire();
                mWakeLockOne.release();
            }
        }
    }

    /**
     * 转换图片成圆形
     *
     * @param bitmap
     * 传入Bitmap对象
     * @return
     */
    public static Bitmap mRoundBitmap;

    public static Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }
        if (mRoundBitmap != null) {
            mRoundBitmap.recycle();
            mRoundBitmap = null;
        }
        mRoundBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Bitmap output = mRoundBitmap;
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }


    public static Bitmap toRoundBitmap(Bitmap bitmap, int showWidth, int showHeight, int angle) {
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();

        float scaleShow = showWidth / showHeight;
        float scaleBitmap = width / height;

        Matrix matrix = new Matrix();

        if (scaleShow >= scaleBitmap) {
            scaleShow = showWidth / width;
        } else if (scaleShow < scaleBitmap) {
            scaleShow = showHeight / height;
        }
        matrix.postScale(scaleShow, scaleShow);
        Bitmap output = Bitmap.createBitmap(showWidth, showHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xfffcfcfc;
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(0, 0, showWidth, showHeight, angle, angle, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_ATOP));
        canvas.drawBitmap(bitmap, matrix, paint);
        return output;
    }

    public static boolean mTouch3Switch = false;
    private static final String KEY_SWITCH = "switch";

    public static void getTouch3ConfigValue() {
        String value = MachineConfig.getPropertyOnce(MachineConfig.KEY_TOUCH3_IDENTIFY);
        // Log.d(TAG, "getTouch3ConfigValue: " + value);
        if (value != null && !value.isEmpty()) {
            JSONObject jobj;
            try {
                jobj = new JSONObject(value);
            } catch (JSONException e1) {
                e1.printStackTrace();
                return;
            }
            boolean enabled = false;
            try {
                enabled = jobj.getBoolean(KEY_SWITCH);
            } catch (JSONException e1) {
                enabled = false;
            }
            mTouch3Switch = enabled;
        }
    }
}
