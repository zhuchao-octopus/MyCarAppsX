package com.octopus.android.carapps.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.common.presentation.PresentationAlertDialog;
import com.octopus.android.carapps.common.ui.UIBase;

public class SettingsUI extends UIBase implements View.OnClickListener {

    private final static String TAG = "SettingsUI";
    public static final int SOURCE = MyCmd.SOURCE_BT_MUSIC;

    public static SettingsUI[] mUI = new SettingsUI[MAX_DISPLAY];

    public static SettingsUI getInstanse(Context context, View view, int index) {
        if (index >= MAX_DISPLAY) {
            return null;
        }

        mUI[index] = new SettingsUI(context, view, index);

        return mUI[index];
    }

    public SettingsUI(Context context, View view, int index) {
        super(context, view, index);
    }

    private static final int[] BUTTON_ON_CLICK = new int[]{
            R.id.screen_size_title, R.id.switch_screen, R.id.screen_view_select, R.id.wallpaper_instructions, R.id.backlight_screen1
    };

    // private static final int[][] VIEW_HIDE = new int[][] {
    // {0 },
    // { R.id.eq } };


    private static final int[] VIEW_TYPE0 = new int[]{
            R.id.wallpaper_instructions, R.id.screen_view_select
    };

    private static final int[] VIEW_TYPE1 = new int[]{
            R.id.screen_size_title,
            /*R.id.switch_screen*/
    };


    private void initPresentationUI() {
        for (int i : BUTTON_ON_CLICK) {
            View v = mMainView.findViewById(i);
            if (v != null) {
                v.setOnClickListener(this);
            }
        }

    }

    public int mType = 1;

    public void onCreate() {
        super.onCreate();

        initPresentationUI();

        int[] hide = null;
        switch (mType) {
            case 0:
                hide = VIEW_TYPE0;
                break;
            case 1:
                hide = VIEW_TYPE1;
                break;
        }
        if (hide != null) {
            for (int i = 0; i < hide.length; ++i) {
                View v = mMainView.findViewById(hide[i]);
                if (v != null) {
                    v.setVisibility(View.GONE);
                }
            }
        }

        if (mDisplayIndex == 0) {
            View v = mMainView.findViewById(R.id.common_status_bar_main);
            if (v != null) {
                v.setVisibility(View.GONE);
            }
        } else {
            View v = mMainView.findViewById(R.id.wallpaper_instructions);
            if (v != null) {
                v.setVisibility(View.GONE);
            }
        }
    }

