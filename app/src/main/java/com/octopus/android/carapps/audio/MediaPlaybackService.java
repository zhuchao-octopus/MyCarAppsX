/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.octopus.android.carapps.audio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.octopus.android.carapps.car.ui.BR;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.player.ComMediaPlayer;
import com.octopus.android.carapps.common.player.MusicPlayer;
import com.octopus.android.carapps.common.service.ServiceBase;

import java.util.Timer;
import java.util.TimerTask;

public class MediaPlaybackService extends ServiceBase {

    private static MusicPlayer mMediaPlayer;

    private static final String TAG = "MediaPlaybackService";

    @SuppressLint("StaticFieldLeak")
    static MediaPlaybackService mThis;

    public static MusicPlayer getMediaPlayer() {
        if (mMediaPlayer == null) {
            if(mThis != null)
                mMediaPlayer = new MusicPlayer(mThis.mContext);
            else
                mMediaPlayer = new MusicPlayer(null);
        }
        return mMediaPlayer;
    }

    public MediaPlaybackService(Context context) {
        super(context);
    }

    public static MediaPlaybackService getInstance(Context context) {
        if (mThis == null) {
            mThis = new MediaPlaybackService(context);
            mThis.onCreate();
        }
        return mThis;
    }

    public void onCreate() {

        mMediaPlayer = getMediaPlayer();

        mMediaPlayer.setIMediaCallBack(mIMediaCallBack);
        mMediaPlayer.registerMountListener();

    }

    @Override
    public void onDestroy() {

        // mPlayer.release();
        // mPlayer = null;

        if (mMediaPlayer != null) {
            mMediaPlayer.setIMediaCallBack(null);
            mMediaPlayer.unregisterMountListener();
            mMediaPlayer = null;
        }

        // mAudioManager.abandonAudioFocus(mAudioFocusListener);

        super.onDestroy();
    }

    public void doEQResule() {
        mIMediaCallBack.callback(GlobalDef.MSG_UPDATE_EQ_MODE, 0, null);
    }

