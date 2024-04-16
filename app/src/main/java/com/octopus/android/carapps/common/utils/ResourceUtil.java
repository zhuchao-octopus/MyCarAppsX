package com.octopus.android.carapps.common.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import com.common.util.MachineConfig;
import com.octopus.android.carapps.car.ui.GlobalDef;

public class ResourceUtil {

    public static int mScreenWidth = 0;
    public static int mScreenHeight = 0;
    public static int baseSW = 0;

    private static boolean multiWindow = false;
    private static final String TAG = "ResourceUtil";

    @SuppressLint("DiscouragedApi")
    public static int getLayoutId(Context paramContext, String paramString) {
        return paramContext.getResources().getIdentifier(paramString, "layout", paramContext.getPackageName());
    }

    @SuppressLint("DiscouragedApi")
    public static int getStringId(Context paramContext, String paramString) {
        return paramContext.getResources().getIdentifier(paramString, "string", paramContext.getPackageName());
    }

    @SuppressLint("DiscouragedApi")
    public static int getDrawableId(Context paramContext, String paramString) {
        return paramContext.getResources().getIdentifier(paramString, "drawable", paramContext.getPackageName());
    }

    @SuppressLint("DiscouragedApi")
    public static int getStyleId(Context paramContext, String paramString) {
        return paramContext.getResources().getIdentifier(paramString, "style", paramContext.getPackageName());
    }

    @SuppressLint("DiscouragedApi")
    public static int getId(Context paramContext, String paramString) {
        return paramContext.getResources().getIdentifier(paramString, "id", paramContext.getPackageName());
    }

    @SuppressLint("DiscouragedApi")
    public static int getColorId(Context paramContext, String paramString) {
        return paramContext.getResources().getIdentifier(paramString, "color", paramContext.getPackageName());
    }

    public static boolean isMultiWindow(Activity activity) {
        ///try {
        //    Class<?> CActivity = Class.forName("android.app.Activity");
        //    java.lang.reflect.Method method = CActivity.getMethod("isInMultiWindowMode", (Class<?>) null);
        //    return (boolean) method.invoke(activity);
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}

