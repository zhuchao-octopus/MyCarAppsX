package com.octopus.android.carapps.btmusic;

import java.util.ArrayList;

import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.UtilCarKey;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.adapter.BTMusicNode;
import com.octopus.android.carapps.adapter.MyListViewAdapterBTMusic;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.ui.UIBase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class BTMusicUI extends UIBase implements View.OnClickListener {

    public static final int SOURCE = MyCmd.SOURCE_BT_MUSIC;

    public static BTMusicUI[] mUI = new BTMusicUI[MAX_DISPLAY];

    public static BTMusicUI getInstanse(Context context, View view, int index) {
        if (index >= MAX_DISPLAY) {
            return null;
        }

        mUI[index] = new BTMusicUI(context, view, index);

        return mUI[index];
    }

    public BTMusicUI(Context context, View view, int index) {
        super(context, view, index);
        mSource = SOURCE;
    }

    private static final int[] BUTTON_ON_CLICK = new int[]{R.id.pp,
            R.id.prev, R.id.next, R.id.eq, R.id.bt, R.id.list, R.id.list_up,
            R.id.root};//, R.id.mic, R.id.vol};

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

    private void setPlayButtonStatus(boolean playing) {
        if (playing) {
            ((ImageView) mMainView.findViewById(R.id.pp)).getDrawable()
                    .setLevel(1);
        } else {
            ((ImageView) mMainView.findViewById(R.id.pp)).getDrawable()
                    .setLevel(0);
        }
    }

    private void updateConnectView() {
        View v
                = mMainView.findViewById(R.id.disconnect_info);
        if (v != null) {
            if (BTMusicService.mPlayStatus < BTMusicService.A2DP_INFO_CONNECTED) {


                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    String s = String.format(
                            mContext.getString(R.string.a2dp_disconnect_info),
                            BTMusicService.mBtName);

                    s += String.format(
                            mContext.getString(R.string.a2dp_disconnect_info2),
                            BTMusicService.mBtPin);
                    tv.setText(s);
                }

                v.setVisibility(View.VISIBLE);

                BTMusicService.mID3Name = "";
                BTMusicService.mID3Artist = "";
                BTMusicService.mID3Album = "";

                BTMusicService.mTotalTime = 0;
                BTMusicService.mCurTime = 0;
                updateView();
                updateTimeView();
                if (mTime != null) {
                    mTime.setVisibility(View.GONE);
                }

                View btList = mMainView.findViewById(R.id.list_layout);
                if (btList != null) {
                    if (btList.getVisibility() == View.VISIBLE) {
                        btList.setVisibility(View.GONE);
                        if (mListView != null) {

                            mListView.setAdapter(null);
                        }
                    }
                }

                if (mListPath != null) {
                    mListPath.clear();
                }
                if (mListPlaying != null) {
                    mListPlaying.clear();
                }
            } else {
                v.setVisibility(View.GONE);
                if (mTime != null) {
                    mTime.setVisibility(View.VISIBLE);
                }
                if (BTMusicService.mPlayStatus >= BTMusicService.A2DP_INFO_CONNECTED) {
                    // GlobalDef.reactiveSource(mContext, SOURCE,
                    // BTMusicService.mAudioFocusListener);
                }
            }
        }

        v = mMainView.findViewById(R.id.tv_devicename);
        if (v != null) {
            ((TextView) v).setText(BTMusicService.mBtName);
        }
        v = mMainView.findViewById(R.id.tv_pincode);
        if (v != null) {
            ((TextView) v).setText(BTMusicService.mBtPin);
        }

    }

    private void updateView() {

        if (mTitle != null && BTMusicService.mID3Name != null) {
            mTitle.setText(BTMusicService.mID3Name);
        }

        if (mTitle != null && BTMusicService.mID3Artist != null) {
            mArtist.setText(BTMusicService.mID3Artist);
        }

        if (mTitle != null && BTMusicService.mID3Album != null) {
            mAlbum.setText(BTMusicService.mID3Album);
        }

    }

    @SuppressLint("DefaultLocale")
    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds)
                    .toString();
        } else {
            return String.format("%02d:%02d", minutes, seconds).toString();
        }
        // return str;
    }

    @SuppressLint("SetTextI18n")
    private void updateTimeView() {

        if (mTime != null) {
            if (BTMusicService.mTotalTime > BTMusicService.mCurTime
                    && BTMusicService.mPlayStatus == BTMusicService.A2DP_INFO_PLAY) {
                mTime.setText(stringForTime(BTMusicService.mCurTime) + "/"
                        + stringForTime(BTMusicService.mTotalTime));
            }
        }
    }

    private TextView mTitle;
    private TextView mArtist;
    private TextView mAlbum;
    private TextView mTime;

    BTMusicService mServiceBase;

    public void onCreate() {
        super.onCreate();

        // setContentView(R.layout.screen0_bt_music);

        // mATBluetooth = ATBluetooth.create();

        initPresentationUI();

        mTitle = (TextView) mMainView.findViewById(R.id.song);
        mArtist = (TextView) mMainView.findViewById(R.id.artist);
        mAlbum = (TextView) mMainView.findViewById(R.id.album);
        mTime = (TextView) mMainView.findViewById(R.id.time);

        initBTType();

    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        if (mServiceBase == null) {
            mServiceBase = BTMusicService.getInstanse();
        }
        if (mServiceBase == null) {
            return;
        }
        int id = v.getId();
        if (id == R.id.prev) {
            mServiceBase.doKeyControl(MyCmd.Keycode.PREVIOUS);
            if (BTMusicService.mPlayStatus >= BTMusicService.A2DP_INFO_CONNECTED) {
                GlobalDef.reactiveSource(mContext, SOURCE,
                        BTMusicService.mAudioFocusListener);
            }
        } else if (id == R.id.pp) {
            if (BTMusicService.mPlayStatus == BTMusicService.A2DP_INFO_PLAY) {

                mServiceBase.doKeyControl(MyCmd.Keycode.PAUSE);
            } else {

                mServiceBase.doKeyControl(MyCmd.Keycode.PLAY);
                if (BTMusicService.mPlayStatus >= BTMusicService.A2DP_INFO_CONNECTED) {
                    GlobalDef.reactiveSource(mContext, SOURCE,
                            BTMusicService.mAudioFocusListener);
                }
            }
        } else if (id == R.id.next) {
            mServiceBase.doKeyControl(MyCmd.Keycode.NEXT);
            if (BTMusicService.mPlayStatus >= BTMusicService.A2DP_INFO_CONNECTED) {
                GlobalDef.reactiveSource(mContext, SOURCE,
                        BTMusicService.mAudioFocusListener);
            }
        } else if (id == R.id.eq) {
            try {
                Intent it = new Intent(mContext.getString(R.string.app_eq));
                mContext.startActivity(it);
            } catch (Exception e) {
//				Log.e(TAG, e.getMessage());
            }
        } else if (id == R.id.bt) {
            UtilCarKey.doKeyBT(mContext);
        } else if (id == R.id.list) {
            View btList = mMainView.findViewById(R.id.list_layout);
            if (btList != null) {
                showList(!(btList.getVisibility() == View.VISIBLE));
            }
        } else if (id == R.id.list_up) {
            doUpList();
        } else if (id == R.id.root) {
            sendListCmd(REQUEST_CMD_DSCD, "1,'/'");
        }
        /*else if (id == R.id.mic) {
            UtilCarKey.doKeyMic(mContext);
        } else if (id == R.id.vol) {
            Intent it = new Intent(MyCmd.BROADCAST_START_VOLUMESETTINGS_COMMON);
            it.setPackage("com.my.out");
            mContext.sendBroadcast(it);
        }*/

    }

    public boolean mWillDestory = false;

    @Override
    public void onPause() {
        super.onPause();
        if (mDisplayIndex == SCREEN0 && !mWillDestory) {
            updateFullUI();
        }

    }

    private int mPrePlayStatus;

    @Override
    public void onResume() {
        super.onResume();
        updateFullUI();

        BTMusicService.setHandler(mHandler, mDisplayIndex);
        // if (BTMusicService.mPlayStatus >= BTMusicService.A2DP_INFO_CONNECTED)
        // {
        GlobalDef.reactiveSource(mContext, SOURCE,
                BTMusicService.mAudioFocusListener);

        BroadcastUtil.sendToCarServiceSetSource(mContext, SOURCE);
        // }

        mPrePlayStatus = BTMusicService.mPlayStatus;
        if (BTMusicService.mPlayStatus >= BTMusicService.A2DP_INFO_CONNECTED &&
                BTMusicService.mPlayStatus != BTMusicService.A2DP_INFO_PLAY) {
            // Log.d("a", "doKeyControl(MyCmd.Keycode.PLAY);");
            if (mServiceBase == null) {
                mServiceBase = BTMusicService.getInstanse();
            }
            if (mServiceBase != null) {
                mServiceBase.doKeyControl(MyCmd.Keycode.PLAY);
            }
        }
        Intent it = new Intent(MyCmd.BROADCAST_CMD_TO_BT);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.BT_REQUEST_A2DP_INFO);

        mContext.sendBroadcast(it);

        if (mDisplayIndex == 0) {
            GlobalDef.setCurrentScreen0(this);
            GlobalDef.notifyUIScreen0Change(SOURCE, 1);
        }

        GlobalDef.requestEQInfo();
    }

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case MyCmd.Cmd.BT_SEND_A2DP_STATUS: {
                    updateConnectView();

                    // Log.d("a", mPrePlayStatus+":"+BTMusicService.mPlayStatus);

                    // if (BTMusicService.mPlayStatus !=
                    // BTMusicService.A2DP_INFO_PLAY&&mPrePlayStatus<BTMusicService.A2DP_INFO_CONNECTED)
                    // {
                    // BTMusicService.doKeyControl(MyCmd.Keycode.PLAY);
                    // }

                    setPlayButtonStatus(BTMusicService.mPlayStatus == BTMusicService.A2DP_INFO_PLAY);
                }

                break;
                case MyCmd.Cmd.BT_SEND_ID3_INFO: {
                    updateView();
                }

                break;
                case MyCmd.Cmd.BT_SEND_TIME_STATUS: {
                    updateTimeView();
                }

                break;
            }
            super.handleMessage(msg);
        }

    };

    // for playlist

    public static int mBTType = -1;

    private static void getBTType() {
        String s = MachineConfig.getProperty(MachineConfig.KEY_BT_TYPE);
        if (s != null) {
            try {
                mBTType = Integer.valueOf(s);
            } catch (Exception e) {
                mBTType = MachineConfig.VAULE_BT_TYPE_IVT;
            }
        }
    }

    private void initBTType() {
        View v = mMainView.findViewById(R.id.list);
        if (v != null) {
            if (mBTType == -1) {
                getBTType();
            }
            if (mBTType == MachineConfig.VAULE_BT_TYPE_PARROT) {
                v.setVisibility(View.VISIBLE);
                initListView();
            } else {
                v.setVisibility(View.GONE);
                v = mMainView.findViewById(R.id.bt_playing_list);
                if (v != null) {
                    v.setVisibility(View.GONE);
                }
            }
        }
    }

    private void initListView() {

        mListView = (ListView) mMainView.findViewById(R.id.bt_list);
        if (mListView != null) {
            mTextViewRoot = (TextView) mMainView.findViewById(R.id.list_root);
            mListView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View view,
                                        int postion, long id) {

                    BTMusicNode node = mListContent.get(postion);
                    if (node.flag == 0) {
                        sendListCmd(REQUEST_CMD_DSCD, (postion + 1) + "");
                    } else {
                        sendListCmd(REQUEST_BT_MUSIC_CPCC, "1," + (postion + 1));
                        showList(false);
                        updatePlayingList();
                        showPlayingList(true);
                    }

                }
            });
        }
        mListViewPLaying = (ListView) mMainView
                .findViewById(R.id.bt_playing_list);
        if (mListViewPLaying != null) {
            mListViewPLaying.setVisibility(View.GONE);
            mListViewPLaying.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View view,
                                        int postion, long id) {

                    BTMusicNode node = mListPlaying.get(postion);
                    if (node.flag == 0) {
                        sendListCmd(REQUEST_CMD_DSCD, (postion + 1) + "");
                    } else {
                        sendListCmd(REQUEST_BT_MUSIC_CPCC, "1," + (postion + 1));
                        // showList(false);
                    }

                }
            });
        }

    }

    public static final int REQUEST_CMD_DGBM = 0x9c;
    public static final int REQUEST_CMD_DSCD = 0x9d;
    public static final int REQUEST_CMD_DGCD = 0x9e;
    public static final int REQUEST_CMD_DGEC = 0x9f;
    public static final int REQUEST_CMD_DLSE = 0xa0;
    public static final int REQUEST_CMD_PBSCEX = 0xa1;
    public static final int REQUEST_CMD_DLPE = 0xa2;

    public static final int REQUEST_BT_MUSIC_CPCC = 0x93;

    private void showPlayingList(boolean show) {
        if (mListViewPLaying != null) {
            if (!show) {
                mListViewPLaying.setVisibility(View.GONE);
                if (mListPlaying != null) {
                    mListPlaying.clear();
                }

            } else {
                if (mListPlaying != null && mListPlaying.size() > 0) {
                    mListViewPLaying.setVisibility(View.VISIBLE);
                } else {
                    mListViewPLaying.setVisibility(View.GONE);
                }

            }
        }
    }

    private void updatePlayingList() {
        if (mListViewPLaying != null) {
            mListViewPLaying.setAdapter(null);

            if (mListContent != null) {
                if (mListPlaying == null) {
                    mListPlaying = new ArrayList<BTMusicNode>();
                }
                for (BTMusicNode node : mListContent) {
                    mListPlaying.add(node);
                }
                mAdapterPLaying = new MyListViewAdapterBTMusic(
                        mContext, R.layout.tl_list, mListPlaying);
                mListViewPLaying.setAdapter(mAdapterPLaying);
            }

        }

    }

    private void showList(boolean show) {
        View btList = mMainView.findViewById(R.id.list_layout);
        if (btList != null) {
            if (!show) {
                btList.setVisibility(View.GONE);
            } else {
                btList.setVisibility(View.VISIBLE);
                mMainView.findViewById(R.id.bt_list_updating).setVisibility(
                        View.GONE);
                sendListCmd(REQUEST_CMD_DGCD, null);
                // sendListCmd(REQUEST_CMD_DGEC, null);
            }
        }
    }

    private void sendListCmd(int cmd, String obj) {
        Intent it = new Intent(MyCmd.BROADCAST_CMD_TO_BT);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.BT_REQUEST_A2DP_LIST_INFO);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, cmd);
        if (obj != null) {
            it.putExtra(MyCmd.EXTRA_COMMON_DATA2, obj);
        }
        mContext.sendBroadcast(it);
    }

    private ListView mListView = null;
    private ListView mListViewPLaying = null;
    private TextView mTextViewRoot = null;
    private ArrayList<BTMusicNode> mListContent;
    private static ArrayList<BtListPathArrayList> mListPath;
    private static ArrayList<BTMusicNode> mListPlaying;
    MyListViewAdapterBTMusic mAdapterPLaying;

    public void updateList(Object obj) {
        if (mListView != null && obj != null) {
            if (mListContent == null) {
                mListContent = new ArrayList<BTMusicNode>();
            }
            ArrayList<String> list = (ArrayList<String>) obj;
            mListView.setAdapter(null);
            mListContent.clear();
            for (int i = 0; i < list.size(); ++i) {
                String[] ss = list.get(i).split("',");
                String[] ss2;// = ss[1].split(",'");
                if (ss.length > 1) {
                    ss2 = ss[1].split(",");
                } else {
                    ss2 = ss;
                }
                // int index;
                int flag = 1;
                int count = 0;
                try {
                    if (ss2.length > 0) {
                        flag = Integer.valueOf(ss2[0]);
                        if (ss2.length > 1) {
                            count = Integer.valueOf(ss2[1]);
                        }
                    }

                } catch (Exception e) {

                }

                BTMusicNode node = new BTMusicNode((mListContent.size() + 1)
                        + "," + ss[0].substring(1, ss[0].length()), flag, count);
                mListContent.add(node);
            }
            MyListViewAdapterBTMusic adapter = new MyListViewAdapterBTMusic(
                    mContext, R.layout.tl_list, mListContent);
            mListView.setAdapter(adapter);

            updateListPath(mCurPath, mListContent);
        }
    }

    private String mCurPath = null;

    private void updateListProcess(int pos, int total) {
        mMainView.findViewById(R.id.bt_list_updating).setVisibility(
                View.VISIBLE);
        ((TextView) mMainView.findViewById(R.id.bt_list_progress_text))
                .setText(pos + "/" + total);
    }

    public void updateListRoot(String root) {
        if (mTextViewRoot != null) {
            if (root == null) {
                root = "";
            }
            if (!root.equals(mCurPath)) {
                mCurPath = root;
                showPlayingList(false);
                ArrayList<BTMusicNode> l = getListByPath(mCurPath);
                if (l == null) {
                    sendListCmd(REQUEST_CMD_DGEC, null);
                } else {
                    if (mListContent == null) {
                        mListContent = new ArrayList<BTMusicNode>();
                    } else {
                        mListContent.clear();
                    }
                    for (BTMusicNode node : l) {
                        mListContent.add(node);
                    }
                    MyListViewAdapterBTMusic adapter = new MyListViewAdapterBTMusic(
                            mContext, R.layout.tl_list, mListContent);
                    mListView.setAdapter(adapter);
                }
            }

            mTextViewRoot.setText(root);
        }
    }

    public void updateList(Intent intent) {
        try {
            int status = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
            if (status == 0) {
                //Object s = intent.getExtra(MyCmd.EXTRA_COMMON_DATA2);
                //mMainView.findViewById(R.id.bt_list_updating).setVisibility(View.GONE);
                //updateList(s);

            } else if (status > 0) {
                int total = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA2, 0);
                if (total > 2) {
                    updateListProcess(status, total);
                }
            }
        } catch (Exception e) {

        }
    }

    private void doUpList() {
        sendListCmd(REQUEST_CMD_DSCD, "0");
        // String root = mTextViewRoot.getText().toString();
        // if (root != null && root.length() > 0) {
        // String[] ss = root.split("/");
        // if (ss.length > 1) {
        // root = root.substring(0, root.lastIndexOf("/"));
        // sendListCmd(REQUEST_CMD_DSCD, "1,'/"+root+"/'");
        // }
        // }
    }

    private static void updateListPath(String path, ArrayList<BTMusicNode> lp) {
        if (path == null || path.length() <= 0) {
            return;
        }

        ArrayList<BTMusicNode> l = getListByPath(path);
        if (l == null) {
            if (mListPath == null) {
                mListPath = new ArrayList<BtListPathArrayList>();
            }

            BtListPathArrayList node = new BtListPathArrayList(path, lp);
            mListPath.add(node);

        }
    }

    private static ArrayList<BTMusicNode> getListByPath(String path) {
        if (mListPath != null) {
            for (BtListPathArrayList node : mListPath) {
                if (node.mPath.equals(path)) {
                    return node.mList;
                }
            }
        }
        return null;
    }
}
