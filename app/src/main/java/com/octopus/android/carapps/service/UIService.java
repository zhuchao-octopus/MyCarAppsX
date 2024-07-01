package com.octopus.android.carapps.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.ProtocolAk47;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.octopus.android.carapps.appwidget.MediaAppWidgetProvider;
import com.octopus.android.carapps.audio.MediaPlaybackService;
import com.octopus.android.carapps.audio.MusicActivity;
import com.octopus.android.carapps.auxplayer.AuxInService;
import com.octopus.android.carapps.btmusic.BTMusicActivity;
import com.octopus.android.carapps.btmusic.BTMusicService;
import com.octopus.android.carapps.common.ui.BR;
import com.octopus.android.carapps.common.ui.GlobalDef;
import com.octopus.android.carapps.common.presentation.PresentationUI;
import com.octopus.android.carapps.common.service.ServiceBase;
import com.octopus.android.carapps.common.utils.ParkBrake;
import com.octopus.android.carapps.common.utils.ResourceUtil;
import com.octopus.android.carapps.dvd.DVDPlayer;
import com.octopus.android.carapps.dvd.DVDService;
import com.octopus.android.carapps.frontcamera.FrontCameraService;
import com.octopus.android.carapps.radio.RadioActivity;
import com.octopus.android.carapps.radio.RadioService;
import com.octopus.android.carapps.radio.RadioUI;
import com.octopus.android.carapps.tv.TVService;
import com.octopus.android.carapps.video.VideoActivity;
import com.octopus.android.carapps.video.VideoService;
import com.zhuchao.android.fbase.MMLog;

import java.util.Objects;

public class UIService extends Service {
    public static final String TAG = "UIService";

    ServiceBase mServiceBase;
    RadioService mRadioService;
    DVDService mDVDService;
    AuxInService mAuxInService;
    MediaPlaybackService mMediaPlaybackService;
    BTMusicService mBTMusicService;
    VideoService mVideoService;
    TVService mTVService;

    private static UIService mThis;