    private final static int MSG_DELAY_PAUSE = 1;
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DELAY_PAUSE:
                    Log.d("ffck", "MSG_DELAY_PAUSE:" + GlobalDef.mSource);
                    if (GlobalDef.mSource != MusicUI.SOURCE) {
                        stopReportCanbox();
                        mMediaPlayer.setShowBePlay(false);
                        mMediaPlayer.savePlayStatus();
                    }
                    break;
            }
        }
    };

    public void doCmd(int cmd, Intent intent) {
        switch (cmd) {
            case MyCmd.Cmd.SOURCE_CHANGE:
                int source = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);

                Log.d("ffck", "SOURCE_CHANGE:" + source);
                if (source != MusicUI.SOURCE) {
                    mHandler.removeMessages(MSG_DELAY_PAUSE);
                    mHandler.sendEmptyMessageDelayed(MSG_DELAY_PAUSE, 200);

                } else {
                    startReportCanbox();

                    mMediaPlayer.setShowBePlay(true);
                    source = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA2, 0);
                    if (source == MyCmd.SOURCE_CANBOX_PHONE) {

                        mMediaPlayer.resetPlayStatus();
                    }
                    mMediaPlayer.notifyChange(MusicPlayer.PLAYSTATE_CHANGED, 0);
                }
                break;
            case MyCmd.Cmd.RETURN_ABANDON_FOCUS:
                GlobalDef.abandonAudioFocus(mAudioFocusListener);
                break;
            case MyCmd.Cmd.ACC_POWER_OFF:
                Log.d(TAG, "audio ACC_POWER_OFF");
                if (mMediaPlayer != null) {
                    mMediaPlayer.savePlayForSleepRemount();
                }
                MusicActivity.finishActivity(GlobalDef.mContext);
                break;
            case MyCmd.Cmd.ACC_POWER_ON:
                Log.d(TAG, "audio ACC_POWER_ON");
                if (mMediaPlayer != null) {
                    mMediaPlayer.checkSleepOnStorage();
                    if (GlobalDef.mIsMediaWidget) {
                        mMediaPlayer.notifyChange(ComMediaPlayer.PLAYSTATE_CHANGED, 0);
                    }
                }
                break;
            case MyCmd.Cmd.MUSIC_SEND_PLAY_STATUS:
                String s = intent.getStringExtra(MyCmd.EXTRA_COMMON_CMD);
                if (s != null && s.contains("com.android.launcher")) {
                    mMediaPlayer.notifyChange(ComMediaPlayer.PLAYSTATE_CHANGED, 0);
                }
                break;

            case MyCmd.Cmd.REVERSE_STATUS:

                Log.d("ffck", "REVERSE_STATUS");
                int status = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                mIMediaCallBack.callback(GlobalDef.MSG_REVERSE_COME, status, null);
                break;

        }
    }

    public void doKeyControl(int code) {
        //		Log.d("eec","doKeyControl"+code);
        boolean ret = true;
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
                ret = false;
                break;
            case MyCmd.Keycode.KEY_REPEAT:
                mMediaPlayer.setRepeatMode(ComMediaPlayer.REPEAT_ALL);
                break;
            case MyCmd.Keycode.KEY_SHUFFLE:
                mMediaPlayer.setRepeatMode(ComMediaPlayer.REPEAT_SHUFFLE);
                break;
            case MyCmd.Keycode.KEY_REPEAT_ONE:
                mMediaPlayer.setRepeatMode(ComMediaPlayer.REPEAT_CURRENT);
                break;

            case MyCmd.Keycode.PLAY_PAUSE:
                if (mMediaPlayer.isInitialized()) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                        ret = false;
                    } else {
                        mMediaPlayer.play();
                    }
                }
                break;
            default:
                ret = false;
                break;
            // case MyCmd.Keycode.CH_UP:
            // case MyCmd.Keycode.CH_DOWN:
            // Handler h = null;
            // if (mHandlerUICallBack[0] != null) {
            // h = mHandlerUICallBack[0];
            // } else if (mHandlerUICallBack[1] != null) {
            // h = mHandlerUICallBack[1];
            // }
            // if (h != null) {
            // h.sendMessage(h.obtainMessage(MusicPlayer.KEY, code));
            // }
            // break;

        }
        //		if(ret && mPausedByTransientLossOfFocus){
        //			GlobalDef.reactiveSource(mContext, MusicUI.SOURCE, MediaPlaybackService.mAudioFocusListener);
        //			mPausedByTransientLossOfFocus = false;
        //		}
    }

    private static Handler[] mHandlerUICallBack = new Handler[2];

    public static void setUICallBack(Handler cb, int index) {
        mHandlerUICallBack[index] = cb;
    }

    private static ComMediaPlayer.IMediaCallBack mIMediaCallBack = new ComMediaPlayer.IMediaCallBack() {
        public void callback(int what, int status, Object obj) {

            if (what == MusicPlayer.STORAGE_MOUNTED) {
                if (mHandlerUICallBack[0] != null) {
                    mHandlerUICallBack[0].sendMessageDelayed(mHandlerUICallBack[0].obtainMessage(what, status, 0, obj), 20);
                } else if (mThis != null) {
                    if (Util.isRKSystem()) {

                        MusicUI mUI = MusicUI.getInstance(0);
                        if (mUI != null) {
                            Log.d("abd", "mIMediaCallBack!!" + mUI.mGpsRunAfter);
                        }
                        if (mUI != null && mUI.mGpsRunAfter) {
                            mUI.autoPlayGpsRunAfterSleep(status, obj);
                        } else {
                            mThis.doStorageMounted(status);
                        }
                    } else {
                        mThis.doStorageMounted(status);
                    }

                }
            } else if (what == MusicPlayer.STORAGE_EJECT) {
                doStorageEject();
            } else {
                for (int i = 0; i < mHandlerUICallBack.length; ++i) {
                    if (mHandlerUICallBack[i] != null) {
                        mHandlerUICallBack[i].sendMessageDelayed(mHandlerUICallBack[i].obtainMessage(what, status, 0, obj), 20);
                    }
                }
            }
        }
    };

    private void doStorageMounted(int page) {

        Log.d(TAG, "doStorageMounted" + BR.mBootTime + ":" + GlobalDef.mAutoMountByPowerOn);


        if (BR.mBootTime == 0 || GlobalDef.mAutoTest) {
            Log.d(TAG, "doStorageMounted quit!");
            return;
        }

        boolean open = false;
        boolean app0Show = (MusicUI.getInstance(0) != null && !MusicUI.getInstance(0).mPause);

        boolean app1Show = (MusicUI.getInstance(1) != null && !MusicUI.getInstance(1).mPause);

        // GlobalDef.mAutoPlayMusicWhenStorageMounted
        try {
            int i = Settings.Global.getInt(mContext.getContentResolver(), SystemConfig.AUTO_PLAY_MUSIC_DEVICES_MOUNTED);

            GlobalDef.mAutoPlayMusicWhenStorageMounted = (i == 1);

        } catch (SettingNotFoundException snfe) {

        }

        if (GlobalDef.mAutoPlayMusicWhenStorageMounted) {
            if (!app0Show) {
                open = true;
            }
        } else {
            // if (app1Show) {
            // open = true;
            // }
        }

        if (open && !AppConfig.getTopActivity().equals("com.my.filemanager/com.my.filemanager.FileManagerActivity")) {

            if (!Util.isRKSystem() || !GlobalDef.mAutoMountByPowerOn) {
                try {
                    Intent it = new Intent(Intent.ACTION_VIEW);
                    it.setClassName(mContext.getPackageName(), "com.my.audio.MusicActivity");
                    it.putExtra("page", page);
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MusicActivity.setIntentForAndroidQ(it);
                    mContext.startActivity(it);

                    GlobalDef.wakeLockOnce();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }

    }

    private static void doStorageEject() {

        Log.d("abc", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" + MusicUI.getInstance(0));
        if ((MusicUI.getInstance(0) != null && !MusicUI.getInstance(0).mPause)) {
            MusicActivity.finishActivity(GlobalDef.mContext);
            BroadcastUtil.sendToCarServiceSetSource(GlobalDef.mContext, MyCmd.SOURCE_MX51);
            Log.d("abc", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

        } else if ((MusicUI.getInstance(1) != null && !MusicUI.getInstance(1).mPause)) {
            // Kernel.doKeyEvent(Kernel.KEY_BACK);
            BroadcastUtil.sendToCarServiceSetSource(GlobalDef.mContext, MyCmd.SOURCE_MX51);
        }

        if (mMediaPlayer != null) {
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
                        if (GlobalDef.mCanboxNeedPlayInfo) {
                            BroadcastUtil.sendCanboxInfo(mContext, MyCmd.AUDIO_SOURCE_CHANGE, mMediaPlayer.getCurPosition(), (int) (mMediaPlayer.getCurrentPosition() / 1000L), mMediaPlayer.getCurrentTotal());

                        }

                        if (GlobalDef.mIsMediaWidget) {
                            Intent i = new Intent(MyCmd.BROADCAST_CMD_FROM_MUSIC);
                            i.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.MUSIC_SEND_PLAY_TIME);

                            i.putExtra(MyCmd.EXTRA_COMMON_DATA2, mMediaPlayer.getCurrentPosition());
                            i.putExtra(MyCmd.EXTRA_COMMON_DATA3, mMediaPlayer.getDuration());
                            i.setPackage("com.my.appwidget");
                            mContext.sendBroadcast(i);
                        }
                        //							}
                        //						}
                    }

                    // sendBroadcastInt(AUDIO_SOURCE_CHANGE, mCurrentPlayPos,
                    // (int) (position() / 1000L), mCurrentTotal);
                    // Log.d("canbox", "music report");
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
                        Log.v(TAG, "AudioFocus: received AUDIOFOCUS_LOSS" + focusChange);
                        if (mMediaPlayer.isPlaying()) {
                            mPausedByTransientLossOfFocus = true;
                            mMediaPlayer.pause();

                        }
                        if (GlobalDef.mSource == MyCmd.SOURCE_MUSIC) {
                            // GlobalDef.setSource(context, source);
                            BroadcastUtil.sendToCarServiceSetSource(GlobalDef.mContext, MyCmd.SOURCE_OTHERS_APPS);
                        }
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.v(TAG, "AudioFocus: received AUDIOFOCUS_GAIN");
                    if (GlobalDef.mSource == MyCmd.SOURCE_OTHERS_APPS) {
                        //					GlobalDef.setSource(context, source);
                        BroadcastUtil.sendToCarServiceSetSource(GlobalDef.mContext, MyCmd.SOURCE_MUSIC);
                    }

                    if (!mMediaPlayer.isPlaying() && mPausedByTransientLossOfFocus) {
                        mPausedByTransientLossOfFocus = false;
                        // startAndFadeIn();
                        mMediaPlayer.setShowBePlay(true);
                        mMediaPlayer.play();
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
        return MusicUI.SOURCE;
    }

}
