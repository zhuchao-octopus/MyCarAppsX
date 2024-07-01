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
        //if (GlobalDef.mContext == null) {
        //    GlobalDef.initCustomUI(context);
        //}
        //String value = GlobalDef.getSystemUI();
        // DisplayMetrics dm = context.getResources().getDisplayMetrics();
        // mScreenWidth = dm.widthPixels;
        // mScreenHeight = dm.heightPixels;
        DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = displayManager.getDisplays();
        //DisplayInfo outDisplayInfo = new DisplayInfo();
        //display[0].getDisplayInfo(outDisplayInfo);
        //mScreenWidth = outDisplayInfo.appWidth;
        //mScreenHeight = outDisplayInfo.appHeight;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

        Configuration configuration = context.getResources().getConfiguration();
        //configuration.smallestScreenWidthDp = 440;
        MMLog.d(TAG,"Configuration:" + configuration);
        //for(Display display:displays)
        for(int i =0;i<displays.length;i++)
           MMLog.d(TAG,"Display["+i+"]:" + displays[i].toString());
        context.getResources().updateConfiguration(configuration, null);//test
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
