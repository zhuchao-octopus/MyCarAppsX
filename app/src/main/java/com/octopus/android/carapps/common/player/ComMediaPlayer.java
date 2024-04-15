package com.octopus.android.carapps.common.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.common.util.AppConfig;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.audio.MusicUI;
import com.octopus.android.carapps.car.ui.BR;
import com.octopus.android.carapps.car.ui.GlobalDef;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class ComMediaPlayer extends MediaPlayer {

    public String TAG = "unused"; // will define by sub class
    public static final int REPEAT_NONE = 0;

    public static final int REPEAT_CURRENT = 1;

    public static final int REPEAT_ALL = 2;

    public static final int REPEAT_SHUFFLE = 3;

    private int mRepeatMode = REPEAT_ALL;

    private int mLockTime = 500; // ms
    private long mStartPlayTime = 0;

    private String mFileToPlay;

    private final static String[] mCursorCols = new String[]{MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM_ID};

    private final static String[] mCursorColsTime = new String[]{MediaStore.Audio.Media.DURATION};
    private final static String[] mCursorColsAlbum = new String[]{MediaStore.Audio.Media.ALBUM_ID};

    // callback msg
    public static final int PLAYING_LIST_CHANGED = 1;
    public static final int META_CHANGED = 2;
    public static final int PLAY_END = 3;
    public static final int PLAY_FAIL = 4;

    public static final int KEY_FORM_SYSTEM = 7;

    public static final int MEDIA_SEEK = 10;

    public static final int PLAYSTATE_CHANGED = 11;

    public static final int SHOW_SCANNING_ACTION = 12;

    public static final int HIDE_SCANNING_ACTION = 13;

    public static final int QUEUE_CHANGED = 14;

    public static final int STORAGE_MOUNTED = 15;
    public static final int STORAGE_EJECT = 16;

    public static final int STORAGE_MOUNTED_FROM_SLEEP = 17;
    public static final int STORAGE_MOUNTED_NOMEDIA = 20;


    public static final int SEARCH_LIST_FILE_UPDATE = 21;

    public static final int PARK_BRAKE = 17;

    public static final int PLAY_REPEAT = 18;

    public static final int KEY = 19;


    public static final int ID3_TIME_CHANGE = 30;

    private int mIsLock = 0;
    private int mPreLock = 0;

    private boolean isLock() {
        // if ((System.currentTimeMillis() - mStartPlayTime) < mLockTime) {
        Log.d(TAG, "isLock:" + mIsLock);
        // return true;
        // }
        return (mIsLock != 0);
    }

    // /scan
    //private MediaScanner mScanner = null;
    private Method mScannerMethod = null;

    public ComMediaPlayer(Context ac) {
        //if (ac == null) {
        //    ac = ActivityThread.currentApplication();
        //}
        mContext = ac;
        initDiskPath();
        updateCompletionListener(true);
    }

    MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            // Log.d("allen", "onCompletion:" + mp);

            mHandler.removeMessages(MSG_DELAY_COMPLETE_PLAY);
            if (mShowBePlay) {
                if (mUseThread) {
                    if (mIsLock > 0 && ((mIsLock - mPreLock) == 1)) {
                        mHandler.sendEmptyMessageDelayed(MSG_DELAY_COMPLETE_PLAY, TIME_DELAY_COMPLETE_PLAY);
                    } else {
                        nextIfComplete();
                    }
                } else {
                    nextIfComplete();
                }
            }
        }
    };

    public void updateCompletionListener(boolean b) {
        if (b) {

            setOnCompletionListener(listener);
        } else {

            setOnCompletionListener(null);
        }

    }

    private void notifyChange(int what) {
        notifyChange(what, 0, null);

    }

    public void notifyChange(int what, int status) {
        notifyChange(what, status, null);
    }

    private void notifyChange(int what, Object obj) {
        notifyChange(what, 0, obj);
    }

    private void notifyChange(int what, int status, Object obj) {

        if (mIMediaCallBack != null) {
            mIMediaCallBack.callback(what, status, obj);
        }

        if (mScanMusic) {

            if ((what == PLAYSTATE_CHANGED
                    || (what == MEDIA_SEEK && isPlaying())
                    || (status == 1 && what == META_CHANGED))) {
                Intent i = new Intent(MyCmd.BROADCAST_CMD_FROM_MUSIC);
                i.putExtra(MyCmd.EXTRA_COMMON_CMD,
                        MyCmd.Cmd.MUSIC_SEND_PLAY_STATUS);
                if (isInitialized()) {
                    i.putExtra(MyCmd.EXTRA_COMMON_DATA, isPlaying() ? 1 : 0);
                    // Log.d("aa", this.isInitialized()+ ""+isPlaying());
                    i.putExtra(MyCmd.EXTRA_COMMON_DATA2,
                            this.getCurrentPosition());
                    i.putExtra(MyCmd.EXTRA_COMMON_DATA3, this.getDuration());
                    i.putExtra(MyCmd.EXTRA_COMMON_DATA4, getTrackName());
                    i.putExtra(MyCmd.EXTRA_COMMON_OBJECT, getArtworkLimit());
                } else {
                    i.putExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                    // i.putExtra(MyCmd.EXTRA_COMMON_DATA, isPlaying() ? 1 : 0);
                    // Log.d("aa", this.isInitialized()+ ""+isPlaying());
                    // i.putExtra(MyCmd.EXTRA_COMMON_DATA2,
                    // 0);
                    // i.putExtra(MyCmd.EXTRA_COMMON_DATA3, 0);
                    // i.putExtra(MyCmd.EXTRA_COMMON_DATA4, "");
                    // i.putExtra(MyCmd.EXTRA_COMMON_OBJECT, null);
                }
                // i.setPackage("com.android.launcher");
                if (!GlobalDef.mIsMediaWidget) {
                    i.setPackage(AppConfig.getLauncherPackage());
                } else {
                    i.setPackage("com.my.appwidget");
                }
                mContext.sendBroadcast(i);
            }

            if (what == META_CHANGED) { // canbox use
                Intent i = new Intent(MyCmd.BROADCAST_CMD_FROM_MUSIC);
                if (isInitialized()) {
                    i.putExtra(MyCmd.EXTRA_COMMON_DATA, getTrackName());
                    i.putExtra(MyCmd.EXTRA_COMMON_DATA2, getArtistName());
                    i.putExtra(MyCmd.EXTRA_COMMON_DATA3, getAlbumName());
                }
                i.setPackage("com.my.out");
                mContext.sendBroadcast(i);
            }
        }
    }

    public void play(int id) {

        synchronized (this) {
            if (id < mCurrentUriList.size()) {
//				mCurrentPlayPos = id;
//
//				playCurrent();
                if (!mShowBePlay) {
                    return;
                }

                if (isLock()) {
                    Log.d(TAG, "play id lock");
                    return;
                }
                if (mCurrentUriList != null
                        && (id >= 0 && id < mCurrentUriList
                        .size())) {

                    mCurrentPlayPos = id;

                    Log.d(TAG, "play id ok:" + id);
                    if (mUseThread) {
                        if (mThreadPlayFile == null) {
                            mThreadPlayFile = new Thread(mPlayRunable);
                            mThreadPlayFile.start();
                        }
                    } else {
                        playCurrentThread();
                    }

                }
            }
        }
    }

    @Override
    public void pause() {
        synchronized (this) {
            if (isPlaying()) {
                //this.setVolume(0.0f);
                super.pause();
                notifyChange(PLAYSTATE_CHANGED, 0);

            }
        }
    }

    @Override
    public void stop() {

        synchronized (this) {
            if (isInitialized()) {
                super.stop();
                notifyChange(PLAYSTATE_CHANGED, 0);
            }
            mFileToPlay = null;
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
        }
    }

    private boolean mUseThread = true;

    Thread mThreadPlayFile = null;
    Runnable mPlayRunable = new Runnable() {
        public void run() {
            playCurrentThread();

        }
    };

    private void playCurrent() {
        if (!mShowBePlay) {
            return;
        }

        if (isLock()) {
            Log.d(TAG, "playCurrent lock");
            return;
        }
        if (mCurrentUriList != null
                && (mCurrentPlayPos >= 0 && mCurrentPlayPos < mCurrentUriList
                .size())) {

            // Log.d("allen", "playCurrent:" + mCurrentUriList.size());
            if (mUseThread) {
                if (mThreadPlayFile == null) {
                    mThreadPlayFile = new Thread(mPlayRunable);
                    mThreadPlayFile.start();
                }
            } else {
                playCurrentThread();
            }

        }

    }

    // private void playCurrent() {
    // if (!mShowBePlay) {
    // return;
    // }
    // if (mCurrentUriList != null
    // && (mCurrentPlayPos >= 0 && mCurrentPlayPos < mCurrentUriList
    // .size())) {
    // stop();
    // if (mCursor != null) {
    // mCursor.close();
    // mCursor = null;
    // }
    // open(mCurrentUriList.get(mCurrentPlayPos));
    // play();
    //
    // notifyChange(META_CHANGED);
    //
    // saveData(SAVE_DATA_CUR_POS, mCurrentPlayPos);
    // // sendBroadcastInt(AUDIO_SOURCE_CHANGE, mCurrentPlayPos, 0);
    // }
    // }

    private View mOpeningFile;

    public void setOpeningFile(View v) {
        if (v != null) {
            v.setVisibility(View.GONE);
            mOpeningFile = v;
        }
    }

    private void playCurrentThread() {

        try {
            //BroadcastUtil.sendToCarServiceCmd(mContext, MyCmd.Cmd.MCU_AUTO_MUTE);
            Log.d(TAG, ">>playCurrentThread: send");
            Util.doSleep(200);
            // Log.d(TAG, ">>playCurrentThread11: stop");
            // this.setVolume(0.0f);
            if (mCurrentPlayPos >= 0
                    && mCurrentPlayPos < mCurrentUriList.size()) {
                // return;

                mIsLock = (mPreLock + 1);
                if (mOpeningFile != null) {
                    // mHandler.post(new Runnable() {
                    // public void run() {
                    // mOpeningFile.setVisibility(View.VISIBLE);
                    // }
                    // });
                    mHandler.removeMessages(MSG_HIDE_BUSY_DIALOG);
                    mHandler.removeMessages(MSG_SHOW_BUSY_DIALOG);
                    mHandler.sendEmptyMessageDelayed(MSG_SHOW_BUSY_DIALOG,
                            TIME_SHOW_BUSY_DIALOG);
                }

                stop();
                // Util.doSleep(1000);
                // Log.d(TAG, ">>playCurrentThread play:");
                if (!checkIfErrFiles(mCurrentUriList.get(mCurrentPlayPos))) {
                    // this.setVolume(volume);

                    open(mCurrentUriList.get(mCurrentPlayPos));
                    play();

                    notifyChange(META_CHANGED);

                    saveData(SAVE_DATA_CUR_POS, mCurrentPlayPos);
                } else {

                    mIsInitialized = false;
                    doPlayFail(mCurrentUriList.get(mCurrentPlayPos));

                    mNeedSeekToMemory = false;

                }

                if (mOpeningFile != null) {
                    // mHandler.post(new Runnable() {
                    // public void run() {
                    // mOpeningFile.setVisibility(View.GONE);
                    // notifyChange(META_CHANGED);
                    // }
                    //
                    // });

                    mHandler.removeMessages(MSG_HIDE_BUSY_DIALOG);
                    mHandler.removeMessages(MSG_SHOW_BUSY_DIALOG);
                    mHandler.sendEmptyMessage(MSG_HIDE_BUSY_DIALOG);
                }
                mPreLock = mIsLock;
                mIsLock = 0;
                seekToMemory();

                mPlayDelayStart = System.currentTimeMillis();
                Log.d(TAG, "<<playCurrentThread:");
            }
        } catch (Exception e) {
            Log.d(TAG, "<<playCurrentThread: err" + e);
        }
        mThreadPlayFile = null;
        // this.setVolume(1.0f);
    }

    public void setScan(boolean b) {

        if (b) {
            // if (mScanner == null) {
            // mScanner = new MediaScanner(mContext, "external");
            // Locale locale =
            // mContext.getResources().getConfiguration().locale;
            // String language = locale.getLanguage();
            // String country = locale.getCountry();
            // mScanner.setLocale(language + "_" + country);
            // }
            /*
            if (mScannerMethod == null) {
                if (Build.VERSION.SDK_INT >= 25) {
                    try {
                        Class cls = Class.forName("android.media.MediaScanner");
                        Constructor c = cls.getConstructor(Context.class,
                                String.class);
                        mScanner = (MediaScanner) c.newInstance(mContext,
                                "external");

                        mScannerMethod = MediaScanner.class.getMethod(
                                "scanSingleFile", new Class[]{String.class,
                                        String.class});
                        // Uri url = (Uri) mScannerMethod.invoke(p,
                        // "/sdcard/Casablanca.mp3",
                        // null);

                    } catch (Exception ignored) {
                    }
                } else {
                    try {
                        Class cls = Class.forName("android.media.MediaScanner");
                        Constructor c = cls.getConstructor(Context.class);
                        mScanner = (MediaScanner) c.newInstance(mContext);

                        Locale locale = mContext.getResources()
                                .getConfiguration().locale;
                        String language = locale.getLanguage();
                        String country = locale.getCountry();
                        // mScanner.setLocale(language + "_" + country);
                        Method setLocale = MediaScanner.class.getMethod(
                                "setLocale", new Class[]{String.class});

                        if (setLocale != null) {
                            setLocale
                                    .invoke(mScanner, language + "_" + country);

                            mScannerMethod = MediaScanner.class.getMethod(
                                    "scanSingleFile", new Class[]{
                                            String.class, String.class,
                                            String.class});
                        }
                    } catch (Exception e) {
                        Log.d("abc", "" + e);
                    }
                }
            }*/

        }
        mScanMusic = b;
    }

    private boolean mScanMusic = false;

    private void open(String path) {

        synchronized (this) {
            try {

                // Log.d(TAG, ">>open:"+path);
                reset();
                setDataSource(path);
                prepare();
                mFileToPlay = path;
                mStartPlayTime = System.currentTimeMillis();

            } catch (Exception e) {
                // if (e != null) {
                // Log.e("MovicePlayer",
                // "" + e.toSring() + "        " + e.getMessage());
                // }
                doPlayFail(path);
                mIsInitialized = false;
                return;
            }

            if (mScanMusic) {
                try {
                    doScan(path);
                } catch (Exception e) {
                    Log.d(TAG, "e:" + e);
                }
            }

            mFailNum = 0;
            mIsInitialized = true;
        }
    }

    public void doScan(String path) {
        if (path == null) {
            return;
        }
        if (mCursor == null) {
            ContentResolver resolver = mContext.getContentResolver();
            Uri uri;
            String where;
            String selectionArgs[];
            if (path.startsWith("content://media/")) {
                uri = Uri.parse(path);
                where = null;
                selectionArgs = null;
            } else {
                // uri = MediaStore.Audio.Media.getContentUriForPath(path);
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;// test
                where = MediaStore.Audio.Media.DATA + "=?";
                selectionArgs = new String[]{path};
            }

            try {
                mCursor = resolver.query(uri, mCursorCols, where,
                        selectionArgs, null);
                if (mCursor != null) {
//					if (mCursor.getCount() == 0) {
//						if (mScannerMethod != null) {
//							try {
//								if (Build.VERSION.SDK_INT >= 25) {
//									uri = (Uri) mScannerMethod.invoke(mScanner,
//											path, null);
//								} else {
//									uri = (Uri) mScannerMethod.invoke(mScanner,
//											path, "external", null);
//
//								}
//							} catch (Exception e) {
//
//							}
//							Log.d(TAG, "mScannerMethod:" + uri);
//							if (uri != null) {
//								mCursor = resolver.query(uri, mCursorCols,
//										where, selectionArgs, null);
//							}
//						}
//					}

                    Log.d(TAG, "mScannerMethod no canner:" + uri);
                    if (mCursor.getCount() == 0) {
                        mCursor.close();
                        mCursor = null;
                    } else {
                        mCursor.moveToNext();

                    }
                }
            } catch (UnsupportedOperationException ex) {
            }
        }
        mFileToPlay = path;
    }

    private boolean mIsInitialized;

    public boolean isInitialized() {
        return mIsInitialized;
    }

    /**
     * Starts playback of a previously opened file.
     */
    // private int mStep;
    // private final static int FADER_STEP = 9;
    // private AudioManager mAudioManager;
    private void doMuteFirstRunMusic() {
        // if (mAudioManager.getStreamVolume(AppConfig.MEDIA_SOUND_CHANNEL) > 0)
        // {
        // mAudioManager.setStreamVolume(AppConfig.MEDIA_SOUND_CHANNEL, 0, 0);
        // mStep = 0;
        // mHandlerUnMuteFader.removeMessages(0);
        // mHandlerUnMuteFader.sendEmptyMessageDelayed(0, 600);
        // }
    }

    private void unMuteFader(int fader) {

        // int mute;
        // mute = (AppConfig.MEDIA_SOUND_VOLUME * fader / FADER_STEP);
        // mAudioManager.setStreamVolume(AppConfig.MEDIA_SOUND_CHANNEL, mute,
        // 0);
        //
        // if (fader < FADER_STEP) {
        // mHandlerUnMuteFader.sendEmptyMessageDelayed(0, 95);
        // }
    }

    public void resetVolume() {
        // Log.d(TAG, "resetVolume:" + mStep);
        // // if (mStep == -1) {
        // // mHandlerUnMuteFader.removeMessages(0);
        // mAudioManager.setStreamVolume(AppConfig.MEDIA_SOUND_CHANNEL,
        // AppConfig.MEDIA_SOUND_VOLUME, 0);
        // }
    }

    // private Handler mHandlerUnMuteFader = new Handler() {
    // public void handleMessage(android.os.Message msg) {
    // // if (mShowBePlay) {
    // ++mStep;
    // unMuteFader(mStep);
    // // } else {
    // // mStep = 0;
    // // }
    // };
    // };

    public boolean isPlaying() {
        try {
            return super.isPlaying();
        } catch (Exception e) {
            Log.e(TAG, "isPlaying exception: " + e);
        }
        return false;
    }

    public void play() {
        try {

            // Log.d(TAG, "play!!" + isPlaying());
            if (!isPlaying()) {
                //this.setVolume(1.0f);
                doMuteFirstRunMusic();
                start();

                notifyChange(PLAYSTATE_CHANGED, 1);

                // requestAudioFocus();
            } else {

            }
        } catch (Exception e) {
            if (e != null) {

                Log.d(TAG, "play:" + e.getMessage());
            }
        }

    }

    public void prev() {

        synchronized (this) {
            // if (isLock()) {
            // return;
            // }
            if (mCurrentUriList.size() != 0) {
                if (mRepeatMode == REPEAT_SHUFFLE) {
                    mCurrentPlayPos = getRandomSong();
                } else {
                    mCurrentPlayPos = (mCurrentPlayPos + mCurrentUriList.size() - 1)
                            % mCurrentUriList.size();
                }

                if (!isLock()
                        && (System.currentTimeMillis() - mPlayDelayStart) > TIME_DELAY_PLAY) {
                    playCurrent();
                } else {
                    if (!isLock() && this.isInitialized() && isPlaying()) {
                        pause();
                    }
                    playDelay();
                    notifyChange(META_CHANGED, 1);
                }
                mPlayDelayStart = System.currentTimeMillis();
            }
        }
    }

    private Random mRandom = new Random();

    private int getRandomPositive() {
        int r = mRandom.nextInt();
        if (r < 0)
            r = 0 - r;
        return r;
    }

    private int getRandomSong() {
        int pos = (getRandomPositive()) % mCurrentUriList.size();
        if (pos == mCurrentPlayPos) {
            pos = (mCurrentPlayPos + 1) % mCurrentUriList.size();
        }
        return pos;
    }

    public void next() {

        synchronized (this) {
            // if (isLock()) {
            // return;
            // }
            if (mCurrentUriList.size() != 0) {
                if (mRepeatMode == REPEAT_SHUFFLE) {
                    mCurrentPlayPos = getRandomSong();
                } else {
                    mCurrentPlayPos = (mCurrentPlayPos + 1)
                            % mCurrentUriList.size();
                }

                if (!isLock()
                        && (System.currentTimeMillis() - mPlayDelayStart) > TIME_DELAY_PLAY) {
                    playCurrent();
                } else {
                    if (!isLock() && this.isInitialized() && isPlaying()) {
                        pause();
                    }
                    playDelay();
                    notifyChange(META_CHANGED, 1);
                }
                mPlayDelayStart = System.currentTimeMillis();

            }
        }
    }

    public void nextIfComplete() {
        if (!mShowBePlay) {
            return;
        }
        // boolean isplay = false;
        // try{
        // isplay = isPlaying();
        // }catch (Exception e){
        //
        // }
        // if(isplay){
        // Log.d(TAG, "??nextIfComplete:"+isplay);
        // return;
        // }
        synchronized (this) {
            if (mFailNum != 0)
                return;

            if (mCurrentUriList.size() != 0) {
                if (mRepeatMode == REPEAT_SHUFFLE) {
                    mCurrentPlayPos = getRandomSong();
                } else if (mRepeatMode == REPEAT_CURRENT) {

                } else {
                    mCurrentPlayPos = (mCurrentPlayPos + 1)
                            % mCurrentUriList.size();
                }
            }
            mStartPlayTime = 0;
            playCurrent();
        }
    }

    // MediaPlayer.OnCompletionListener listener = new
    // MediaPlayer.OnCompletionListener() {
    // public void onCompletion(MediaPlayer mp) {
    //
    // nextIfComplete();
    // }
    // };

    public void ff() {

        synchronized (this) {
            if (isInitialized()) {
                int pos = getCurrentPosition();
                pos += 15000;
                if (pos < 0)
                    pos = 0;
                if (pos > getDuration())
                    pos = getDuration();
                seekTo(pos);
            }
        }

    }

    public void fr() {
        synchronized (this) {
            if (isInitialized()) {
                int pos = getCurrentPosition();
                pos -= 15000;
                if (pos < 0)
                    pos = 0;
                if (pos > getDuration())
                    pos = getDuration();
                seekTo(pos);
            }
        }
    }

    public int pressRepeatMode() {

        synchronized (this) {
            if (mRepeatMode == REPEAT_NONE) {
                setRepeatMode(REPEAT_ALL);
            } else if (mRepeatMode == REPEAT_ALL) {
                setRepeatMode(REPEAT_CURRENT);
            } else if (mRepeatMode == REPEAT_CURRENT) {
                setRepeatMode(REPEAT_SHUFFLE);
            } else {
                setRepeatMode(REPEAT_NONE);
            }
//			notifyChange(PLAY_REPEAT);

            return mRepeatMode;

        }
    }

    public int pressRepeatMode2() {

        synchronized (this) {
            if (mRepeatMode == REPEAT_ALL) {
                setRepeatMode(REPEAT_CURRENT);
            } else if (mRepeatMode == REPEAT_CURRENT) {
                setRepeatMode(REPEAT_SHUFFLE);
            } else {
                setRepeatMode(REPEAT_ALL);
            }
//			notifyChange(PLAY_REPEAT);

            return mRepeatMode;

        }
    }

    public int setRepeatMode(int mode) {

        synchronized (this) {
            mRepeatMode = mode;
            notifyChange(PLAY_REPEAT);
            saveData(SAVE_DATA_REPEAT, mRepeatMode);
            return mRepeatMode;
        }
    }

    public int getRepeatMode() {
        return mRepeatMode;
    }

    // private ContentResolver mContentResolver = null;
    public final static String MEDIA_PLAY_PATH = "sdcard";// MyCmd.get_PATH_MEDIA1();

    private ArrayList<String> mCurrentPlayingShow = new ArrayList<String>(); // just

    private static String SAVE_DATA_LIST = "audio_data_list";
    private static String SAVE_DATA_URL = "url";
    private static String SAVE_DATA_REPEAT = "repeat";
    private static String SAVE_DATA_PLAY_TIME = "time";
    // private static String SAVE_DATA_PAGE = "audio_page";

    private static String SAVE_DATA_PLAY_MUSIC_NUM = "num";
    private static String SAVE_DATA_VISUALIZER_STYLE = "visualizer_style";

    private static String SAVE_DATA_ERR_FILES_NUM = "err_files_num";
    private static String SAVE_DATA_ERR_FILES = "err_files";

    protected String[] mMediaFile;
    private final static int SMALL_FILE = 800 * 1024;//

    private boolean isMediaFile(String file) {
        if (file != null) {
            file = file.toLowerCase();
            for (String ext : mMediaFile) {
                if (file.endsWith(ext)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMediaFile(File f) {
        if (isMediaFile(f.getName())) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(f);
                if (mScanMusic) {
                    if (fis.available() > SMALL_FILE) {
                        return true;
                    }
                } else {
                    return true;
                }
            } catch (Exception ce) {

            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Exception ce) {

                    }
                }
            }
        }
        return false;
    }

    private String mCurrentFolderPath = null;
    private int mCurrentFolderNum = 0;

    public int getStorageCurrentFolderNumber() {
        return mCurrentFolderNum;
    }

    public String getStorageCurrentFolderPath() {
        return mCurrentFolderPath;
    }

    public boolean checkPlayingListIsExist(String path) {
        int[] del = new int[mCurrentUriList.size()];
        if (path != null) {
            for (int i = 0; i < mCurrentUriList.size(); ++i) {
                if (mCurrentUriList.get(i).startsWith(path)) {
                    del[i] = 1;
                }
            }
        }

        return true;
    }

    public void saveData(String s, int v) {
        saveDataEx(mSaveDataPath, s, v);
    }

    private void saveDataEx(String data, String s, int v) {
        SharedPreferences.Editor sharedata = mContext.getSharedPreferences(
                data, 0).edit();
        sharedata.putInt(s, v);
        sharedata.commit();
    }

    private void saveDataEx(String data, String s, String v) {
        SharedPreferences.Editor sharedata = mContext.getSharedPreferences(
                data, 0).edit();
        sharedata.putString(s, v);
        sharedata.commit();
    }

    public int getCurSongNum() {
        return mCurrentUriList.size();
    }

    public int getData(String s) {
        return getDataEx(mSaveDataPath, s);
    }

    private int getDataEx(String data, String s) {
        SharedPreferences sharedata = mContext.getSharedPreferences(data, 0);
        return sharedata.getInt(s, 0);
    }

