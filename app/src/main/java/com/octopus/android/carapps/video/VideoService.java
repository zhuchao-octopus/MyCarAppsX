package com.octopus.android.carapps.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.player.ComMediaPlayer;
import com.octopus.android.carapps.common.player.MoviePlayer;
import com.octopus.android.carapps.common.player.MusicPlayer;
import com.octopus.android.carapps.common.service.ServiceBase;

import java.util.Timer;
import java.util.TimerTask;

public class VideoService extends ServiceBase {
    public static final String TAG = "VideoService";

    private static MoviePlayer mMediaPlayer = null;

    @SuppressLint("StaticFieldLeak")
    private static VideoService mThis;

    public static VideoService getInstanse(Context context) {
        if (mThis == null) {
            mThis = new VideoService(context);

            mThis.onCreate();
        }
        return mThis;
    }

    public VideoService(Context context) {
        super(context);
    }


    private static Handler[] mHandlerPresentation = new Handler[2];

    public static void setHandler(Handler h, int index) {
        if (index < mHandlerPresentation.length) {
            mHandlerPresentation[index] = h;
        }
    }

    public static MoviePlayer getMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MoviePlayer(null);
            mMediaPlayer.setIMediaCallBack(mIMediaCallBack);
            mMediaPlayer.registerMountListener();
        }
        return mMediaPlayer;
    }

    public static void clearMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setIMediaCallBack(null);
            mMediaPlayer.unregisterMountListener();
            mMediaPlayer = null;
        }
    }

    public void onCreate() {
        mMediaPlayer = getMediaPlayer();
        //		mMediaPlayer.setIMediaCallBack(mIMediaCallBack);
        //		mMediaPlayer.registerMountListener();
    }

    @Override
    public void onDestroy() {

        if (mMediaPlayer != null) {
            mMediaPlayer.setIMediaCallBack(null);
            mMediaPlayer.unregisterMountListener();
            mMediaPlayer = null;
        }

        super.onDestroy();
    }

    public void doEQResule() {
        returnInfoToUI(GlobalDef.MSG_UPDATE_EQ_MODE);
    }


    private void returnInfoToUI(int value) {
        for (int i = 0; i < mHandlerPresentation.length; ++i) {
            if (mHandlerPresentation[i] != null) {
                mHandlerPresentation[i].sendEmptyMessage(value);
            }
        }
    }

    private final static int MSG_DELAY_PAUSE = 1;
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DELAY_PAUSE:
                    if (GlobalDef.mSource != VideoUI.SOURCE) {
                        stopReportCanbox();
                        savePlayStatus();
                    }
                    break;
            }
        }
    };

    public void doCmd(int cmd, Intent intent) {
        switch (cmd) {
            case MyCmd.Cmd.SOURCE_CHANGE:
                int source = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                if (source != VideoUI.SOURCE) {
                    mHandler.removeMessages(MSG_DELAY_PAUSE);
                    mHandler.sendEmptyMessageDelayed(MSG_DELAY_PAUSE, 200);
                } else {
                    startReportCanbox();
                }
                break;

            case MyCmd.Cmd.MCU_BRAK_CAR_STATUS:
                int brake = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                callBackToUI(ComMediaPlayer.PARK_BRAKE, brake, null);
                break;
            case MyCmd.Cmd.ACC_POWER_OFF:
                if (Util.isRKSystem() && mMediaPlayer != null) {

                    //Log.d(TAG, "video ACC_POWER_OFF:"+GlobalDef.mSource);
                    //				if(GlobalDef.mSource ==VideoUI.SOURCE){
                    //					mMediaPlayer.mSleepInPlay = true;
                    //				} else {
                    //					mMediaPlayer.mSleepInPlay = false;
                    //				}
                    mMediaPlayer.savePlayForSleepRemount();
                    //				VideoActivity.finishActivity(GlobalDef.mContext);
                }
                break;
            case MyCmd.Cmd.ACC_POWER_ON:
                //			Log.d(TAG, "audio ACC_POWER_OFF");
                if (mMediaPlayer != null) {
                    mMediaPlayer.checkSleepOnStorage();
                }
                break;
        }
    }


    private void savePlayStatus() {
        // if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
        // mMediaPlayer.pause();
        // }
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isInitialized()) {
                if (Util.isAndroidQ()) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                        mMediaPlayer.savePlayTime();
                    }
                } else {
                    mMediaPlayer.savePlayStatus();
                    mMediaPlayer.savePlayTime();
                    // mMediaPlayer.stopPlay();
                }
            }
            mMediaPlayer.updateCompletionListener(false);
        }
    }

    public void doKeyControl(int code) {
        if (mMediaPlayer == null) {
            return;
        }
        switch (code) {
            case MyCmd.Keycode.CH_UP:
            case MyCmd.Keycode.KEY_SEEK_NEXT:
            case MyCmd.Keycode.KEY_TURN_A:
            case MyCmd.Keycode.NEXT:

            case MyCmd.Keycode.KEY_DVD_UP:
            case MyCmd.Keycode.KEY_DVD_RIGHT:
                mMediaPlayer.next();
                break;

            case MyCmd.Keycode.CH_DOWN:
            case MyCmd.Keycode.KEY_SEEK_PREV:
            case MyCmd.Keycode.KEY_TURN_D:
            case MyCmd.Keycode.PREVIOUS:
            case MyCmd.Keycode.KEY_DVD_DOWN:
            case MyCmd.Keycode.KEY_DVD_LEFT:
                mMediaPlayer.prev();
                break;
            case MyCmd.Keycode.FAST_R:
                mMediaPlayer.fr();
                break;
            case MyCmd.Keycode.FAST_F:
                mMediaPlayer.ff();
                break;
            case MyCmd.Keycode.STOP:
                //			mMediaPlayer.stop();
                mMediaPlayer.pause(); //to be do
                break;
            case MyCmd.Keycode.PLAY:
                mMediaPlayer.play();
                break;
            case MyCmd.Keycode.PAUSE:
                mMediaPlayer.pause();
                break;
            case MyCmd.Keycode.PLAY_PAUSE:
                if (mMediaPlayer.isInitialized()) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                    } else {
                        mMediaPlayer.play();
                    }
                }
                break;

        }
    }

    private static Handler[] mHandlerUICallBack = new Handler[2];

    public static void setUICallBack(Handler cb, int index) {
        mHandlerUICallBack[index] = cb;
    }

    private static ComMediaPlayer.IMediaCallBack mIMediaCallBack = new ComMediaPlayer.IMediaCallBack() {
        public void callback(int what, int status, Object obj) {

            if (what == ComMediaPlayer.STORAGE_MOUNTED) {
                if (mHandlerUICallBack[0] != null) {
                    mHandlerUICallBack[0].sendMessageDelayed(mHandlerUICallBack[0].obtainMessage(what, status, 0, obj), 20);
                } else if (mThis != null) {
                    mThis.doStorageMounted(status);
                }
            } else if (what == MusicPlayer.STORAGE_EJECT) {
                doStorageEject();
            } else {
                callBackToUI(what, status, obj);
            }
        }
    };

    private static void callBackToUI(int what, int status, Object obj) {
        for (int i = 0; i < mHandlerUICallBack.length; ++i) {
            if (mHandlerUICallBack[i] != null) {
                mHandlerUICallBack[i].sendMessageDelayed(mHandlerUICallBack[i].obtainMessage(what, status, 0, obj), 20);
            }
        }
    }

    private void doStorageMounted(int page) {

        // boolean open = false;
        // boolean appShow = (MusicUI.getInstanse(0) != null && !MusicUI
        // .getInstanse(0).mPause);
        //
        // if (GlobalDef.mAutoPlayMusicWhenStorageMounted && !appShow) {
        // open = true;
        // }
        //
        // if (open) {
        // try {
        // Intent it = new Intent(Intent.ACTION_VIEW);
        // it.setClassName(mContext.getPackageName(),
        // "com.my.audio.MusicActivity");
        // it.putExtra("page", page);
        // it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // mContext.startActivity(it);
        // } catch (Exception e) {
        // Log.e(TAG, e.getMessage());
        // }
        // }

    }

    private static void doStorageEject() {

        if ((VideoUI.getInstanse(0) != null && !VideoUI.getInstanse(0).mPause)) {
            // Kernel.doKeyEvent(Kernel.KEY_BACK);
            VideoActivity.finishActivity(GlobalDef.mContext);
            BroadcastUtil.sendToCarServiceSetSource(GlobalDef.mContext, MyCmd.SOURCE_MX51);
        } else if ((VideoUI.getInstanse(1) != null && !VideoUI.getInstanse(1).mPause)) {
            // Kernel.doKeyEvent(Kernel.KEY_BACK);
            BroadcastUtil.sendToCarServiceSetSource(GlobalDef.mContext, MyCmd.SOURCE_MX51);
        }

        if (mMediaPlayer != null) {
            // mMediaPlayer.stop();
            mMediaPlayer.releasePlay();
        }
    }

    // for canbox
    private Timer mTimerCanbox;

    private void startReportCanbox() {
        // stopReportCanbox();
        if (mTimerCanbox == null) {
            TimerTask task = new TimerTask() {
                public void run() {
                    if (mMediaPlayer != null && mMediaPlayer.isInitialized()) {
                        try {
                            if (GlobalDef.mCanboxNeedPlayInfo) {
                                BroadcastUtil.sendCanboxInfo(mContext, MyCmd.VIDEO_SOURCE_CHANGE, mMediaPlayer.getCurPosition(), (int) (mMediaPlayer.getCurrentPosition() / 1000L), mMediaPlayer.getCurrentTotal());
                            }
                        } catch (Exception e) {

                        }
                    }
                    // sendBroadcastInt(AUDIO_SOURCE_CHANGE, mCurrentPlayPos,
                    // (int) (position() / 1000L), mCurrentTotal);
                    // Log.d("canbox", "video report");
                }
            };
            mTimerCanbox = new Timer();
            mTimerCanbox.schedule(task, 1, 1000);
        }
    }

    private void stopReportCanbox() {
        if (mTimerCanbox != null) {
            mTimerCanbox.cancel();
            mTimerCanbox.purge();
            mTimerCanbox = null;
        }
    }

    private static boolean mPausedByTransientLossOfFocus = false;

    public static OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            // AudioFocus is a new feature: focus updates are made verbose on
            // purpose

            Log.e(TAG, "OnAudioFocusChangeListener" + focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (!GlobalDef.isAudioFocusGPS()) {
                        Log.v(TAG, "AudioFocus: received AUDIOFOCUS_LOSS:" + focusChange);
                        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                            mPausedByTransientLossOfFocus = true;
                            mMediaPlayer.pause();
                        }
                        if (GlobalDef.mSource == MyCmd.SOURCE_VIDEO) {
                            GlobalDef.setSource(GlobalDef.mContext, MyCmd.SOURCE_OTHERS_APPS);

                        }
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.v(TAG, "AudioFocus: received AUDIOFOCUS_GAIN");
                    if (mMediaPlayer != null && !mMediaPlayer.isPlaying() && mPausedByTransientLossOfFocus) {
                        mPausedByTransientLossOfFocus = false;
                        // startAndFadeIn();
                        mMediaPlayer.play();
                    }
                    if (GlobalDef.mSource == MyCmd.SOURCE_OTHERS_APPS) {
                        GlobalDef.setSource(GlobalDef.mContext, MyCmd.SOURCE_VIDEO);
                    }

                    break;
                default:
                    Log.e(TAG, "Unknown audio focus change code");
            }
        }
    };

    public void abandonAudioFocus() {
        mPausedByTransientLossOfFocus = false;
        GlobalDef.abandonAudioFocus(mAudioFocusListener);
    }

    public int getSource() {
        return VideoUI.SOURCE;
    }
}