    public void udpateWallPaper() {
        mMainView.setBackgroundColor(0xff303030);
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.screen_size_title) {
            showScreenDialog();
        } else if (id == R.id.screen_view_select) {
            showScreenViewDialog();
        } else if (id == R.id.switch_screen) {
            showScreenSwitchDialog();
        } else if (id == R.id.wallpaper_instructions) {
            try {
                Intent it = new Intent(Intent.ACTION_VIEW);
                it.setClassName("com.car.ui", "com.my.wallpaper.WallpaperChooser");

                it.putExtra("type", 1);
                it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(it);
            } catch (Exception e) {
                Log.e(TAG, "" + e);
            }
        } else if (id == R.id.backlight_screen1) {
            Intent it = new Intent(MyCmd.BROADCAST_START_BACKLIGHTSETTINGS);
            it.putExtra(MyCmd.EXTRA_COMMON_CMD, -1);
            mContext.sendBroadcast(it);
        }
    }

    private final static String SCREEN_SWITCH = "/sys/class/ak/source/disp_inv";

    private void showScreenSwitchDialog() {

        if (mDisplayIndex == 0) {
            new AlertDialog.Builder(mContext).setTitle(R.string.if_reboot).setPositiveButton(android.R.string.ok, mClickListenerSwitch).setNegativeButton(android.R.string.cancel, null).show();
        } else {
			/*new PresentationAlertDialog.Builder(mContext)
					.setTitle(R.string.if_reboot)
					.setPositiveButton(android.R.string.ok,
							mClickListenerSwitch)
					.setNegativeButton(android.R.string.cancel, null).show();*/
        }

    }

    private final DialogInterface.OnClickListener mClickListenerSwitch = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.APP_REQUEST_SEND_KEY, MyCmd.Keycode.POWER);

            mHandler.sendEmptyMessageDelayed(0, 4000);
            //			Util.doSleep(4000);

            //			int i = Util.getFileValue(SCREEN_SWITCH);
            //			if (i == 0) {
            //				i = 1;
            //			} else {
            //				i = 0;
            //			}
            //
            //			Util.setFileValue(SCREEN_SWITCH, i);
        }
    };


    private void updateScreenSize() {

        String s = MachineConfig.getProperty(MachineConfig.KEY_SCREEN1_W);
        if ("800".equals(s)) {
            mSelectScreenSize = 1;
        } else if ("1600".equals(s)) {
            mSelectScreenSize = 2;
        } else {
            mSelectScreenSize = 0;
        }

        String[] array = mContext.getResources().getStringArray(R.array.screen_size_select);
        ((TextView) mMainView.findViewById(R.id.screen_size_value)).setText(array[mSelectScreenSize]);

    }

    private void setScreenSize(int index) {
        String w = "1024";
        String h = "600";
        switch (index) {
            case 1:
                w = "800";
                h = "480";
                break;
            case 2:
                w = "1600";
                h = "480";
                break;
        }
        MachineConfig.setProperty(MachineConfig.KEY_SCREEN1_W, w);
        MachineConfig.setProperty(MachineConfig.KEY_SCREEN1_H, h);

        reboot();
    }

    private void reboot() {
        BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.APP_REQUEST_SEND_KEY, MyCmd.Keycode.POWER);
        Util.doSleep(4000);
        Util.sudoExec("reboot");
    }

    private int mSelectScreenSize = 0;

    private void showScreenDialog() {
        // String []array = mContext.getResources().getStringArray(id);
        Dialog d = new AlertDialog.Builder(mContext)

                .setTitle(R.string.screen_size_title).setSingleChoiceItems(R.array.screen_size_select, mSelectScreenSize, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mSelectScreenSize != which) {
                            mSelectScreenSize = which;

                            Dialog d = new AlertDialog.Builder(mContext)

                                    .setTitle(R.string.if_reboot).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            setScreenSize(mSelectScreenSize);

                                        }
                                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            updateScreenSize();
                                        }
                                    }).show();
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton(android.R.string.cancel, null).show();
    }

    private void showScreenViewDialog() {
        // String []array = mContext.getResources().getStringArray(id);
        for (int i = 0; i < mCheckedItems.length; ++i) {
            mCheckedItemsTemp[i] = mCheckedItems[i];
        }

        if (mDisplayIndex == 0) {
            new AlertDialog.Builder(mContext)

                    .setTitle(R.string.screen_view_select).setMultiChoiceItems(R.array.screen1_view_select, mCheckedItemsTemp, new OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean b) {
                            // if (which < mCheckedItemsTemp.length) {
                            // mCheckedItemsTemp[which] = b;
                            // }
                        }
                    }).setNegativeButton(android.R.string.cancel, null).setPositiveButton(android.R.string.ok, mClickListenerScreenview

                    ).show();
        } else {
            new PresentationAlertDialog.Builder(mContext)

                    .setTitle(R.string.screen_view_select).setMultiChoiceItems(R.array.screen1_view_select, mCheckedItemsTemp, new OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean b) {
                            // if (which < mCheckedItemsTemp.length) {
                            // mCheckedItemsTemp[which] = b;
                            // }
                        }
                    }).setNegativeButton(android.R.string.cancel, null).setPositiveButton(android.R.string.ok, mClickListenerScreenview

                    ).show();
        }

    }

    private DialogInterface.OnClickListener mClickListenerScreenview = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            for (int i = 0; i < mCheckedItems.length; ++i) {
                mCheckedItems[i] = mCheckedItemsTemp[i];
            }
            updateScreen1Setting();
            setScreen1Setting();
        }
    };

    private boolean[] mCheckedItems = new boolean[3];
    private boolean[] mCheckedItemsTemp = new boolean[3];

    private void updateScreen1Setting() {

        String[] array = mContext.getResources().getStringArray(R.array.screen1_view_select);
        String s = "";
        for (int i = 0; i < mCheckedItems.length; ++i) {
            if (mCheckedItems[i]) {
                if (i < array.length) {
                    if (s.length() > 0) {
                        s += " , ";
                    }
                    s += array[i];
                }
            }
        }

        ((TextView) mMainView.findViewById(R.id.screen_view_select_value)).setText(s);

    }

    private void setScreen1Setting() {
        String[] array = mContext.getResources().getStringArray(R.array.screen1_view_select_value);
        String s = "";
        for (int i = 0; i < mCheckedItems.length; ++i) {
            if (mCheckedItems[i]) {
                if (i < array.length) {
                    if (s.length() > 0) {
                        s += ",";
                    }
                    s += array[i];
                }
            }
        }
        MachineConfig.setProperty(MachineConfig.KEY_SCREEN1_VIEW, s);

        Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_SCREEN1_VIEW);
        mContext.sendBroadcast(it);

    }

    private void getScreen1Setting() {
        String value = MachineConfig.getProperty(MachineConfig.KEY_SCREEN1_VIEW);
        if (null != value) {
            String[] array = mContext.getResources().getStringArray(R.array.screen1_view_select_value);

            for (int i = 0; i < array.length; ++i) {
                if (i < mCheckedItems.length) {
                    if (value.contains(array[i])) {
                        mCheckedItems[i] = true;
                    } else {
                        mCheckedItems[i] = false;
                    }
                }
            }
        }
    }

    public boolean mWillDestory = false;

    @Override
    public void onPause() {
        super.onPause();

    }

    private int mPrePlayStatus;

    @Override
    public void onResume() {
        super.onResume();

        updateScreenSize();
        getScreen1Setting();
        updateScreen1Setting();
    }

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    int i = Util.getFileValue(SCREEN_SWITCH);
                    if (i == 0) {
                        i = 1;
                    } else {
                        i = 0;
                    }

                    Util.setFileValue(SCREEN_SWITCH, i);
                    break;
            }
            super.handleMessage(msg);
        }

    };

}