    @Override
    public void onCreate() {
        ResourceUtil.updateAppUi(this);
        super.onCreate();
        MMLog.d(TAG, "UIService onCreate");
        GlobalDef.init(this);
        mThis = this;
        ParkBrake.isBrake();
        registerListener();

        initMediaRouter();
        initScreen0();
        initScreen1();

        mRadioService = RadioService.getInstanse(this);
        mMediaPlaybackService = MediaPlaybackService.getInstance(this);
        mVideoService = VideoService.getInstanse(this);
        mBTMusicService = BTMusicService.getInstanse(this);
        mDVDService = DVDService.getInstanse(this);
        mAuxInService = AuxInService.getInstanse(this);
        mTVService = TVService.getInstanse(this);
        // for first boot mount msg.
        mHandler.postDelayed(new Runnable() {
            public void run() {
                if (BR.mBootTime == 0) {
                    BR.mBootTime = System.currentTimeMillis();
                }
                initOTGTest();
            }
        }, 5200);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {

        unregisterListener();
        super.onDestroy();
    }

    private void updateSource(int cmd, Intent intent) {

        int source = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);

        Log.d("fk", "updateSource:" + source);

        if (GlobalDef.mSource != source && mServiceBase != null) {
            if (((mServiceBase.getSource() == GlobalDef.mSource) && source != MyCmd.SOURCE_OTHERS_APPS) || (GlobalDef.mSource == MyCmd.SOURCE_OTHERS_APPS && mServiceBase.getSource() != source)) {
                // move to GlobalDef.
                // if other APP can set source ,need this.
                mServiceBase.abandonAudioFocus();
            }
        }

        switch (source) {
            case MyCmd.SOURCE_RADIO:
                mServiceBase = RadioService.getInstanse(this);
                mServiceBase.doEQResule();
                break;
            case MyCmd.SOURCE_MUSIC:
                mServiceBase = com.octopus.android.carapps.audio.MediaPlaybackService.getInstance(this);
                mServiceBase.doEQResule();
                break;
            case MyCmd.SOURCE_VIDEO:
                mServiceBase = VideoService.getInstanse(this);
                mServiceBase.doEQResule();
                break;
            case MyCmd.SOURCE_BT_MUSIC:
                mServiceBase = BTMusicService.getInstanse(this);
                mServiceBase.doEQResule();
                break;
            case MyCmd.SOURCE_AUX:
                mServiceBase = mAuxInService;// AuxInService.getInstanse(this);
                break;
            case MyCmd.SOURCE_DTV_CVBS:
                mServiceBase = mTVService;// AuxInService.getInstanse(this);
                break;
            case MyCmd.SOURCE_DVD:
                mServiceBase = mDVDService;// DVDService.getInstanse(this);
                mServiceBase.doEQResule();
                break;
        }

        if (mServiceBase != null) {
            mServiceBase.doCmd(cmd, intent);
        }

        if (mPresentationUI != null && source != MyCmd.SOURCE_OTHERS_APPS) {
            mPresentationUI.update(source);
        }

        // if (GlobalDef.mSource == MyCmd.SOURCE_DVD
        // && (source == MyCmd.SOURCE_RADIO
        // || source == MyCmd.SOURCE_MUSIC
        // || source == MyCmd.SOURCE_VIDEO
        // || source == MyCmd.SOURCE_BT_MUSIC
        // || source == MyCmd.SOURCE_AUX)) {
        //
        // }

        GlobalDef.mSourceWillUpdate = GlobalDef.mSource = source;

        //		if (GlobalDef.mIs8600) {
        //			String s = null;
        //			switch (source) {
        //			case MyCmd.SOURCE_RADIO:
        //			case MyCmd.SOURCE_MUSIC:
        //				s = "";
        //				break;
        //			case MyCmd.SOURCE_AUX:
        //			case MyCmd.SOURCE_VIDEO:
        //				s = "AV";
        //				break;
        //			case MyCmd.SOURCE_BT_MUSIC:
        //				s = "BTAV";
        //				break;
        //			case MyCmd.SOURCE_DTV_CVBS:
        //				s = "TV";
        //				break;
        //			case MyCmd.SOURCE_DVD:
        //				s = "DVD";
        //				break;
        //			default:
        //				s = "OFF";
        //				break;
        //			}
        //			if (s != null) {
        //				GlobalDef.setSmallLcd(s);
        //			}
        //			if ("OFF".equals(s)){
        //				GlobalDef.setSmallLcdAinm("0");
        //			} else {
        //				GlobalDef.setSmallLcdAinm("1");
        //			}
        //			if (MyCmd.SOURCE_RADIO != source && MyCmd.SOURCE_MUSIC != source) {
        ////				if () {
        ////					GlobalDef.setSmallLcdIcon("0x00000800");
        ////				} else {
        //					GlobalDef.setSmallLcdIcon("0");
        ////				}
        //			}
        //		}

    }

