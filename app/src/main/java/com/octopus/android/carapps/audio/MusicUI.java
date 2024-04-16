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
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.media.audiofx.Visualizer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.adapter.MyListViewAdapter;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.player.ComMediaPlayer;
import com.octopus.android.carapps.common.player.MusicPlayer;
import com.octopus.android.carapps.common.ui.UIBase;
import com.octopus.android.carapps.common.view.MusicRollImage;
import com.octopus.android.carapps.common.view.MyScrollView;
import com.octopus.android.carapps.qylk.lrc.LRCbean;
import com.octopus.android.carapps.qylk.lrc.LrcParser;
import com.octopus.android.carapps.qylk.myview.LrcPackage;
import com.octopus.android.carapps.qylk.myview.LrcView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This activity plays a video from a specified URI.
 */
public class MusicUI extends UIBase implements View.OnClickListener {

    private final static String TAG = "MusicUI";

    private static final int MSG_FIRST_RUN = 1;
    private static final int MSG_UPDATE_SEEK_BAR = 2;
    private static final int MSG_SHOW_PROC = 4;
    private static final int MSG_SET_PROCESS = 5;
    private static final int MSG_SET_RUN_BEFORE_LAUNCHER = 9;
    private SeekBar mProgress;
    private TextView mCurrentTime;
    private TextView mEndTime;

    private TextView mTitle;
    private TextView mArtist;
    private TextView mAlbum;
    private TextView mMediaPath;

    private LrcView mLrcview;
    private TextView mNoLyric;

    public static ProgressDialog mDialogScan;
    public static int mStatus = 0;

    private boolean mIs3PageSroll = false;
    public static final int SOURCE = MyCmd.SOURCE_MUSIC;

    private static final MusicUI[] mUI = new MusicUI[MAX_DISPLAY];

    public static MusicUI getInstance(Context context, View view, int index) {
        if (index >= MAX_DISPLAY) {
            return null;
        }

        mUI[index] = new MusicUI(context, view, index);

        return mUI[index];
    }

    public static MusicUI getInstance(int index) {
        if (index >= MAX_DISPLAY) {
            return null;
        }
        return mUI[index];
    }

    public MusicUI(Context context, View view, int index) {
        super(context, view, index);
        mSource = SOURCE;
    }

    private static final int[] BUTTON_ON_CLICK = new int[]{
            R.id.back, R.id.prev, R.id.pp, R.id.next, R.id.repeat, R.id.eq, R.id.btn_all, R.id.btn_local, R.id.btn_sd, R.id.btn_usb, R.id.btn_usb2, R.id.btn_usb3, R.id.btn_usb4, R.id.btn_sd2,
            R.id.btn_add_all, R.id.repeat2, R.id.btn_folder, R.id.album_and_visualizer_view, R.id.list, R.id.eq_mode_switch, R.id.id3, R.id.folder_switch, R.id.play_repeat_tag
    };//,R.id.mic,R.id.vol };

    private static final int[][] VIEW_HIDE = new int[][]{
            //{ R.id.common_status_bar_main, R.id.layout_visulizerView },
            {R.id.common_status_bar_main}, {R.id.eq}
    };

