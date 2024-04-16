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

package com.octopus.android.carapps.video;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.adapter.MyListViewAdapter;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.player.ComMediaPlayer;
import com.octopus.android.carapps.common.player.MoviePlayer;
import com.octopus.android.carapps.common.player.MusicPlayer;
import com.octopus.android.carapps.common.ui.UIBase;
import com.octopus.android.carapps.common.utils.ParkBrake;
import com.octopus.android.carapps.common.utils.ResourceUtil;
import com.octopus.android.carapps.common.view.MyScrollView;

import java.io.File;
import java.util.ArrayList;

/**
 * This activity plays a video from a specified URI.o
 */
public class VideoUI extends UIBase implements OnGestureListener, OnDoubleTapListener, View.OnClickListener {

    private final static String TAG = "VideoUI";

    private final static boolean VIDEO_PLAY_BACKGROUND_SCREEN0_ONLY = false;

    private boolean mIs3PageSroll = false;
    public static final int SOURCE = MyCmd.SOURCE_VIDEO;

    private static VideoUI[] mUI = new VideoUI[MAX_DISPLAY];

    public static VideoUI getInstanse(Context context, View view, int index) {
        if (index >= MAX_DISPLAY) {
            return null;
        }

        mUI[index] = new VideoUI(context, view, index);

        return mUI[index];
    }

    public static VideoUI getInstanse(int index) {
        if (index >= MAX_DISPLAY) {
            return null;
        }
        return mUI[index];
    }

    public VideoUI(Context context, View view, int index) {
        super(context, view, index);

        mSource = SOURCE;
    }

    private static final int[] BUTTON_ON_CLICK = new int[]{
            R.id.prev, R.id.pp, R.id.next, R.id.repeat, R.id.btn_all, R.id.btn_local, R.id.btn_sd, R.id.btn_usb, R.id.btn_usb2, R.id.btn_usb3, R.id.btn_usb4, R.id.btn_usb4, R.id.full, R.id.btn_sd2,
            R.id.btn_add_all, R.id.repeat2, R.id.eq_mode_switch, R.id.list, R.id.full2, R.id.stop, R.id.eq
    };// ,R.id.mic,R.id.vol };

    private static final int[] VIEW_HIDE1 = new int[]{
            R.id.progress_bar, R.id.list_content, R.id.rightButtonLayout
    };

    // private static final int[] VIEW_HIDE2 = new int[] {
    // R.id.bottom_button_layout, R.id.list_content,
    // R.id.rightButtonLayout, R.id.progress_bar, R.id.home_back,
    // R.id.common_status_bar_main };

    private static final int[][] VIEW_HIDE = new int[][]{
            //{ R.id.common_status_bar_main, R.id.layout_visulizerView },
            {R.id.common_status_bar_main,}, {R.id.eq}
    };

    private static final int[][] VIEW_HIDE2 = new int[][]{
            {R.id.surface_view, R.id.list_content, R.id.rightButtonLayout}, {
            R.id.bottom_button_layout, R.id.list_content, R.id.rightButtonLayout, R.id.progress_bar, R.id.home_back, R.id.common_status_bar_main
    },
            //{ R.id.layout_visulizerView }
    };

    private static final int[][] VIEW_SHOW2 = new int[][]{
            //{ R.id.layout_visulizerView },
            {0}, {R.id.list_content, R.id.rightButtonLayout, R.id.surface_view}
    };

    private int mScreen1Type = -1;
    private final static int TYPE_NORMAL = 0;
    private final static int TYPE_VIDEO_ONLY = 1;
    private final static int TYPE_FULL_FUNCTION = 2;