    private int mSourceScreen1 = MyCmd.SOURCE_NONE;
    private final Handler mHandler = new Handler(Objects.requireNonNull(Looper.myLooper())) {

        @Override
        public void handleMessage(Message msg) {
            Intent intent = (Intent) msg.obj;
            // int data = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
            switch (msg.what) {
                case MyCmd.Cmd.SOURCE_CHANGE:
                    mUIScreen0Monitor.removeMessages(0);
                    updateSource(msg.what, intent);
                    break;

                case MyCmd.Cmd.RETURN_CURRENT_SOURCE:
                    int id = intent.getIntExtra(MyCmd.EXTRA_COMMON_ID, 0);
                    if (id == MyCmd.ID.CAR_UI) {
                        mUIScreen0Monitor.removeMessages(0);
                        updateSource(msg.what, intent);
                    }
                    break;
                // case MyCmd.Cmd.RETURN_CURRENT_SOURCE:
                // if (mPresentationUI != null) {
                // mPresentationUI.update(data);
                // }
                //
                // updateSource(data);
                // mSource = data;
                // break;

            }

            super.handleMessage(msg);
        }

    };
    private final Handler mUIScreen0Monitor = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (mPresentationUI != null) {
                mPresentationUI.updateScreen1Change(msg.arg1, msg.arg2);
            }
            super.handleMessage(msg);
        }

    };

    private void doKeyControl(int code) {

        switch (code) {
            case MyCmd.Keycode.RADIO_POWER: {
                if (GlobalDef.mSource != RadioUI.SOURCE) {

                    GlobalDef.reactiveSource(this, RadioUI.SOURCE, RadioService.mAudioFocusListener);

                    if (mServiceBase != null) {
                        mServiceBase.doKeyControl(code);
                    }
                    // BroadcastUtil.sendToCarServiceSetSource(this,
                    // RadioUI.SOURCE);
                } else {
                    BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);

                }
            }
            break;
            default:
                if (mServiceBase != null) {
                    if (mServiceBase.getSource() == GlobalDef.mSource) {
                        mServiceBase.doKeyControl(code);
                    }
                }
                break;
        }

    }

    private int mRevocerSource = PresentationUI.SOURCE_NONE;

    private void doReverse(int on) {
        if (mPresentationUI != null) {
            if (on == 1) {
                if (mPresentationUI.mSource == PresentationUI.SOURCE_AUX) {
                    mRevocerSource = PresentationUI.SOURCE_AUX;

                    mPresentationUI.update(PresentationUI.SOURCE_SCREEN_SAVER);
                }
            } else {
                if (mRevocerSource == PresentationUI.SOURCE_AUX) {
                    mPresentationUI.update(mRevocerSource);
                    mRevocerSource = PresentationUI.SOURCE_NONE;
                }
            }
        }
    }

    private BroadcastReceiver mReceiver = null;
    private BroadcastReceiver mReceiverWidgetKey = null;

    private void registerListener() {
        if (mReceiver == null) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    // Log.d("MusicPlayer", action);
                    if (action.equals(MyCmd.BROADCAST_CAR_SERVICE_SEND)) {

                        int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, 0);

                        Log.d(TAG, "" + cmd);

                        switch (cmd) {
                            case MyCmd.Cmd.MCU_RADIO_RECEIVE_DATA:
                                if (mRadioService != null) {
                                    mRadioService.doCmd(cmd, intent);
                                }
                                break;
                            case MyCmd.Cmd.MCU_CANBOX_RECEIVE_DATA:
                                // if (mCanService != null) {
                                // mCanService.doCmd(cmd, intent);
                                // }
                                break;
                            case MyCmd.Cmd.REVERSE_STATUS:
                                GlobalDef.mReverseStatus = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                                doReverse(GlobalDef.mReverseStatus);
                                try {
                                    // if (mServiceBase != null
                                    // && (mServiceBase == mDVDService ||
                                    // mServiceBase == mAuxInService)) {
                                    // mServiceBase.doCmd(cmd, intent);
                                    // }
                                    mDVDService.doCmd(cmd, intent);
                                    mAuxInService.doCmd(cmd, intent);
                                    mTVService.doCmd(cmd, intent);
                                    mMediaPlaybackService.doCmd(cmd, intent);
                                    FrontCameraService.reverseCome(cmd, intent);
                                } catch (Exception e) {
                                    Log.e(TAG, "mServiceBase instanceof AuxInService err");
                                }
                                break;
                            case MyCmd.Cmd.MCU_DVD_RECEIVE_DATA:
                                mDVDService.doCmd(cmd, intent);
                                break;
                            case MyCmd.Cmd.SOURCE_CHANGE:
                            case MyCmd.Cmd.RETURN_CURRENT_SOURCE:
                                mHandler.sendMessage(mHandler.obtainMessage(cmd, intent));

                                if (mServiceBase != null) {
                                    mServiceBase.doCmd(cmd, intent);
                                }

                                break;
                            case MyCmd.Cmd.REQUEST_SCREEN1_SHOW:
                                int source = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                                Log.d("allen1", "!!!!!!!!" + source);
                                if (mPresentationUI != null && source != MyCmd.SOURCE_OTHERS_APPS) {
                                    mPresentationUI.update(source);
                                }
                                break;
                            case MyCmd.Cmd.MCU_AUDIO_RECEIVE_DATA:
                                byte[] buf = intent.getByteArrayExtra(MyCmd.EXTRA_COMMON_DATA);
                                doEQData(buf);
                                break;
                            case MyCmd.Cmd.AUTO_TEST_START: {
                                int data = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                                if (data == 1) {
                                    GlobalDef.mAutoTest = true;
                                } else {
                                    data = 0;
                                }
                            }
                            break;
                            case MyCmd.Cmd.AUTO_TEST_END: {
                                GlobalDef.mAutoTest = false;
                            }
                            break;
                            default:
                                if (mServiceBase != null) {
                                    mServiceBase.doCmd(cmd, intent);
                                }
                                break;
                        }
                    } else if (action.equals(MyCmd.ACTION_KEY_PRESS)) {

                        int code = intent.getIntExtra(MyCmd.EXTRAS_KEY_CODE, 0);

                        doKeyControl(code);

                    } else if (action.equals(MyCmd.BROADCAST_ACTIVITY_STATUS)) {
                        // widget used
                        if (GlobalDef.mSource == MyCmd.SOURCE_MUSIC) {
                            if (mMediaPlaybackService != null) {
                                mMediaPlaybackService.doCmd(MyCmd.Cmd.MUSIC_SEND_PLAY_STATUS, intent);
                            }
                        }

                    } else if (action.equals(Intent.ACTION_LOCALE_CHANGED) || action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                        // ResourceUtil.updateSingleUi(mThis);
                        Log.d("ddc", "122:" + action);
                        ResourceUtil.updateAppUi(context);
                        updateAllUI(context, false);
                    } else if (action.equals(MyCmd.BROADCAST_MACHINECONFIG_UPDATE)) {

                        String s = intent.getStringExtra(MyCmd.EXTRA_COMMON_CMD);
                        if (MachineConfig.KEY_RDS.equals(s)) {
                            MachineConfig.getPropertyOnce(MachineConfig.KEY_RDS);
                        } else if (SystemConfig.KEY_LAUNCHER_UI_RM10.equals(s)) {
                            GlobalDef.initCustomUI(context);
                            updateAllUI(context, true);
                            // GlobalDef.initCustomUI(context);
                            // RadioActivity.finishActivity(null);
                            // VideoActivity.finishActivity(null);
                            // MusicActivity.finishActivity(null);
                            // DVDPlayer.finishActivity();
                        } else if (MachineConfig.KEY_TOUCH3_IDENTIFY.equals(s)) {
                            GlobalDef.getTouch3ConfigValue();
                        } else if (MachineConfig.KEY_CAN_BOX.equals(s)) {
                            GlobalDef.updateCanBox(s);
                        }

                    } else if (action.equals(MyCmd.BROADCAST_ACC_DELAY_POWER_OFF)) {
                        GlobalDef.mPowerOffTime = SystemClock.uptimeMillis();
                        if (mMediaPlaybackService != null) {
                            mMediaPlaybackService.doCmd(MyCmd.Cmd.ACC_POWER_OFF, intent);
                        }
                        if (mVideoService != null) {
                            mVideoService.doCmd(MyCmd.Cmd.ACC_POWER_OFF, intent);
                        }
                        if (mDVDService != null) {
                            mDVDService.doCmd(MyCmd.Cmd.ACC_POWER_OFF, intent);
                        }
                        ParkBrake.saveCameraStatasIfSleep();
                    } else if (action.equals(MyCmd.BROADCAST_ACC_DELAY_POWER_ON)) {
                        Log.d("MusicPlayer", "GlobalDef.mSleepOnTime1:" + GlobalDef.mSleepOnTime);
                        GlobalDef.mPowerOffTime = 0;
                        if (Util.isRKSystem()) {
                            GlobalDef.mSleepOnTime = SystemClock.uptimeMillis();

                            if (mMediaPlaybackService != null) {
                                mMediaPlaybackService.doCmd(MyCmd.Cmd.ACC_POWER_ON, intent);
                            }
                            if (mVideoService != null) {
                                mVideoService.doCmd(MyCmd.Cmd.ACC_POWER_ON, intent);
                            }
                        }

                    }
                    // else if (action.equals(
                    // MyCmd.BROADCAST_POWER_OFF)) {
                    // mDVDService.systemPowerOff();
                    // }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(MyCmd.BROADCAST_CAR_SERVICE_SEND);
            iFilter.addAction(MyCmd.ACTION_KEY_PRESS);

            iFilter.addAction(MyCmd.BROADCAST_ACC_DELAY_POWER_OFF);
            iFilter.addAction(MyCmd.BROADCAST_ACC_DELAY_POWER_ON);
            if (GlobalDef.mIsMediaWidget) {
                iFilter.addAction(MyCmd.BROADCAST_ACTIVITY_STATUS);
            }
            iFilter.addAction(MyCmd.BROADCAST_POWER_OFF);

            iFilter.addAction(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);

            iFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
            iFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
            registerReceiver(mReceiver, iFilter);
        }
        if (GlobalDef.mIsMediaWidget && mReceiverWidgetKey == null) {
            mReceiverWidgetKey = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    String ss[] = action.split("=");
                    try {
                        String sender = ss[0];
                        int source = 0;
                        if (MediaAppWidgetProvider.ACTION_WIDGET_MUSIC.startsWith(sender)) {
                            source = MyCmd.SOURCE_MUSIC;
                        } else if (MediaAppWidgetProvider.ACTION_WIDGET_BT_MUSIC.startsWith(sender)) {
                            source = MyCmd.SOURCE_BT_MUSIC;
                        } else if (MediaAppWidgetProvider.ACTION_WIDGET_RADIO.startsWith(sender)) {
                            source = MyCmd.SOURCE_RADIO;
                        }

                        int code = Integer.valueOf(ss[1]);

                        if (GlobalDef.mSource == source || code == MyCmd.Keycode.RADIO_POWER) {
                            doKeyControl(code);
                        }
                    } catch (Exception e) {

                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(MediaAppWidgetProvider.ACTION_WIDGET_MUSIC + MyCmd.Keycode.PREVIOUS);
            iFilter.addAction(MediaAppWidgetProvider.ACTION_WIDGET_MUSIC + MyCmd.Keycode.NEXT);
            iFilter.addAction(MediaAppWidgetProvider.ACTION_WIDGET_MUSIC + MyCmd.Keycode.PLAY_PAUSE);
            iFilter.addAction(MediaAppWidgetProvider.ACTION_WIDGET_RADIO + MyCmd.Keycode.PREVIOUS);
            iFilter.addAction(MediaAppWidgetProvider.ACTION_WIDGET_RADIO + MyCmd.Keycode.NEXT);
            iFilter.addAction(MediaAppWidgetProvider.ACTION_WIDGET_RADIO + MyCmd.Keycode.RADIO_POWER);

            iFilter.addAction(MediaAppWidgetProvider.ACTION_WIDGET_BT_MUSIC + MyCmd.Keycode.PREVIOUS);
            iFilter.addAction(MediaAppWidgetProvider.ACTION_WIDGET_BT_MUSIC + MyCmd.Keycode.NEXT);
            iFilter.addAction(MediaAppWidgetProvider.ACTION_WIDGET_BT_MUSIC + MyCmd.Keycode.PLAY_PAUSE);
            registerReceiver(mReceiverWidgetKey, iFilter);
        }
    }

    private void doEQData(byte[] buf) {
        if (buf != null && buf.length >= 17) {
            switch (buf[1]) {
                case ProtocolAk47.RECEVE_AUDIO_EQ_INFO:
                    GlobalDef.mEQModeParam = buf[2];
                    if (mServiceBase != null) {
                        mServiceBase.doEQResule();
                    }
                    break;

            }
        }
    }

    private void unregisterListener() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        if (mReceiverWidgetKey != null) {
            unregisterReceiver(mReceiverWidgetKey);
            mReceiverWidgetKey = null;
        }
    }

    private void sendToCarServiceID(int cmd, int id) {
        Intent it = new Intent(MyCmd.BROADCAST_CMD_TO_CAR_SERVICE_CAR_UI);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
        it.putExtra(MyCmd.EXTRA_COMMON_ID, id);
        it.setPackage(AppConfig.PACKAGE_CAR_SERVICE);
        sendBroadcast(it);
    }

    private void initScreen0() {
        GlobalDef.setUIScreen0Monitor(mUIScreen0Monitor);

        sendToCarServiceID(MyCmd.Cmd.QUERY_CURRENT_SOURCE, MyCmd.ID.CAR_UI);
    }

    private MediaRouter mMediaRouter = null;
    private PresentationUI mPresentationUI;

    private void initScreen1() {
        if (mPresentationUI == null) {
            mPresentationUI = PresentationUI.getInstanse(this);
            if (mPresentationUI != null) {
                mPresentationUI.update(PresentationUI.SOURCE_SCREEN_SAVER);

                sendToCarServiceID(MyCmd.Cmd.QUERY_CURRENT_SOURCE, MyCmd.ID.CAR_UI);
            }
        }
    }

    private void releaseScreen1() {
        if (mPresentationUI != null) {
            mPresentationUI.release();
            mPresentationUI = null;
        }
    }

    private void initMediaRouter() {
        mMediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);
        mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
    }

    private final MediaRouter.SimpleCallback mMediaRouterCallback = new MediaRouter.SimpleCallback() {
        @Override
        public void onRouteSelected(MediaRouter router, int type, RouteInfo info) {
            Log.w(TAG, "onRouteSelected: type=" + type + ", info=" + info);

        }

        @Override
        public void onRouteUnselected(MediaRouter router, int type, RouteInfo info) {
            Log.w(TAG, "onRouteUnselected: type=" + type + ", info=" + info);
        }

        @Override
        public void onRoutePresentationDisplayChanged(MediaRouter router, RouteInfo info) {
            Log.w(TAG, "onRoutePresentationDisplayChanged: info=" + info);
            Display display = info.getPresentationDisplay();
            if (display == null) {
                releaseScreen1();
            } else {
                initScreen1();
            }
        }
    };

    private void updateAllUI(Context context, boolean finishUI) {
        // Log.d("ddc", "updateAllUI:");
        // GlobalDef.initCustomUI(context);
        RadioActivity.updateBySystemConfig(finishUI);
        VideoActivity.updateBySystemConfig(finishUI);
        MusicActivity.updateBySystemConfig(finishUI);
        DVDPlayer.updateBySystemConfig(finishUI);
        BTMusicActivity.updateBySystemConfig(finishUI);
    }

    private static final String OTG = "/sys/class/ak/source/otg_id";
    private static final String OTG_60 = "/sys/devices/platform/dwc_otg/otg_mode";

    private void initOTGTest() {
        if (MachineConfig.getProperty(MachineConfig.KEY_OTG_TEST) != null) {
            if (!Util.isRKSystem()) {
                if (Build.VERSION.SDK_INT > 23) {
                    Util.setFileValue(OTG, 1);
                } else {
                    Util.setFileValue(OTG_60, 1);
                }
            } else {
                if (Util.isPX6()) {
                    if (Util.isAndroidP()) {
                        //Util.setFileValue("/sys/devices/platform/usb0/dwc3_mode", "peripheral");
                    } else if (Util.isAndroidQ()) {
                        //Util.setFileValue("/sys/devices/platform/ff770000.syscon/ff770000.syscon:usb2-phy@e450/otg_mode", "peripheral");
                    }
                } else {
                    Util.sudoExec("chmod:666:/sys/bus/platform/drivers/usb20_otg/force_usb_mode");
                    Util.checkAKDRunning();
                    Util.setFileValue("/sys/bus/platform/drivers/usb20_otg/force_usb_mode", 2);
                }
            }
            Toast.makeText(this, "To usb otg debug", Toast.LENGTH_LONG).show();
        }
    }
}
