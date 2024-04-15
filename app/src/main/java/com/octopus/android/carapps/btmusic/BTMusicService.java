package com.octopus.android.carapps.btmusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.common.util.UtilCarKey;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.service.ServiceBase;

public class BTMusicService extends ServiceBase {
	public static final String TAG = "BTMusicService";

	private static BTMusicService mThis;

	public static boolean mSuppotAutoAnswer = true;
	public static boolean mSuppotEditPin = true;

	public static BTMusicService getInstanse(Context context) {
		if (mThis == null) {
			mThis = new BTMusicService(context);

			mThis.onCreate();
		}
		return mThis;
	}

	public static BTMusicService getInstanse() {
		return mThis;
	}

	public BTMusicService(Context context) {
		super(context);
	}

	public void onDestroy() {
		unregisterEC();
		unregisterListener();
	}

	public void onCreate() {
		registerListener();
		registerEC();

		Intent it = new Intent(MyCmd.BROADCAST_CMD_TO_BT);
		it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.BT_REQUEST_A2DP_INFO);

		mContext.sendBroadcast(it);
	}
	
	
	private final static int MSG_DELAY_PAUSE = 1;
	private final Handler mHandler = new Handler(Looper.myLooper()) {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_DELAY_PAUSE:
				if (GlobalDef.mSource != BTMusicUI.SOURCE) {
					doKeyControl(MyCmd.Keycode.PAUSE);
				}
				break;
			}
		}
	};

	public void doCmd(int cmd, Intent intent) {
		switch (cmd) {
		case MyCmd.Cmd.SOURCE_CHANGE:
			int source = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
			if (source != BTMusicUI.SOURCE) {
				mHandler.removeMessages(MSG_DELAY_PAUSE);
				mHandler.sendEmptyMessageDelayed(MSG_DELAY_PAUSE, 200);
			} else {
				if(UtilCarKey.mBtMusicInBTapk){
					GlobalDef.reactiveSource(mContext, BTMusicUI.SOURCE,
							BTMusicService.mAudioFocusListener);
				}
			}
			break;
		}
	}

	public void doKeyControl(int code) {

		switch (code) {

		case MyCmd.Keycode.PLAY:
		case MyCmd.Keycode.NEXT:

		case MyCmd.Keycode.PREVIOUS:
		case MyCmd.Keycode.PLAY_PAUSE:
		case MyCmd.Keycode.PAUSE:
		case MyCmd.Keycode.CH_DOWN:
		case MyCmd.Keycode.KEY_SEEK_NEXT:
		case MyCmd.Keycode.KEY_TURN_A:

		case MyCmd.Keycode.CH_UP:
		case MyCmd.Keycode.KEY_SEEK_PREV:
		case MyCmd.Keycode.KEY_TURN_D:

		case MyCmd.Keycode.KEY_DVD_UP:
		case MyCmd.Keycode.KEY_DVD_RIGHT:
		case MyCmd.Keycode.KEY_DVD_DOWN:
		case MyCmd.Keycode.KEY_DVD_LEFT:
			break;
		default:
			code = 0;
			break;
		}
		if (code != 0) {
			Intent it = new Intent(MyCmd.BROADCAST_CMD_TO_BT);
			it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.BT_RECEIVE_DATA_KEY);
			it.putExtra(MyCmd.EXTRA_COMMON_DATA, code);
			mContext.sendBroadcast(it);
		}
	}

	private static Handler[] mHandlerPresentation = new Handler[2];

	public static void setHandler(Handler h, int index) {
		if (index < mHandlerPresentation.length) {
			mHandlerPresentation[index] = h;
		}
	}

	private void returnInfoToUI(int value) {
		for (int i = 0; i < mHandlerPresentation.length; ++i) {
			if (mHandlerPresentation[i] != null) {
				mHandlerPresentation[i].sendEmptyMessage(value);
			}
		}
	}

	private BroadcastReceiver mReceiver = null;

	private void unregisterListener() {
		if (mReceiver != null) {
			mContext.unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}

	private void registerListener() {
		if (mReceiver == null) {
			mReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();

					if (action.equals(MyCmd.BROADCAST_CMD_FROM_BT)) {
						doBTCmd(intent);
					}

				}
			};
			IntentFilter iFilter = new IntentFilter();

			iFilter.addAction(MyCmd.BROADCAST_CMD_FROM_BT);

			mContext.registerReceiver(mReceiver, iFilter);
		}
	}

	public static final int A2DP_INFO_INITIAL = 0;
	public static final int A2DP_INFO_READY = 1;
	public static final int A2DP_INFO_CONNECTING = 2;
	public static final int A2DP_INFO_CONNECTED = 3;
	public static final int A2DP_INFO_PLAY = 4;
	public static final int A2DP_INFO_PAUSED = 5;

	public static int mPlayStatus = A2DP_INFO_INITIAL;

	public static String mBtName = "";
	public static String mBtPin = "";

	public static String mID3Name = "";
	public static String mID3Artist = "";
	public static String mID3Album = "";

	public static int mTotalTime = 0;
	public static int mCurTime = 0;

	public static boolean mGpsRunAfter = false;

	private void doBTCmd(Intent intent) {
		int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, 0);

		switch (cmd) {
		case MyCmd.Cmd.BT_SEND_A2DP_STATUS: {
			int play = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);

			String name = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA2);
			String pin = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA3);

			if (name != null) {
				mBtName = name;
			}
			if (pin != null) {
				mBtPin = pin;
			}
			if (GlobalDef.mAutoTest) {
				BroadcastUtil.sendToCarService(mContext,
						MyCmd.Cmd.AUTO_TEST_RESULT, MyCmd.SOURCE_BT,
						(mBtName == null) ? 1 : 0);
			}

			if (play != mPlayStatus) {

				if (BTMusicUI.mUI[0] != null
						&& (!BTMusicUI.mUI[0].mPause || mGpsRunAfter)
						&& play == A2DP_INFO_CONNECTED
						&& mPlayStatus < A2DP_INFO_CONNECTED) {
					doKeyControl(MyCmd.Keycode.PLAY);
					GlobalDef.reactiveSource(mContext, BTMusicUI.SOURCE,
							BTMusicService.mAudioFocusListener);
					mGpsRunAfter = false;
				} else {
					if (mGpsRunAfter && GlobalDef.mSource == BTMusicUI.SOURCE
							&& play == A2DP_INFO_CONNECTED
							&& mPlayStatus < A2DP_INFO_CONNECTED) {
						doKeyControl(MyCmd.Keycode.PLAY);
						GlobalDef.reactiveSource(mContext, BTMusicUI.SOURCE,
								BTMusicService.mAudioFocusListener);
						mGpsRunAfter = false;
					}
				}

				mPlayStatus = play;
				// updateConnectView();
			}

			// setPlayButtonStatus(mPlayStatus == A2DP_INFO_PLAY);

		}
			break;
		case MyCmd.Cmd.BT_SEND_ID3_INFO: {
			mID3Name = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA);
			mID3Artist = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA2);
			mID3Album = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA3);
			// updateView();
		}
			break;
		case MyCmd.Cmd.BT_SEND_TIME_STATUS: {
			mCurTime = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
			mTotalTime = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA2, 0);
			// updateView();
		}
			break;
		case MyCmd.Cmd.BT_SEND_A2DP_CUR_FOLDER: {

			if (BTMusicUI.mUI[0] != null && !BTMusicUI.mUI[0].mPause) {
				String s = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA);
				BTMusicUI.mUI[0].updateListRoot(s);
			}

			if (BTMusicUI.mUI[1] != null && !BTMusicUI.mUI[1].mPause) {
				String s = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA);
				BTMusicUI.mUI[1].updateListRoot(s);
			}

			// updateView();
		}
			break;
		case MyCmd.Cmd.BT_SEND_A2DP_LIST_INFO: {

			if (BTMusicUI.mUI[0] != null && !BTMusicUI.mUI[0].mPause) {
				BTMusicUI.mUI[0].updateList(intent);
			}

			if (BTMusicUI.mUI[1] != null && !BTMusicUI.mUI[1].mPause) {
				BTMusicUI.mUI[0].updateList(intent);
			}

			// updateView();
		}
			break;
		case MyCmd.Cmd.BT_CARPLAY_STATUS: {

			int caplay = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
			Log.d(TAG, "MyCmd.Cmd.BT_CARPLAY_STATUS caplay:"+caplay);
			if (caplay == 0) {
				GlobalDef.rerequestAudioFocusByCarPlayOff();
			}
		}
			break;
		default:
			return;
		}
		returnInfoToUI(cmd);
	}

	private static boolean mPausedByTransientLossOfFocus = false;
	public static OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
			// AudioFocus is a new feature: focus updates are made verbose on
			// purpose

			Log.v(TAG, "onAudioFocusChange " + focusChange);

			BTMusicService mServiceBase = BTMusicService.getInstanse();
			if (mServiceBase == null) {
				return;
			}
			switch (focusChange) {
			
			case AudioManager.AUDIOFOCUS_LOSS:
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				if (!GlobalDef.isAudioFocusGPS()) {
					if (GlobalDef.mSource == BTMusicUI.SOURCE) {
						if (mServiceBase.mPlayStatus == BTMusicService.A2DP_INFO_PLAY) {
							mPausedByTransientLossOfFocus = true;
							mServiceBase.doKeyControl(MyCmd.Keycode.PAUSE);
						}
						GlobalDef.setSource(GlobalDef.mContext,
								MyCmd.SOURCE_OTHERS_APPS);
					}
				}
				break;
			case AudioManager.AUDIOFOCUS_GAIN:
				if (mPausedByTransientLossOfFocus) {
					if ((mServiceBase.mPlayStatus == BTMusicService.A2DP_INFO_PAUSED)) {
						mPausedByTransientLossOfFocus = false;
						mServiceBase.doKeyControl(MyCmd.Keycode.PLAY);
					}
					GlobalDef.setSource(GlobalDef.mContext, BTMusicUI.SOURCE);
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
		return BTMusicUI.SOURCE;
	}

	// easyconnect

	public static final String BROADCAST_CONNECTION_BREAK = "net.easyconn.connection.break";
	public static final String BROADCAST_BT_CHECKSTATUS = "net.easyconn.bt.checkstatus";
	public static final String BROADCAST_BT_CONNECTED = "net.easyconn.bt.connected";
	public static final String BROADCAST_BT_NOT_CONNECTED = "net.easyconn.bt.notconnected";
	public static final String BROADCAST_BT_CONNECT = "net.easyconn.bt.connect";
	public static final String BROADCAST_BT_A2DP_ACQUIRE = "net.easyconn.a2dp.acquire";
	public static final String BROADCAST_BT_A2DP_ACQUIRE_OK = "net.easyconn.a2dp.acquire.ok";
	public static final String BROADCAST_BT_A2DP_RELEASE = "net.easyconn.a2dp.release";
	public static final String BROADCAST_BT_A2DP_RELEASE_OK = "net.easyconn.a2dp.release.ok";
	public static final String BROADCAST_APP_QUIT = "net.easyconn.app.quit";
	public static final String BROADCAST_BT_OPENED = "net.easyconn.bt.opened";

	BroadcastReceiver mEasyConnectBrocastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Intent brocastIntent = new Intent();
			String action = intent.getAction();

			if (action.equals(BROADCAST_BT_CHECKSTATUS)) {
				// if(BTService.getBluetoothStatus() == 0){
				// Intent startIntent = new
				// Intent(getApplicationContext(),BluetoothApplication.class);
				// startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// startActivity(startIntent);
				// }else{
				if (mPlayStatus >= BTMusicService.A2DP_INFO_CONNECTING) {
					brocastIntent.setAction(BROADCAST_BT_CONNECTED);
					mContext.sendBroadcast(brocastIntent);
				} else {
					// mBluetooth.sendBTCommand(Bluetooth.BT_USER_READ_LOC_DEV_NAME);
					// mBluetooth.sendBTCommand(Bluetooth.BT_USER_READ_PIN_CODE);
					// mBluetooth.sendBTCommand(Bluetooth.BT_USER_READ_LOC_DEV_ADDR);
					// mATBluetooth.write(ATBluetooth.REQUEST_NAME);
					// mATBluetooth.write(ATBluetooth.REQUEST_PIN);

					brocastIntent.setAction(BROADCAST_BT_OPENED);
					brocastIntent.putExtra("name", mBtName);
					brocastIntent.putExtra("pin", mBtPin);
					// brocastIntent.putExtra("mac",String.format("%02x:%02x:%02x:%02x:%02x:%02x",
					// mBluetooth.mLocDevAddr[0] &
					// 0xff,mBluetooth.mLocDevAddr[1] & 0xff,
					// mBluetooth.mLocDevAddr[2] &
					// 0xff,mBluetooth.mLocDevAddr[3] & 0xff,
					// mBluetooth.mLocDevAddr[4] &
					// 0xff,mBluetooth.mLocDevAddr[5] & 0xff));
					// Log.d(TAG,"mEasyConnectBrocastReceiver name = " +
					// mBluetooth.mLocDevName+",pin = "+mBluetooth.mPinCode+",mac="+
					// String.format("%02x:%02x:%02x:%02x:%02x:%02x",
					// mBluetooth.mLocDevAddr[0] &
					// 0xff,mBluetooth.mLocDevAddr[1] & 0xff,
					// mBluetooth.mLocDevAddr[2] &
					// 0xff,mBluetooth.mLocDevAddr[3] & 0xff,
					// mBluetooth.mLocDevAddr[4] &
					// 0xff,mBluetooth.mLocDevAddr[5] & 0xff));
					mContext.sendBroadcast(brocastIntent);
				}
				// }
			} else if (action.equals(BROADCAST_BT_A2DP_ACQUIRE)) {
				// MyServiceBinder.setMusic(true);

				GlobalDef.reactiveSource(mContext, BTMusicUI.SOURCE,
						BTMusicService.mAudioFocusListener);

				brocastIntent.setAction(BROADCAST_BT_A2DP_ACQUIRE_OK);
				mContext.sendBroadcast(brocastIntent);
			} else if (action.equals(BROADCAST_BT_A2DP_RELEASE)) {
				// mBluetooth.sendMSCCommand(Bluetooth.MSC_MUSIC_OFF);
				if(GlobalDef.mSource == BTMusicUI.SOURCE){
					BroadcastUtil.sendToCarServiceSetSource(mContext, MyCmd.SOURCE_MX51);
				}
				brocastIntent.setAction(BROADCAST_BT_A2DP_RELEASE_OK);
				mContext.sendBroadcast(brocastIntent);
			} else if (action.equals(BROADCAST_APP_QUIT)) {
				// MyServiceBinder.setMusic(false);
				if(GlobalDef.mSource == BTMusicUI.SOURCE){
					BroadcastUtil.sendToCarServiceSetSource(mContext, MyCmd.SOURCE_MX51);
				}
			}
		}
	};

	private void registerEC() {

		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(BROADCAST_BT_CHECKSTATUS);
		mIntentFilter.addAction(BROADCAST_BT_A2DP_ACQUIRE);
		mIntentFilter.addAction(BROADCAST_BT_A2DP_RELEASE);
		mIntentFilter.addAction(BROADCAST_APP_QUIT);
		mContext.registerReceiver(mEasyConnectBrocastReceiver, mIntentFilter);
	}

	private void unregisterEC() {

		mContext.unregisterReceiver(mEasyConnectBrocastReceiver);
	}
}