        //return false;
        return activity.isInMultiWindowMode();
    }

    public static boolean isMultiWindow() {
        return multiWindow;
    }

    public static boolean updateAppUi(Context context) { // app used except
        // launcher

        //		String value = MachineConfig
        //				.getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);

        if (GlobalDef.mContext == null) {
            GlobalDef.initCustomUI(context);
        }
        String value = GlobalDef.getSystemUI();


        // if (value != null) {
        //		int sw = 0;
        //		int w = 0;
        //		int h = 0;
        int type = 0; // deault 800X480


        if (MachineConfig.VALUE_SYSTEM_UI_KLD12_80.equals(value) || MachineConfig.VALUE_SYSTEM_UI19_KLD1.equals(value) || MachineConfig.VALUE_SYSTEM_UI28_7451.equals(value)) {
            baseSW = 340;
        } else if (MachineConfig.VALUE_SYSTEM_UI_KLD3_8702.equals(value) || MachineConfig.VALUE_SYSTEM_UI_KLD15_6413.equals(value) || MachineConfig.VALUE_SYSTEM_UI32_KLD8.equals(value) || MachineConfig.VALUE_SYSTEM_UI37_KLD10.equals(value)) {
            baseSW = 360;
        } else if (MachineConfig.VALUE_SYSTEM_UI45_8702_2.equals(value)) {
            baseSW = 370;
        } else if (MachineConfig.VALUE_SYSTEM_UI_KLD10_887.equals(value)) {
            baseSW = 380;
        } else if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(value)) {
            baseSW = 390;
        } else if (MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(value)) {
            baseSW = 400;
        } else if (MachineConfig.VALUE_SYSTEM_UI22_1050.equals(value) || MachineConfig.VALUE_SYSTEM_UI_PX30_1.equals(value)) {
            baseSW = 410;
        } else if (MachineConfig.VALUE_SYSTEM_UI16_7099.equals(value) || MachineConfig.VALUE_SYSTEM_UI31_KLD7.equals(value) || MachineConfig.VALUE_SYSTEM_UI36_664.equals(value)) {
            baseSW = 420;
        } else if (MachineConfig.VALUE_SYSTEM_UI34_KLD9.equals(value)) {
            baseSW = 430;
        }
        //wrong 813 no use
        else if (MachineConfig.VALUE_SYSTEM_UI35_KLD813.equals(value)) {
            baseSW = 440;
        } else if (MachineConfig.VALUE_SYSTEM_UI35_KLD813_2.equals(value)) {
            baseSW = 450;
        } else if (MachineConfig.VALUE_SYSTEM_UI21_RM12.equals(value)) {
            baseSW = 460;
        } else if (MachineConfig.VALUE_SYSTEM_UI44_KLD007.equals(value)) {
            baseSW = 470;
        } else if (MachineConfig.VALUE_SYSTEM_UI43_3300_1.equals(value)) {
            baseSW = 480;
        } else {
            baseSW = 320;
        }

        // DisplayMetrics dm = context.getResources().getDisplayMetrics();
        // mScreenWidth = dm.widthPixels;
        // mScreenHeight = dm.heightPixels;
        DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display[] display = displayManager.getDisplays();

        //DisplayInfo outDisplayInfo = new DisplayInfo();
        //display[0].getDisplayInfo(outDisplayInfo);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        //mScreenWidth = outDisplayInfo.appWidth;
        //mScreenHeight = outDisplayInfo.appHeight;


        //DisplayMetrics dm = context.getResources().getDisplayMetrics();
        if (dm.widthPixels != mScreenWidth) {
            multiWindow = true;
        } else {
            multiWindow = false;
        }
        if (mScreenWidth < 1024 && mScreenHeight == 480 || mScreenWidth > 1280) {// 1280X480?
            if (dm.widthPixels == mScreenWidth) {
                type = 0;
            } else {
                type = 1;
            }
        } else if (mScreenWidth == 1280 && mScreenHeight == 480) {// 1280X480?
            if (dm.widthPixels == mScreenWidth) {
                type = 3;
            } else {
                type = 4;
            }
        } else if (mScreenWidth == 1024 && mScreenHeight == 600) {
            if (dm.widthPixels == mScreenWidth) {
                type = 6;
            } else {
                type = 7;
            }
        } else if (mScreenWidth == 1280 && mScreenHeight == 720) {
            if (dm.widthPixels == mScreenWidth) {
                type = 8;
            } else {
                type = 9;
            }
        }

        baseSW += type;

        // if (MachineConfig.VALUE_SYSTEM_UI_KLD12_80.equals(value)) {
        // if (type == 0) {
        // sw = 326;
        // } else {
        // sw = 327;
        // }
        // } else if (MachineConfig.VALUE_SYSTEM_UI_KLD3_8702.equals(value)) {
        // if (type == 0) {
        // sw = 332;
        // } else {
        // sw = 333;
        // }
        // } else if (MachineConfig.VALUE_SYSTEM_UI_KLD10_887.equals(value)) {
        // if (type == 0) {
        // sw = 334;
        // } else if (type == 2) {
        // sw = 335;
        // } else {
        // sw = 336;
        // }
        // }
        //


        Configuration c = context.getResources().getConfiguration();
        Log.d("ddc", "sw:" + baseSW + ":" + c + ": not set");
        if (baseSW != 0) {
            c.smallestScreenWidthDp = baseSW;
        }
        //		if (w != 0) {
        //			c.screenWidthDp = w;
        //		}
        //		if (h != 0) {
        //			c.screenHeightDp = h;
        //		}
        //		context.getResources().updateConfiguration(c, null);

        // }
        return multiWindow;
    }

    public static String updateSingleUi(Context context) { // only launcher use
        // now

        String value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);

        int sw = 0;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        if (dm.widthPixels == 1024 && dm.heightPixels == 600) {
            sw = 321;
        }

        Configuration c = context.getResources().getConfiguration();
        if (sw != 0) {
            c.smallestScreenWidthDp = sw;
        }

        context.getResources().updateConfiguration(c, null);
        return value;
    }

    public static boolean isInSplitScreen(Context context) {
        DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display[] display = displayManager.getDisplays();

        //DisplayInfo outDisplayInfo = new DisplayInfo();
        //display[0].getDisplayInfo(outDisplayInfo);

        //mScreenWidth = outDisplayInfo.appWidth;
        //mScreenHeight = outDisplayInfo.appHeight;

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        if (dm.widthPixels != mScreenWidth) {
            return true;
        }
        return false;
    }
}