//	public void initPlayingList() {
//		mCurrentUriList.clear();
//		// get saved list & checked save list
//		mCurrentPlayPos = getData(SAVE_DATA_CUR_POS);
//		mRepeatMode = getData(SAVE_DATA_REPEAT);
//		searchFilePrepare(mUsbUriList, mUSBFolderUriList, MEDIA_PLAY_PATH);
//		// for (String s : mUsbUriList) {
//		// mCurrentUriList.add(s);
//		// }
//
//		mCurrentUriList = mUsbUriList;
//
//		if (mCurrentUriList != null) {
//			mCurrentShow.clear();
//			for (String s : mCurrentUriList) {
//				mCurrentShow.add(cutPathSuffix(cutPathPrex(s)));
//			}
//		}
//
//		if (mCurrentPlayPos >= mCurrentUriList.size()) {
//			mCurrentPlayPos = 0;
//		}
//		// by allen
//		if (mCurrentUriList.size() > 0) {
//
//			getErrFiles();
//		} else {
//			updateErrFiles(-1);
//		}
//	}

    private Context mContext;

    private String mPathFail;

    // Toast mToastFail = null;

    private void doPlayFail(String path) {

        ++mFailNum;

        if (mContext != null) {
            mPathFail = path;
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (mPathFail != null) {
                        String s;
                        int start = mPathFail.lastIndexOf('/');
                        if (start != -1) {
                            s = mPathFail.substring(start + 1,
                                    mPathFail.length());
                        } else {
                            s = mPathFail;
                        }
                        // if (mToastFail != null) {
                        // mToastFail.cancel();
                        // mToastFail = null;
                        // }
                        // mToastFail = Toast.makeText(mContext, mPathFail +
                        // "  "
                        // + mContext.getString(R.string.play_fail),
                        // Toast.LENGTH_SHORT);
                        // mToastFail.show();

                        notifyChange(
                                PLAY_FAIL,
                                mPathFail
                                        + "  "
                                        + mContext
                                        .getString(R.string.play_fail));

                    }
                }
            }, 2);

            mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (mShowBePlay && !mIsInitialized) {
                        if (mCurrentUriList.size() > 1
                                && mFailNum < mCurrentUriList.size()) {
                            mCurrentPlayPos = (mCurrentPlayPos + 1)
                                    % mCurrentUriList.size();
                            playCurrent();
                        }
                    } else {
                        mFailNum = 0;
                    }
                }
            }, 1500);
        }
    }

    private long mPlayDelayStart = 0;
    private int mPlayDelayIndex = -1;

    private void playDelay() {
        Log.d(TAG, "playDelay!!");
        mPlayDelayIndex = mCurrentPlayPos;
        mHandler.removeMessages(MSG_DELAY_PLAY);
        mHandler.sendEmptyMessageDelayed(MSG_DELAY_PLAY, TIME_DELAY_PLAY);
    }

    private final static int MSG_DELAY_PLAY = 0;

    private final static int TIME_DELAY_PLAY = 300;

    private final static int MSG_DELAY_COMPLETE_PLAY = 1;
    private final static int TIME_DELAY_COMPLETE_PLAY = 150;


    private final static int MSG_SHOW_BUSY_DIALOG = 3;
    private final static int TIME_SHOW_BUSY_DIALOG = 150;
    private final static int MSG_HIDE_BUSY_DIALOG = 4;


    private final static int MSG_CHECK_STORAGE_IF_SLEEP_ON = 5;


    private final static int MSG_CHECK_SAVE_PLAY_STATUS_IF_OPENING = 6;

    private int mFailNum;
    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK_SAVE_PLAY_STATUS_IF_OPENING:
                    if (mRepeatCheckSavePlayStatus > 0) {
                        savePlayStatus();
                        mRepeatCheckSavePlayStatus--;
                    }
                    break;
                case MSG_DELAY_PLAY:
                    if (mPlayDelayIndex == mCurrentPlayPos) {
//					Log.d("eec", "MSG_DELAY_PLAY!!!!!!!"+isLock()+mShowBePlay);
                        if (!isLock()) {
                            playCurrent();
                        } else {
                            Log.d(TAG,
                                    "MSG_DELAY_PLAY: is few in here. still lock???");
                            playDelay();
                        }
                    }
                    break;
                case MSG_DELAY_COMPLETE_PLAY:

                    // Log.d(TAG,
                    // "MSG_DELAY_COMPLETE_PLAY"+":"+mThis+":"+isInitialized()+":"+isPlaying());

                    if (mIsLock > 0 && ((mIsLock - mPreLock) == 1)) {
                        mHandler.removeMessages(MSG_DELAY_COMPLETE_PLAY);
                        mHandler.sendEmptyMessageDelayed(MSG_DELAY_COMPLETE_PLAY,
                                TIME_DELAY_COMPLETE_PLAY);

                    } else {

                        nextIfComplete();
                    }
                    break;
                case MSG_SHOW_BUSY_DIALOG:
                    Log.e(TAG, ">>>>>>>>>>>MSG_SHOW_BUSY_DIALOG ");

                    if (mOpeningFile != null) {
                        mOpeningFile.setVisibility(View.VISIBLE);
                    }
                    break;
                case MSG_HIDE_BUSY_DIALOG:
                    Log.e(TAG, ">>>>>>>>>>>MSG_HIDE_BUSY_DIALOG ");

                    if (mOpeningFile != null) {
                        mOpeningFile.setVisibility(View.GONE);
                    }
                    break;
                case MSG_CHECK_STORAGE_IF_SLEEP_ON:
                    doCheckSleepOnStorage();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public int getSavePlayTime() {
        return getData(SAVE_DATA_PLAY_TIME);
    }

    public void savePlayTime() {
        if (isInitialized()) {
            int time = getCurrentPosition();
            saveData(SAVE_DATA_PLAY_TIME, time);
        }

    }

    boolean mNeedSeekToMemory = false;

    private void seekToMemory() {
        if (mNeedSeekToMemory) {
            mNeedSeekToMemory = false;
            int time = getData(SAVE_DATA_PLAY_TIME);

            if (isInitialized()) {
                int d = getDuration();

                if (time < getDuration()) {
                    seekTo(time);
                }
            }
        }
    }

    public void playCurrentMemory() {

        mNeedSeekToMemory = true;
        playCurrent();
        notifyChange(META_CHANGED);
        // int time = getData(SAVE_DATA_PLAY_TIME);
        //
        //
        //
        // if (isInitialized()) {
        // int d = getDuration();
        //
        // if (time < getDuration()) {
        // seekTo(time);
        // }
        // }
    }

    private static final int SAVE_STATUS_PLAY = 0x1 << 1;

    private static final int SAVE_STATUS_PAUSE = 0x1 << 2;

    private static final int SAVE_STATUS_STOP = 0x1 << 3;

    private static final int SAVE_STATUS_FIRST_RUN = 0x1 << 16;
    private int mSavePlayStatus = SAVE_STATUS_FIRST_RUN;
    private int mRepeatCheckSavePlayStatus = 40;

    public void savePlayStatus() {
//		Log.d("eec", "savePlayStatus" + isPlaying() + mThreadPlayFile);
        mHandler.removeMessages(MSG_DELAY_PLAY);
        if (mThreadPlayFile != null) {
            mHandler.removeMessages(MSG_CHECK_SAVE_PLAY_STATUS_IF_OPENING);
            mHandler.sendEmptyMessageDelayed(
                    MSG_CHECK_SAVE_PLAY_STATUS_IF_OPENING, 50);
        } else {
            mRepeatCheckSavePlayStatus = 40;
            try {
                if (isPlaying()) {
                    mSavePlayStatus = SAVE_STATUS_PLAY;
                    pause();
                }
                savePlayTime();
            } catch (Exception e) {

            }
        }
    }

    public void savePlayStatusRelease() {
        if (isPlaying()) {
            savePlayTime();
            try {
                stop();
                reset();
                setDataSource("");
                prepare();
                start();
            } catch (Exception e) {
                Log.d(TAG, "releasePlay:" + e); // must to here
            }
        }

    }

    public void stopPlay() {

        savePlayTime();
        stop();
        mIsInitialized = false;
    }

    public void releasePlay() {

        if (mIsInitialized) {
            try {
                stop();
                reset();
                setDataSource("");
                prepare();
                start();
            } catch (Exception e) {
                Log.d(TAG, "releasePlay:" + e); // must to here
            }
        }

        saveData(SAVE_DATA_CUR_POS, 0);
        saveData(SAVE_DATA_PLAY_TIME, 0);

        mIsInitialized = false;
    }

    // old method , the true reset
    // public void resetPlayStatus() {
    // switch (mSavePlayStatus) {
    // case SAVE_STATUS_PLAY:
    // play(); //is ok?
    // break;
    // case SAVE_STATUS_PAUSE:
    // case SAVE_STATUS_STOP:
    // default:
    // break;
    // }
    // mSavePlayStatus = 0;
    // }

    public boolean resetPlayStatus() {
        boolean ret = false;
        if (isInitialized()) {
            if (!isPlaying()) {
                play();
            }
            ret = true;
        } else {

        }

        notifyChange(META_CHANGED);
        return ret;
    }

    private OnErrorListener mOnErrorListener = new OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            // TODO Auto-generated method stub
            Log.e(TAG, ">>>>>>>>>>>>>>>>>>>onError: what=" + what + " ,extra="
                    + extra);
            if (what == MEDIA_ERROR_SERVER_DIED) {

            }
            return true;
        }

    };
    private boolean mShowBePlay = true;

    public void setShowBePlay(boolean b) {
        Log.d(TAG, "setShowBePlay:" + b);
        mShowBePlay = b;
        if (b) {
            mHandler.removeMessages(MSG_CHECK_SAVE_PLAY_STATUS_IF_OPENING);
            setOnErrorListener(mOnErrorListener);
        } else {
            setOnErrorListener(null);
        }
    }

    public boolean getShowBePlay() {
        return mShowBePlay;
    }

    private BroadcastReceiver mMountReceiver = null;

    public void unregisterMountListener() {
        if (mMountReceiver != null) {
            mContext.unregisterReceiver(mMountReceiver);
            mMountReceiver = null;
        }
        if (mScanMusic) {
            unregisterListener();
        }
    }

    private void flashAllCfq() {

        if (Util.isNexellSystem60()) {
            // Util.sudoExec("chmod:666:/sys/block/mmcblk1/queue/scheduler");
            // Util.setFileValue("/sys/block/mmcblk1/queue/scheduler", "cfq");
            // Util.sudoExec("chmod:666:/sys/block/mmcblk2/queue/scheduler");
            // Util.setFileValue("/sys/block/mmcblk2/queue/scheduler", "cfq");

            // Util.sudoExec("chmod:666:/sys/block/sda/queue/scheduler");
            Util.setFileValue("/sys/block/sda/queue/scheduler", "cfq");
            // Util.sudoExec("chmod:666:/sys/block/sdb/queue/scheduler");
            Util.setFileValue("/sys/block/sdb/queue/scheduler", "cfq");
            // Util.sudoExec("chmod:666:/sys/block/sdc/queue/scheduler");
            Util.setFileValue("/sys/block/sdc/queue/scheduler", "cfq");
            // Util.sudoExec("chmod:666:/sys/block/sdd/queue/scheduler");
            Util.setFileValue("/sys/block/sdd/queue/scheduler", "cfq");
        }
    }

    // private ArrayList<String> mSaveForSleepPlayList;
    // private ArrayList<String> mSaveForSleepUsbUriList;
    private int mSaveForSleepPos = -1;
    private int mSaveForSleepPage = -1;
    private int mSaveForSleepFolder = -1;
    private int mSaveForSleepTime = -1;
    public String mSaveForSleepPath;

    public int mMemoryPlayFromSleep = -1;

    public boolean mSleepInPlay = false;

    public void checkSleepOnStorage() {
        Log.d(TAG, "checkSleepOnStorage");

        mHandler.removeMessages(MSG_CHECK_STORAGE_IF_SLEEP_ON);
        mHandler.sendEmptyMessageDelayed(MSG_CHECK_STORAGE_IF_SLEEP_ON, GlobalDef.TIME_ONE_SLEEP_MOUNT);
    }

    public void doCheckSleepOnStorage() {

        Log.d(TAG, "doCheckSleepOnStorage!!" + mCurrentUriList.size());

        if (mCurrentUriList.size() != 0) {
            File f = new File(mCurrentUriList.get(0));
            Log.d(TAG, f.exists() + "doCheckSleepOnStorage" + mCurrentUriList.get(0));
            if (!f.exists()) {
                mCurrentUriList.clear();
                mCurrentShow.clear();
                notifyChange(QUEUE_CHANGED, MusicPlayer.LIST_PLAYING);
            }
        }
    }

    private boolean isUSBSaveDevice(String path) {
        if (mCurrentUriList.get(0).contains("USB")) {
            return true;
        } else {
            if (Util.isPX6() && path != null
                    && path.startsWith("/storage/MediaCard/")) {
                return true;
            }
        }
        return false;
    }

    public void resetSleepSDPlay() {
        playCurrent();
    }

    //	public boolean mSaveSDPlayIfSleep = false;
    public void savePlayForSleepRemount() {
        mMemoryPlayFromSleep = -1;
        mSaveForSleepPath = null;
        mHandler.removeMessages(MSG_CHECK_STORAGE_IF_SLEEP_ON);
        if (mCurrentUriList.size() > 0) {
            savePlayStatus();
            if (isUSBSaveDevice(mCurrentUriList.get(0))) {

                mSaveForSleepPage = getData(SAVE_DATA_PAGE);
                mSaveForSleepPos = getData(SAVE_DATA_CUR_POS);
                mSaveForSleepFolder = getData(SAVE_DATA_CUR_FOLDER);
                mSaveForSleepTime = getData(SAVE_DATA_PLAY_TIME);

                if (mSaveForSleepPos >= 0
                        && mSaveForSleepPos < mCurrentUriList.size()) {
                    mSaveForSleepPath = mCurrentUriList.get(mSaveForSleepPos);

                    mMemoryPlayFromSleep = 0;
                }

                Log.d(TAG, mCurrentUriList.get(0)
                        + "::savePlayForSleepRemount:" + mSaveForSleepPath
                        + ":" + mSaveForSleepPos + ":" + mSaveForSleepPage);
            } else {
//				mSaveSDPlayIfSleep = true;
//				Log.d(TAG, "mSaveSDPlayIfSleep:" + mSaveSDPlayIfSleep);
            }

            if (isInitialized()) {
//				releasePlay();
                try {
                    stop();
                    reset();
                    setDataSource("");
                    prepare();
                    start();
                } catch (Exception e) {
                    Log.d(TAG, "releasePlay:" + e); // must to here
                }
                mIsInitialized = false;
            }
        }
//		else {
//			mMemoryPlayFromSleep = -1;
//			mSaveForSleepPath = null;
//		}
    }

    public void savePlayForSleepRemount(String path) {
        // if (Util.isPX5()) {
        // if (GlobalDef.isOneSleepRemountTime()) {
        // if (mCurrentUriList.size() > 0) {
        //
        // mSaveForSleepPage = getData(SAVE_DATA_PAGE);
        // mSaveForSleepPos = getData(SAVE_DATA_CUR_POS);
        // mSaveForSleepFolder = getData(SAVE_DATA_CUR_FOLDER);
        //
        // if (mSaveForSleepPos >= 0
        // && mSaveForSleepPos < mCurrentUriList.size()) {
        // mSaveForSleepPath = mCurrentUriList
        // .get(mSaveForSleepPos);
        //
        // mMemoryPlayFromSleep = 0;
        // }
        // }
        // } else {
        //
        // // mSaveForSleepPos = -1;
        // // mSaveForSleepPath = null;
        // }
        // }
        // Log.d("allen", "savePlayForSleepRemount:"+mSaveForSleepPath);
    }

    public boolean mSleepRestore = false;

    private void restorePlayForSleepRemount(String path, int id) {
        if (Util.isRKSystem()) {

            Log.d(TAG, "1restorePlayForSleepRemount:" + path);

            mQeryFlag = QUERY_FLAG_ALL;
            switch (id) {
                case LIST_USB:
                    mQeryFlag &= ~QUERY_FLAG_USB;
                    break;
                case LIST_USB2:
                    mQeryFlag &= ~QUERY_FLAG_USB2;
                    break;
                case LIST_USB3:
                    mQeryFlag &= ~QUERY_FLAG_USB3;
                    break;
                case LIST_USB4:
                    mQeryFlag &= ~QUERY_FLAG_USB4;
                    break;
                case LIST_SD:
                    mQeryFlag &= ~QUERY_FLAG_SD;
                    break;
                case LIST_SD2:
                    mQeryFlag &= ~QUERY_FLAG_SD2;
                    break;

            }
            // if (GlobalDef.isOneSleepRemountTime() && mMemoryPlayFromSleep ==
            // 0) {
            if (mSaveForSleepPath != null && mSaveForSleepPath.startsWith(path)) {
                File f = new File(mSaveForSleepPath);
                if (f.exists()) {
                    mMemoryPlayFromSleep = 1;
                    saveData(SAVE_DATA_PAGE, mSaveForSleepPage);
                    saveData(SAVE_DATA_CUR_FOLDER, mSaveForSleepFolder);
                    saveData(SAVE_DATA_CUR_POS, mSaveForSleepPos);
                    saveData(SAVE_DATA_PLAY_TIME, mSaveForSleepTime);
                    mSleepRestore = true;
                }
                Log.d(TAG, "1restorePlayForSleepRemount:" + f.exists()
                        + ":" + mSaveForSleepPage + ":" + mSaveForSleepPos + ":" + mSaveForSleepFolder);
            }
            Log.d(TAG, "2restorePlayForSleepRemount:" + mSaveForSleepPath);
            // }
        }
    }

    private String mMountPath;

    public void registerMountListener() {
        if (mMountReceiver == null) {
            mMountReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
//					Log.e(TAG, "mediaplayer+" + action);
                    if (action.equals(Intent.ACTION_MEDIA_EJECT)) {

                        String path = intent.getData().toString()
                                .substring("file://".length());
                        Log.e(TAG, "eject+" + path);
                        if (path != null && path.toString().contains("cdrom")) {
                            return;
                        }

                        if (Util.isRKSystem()) {
                            if (GlobalDef.mPowerOffTime != 0) {
                                Log.d(TAG, "mPowerOffTime == 0 no eject");
                                return;
                            }

                            if (GlobalDef.isOneSleepRemountTime()) {
                                Log.d(TAG, "isOneSleepRemountTime no eject");
                                return;
                            } else {
                                mSaveForSleepPath = null;
                            }

//							if(path!=null && (path.endsWith("3") ||
//									path.endsWith("4")) && GlobalDef.isOneSleepRemountTime()){
//								Log.d(TAG, "eeeeeeee222222eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
//								return;
//							}	
                        }


                        if (path != null) {

                            // Intent i = new Intent();
                            Log.d(TAG, "eject play size" + mCurrentUriList.size());
                            if (mCurrentUriList.size() != 0) {
                                Log.d(TAG, "eject play path" + mCurrentUriList.get(0));
                                if (mCurrentUriList.get(0).startsWith(path)) {
                                    // savePlayForSleepRemount(path);
                                    closeExternalStorageFiles(intent.getData()
                                            .getPath());

                                    mCurrentUriList.clear();
                                    mCurrentTotal = 0;

                                    if (mSaveForSleepPath == null
                                            || mSaveForSleepPath
                                            .startsWith(path)) {

                                        notifyChange(STORAGE_EJECT);
                                        mCursor = null;
                                        notifyChange(PLAYSTATE_CHANGED);
                                    }
                                }
                            }

                            notifyChange(QUEUE_CHANGED, getIdFromPath(path));

                            cleanMediaChangeStatus(path);

                            if (!GlobalDef.isOneSleepRemountTime() && mScanMusic) {
                                Toast.makeText(mContext,
                                        mContext.getString(R.string.device_eject),
                                        Toast.LENGTH_SHORT).show();
                            }

                        }

                    } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                        String path = intent.getData().toString()
                                .substring("file://".length());
                        Log.d(TAG, "mount+" + path);
                        if (path != null && path.toString().contains("cdrom")) {
                            return;
                        }

                        if (Util.isRKSystem()) {
                            if (GlobalDef.mPowerOffTime != 0) {
                                Log.d(TAG, "mount GlobalDef.mPowerOffTime:" + GlobalDef.mPowerOffTime);
                                return;
                            }

                            initDiskPath();

                            Log.d(TAG, "mBootTime:" + BR.mBootTime + ":currentTimeMillis:" + System.currentTimeMillis());

                            if (BR.mBootTime != 0) {

                                Log.d(TAG, GlobalDef.isOneSleepRemountTime() + ":" + path + ":"
                                        + mSaveForSleepPath);

                                if ((!GlobalDef.isOneSleepRemountTime())
                                        || (mSaveForSleepPath != null && mSaveForSleepPath
                                        .startsWith(path))) {
                                    mMountPath = path;

                                    GlobalDef.mAutoMountByPowerOn = GlobalDef.isOneSleepRemountTime();

//									Log.d(TAG, GlobalDef.isOneSleepRemountTime()+":"+path + "f222222222222222ff"
//											+ mSaveForSleepPath);

                                    mHandler.postDelayed(new Runnable() {
                                        public void run() {
                                            if (mUpdateAsyncTask == null) {

                                                Log.d(TAG, "do mount scan AsyncTask");

                                                mUpdateAsyncTask = new UpdateAsyncTask();
                                                mUpdateAsyncTask.execute();

                                            }
                                        }
                                    }, 150);
                                } else {
                                    mHandler.postDelayed(new Runnable() {
                                        public void run() {
                                            notifyChange(STORAGE_MOUNTED_NOMEDIA, 0);
                                        }
                                    }, 150);
                                }
                            } else {
                                mHandler.postDelayed(new Runnable() {
                                    public void run() {
                                        notifyChange(STORAGE_MOUNTED_NOMEDIA, 0);
                                    }
                                }, 150);
                            }

                        } else {
                            flashAllCfq();


                            initDiskPath();

                            mMountPath = path;

                            mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    if (mUpdateAsyncTask == null) {
                                        mUpdateAsyncTask = new UpdateAsyncTask();
                                        mUpdateAsyncTask.execute();
                                    }
                                }
                            }, 150);

                        }


                        if (BR.mBootTime != 0 && mScanMusic) {
                            Toast.makeText(mContext,
                                    mContext.getString(R.string.device_mount),
                                    Toast.LENGTH_SHORT).show();
                        }

                    } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                        String path = intent.getData().toString()
                                .substring("file://".length());

                        Log.d(TAG, "Intent.ACTION_MEDIA_SCANNER_FINISHED:" + mCursor + ":" + path);
                        if (path != null && path.toString().contains("cdrom")) {
                            return;
                        }
                        if (mCurrentUriList.size() != 0) {

                            doScanId3Time();


                            if (mCurrentUriList.get(0).startsWith(path)) {
                                if (mCursor == null) {
                                    doScan(mFileToPlay);
                                    if (mCursor != null) {
                                        notifyChange(META_CHANGED);
                                    }
                                }
                            }
                        }
                    }
                }
            };

            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            if (mScanMusic) {
                iFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
            }
            iFilter.addDataScheme("file");

            mContext.registerReceiver(mMountReceiver, iFilter);
        }
        if (mScanMusic) {
            registerListener();
        }
    }

    private BroadcastReceiver mReceiver = null;

    public void unregisterListener() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }

    }

    public void registerListener() {
        if (mReceiver == null) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    Log.d("allen", "onReceive:" + action);
                    if (action.equals(MyCmd.BROADCAST_CMD_TO_MUSIC)) {
                        int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, 0);
                        if (cmd == MyCmd.Cmd.MUSIC_REQUEST_PLAY_STATUS) {
                            notifyChange(PLAYSTATE_CHANGED);
                        }
                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(MyCmd.BROADCAST_CMD_TO_MUSIC);

            mContext.registerReceiver(mReceiver, iFilter);
        }
    }

    public static interface IMediaCallBack {
        public void callback(int what, int status, Object obj);
    }

    ;

    private IMediaCallBack mIMediaCallBack = null;

    public void setIMediaCallBack(IMediaCallBack cb) {
        mIMediaCallBack = cb;
    }

    // for other

    public boolean playSearchFile(String s) {
        int i = -1;
        int size = 0;
        if (mCurrentUriList != null && s != null) {
            size = mCurrentUriList.size();

            for (i = 0; i < mCurrentUriList.size(); ++i) {
                if (s.equals(mCurrentUriList.get(i))) {
                    break;
                }
            }
        }

        if (!(i >= 0 && i < size)) {
            mCurrentUriList.add(s);
            i = (mCurrentUriList.size() - 1);
        }

        play(i);
        return true;
    }

    private long getSongAlbumID(String path) {
        ContentResolver resolver = mContext.getContentResolver();
        Uri uri;
        String where;
        String selectionArgs[];
        Cursor c;
        if (path.startsWith("content://media/")) {
            uri = Uri.parse(path);
            where = null;
            selectionArgs = null;
        } else {
            // uri = MediaStore.Audio.Media.getContentUriForPath(path);
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;// test
            where = MediaStore.Audio.Media.DATA + "=?";
            selectionArgs = new String[]{path};
        }

        try {
            c = resolver.query(uri, mCursorColsAlbum, where, selectionArgs,
                    null);

            if (c == null || c.isClosed()) {
                return 0;
            }
            if (c.getCount() == 0) {
                c.close();
                c = null;
            } else {
                c.moveToNext();

            }

            return c.getLong(c
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

        } catch (Exception e) {
            return 0;
        }
    }


    private String getSongDuration(String path) {
        ContentResolver resolver = mContext.getContentResolver();
        Uri uri;
        String where;
        String selectionArgs[];
        Cursor c;
        if (path.startsWith("content://media/")) {
            uri = Uri.parse(path);
            where = null;
            selectionArgs = null;
        } else {
            // uri = MediaStore.Audio.Media.getContentUriForPath(path);
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;// test
            where = MediaStore.Audio.Media.DATA + "=?";
            selectionArgs = new String[]{path};
        }

        try {
            c = resolver.query(uri, mCursorColsTime, where, selectionArgs,
                    null);

            if (c == null || c.isClosed()) {
                return null;
            }
            if (c.getCount() == 0) {
                c.close();
                c = null;
            } else {
                c.moveToNext();

            }
            return c.getString(c
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
        } catch (Exception e) {
            return "";
        }
    }

    // music cursor
    private Cursor mCursor;

    public String getArtistName() {
        synchronized (this) {

            try {
                if (mCursor == null || mCursor.isClosed()) {
                    return null;
                }
                return mCursor.getString(mCursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            } catch (Exception e) {
                return "";
            }
        }
    }

    public long getArtistId() {
        synchronized (this) {

            try {
                if (mCursor == null || mCursor.isClosed()) {
                    return -1;
                }
                return mCursor
                        .getLong(mCursor
                                .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
            } catch (Exception e) {
                return 0;
            }
        }
    }

    public String getAlbumName() {
        synchronized (this) {
            try {
                if (mCursor == null || mCursor.isClosed()) {
                    return null;
                }
                return mCursor.getString(mCursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));

            } catch (Exception e) {
                return "";
            }
        }
    }

    public long getAlbumId() {
        synchronized (this) {
            try {
                if (mCursor == null || mCursor.isClosed()) {
                    return -1;
                }
                return mCursor
                        .getLong(mCursor
                                .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

            } catch (Exception e) {
                return 0;
            }
        }
    }

    public String getTrackName() {
        synchronized (this) {
            String name = "";
            try {
                if (mCursor != null && !mCursor.isClosed()) {
                    // return null;

                    name = mCursor
                            .getString(mCursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                }

                if (name == null || name.length() == 0) {
                    if (this.mCurrentPlayPos >= 0
                            && this.mCurrentPlayPos < mCurrentUriList.size()) {
                        name = mCurrentUriList.get(mCurrentPlayPos);
                        name = cutPathPrex(name);
                    }

                }

            } catch (Exception e) {
                if (this.mCurrentPlayPos >= 0
                        && this.mCurrentPlayPos < mCurrentUriList.size()) {
                    name = mCurrentUriList.get(mCurrentPlayPos);
                    name = cutPathPrex(name);
                }
            }
            return name;
        }
    }

    public String getPath() {
        return mFileToPlay;
    }

    public void setVisualizerStyle(int index, int v) {
        saveData(SAVE_DATA_VISUALIZER_STYLE + index, v);
    }

    public int getVisualizerStyle(int index) {
        return getData(SAVE_DATA_VISUALIZER_STYLE + index);
    }

    private Bitmap mArtWork;
    private long mAlbumID = -1;

    public Bitmap getArtwork() {
        long album_id = getAlbumId();

        if (album_id < 0) {
            mArtWork = null;
            return mArtWork;
        }

        if (album_id == mAlbumID) {
            return mArtWork;
        }

        mAlbumID = album_id;
        mArtWork = getArtworkEx(album_id);

        return mArtWork;
    }

    public Bitmap getArtwork(int pos) {
        Bitmap bmpWork = null;
        if (mCurrentUriList.size() > 0) {
            String path = mCurrentUriList.get(pos);
            long album_id = getSongAlbumID(path);


            if (album_id < 0) {
                return bmpWork;
            }

            bmpWork = getArtworkEx(album_id);
        }


        return bmpWork;
    }

    public Bitmap getArtworkEx(long album_id) {

        // long album_id = getAlbumId();

//		mAlbumID = album_id;
        ContentResolver res = mContext.getContentResolver();
        Uri uri = ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"), album_id);
        if (uri != null) {
            InputStream in = null;
            BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
            try {
                in = res.openInputStream(uri);
                sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                sBitmapOptions.inDither = false;
//				Bitmap b = BitmapFactory.decodeStream(in, null, sBitmapOptions);
//				if (b != null && b.getByteCount() > 500 * 1024) {
//					Log.d(TAG, "getArtwork size too big:" + mArtWork.getByteCount());
//					b = null;
//				}
//				return b;
                return BitmapFactory.decodeStream(in, null, sBitmapOptions);
            }
            // catch (FileNotFoundException ex) {
            // // The album art thumbnail does not actually exist. Maybe the
            // // user deleted it, or
            // // maybe it never existed to begin with.
            // return getArtworkFromFile();
            // }
            catch (Exception ex) {
                return null;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                }
            }
        }
        // return getArtworkFromFile();

        return null;
    }

    public Bitmap getArtworkFromFile() {
        // String TrackPath = getPath();
        // Bitmap bm = null;
        // if (TrackPath == null) {
        // return null;
        // }
        //
        // File f = new File(TrackPath.replaceAll(".{3}$", "bmp"));
        // if (!f.exists()) {
        // // try jpg
        // f = new File(TrackPath.replaceAll(".{3}$", "jpg"));
        // if (!f.exists()) {
        // // try png
        // f = new File(TrackPath.replaceAll(".{3}$", "png"));
        // }
        // }
        //
        // if (f.exists()) {
        // ParcelFileDescriptor pfd = null;
        // try {
        // pfd = ParcelFileDescriptor.open(f,
        // ParcelFileDescriptor.MODE_READ_ONLY);
        // bm = BitmapFactory
        // .decodeFileDescriptor(pfd.getFileDescriptor());
        // pfd.close();
        // } catch (FileNotFoundException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
        // return bm;
        return null;
    }

    // for some special video files for yixiang by allen.

    private ArrayList<String> mErrFile = new ArrayList<String>(); // just

    private int mErrFileNum = 0;

    private void getErrFiles() {
        SharedPreferences sharedata = mContext.getSharedPreferences(
                mSaveDataPath, 0);

        mErrFileNum = getData(SAVE_DATA_ERR_FILES_NUM);
        if (mErrFileNum > 0) {
            mErrFile.clear();
            for (int i = 0; i < mErrFileNum; ++i) {
                String s = sharedata.getString(SAVE_DATA_ERR_FILES + i, null);
                if (s != null) {
                    mErrFile.add(s);
                }
            }
            int num = mErrFile.size();
            if (num != mErrFileNum) {
                mErrFileNum = num;
                saveData(SAVE_DATA_ERR_FILES_NUM, mErrFileNum);
            }
        }

    }

    public void updateErrFiles(int index) {
        if (index >= 0 && index < mCurrentUriList.size()) {
            saveDataEx(mSaveDataPath, SAVE_DATA_ERR_FILES + mErrFileNum,
                    mCurrentUriList.get(index));

            mErrFile.add(mCurrentUriList.get(index));

            ++mErrFileNum;

            saveData(SAVE_DATA_ERR_FILES_NUM, mErrFileNum);
            // if (mToastFail != null) {
            // mToastFail.cancel();
            // mToastFail = null;
            // }
            // mToastFail = Toast.makeText(mContext, mCurrentUriList.get(index)
            // + "  " + mContext.getString(R.string.play_fail),
            // Toast.LENGTH_SHORT);
            // mToastFail.show();

        } else {
            if (mErrFileNum > 0) {
                mErrFileNum = 0;
                mErrFile.clear();
                saveData(SAVE_DATA_ERR_FILES_NUM, mErrFileNum);
            }
        }
    }

    private boolean checkIfErrFiles(String s) {
        if (mErrFileNum > 0 && s != null) {
            for (String errFile : mErrFile) {
                if (s.equals(errFile)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void seekTo(int pos) {
        super.seekTo(pos);
        notifyChange(MEDIA_SEEK, 1);
    }

    // fils search

    // ///////////////////////////////////////////////
    private int mCurrentPage = -1;
    private int mCurrentPlayPos = -1;
    private int mCurrentTotal = -1;

    // private ContentResolver mContentResolver = null;
    public static String MEDIA_USB_PATH = "/storage/usbdisk1/";
    public static String MEDIA_USB2_PATH = "/storage/usbdisk2/";
    public static String MEDIA_USB3_PATH = "/storage/usbdisk3/";
    public static String MEDIA_USB4_PATH = "/storage/usbdisk4/";
    public static String MEDIA_SD_PATH = "/storage/sdcard1/";
    public static String MEDIA_SD2_PATH = "/storage/sdcard2/";
    public final static String MEDIA_LOCAL_PATH = Environment
            .getExternalStorageDirectory().getAbsolutePath();

    private ArrayList<String> mCurrentUriList = new ArrayList<String>(); // just
    // for
    // play

    private ArrayList<String> mCurrentPreparePlay = new ArrayList<String>();
    private ArrayList<String> mCurrentShow = new ArrayList<String>(); // just
    // for
    // show

    private ArrayList<String> mLocalUriList = new ArrayList<String>();
    private ArrayList<String> mLocalFolderUriList = new ArrayList<String>();
    private ArrayList<String> mLocalStrList = new ArrayList<String>();

    private ArrayList<String> mUsbUriList = new ArrayList<String>();
    private ArrayList<String> mUSBFolderUriList = new ArrayList<String>();
    private ArrayList<String> mUsbStrList = new ArrayList<String>();

    private ArrayList<String> mUsb2UriList = new ArrayList<String>();
    private ArrayList<String> mUSB2FolderUriList = new ArrayList<String>();
    private ArrayList<String> mUsb2StrList = new ArrayList<String>();

    private ArrayList<String> mUsb3UriList = new ArrayList<String>();
    private ArrayList<String> mUSB3FolderUriList = new ArrayList<String>();
    private ArrayList<String> mUsb3StrList = new ArrayList<String>();
    private ArrayList<String> mUsb4UriList = new ArrayList<String>();
    private ArrayList<String> mUSB4FolderUriList = new ArrayList<String>();
    private ArrayList<String> mUsb4StrList = new ArrayList<String>();

    private ArrayList<String> mSDUriList = new ArrayList<String>();
    private ArrayList<String> mSDFolderUriList = new ArrayList<String>();
    private ArrayList<String> mSDStrList = new ArrayList<String>();

    private ArrayList<String> mSD2UriList = new ArrayList<String>();
    private ArrayList<String> mSD2FolderUriList = new ArrayList<String>();
    private ArrayList<String> mSD2StrList = new ArrayList<String>();

    private ArrayList<String> mSearchingUriList = null;
    private ArrayList<String> mSearchingFolerList = null;
    private String mSearchingPath = null;

    private void cleanList() {
        mSD2StrList.clear();
        mSDStrList.clear();
        mUsbStrList.clear();
        mUsb2StrList.clear();
        mUsb3StrList.clear();
        mLocalStrList.clear();

        mSDUriList.clear();
        mSD2UriList.clear();
        mUsbUriList.clear();
        mUsb2UriList.clear();
        mUsb3UriList.clear();
        mSDUriList.clear();
        mLocalUriList.clear();

        mSD2FolderUriList.clear();
        mSDFolderUriList.clear();
        mUSBFolderUriList.clear();
        mUSB2FolderUriList.clear();
        mUSB3FolderUriList.clear();
        mLocalFolderUriList.clear();

        mCurrentPlayPos = -1;
        mCurrentTotal = -1;

    }

    private String getPathPrex(String str) {
        String s = str;
        if (s != null) {
            int start = str.lastIndexOf('/');
            if (start != -1) {
                s = str.substring(start + 1);
            }
        }
        return s;
    }

    private String cutPathPrex(String str) {
        String s = str;
        if (s != null) {
            int start = str.lastIndexOf('/');
            if (start != -1) {
                s = str.substring(start + 1);
            }
        }
        return s;
    }

    private String cutPathSuffix(String str) {
        String s = str;
        if (s != null) {
            int start = str.lastIndexOf('.');
            if (start != -1) {
                s = str.substring(0, start);
            }
        }
        return s;
    }

    private boolean isCurFolderFile(String folder, String file) {
        boolean ret = false;
        if (file != null && folder != null && file.startsWith(folder)) {
            String s = file.substring(folder.length() + 1);
            int start = s.indexOf('/');
            if (start == -1) {
                ret = true;
            }
        }
        return ret;
    }

    private void getShowList(ArrayList<String> listFile,
                             ArrayList<String> listFoler, int folderIndex, String root) {
        mCurrentShow.clear();
        mCurrentPreparePlay.clear();

        try {
            if (mSearchingPath != null) {
                if (listFile != null && listFile.size() > 0
                        && listFile.get(0) != null
                        && listFile.get(0).startsWith(mSearchingPath)) {
                    return;
                }
            }

            if (folderIndex == -2) { // all files

                for (String s : listFile) {

                    mCurrentShow.add(cutPathSuffix(cutPathPrex(s)));
                    mCurrentPreparePlay.add(s);

                }

            } else if (folderIndex == -1) {
                for (String s : listFoler) {
                    mCurrentShow.add(cutPathPrex(s));
                }

                for (String s : listFile) {
                    if (isCurFolderFile(root, s)) {
                        mCurrentShow.add(cutPathSuffix(cutPathPrex(s)));
                        mCurrentPreparePlay.add(s);
                    }
                }
            } else {
                String folder = null;
                if (listFoler != null && listFoler.size() != 0
                        && folderIndex < listFoler.size()) {
                    folder = listFoler.get(folderIndex);
                    mCurrentShow.add(cutPathPrex(folder));
                }

                for (String s : listFile) {
                    if (isCurFolderFile(folder, s)) {
                        mCurrentShow.add(cutPathSuffix(cutPathPrex(s)));
                        mCurrentPreparePlay.add(s);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getShowList err:" + e);
            mCurrentShow.clear();
            mCurrentPreparePlay.clear();
        }
    }

    private void getShowFoderList(ArrayList<String> listFile,
                                  ArrayList<String> listFoler, String root) {
        mCurrentShow.clear();
        mCurrentPreparePlay.clear();

        // try{
        // if (folderIndex == -2) { // all files
        //
        // for (String s : listFile) {
        //
        // mCurrentShow.add(cutPathSuffix(cutPathPrex(s)));
        // mCurrentPreparePlay.add(s);
        //
        // }
        //
        // } else if (folderIndex == -1) {
        // for (String s : listFoler) {
        // mCurrentShow.add(cutPathPrex(s));
        // }
        //
        // for (String s : listFile) {
        // if (isCurFolderFile(root, s)) {
        // mCurrentShow.add(cutPathSuffix(cutPathPrex(s)));
        // mCurrentPreparePlay.add(s);
        // }
        // }
        // }
        //
        // else {
        // String folder = null;
        // if (listFoler != null && listFoler.size() != 0
        // && folderIndex < listFoler.size()) {
        // folder = listFoler.get(folderIndex);
        // mCurrentShow.add(cutPathPrex(folder));
        // }
        //
        // for (String s : listFile) {
        // if (isCurFolderFile(folder, s)) {
        // mCurrentShow.add(cutPathSuffix(cutPathPrex(s)));
        // mCurrentPreparePlay.add(s);
        // }
        // }
        // }
        // }catch(Exception e){
        // Log.d(TAG, "getShowList err:"+e);
        // }
    }

    private void getShowAllFoderList() {
        mCurrentShow.clear();
        mCurrentPreparePlay.clear();

    }

    private void setFolderList(String data, ArrayList<String> list) {
        String s;
        int j;
        int start;

        start = data.lastIndexOf('/');
        if (start != -1) {
            s = data.substring(0, start);
            for (j = 0; j < list.size(); ++j) { // find if exit
                if (list.get(j).equals(s)) {
                    break;
                }
            }

            if (j == list.size()) {
                Log.e("add foler:", s + ":" + list.size());
                list.add(s);
            }
        }
    }

    private boolean isExitFolder(String data) {
        int start = data.lastIndexOf('/');
        // return (start == -1) ? false : true;
        return (start > 0) ? true : false;
    }

    private void searchFile(File filepath, int deep) {
        try {
//			Util.doSleep(10);//test
            File[] files = filepath.listFiles();
//			Log.d("allen", filepath.getPath() + ":" + deep);
            --deep;
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isDirectory()) {

                        if (file.canRead()) {
                            if (deep > 0) {
                                searchFile(file, deep);
                            }
                        }
                    } else {
                        if (isMediaFile(file)) { // add to list
                            if (mSearchingUriList != null) {
                                mSearchingUriList.add(file.getPath());
                            }

                            if (mSearchingFolerList != null) {
                                if (isExitFolder(file.getPath().substring(
                                        mSearchingPath.length()))) {
                                    setFolderList(file.getPath(),
                                            mSearchingFolerList);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, ":Exception:fils:" + e);
        }
    }

    private int searchFilePrepare(ArrayList<String> uri,
                                  ArrayList<String> folder, String path) {
        if (path == null || path.length() < 1) {
            return 0;
        }
        // long l = System.currentTimeMillis();
        mSearchingUriList = uri;
        mSearchingFolerList = folder;
        mSearchingPath = path;

        mSearchingUriList.clear();
        mSearchingFolerList.clear();
        File fsearch = new File(mSearchingPath);

        mHandler.removeMessages(MSG_HIDE_BUSY_DIALOG);
        mHandler.removeMessages(MSG_SHOW_BUSY_DIALOG);
        mHandler.sendEmptyMessageDelayed(MSG_SHOW_BUSY_DIALOG,
                TIME_SHOW_BUSY_DIALOG);


        searchFile(fsearch, 4);
        mSearchingPath = null;
        Log.d(TAG, path + "searchFilePrepare:" + mSearchingUriList.size() + ":" + mCurrentUriList.size());

        mHandler.removeMessages(MSG_HIDE_BUSY_DIALOG);
        mHandler.removeMessages(MSG_SHOW_BUSY_DIALOG);
        mHandler.sendEmptyMessage(MSG_HIDE_BUSY_DIALOG);
        return mSearchingUriList.size();

    }

    public static final int QUERY_FLAG_SD = 0x1;
    public static final int QUERY_FLAG_USB = 0x2;
    public static final int QUERY_FLAG_LOCAL = 0x4;
    public static final int QUERY_FLAG_SD2 = 0x8;

    public static final int QUERY_FLAG_USB2 = 0x10;

    public static final int QUERY_FLAG_USB3 = 0x20;
    public static final int QUERY_FLAG_USB4 = 0x40;

    private static final int QUERY_FLAG_ALL = QUERY_FLAG_SD | QUERY_FLAG_USB
            | QUERY_FLAG_LOCAL | QUERY_FLAG_SD2 | QUERY_FLAG_USB2
            | QUERY_FLAG_USB3 | QUERY_FLAG_USB4;
    public static int mQeryFlag = QUERY_FLAG_ALL;

    protected String mSaveDataPath = "audio_data";
    public static String SAVE_DATA_PAGE = "audio_page";
    private static String SAVE_DATA_CUR_POS = "audio_cur_pos";
    private static String SAVE_DATA_CUR_FOLDER = "audio_cur_folder";

    // ////////////////////
    public static final int LIST_PLAYING = 1;
    public static final int LIST_SD = 2;
    public static final int LIST_USB = 3;
    public static final int LIST_LOCAL = 4;
    public static final int LIST_SD2 = 5;

    public static final int LIST_USB2 = 6;
    public static final int LIST_USB3 = 7;
    public static final int LIST_USB4 = 8;

    public static final int LIST_ALL_FOLDER = 9;

    private int getIdFromPath(String data) {
        int id = 0;
        if (data != null) {
            if (MEDIA_USB_PATH.startsWith(data)) {
                id = LIST_USB;
            } else if (MEDIA_SD_PATH.startsWith(data)) {
                id = LIST_SD;
            } else if (MEDIA_SD2_PATH.startsWith(data)) {
                id = LIST_SD2;
            } else if (MEDIA_USB2_PATH.startsWith(data)) {
                id = LIST_USB2;
            } else if (MEDIA_USB3_PATH.startsWith(data)) {
                id = LIST_USB3;
            } else if (MEDIA_USB4_PATH.startsWith(data)) {
                id = LIST_USB4;
            } else {
                // id = LIST_LOCAL;
            }
        }
        return id;
    }

    private ArrayList<String> mAsyncSearchingUriList = null;
    private ArrayList<String> mAsyncSearchingFolerList = null;
    private String mAsyncSearchingPath = null;

    private int searchFilePrepareEx(ArrayList<String> uri,
                                    ArrayList<String> folder, String path) {

        if (path == null || path.length() < 1) {
            return 0;
        }

        mAsyncSearchingUriList = uri;
        mAsyncSearchingFolerList = folder;
        mAsyncSearchingPath = path;

        if (mSearchFileAsyncTask == null) {
            mAsyncSearchingUriList.clear();
            mAsyncSearchingFolerList.clear();
            mSearchFileAsyncTask = new SearchFileAsyncTask();
            mSearchFileAsyncTask.execute();


            return 0;
        } else {
            return -1;
        }
    }

    public void clearQueryFlag(int clear) {
        mQeryFlag |= clear;
    }

    public ArrayList<String> getStrList(int id, int folderIndex) {
        Log.d(TAG, "mQeryFlag:" + Integer.toHexString(mQeryFlag) + ":getStrList id:" + id + ":current size:"
                + mCurrentUriList.size());

        switch (id) {
            case LIST_PLAYING: {
                mCurrentShow.clear();
                for (String s : mCurrentUriList) {
                    mCurrentShow.add(cutPathSuffix(cutPathPrex(s)));
                }
            }
            return mCurrentShow;
            case LIST_SD:
                if ((mQeryFlag & QUERY_FLAG_SD) != 0) {
                    if (-1 != searchFilePrepareEx(mSDUriList, mSDFolderUriList, MEDIA_SD_PATH)) {
                        mQeryFlag &= ~QUERY_FLAG_SD;
                    }
                }
                getShowList(mSDUriList, mSDFolderUriList, folderIndex,
                        MEDIA_SD_PATH);
                break;
            case LIST_SD2:
                if ((mQeryFlag & QUERY_FLAG_SD2) != 0) {
                    if (-1 != searchFilePrepareEx(mSD2UriList, mSD2FolderUriList,
                            MEDIA_SD2_PATH)) {
                        mQeryFlag &= ~QUERY_FLAG_SD2;
                    }
                }
                getShowList(mSD2UriList, mSD2FolderUriList, folderIndex,
                        MEDIA_SD2_PATH);
                break;
            case LIST_USB:
                if ((mQeryFlag & QUERY_FLAG_USB) != 0) {
                    if (-1 != searchFilePrepareEx(mUsbUriList, mUSBFolderUriList,
                            MEDIA_USB_PATH)) {
                        mQeryFlag &= ~QUERY_FLAG_USB;
                    }
                }
                getShowList(mUsbUriList, mUSBFolderUriList, folderIndex,
                        MEDIA_USB_PATH);
                break;
            case LIST_USB2:
                if ((mQeryFlag & QUERY_FLAG_USB2) != 0) {
                    if (-1 != searchFilePrepareEx(mUsb2UriList, mUSB2FolderUriList,
                            MEDIA_USB2_PATH)) {
                        mQeryFlag &= ~QUERY_FLAG_USB2;
                    }
                }
                getShowList(mUsb2UriList, mUSB2FolderUriList, folderIndex,
                        MEDIA_USB2_PATH);
                break;
            case LIST_USB3:
                if ((mQeryFlag & QUERY_FLAG_USB3) != 0) {
                    if (-1 != searchFilePrepareEx(mUsb3UriList, mUSB3FolderUriList,
                            MEDIA_USB3_PATH)) {
                        mQeryFlag &= ~QUERY_FLAG_USB3;
                    }
                }
                getShowList(mUsb3UriList, mUSB3FolderUriList, folderIndex,
                        MEDIA_USB3_PATH);
                break;
            case LIST_USB4:
                if ((mQeryFlag & QUERY_FLAG_USB4) != 0) {
                    if (-1 != searchFilePrepareEx(mUsb4UriList, mUSB4FolderUriList,
                            MEDIA_USB4_PATH)) {
                        mQeryFlag &= ~QUERY_FLAG_USB4;
                    }
                }
                getShowList(mUsb4UriList, mUSB4FolderUriList, folderIndex,
                        MEDIA_USB4_PATH);

                Log.d(TAG, "1:" + mUsb4UriList.size());
                break;
            case LIST_LOCAL:
                if ((mQeryFlag & QUERY_FLAG_LOCAL) != 0) {
                    if (-1 != searchFilePrepareEx(mLocalUriList, mLocalFolderUriList,
                            MEDIA_LOCAL_PATH)) {
                        mQeryFlag &= ~QUERY_FLAG_LOCAL;
                    }
                }
                getShowList(mLocalUriList, mLocalFolderUriList, folderIndex,
                        MEDIA_LOCAL_PATH);
                break;
            case LIST_ALL_FOLDER:
                getShowAllFoderList();
                break;

        }
        mPreparePlayFolder = folderIndex;
        mPreparePlayPage = id;
        return mCurrentShow;
    }

    public ArrayList<String> getStrListEx(int id, int folderIndex) {
        Log.d(TAG, "mQeryFlag:" + Integer.toHexString(mQeryFlag) + ":getStrList id:" + id + ":current size:"
                + mCurrentUriList.size());
        switch (id) {
            case LIST_PLAYING: {
                mCurrentShow.clear();
                for (String s : mCurrentUriList) {
                    mCurrentShow.add(cutPathSuffix(cutPathPrex(s)));
                }
            }
            return mCurrentShow;
            case LIST_SD:
                if ((mQeryFlag & QUERY_FLAG_SD) != 0) {
                    searchFilePrepare(mSDUriList, mSDFolderUriList, MEDIA_SD_PATH);
                    mQeryFlag &= ~QUERY_FLAG_SD;
                }
                getShowList(mSDUriList, mSDFolderUriList, folderIndex,
                        MEDIA_SD_PATH);
                break;
            case LIST_SD2:
                if ((mQeryFlag & QUERY_FLAG_SD2) != 0) {
                    searchFilePrepare(mSD2UriList, mSD2FolderUriList,
                            MEDIA_SD2_PATH);
                    mQeryFlag &= ~QUERY_FLAG_SD2;
                }
                getShowList(mSD2UriList, mSD2FolderUriList, folderIndex,
                        MEDIA_SD2_PATH);
                break;
            case LIST_USB:
                if ((mQeryFlag & QUERY_FLAG_USB) != 0) {
                    searchFilePrepare(mUsbUriList, mUSBFolderUriList,
                            MEDIA_USB_PATH);
                    mQeryFlag &= ~QUERY_FLAG_USB;
                }
                getShowList(mUsbUriList, mUSBFolderUriList, folderIndex,
                        MEDIA_USB_PATH);
                break;
            case LIST_USB2:
                if ((mQeryFlag & QUERY_FLAG_USB2) != 0) {
                    searchFilePrepare(mUsb2UriList, mUSB2FolderUriList,
                            MEDIA_USB2_PATH);
                    mQeryFlag &= ~QUERY_FLAG_USB2;
                }
                getShowList(mUsb2UriList, mUSB2FolderUriList, folderIndex,
                        MEDIA_USB2_PATH);
                break;
            case LIST_USB3:
                if ((mQeryFlag & QUERY_FLAG_USB3) != 0) {
                    searchFilePrepare(mUsb3UriList, mUSB3FolderUriList,
                            MEDIA_USB3_PATH);
                    mQeryFlag &= ~QUERY_FLAG_USB3;
                }
                getShowList(mUsb3UriList, mUSB3FolderUriList, folderIndex,
                        MEDIA_USB3_PATH);
                break;
            case LIST_USB4:
                if ((mQeryFlag & QUERY_FLAG_USB4) != 0) {
                    searchFilePrepare(mUsb4UriList, mUSB4FolderUriList,
                            MEDIA_USB4_PATH);
                    mQeryFlag &= ~QUERY_FLAG_USB4;
                }
                getShowList(mUsb4UriList, mUSB4FolderUriList, folderIndex,
                        MEDIA_USB4_PATH);
                break;
            case LIST_LOCAL:
                if ((mQeryFlag & QUERY_FLAG_LOCAL) != 0) {
                    searchFilePrepare(mLocalUriList, mLocalFolderUriList,
                            MEDIA_LOCAL_PATH);
                    mQeryFlag &= ~QUERY_FLAG_LOCAL;
                }
                getShowList(mLocalUriList, mLocalFolderUriList, folderIndex,
                        MEDIA_LOCAL_PATH);
                break;
            case LIST_ALL_FOLDER:
                getShowAllFoderList();
                break;

        }
        mPreparePlayFolder = folderIndex;
        mPreparePlayPage = id;
        return mCurrentShow;
    }

    private int mPreparePlayFolder = 0;
    private int mPreparePlayPage = 0;

    public int getStrFolderNum(int id) {
        int i = 0;
        switch (id) {
            case LIST_PLAYING:
                break;
            case LIST_SD:
                return mSDFolderUriList.size();
            case LIST_SD2:
                return mSD2FolderUriList.size();
            case LIST_USB:
                return mUSBFolderUriList.size();
            case LIST_USB2:
                return mUSB2FolderUriList.size();
            case LIST_USB3:
                return mUSB3FolderUriList.size();

            case LIST_USB4:
                return mUSB4FolderUriList.size();
            case LIST_LOCAL:
                return mLocalFolderUriList.size();
        }
        return i;
    }

    public int getCurPosition() {
        return mCurrentPlayPos;
    }

    public int getCurrentTotal() {
        return mCurrentTotal;
    }

    public int getCurPage() {
        return mCurrentPage;
    }

    public boolean mQuerying = true;

    public boolean getQuerying() {
        return mQuerying;
    }

    private boolean mFirstBoot = true;

    public boolean checkPlayingListIsExist() {
        if (mFirstBoot) {
            mFirstBoot = false;
            int page = getData(SAVE_DATA_PAGE);
            int pos = getData(SAVE_DATA_CUR_POS);
            int folder = getData(SAVE_DATA_CUR_FOLDER);

            mRepeatMode = getData(SAVE_DATA_REPEAT);

            getStrListEx(page, folder);
            mQeryFlag = QUERY_FLAG_ALL;

            Log.d("allen", page + ":fffcheckPlayingListIsExistfff:" + pos + ":" + mCurrentShow.size());
            if (mCurrentShow.size() > 0) {
                mPreparePlayPage = page;
                mPreparePlayFolder = folder;
                updateCurrentUriList();
                if (pos >= mCurrentShow.size()) {
                    mCurrentPlayPos = 0;
                }
                mCurrentPlayPos = pos;
                return true;
            }

        }
        return false;
    }

    public boolean checkPlayingListIsExistFromSleep() {
        mFirstBoot = false;
        int page = getData(SAVE_DATA_PAGE);
        int pos = getData(SAVE_DATA_CUR_POS);
        int folder = getData(SAVE_DATA_CUR_FOLDER);

        mRepeatMode = getData(SAVE_DATA_REPEAT);

        getStrList(page, folder);

        mQeryFlag = QUERY_FLAG_ALL;

        Log.d(TAG, "checkPlayingListIsExistFromSleep:" + mCurrentShow.size()
                + ":" + mSaveForSleepPage + ":" + mSaveForSleepPos + ":" + mSaveForSleepFolder);

        if (mCurrentShow.size() > 0) {
            mPreparePlayPage = page;
            mPreparePlayFolder = folder;
            updateCurrentUriList();
            if (pos >= mCurrentShow.size()) {
                mCurrentPlayPos = 0;
            }
            mCurrentPlayPos = pos;
            return true;
        }

        return false;
    }

    // public static MediaPlaybackService getThis() {
    // return mThis;
    // }

    public boolean equalPreparePlaylist() {
        if (mCurrentPreparePlay.size() == 0)
            return false;
        return mCurrentPreparePlay.equals(mCurrentUriList);
    }

    public boolean equalCurrentPlaylist(ArrayList<String> list) {
        if (list.size() == 0)
            return false;
        return list.equals(mCurrentUriList);
    }

    public void updateCurrentUriList() {
        mCurrentUriList.clear();
        for (String s : mCurrentPreparePlay) {
            mCurrentUriList.add(s);
        }
        mCurrentTotal = mCurrentPreparePlay.size();
        mCurrentPlayPos = 0;

        mCurrentPage = mPreparePlayPage;
        saveData(SAVE_DATA_PAGE, mPreparePlayPage);
        saveData(SAVE_DATA_CUR_FOLDER, mPreparePlayFolder);
    }

    public boolean addAllFilePlay(int page) {
        ArrayList<String> list = null;
        switch (page) {
            case LIST_SD:
                list = mSDUriList;
                break;
            case LIST_SD2:
                list = mSD2UriList;
                break;
            case LIST_USB:
                list = mUsbUriList;
                break;
            case LIST_USB2:
                list = mUsb2UriList;
                break;
            case LIST_USB3:
                list = mUsb3UriList;
                break;
            case LIST_USB4:
                list = mUsb4UriList;
                break;
            case LIST_LOCAL:
                list = mLocalUriList;
                break;
        }

        if (list != null) {
            Log.d(TAG, list.size() + ":addAllFilePlay:");
        } else {
            Log.d(TAG, list + ":addAllFilePlay:");
        }

        if (list == null || list.size() <= 0) {
            return false;
        }


        mCurrentUriList.clear();
        for (String s : list) {
            mCurrentUriList.add(s);
        }

        mCurrentTotal = mCurrentUriList.size();
        mCurrentPlayPos = 0;

        mCurrentPage = page;
        saveData(SAVE_DATA_PAGE, mCurrentPage);
        saveData(SAVE_DATA_CUR_FOLDER, -2);

        return true;
    }

    private int checkMediaChangeStatus(String path) {
        int id = 0;
        int mediaNum = 0;
        if (path != null) {
            if (MEDIA_SD_PATH.startsWith(path)) {
                mediaNum = searchFilePrepare(mSDUriList, mSDFolderUriList,
                        MEDIA_SD_PATH);
                if (mSDUriList.size() > 0) {
                    // Intent i = new Intent(STORAGE_MOUNTED);
                    // i.putExtra("id", getIdFromPath(path));
                    // mContext.sendBroadcast(i);
                    id = getIdFromPath(path);
                }

            }
            if (MEDIA_SD2_PATH.startsWith(path)) {
                mediaNum = searchFilePrepare(mSD2UriList, mSD2FolderUriList,
                        MEDIA_SD2_PATH);
                if (mSD2UriList.size() > 0) {

                    id = getIdFromPath(path);
                    // notifyChange(STORAGE_MOUNTED, getIdFromPath(path));
                }

            } else if (MEDIA_USB_PATH.startsWith((path))) {
                mediaNum = searchFilePrepare(mUsbUriList, mUSBFolderUriList,
                        MEDIA_USB_PATH);
                if (mUsbUriList.size() > 0) {

                    id = getIdFromPath(path);
                    // notifyChange(STORAGE_MOUNTED, getIdFromPath(path));
                }
            } else if (MEDIA_USB2_PATH.startsWith((path))) {
                mediaNum = searchFilePrepare(mUsb2UriList, mUSB2FolderUriList,
                        MEDIA_USB2_PATH);
                if (mUsb2UriList.size() > 0) {

                    id = getIdFromPath(path);
                    // notifyChange(STORAGE_MOUNTED, getIdFromPath(path));
                }
            } else if (MEDIA_USB3_PATH.startsWith((path))) {
                mediaNum = searchFilePrepare(mUsb3UriList, mUSB3FolderUriList,
                        MEDIA_USB3_PATH);
                if (mUsb3UriList.size() > 0) {

                    id = getIdFromPath(path);
                    // notifyChange(STORAGE_MOUNTED, getIdFromPath(path));
                }
            } else if (MEDIA_USB4_PATH.startsWith((path))) {
                mediaNum = searchFilePrepare(mUsb4UriList, mUSB4FolderUriList,
                        MEDIA_USB4_PATH);
                if (mUsb4UriList.size() > 0) {

                    id = getIdFromPath(path);
                    // notifyChange(STORAGE_MOUNTED, getIdFromPath(path));
                }
            } else if (MEDIA_LOCAL_PATH.startsWith(path)) {
                mediaNum = searchFilePrepare(mLocalUriList,
                        mLocalFolderUriList, MEDIA_LOCAL_PATH);
                if (mLocalUriList.size() > 0) {

                    id = getIdFromPath(path);
                    // notifyChange(STORAGE_MOUNTED, getIdFromPath(path));
                }
            }
        }
        if (mediaNum > 0) {
            restorePlayForSleepRemount(path, id);
            notifyChange(STORAGE_MOUNTED, id);
        } else {
            notifyChange(STORAGE_MOUNTED_NOMEDIA, id);
        }
        return mQeryFlag;
    }

    private int cleanMediaChangeStatus(String path) {
        if (path != null) {
            if (MEDIA_SD_PATH.startsWith(path)) {
                mSDUriList.clear();
                mSDFolderUriList.clear();
            } else if (MEDIA_USB_PATH.startsWith(path)) {
                mUsbUriList.clear();
                mUSBFolderUriList.clear();
            } else if (MEDIA_SD2_PATH.startsWith(path)) {
                mSD2UriList.clear();
                mSD2FolderUriList.clear();
            }

        }
        return mQeryFlag;
    }

    public void clearQueryFlag() {
        mQeryFlag = QUERY_FLAG_ALL;
    }

    public void closeExternalStorageFiles(String storagePath) {
        // stop playback and clean up if the SD card is going to be unmounted.
//		stop();

        if (isInitialized()) {
//			releasePlay();
            try {
                stop();
                reset();
                setDataSource("");
                prepare();
                start();
            } catch (Exception e) {
                Log.d(TAG, "releasePlay:" + e); // must to here
            }
            mIsInitialized = false;
        }

        notifyChange(META_CHANGED);
    }

    //


    SearchFileAsyncTask mSearchFileAsyncTask;

    class SearchFileAsyncTask extends AsyncTask<Void, Integer, Integer> {

        SearchFileAsyncTask() {
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(Void... params) {


            searchFilePrepare(mAsyncSearchingUriList, mAsyncSearchingFolerList, mAsyncSearchingPath);

            mSearchFileAsyncTask = null;


            notifyChange(SEARCH_LIST_FILE_UPDATE);
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }

    UpdateAsyncTask mUpdateAsyncTask;

    class UpdateAsyncTask extends AsyncTask<Void, Integer, Integer> {

        UpdateAsyncTask() {
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(Void... params) {
            String mount;

            // for (int i = 0; i < 3; ++i) {
            // if (mMountPath == null) {
            // break;
            // }
            mount = mMountPath;
            // mMountPath = null;

            checkMediaChangeStatus(mount);

            // }

            mUpdateAsyncTask = null;
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }

    public ArrayList<String> getId3Time() {

        Log.d("abd", "getId3Time22222");
        if (mCurrentUriList.size() > 0 && !mCurrentUriList.get(0).equals(mScanTimePath)) {
            doScanId3Time();
            return null;
        }
        return mCurrentId3Time;
    }

    private void doScanId3Time() {
        if (MachineConfig.VALUE_SYSTEM_UI45_8702_2.equals(GlobalDef
                .getSystemUI())) {

            Log.d("abd", "doScanId3Time");
            if (mUpdateTimeAsyncTask != null) {
                mUpdateTimeAsyncTask.cancel(true);
            }
            mUpdateTimeAsyncTask = new UpdateTimeAsyncTask();
            mUpdateTimeAsyncTask.execute();
        }
    }

    private static String mScanTimePath;
    private ArrayList<String> mCurrentId3Time;// = new ArrayList<Integer>();
    UpdateTimeAsyncTask mUpdateTimeAsyncTask;

    class UpdateTimeAsyncTask extends AsyncTask<Void, Integer, Integer> {

        UpdateTimeAsyncTask() {
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(Void... params) {

            if (mCurrentId3Time == null) {
                mCurrentId3Time = new ArrayList<String>();
            }

            if (mCurrentUriList.size() > 0) {
                mScanTimePath = mCurrentUriList.get(0);
                mCurrentId3Time.clear();
                for (int i = 0; i < mCurrentUriList.size(); ++i) {
                    String time = getSongDuration(mCurrentUriList.get(i));
                    try {
                        int timeMs = Integer.valueOf(time);
                        time = MusicUI.stringForTime(timeMs);
                    } catch (Exception e) {

                    }

                    mCurrentId3Time.add(time);
                }
            } else {
                mCurrentId3Time.clear();
            }
//			Log.d("abd", "UpdateTimeAsyncTask finish");
            notifyChange(ID3_TIME_CHANGE);
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }
    // private AudioManager mAudioManager;
    // private boolean mPausedByTransientLossOfFocus = false;
    //
    // void requestAudioFocus() {
    //
    // Log.e("aa", TAG + "requestAudioFocus" + mAudioFocusListener);
    // mAudioManager.requestAudioFocus(mAudioFocusListener,
    // AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    // }
    //
    // void abandonAudioFocus() {
    //
    // Log.e("aa", TAG + "abandonAudioFocus" + mAudioFocusListener);
    // mAudioManager.abandonAudioFocus(mAudioFocusListener);
    // }
    //
    // private OnAudioFocusChangeListener mAudioFocusListener = new
    // OnAudioFocusChangeListener() {
    // public void onAudioFocusChange(int focusChange) {
    // // AudioFocus is a new feature: focus updates are made verbose on
    // // purpose
    //
    // Log.e("aa", TAG + ":" + focusChange);
    // switch (focusChange) {
    // case AudioManager.AUDIOFOCUS_LOSS:
    // Log.v(TAG, "AudioFocus: received AUDIOFOCUS_LOSS");
    // if (isPlaying()) {
    // mPausedByTransientLossOfFocus = false;
    // pause();
    // }
    // break;
    // case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
    // case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
    // Log.v(TAG, "AudioFocus: received AUDIOFOCUS_LOSS_TRANSIENT");
    // if (isPlaying()) {
    // mPausedByTransientLossOfFocus = true;
    // pause();
    // }
    // break;
    // case AudioManager.AUDIOFOCUS_GAIN:
    // Log.v(TAG, "AudioFocus: received AUDIOFOCUS_GAIN");
    // if (!isPlaying() && mPausedByTransientLossOfFocus) {
    // mPausedByTransientLossOfFocus = false;
    // // startAndFadeIn();
    // }
    // break;
    // default:
    // Log.e(TAG, "Unknown audio focus change code");
    // }
    // }
    // };

    // for 7.0

    public static class StorageInfo {
        public String mPath;
        public String mState;
        public int mType;

        public final static int TYPE_INTERAL = 0;
        public final static int TYPE_SD = 1;
        public final static int TYPE_USB = 2;

        public StorageInfo(String path, int type) {
            mPath = path;
            mType = type;
        }

        // public boolean isMounted() {
        // return "mounted".equals(state);
        // }

    }

    private static List<StorageInfo> listAllStorage(Context context) {
        ArrayList<StorageInfo> storages = new ArrayList<StorageInfo>();
        StorageManager storageManager = (StorageManager) context
                .getSystemService(Context.STORAGE_SERVICE);
        try {
            Class<?>[] paramClasses = {};
            Method getVolumeList = StorageManager.class.getMethod("getVolumes",
                    paramClasses);
            Object[] params = {};
            List<Object> VolumeInfo = (List<Object>) getVolumeList.invoke(
                    storageManager, params);

            if (VolumeInfo != null) {
                for (Object volumeinfo : VolumeInfo) {

                    Method getPath = volumeinfo.getClass().getMethod("getPath",
                            new Class[0]);

                    File path = (File) getPath
                            .invoke(volumeinfo, new Object[0]);

                    Method getDisk = volumeinfo.getClass().getMethod("getDisk",
                            new Class[0]);

                    Object diskinfo = getDisk.invoke(volumeinfo, new Object[0]);
                    int type = StorageInfo.TYPE_INTERAL;
                    if (diskinfo != null) {
                        Log.e("aa", ":" + (path == null ? "path is null" : path.toString()));
                        if (path == null || !path.toString().contains("cdrom")) {
                            Method isSd = diskinfo.getClass().getMethod("isSd",
                                    new Class[0]);

                            type = ((Boolean) isSd.invoke(diskinfo,
                                    new Object[0])) ? StorageInfo.TYPE_SD
                                    : StorageInfo.TYPE_USB;

                            if (path != null) {
                                StorageInfo si = new StorageInfo(
                                        path.toString(), type);
                                storages.add(si);
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        storages.trimToSize();
        return storages;
    }

    // public static String MEDIA_USB_PATH = "/storage/usbdisk1/";
    // public static String MEDIA_USB2_PATH = "/storage/usbdisk2/";
    // public static String MEDIA_USB3_PATH = "/storage/usbdisk3/";
    // public static String MEDIA_USB4_PATH = "/storage/usbdisk4/";
    // public static String MEDIA_SD_PATH = "/storage/sdcard1/";
    // public static String MEDIA_SD2_PATH = "/storage/sdcard2/";
    private void clearAllPath() {
        MEDIA_SD_PATH = "";
        MEDIA_SD2_PATH = "";
        MEDIA_USB_PATH = "";
        MEDIA_USB2_PATH = "";
        MEDIA_USB3_PATH = "";
        MEDIA_USB4_PATH = "";
    }

    private void initDiskPath() {
        if (Build.VERSION.SDK_INT >= 25) {

            MEDIA_USB_PATH = "";
            MEDIA_USB2_PATH = "";
            MEDIA_USB3_PATH = "";
            MEDIA_USB4_PATH = "";
            MEDIA_SD_PATH = "";
            MEDIA_SD2_PATH = "";

            List<StorageInfo> list = listAllStorage(mContext);

            int sd = 0;
            int usb = 2;
            clearAllPath();
            for (int i = 0; i < list.size(); ++i) {

                StorageInfo si = list.get(i);
                Log.d("dd", i + ":" + si.mPath + ":" + si.mType);
                if (si.mType == StorageInfo.TYPE_SD) {
                    ++sd;
                    switch (sd) {
                        case 1:
                            MEDIA_SD_PATH = si.mPath;
                            break;
                        case 2:
                            MEDIA_SD2_PATH = si.mPath;
                            break;
                    }
                } else if (si.mType == StorageInfo.TYPE_USB) {
                    // usb++;
                    if (si.mPath.endsWith("1")) {
                        MEDIA_USB_PATH = si.mPath;
                    } else if (si.mPath.endsWith("2")) {
                        MEDIA_USB2_PATH = si.mPath;
                    } else if (si.mPath.endsWith("3")) {
                        MEDIA_USB3_PATH = si.mPath;
                    } else if (si.mPath.endsWith("4")) {
                        MEDIA_USB4_PATH = si.mPath;
                    }

                    // switch (usb) {
                    // case 3:
                    // MEDIA_USB_PATH = si.mPath;
                    // break;
                    // case 4:
                    // MEDIA_USB2_PATH = si.mPath;
                    // break;
                    // case 5:
                    // MEDIA_USB3_PATH = si.mPath;
                    // break;
                    // case 6:
                    // MEDIA_USB4_PATH = si.mPath;
                    // break;
                    // }
                }
                // if(!si.path.equals(MEDIA_LOCAL_PATH)){
                // switch(i){
                // case 1:
                // MEDIA_SD_PATH = si.path;
                // break;
                // case 2:
                // MEDIA_SD2_PATH = si.path;
                // break;
                // case 3:
                // MEDIA_USB_PATH = si.path;
                // break;
                // case 4:
                // MEDIA_USB2_PATH = si.path;
                // break;
                // case 5:
                // MEDIA_USB3_PATH = si.path;
                // break;
                // case 6:
                // MEDIA_USB4_PATH = si.path;
                // break;
                // }
                // }

            }
        }
    }

    /*
        public void playSaveDeviceFile() {
            if (mCurrentUriList.size() > 0) {
                String path = mCurrentUriList.get(0);
                if (mListStorage != null) {
                    for (int i = 0; i < mListStorage.size(); ++i) {
                        StorageInfo si = mListStorage.get(i);
                        if (path.startsWith(si.mPath)) {
                            String uuid = si.mState;
                            if (uuid == null || uuid.length() == 0) {
                                uuid = "unknow";
                            }

                            SharedPreferences sharedata = mContext
                                    .getSharedPreferences(mSaveDataPath, 0);
                            path = sharedata.getString(uuid, null);
                            break;
                        }
                    }
                }
                int index = 0;
                if (path != null) {
                    for (int i = 0; i < mCurrentUriList.size(); ++i) {
                        if (mCurrentUriList.get(i).equals(path)){
                            index = i;
                            break;
                        }
                    }

                }
                play(index);
            }

        }

        public void saveDevicePlayingFilePath(String path) {
            Log.d("allen", "saveDevicePlayingFilePath:"+path);
            if (mListStorage != null) {
                for (int i = 0; i < mListStorage.size(); ++i) {
                    StorageInfo si = mListStorage.get(i);
                    if (path.startsWith(si.mPath)) {
                        String uuid = si.mState;
                        if (uuid == null || uuid.length() == 0) {
                            uuid = "unknow";
                        }
                        saveDataEx(mSaveDataPath, uuid, path);
                        break;
                    }
                }
            }

        }
    */
    public Bitmap getArtworkLimit() {
        Bitmap b = getArtwork();
        if (b != null && b.getByteCount() > 500 * 1024) {
            Log.d(TAG, "getArtworkLimit size too big:" + mArtWork.getByteCount());
            b = null;
        }
        return b;
    }

    public int addExternalFiles(String file) {
        // check if exist
        int ret = 0;
        for (int i = 0; i < mCurrentUriList.size(); ++i) {
            if (file.equals(mCurrentUriList.get(i))) {
                ret = i;
                break;
            }
        }

        if (ret == 0) {
            mCurrentUriList.add(file);
            getStrListEx(MusicPlayer.LIST_PLAYING, -1);
            ret = mCurrentUriList.size() - 1;
            ret |= 0x40000000;
        }

        return ret;
    }
}