    private static final int[] VIEW_HIDE2 = new int[]{
            R.id.bottom_button_layout, R.id.list_content, R.id.seek_bar_layout, R.id.tb
    };

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
                mUI[1].checkIfNeedUpdatePlayingList();
                mUI[1].updateFullUI();

            }
        }
    }

    private boolean checkOneTime = false;

    private void checkIfNeedUpdatePlayingList() {// only display1 show
        if (checkOneTime) {
            return;
        }

        checkOneTime = false;
        // if (mResPageId == R.id.btn_all) {
        ArrayList<String> list = mMediaPlayer.getStrList(MusicPlayer.LIST_PLAYING, -1);
        if (list.size() > 0) {
            MyListViewAdapter ap = (MyListViewAdapter) mPlayingTVList.getAdapter();
            if (ap == null || ap.getCount() != list.size()) {
                // setPage(R.id.btn_all);
                // mCurrentList = mPlayingTVList;

                // if (mPlayingTVList.getAdapter() == null) {
                updateListViewAdapter(mPlayingTVList, list, 0);
                mCreate = false;
            }
            // }
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
            mDialogScan = null;
        }
    }

    private void initDialogScan() {
        mDialogScan = new ProgressDialog(mContext);
        mDialogScan.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialogScan.setMessage(mContext.getString(R.string.scanning));
        mDialogScan.setIndeterminate(true);
        mDialogScan.setCancelable(true);
        mDialogScan.setCanceledOnTouchOutside(true);

        mDialogScan.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                hideDialogScan();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {

        super.onCreate();
        mCreate = true;
        mMediaPlayer = com.octopus.android.carapps.audio.MediaPlaybackService.getMediaPlayer();
        mMediaPlayer.setOpeningFile(mMainView.findViewById(R.id.progressBar1));
        mProgress = (SeekBar) mMainView.findViewById(R.id.progress);

        if (mProgress != null) {
            mProgress.setOnSeekBarChangeListener(mSeekListener);
            mProgress.setMax(1000);
            mProgress.setOnTouchListener(new OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent me) {
                    if (mMyScrollView != null) {
                        @SuppressLint("ClickableViewAccessibility") int action = me.getActionMasked();
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

        mEndTime = (TextView) mMainView.findViewById(R.id.totaltime);
        mCurrentTime = (TextView) mMainView.findViewById(R.id.currenttime);

        mTitle = (TextView) mMainView.findViewById(R.id.song);
        mArtist = (TextView) mMainView.findViewById(R.id.artist);
        mAlbum = (TextView) mMainView.findViewById(R.id.album);

        mMediaPath = (TextView) mMainView.findViewById(R.id.media_path);

        mViewFolderListviewSame = mMainView.findViewById(R.id.folder_listview_same_layout);

        mAlbumArt = (ImageView) mMainView.findViewById(R.id.album_art);
        //		mAlbumArt1 = (ImageView) mMainView.findViewById(R.id.album_art1);
        //		mAlbumArt2 = (ImageView) mMainView.findViewById(R.id.album_art2);
        mVisualizerView = (VisualizerView) mMainView.findViewById(R.id.visualizer_view);

        // mHandler.sendMessage(mHandler.obtainMessage(MSG_FIRST_RUN, 0, 1000));
        View v = mMainView.findViewById(R.id.music_page_scroll_view);
        if (v != null) {
            mIs3PageSroll = true;
            mMyScrollView = (MyScrollView) v;
            mMyScrollView.setCallback(mICallBack);

        }
        initPresentationUI();

        initFocusChange();
        // registerListener();
        initLyc();
    }

    private int lrcViewHeight = -1;

    private void initLyc() {
        View v = mMainView.findViewById(R.id.lrcshow);
        if (v != null) {
            mLrcview = (LrcView) v;

            int lines = mMainView.getResources().getInteger(R.integer.lrc_lines);
            if (lines == 3) { // for line3
                mLrcview.setLines(lines, mMainView.getResources().getInteger(R.integer.lrc_margin_top));
                ViewTreeObserver vto = mLrcview.getViewTreeObserver();
                vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        if (lrcViewHeight <= 0) {
                            lrcViewHeight = mLrcview.getMeasuredHeight();
                            // Log.d(TAG,"height=" + lrcViewHeight + " , lines="
                            // + mLrcview.lines);
                            if (mLrcview.lines == 3) {
                                if (lrcViewHeight > 0) {
                                    int h = (lrcViewHeight - mLrcview.marginTop) / 3;
                                    float curline_scaling = (float) mMainView.getResources().getInteger(R.integer.lrc_curline_scaling) / 100.0f;
                                    float otherline_scaling = (float) mMainView.getResources().getInteger(R.integer.lrc_otherline_scaling) / 100.0f;
                                    mLrcview.setLrcTextSizePixel((int) (h * curline_scaling), true);
                                    mLrcview.setLrcTextSizePixel((int) (h * otherline_scaling), false);
                                    int curlineColor = mMainView.getResources().getInteger(R.integer.lrc_curline_color);
                                    int otherlineColor = mMainView.getResources().getInteger(R.integer.lrc_otherline_color);
                                    mLrcview.setLcrTextPrevNextColor(Color.rgb(otherlineColor >> 16 & 0xff, otherlineColor >> 8 & 0xff, otherlineColor & 0xff));
                                    mLrcview.setFirstColor(Color.rgb(curlineColor >> 16 & 0xff, curlineColor >> 8 & 0xff, curlineColor & 0xff));
                                }
                            }
                        }
                        return true; // true:continue draw(), false: will
                        // remeasure()、layout()...
                    }
                });
            }
            int size = mContext.getResources().getInteger(R.integer.lrc_text_size);
            mLrcview.setLrcTextSize(size);
            mLrcview.setFirstColor(Color.rgb(0xff, 0xff, 0xff));
            mLrcview.setShadow(false);
            mLrcview.setVisibility(View.INVISIBLE);
            mNoLyric = (TextView) mMainView.findViewById(R.id.tv_no_lyric);
            if (MachineConfig.VALUE_SYSTEM_UI21_RM12.equals(GlobalDef.getSystemUI())) {
                mID3View = ID3_LRC;
            } else if (MachineConfig.VALUE_SYSTEM_UI45_8702_2.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI43_3300_1.equals(GlobalDef.getSystemUI())) {
                ID3_NUM = 3;
                mID3View = mMediaPlayer.getData(SAVE_DATA_ID3VIEW);
            } else {
                mID3View = mMediaPlayer.getData(SAVE_DATA_ID3VIEW);
            }
            updateID3View();
        }
    }

    private int mID3View = 0;
    private final static int ID3_BASE = 0;
    private final static int ID3_LRC = 1;
    private final static int ID3_VIS = 2;

    private int ID3_NUM = 2;

    private void updateID3View() {
        int id = 0;
        switch (mID3View) {
            case ID3_BASE:
                if (mLrcview != null) {
                    mLrcview.setVisibility(View.GONE);
                    setViewVisible(mMainView, R.id.id3_artist_album, View.VISIBLE);
                    setViewVisible(mMainView, R.id.visualizer_view, View.GONE);
                    id = R.drawable.ico_artist_album;
                }
                break;
            case ID3_LRC:
                if (mLrcview != null) {
                    mLrcview.setVisibility(View.VISIBLE);
                    setViewVisible(mMainView, R.id.id3_artist_album, View.INVISIBLE);
                    id = R.drawable.ico_lrc;
                    setViewVisible(mMainView, R.id.visualizer_view, View.GONE);
                }
                break;
            case ID3_VIS:
                if (mLrcview != null) {
                    mLrcview.setVisibility(View.GONE);
                    setViewVisible(mMainView, R.id.id3_artist_album, View.INVISIBLE);
                    setViewVisible(mMainView, R.id.visualizer_view, View.VISIBLE);
                    id = R.drawable.ico_vis;
                }

                break;
        }

        if (id != 0) {
            View v = mMainView.findViewById(R.id.id3);
            if (v != null) {
                ((ImageView) v).setImageResource(id);
            }
        }
    }

    private void toggleID3() {
        mID3View = (mID3View + 1) % ID3_NUM;
        mMediaPlayer.saveData(SAVE_DATA_ID3VIEW, mID3View);
        updateID3View();
    }

    MyScrollView mMyScrollView;
    private int mPreStoragePage = -1;
    MyScrollView.ICallBack mICallBack = new MyScrollView.ICallBack() {
        public void callback(int cmd, int visible_page) {

            if (cmd == MyScrollView.CMD_NEWPAGE) {
                Log.d("ccde", "" + visible_page);
                if (visible_page == 0) {
                    if (mPreStoragePage == -1) {
                        mPreStoragePage = getPlayingIdToPage();// R.id.btn_local;
                    }

                    doSetPage(mPreStoragePage);

                } else if (visible_page == 2) {
                    doSetPage(R.id.btn_all);
                    mMainView.findViewById(R.id.tv_all_list).setVisibility(View.VISIBLE);
                } else if (visible_page == 1) {
                    if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI43_3300_1.equals(GlobalDef.getSystemUI())) {
                        doSetPage(R.id.btn_all);

                        if (scrollToPlayingPage) {
                            scrollToPlayingPage = false;
                            if (mContext instanceof Activity) {
                                View vf = ((Activity) mContext).getCurrentFocus();
                                if (vf != null) {
                                    mMainView.findViewById(R.id.tv_all_list).requestFocus();
                                }
                            }
                        }
                    }
                }
            }
        }

        ;
    };

    private void initListView(int id) {
        //

        if (id == R.id.tv_all_list) {
            if (mPlayingTVList == null) {
                mPlayingTVList = (ListView) mMainView.findViewById(R.id.tv_all_list);
                mPlayingTVList.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> arg0, View view, int postion, long id) {
                        GlobalDef.reactiveSource(mContext, SOURCE, com.octopus.android.carapps.audio.MediaPlaybackService.mAudioFocusListener);
                        mMediaPlayer.play(postion);
                    }
                });
            }

            if (mCurrentList != null) {
                if ((mCurrentList.getAdapter() == null || !mMediaPlayer.equalCurrentPlaylist(((MyListViewAdapter) mCurrentList.getAdapter()).getList()))) {
                    updateListViewAdapter(mPlayingTVList, mMediaPlayer.getStrList(MusicPlayer.LIST_PLAYING, -1), 0);
                    mCurrentList = mPlayingTVList;
                    updateView();
                }
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
                                toPlayingIfFolderUI();
                            }
                        }
                    });
                }
            } else {
                mMediaPlayer.getStrList(MusicPlayer.LIST_LOCAL, ((MyListViewAdapter) mLocalTVList.getAdapter()).getFolderSet());
                ((MyListViewAdapter) mLocalTVList.getAdapter()).clearSelectItem();
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
                                toPlayingIfFolderUI();
                            }
                        }
                    });
                }
            } else {
                mMediaPlayer.getStrList(MusicPlayer.LIST_USB, ((MyListViewAdapter) mUsbTVList.getAdapter()).getFolderSet());
                ((MyListViewAdapter) mUsbTVList.getAdapter()).clearSelectItem();
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
                                toPlayingIfFolderUI();
                            }
                        }
                    });
                }
            } else {
                mMediaPlayer.getStrList(MusicPlayer.LIST_USB2, ((MyListViewAdapter) mUsb2TVList.getAdapter()).getFolderSet());
                ((MyListViewAdapter) mUsb2TVList.getAdapter()).clearSelectItem();
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
                                toPlayingIfFolderUI();
                            }
                        }
                    });
                }
            } else {
                mMediaPlayer.getStrList(MusicPlayer.LIST_USB3, ((MyListViewAdapter) mUsb3TVList.getAdapter()).getFolderSet());
                ((MyListViewAdapter) mUsb3TVList.getAdapter()).clearSelectItem();
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
                                toPlayingIfFolderUI();
                            }
                        }
                    });
                }
            } else {
                mMediaPlayer.getStrList(MusicPlayer.LIST_USB4, ((MyListViewAdapter) mUsb4TVList.getAdapter()).getFolderSet());
                ((MyListViewAdapter) mUsb4TVList.getAdapter()).clearSelectItem();
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
                                toPlayingIfFolderUI();
                            }
                        }
                    });
                }
            } else {
                mMediaPlayer.getStrList(MusicPlayer.LIST_SD, ((MyListViewAdapter) mSDTVList.getAdapter()).getFolderSet());
                ((MyListViewAdapter) mSDTVList.getAdapter()).clearSelectItem();
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
                                toPlayingIfFolderUI();
                            }
                        }
                    });
                }
            } else {
                mMediaPlayer.getStrList(MusicPlayer.LIST_SD2, ((MyListViewAdapter) mSD2TVList.getAdapter()).getFolderSet());
                ((MyListViewAdapter) mSD2TVList.getAdapter()).clearSelectItem();
            }
        } else if (id == R.id.tv_all_folder_list) {
            if (mAllFoderList == null) {
                ArrayList<String> list = mMediaPlayer.getStrList(MusicPlayer.LIST_ALL_FOLDER, -1);
                if (list.size() > 0) {
                    mAllFoderList = (ListView) mMainView.findViewById(R.id.tv_all_folder_list);

                    updateListViewAdapter(mAllFoderList, list, mMediaPlayer.getStrFolderNum(MusicPlayer.LIST_ALL_FOLDER));

                    mAllFoderList.setOnItemClickListener(new OnItemClickListener() {
                        public void onItemClick(AdapterView<?> arg0, View view, int postion, long id) {
                            int folderNum = ((MyListViewAdapter) arg0.getAdapter()).getFolderNum();
                            if (postion < folderNum) {
                                clickFolder(mAllFoderList, MusicPlayer.LIST_ALL_FOLDER, postion, ((MyListViewAdapter) arg0.getAdapter()).getFolderLevel());
                            } else {
                                play(postion - folderNum);

                                toPlayingIfFolderUI();
                            }
                        }
                    });
                }
            } else {
                mMediaPlayer.getStrList(MusicPlayer.LIST_ALL_FOLDER, ((MyListViewAdapter) mAllFoderList.getAdapter()).getFolderSet());
                ((MyListViewAdapter) mAllFoderList.getAdapter()).clearSelectItem();
            }
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

            if (MachineConfig.VALUE_SYSTEM_UI45_8702_2.equals(GlobalDef.getSystemUI())) {
                if (mPlayingTVList == lv) {
                    id = R.layout.tl_list_music_playing;
                }

            }

            MyListViewAdapter ad = new MyListViewAdapter(mContext, id, list, folder);
            lv.setAdapter(ad);

            if (MachineConfig.VALUE_SYSTEM_UI45_8702_2.equals(GlobalDef.getSystemUI())) {
                ArrayList<String> al = mMediaPlayer.getId3Time();
                ad.updateTimeTextId(R.id.list_text_time);
                ad.updateId3Time(al);
                ad.setPlayingType(1);
                ad.setSelectTextColor(mContext.getResources().getColor(R.color.list_normal_colre));
            }

        }
    }

    private void clickFolder(ListView lv, int id, int index, int level) {
        if (level == 1) {
            updateListViewAdapter(lv, mMediaPlayer.getStrList(id, index), -1);
            ((MyListViewAdapter) lv.getAdapter()).setFolderSet(index);
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
        }

        public void onStopTrackingTouch(SeekBar bar) {

            // Log.d("aa", "onStopTrackingTouch");
        }
    };

    public static String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (MachineConfig.VALUE_SYSTEM_UI45_8702_2.equals(GlobalDef.getSystemUI())) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            if (hours > 0) {
                return String.format("%d:%02d:%02d", hours, minutes, seconds).toString();
            } else {
                return String.format("%02d:%02d", minutes, seconds).toString();
            }
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

    public boolean mGpsRunAfter = false;

    private void doMemoryPlay() {
        hideDialogScan();

        Log.d("MusicPlayer", "1doMemoryPlay:" + mMediaPlayer.isInitialized());
        if (mMediaPlayer.isInitialized()) {
            mMediaPlayer.resetPlayStatus();
        } else {
            // if (MoviePlayer.mFirstRun) {
            // MoviePlayer.mFirstRun = false;

            // Log.e("allen", "doMemoryPlay:" + GlobalDef.mSource);
            // if(GlobalDef.mSource == MyCmd.SOURCE_NONE || GlobalDef.mSource !=
            // SOURCE){
            // Util.doSleep(350); //wait carservice return source.
            // Log.e("allen", "222doMemoryPlay:" + GlobalDef.mSource);
            // }
            Log.d("abd", mMediaPlayer.mSaveForSleepPath + "doMemoryPlay:" + mFirstPowerOn);
            if (Util.isRKSystem() && mMediaPlayer.mSaveForSleepPath != null) {
                if (mFirstPowerOn == 1) {
                    mFirstPowerOn = 0;
                    return;
                } else if (mFirstPowerOn == 3) {
                    mFirstPowerOn = 0;
                    File f = new File(mMediaPlayer.mSaveForSleepPath);
                    // Log.d("abd", "doMemoryPlay f.exists():"+f.exists());
                    if (!f.exists()) {
                        return;
                    }
                }
            }
            if ((GlobalDef.mSourceWillUpdate == SOURCE || GlobalDef.mSource == SOURCE || GlobalDef.mSource == MyCmd.SOURCE_NONE) && (!mPause || mGpsRunAfter)) {
                mMediaPlayer.playCurrentMemory();
                mGpsRunAfter = false;
            }

            // } else {
            // mMediaPlayer.resetPlayStatus();
            // }
        }

        if (mDisplayIndex == 0) {
            if (mCreate) {
                mCreate = false;
                setPage(R.id.btn_all);
                mCurrentList = mPlayingTVList;

                // if (mPlayingTVList.getAdapter() == null) {
                updateListViewAdapter(mPlayingTVList, mMediaPlayer.getStrList(MusicPlayer.LIST_PLAYING, -1), 0);
                // }
            }
        } else {
            int id = -1;
            if (mUI[0] != null) {
                id = mUI[0].mResPageId;
            }
            if (id == -1) {
                id = R.id.btn_all;
            }
            setPage(id);
            // mCurrentList = mPlayingTVList;

            // if (mPlayingTVList.getAdapter() == null) {
            updateListViewAdapter(mPlayingTVList, mMediaPlayer.getStrList(MusicPlayer.LIST_PLAYING, -1), 0);
        }
        // Log.d("dd", "doMemoryPlay");
        updateView();
    }

    private final static boolean mIsAsync = true;

    private void doFirstRun() {
        if (mIsAsync) {
            if (mUpdateAsyncTask != null) {
                mUpdateAsyncTask.cancel(true);
                mUpdateAsyncTask = null;
            }
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
        ArrayList<String> list = mMediaPlayer.getStrListEx(MusicPlayer.LIST_PLAYING, -1);
        Log.d("MusicPlayer", "doFirstRunAsync:" + list.size());
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

    }

    private void stoptProcessBar() {
        mHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case MSG_FIRST_RUN: {
                    if (mMediaPlayer != null) {
                        doFirstRun();
                    }
                }
                break;
                case MSG_UPDATE_SEEK_BAR: {
                    startProcessBar();
                }
                break;
                case MSG_SET_RUN_BEFORE_LAUNCHER:
                    break;
                case MSG_SET_PROCESS:
                    setProcess(mProgress);
                    break;
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
    private ListView mAllFoderList = null;
    private ListView mCurrentList = null;

    private TextView mPageSeleteButton = null;
    private MusicPlayer mMediaPlayer;

    private int mResPageId = -1;

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

    private int mStoragePage = 0;

    private void doSetPage(int id) {
        if (mFolderType == FOLDER_TYPE_FOLDER) {
            setViewVisible(mMainView, R.id.folder_listview_same_layout, View.GONE);
            setViewVisible(mMainView, R.id.list_content, View.VISIBLE);
        }
        if (id == mResPageId) {
            return;
        }
        if (mIs3PageSroll && id != R.id.btn_all) {
            mPreStoragePage = id;
        }

        if (id != R.id.btn_all) {
            mStoragePage = id;
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
                if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI43_3300_1.equals(GlobalDef.getSystemUI())) {

                } else {
                    mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
                }
            }

            if (GlobalDef.mAudioUIType == GlobalDef.AUDIO_TYPE_ALL_FODER) {
                setViewVisible(mMainView, R.id.tv_all_folder_list, View.GONE);
                setViewVisible(mMainView, R.id.back_to_up, View.GONE);
            }
            // mHandler.sendMessageDelayed(
            // mHandler.obtainMessage(MSG_UPDATE_LIST, R.id.tv_all_list, 0), 0);
            initListView(R.id.tv_all_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_PLAYING)) {
            // mCurrentPlayPos = 0;
            mCurrentList = mPlayingTVList;
            // }
        } else if (id == R.id.btn_folder) {
            setViewVisible(mMainView, R.id.tv_all_list, View.GONE);
            setViewVisible(mMainView, R.id.tv_all_folder_list, View.VISIBLE);
            setViewVisible(mMainView, R.id.back_to_up, View.VISIBLE);

            initListView(R.id.tv_all_folder_list);

            mCurrentList = mAllFoderList;
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

            if (mPageSeleteButton != null) {
                // mPageSeleteButton.setCompoundDrawables(this.getResources().getDrawable(R.id),
                // null, null, null);
                d = mPageSeleteButton.getCompoundDrawables();
                if (d[0] != null) {
                    d[0].setLevel(0);
                } else if (d[1] != null) {
                    d[1].setLevel(0);
                }
            }

            mPageSeleteButton = tv;
            mResPageId = id;

            hideFolderUI();
        }
    }

    private void updatePlayStatus() {
        if (mMediaPlayer != null) {
            setPlayButtonStatus(mMediaPlayer.isPlaying());
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

    private static final int VISUALIZER_TYPE_ALBUMART = 0;
    private static final int VISUALIZER_TYPE_COLUMN = 1;
    private static final int VISUALIZER_TYPE_WAVEFORM = 2;
    private static final int VISUALIZER_TYPE_FFT = 3;

    private Visualizer mVisualizer;

    private ImageView mAlbumArt;
    //	private ImageView mAlbumArt1;
    //	private ImageView mAlbumArt2;
    private VisualizerView mVisualizerView;

    private int mAudioSessionId;
    private int mVisualizerType = VISUALIZER_TYPE_COLUMN;

    private void updateVisualizerView() {
        if (mVisualizerView == null) {
            return;
        }
        if (MachineConfig.VALUE_SYSTEM_UI45_8702_2.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI43_3300_1.equals(GlobalDef.getSystemUI())) {
            mVisualizerView.setColumnStyle();
        } else {
            if (VISUALIZER_TYPE_ALBUMART == mVisualizerType) {
                mAlbumArt.setVisibility(View.VISIBLE);
                mVisualizerView.setVisibility(View.INVISIBLE);
            } else {
                mAlbumArt.setVisibility(View.GONE);
                mVisualizerView.setVisibility(View.VISIBLE);
            }
            switch (mVisualizerType) {
                case VISUALIZER_TYPE_COLUMN:
                    mVisualizerView.setColumnStyle();
                    break;
                case VISUALIZER_TYPE_WAVEFORM:
                    mVisualizerView.setWaveStyle();
                    break;
                case VISUALIZER_TYPE_FFT:
                    mVisualizerView.setFFTStyle();
                    break;
                case VISUALIZER_TYPE_ALBUMART:
                    break;
            }
        }
    }

    private void releaseVisualizer() {
        if (mVisualizer != null) {
            mVisualizer.release();
            mVisualizer = null;
            mAudioSessionId = -1;
        }
    }

    private void resetVisualizer() {
        if (mVisualizerView == null) {
            return;
        }

        if (mMediaPlayer == null) {
            if (mVisualizer != null) {
                mVisualizer.release();
                mVisualizer = null;
                mAudioSessionId = -1;
            }
            return;
        }
        try {
            if (mMediaPlayer.isPlaying()) {

                if (mMediaPlayer.getAudioSessionId() == -1 || mAudioSessionId == mMediaPlayer.getAudioSessionId()) return;

                mAudioSessionId = mMediaPlayer.getAudioSessionId();

                if (mVisualizer != null) {
                    mVisualizer.release();
                    mVisualizer = null;
                }
                mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
                mVisualizer.setEnabled(false);
                // if (mVisualizerView != null) {
                // mVisualizerView.setVisualizer(mVisualizer);
                // }
                mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
                mVisualizer.setDataCaptureListener(mVisualizerView, Visualizer.getMaxCaptureRate() / 2, true, true);
                mVisualizer.setEnabled(true);
            }
        } catch (UnsupportedOperationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RuntimeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void updateArt() {
        Bitmap bm = mMediaPlayer.getArtwork();
        if ((mAlbumArt instanceof MusicRollImage)) {
            if (bm != null) {
                bm = GlobalDef.toRoundBitmap(bm);
                mAlbumArt.setImageBitmap(bm);
                mAlbumArt.getDrawable().setDither(true);
                mAlbumArt.setScaleType(ImageView.ScaleType.CENTER_CROP);

                // mAlbumArt.setScaleType(ImageView.ScaleType.CENTER);
                // mAlbumArt.setVisibility(View.VISIBLE);
            } else {
                mAlbumArt.setImageDrawable(null);
                // mAlbumArt.setVisibility(View.GONE);
                //				mAlbumArt.setImageDrawable(mContext.getResources().getDrawable(
                //						R.drawable.music_pic));
                //				mAlbumArt.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
        } else {
            if (bm != null) {
                bm = createReflectedImage(bm, 80);
                mAlbumArt.setImageBitmap(bm);
                mAlbumArt.getDrawable().setDither(true);
                mAlbumArt.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                // mAlbumArt.setScaleType(ImageView.ScaleType.CENTER);
                // mAlbumArt.setVisibility(View.VISIBLE);
            } else {
                // mAlbumArt.setVisibility(View.GONE);
                mAlbumArt.setImageDrawable(mContext.getResources().getDrawable(R.drawable.music_pic));
                mAlbumArt.setScaleType(ImageView.ScaleType.CENTER);
            }
        }
    }

    private void updateView() {

        updateStorageView();
        if (mCurrentList != null && mMediaPlayer.getCurPosition() >= 0 && mMediaPlayer.getCurPosition() < mCurrentList.getCount() && mCurrentList.getAdapter() != null) {

            int playPos = mMediaPlayer.getCurPosition() + ((MyListViewAdapter) mCurrentList.getAdapter()).getFolderNum();
            if (mMediaPlayer.equalPreparePlaylist() || mCurrentList == mPlayingTVList) {
                ((MyListViewAdapter) mCurrentList.getAdapter()).setSelectItem(playPos);

                if (((mCurrentList.getLastVisiblePosition()) < playPos) || (mCurrentList.getFirstVisiblePosition() > playPos)) {
                    mCurrentList.setSelection(playPos);
                }
            }
        }

        if (!GlobalDef.isOneSleepRemountTime()) {
            mMediaPlayer.doCheckSleepOnStorage();
        }

        if (mMediaPlayer != null) {
            // Log.d("allen", mMediaPlayer.getTrackName()+":"+mDisplayIndex);
            mTitle.setText(mMediaPlayer.getTrackName());
            mArtist.setText(mMediaPlayer.getArtistName());
            if (mAlbum != null) {
                mAlbum.setText(mMediaPlayer.getAlbumName());
            }
            setMediaPath();

            setPlayRepeat(mMediaPlayer.getRepeatMode());

            updateArt();
            resetVisualizer();
            mVisualizerType = mMediaPlayer.getVisualizerStyle(mDisplayIndex);
            updateVisualizerView();

        }

        View v = mMainView.findViewById(R.id.playing_files);
        if (v != null) {
            String s = "";
            if (mMediaPlayer != null && mMediaPlayer.isInitialized() && mMediaPlayer.getCurSongNum() > 0) {
                s = (1 + mMediaPlayer.getCurPosition()) + "/" + mMediaPlayer.getCurSongNum();
            }

            ((TextView) v).setText(s);
        }

        loadLRC();
    }

    private int mLRCLoad = 0;

    private void loadLRC() {
        if (mLrcview != null) {
            boolean lrc = false;

            lrc = false;
            if (mID3View == ID3_LRC) {
                File f;
                if (mMediaPlayer != null) {
                    String TrackPath = mMediaPlayer.getPath();
                    if (TrackPath != null) {
                        String LrcPath = TrackPath.replaceAll(".{3}$", "lrc");
                        f = new File(LrcPath);
                        if (f.exists()) {
                            lrc = true;

                            List<LRCbean> lrclist = LrcParser.ParseLrc(LrcPath);
                            if (lrclist != null) {
                                mLrcview.setLyric(new LrcPackage(lrclist, (int) mMediaPlayer.getDuration()));
                                mLrcview.initLrcIndex((int) mMediaPlayer.getCurrentPosition());
                                mLrcview.setVisibility(View.VISIBLE);
                                mNoLyric.setVisibility(View.GONE);
                            } else {
                                mLrcview.clearView();
                                mLrcview.setVisibility(View.GONE);
                                mNoLyric.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }

            if (!lrc) {
                mLrcview.clearView();
                mLrcview.setVisibility(View.GONE);
                mNoLyric.setVisibility(View.VISIBLE);
            }

            if (mID3View != ID3_LRC) {
                mLRCLoad = 0;
            } else {
                if (lrc) {
                    mLRCLoad = 1;
                } else {
                    mLRCLoad = -1;
                }
            }
        }
    }

    private void setMediaPath() {
        if (mMediaPath != null) {
            String s = mMediaPlayer.getPath();
            if (s != null) {
                s = s.substring(1, s.length());
                String path = s.substring(s.indexOf("/"), s.lastIndexOf("/"));

                mMediaPath.setText(path);

                int drawable_id = 0;
                if (Util.isRKSystem()) {
                    if (path.contains("USB")) {
                        drawable_id = R.drawable.storage_type_usb_normal;
                    } else if (path.contains("Card")) {
                        drawable_id = R.drawable.storage_type_sd_normal;
                    } else {
                        drawable_id = R.drawable.storage_type_arm_normal;
                    }
                    if (drawable_id != 0) {
                        Drawable d = mContext.getResources().getDrawable(drawable_id);
                        if (d != null) {
                            ImageView iv = (ImageView) mMainView.findViewById(R.id.iv_media_path);
                            if (iv != null) {
                                iv.setImageDrawable(d);
                            }
                        }
                    }
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
            position = mMediaPlayer.getCurrentPosition();
            duration = mMediaPlayer.getDuration();
            if (mProgress != null && position >= 0 && duration > 0
                /* && position <= duration */) {
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
            if (mLrcview != null) {
                if (mLrcview.getVisibility() == View.VISIBLE) {
                    if (mLRCLoad == 0) {
                        loadLRC();
                    }
                    if (mLRCLoad == 1) {
                        try {
                            mLrcview.initLrcIndex(position);
                        } catch (Exception e) {

                        }
                    } else {
                        // mLrcview.clearView();
                    }
                }
            }
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
        } else {
            ((ImageView) mMainView.findViewById(R.id.pp)).getDrawable().setLevel(0);
        }

        if (mAlbumArt instanceof MusicRollImage) {
            MusicRollImage new_name = (MusicRollImage) mAlbumArt;
            if (playing) {
                if (GlobalDef.mReverseStatus != 1) {
                    new_name.play();
                }
            } else {
                new_name.pause();
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.back) {
            if (SCREEN0 == mDisplayIndex) {

                ((Activity) mContext).finish();

                BroadcastUtil.sendToCarServiceSetSource(mContext, MyCmd.SOURCE_AV_OFF);

            } else {
                // dismiss();
            }
        } else if (id == R.id.home) {
            mContext.startActivity(new Intent(Intent.ACTION_MAIN).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP).addCategory(Intent.CATEGORY_HOME));
        } else if (id == R.id.btn_all || id == R.id.btn_local || id == R.id.btn_sd || id == R.id.btn_sd2 || id == R.id.btn_usb || id == R.id.btn_usb2 || id == R.id.btn_usb3 || id == R.id.btn_usb4) {
            setPageforce(id);
        } else if (id == R.id.eq_mode_switch) {
            GlobalDef.swtichEQMode();
        } else if (id == R.id.btn_add_all) {
            boolean ret = addAllFilePlay(resourceIdToPage(mResPageId));
            if (ret && mMyScrollView != null) {
                scrollToPlayingPage = true;
                mMyScrollView.scrollToPage(1);
            }
        } else if (id == R.id.pp) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            } else {
                mMediaPlayer.play();
                GlobalDef.reactiveSource(mContext, SOURCE, MediaPlaybackService.mAudioFocusListener);
            }
            // case R.id.shuffle: {
            // if (mMediaPlayer.pressShuffleMode() == MusicPlayer.SHUFFLE_NONE) {
            // ((ImageView) view).getDrawable().setLevel(0);
            // } else {
            // ((ImageView) view).getDrawable().setLevel(1);
            // }
            // }
            // break;
        } else if (id == R.id.play_repeat_tag || id == R.id.repeat) {
            int r = mMediaPlayer.pressRepeatMode();

            // setPlayRepeat(r);
        } else if (id == R.id.repeat2) {
            int r = mMediaPlayer.pressRepeatMode2();

            // setPlayRepeat(r);
        } else if (id == R.id.id3) {
            toggleID3();
        } else if (id == R.id.list) {
            if (mMyScrollView != null) {
                mMyScrollView.scrollToPage(0);
            }
        } else if (id == R.id.prev) {
            mMediaPlayer.prev();
            GlobalDef.reactiveSource(mContext, SOURCE, MediaPlaybackService.mAudioFocusListener);
            // updateView();
        } else if (id == R.id.next) {
            mMediaPlayer.next();
            GlobalDef.reactiveSource(mContext, SOURCE, MediaPlaybackService.mAudioFocusListener);
            // updateView();
        } else if (id == R.id.cd_eq_button || id == R.id.eq) {
            try {
                Intent it = new Intent("com.eqset.intent.action.EQActivity");
                mContext.startActivity(it);
            } catch (Exception e) {

            }
        } else if (id == R.id.btn_folder) {
            toggleFolderView();
        } else if (id == R.id.album_and_visualizer_view) {
            if (Util.isNexellSystem60()) { // 7.1 8.0 is not ok ,to do?
                if (mVisualizerType == VISUALIZER_TYPE_FFT) {
                    mVisualizerType = VISUALIZER_TYPE_ALBUMART;
                } else {
                    ++mVisualizerType;
                }
                if (mMediaPlayer != null) {
                    mMediaPlayer.setVisualizerStyle(mDisplayIndex, mVisualizerType);
                }
                updateVisualizerView();
            }
        } else if (id == R.id.folder_switch) {
            doFolderSwitch();
        }
		/*else if (id == R.id.mic) {
            UtilCarKey.doKeyMic(mContext);
        } else if (id == R.id.vol) {
            Intent it = new Intent(MyCmd.BROADCAST_START_VOLUMESETTINGS_COMMON);
            it.setPackage("com.my.out");
            mContext.sendBroadcast(it);
        }*/
    }

    private int mUpdateNewActivityPage = 0;

    private void updateNewActivity(int page) {

        mUpdateNewActivityPage = page;
        if (mMyScrollView != null) {
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    mMyScrollView.scrollToPage(1);
                }
            }, 300);
        }

        mHandler.post(new Runnable() {
            public void run() {
                Log.d("MusicPlayer", "updateNewActivity mSleepRestore:" + mMediaPlayer.mSleepRestore);
                if (mMediaPlayer.mSleepRestore) {
                    mMediaPlayer.mSleepRestore = false;
                    Log.d("MusicPlayer", "updateNewActivity mMemoryPlayFromSleep:" + mMediaPlayer.mMemoryPlayFromSleep);
                    if (mMediaPlayer.mMemoryPlayFromSleep == 1) {
                        updateMountFromSleep(mUpdateNewActivityPage);
                    } else {
                        updateStorageView();
                    }
                } else {
                    mResPageId = -1;
                    mPreStoragePage = -1;
                    if (mUpdateNewActivityPage != 0) {
                        addAllFilePlay(mUpdateNewActivityPage);
                        mUpdateNewActivityPage = 0;
                    }
                }
            }
        });
    }

    private void updateMountFromSleep(int page) {

        setPage(R.id.btn_all);
        mMediaPlayer.checkPlayingListIsExistFromSleep();
        ArrayList<String> list = mMediaPlayer.getStrList(MusicPlayer.LIST_PLAYING, -1);
        Log.d("MusicPlayer", "updateMountFromSleep:" + list.size());

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

            GlobalDef.reactiveSource(mContext, SOURCE, MediaPlaybackService.mAudioFocusListener);
            mMediaPlayer.play(0);

            mCurrentList = mPlayingTVList;

            updateListViewAdapter(mPlayingTVList, mMediaPlayer.getStrList(MusicPlayer.LIST_PLAYING, -1), 0);
        }
        return ret;
    }

    private void play(int id) {
        if (!mMediaPlayer.equalPreparePlaylist()) {
            mMediaPlayer.updateCurrentUriList();
        }
        GlobalDef.reactiveSource(mContext, SOURCE, MediaPlaybackService.mAudioFocusListener);
        mMediaPlayer.play(id);
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

    // private int pageToResourceId(int page) {
    // int r = 0;
    // switch (page) {
    // case MusicPlayer.LIST_USB:
    // r = R.id.btn_usb;
    // break;
    // case MusicPlayer.LIST_USB2:
    // r = R.id.btn_usb2;
    // break;
    // case MusicPlayer.LIST_USB3:
    // r = R.id.btn_usb3;
    // break;
    // case MusicPlayer.LIST_SD:
    // r = R.id.btn_sd;
    // break;
    // case MusicPlayer.LIST_SD2:
    // r = R.id.btn_sd2;
    // break;
    // case MusicPlayer.LIST_LOCAL:
    // r = R.id.btn_local;
    // break;
    // // default:
    // case MusicPlayer.LIST_PLAYING:
    // r = R.id.btn_all;
    // break;
    // }
    // return r;
    // }

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
                    try {
                        Drawable d[] = mPageSeleteButton.getCompoundDrawables();
                        d[0].setLevel(0);
                    } catch (Exception e) {

                    }
                    mPageSeleteButton = null;
                }

                break;
        }
    }

    public boolean mWillDestory = false;

    @Override
    public void onPause() {
        super.onPause();
        if (mDisplayIndex == SCREEN0) {
            if (!mWillDestory) {
                updateFullUI();
            }
            clearQueryFlag();
        }

        MediaPlaybackService.setUICallBack(null, mDisplayIndex);

        stoptProcessBar();
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

    public int mStartActivityNewPage = 0;
    public int mFirstPowerOn = 0;

    @Override
    public void onResume() {
        super.onResume();
        mStoragePage = 0;
        mMediaPlayer.setShowBePlay(true);
        updateFullUI();
        updatePlayStatus();
        // mMediaPlayer = MusicPlayer.getThis();

        // Log.d("dd", "onResume");

        GlobalDef.reactiveSource(mContext, SOURCE, MediaPlaybackService.mAudioFocusListener);
        MediaPlaybackService.setUICallBack(mIMediaCallBack, mDisplayIndex);
        BroadcastUtil.sendToCarServiceSetSource(mContext, SOURCE);
        // if (mMediaPlayer != null) {
        //
        // mMediaPlayer.resetPlayStatus();
        // }

        if (mDisplayIndex == 1) {
            if (mUI[0] != null) {
                doSetPage(mUI[0].mResPageId);
            }

            if (mUI[0] == null || mUI[0].mPause) {
                mHandler.sendEmptyMessageDelayed(MSG_FIRST_RUN, 1);
            } else {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {

                    setPage(R.id.btn_all);
                    mCurrentList = mPlayingTVList;

                    updateListViewAdapter(mPlayingTVList, mMediaPlayer.getStrList(MusicPlayer.LIST_PLAYING, -1), 0);

                    // updateView();
                }
            }

        } else {
            Log.d("MusicPlayer", mStartActivityNewPage + "zzzz" + mFirstPowerOn + ":" + mMediaPlayer);
            if (mStartActivityNewPage == 0) {
                int delay = 50;
                if (Util.isRKSystem()) {
                    if (mFirstPowerOn == 2 && mMediaPlayer != null) {
                        int page = mMediaPlayer.getData(ComMediaPlayer.SAVE_DATA_PAGE);
                        // Log.d("MusicPlayer", page+"zzzz");
                        if (page == ComMediaPlayer.LIST_USB3 || page == ComMediaPlayer.LIST_USB4) {
                            delay = 1800;
                            // Log.d("MusicPlayer", delay+"zzzz!!!!!!!");
                        }
                    }
                }
                mHandler.sendEmptyMessageDelayed(MSG_FIRST_RUN, delay);
            } else {
                updateNewActivity(mStartActivityNewPage);
                mStartActivityNewPage = 0;
            }

            GlobalDef.setCurrentScreen0(this);
            GlobalDef.notifyUIScreen0Change(SOURCE, 1);

        }

        startProcessBar();

        updateView();

        GlobalDef.requestEQInfo();
    }

    private static String SAVE_DATA_ID3VIEW = "audio_cur_id3_view";
    // @Override
    // public void dismiss() {
    // super.dismiss();
    // if (mDisplayIndex == SCREEN0) {
    // if (!(mPresentationUI[1] != null && !mPresentationUI[1].isShowing())) {
    // onDestroy();
    // }
    // } else {
    // if (!(mPresentationUI[0] != null && !mPresentationUI[0].mPause)) {
    // onDestroy();
    // }
    // BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.SCREEN1_HIDE,
    // MusicUI.SOURCE);
    // }
    // }
    private boolean mCreate = false;

    @Override
    public void onDestroy() {
        releaseVisualizer();
        // MediaPlaybackService.setUICallBack(null, mDisplayIndex);
    }

    // ////////////////////////////////////////

    private Handler mIMediaCallBack = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, msg.what + ":" + msg.arg1);
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
                    break;
                case ComMediaPlayer.PLAY_REPEAT:
                    setPlayRepeat(mMediaPlayer.getRepeatMode());
                    break;
                case MusicPlayer.STORAGE_MOUNTED: {
                    Log.d("MusicPlayer", "STORAGE_MOUNTED" + msg.arg1);
                    updateNewActivity(msg.arg1);

                    if (msg.arg1 == 0) {
                        updateStorageView();
                    }
                    // updateView();

                }
                break;
                case MusicPlayer.STORAGE_MOUNTED_NOMEDIA:
                    updateStorageView();
                    break;
                case MusicPlayer.STORAGE_MOUNTED_FROM_SLEEP: {

                    updateNewActivity(msg.arg1);
                    // if (msg.arg1 == 0) {
                    updateStorageView();
                    // }
                    // updateView();

                }
                break;
                case MusicPlayer.KEY:
                    doKey((int) msg.obj);
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

                case ComMediaPlayer.ID3_TIME_CHANGE:

                    if (MachineConfig.VALUE_SYSTEM_UI45_8702_2.equals(GlobalDef.getSystemUI())) {
                        if (mPlayingTVList != null) {
                            MyListViewAdapter ad = (MyListViewAdapter) mPlayingTVList.getAdapter();
                            ArrayList<String> al = mMediaPlayer.getId3Time();
                            ad.updateId3Time(al);
                        }
                    }

                    break;
                case MSG_HIDE_MYTOAST:
                    hideMyToast();
                    break;
                case GlobalDef.MSG_UPDATE_EQ_MODE:
                    GlobalDef.updateEQModeButton(mMainView, R.id.eq_mode_switch);
                    break;
                case GlobalDef.MSG_REVERSE_COME:
                    Log.d("ffck", "MSG_REVERSE_COME");
                    if (mAlbumArt instanceof MusicRollImage) {
                        MusicRollImage new_name = (MusicRollImage) mAlbumArt;
                        if (msg.arg1 != 1) {
                            new_name.play();
                        } else {
                            new_name.pause();
                        }
                    }
                    break;
            }

        }
    };

    private void doKey(int key) {
        switch (key) {
            case MyCmd.Keycode.CH_UP:

                break;
            case MyCmd.Keycode.CH_DOWN:
                break;
        }
    }

    // add by allen

    private Bitmap skewImage(Bitmap paramBitmap, int paramInt) {
        Bitmap localBitmap1 = paramBitmap;// createReflectedImage(paramBitmap,
        // paramInt);
        Camera localCamera = new Camera();
        localCamera.save();
        Matrix localMatrix = new Matrix();
        localCamera.rotateY(-15.0F);
        localCamera.getMatrix(localMatrix);
        localCamera.restore();
        localMatrix.preTranslate(-localBitmap1.getWidth() >> 1, -localBitmap1.getHeight() >> 1);
        Bitmap localBitmap2 = Bitmap.createBitmap(localBitmap1, 0, 0, localBitmap1.getWidth(), localBitmap1.getHeight(), localMatrix, true);
        Bitmap localBitmap3 = Bitmap.createBitmap(localBitmap2.getWidth(), localBitmap2.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas localCanvas = new Canvas(localBitmap3);
        Paint localPaint = new Paint();
        localPaint.setAntiAlias(true);
        localPaint.setFilterBitmap(true);
        localCanvas.drawBitmap(localBitmap2, 0.0F, 0.0F, localPaint);
        localBitmap2.recycle();
        return localBitmap3;
    }

    private Bitmap createReflectedImage(Bitmap paramBitmap, int paramInt) {
        Bitmap localBitmap2 = null;
        try {
            int i = paramBitmap.getWidth();
            int j = paramBitmap.getHeight();
            Matrix localMatrix = new Matrix();
            localMatrix.preScale(1.0F, -1.0F);
            Bitmap localBitmap1 = Bitmap.createBitmap(paramBitmap, 0, (j - paramInt) > 0 ? (j - paramInt) : 0, i, paramInt, localMatrix, false);
            localBitmap2 = Bitmap.createBitmap(i, j + paramInt, Bitmap.Config.ARGB_8888);
            Canvas localCanvas = new Canvas(localBitmap2);
            Paint localPaint1 = new Paint();
            localCanvas.drawBitmap(paramBitmap, 0.0F, 0.0F, localPaint1);
            localCanvas.drawBitmap(localBitmap1, 0.0F, j, localPaint1);
            Paint localPaint2 = new Paint();
            localPaint2.setShader(new LinearGradient(0.0F, paramBitmap.getHeight(), 0.0F, localBitmap2.getHeight(), 1895825407, 16777215, Shader.TileMode.MIRROR));
            localPaint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            localCanvas.drawRect(0.0F, j, i, localBitmap2.getHeight(), localPaint2);
            localBitmap1.recycle();
        } catch (Exception e) {

        }
        if (localBitmap2 == null) {
            return paramBitmap;
        }
        return localBitmap2;
    }

    private Bitmap drawableToBitamp(Drawable drawable) {
        Bitmap bitmap;
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        System.out.println("Drawable转Bitmap");
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        bitmap = Bitmap.createBitmap(w, h, config);
        // 注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    Animation animation;

    // void test() {
    // animation = (AnimationSet) AnimationUtils.loadAnimation(mContext,
    // R.anim.mic_rotate);
    //
    // Drawable d = mContext.getResources().getDrawable(R.drawable.music_pic);
    // Bitmap b = drawableToBitamp(d);
    // ImageView v = (ImageView) this.mMainView.findViewById(R.id.album_art);
    // b = toRoundBitmap(b);
    // v.setImageBitmap(b);
    //
    // LinearInterpolator lir = new LinearInterpolator();
    // animation.setInterpolator(lir);
    // v.setAnimation(animation);
    //
    // animation.start();
    // }

    private final static int MSG_HIDE_MYTOAST = 1000000;
    private View mTextViewToast;
    private RelativeLayout.LayoutParams mToastParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

    private void showMyToast(String s) {
        if (mTextViewToast == null) {
            LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //mTextViewToast = inflate.inflate(com.android.internal.R.layout.transient_notification, null);
            mToastParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            // mTextViewToast = new TextView(mContext);
        }

        //TextView tv = (TextView) mTextViewToast.findViewById(com.android.internal.R.id.message);
        //if (tv != null) {
        //	tv.setText(s);
        //}
        // Log.d("allen1", mTextViewToast.getParent() + "");
        if (mTextViewToast.getParent() == null) {
            try {
                if (mMainView instanceof RelativeLayout) {
                    RelativeLayout new_name = (RelativeLayout) mMainView;
                    new_name.addView(mTextViewToast, mToastParams);
                }
            } catch (Exception e) {

            }
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

    // new ui
    private void setViewVisible(View mainView, int id, int visibility) {
        View v = mainView.findViewById(id);
        if (v != null) {
            v.setVisibility(visibility);
        }
    }

    private void toggleFolderView() {
        if (R.id.btn_all == mResPageId) {
            doSetPage(R.id.btn_folder);
        } else {
            doSetPage(R.id.btn_all);
        }

    }

    // just for px5 .gps after sleep boot
    public void autoPlayGpsRunAfterSleep(int status, Object obj) {
        Log.d("abd", "autoPlayGpsRunAfterSleep!!");
        mIMediaCallBack.sendMessageDelayed(mIMediaCallBack.obtainMessage(MusicPlayer.STORAGE_MOUNTED, status, 0, obj), 20);

        mGpsRunAfter = false;

    }

    private int mFolderType = -1;
    private int mPreFolderType = -1;
    private final static int FOLDER_TYPE_FOLDER = 0;
    private final static int FOLDER_TYPE_FILES = 1;
    private final static int FOLDER_TYPE_PLAYING = 2;

    private View mViewFolderListviewSame = null;

    private void doFolderSwitch() {
        // mFolderType = (mFolderType+1)%3;

        switch (mFolderType) {
            case FOLDER_TYPE_FOLDER:
                mPreFolderType = mFolderType;
                mFolderType = FOLDER_TYPE_FILES;
                break;
            case FOLDER_TYPE_FILES:
                if (mPreFolderType == FOLDER_TYPE_FOLDER) {
                    mFolderType = FOLDER_TYPE_PLAYING;
                } else {
                    mFolderType = FOLDER_TYPE_FOLDER;
                }
                break;
            case FOLDER_TYPE_PLAYING:
                mPreFolderType = mFolderType;
                mFolderType = FOLDER_TYPE_FILES;
                break;
        }

        updateFolderUI();
    }

    private void toPlayingIfFolderUI() {
        if (mViewFolderListviewSame != null) {
            doSetPage(R.id.btn_all);
        }
        scrollToPlayingPage();
    }

    private void hideFolderUI() {
        if (mViewFolderListviewSame != null) {

            if (mResPageId == R.id.btn_all) {
                mFolderType = FOLDER_TYPE_PLAYING;
            } else {
                mFolderType = FOLDER_TYPE_FILES;
            }

            setViewVisible(mMainView, R.id.folder_listview_same_layout, View.GONE);
            setViewVisible(mMainView, R.id.list_content, View.VISIBLE);
        }
    }

    private void updateFolderUI() {
        if (mViewFolderListviewSame != null) {

            switch (mFolderType) {
                case FOLDER_TYPE_FOLDER:
                    setViewVisible(mMainView, R.id.folder_listview_same_layout, View.VISIBLE);
                    setViewVisible(mMainView, R.id.list_content, View.GONE);
                    // mResPageId= -1;
                    break;
                case FOLDER_TYPE_FILES:
                    setViewVisible(mMainView, R.id.folder_listview_same_layout, View.GONE);
                    setViewVisible(mMainView, R.id.list_content, View.VISIBLE);
                    if (mStoragePage == 0) {
                        mStoragePage = getPlayingIdToPage();
                    }
                    setPageforce(mStoragePage);

                    break;
                case FOLDER_TYPE_PLAYING:
                    setViewVisible(mMainView, R.id.folder_listview_same_layout, View.GONE);
                    setViewVisible(mMainView, R.id.list_content, View.VISIBLE);
                    doSetPage(R.id.btn_all);
                    break;
            }
        }
    }

    private boolean scrollToPlayingPage = false;

    private void scrollToPlayingPage() {
        if (mMyScrollView != null) {
            scrollToPlayingPage = true;
            mMyScrollView.scrollToPage(1);
            View v = ((Activity) mContext).getCurrentFocus();
            Log.d("cc", "ddd!!!" + v);
            if (v != null) {
                v.clearFocus();
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

        if (mStoragePage == R.id.btn_local) {
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
        } else if (mStoragePage == R.id.btn_sd) {
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
        } else if (mStoragePage == R.id.btn_sd2) {
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
        } else if (mStoragePage == R.id.btn_usb2) {
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
        } else if (mStoragePage == R.id.btn_usb3) {
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
        } else if (mStoragePage == R.id.btn_usb4) {
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
        } else if (mStoragePage == R.id.btn_usb) {
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
        } else if (mStoragePage == R.id.btn_all) {
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
                if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI43_3300_1.equals(GlobalDef.getSystemUI())) {

                } else {
                    mMainView.findViewById(R.id.tv_all_list).setVisibility(View.GONE);
                }
            }

            if (GlobalDef.mAudioUIType == GlobalDef.AUDIO_TYPE_ALL_FODER) {
                setViewVisible(mMainView, R.id.tv_all_folder_list, View.GONE);
                setViewVisible(mMainView, R.id.back_to_up, View.GONE);
            }
            // mHandler.sendMessageDelayed(
            // mHandler.obtainMessage(MSG_UPDATE_LIST, R.id.tv_all_list, 0), 0);
            initListView(R.id.tv_all_list);
            // if (mMediaPlayer.setList(MusicPlayer.LIST_PLAYING)) {
            // mCurrentPlayPos = 0;
            mCurrentList = mPlayingTVList;
            // }
        } else if (mStoragePage == R.id.btn_folder) {
            setViewVisible(mMainView, R.id.tv_all_list, View.GONE);
            setViewVisible(mMainView, R.id.tv_all_folder_list, View.VISIBLE);
            setViewVisible(mMainView, R.id.back_to_up, View.VISIBLE);

            initListView(R.id.tv_all_folder_list);

            mCurrentList = mAllFoderList;
        }
    }

    // private static final int[] BUTTON_ON_FOCUS_CHANGE = new int[] {
    // R.id.channel_00, R.id.channel_01, R.id.channel_02, R.id.channel_03,
    // R.id.channel_04, R.id.channel_05, R.id.channel_06, R.id.channel_07,
    // R.id.channel_08, R.id.channel_09, R.id.channel_10, R.id.channel_11,
    // R.id.channel_12, R.id.channel_13, R.id.channel_14, R.id.channel_15,
    // R.id.channel_16, R.id.channel_17, R.id.channel_18, R.id.channel_19,
    // R.id.channel_20, R.id.channel_21, R.id.channel_22, R.id.channel_23,
    // R.id.channel_24, R.id.channel_25, R.id.channel_26, R.id.channel_27,
    // R.id.channel_28, R.id.channel_29, };

    private void initFocusChange() {
        // if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef
        // .getSystemUI())||
        // MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef
        // .getSystemUI())) {
        // if (mDisplayIndex == 0) {
        // for (int id : BUTTON_ON_CLICK) {
        // View v = mMainView.findViewById(id);
        // if (v != null) {
        // v.setOnFocusChangeListener(mOnFocusChangeListener);
        // }
        // }
        // }
        // }
    }

    // private static final int[] BUTTON_ON_CLICK = new int[] { R.id.back,
    // R.id.prev, R.id.pp, R.id.next, R.id.repeat, R.id.eq, R.id.btn_all,
    // R.id.btn_local, R.id.btn_sd, R.id.btn_usb, R.id.btn_usb2,
    // R.id.btn_usb3, R.id.btn_usb4, R.id.btn_sd2, R.id.btn_add_all,
    // R.id.repeat2, R.id.btn_folder,
    // R.id.album_and_visualizer_view, R.id.list, R.id.eq_mode_switch,R.id.id3,
    // R.id.folder_switch, R.id.play_repeat_tag};
    // private OnFocusChangeListener mOnFocusChangeListener = new
    // OnFocusChangeListener() {
    // @Override
    // public void onFocusChange(View arg0, boolean arg1) {
    // // TODO Auto-generated method stub
    // int page = 1;
    // Log.d("abcd", "onFocusChange:"+arg0);
    // switch (arg0.getId()) {
    // case R.id.btn_sd:
    // case R.id.btn_local:
    // case R.id.btn_sd2:
    // case R.id.btn_usb:
    // case R.id.btn_usb2:
    // case R.id.btn_usb3:
    // case R.id.btn_usb4:
    // case R.id.btn_add_all:
    // page = 0;
    // break;
    // }
    // if (mMyScrollView != null) {
    //
    // mMyScrollView.scrollToPage(page);
    //
    // }
    // }
    // };
    private String mAddExternalFile = null;

    private void doAddExternalFiles() {
        int i = mMediaPlayer.addExternalFiles(mAddExternalFile);
        mMediaPlayer.play(i & 0x3fffffff);
        mAddExternalFile = null;

        if ((i & 0x40000000) != 0) {
            setPage(R.id.btn_all);
            mCurrentList = mPlayingTVList;
            updateListViewAdapter(mPlayingTVList, mMediaPlayer.getStrListEx(MusicPlayer.LIST_PLAYING, -1), 0);
        }
    }

    public void addExternalFiles(String files) {
        // Log.d(TAG, "addExternalFiles:" + files);
        File f = new File(files);
        if (!f.exists()) {
            Toast.makeText(mContext, mContext.getString(R.string.file_not_found) + " " + files, Toast.LENGTH_LONG).show();
        } else {
            mAddExternalFile = files;
            if (!mPause) {
                doAddExternalFiles();
            }
        }
    }
}