    public void updateFullUI() {
        if (mDisplayIndex == SCREEN1) {

            int type = TYPE_NORMAL;
            if (mUI[0] != null) {
                if (mUI[0].mIsFullScrean && !mUI[0].mPause) {
                    type = TYPE_VIDEO_ONLY;
                } else if (mUI[0].mPause) {
                    type = TYPE_FULL_FUNCTION;
                }
            } else {
                type = TYPE_FULL_FUNCTION;
            }

            if (type != TYPE_NORMAL) {
                initMovieView();
            }

            Log.d(TAG, mScreen1Type + ":" + type);
            if (type != mScreen1Type) {

                mScreen1Type = type;
                // if (type == TYPE_VIDEO_ONLY
                // || type == TYPE_FULL_FUNCTION) {
                // initMovieView();
                // }
                for (int i = 0; i < VIEW_HIDE2[type].length; ++i) {
                    View v = mMainView.findViewById(VIEW_HIDE2[type][i]);
                    if (v != null) {
                        v.setVisibility(View.GONE);
                    }
                }

                for (int i = 0; i < VIEW_SHOW2[type].length; ++i) {
                    View v = mMainView.findViewById(VIEW_SHOW2[type][i]);
                    if (v != null) {
                        v.setVisibility(View.VISIBLE);
                    }

                }

                if (mScreen1InitStatus == SCREEN1_STATUS_FULLSCREEN || mIsFullScrean || type != TYPE_FULL_FUNCTION) {
                    mScreen1InitStatus = 0;
                    changeToFullScreen();
                }

            }

            // for (int i = 0; i < VIEW_HIDE1.length; ++i) {
            // View v = mMainView.findViewById(VIEW_HIDE2[i]);
            // if (v != null) {
            // v.setVisibility((type == TYPE_VIDEO_ONLY) ? View.VISIBLE
            // : View.GONE);
            // }
            // }
            // if (type != TYPE_FULL_FUNCTION || mIsFullScrean) {
            // for (int i = 0; i < VIEW_HIDE2.length; ++i) {
            // View v = mMainView.findViewById(VIEW_HIDE2[i]);
            // if (v != null) {
            // v.setVisibility(View.GONE);
            // }
            // }
            // }

            // mScreen1Type = type;
        } else {
            // if (mUI[1] != null && !mUI[1].mPause) {
            // mUI[1].updateFullUI();
            // }
            initMovieView();
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

    private static final int TIME_HIDE_MENU = 4000;

    private static final int MSG_HIDE_MENU = 1;
    private static final int MSG_UPDATE_SEEK_BAR = 2;

    private static final int MSG_AUTO_FULLSCREEN = 5;
    private static final int MSG_FIRST_RUN = 3;
    private static final int MSG_STORAGE_UNMOUNT = 10;

    private static final int MSG_SET_PROCESS = 6;
    private static final int MSG_SET_RUN_BEFORE_LAUNCHER = 11;
    private static final int MSG_SHOW_SCREEN1_VIDEO = 12;
    private static final int MSG_CHECK_BRAKE = 13;

    private static final int MSG_CHECK_NO_FILE = 14;
    private static final int MSG_HIDE_SCAN_DIALOG = 100;

    private static final int MSG_UPDATE_PLAY_STATUS = 15;

    private static final int TIME_AUTO_FULLSCREEN = 6000;

    private static final int TIME_CHECK_BRAKE = 1000;

    private View mBrakeCarView;
    private LinearLayout mHomeBackKey;

    private LinearLayout mProgressBar;

    public boolean mIsFullScrean = false;
    private int defaultWidth = 0;
    private int defaultHeight = 0;

    private long mHomeBackHideTime;
    private int mBrake = -1;

    private SeekBar mProgress;
    private TextView mCurrentTime;
    private TextView mEndTime;

    private boolean mIsShowVideo;

    private MoviePlayer mMediaPlayer;

    private View mVideoHide = null;

    private MoviceView mMoviceView = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mCreate = true;
        mMediaPlayer = VideoService.getMediaPlayer();

        mMediaPlayer.setOpeningFile(mMainView.findViewById(R.id.progressBar1));

        mGestureDetector = new GestureDetector(this);
        mGestureDetector.setOnDoubleTapListener(this);

        mHomeBackKey = (LinearLayout) mMainView.findViewById(R.id.home_back);

        mProgressBar = (LinearLayout) mMainView.findViewById(R.id.progress_bar);

        mMoviceView = (MoviceView) mMainView.findViewById(R.id.surface_view);
        mVideoHide = mMainView.findViewById(R.id.video_hide);

        mBrakeCarView = mMainView.findViewById(R.id.brake_warning_text);
        GlobalDef.updateBrakeCarText((TextView) mBrakeCarView, MachineConfig.VALUE_SYSTEM_UI_KLD7_1992, R.string.warning_driving_1992);

        initPresentationUI();

        // if (mIsShowVideo) {
        // initMovieView();
        // }

        mProgress = (SeekBar) mMainView.findViewById(R.id.progress2);
        if (mProgress != null) {
            mProgress.setOnSeekBarChangeListener(mSeekListener);
            mProgress.setMax(1000);
            mProgress.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent me) {
                    if (mMyScrollView != null) {
                        int action = me.getActionMasked();
                        if (action == MotionEvent.ACTION_UP) {
                            mMyScrollView.requestDisallowInterceptTouchEvent(false);
                        } else {
                            mMyScrollView.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                    return false;
                }
            });
        }

        mEndTime = (TextView) mMainView.findViewById(R.id.totaltime2);
        mCurrentTime = (TextView) mMainView.findViewById(R.id.currenttime2);

        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_SEEK_BAR, 0, 0), 1000);
        // mHandler.sendMessageDelayed(
        // mHandler.obtainMessage(MSG_FIRST_RUN, 0, 0), 200);

        View v = mMainView.findViewById(R.id.music_page_scroll_view);
        if (v != null) {
            mIs3PageSroll = true;
            mMyScrollView = (MyScrollView) v;
            mMyScrollView.setCallback(mICallBack);
            mMyScrollView.setPageType(1);
        }

        initFocusChange();
    }

    private void initMovieView() {

        int index = (mDisplayIndex + 1) % MAX_DISPLAY;

        if (mUI[index] != null) {
            // mUI[index].deInitMovieView();

            mUI[index].mVideoHide.setVisibility(View.VISIBLE);
        }
        // mMediaPlayer.setOnSeekCompleteListener(this);
        // mMediaPlayer.setOnCompletionListener(this);

        //		if (mMainView.findViewById(R.id.full2) != null) {
        mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        //		}
        initFullFixButton();
        mMoviceView.setGestureDetector(mGestureDetector);

        mMoviceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mMoviceView.getHolder().addCallback(mSHCallback);
        mMoviceView.setVisibility(View.VISIBLE);

        if (null != mMediaPlayer && !mSurfaceDestroyed) {
            boolean play = false;
            if (mMediaPlayer.isPlaying()) {
                play = true;
                // mMediaPlayer.pause();
            }
            mMediaPlayer.setDisplay(mMoviceView.getHolder());
            if (play) {
                // mMediaPlayer.start();
            }
        }

        defaultWidth = mContext.getResources().getInteger(R.integer.video_def_width);
        defaultHeight = mContext.getResources().getInteger(R.integer.video_def_height);
        //		View v = mMoviceView;
        //		while(true){
        //			ViewGroup.LayoutParams lp  = v.getLayoutParams();
        //			if (lp.width >0 && lp.height>0){
        //				defaultWidth = lp.width;
        //				defaultHeight = lp.height;
        //				break;
        //			} else {
        //				ViewParent p = v.getParent();
        //				if (p == null){
        //					break;
        //				} else {
        //					v = (View)p;
        //				}
        //			}
        //
        //		}


        if (mScreen1Type == TYPE_VIDEO_ONLY) {
            hideHomeBackKey();
        }

        mVideoHide.setVisibility(View.GONE);
    }

    protected void deInitMovieView() {
        // if (mMoviceView!=null){
        // mMediaPlayer.setOnSeekCompleteListener(this);
        // mMediaPlayer.setOnCompletionListener(this);
        //
        // mMoviceView.getHolder().addCallback(null);

        // mMoviceView.setVisibility(View.GONE);
        // }

    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {

        // if (mDisplayIndex == SCREEN1 && mUI[SCREEN0] != null
        // && !mUI[SCREEN0].mPause) {
        // return true;
        // }
        if (!mIsFullScrean) {
            // changeToFullScreen();

            scrollToPlayingPage();
        } else {
            changeToSmallScreen();
        }
        return true;

    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {

        return true;
    }

    private void setFullScreen() {
        if (this.mDisplayIndex == SCREEN0) {
            ((Activity) mContext).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void quitFullScreen() {

        if (this.mDisplayIndex == SCREEN0) {
            final WindowManager.LayoutParams attrs = ((Activity) mContext).getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            ((Activity) mContext).getWindow().setAttributes(attrs);
            ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private boolean doDoubleClickVideo() {
        if (mDisplayIndex == SCREEN1 && mUI[SCREEN0] != null && !mUI[SCREEN0].mPause) {
            return true;
        }
        if (!mIsFullScrean) {
            changeToFullScreen();
        } else {
            changeToSmallScreen();
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return doDoubleClickVideo();
    }

    public static ProgressDialog mDialogScan;
    public static int mStatus = 0;

    private int showDialogScan() {
        if (mDialogScan == null) {
            initDialogScan();
        }
        if (mDialogScan != null) {
            mDialogScan.show();

        }
        return mStatus;
    }

    private void hideDialogScan() {
        if (mDialogScan != null) {
            mDialogScan.dismiss();
        }
    }

    private void initDialogScan() {
        // mDialogScan = new ProgressDialog(this);
        // mDialogScan.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // mDialogScan.setMessage(getString(R.string.scanning));
        // mDialogScan.setIndeterminate(true);
        // mDialogScan.setCancelable(true);
        // mDialogScan.setCanceledOnTouchOutside(true);
        //
        // mDialogScan.setOnCancelListener(new OnCancelListener() {
        // public void onCancel(DialogInterface dialog) {
        // hideDialogScan();
        // }
        // });
    }

    public void prepareFullScreen() {

        //		 Log.d(TAG, "prepareFullScreen!!" + this);

        // if (mDisplayIndex == SCREEN1) {
        mHandler.removeMessages(MSG_AUTO_FULLSCREEN);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_AUTO_FULLSCREEN, 0, 0), TIME_AUTO_FULLSCREEN);
        // }
    }

    private void autoToFullScreen() {
        // if (mDisplayIndex == SCREEN0) {
        // if (!mPause) {
        // if (!mIsFullScrean){
        // changeToFullScreen();
        // }
        // }
        // } else {
        //
        // }
        // Log.d("allen", "autoToFullScreen!!" + this);
        if (mBrake == 1) {
            changeToFullScreen();
        }
    }

    private void changeToFullScreen() {

        if (ResourceUtil.isMultiWindow()) {
            return;
        }

        if (mMediaPlayer == null || !mMediaPlayer.isInitialized()) {
            return;
        }

        if (mMyScrollView != null) {
            if (mMyScrollView.getTouchDown() || mMyScrollView.getCurPage() != 1) {
                prepareFullScreen();
                return;
            }
        }

        mIsFullScrean = true;

        if (mDisplayIndex == SCREEN0) {
            // PresentationUI.updateVideoShow(true);

            // if (GlobalDef.getScreenNum(mContext) > 1) {
            // GlobalDef.notifyUIScreen0Change(SOURCE, 1);
            //
            // // GlobalDef.reactiveSource(mContext, SOURCE,
            // VideoService.mAudioFocusListener);
            //
            // if (mUI[1] != null) {
            // mUI[1].updateFullUI();
            // }
            // } else {
            changeToFullScreenEx();
            // }
        } else {

            changeToFullScreenEx();

        }
    }

    private void clearFocus() {
        if (mContext instanceof Activity) {
            View v = ((Activity) mContext).getCurrentFocus();
            if (v != null) {
                mMoviceView.setFocusable(true);
                mMoviceView.requestFocus();
                mMoviceView.requestFocusFromTouch();
            }
        }
    }

    private void changeToFullScreenEx() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {

            setFullScreen();

            if (mMainView.findViewById(R.id.list_content) != null) {
                mMainView.findViewById(R.id.list_content).setVisibility(View.GONE);
            }

            if (mMainView.findViewById(R.id.list_content2) != null) {
                mMainView.findViewById(R.id.list_content2).setVisibility(View.GONE);
            }
            if (mMainView.findViewById(R.id.fake_black_cover_video) != null) {
                mMainView.findViewById(R.id.fake_black_cover_video).setVisibility(View.GONE);
            }

            if (mMainView.findViewById(R.id.rightButtonLayout) != null) {
                mMainView.findViewById(R.id.rightButtonLayout).setVisibility(View.GONE);
            }

            // mMoviceView.getHolder().setFixedSize(1024, 480);

            hideHomeBackKey();

            onChangeVideoSize();
            clearFocus();
        }
    }

    public void changeToSmallScreen() {

        mIsFullScrean = false;

        if (mDisplayIndex == SCREEN0) {
            // PresentationUI.updateVideoShow(false);

            // GlobalDef.reactiveSource(mContext, SOURCE,
            // VideoService.mAudioFocusListener);
            // if (GlobalDef.getScreenNum(mContext) > 1) {
            //
            // GlobalDef.notifyUIScreen0Change(SOURCE, 1);
            //
            // updateFullUI();
            // } else {
            mMoviceView.setFocusable(false);
            changeToSmallScreenEx();
            // }
            // if (mUI[0]!=null){
            // mUI[0].updateFullUI();
            // }
        } else {
            if (mScreen1Type == TYPE_FULL_FUNCTION) {
                changeToSmallScreenEx();
            } else {
                if (mUI[0] != null) {
                    mUI[0].changeToSmallScreen();
                }
            }
        }

        prepareFullScreen();
    }

    private void changeToSmallScreenEx() {
        quitFullScreen();

        mMoviceView.getHolder().setFixedSize(defaultWidth, defaultHeight);

        if (mMainView.findViewById(R.id.list_content) != null) {
            mMainView.findViewById(R.id.list_content).setVisibility(View.VISIBLE);
        }

        if (mMainView.findViewById(R.id.list_content2) != null) {
            mMainView.findViewById(R.id.list_content2).setVisibility(View.VISIBLE);
        }

        if (mMainView.findViewById(R.id.fake_black_cover_video) != null) {
            mMainView.findViewById(R.id.fake_black_cover_video).setVisibility(View.VISIBLE);
        }
        if (mMainView.findViewById(R.id.rightButtonLayout) != null) {
            mMainView.findViewById(R.id.rightButtonLayout).setVisibility(View.VISIBLE);
        }

        showHomeBackKey();
        onChangeVideoSize();
    }

    private void brakeCarShowText(boolean brake) {
        if (mBrakeCarView != null) {
            if (brake) {
                mBrakeCarView.setVisibility(View.GONE);
                prepareFullScreen();
            } else {
                mBrakeCarView.setVisibility(View.VISIBLE);
            }
        }
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

    View.OnScrollChangeListener mOnScrollChangeListener = new View.OnScrollChangeListener() {
        public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            prepareFullScreen();
        }
    };

    OnScrollListener mOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            prepareFullScreen();
        }
    };

    MyScrollView mMyScrollView;
    private int mPreStoragePage = -1;

    private int mPreScrollViewPage = -1;
    MyScrollView.ICallBack mICallBack = new MyScrollView.ICallBack() {
        public void callback(int cmd, int param) {

            Log.d("abcd", "dd:" + param);
            if (cmd == MyScrollView.CMD_NEWPAGE) {

                if (mIsFullScrean) {
                    if (param != 1) {
                        changeToSmallScreen();
                    }
                }

                if (param == 0) {
                    if (mPreStoragePage == -1) {
                        mPreStoragePage = getPlayingIdToPage();//R.id.btn_local;
                    }
                    if (mPreScrollViewPage == 1) {
                        mResPageId = -1;
                    }
                    doSetPage(mPreStoragePage);
                } else if (param == 2) {
                    //					doSetPage(R.id.btn_all);
                    mMainView.findViewById(R.id.tv_all_list).setVisibility(View.VISIBLE);

                    if (mContext instanceof Activity) {
                        View vf = ((Activity) mContext).getCurrentFocus();
                        if (vf != null) {
                            mMainView.findViewById(R.id.tv_all_list).requestFocus();
                        }
                    }

                }
                if (param == 1) {
                    mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
                }
            } else if (cmd == MyScrollView.CMD_TOUCH_EVENT) {
                switch (param) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_DOWN:
                        if (mIsFullScrean) {
                            changeToSmallScreen();
                        } else {
                            prepareFullScreen();
                        }
                        break;
                    // case MotionEvent.ACTION_UP:
                    // changeToSmallScreen();
                    // break;
                }
            } else if (cmd == MyScrollView.CMD_PAGE_END) {
                if (param == 0) {
                    if (mPreStoragePage == -1) {
                        mPreStoragePage = R.id.btn_local;
                    }
                    if (mPreScrollViewPage == 1) {
                        mResPageId = -1;
                    }
                    doSetPage(mPreStoragePage);
                } else if (param == 2) {
                    doSetPage(R.id.btn_all);
                    mMainView.findViewById(R.id.tv_all_list).setVisibility(View.VISIBLE);
                }
                if (param == 1) {
                    mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);


                    mMainView.findViewById(R.id.tv_local_list).setVisibility(View.GONE);
                    mMainView.findViewById(R.id.tv_sd_list).setVisibility(View.GONE);
                    mMainView.findViewById(R.id.tv_sd2_list).setVisibility(View.GONE);
                    mMainView.findViewById(R.id.tv_usb_list).setVisibility(View.GONE);
                    mMainView.findViewById(R.id.tv_usb2_list).setVisibility(View.GONE);
                    mMainView.findViewById(R.id.tv_usb3_list).setVisibility(View.GONE);
                    mMainView.findViewById(R.id.tv_usb4_list).setVisibility(View.GONE);
                }
                mPreScrollViewPage = param;
            }
        }

        ;
    };

    private void initListView(int id) {
        //
        MyListViewAdapter adapter = null;
        if (id == R.id.tv_all_list) {
            if (mPlayingTVList == null) {
                mPlayingTVList = (ListView) mMainView.findViewById(R.id.tv_all_list);
                mPlayingTVList.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> arg0, View view, int postion, long id) {
                        GlobalDef.reactiveSource(mContext, SOURCE, VideoService.mAudioFocusListener);
                        mMediaPlayer.play(postion);
                        prepareFullScreen();
                    }
                });

                mPlayingTVList.setOnScrollListener(mOnScrollListener);

            }
            adapter = (MyListViewAdapter) mPlayingTVList.getAdapter();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                adapter = null;
            }
            //			if (mCurrentList != null) {
            //				if ((mCurrentList.getAdapter() == null || !mMediaPlayer
            //						.equalCurrentPlaylist(((MyListViewAdapter) mCurrentList
            //								.getAdapter()).getList()))) {
            //					updateListViewAdapter(mPlayingTVList,
            //							mMediaPlayer.getStrList(MusicPlayer.LIST_PLAYING,
            //									-1), 0);
            //					mCurrentList = mPlayingTVList;
            //					updateView();
            //				}
            //			}

            boolean update = false;
            if (mCurrentList != null) {
                if ((mCurrentList.getAdapter() == null || !mMediaPlayer.equalCurrentPlaylist(((MyListViewAdapter) mCurrentList.getAdapter()).getList()))) {
                    //					updateListViewAdapter(mPlayingTVList,
                    //							mMediaPlayer.getStrList(MusicPlayer.LIST_PLAYING,
                    //									-1), 0);
                    //					mCurrentList = mPlayingTVList;
                    //					updateView();
                    update = true;
                }
            }

            if (mPlayingTVList != null && !update) {
                if (mPlayingTVList.getAdapter() != null) {

                    update = !mMediaPlayer.equalCurrentPlaylist(((MyListViewAdapter) mPlayingTVList.getAdapter()).getList());
                }
            }

            if (update) {
                updateListViewAdapter(mPlayingTVList, mMediaPlayer.getStrList(MusicPlayer.LIST_PLAYING, -1), 0);
                mCurrentList = mPlayingTVList;
                updateView();
            }
            if (mCurrentList != null) {
                adapter = (MyListViewAdapter) mCurrentList.getAdapter();
            }
        } else if (id == R.id.tv_local_list) {
            if (mLocalTVList == null) {
                ArrayList<String> list = mMediaPlayer.getStrList(MusicPlayer.LIST_LOCAL, -1);
                if (list.size() > 0) {
                    mLocalTVList = (ListView) mMainView.findViewById(R.id.tv_local_list);
                    updateListViewAdapter(mLocalTVList, list, mMediaPlayer.getStrFolderNum(MusicPlayer.LIST_LOCAL));
                    mLocalTVList.setOnItemClickListener(new OnItemClickListener() {
                        public void onItemClick(AdapterView<?> arg0, View view, int postion, long id) {
                            int folderNum = ((MyListViewAdapter) arg0.getAdapter()).getFolderNum();
                            if (postion < folderNum) {
                                clickFolder(mLocalTVList, MusicPlayer.LIST_LOCAL, postion, ((MyListViewAdapter) arg0.getAdapter()).getFolderLevel());
                            } else {
                                play(postion - folderNum);
                                scrollToPlayingPage();
                            }
                        }
                    });

                    mLocalTVList.setOnScrollListener(mOnScrollListener);
                }
            } else {
                mMediaPlayer.getStrList(MusicPlayer.LIST_LOCAL, ((MyListViewAdapter) mLocalTVList.getAdapter()).getFolderSet());
                ((MyListViewAdapter) mLocalTVList.getAdapter()).clearSelectItem();
                checkListHiglightItem((MyListViewAdapter) mLocalTVList.getAdapter());
            }
            if (mLocalTVList != null) {
                adapter = (MyListViewAdapter) mLocalTVList.getAdapter();
            }
        } else if (id == R.id.tv_usb_list) {
            if (mUsbTVList == null) {
                ArrayList<String> list = mMediaPlayer.getStrList(MusicPlayer.LIST_USB, -1);

                if (list.size() > 0) {
                    mUsbTVList = (ListView) mMainView.findViewById(R.id.tv_usb_list);
                    updateListViewAdapter(mUsbTVList, list, mMediaPlayer.getStrFolderNum(MusicPlayer.LIST_USB));

                    mUsbTVList.setOnItemClickListener(new OnItemClickListener() {
                        public void onItemClick(AdapterView<?> arg0, View view, int postion, long id) {
                            int folderNum = ((MyListViewAdapter) arg0.getAdapter()).getFolderNum();
                            if (postion < folderNum) {
                                clickFolder(mUsbTVList, MusicPlayer.LIST_USB, postion, ((MyListViewAdapter) arg0.getAdapter()).getFolderLevel());
                            } else {
                                play(postion - folderNum);
                                scrollToPlayingPage();
                            }
                        }
                    });

                    mUsbTVList.setOnScrollListener(mOnScrollListener);
                }
            } else {
                mMediaPlayer.getStrList(MusicPlayer.LIST_USB, ((MyListViewAdapter) mUsbTVList.getAdapter()).getFolderSet());
                ((MyListViewAdapter) mUsbTVList.getAdapter()).clearSelectItem();
                checkListHiglightItem((MyListViewAdapter) mUsbTVList.getAdapter());
            }
            if (mUsbTVList != null) {
                adapter = (MyListViewAdapter) mUsbTVList.getAdapter();
            }
        } else if (id == R.id.tv_usb2_list) {
            if (mUsb2TVList == null) {
                ArrayList<String> list = mMediaPlayer.getStrList(MusicPlayer.LIST_USB2, -1);

                if (list.size() > 0) {
                    mUsb2TVList = (ListView) mMainView.findViewById(R.id.tv_usb2_list);
                    updateListViewAdapter(mUsb2TVList, list, mMediaPlayer.getStrFolderNum(MusicPlayer.LIST_USB2));

                    mUsb2TVList.setOnItemClickListener(new OnItemClickListener() {
                        public void onItemClick(AdapterView<?> arg0, View view, int postion, long id) {
                            int folderNum = ((MyListViewAdapter) arg0.getAdapter()).getFolderNum();
                            if (postion < folderNum) {
                                clickFolder(mUsb2TVList, MusicPlayer.LIST_USB2, postion, ((MyListViewAdapter) arg0.getAdapter()).getFolderLevel());
                            } else {
                                play(postion - folderNum);
                                scrollToPlayingPage();
                            }
                        }
                    });

                    mUsb2TVList.setOnScrollListener(mOnScrollListener);
                }
            } else {
                mMediaPlayer.getStrList(MusicPlayer.LIST_USB2, ((MyListViewAdapter) mUsb2TVList.getAdapter()).getFolderSet());
                ((MyListViewAdapter) mUsb2TVList.getAdapter()).clearSelectItem();
                checkListHiglightItem((MyListViewAdapter) mUsb2TVList.getAdapter());
            }
            if (mUsb2TVList != null) {
                adapter = (MyListViewAdapter) mUsb2TVList.getAdapter();
            }
        } else if (id == R.id.tv_usb3_list) {
            if (mUsb3TVList == null) {
                ArrayList<String> list = mMediaPlayer.getStrList(MusicPlayer.LIST_USB3, -1);

                if (list.size() > 0) {
                    mUsb3TVList = (ListView) mMainView.findViewById(R.id.tv_usb3_list);
                    updateListViewAdapter(mUsb3TVList, list, mMediaPlayer.getStrFolderNum(MusicPlayer.LIST_USB3));

                    mUsb3TVList.setOnItemClickListener(new OnItemClickListener() {
                        public void onItemClick(AdapterView<?> arg0, View view, int postion, long id) {
                            int folderNum = ((MyListViewAdapter) arg0.getAdapter()).getFolderNum();
                            if (postion < folderNum) {
                                clickFolder(mUsb3TVList, MusicPlayer.LIST_USB3, postion, ((MyListViewAdapter) arg0.getAdapter()).getFolderLevel());
                            } else {
                                play(postion - folderNum);
                                scrollToPlayingPage();
                            }
                        }
                    });
                    mUsb3TVList.setOnScrollListener(mOnScrollListener);
                }
            } else {
                mMediaPlayer.getStrList(MusicPlayer.LIST_USB3, ((MyListViewAdapter) mUsb3TVList.getAdapter()).getFolderSet());
                ((MyListViewAdapter) mUsb3TVList.getAdapter()).clearSelectItem();
                checkListHiglightItem((MyListViewAdapter) mUsb3TVList.getAdapter());
            }
            if (mUsb3TVList != null) {
                adapter = (MyListViewAdapter) mUsb3TVList.getAdapter();
            }
        } else if (id == R.id.tv_usb4_list) {
            if (mUsb4TVList == null) {
                ArrayList<String> list = mMediaPlayer.getStrList(MusicPlayer.LIST_USB4, -1);

                if (list.size() > 0) {
                    mUsb4TVList = (ListView) mMainView.findViewById(R.id.tv_usb4_list);
                    updateListViewAdapter(mUsb4TVList, list, mMediaPlayer.getStrFolderNum(MusicPlayer.LIST_USB4));

                    mUsb4TVList.setOnItemClickListener(new OnItemClickListener() {
                        public void onItemClick(AdapterView<?> arg0, View view, int postion, long id) {
                            int folderNum = ((MyListViewAdapter) arg0.getAdapter()).getFolderNum();
                            if (postion < folderNum) {
                                clickFolder(mUsb4TVList, MusicPlayer.LIST_USB4, postion, ((MyListViewAdapter) arg0.getAdapter()).getFolderLevel());
                            } else {
                                play(postion - folderNum);
                                scrollToPlayingPage();
                            }
                        }
                    });
                    mUsb4TVList.setOnScrollListener(mOnScrollListener);
                }
            } else {
                mMediaPlayer.getStrList(MusicPlayer.LIST_USB4, ((MyListViewAdapter) mUsb4TVList.getAdapter()).getFolderSet());
                ((MyListViewAdapter) mUsb4TVList.getAdapter()).clearSelectItem();
                checkListHiglightItem((MyListViewAdapter) mUsb4TVList.getAdapter());
            }
            if (mUsb4TVList != null) {
                adapter = (MyListViewAdapter) mUsb4TVList.getAdapter();
            }
        } else if (id == R.id.tv_sd_list) {
            if (mSDTVList == null) {
                ArrayList<String> list = mMediaPlayer.getStrList(MusicPlayer.LIST_SD, -1);
                if (list.size() > 0) {
                    mSDTVList = (ListView) mMainView.findViewById(R.id.tv_sd_list);

                    updateListViewAdapter(mSDTVList, list, mMediaPlayer.getStrFolderNum(MusicPlayer.LIST_SD));

                    mSDTVList.setOnItemClickListener(new OnItemClickListener() {
                        public void onItemClick(AdapterView<?> arg0, View view, int postion, long id) {
                            int folderNum = ((MyListViewAdapter) arg0.getAdapter()).getFolderNum();
                            if (postion < folderNum) {
                                clickFolder(mSDTVList, MusicPlayer.LIST_SD, postion, ((MyListViewAdapter) arg0.getAdapter()).getFolderLevel());
                            } else {
                                play(postion - folderNum);
                                scrollToPlayingPage();
                            }
                        }
                    });
                    mSDTVList.setOnScrollListener(mOnScrollListener);
                }
            } else {
                mMediaPlayer.getStrList(MusicPlayer.LIST_SD, ((MyListViewAdapter) mSDTVList.getAdapter()).getFolderSet());
                ((MyListViewAdapter) mSDTVList.getAdapter()).clearSelectItem();
                checkListHiglightItem((MyListViewAdapter) mSDTVList.getAdapter());
            }
            if (mSDTVList != null) {
                adapter = (MyListViewAdapter) mSDTVList.getAdapter();
            }
        } else if (id == R.id.tv_sd2_list) {
            if (mSD2TVList == null) {
                ArrayList<String> list = mMediaPlayer.getStrList(MusicPlayer.LIST_SD2, -1);
                if (list.size() > 0) {
                    mSD2TVList = (ListView) mMainView.findViewById(R.id.tv_sd2_list);

                    updateListViewAdapter(mSD2TVList, list, mMediaPlayer.getStrFolderNum(MusicPlayer.LIST_SD2));

                    mSD2TVList.setOnItemClickListener(new OnItemClickListener() {
                        public void onItemClick(AdapterView<?> arg0, View view, int postion, long id) {
                            int folderNum = ((MyListViewAdapter) arg0.getAdapter()).getFolderNum();
                            if (postion < folderNum) {
                                clickFolder(mSD2TVList, MusicPlayer.LIST_SD2, postion, ((MyListViewAdapter) arg0.getAdapter()).getFolderLevel());
                            } else {
                                play(postion - folderNum);
                                scrollToPlayingPage();
                            }
                        }
                    });
                    mSD2TVList.setOnScrollListener(mOnScrollListener);
                }
            } else {
                mMediaPlayer.getStrList(MusicPlayer.LIST_SD2, ((MyListViewAdapter) mSD2TVList.getAdapter()).getFolderSet());
                ((MyListViewAdapter) mSD2TVList.getAdapter()).clearSelectItem();
                checkListHiglightItem((MyListViewAdapter) mSD2TVList.getAdapter());
            }
            if (mSD2TVList != null) {
                adapter = (MyListViewAdapter) mSD2TVList.getAdapter();
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void checkListHiglightItem(MyListViewAdapter la) {
        try {
            ArrayList<String> ls;

            String playing = ((MyListViewAdapter) mPlayingTVList.getAdapter()).getSelectName();

            int folderNum = la.getFolderNum();
            folderNum = folderNum + mMediaPlayer.getCurPosition();
            ls = la.getList();
            String s = ls.get(folderNum);
            if (s.equals(playing)) {
                la.setSelectItem(folderNum);
            }

        } catch (Exception e) {

        }
    }

    private void updateListViewAdapter(ListView lv, ArrayList<String> list, int folder) {
        if (lv != null) {
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

            if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef.getSystemUI())) {
                if (mPlayingTVList == lv) {
                    id = R.layout.tl_list_video_playing;
                } else {
                    id = R.layout.tl_list_video;
                }
            }
            lv.setAdapter(new MyListViewAdapter(mContext, id, list, folder));
        }
    }

    private void clickFolder(ListView lv, int id, int index, int level) {
        if (level == 1) {
            updateListViewAdapter(lv, mMediaPlayer.getStrList(id, index), -1);
            if (lv != null) {
                MyListViewAdapter adapter = (MyListViewAdapter) lv.getAdapter();
                if (adapter != null) adapter.setFolderSet(index);
            }
        } else {
            updateListViewAdapter(lv, mMediaPlayer.getStrList(id, -1), mMediaPlayer.getStrFolderNum(id));
        }
    }

    private void setProcess(SeekBar bar) {

        int progress = bar.getProgress();
        long duration = mMediaPlayer.getDuration();
        long newposition = (duration * progress) / 1000L;
        mMediaPlayer.seekTo((int) newposition);
        if (mCurrentTime != null) mCurrentTime.setText(stringForTime((int) newposition));

        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 1000);
    }

    private void prepareSetProcess() {
        mHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
        mHandler.removeMessages(MSG_SET_PROCESS);
        mHandler.sendEmptyMessageDelayed(MSG_SET_PROCESS, 20);
    }

    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser || mMediaPlayer == null || !mMediaPlayer.isPlaying()) {
                return;
            }
            prepareSetProcess();
            // long duration = mMediaPlayer.getDuration();
            // long newposition = (duration * progress) / 1000L;
            // mMediaPlayer.seekTo((int) newposition);
            // if (mCurrentTime != null)
            // mCurrentTime.setText(stringForTime((int) newposition));

            prepareFullScreen();
        }

        public void onStopTrackingTouch(SeekBar bar) {
        }
    };

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

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

    public boolean onDown(MotionEvent e) {
        return true;
    }

    public void onShowPress(MotionEvent e) {

    }

    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    public void onLongPress(MotionEvent e) {

    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        return true;
    }


    private void doMemoryPlay() {

        hideDialogScan();
        if (mMediaPlayer.isInitialized()) {
            if (!mPause) {
                mMediaPlayer.playCurrentMemory();
            }
        } else {
            // if (MoviePlayer.mFirstRun) {
            // MoviePlayer.mFirstRun = false;
            //Log.d("dddd", mMediaPlayer.mSleepInPlay+"!!!!!!!!!!!!!!!!!!!!!!!!!!"+GlobalDef.isOneSleepRemountTime());

            if (Util.isRKSystem() && mMediaPlayer.mSaveForSleepPath != null) {
                if (mFirstPowerOn == 1) {
                    mFirstPowerOn = 0;
                    return;
                } else if (mFirstPowerOn == 3) {
                    mFirstPowerOn = 0;
                    File f = new File(mMediaPlayer.mSaveForSleepPath);
                    //					Log.d("abd", "doMemoryPlay f.exists():"+f.exists());
                    if (!f.exists()) {
                        return;
                    }
                }
            }

            if (!mPause /*|| !mMediaPlayer.mSleepInPlay)*/) {
                mMediaPlayer.playCurrentMemory();
            }
            // } else {
            // mMediaPlayer.resetPlayStatus();
            // }
        }

        if (mCreate) {
            mCreate = false;
            setPage(R.id.btn_all);
            mCurrentList = mPlayingTVList;

            // if(mPlayingTVList.getAdapter()==null){
            updateListViewAdapter(mPlayingTVList, mMediaPlayer.getStrList(MusicPlayer.LIST_PLAYING, -1), 0);
            // }
            // updatePlayModeView();
        }
        updateView();
    }

    private final static boolean mIsAsync = true;

    private void doFirstRun() {
        if (mIsAsync) {
            if (mUpdateAsyncTask == null) {
                mUpdateAsyncTask = new UpdateAsyncTask();
                mUpdateAsyncTask.execute();
            }
        } else {
            doFirstRunAsync();
        }
    }

    private void doFirstRunAsync() {
        mMediaPlayer.checkPlayingListIsExist();
        ArrayList<String> list = mMediaPlayer.getStrList(MusicPlayer.LIST_PLAYING, -1);
        if (list.size() > 0) { //
            mHandler.post(new Runnable() {
                public void run() {
                    doMemoryPlay();
                }
            });
        } else {
            list = mMediaPlayer.getStrListEx(MusicPlayer.LIST_SD, -1);
            if (list.size() > 0) {
                // setPage(R.id.btn_sd);
                updateNewActivity(MusicPlayer.LIST_SD);
            } else {
                list = mMediaPlayer.getStrListEx(MusicPlayer.LIST_SD2, -1);
                if (list.size() > 0) {
                    updateNewActivity(MusicPlayer.LIST_SD2);
                } else {
                    list = mMediaPlayer.getStrListEx(MusicPlayer.LIST_USB, -1);
                    if (list.size() > 0) {
                        updateNewActivity(MusicPlayer.LIST_USB);
                    } else {
                        list = mMediaPlayer.getStrListEx(MusicPlayer.LIST_USB2, -1);
                        if (list.size() > 0) {
                            updateNewActivity(MusicPlayer.LIST_USB2);
                        } else {
                            list = mMediaPlayer.getStrListEx(MusicPlayer.LIST_USB3, -1);
                            if (list.size() > 0) {
                                updateNewActivity(MusicPlayer.LIST_USB3);
                            } else {
                                list = mMediaPlayer.getStrListEx(MusicPlayer.LIST_USB4, -1);
                                if (list.size() > 0) {
                                    updateNewActivity(MusicPlayer.LIST_USB4);
                                } else {

                                    list = mMediaPlayer.getStrListEx(MusicPlayer.LIST_LOCAL, -1);
                                    if (list.size() > 0) {
                                        updateNewActivity(MusicPlayer.LIST_LOCAL);
                                    } else {
                                        mHandler.post(new Runnable() {
                                            public void run() {
                                                clearListView(MusicPlayer.LIST_PLAYING);
                                            }
                                        });

                                    }
                                }
                            }
                        }
                    }
                }
            }

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
            doFirstRunAsync();
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

    private void startProcessBar() {
        stoptProcessBar();
        setProgress();

        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 1000);
        // mHandler.obtainMessage(MSG_UPDATE_SEEK_BAR, 0, 0), 1000);

    }

    private void stoptProcessBar() {
        mHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            // Log.e(TAG, "########################### false" + msg.what);

            switch (msg.what) {

                case MSG_HIDE_MENU: {
                    if ((System.currentTimeMillis() - mHomeBackHideTime) >= TIME_HIDE_MENU && (mIsFullScrean)) {
                        hideHomeBackKey();
                    }
                }
                break;
                case MSG_UPDATE_SEEK_BAR: {
                    startProcessBar();
                }
                break;
                case MSG_STORAGE_UNMOUNT: {
                    // clearListView(msg.arg1);
                    // if (msg.arg2 != 0) {
                    // if (mMediaPlayer != null) {
                    // // mMediaPlayer.pause();
                    // playPath("");
                    // }
                    // clearListView(msg.arg2);
                    // }
                    // queryVedioFromDB();

                }
                break;
                case MSG_FIRST_RUN: {
                    doFirstRun();

                }
                break;
                case MSG_HIDE_SCAN_DIALOG: {

                }
                break;
                case MSG_AUTO_FULLSCREEN: {
                    if (!mPause && GlobalDef.mSource == MyCmd.SOURCE_VIDEO) {
                        autoToFullScreen();
                    }
                    break;
                }
                case MSG_SHOW_SCREEN1_VIDEO: {
                    showScreen1Video();
                    break;
                }
                case MSG_CHECK_BRAKE:
                    startCheckBrakeCar();
                    break;

                case MSG_SET_PROCESS:
                    setProcess(mProgress);
                    break;
                case MSG_CHECK_NO_FILE:
                    checkShowSignal();
                    break;
                case MSG_UPDATE_PLAY_STATUS:
                    updatePlayStatus();
                    break;
                // case MyServiceBinder.MESSAGE_COMMON_CALLBACK_FROM_SERVICE: {
                // // Log.e("!!!!!", ""+msg.arg1);
                // switch (msg.arg1) {
                // case MyCmd.CALLBACK_COMMON_KEY_NEXT:
                // onClick(R.id.next2);
                // break;
                // case MyCmd.CALLBACK_COMMON_KEY_PLAYPAUSE:
                // onClick(R.id.pp2);
                // break;
                // case MyCmd.CALLBACK_COMMON_KEY_PREVIOUS:
                // onClick(R.id.prev2);
                // break;
                // case MyCmd.CALLBACK_COMMON_VIDEO_SAVE_PLAYSTATUS:
                // if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                // mMediaPlayer.pause();
                // mSavePlayerStatus = 1;
                // }
                // break;
                // case MyCmd.CALLBACK_COMMON_VIDEO_RESET_PLAYSTATUS:
                // if (mSavePlayerStatus != 0) {
                // mSavePlayerStatus = 0;
                // if (mMediaPlayer != null) {
                // mMediaPlayer.start();
                // }
                // }
                // break;
                // case MyCmd.CALLBACK_COMMON_KEY_FF:
                // onClick(R.id.ffwd2);
                // break;
                // case MyCmd.CALLBACK_COMMON_KEY_FR:
                // onClick(R.id.rew2);
                // break;
                // }
                //
                // }
                // break;

            }

        }
    };

    private ListView mPlayingTVList = null;
    private ListView mLocalTVList = null;
    private ListView mSDTVList = null;
    private ListView mSD2TVList = null;
    private ListView mUsbTVList = null;
    private ListView mUsb2TVList = null;
    private ListView mUsb3TVList = null;
    private ListView mUsb4TVList = null;
    private ListView mCurrentList = null;

    private TextView mPageSeleteButton = null;

    private int mResPageId = 0;

    private void setPage(int id) {
        int index = (mDisplayIndex + 1) % 2;
        if ((mUI[index] != null)) {
            mUI[index].doSetPage(id);
        }
        doSetPage(id);
    }

    private void setPageforce(int id) {
        int ret = 0;
        if (id == R.id.btn_usb) {
            ret = ComMediaPlayer.QUERY_FLAG_USB;
        } else if (id == R.id.btn_usb2) {
            ret = ComMediaPlayer.QUERY_FLAG_USB2;
        } else if (id == R.id.btn_usb3) {
            ret = ComMediaPlayer.QUERY_FLAG_USB3;
        } else if (id == R.id.btn_usb4) {
            ret = ComMediaPlayer.QUERY_FLAG_USB4;
        } else if (id == R.id.btn_sd) {
            ret = ComMediaPlayer.QUERY_FLAG_SD;
        } else if (id == R.id.btn_sd2) {
            ret = ComMediaPlayer.QUERY_FLAG_SD2;
        } else if (id == R.id.btn_local) {
            ret = ComMediaPlayer.QUERY_FLAG_LOCAL;
        }
        if (ret != 0) {

            mResPageId = 0;
            mMediaPlayer.clearQueryFlag(ret);
        }
        setPage(id);
    }

    @SuppressLint("NonConstantResourceId")
    private void doSetPage(int id) {
        if (id == mResPageId) {
            return;
        }
        if (mIs3PageSroll && id != R.id.btn_all) {
            mPreStoragePage = id;
        }
        boolean ret = true;
        if (id == R.id.btn_local) {
            if (!mIs3PageSroll) {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
            }
            mMainView.findViewById(R.id.tv_local_list).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.tv_sd_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb3_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb4_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.btn_add_all).setVisibility(View.VISIBLE);
            initListView(R.id.tv_local_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_LOCAL)) {
            mCurrentList = mLocalTVList;
            // }
        } else if (id == R.id.btn_sd) {
            if (!mIs3PageSroll) {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
            }
            mMainView.findViewById(R.id.tv_local_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd_list).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.tv_sd2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb3_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb4_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.btn_add_all).setVisibility(View.VISIBLE);
            initListView(R.id.tv_sd_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_SD)) {
            mCurrentList = mSDTVList;
            // }
        } else if (id == R.id.btn_sd2) {
            if (!mIs3PageSroll) {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
            }
            mMainView.findViewById(R.id.tv_local_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd2_list).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.tv_sd_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb3_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb4_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.btn_add_all).setVisibility(View.VISIBLE);
            initListView(R.id.tv_sd2_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_SD)) {
            mCurrentList = mSD2TVList;
            // }
        } else if (id == R.id.btn_usb2) {
            if (!mIs3PageSroll) {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
            }
            mMainView.findViewById(R.id.tv_local_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.btn_add_all).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.tv_usb2_list).setVisibility(View.VISIBLE);

            mMainView.findViewById(R.id.tv_usb_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb3_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb4_list).setVisibility(View.GONE);
            initListView(R.id.tv_usb2_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_USB)) {
            mCurrentList = mUsb2TVList;
            // }
        } else if (id == R.id.btn_usb3) {
            if (!mIs3PageSroll) {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
            }
            mMainView.findViewById(R.id.tv_local_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb2_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb4_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.btn_add_all).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.tv_usb3_list).setVisibility(View.VISIBLE);

            initListView(R.id.tv_usb3_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_USB)) {
            mCurrentList = mUsb3TVList;
            // }
        } else if (id == R.id.btn_usb4) {
            if (!mIs3PageSroll) {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
            }
            mMainView.findViewById(R.id.tv_local_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb2_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb3_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.btn_add_all).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.tv_usb4_list).setVisibility(View.VISIBLE);

            initListView(R.id.tv_usb4_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_USB)) {
            mCurrentList = mUsb4TVList;
            // }
        } else if (id == R.id.btn_usb) {
            if (!mIs3PageSroll) {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
            }
            mMainView.findViewById(R.id.tv_local_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.btn_add_all).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.tv_usb_list).setVisibility(View.VISIBLE);

            mMainView.findViewById(R.id.tv_usb2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb3_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb4_list).setVisibility(View.GONE);
            initListView(R.id.tv_usb_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_USB)) {
            mCurrentList = mUsbTVList;
            // }
        } else if (id == R.id.btn_all) {
            if (!mIs3PageSroll) {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.VISIBLE);
                mMainView.findViewById(R.id.tv_local_list).setVisibility(View.GONE);
                mMainView.findViewById(R.id.tv_sd_list).setVisibility(View.GONE);
                mMainView.findViewById(R.id.tv_sd2_list).setVisibility(View.GONE);
                mMainView.findViewById(R.id.tv_usb_list).setVisibility(View.GONE);

                mMainView.findViewById(R.id.tv_usb2_list).setVisibility(View.GONE);
                mMainView.findViewById(R.id.tv_usb3_list).setVisibility(View.GONE);

                mMainView.findViewById(R.id.tv_usb4_list).setVisibility(View.GONE);
                mMainView.findViewById(R.id.btn_add_all).setVisibility(View.GONE);
            } else {
                //				if(MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef.getSystemUI())){
                //
                //				} else {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
                //				}
            }
            // mHandler.sendMessageDelayed(
            // mHandler.obtainMessage(MSG_UPDATE_LIST, R.id.tv_all_list, 0), 0);
            initListView(R.id.tv_all_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_PLAYING)) {
            // mCurrentPlayPos = 0;
            mCurrentList = mPlayingTVList;
            // }
        } else {
            ret = false;
        }
        if (ret) {
            // updateView();
            TextView tv = (TextView) mMainView.findViewById(id);
            Drawable d[];
            d = tv.getCompoundDrawables();
            if (d[0] != null) {
                d[0].setLevel(1);
            } else if (d[1] != null) {
                d[1].setLevel(1);
            }

            if (mPageSeleteButton != null && mPageSeleteButton != tv) {
                // mPageSeleteButton.setCompoundDrawables(this.getResources().getDrawable(R.id),
                // null, null, null);
                d = mPageSeleteButton.getCompoundDrawables();
                if (d != null) {
                    if (d.length > 0 && d[0] != null) {
                        d[0].setLevel(0);
                    } else if (d.length > 1 && d[1] != null) {
                        d[1].setLevel(0);
                    }
                }
            }

            if (MachineConfig.VALUE_SYSTEM_UI45_8702_2.equals(GlobalDef.getSystemUI())) {
                tv.setSelected(true);
                if (mPageSeleteButton != null && mPageSeleteButton != tv) {
                    mPageSeleteButton.setSelected(false);
                }
            }

            mPageSeleteButton = tv;
            mResPageId = id;
        }
    }

    private void updatePlayStatus() {
        if (mMediaPlayer != null) {
            try {
                setPlayButtonStatus(mMediaPlayer.isPlaying());
            } catch (Exception e) {
                Log.d("ffk", e + "");
            }
        }
    }

    private void checkStorageIsMountedView(String path, int id) {
        View v = mMainView.findViewById(id);
        if (v != null) {

            if (GlobalDef.checkStorageIsMounted(path)) {
                v.setVisibility(View.VISIBLE);
                if (Util.isRKSystem()) {
                    if (R.id.btn_sd2 == id || R.id.btn_sd == id) {

                        if (path.contains("GPS")) {
                            // String s = path.
                            ((Button) v).setText("GPS");
                        } else {
                            ((Button) v).setText("SD");
                            // int string = R.string.sd;
                            // if(R.id.btn_sd2 == id){
                            // string = R.string.sd2;
                            // }
                            // ((Button)v).setText(string);
                        }

                    } else if (R.id.btn_usb == id || R.id.btn_usb2 == id || R.id.btn_usb3 == id || R.id.btn_usb4 == id) {

                        char end = path.charAt(path.length() - 1);
                        String s = "USB";
                        try {
                            int i = Integer.valueOf(end);
                            s += end;
                        } catch (Exception e) {

                        }
                        ((Button) v).setText(s);
                    }
                }
            } else {
                v.setVisibility(View.GONE);
            }
        }
    }

    private void updateStorageView() {
        checkStorageIsMountedView(ComMediaPlayer.MEDIA_SD_PATH, R.id.btn_sd);
        checkStorageIsMountedView(ComMediaPlayer.MEDIA_SD2_PATH, R.id.btn_sd2);
        checkStorageIsMountedView(ComMediaPlayer.MEDIA_USB_PATH, R.id.btn_usb);
        checkStorageIsMountedView(ComMediaPlayer.MEDIA_USB2_PATH, R.id.btn_usb2);
        checkStorageIsMountedView(ComMediaPlayer.MEDIA_USB3_PATH, R.id.btn_usb3);
        checkStorageIsMountedView(ComMediaPlayer.MEDIA_USB4_PATH, R.id.btn_usb4);
        // checkStorageIsMountedView(MusicPlayer.MEDIA_USB2_PATH,
        // R.id.btn_usb2);

    }

    private void updateView() {

        updateStorageView();

        if (mCurrentList != null && mMediaPlayer.getCurPosition() >= 0 && mMediaPlayer.getCurPosition() < mCurrentList.getCount() && mCurrentList.getAdapter() != null) {

            if (mMediaPlayer.equalPreparePlaylist() || mCurrentList == mPlayingTVList) {
                ((MyListViewAdapter) mCurrentList.getAdapter()).setSelectItem(mMediaPlayer.getCurPosition() + ((MyListViewAdapter) mCurrentList.getAdapter()).getFolderNum());

                if (((mCurrentList.getLastVisiblePosition()) < mMediaPlayer.getCurPosition()) || (mCurrentList.getFirstVisiblePosition() > mMediaPlayer.getCurPosition())) {
                    mCurrentList.setSelection(mMediaPlayer.getCurPosition());
                }
            }
        }

        if (!GlobalDef.isOneSleepRemountTime()) {
            mMediaPlayer.doCheckSleepOnStorage();
        }

        if (mMediaPlayer != null) {
            setPlayRepeat(mMediaPlayer.getRepeatMode());
        }

        if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef.getSystemUI())) {
            mHandler.removeMessages(MSG_CHECK_NO_FILE);
            mHandler.sendEmptyMessageDelayed(MSG_CHECK_NO_FILE, 1000);
        }

    }

    private void checkShowSignal() {
        if (mMediaPlayer != null) {
            View v = mMainView.findViewById(R.id.no_signal);
            if (v != null) {
                if (mMediaPlayer.getCurSongNum() > 0) {
                    v.setVisibility(View.GONE);
                } else {
                    v.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private int setProgress() {
        if (mMediaPlayer == null || !mMediaPlayer.isInitialized()) {
            mProgress.setEnabled(false);
            mProgress.setProgress(0);
            return 0;
        }

        int position = 0;
        int duration = 0;
        if (!mMediaPlayer.isPlaying()) {
            mProgress.setEnabled(false);
            if (mMediaPlayer.getCurSongNum() <= 0) {
                mProgress.setProgress(0);
                mCurrentTime.setText("");
                mEndTime.setText("");
            }
        } else {
            mProgress.setEnabled(true);
            position = (int) mMediaPlayer.getCurrentPosition();
            duration = (int) mMediaPlayer.getDuration();
            if (mProgress != null && position >= 0 && duration > 0 && position <= duration) {
                long pos = 0;
                if (duration > 0) {
                    // use long to avoid overflow
                    pos = 1000L * position / duration;
                }

                mProgress.setProgress((int) pos);

                // if (position > 3000) {
                // mMediaPlayer.savePlayTime();
                // }
            }

            if (mEndTime != null) mEndTime.setText(stringForTime(duration));
            if ((mMediaPlayer.getCurSongNum() > 0)) {
                mCurrentTime.setText(stringForTime(position));
            }
            // if (mLrcview != null) {
            // if (mLrcview.getVisibility() == View.VISIBLE) {
            // if (mCurLyricFile != null && mCurLyricFile.exists()) {
            // mLrcview.initLrcIndex(position);
            // } else {
            // mLrcview.clearView();
            // mCurLyricFile = null;
            // }
            // }
            // }
        }

        return position;
    }

    private void setPlayRepeat(int repeat) {

        // Button repeatButton = ((Button) mMainView.findViewById(R.id.repeat));
        // Drawable repeatButtonDrawable[] =
        // repeatButton.getCompoundDrawables();

        ImageView view = (ImageView) mMainView.findViewById(R.id.repeat);
        if (view != null) {
            if (repeat == MusicPlayer.REPEAT_NONE) {
                ((ImageView) view).getDrawable().setLevel(0);
            } else if (repeat == MusicPlayer.REPEAT_ALL) {
                ((ImageView) view).getDrawable().setLevel(1);
            } else {
                ((ImageView) view).getDrawable().setLevel(2);
            }
        }

        View v = mMainView.findViewById(R.id.play_repeat_tag);
        if (v != null) {
            Drawable repeatTag = ((ImageView) v).getDrawable();
            //
            if (repeat == MusicPlayer.REPEAT_NONE) {
                // repeatButtonDrawable[0].setLevel(1);
                // repeatButton.setText(R.string.text_single);
                repeatTag.setLevel(0);
            } else if (repeat == MusicPlayer.REPEAT_ALL) {
                // repeatButtonDrawable[0].setLevel(2);
                // repeatButton.setText(R.string.text_random);
                repeatTag.setLevel(1);
            } else if (repeat == MusicPlayer.REPEAT_CURRENT) {
                // repeatButtonDrawable[0].setLevel(2);
                // repeatButton.setText(R.string.text_random);
                repeatTag.setLevel(2);
            } else {
                // repeatButtonDrawable[0].setLevel(0);
                // repeatButton.setText(R.string.text_repeat);
                repeatTag.setLevel(3);
            }
        }
        v = mMainView.findViewById(R.id.repeat2);
        if (v != null) {
            Drawable repeatTag = ((ImageView) v).getDrawable();
            if (repeat == MusicPlayer.REPEAT_SHUFFLE) {
                repeatTag.setLevel(2);
            } else if (repeat == MusicPlayer.REPEAT_CURRENT) {
                repeatTag.setLevel(1);
            } else {
                repeatTag.setLevel(0);
            }
        }
    }

    private void setPlayButtonStatus(boolean playing) {
        if (playing) {
            ((ImageView) mMainView.findViewById(R.id.pp)).getDrawable().setLevel(1);
            // mMoviceView.setVisibility(View.VISIBLE);
        } else {
            ((ImageView) mMainView.findViewById(R.id.pp)).getDrawable().setLevel(0);
        }
    }

    private void onClick(int id) {
        if (id == R.id.back) {
            if (SCREEN0 == mDisplayIndex) {

                ((Activity) mContext).finish();

                BroadcastUtil.sendToCarServiceSetSource(mContext, MyCmd.SOURCE_MX51);

            }
        } else if (id == R.id.btn_all || id == R.id.btn_local || id == R.id.btn_sd || id == R.id.btn_sd2 || id == R.id.btn_usb || id == R.id.btn_usb2 || id == R.id.btn_usb3 || id == R.id.btn_usb4) {
            setPageforce(id);
        } else if (id == R.id.btn_add_all) {
            boolean ret = addAllFilePlay(resourceIdToPage(mResPageId));
            if (ret && mMyScrollView != null) {
                mMyScrollView.scrollToPage(1);
            }

            // case R.id.rew: {
            // if (mMediaPlayer != null) {
            // int pos = mMediaPlayer.getCurrentPosition();
            // pos -= 15000;
            // mMediaPlayer.seekTo(pos);
            // if (!mMediaPlayer.isPlaying()) {
            // mMediaPlayer.start();
            // startReportCanbox();
            // }
            // }
            // }
            // break;
            // case R.id.ffwd: {
            // if (mMediaPlayer != null) {
            // int pos = mMediaPlayer.getCurrentPosition();
            // pos += 15000;
            // mMediaPlayer.seekTo(pos);
            // if (!mMediaPlayer.isPlaying()) {
            // mMediaPlayer.start();
            // startReportCanbox();
            // }
            // }
            // }
            // break;
        } else if (id == R.id.repeat) {
            int r = mMediaPlayer.pressRepeatMode();

            setPlayRepeat(r);
        } else if (id == R.id.repeat2) {
            int r = mMediaPlayer.pressRepeatMode2();

            setPlayRepeat(r);
        } else if (id == R.id.list) {
            if (mMyScrollView != null) {
                mMyScrollView.scrollToPage(0);
            }
        } else if (id == R.id.prev) {
            mMediaPlayer.prev();
            GlobalDef.reactiveSource(mContext, SOURCE, VideoService.mAudioFocusListener);
            // updateView();
        } else if (id == R.id.next) {
            mMediaPlayer.next();
            GlobalDef.reactiveSource(mContext, SOURCE, VideoService.mAudioFocusListener);
            // updateView();
        } else if (id == R.id.pp) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            } else {
                mMediaPlayer.play();
                GlobalDef.reactiveSource(mContext, SOURCE, VideoService.mAudioFocusListener);
            }
        } else if (id == R.id.stop) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
            mMediaPlayer.seekTo(0);

            if (mProgress != null) {
                mProgress.setProgress(0);
                mCurrentTime.setText("");
                mEndTime.setText("");
            }
        } else if (id == R.id.full) {
            if (!mIsFullScrean) {
                changeToFullScreen();
            } else {
                changeToSmallScreen();
            }
        } else if (id == R.id.full2) {
            changeVideoSize();
        } else if (id == R.id.eq) {
            try {
                Intent it = new Intent("com.eqset.intent.action.EQActivity");
                mContext.startActivity(it);
            } catch (Exception e) {

            }
        } else if (id == R.id.eq_mode_switch) {
            GlobalDef.swtichEQMode();
        }
		/*else if (id == R.id.mic) {
            UtilCarKey.doKeyMic(mContext);
        } else if (id == R.id.vol) {
            Intent it = new Intent(MyCmd.BROADCAST_START_VOLUMESETTINGS_COMMON);
            it.setPackage("com.my.out");
            mContext.sendBroadcast(it);
        }*/
    }

    public void onClick(View view) {
        int id = view.getId();
        onClick(id);

        prepareFullScreen();
    }

    private int mUpdateNewActivityPage = 0;

    private void updateNewActivity(int page) {

        mUpdateNewActivityPage = page;
        mHandler.post(new Runnable() {
            public void run() {
                if (mMediaPlayer.mSleepRestore) {
                    mMediaPlayer.mSleepRestore = false;
                    if (mMediaPlayer.mMemoryPlayFromSleep == 1) {
                        updateMountFromSleep(mUpdateNewActivityPage);
                    } else {
                        updateStorageView();
                    }
                } else {
                    mResPageId = -1;
                    if (mUpdateNewActivityPage != 0) {
                        addAllFilePlay(mUpdateNewActivityPage);
                        mUpdateNewActivityPage = 0;
                    }
                }
            }
        });
    }

    private void updateMountFromSleep(int page) {
        Log.d("allen", "updateMountFromSleep:" + page);
        setPage(R.id.btn_all);
        mMediaPlayer.checkPlayingListIsExistFromSleep();
        ArrayList<String> list = mMediaPlayer.getStrList(MusicPlayer.LIST_PLAYING, -1);
        if (list.size() > 0) { //
            mHandler.post(new Runnable() {
                public void run() {
                    mMediaPlayer.playCurrentMemory();

                    mCurrentList = mPlayingTVList;

                    // if (mPlayingTVList.getAdapter() == null) {
                    updateListViewAdapter(mPlayingTVList, mMediaPlayer.getStrList(MusicPlayer.LIST_PLAYING, -1), 0);
                    updateView();
                }
            });
        }
    }

    private boolean addAllFilePlay(int page) {
        boolean ret = mMediaPlayer.addAllFilePlay(page);
        if (ret) {
            setPage(R.id.btn_all);

            GlobalDef.reactiveSource(mContext, SOURCE, VideoService.mAudioFocusListener);
            mMediaPlayer.play(0);

            mCurrentList = mPlayingTVList;

            updateListViewAdapter(mPlayingTVList, mMediaPlayer.getStrList(MusicPlayer.LIST_PLAYING, -1), 0);
        }
        return ret;
    }

    private boolean mSurfaceDestroyed = true;
    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // mMediaPlayer.setDisplay(mSurfaceView.getHolder());
            // mMediaPlayer.start();

            // if (null != mMediaPlayer) {
            // mMediaPlayer.setDisplay(mMoviceView.getHolder());
            // }
            // mSurfaceDestroyed = false;

            Log.d(TAG, "surfaceChanged" + w + "::" + h);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            SurfaceHolder h = mMoviceView.getHolder();
            Log.d(TAG, h + ":hhhhhhh" + holder);
            if (null != mMediaPlayer) {
                mMediaPlayer.setDisplay(mMoviceView.getHolder());
            }
            mSurfaceDestroyed = false;

            if (defaultWidth == 0) {
                defaultWidth = mMoviceView.getWidth();
                defaultHeight = mMoviceView.getHeight();
            }
            Log.d(TAG, defaultWidth + ":surfaceCreated" + mMoviceView.getWidth() + "::" + mMoviceView.getHeight());
            // mVideoHide.setVisibility(View.GONE);
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            if (false == mSurfaceDestroyed && null != mMediaPlayer) {
                // if (mDisplayIndex == SCREEN0) {
                // if (mUI[1] != null
                // && (mUI[1].mScreen1Type != TYPE_VIDEO_ONLY ||
                // mUI[1].mScreen1Type != TYPE_FULL_FUNCTION)) {
                // mMediaPlayer.setDisplay(null);
                // }
                // } else {
                // if (mUI[0] != null && mUI[0].mPause) {
                // mMediaPlayer.setDisplay(null);
                // }
                // }
            }
            if (isAllAppHide()) {
                Log.d(TAG, "surfaceDestroyed set null");
                if (mMediaPlayer != null) {
                    if (!Util.isAndroidQ()) mMediaPlayer.setDisplay(null);
                }
            }
            mSurfaceDestroyed = true;

            Log.d(TAG, "surfaceDestroyed");
            // mVideoHide.setVisibility(View.VISIBLE);
            return;
        }
    };

    private void play(int id) {
        if (!mMediaPlayer.equalPreparePlaylist()) {
            mMediaPlayer.updateCurrentUriList();
        }
        GlobalDef.reactiveSource(mContext, SOURCE, VideoService.mAudioFocusListener);
        mMediaPlayer.play(id);
        prepareFullScreen();
        // updateView();
    }

    private int resourceIdToPage(int page) {
        int r = 0;
        if (page == R.id.btn_usb) {
            r = MusicPlayer.LIST_USB;
        } else if (page == R.id.btn_usb2) {
            r = MusicPlayer.LIST_USB2;
        } else if (page == R.id.btn_usb3) {
            r = MusicPlayer.LIST_USB3;
        } else if (page == R.id.btn_usb4) {
            r = MusicPlayer.LIST_USB4;
        } else if (page == R.id.btn_sd) {
            r = MusicPlayer.LIST_SD;
        } else if (page == R.id.btn_sd2) {
            r = MusicPlayer.LIST_SD2;
        } else if (page == R.id.btn_local) {
            r = MusicPlayer.LIST_LOCAL;
        }
        return r;
    }

    private static int mScreen1InitStatus = 0;
    private static final int SCREEN1_STATUS_FULLSCREEN = 1;

    private void showScreen1Video() {
        // test

        mScreen1InitStatus = SCREEN1_STATUS_FULLSCREEN;
        // PresentationUI.updateVideoQuit();

    }

    private GestureDetector mGestureDetector;

    private static boolean mScreen1StartShowFull = false;

    public boolean mWillDestory = false;
    private int mCurrentSeekPos = -1;

    private void clearListView(int id) {
        switch (id) {
            case MusicPlayer.LIST_USB:
                if (mUsbTVList != null) {
                    mUsbTVList.setAdapter(null);
                    mUsbTVList.postInvalidate();
                    mUsbTVList = null;
                }
                break;
            case MusicPlayer.LIST_USB2:
                if (mUsb2TVList != null) {
                    mUsb2TVList.setAdapter(null);
                    mUsb2TVList.postInvalidate();
                    mUsb2TVList = null;
                }
                break;
            case MusicPlayer.LIST_USB3:
                if (mUsb3TVList != null) {
                    mUsb3TVList.setAdapter(null);
                    mUsb3TVList.postInvalidate();
                    mUsb3TVList = null;
                }
                break;
            case MusicPlayer.LIST_USB4:
                if (mUsb4TVList != null) {
                    mUsb4TVList.setAdapter(null);
                    mUsb4TVList.postInvalidate();
                    mUsb4TVList = null;
                }
                break;
            case MusicPlayer.LIST_SD:
                if (mSDTVList != null) {
                    mSDTVList.setAdapter(null);
                    mSDTVList.postInvalidate();
                    mSDTVList = null;
                }
                break;
            case MusicPlayer.LIST_SD2:
                if (mSD2TVList != null) {
                    mSD2TVList.setAdapter(null);
                    mSD2TVList.postInvalidate();
                    mSD2TVList = null;
                }
                break;
            case MusicPlayer.LIST_LOCAL:
                if (mLocalTVList != null) {
                    mLocalTVList.setAdapter(null);
                    mLocalTVList.postInvalidate();
                    mLocalTVList = null;
                }
                break;
            // default:
            case MusicPlayer.LIST_PLAYING:
                if (mPlayingTVList != null) {
                    mPlayingTVList.setAdapter(null);
                    mPlayingTVList.postInvalidate();
                    mPlayingTVList = null;
                }
                mResPageId = 0;
                if (mPageSeleteButton != null) {
                    Drawable d[] = mPageSeleteButton.getCompoundDrawables();
                    if (d != null && d.length > 0 && d[0] != null) d[0].setLevel(0);

                    mPageSeleteButton = null;

                }

                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d("allen12", "onPause" + this);
        VideoService.setUICallBack(null, mDisplayIndex);

        mHandler.removeMessages(MSG_AUTO_FULLSCREEN);

        if (mDisplayIndex == SCREEN0) {

            if (GlobalDef.getScreenNum(mContext) > 1
                /* && ParkBrake.isBrake() */) {
                if (!mWillDestory) {
                    GlobalDef.notifyUIScreen0Change(SOURCE, 0);

                    if (mUI[1] != null && !mUI[1].mPause) {
                        mUI[1].updateFullUI();
                    } else {
                        mScreen1StartShowFull = true;
                    }
                }
            } else {
                if (!VIDEO_PLAY_BACKGROUND_SCREEN0_ONLY) {
                    mMediaPlayer.savePlayStatus();
                    mMediaPlayer.setShowBePlay(false);
                    if (Util.isAndroidQ()) {
                        mMediaPlayer.stopPlay();
                        mMediaPlayer.reset();
                    } else {
                        mMediaPlayer.stop();
                    }
                }
            }

        } else {
            if (mUI[0] != null && !mUI[0].mPause) {
                mUI[0].updateFullUI();
            } else {
                mMediaPlayer.savePlayStatus();
                mMediaPlayer.setShowBePlay(false);
            }
            mScreen1StartShowFull = false;
        }

        // mIsFullScrean = false;

        stoptProcessBar();
    }

    private final Handler mHandlerService = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GlobalDef.MSG_UPDATE_EQ_MODE:
                    GlobalDef.updateEQModeButton(mMainView, R.id.eq_mode_switch);

                    break;
            }
            super.handleMessage(msg);
        }

    };


    public int mFirstPowerOn = 0;

    @Override
    public void onResume() {

        super.onResume();
        Log.d("allen12", "onResume" + this);
        VideoService.setHandler(mHandlerService, mDisplayIndex);
        startCheckBrakeCar();

        VideoService.setUICallBack(mIMediaCallBack, mDisplayIndex);

        updateFullUI();
        if (mDisplayIndex == SCREEN0) {
            // if (mUI[1] != null && !mUI[1].mPause) {
            // // test
            // PresentationUI.updateVideoResume();
            // }
            GlobalDef.setCurrentScreen0(this);
            GlobalDef.notifyUIScreen0Change(SOURCE, 1);
        } else {
            if (mScreen1StartShowFull) {
                changeToFullScreen();
            }
        }

        GlobalDef.reactiveSource(mContext, SOURCE, VideoService.mAudioFocusListener);

        BroadcastUtil.sendToCarServiceSetSource(mContext, SOURCE);
        prepareFullScreen();

        if (mDisplayIndex == 1) {
            if (mUI[0] != null && !mUI[0].mPause) {
                doSetPage(mUI[0].mResPageId);

                mCurrentList = mPlayingTVList;

                updateListViewAdapter(mPlayingTVList, mMediaPlayer.getStrList(MusicPlayer.LIST_PLAYING, -1), 0);
            }

            if (mUI[0] == null || mUI[0].mPause) {
                mHandler.removeMessages(MSG_FIRST_RUN);
                mHandler.sendMessage(mHandler.obtainMessage(MSG_FIRST_RUN, 0, 150));
            } else {

                // updateView();
            }

        } else {
            int delay = 250;
            if (Util.isRKSystem()) {
                if (mFirstPowerOn == 2 && mMediaPlayer != null) {
                    int page = mMediaPlayer.getData(ComMediaPlayer.SAVE_DATA_PAGE);
                    //					Log.d("MusicPlayer", page+"zzzz");
                    if (page == ComMediaPlayer.LIST_USB3 || page == ComMediaPlayer.LIST_USB4) {
                        delay = 1800;
                        //						Log.d("MusicPlayer", delay+"zzzz!!!!!!!");
                    }
                }
            }
            mHandler.removeMessages(MSG_FIRST_RUN);
            mHandler.sendEmptyMessageDelayed(MSG_FIRST_RUN, delay);

            //			mHandler.sendMessage(mHandler.obtainMessage(MSG_FIRST_RUN, 0, 5));
            clearQueryFlag();
        }

        startProcessBar();

        updateView();
        mMediaPlayer.setShowBePlay(true);
        mMediaPlayer.updateCompletionListener(true);
        updatePlayStatus();

        GlobalDef.requestEQInfo();
        // mHandler.sendMessageDelayed(
        // mHandler.obtainMessage(MSG_FIRST_RUN, 0, 0), 200);
    }

    private void clearQueryFlag() {
        if (mMediaPlayer != null) {
            mMediaPlayer.clearQueryFlag();
        }

        if (mResPageId != R.id.btn_local) {
            mLocalTVList = null;
        }
        if (mResPageId != R.id.btn_sd) {
            mSDTVList = null;
        }
        if (mResPageId != R.id.btn_sd2) {
            mSD2TVList = null;
        }
        if (mResPageId != R.id.btn_usb) {
            mUsbTVList = null;
        }
        if (mResPageId != R.id.btn_usb2) {
            mUsb2TVList = null;
        }
        if (mResPageId != R.id.btn_usb3) {
            mUsb3TVList = null;
        }
        if (mResPageId != R.id.btn_usb4) {
            mUsb4TVList = null;
        }
        // mCurrentList = null;
    }

    private boolean mCreate = false;

    @Override
    public void onDestroy() {

        super.onDestroy();
        if (mDisplayIndex == SCREEN0) {

            mHandler.removeMessages(MSG_SHOW_SCREEN1_VIDEO);

        }
        // VideoService.setUICallBack(null, mDisplayIndex);
        // releaseMediaplayer();
        if (isAllAppHide()) {
            stopReportCanbox();
            // mAudioManager.abandonAudioFocus(mAudioFocusListener);
            if (mMediaPlayer != null) {
                // mMediaPlayer.stop();
                if (Util.isRKSystem()) {
                    mMediaPlayer.savePlayStatusRelease();
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    VideoService.clearMediaPlayer();
                }
            }
        }
    }

    private Handler mIMediaCallBack = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MusicPlayer.SEARCH_LIST_FILE_UPDATE:
                    updateSearchListView();
                    break;
                case MusicPlayer.META_CHANGED:
                    updateView();
                    break;
                case MusicPlayer.QUEUE_CHANGED: {

                    clearListView(msg.arg1);

                    updateView();
                }
                break;
                case MusicPlayer.PLAYSTATE_CHANGED:
                    updatePlayStatus();
                    if (Util.isPX6() && Util.isAndroidQ()) mHandler.sendEmptyMessageDelayed(MSG_UPDATE_PLAY_STATUS, 500);
                    break;
                case MusicPlayer.STORAGE_MOUNTED: {
                    if (!mPause) {
                        updateNewActivity(msg.arg1);
                    }
                    if (msg.arg1 == 0) {
                        updateStorageView();
                    }
                }
                break;
                case MusicPlayer.STORAGE_MOUNTED_NOMEDIA:
                    updateStorageView();
                    break;
                case ComMediaPlayer.PARK_BRAKE: {

                    if (msg.arg1 != mBrake) {
                        boolean brake = (msg.arg1 == 1);
                        brakeCarShowText(brake);

                        mBrake = msg.arg1;
                    }
                }
                break;
                case ComMediaPlayer.PLAY_FAIL:
                    boolean show = true;
                    if (mDisplayIndex == 1) {
                        if (mUI[0] != null && !mUI[0].mPause) {
                            show = false;
                        }
                    }
                    if (show && msg.obj != null) {
                        showMyToast((String) msg.obj);
                    }
                    break;
                case MSG_HIDE_MYTOAST:
                    hideMyToast();
                    break;

            }

        }
    };

    private static boolean isAllAppHide() {
        boolean ret = true;
        for (int i = 0; i < MAX_DISPLAY; ++i) {
            if (mUI[i] != null && !mUI[i].mPause) {
                ret = false;
                break;
            }
        }
        return ret;
    }

    // private void releaseMediaplayer(){
    // if(isAllAppHide()) {
    // stopReportCanbox();
    // if (mMediaPlayer != null) {
    // mMediaPlayer.stop();
    // }
    // }
    // }

    boolean mIsHomeBackKeyShow = false;

    private void hideHomeBackKey() {
        mHomeBackKey.setVisibility(View.GONE);

        mProgressBar.setVisibility(View.GONE);

        mMainView.findViewById(R.id.bottom_button_layout).setVisibility(View.GONE);
        //mMainView.findViewById(R.id.common_bottom_button_layout).setVisibility(View.GONE);
        if (mDisplayIndex == SCREEN1) {

            mMainView.findViewById(R.id.common_status_bar_main).setVisibility(View.GONE);
        }
        mIsHomeBackKeyShow = false;
    }

    private void showHomeBackKey() {

        mProgressBar.setVisibility(View.VISIBLE);

        mHomeBackHideTime = System.currentTimeMillis();
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_HIDE_MENU, 0, 0), TIME_HIDE_MENU);

        if (mDisplayIndex == SCREEN1) {

            mMainView.findViewById(R.id.common_status_bar_main).setVisibility(View.VISIBLE);
            if (mScreen1Type == TYPE_FULL_FUNCTION) {

                mHomeBackKey.setVisibility(View.VISIBLE);
                mMainView.findViewById(R.id.bottom_button_layout).setVisibility(View.VISIBLE);
                //mMainView.findViewById(R.id.common_bottom_button_layout).setVisibility(View.VISIBLE);
            }
        } else {

            mMainView.findViewById(R.id.bottom_button_layout).setVisibility(View.VISIBLE);
            //mMainView.findViewById(R.id.common_bottom_button_layout).setVisibility(View.VISIBLE);
            mHomeBackKey.setVisibility(View.VISIBLE);
        }
        mIsHomeBackKeyShow = true;
    }

    private void togleHomeBackKey() {
        if (!mIsHomeBackKeyShow) {
            showHomeBackKey();
        } else {
            hideHomeBackKey();
        }
    }

    private void startReportCanbox() {
        // stopReportCanbox();
        // if (mTimerCanbox == null) {
        // TimerTask task = new TimerTask() {
        // public void run() {
        // // byte min = (byte) (((position() / 1000) / 60) % 60);
        // // byte sec = (byte) ((position() / 1000) % 60);
        // // int percent = 0;
        // // if (duration() > 0) {
        // // percent = ((int) (position() * 100 / duration()) << 16);
        // // }
        // // if (mAkMusic != null) {
        // // mAkMusic.reportCanboxPlayStatus(MyCmd.SOURCE_MX51,
        // // mPlayPos + 1,
        // // (mPlayListLen & 0xffff) | percent, min, sec);
        // if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
        // sendBroadcastInt(
        // VIDEO_SOURCE_CHANGE,
        // mCurrentPlayPos,
        // (int) (mMediaPlayer.getCurrentPosition() / 1000L),
        // mCurrentTotal);
        // }
        // // }
        // }
        // };
        // mTimerCanbox = new Timer();
        // mTimerCanbox.schedule(task, 1, 1000);
        // }
    }

    private void stopReportCanbox() {
        // if (mTimerCanbox != null) {
        // mTimerCanbox.cancel();
        // mTimerCanbox.purge();
        // mTimerCanbox = null;
        // }
    }

    public int getScreen0Type() {
        int ret = SCREEN0_HIDE;
        if (mUI[0] != null) {
            if (!mUI[0].mPause) {
                if (mIsFullScrean) {
                    ret = SCREEN0_SHOW_FULLSCREEN;
                } else {
                    ret = SCREEN0_SHOW_VIDEO;
                }
            }
        }
        return ret;
    }

    private final static int MSG_HIDE_MYTOAST = 1000000;
    private View mTextViewToast;
    private RelativeLayout.LayoutParams mToastParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

    private void showMyToast(String s) {
        try {
            if (mTextViewToast == null) {
                LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //mTextViewToast = inflate.inflate(com.android.internal.R.layout.transient_notification,null);
                mToastParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                // mTextViewToast = new TextView(mContext);
            }

            //TextView tv = (TextView) mTextViewToast.findViewById(com.android.internal.R.id.message);

            //tv.setText(s);

            // Log.d("allen1", mTextViewToast.getParent() + "");
            if (mTextViewToast.getParent() == null) {

                if (mMainView instanceof RelativeLayout) {
                    RelativeLayout new_name = (RelativeLayout) mMainView;
                    new_name.addView(mTextViewToast, mToastParams);
                }

            }
        } catch (Exception e) {

        }
        mIMediaCallBack.removeMessages(MSG_HIDE_MYTOAST);
        mIMediaCallBack.sendEmptyMessageDelayed(MSG_HIDE_MYTOAST, 4000);
    }

    private void hideMyToast() {
        if (mTextViewToast != null && mTextViewToast.getParent() != null) {
            try {
                if (mMainView instanceof RelativeLayout) {
                    RelativeLayout new_name = (RelativeLayout) mMainView;
                    new_name.removeView(mTextViewToast);
                }
            } catch (Exception e) {

            }
        }
    }

    private final static int VIDEO_FULL_SCREEN = 1;
    private final static int VIDEO_FIX_SCREEN = 0;

    private int mVideoFixScreenType = VIDEO_FIX_SCREEN;

    private final static String SAVE_FULLFIX_SCREEN = "full_fix_screen";

    private void initFullFixButton() {
        mVideoFixScreenType = mMediaPlayer.getData(SAVE_FULLFIX_SCREEN);
        udpateFullFixButton();
    }

    private void udpateFullFixButton() {
        View v = mMainView.findViewById(R.id.full2);
        if (v != null) {
            ImageView iv = (ImageView) v;
            Drawable d;
            if (mVideoFixScreenType == VIDEO_FULL_SCREEN) {
                d = mContext.getResources().getDrawable(R.drawable.fix);
            } else {

                d = mContext.getResources().getDrawable(R.drawable.full);
            }
            iv.setImageDrawable(d);
        }
    }

    private void changeVideoSize() {
        if (mVideoFixScreenType == VIDEO_FULL_SCREEN) {
            mVideoFixScreenType = VIDEO_FIX_SCREEN;
        } else {
            mVideoFixScreenType = VIDEO_FULL_SCREEN;
        }
        mMediaPlayer.saveData(SAVE_FULLFIX_SCREEN, mVideoFixScreenType);
        udpateFullFixButton();
        onChangeVideoSize();
    }

    private void onChangeVideoSize() {

        if (ResourceUtil.isMultiWindow()) {
            return;
        }

        int videoWidth;
        int videoHeight;

        int x = 0;
        int y = 0;

        int windowWidth;
        int windowHeight;

        if (mIsFullScrean) {
            DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
            windowWidth = dm.widthPixels;
            windowHeight = dm.heightPixels;
        } else {
            windowWidth = defaultWidth;
            windowHeight = defaultHeight;
        }

        //		Log.d("aac", defaultWidth + ":onChangeVideoSize:"+defaultHeight);

        //		Log.d("aac", windowWidth + ":onChangeVideoSize:"+windowHeight);

        if (mVideoFixScreenType == VIDEO_FULL_SCREEN) {
            videoWidth = windowWidth;
            videoHeight = windowHeight;
        } else {
            videoWidth = mMediaPlayer.getVideoWidth();
            videoHeight = mMediaPlayer.getVideoHeight();

            if (windowWidth > windowHeight) { // only support landscapse
                // now

                // max = Math.max(((float) videoWidth / (float)
                // mMediaPlayer.getVideoHeight()),
                // (float) videoHeight / (float) mMediaPlayer.getVideoWidth());
                //
                //
                // videoWidth = (int) Math.ceil((float) videoWidth / max);
                // videoHeight = (int) Math.ceil((float) videoHeight / max);

                float screen_scale = (windowWidth * 1.0f / windowHeight);
                float video_scale = (videoWidth * 1.0f / videoHeight);
                if (screen_scale > video_scale) {

                    videoWidth = (int) (windowHeight * video_scale);

                    videoHeight = windowHeight;

                } else {
                    videoWidth = windowWidth;

                    videoHeight = (int) (windowWidth / video_scale);
                }

                x = (windowWidth - videoWidth) / 2;
                y = (windowHeight - videoHeight) / 2;
                Log.d("aac", videoWidth + ":" + videoHeight + ":" + x + ":" + y);
            } else {

            }

        }
        //		Log.d("fk", videoWidth + ":" + videoHeight + ":"+mMoviceView.getHeight()+":"+mMoviceView.getWidth());

        //		AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(
        //				videoWidth, videoHeight, x, y);
        //		mMoviceView.setLayoutParams(lp);
        ViewGroup.LayoutParams lp = mMoviceView.getLayoutParams();

        if (lp instanceof AbsoluteLayout.LayoutParams) {
            AbsoluteLayout.LayoutParams new_name = (AbsoluteLayout.LayoutParams) lp;
            //			Log.d("fk", new_name.width + ":22:" + new_name.height + ":");

            new_name.x = x;
            new_name.y = y;
            new_name.width = videoWidth;
            new_name.height = videoHeight;

            mMoviceView.setLayoutParams(new_name);
        } else if (lp instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams new_name = (RelativeLayout.LayoutParams) lp;
            Log.d("fk", new_name.width + ":fff:" + new_name.height + ":");

            new_name.leftMargin = x;
            new_name.topMargin = y;
            new_name.width = videoWidth;
            new_name.height = videoHeight;

            mMoviceView.setLayoutParams(new_name);
        }

    }

    private OnVideoSizeChangedListener mOnVideoSizeChangedListener = new OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            onChangeVideoSize();
        }
    };

    private void scrollToPlayingPage() {
        if (mMyScrollView != null) {
            mMyScrollView.scrollToPage(1);
            if (mContext instanceof Activity) {
                View v = ((Activity) mContext).getCurrentFocus();
                //				Log.d("cc", "ddd!!!" + v);
                if (v != null) {
                    v.clearFocus();
                }
            }
        }
    }


    private int getPlayingIdToPage() {
        int ret = R.id.btn_local;
        if (mMediaPlayer.isInitialized()) {
            int page = mMediaPlayer.getCurPage();
            switch (page) {
                case ComMediaPlayer.LIST_USB:
                    ret = R.id.btn_usb;
                    break;
                case ComMediaPlayer.LIST_USB2:
                    ret = R.id.btn_usb2;
                    break;
                case ComMediaPlayer.LIST_USB3:
                    ret = R.id.btn_usb3;
                    break;
                case ComMediaPlayer.LIST_USB4:
                    ret = R.id.btn_usb4;
                    break;
                case ComMediaPlayer.LIST_SD:
                    ret = R.id.btn_sd;
                    break;
                case ComMediaPlayer.LIST_SD2:
                    ret = R.id.btn_sd2;
                    break;

            }
        }
        return ret;
    }

    private void updateSearchListView() {
        if (mResPageId == R.id.btn_local) {
            if (!mIs3PageSroll) {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
            }
            mMainView.findViewById(R.id.tv_local_list).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.tv_sd_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb3_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb4_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.btn_add_all).setVisibility(View.VISIBLE);
            initListView(R.id.tv_local_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_LOCAL)) {
            mCurrentList = mLocalTVList;
            // }
        } else if (mResPageId == R.id.btn_sd) {
            if (!mIs3PageSroll) {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
            }
            mMainView.findViewById(R.id.tv_local_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd_list).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.tv_sd2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb3_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb4_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.btn_add_all).setVisibility(View.VISIBLE);
            initListView(R.id.tv_sd_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_SD)) {
            mCurrentList = mSDTVList;
            // }
        } else if (mResPageId == R.id.btn_sd2) {
            if (!mIs3PageSroll) {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
            }
            mMainView.findViewById(R.id.tv_local_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd2_list).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.tv_sd_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb3_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb4_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.btn_add_all).setVisibility(View.VISIBLE);
            initListView(R.id.tv_sd2_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_SD)) {
            mCurrentList = mSD2TVList;
            // }
        } else if (mResPageId == R.id.btn_usb2) {
            if (!mIs3PageSroll) {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
            }
            mMainView.findViewById(R.id.tv_local_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.btn_add_all).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.tv_usb2_list).setVisibility(View.VISIBLE);

            mMainView.findViewById(R.id.tv_usb_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb3_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb4_list).setVisibility(View.GONE);
            initListView(R.id.tv_usb2_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_USB)) {
            mCurrentList = mUsb2TVList;
            // }
        } else if (mResPageId == R.id.btn_usb3) {
            if (!mIs3PageSroll) {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
            }
            mMainView.findViewById(R.id.tv_local_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb2_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb4_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.btn_add_all).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.tv_usb3_list).setVisibility(View.VISIBLE);

            initListView(R.id.tv_usb3_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_USB)) {
            mCurrentList = mUsb3TVList;
            // }
        } else if (mResPageId == R.id.btn_usb4) {
            if (!mIs3PageSroll) {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
            }
            mMainView.findViewById(R.id.tv_local_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb2_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb3_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.btn_add_all).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.tv_usb4_list).setVisibility(View.VISIBLE);

            initListView(R.id.tv_usb4_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_USB)) {
            mCurrentList = mUsb4TVList;
            // }
        } else if (mResPageId == R.id.btn_usb) {
            if (!mIs3PageSroll) {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
            }
            mMainView.findViewById(R.id.tv_local_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_sd2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.btn_add_all).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.tv_usb_list).setVisibility(View.VISIBLE);

            mMainView.findViewById(R.id.tv_usb2_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.tv_usb3_list).setVisibility(View.GONE);

            mMainView.findViewById(R.id.tv_usb4_list).setVisibility(View.GONE);
            initListView(R.id.tv_usb_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_USB)) {
            mCurrentList = mUsbTVList;
            // }
        } else if (mResPageId == R.id.btn_all) {
            if (!mIs3PageSroll) {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.VISIBLE);
                mMainView.findViewById(R.id.tv_local_list).setVisibility(View.GONE);
                mMainView.findViewById(R.id.tv_sd_list).setVisibility(View.GONE);
                mMainView.findViewById(R.id.tv_sd2_list).setVisibility(View.GONE);
                mMainView.findViewById(R.id.tv_usb_list).setVisibility(View.GONE);

                mMainView.findViewById(R.id.tv_usb2_list).setVisibility(View.GONE);
                mMainView.findViewById(R.id.tv_usb3_list).setVisibility(View.GONE);

                mMainView.findViewById(R.id.tv_usb4_list).setVisibility(View.GONE);
                mMainView.findViewById(R.id.btn_add_all).setVisibility(View.GONE);
            } else {
                //				if(MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef.getSystemUI())){
                //
                //				} else {
                mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
                //				}
            }
            // mHandler.sendMessageDelayed(
            // mHandler.obtainMessage(MSG_UPDATE_LIST, R.id.tv_all_list, 0), 0);
            initListView(R.id.tv_all_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_PLAYING)) {
            // mCurrentPlayPos = 0;
            mCurrentList = mPlayingTVList;
            // }
        }
    }

    private void initFocusChange() {
        if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef.getSystemUI())) {
            if (mDisplayIndex == 0) {
                for (int id : BUTTON_FOCUS_CHANGE_1200) {
                    View v = mMainView.findViewById(id);
                    if (v != null) {
                        v.setOnFocusChangeListener(mOnFocusChangeListener);
                    }
                }
            }
        }
    }

    private static final int[] BUTTON_FOCUS_CHANGE_1200 = new int[]{R.id.next, R.id.tv_all_list};
    boolean mPreFocusAllList = false;
    private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View arg0, boolean arg1) {

            // TODO Auto-generated method stub
            if (arg1) {

                int id = arg0.getId();
                if (id == R.id.next) {
                    mHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (mMyScrollView != null) {
                                if (mMyScrollView.getCurPage() != 1) {
                                    mMyScrollView.scrollToPage(1);
                                }
                            }

                        }
                    }, 10);
                } else if (id == R.id.tv_all_list) {
                    mPreFocusAllList = true;
                }
            } else {
                if (arg0.getId() == R.id.tv_all_list) {
                    mPreFocusAllList = false;
                }
            }
        }
    };
}
