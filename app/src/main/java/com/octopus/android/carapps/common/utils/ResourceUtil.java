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
import com.octopus.android.carapps.common.ui.GlobalDef;
import com.zhuchao.android.fbase.MMLog;

import java.util.Arrays;

public class ResourceUtil {
    private static final String TAG = "ResourceUtil";
    public static int mScreenWidth = 0;
    public static int mScreenHeight = 0;
    //public static int baseSW = 0;

    //private static boolean multiWindow = false;

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

    public static void updateAppUi(Context context) { // app used except
        ///if (GlobalDef.mContext == null) {
        ///    GlobalDef.initCustomUI(context);
        ///}
        int base_sw = 340;
        String value = GlobalDef.getSystemUI();
        MMLog.d(TAG, "SystemUI Type:" + value);
        /// DisplayMetrics dm = context.getResources().getDisplayMetrics();
        /// mScreenWidth = dm.widthPixels;
        /// mScreenHeight = dm.heightPixels;
        DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = displayManager.getDisplays();
        ///DisplayInfo outDisplayInfo = new DisplayInfo();
        ///display[0].getDisplayInfo(outDisplayInfo);
        ///mScreenWidth = outDisplayInfo.appWidth;
        ///mScreenHeight = outDisplayInfo.appHeight;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

        Configuration configuration = context.getResources().getConfiguration();
        MMLog.d(TAG, "Configuration:" + configuration);

        //for(Display display:displays)
        for (int i = 0; i < displays.length; i++)
            MMLog.d(TAG, "Display[" + i + "]:" + displays[i].toString());

        if (MachineConfig.VALUE_SYSTEM_UI_KLD12_80.equals(value) || MachineConfig.VALUE_SYSTEM_UI19_KLD1.equals(value) || MachineConfig.VALUE_SYSTEM_UI28_7451.equals(value)) {
            base_sw = 346;
        } else if (MachineConfig.VALUE_SYSTEM_UI_KLD3_8702.equals(value) || MachineConfig.VALUE_SYSTEM_UI_KLD15_6413.equals(value) || MachineConfig.VALUE_SYSTEM_UI32_KLD8.equals(value) || MachineConfig.VALUE_SYSTEM_UI37_KLD10.equals(value)) {
            base_sw = 360;
        } else if (MachineConfig.VALUE_SYSTEM_UI45_8702_2.equals(value)) {
            base_sw = 370;
        } else if (MachineConfig.VALUE_SYSTEM_UI_KLD10_887.equals(value)) {
            base_sw = 380;
        } else if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(value)) {
            base_sw = 390;
        } else if (MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(value)) {
            base_sw = 400;
        } else if (MachineConfig.VALUE_SYSTEM_UI22_1050.equals(value) || MachineConfig.VALUE_SYSTEM_UI_PX30_1.equals(value)) {
            base_sw = 410;
        } else if (MachineConfig.VALUE_SYSTEM_UI16_7099.equals(value) || MachineConfig.VALUE_SYSTEM_UI31_KLD7.equals(value) || MachineConfig.VALUE_SYSTEM_UI36_664.equals(value)) {
            base_sw = 420;
        } else if (MachineConfig.VALUE_SYSTEM_UI34_KLD9.equals(value)) {
            base_sw = 430;
        }
        else if (MachineConfig.VALUE_SYSTEM_UI35_KLD813.equals(value)) {
            base_sw = 440;
        } else if (MachineConfig.VALUE_SYSTEM_UI35_KLD813_2.equals(value)) {
            base_sw = 450;
        } else if (MachineConfig.VALUE_SYSTEM_UI21_RM12.equals(value)) {
            base_sw = 460;
        } else if (MachineConfig.VALUE_SYSTEM_UI44_KLD007.equals(value)) {
            base_sw = 470;
        } else if (MachineConfig.VALUE_SYSTEM_UI43_3300_1.equals(value)) {
            base_sw = 480;
        } else {
            base_sw = 320;
        }

        configuration.smallestScreenWidthDp = base_sw;
        context.getResources().updateConfiguration(configuration, null);//test
        MMLog.d(TAG, "Configuration:" + configuration);
    }

    public static String updateSingleUi(Context context) { // only launcher use
        int sw = 0;
        String value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);
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
