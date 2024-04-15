package com.octopus.android.carapps.screen1.launcher;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;

import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.common.obd.OBD;
import com.octopus.android.carapps.common.ui.UIBase;


public class Screen1SaverUI extends UIBase {

	private static final int TYPE0 = 0;
	private static final int TYPE1 = 1;
	private static final int TYPE2 = 0;
	private static final int TYPE3 = 0;

	public static Screen1SaverUI getInstanse(Context context, View view,
			int index) {
		if (index >= MAX_DISPLAY) {
			return null;
		}

		Screen1SaverUI ui = new Screen1SaverUI(context, view, index);

		return ui;
	}

	public Screen1SaverUI(Context context, View view, int index) {
		super(context, view, index);
	}

	private View mViewHour;
	private View mViewMinutus;

	private int mType;

	public void onCreate() {
		mViewHour = mMainView.findViewById(R.id.clock_hour);
		mViewMinutus = mMainView.findViewById(R.id.clock_minute);

		mMainView.findViewById(R.id.screen_save_click).setOnLongClickListener(
				new OnLongClickListener() {
					@Override
					public boolean onLongClick(View arg0) {
						// TODO Auto-generated method stub
						return true;
					}
				});

		int value = MachineConfig
				.getPropertyIntReadOnly(MachineConfig.KEY_OBD_SHOW_IN_SCREEN1);
		if(value == 1){
			mIsObdExist = true;
		}
		
//		mHandlerObd.sendEmptyMessageDelayed(0xffff, 2000); //test
		// registerReceiver();
	}

	private void setType(int type) {

	}

	Handler mHandler = new Handler(Looper.myLooper()) {
		public void handleMessage(Message msg) {
			updateTime();
			super.handleMessage(msg);
		}
	};

	private void updateTime() {

		if (mViewHour != null) {
			// long time = System.currentTimeMillis();
			// final Calendar mCalendar = Calendar.getInstance();
			// mCalendar.setTimeInMillis(time);
			// int h = mCalendar.get(Calendar.HOUR);
			// int m = mCalendar.get(Calendar.MINUTE);
			// int s = mCalendar.get(Calendar.SECOND);
			//
			// if (h > 12) {
			// h -= 12;
			// }

			Date curDate = new Date(System.currentTimeMillis());
			int h = curDate.getHours();

			String strTimeFormat = Settings.System.getString(
					mContext.getContentResolver(),
					android.provider.Settings.System.TIME_12_24);

			if ("12".equals(strTimeFormat)) {
				if (h > 12) {
					h -= 12;
				}
			}

			int m = curDate.getMinutes();
			int s = curDate.getSeconds();

			float rotate;

			rotate = ((h * 60 + m) / 720.0f) * 360.0f;// (h * 360.0f) / 12.0f;

			mViewHour.setRotation(rotate);

			rotate = (m * 360.0f) / 60.0f;

			mViewMinutus.setRotation(rotate);

			mHandler.removeMessages(0);
			
			int second = ((60 - s ) % 60);
			if(second == 0){
				second = 60;		
			}
			
			mHandler.sendEmptyMessageDelayed(0, second * 1000);

		}

	}

	private OBD mObd;
	private boolean mIsObdExist = false;

	private Handler mHandlerObd = new Handler(Looper.myLooper()) {
		@Override
		public void handleMessage(Message msg) {
			float angle;
			Log.d("abc", ""+msg.what);
			switch (msg.what) {
			case OBD.OBD_RPM:
				float rpm = (float) msg.obj;
				angle = (int) (rpm * 360 / OBD.OBD_RPM_MAX);
				udateOBD(R.id.obd_rpm, angle);
				break;
			case OBD.OBD_SPEED:
				float speed = (float) msg.obj;
				angle = (int) (speed * 360 / OBD.OBD_SPEED_MAX);
				udateOBD(R.id.obd_speed, angle);
				break;
			case OBD.OBD_QUERY_INFO:				
				queryInfo();
				break;
			case 0xffff: //test
				mTestRPM=(mTestRPM+1)%255;
				mTestSpeed=(mTestSpeed+100)%16380;

				mHandlerObd.sendEmptyMessageDelayed(0xffff, 1000);

				mHandlerObd.sendMessageDelayed(mHandlerObd.obtainMessage(OBD.OBD_SPEED, mTestRPM), 10);
				mHandlerObd.sendMessageDelayed(mHandlerObd.obtainMessage(OBD.OBD_RPM, mTestSpeed), 10);
				break;
			}
		}
	};
	
	private float mTestRPM = 0;
	private float mTestSpeed = 0;

	private void queryInfo(){
		if(mObd!=null){
			mObd.queryInfo();
		}
		if(!mPause){
			mHandlerObd.sendEmptyMessageDelayed(OBD.OBD_QUERY_INFO, 2000);
		}
	}
	private void udateOBD(int id, float angle) {
		View v = mMainView.findViewById(id);
		if(v!=null){
			v.setRotation(angle);
		}
	}

	private void initOBD() {
		View v = mMainView.findViewById(R.id.obd_layout);
		if (v != null) {
			if (mIsObdExist) {
				if (mObd == null) {
					mObd = new OBD();
				}
				if (!mObd.mConnected) {
					mObd.connect();
				}
				mObd.setCallback(mHandlerObd);
				v.setVisibility(View.VISIBLE);
				queryInfo();
			} else {
				v.setVisibility(View.GONE);
			}
		}
	}

	public void onPause() {
		super.onPause();

		unregisterReceiver();
		if (mObd != null) {
			mObd.setCallback(null);
		}
	}

	public void onResume() {
		super.onResume();
		updateTime();
		registerReceiver();

		initOBD();
	}

	private BroadcastReceiver mBroadcastReceiver;

	private void unregisterReceiver() {
		if (mBroadcastReceiver != null) {
			mContext.unregisterReceiver(mBroadcastReceiver);
			mBroadcastReceiver = null;
		}
	}

	private void registerReceiver() {
		if (mBroadcastReceiver == null) {
			mBroadcastReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {

					if (intent.getAction().equals(Intent.ACTION_TIME_CHANGED)) {
						updateTime();
					}
				}
			};
		}

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_TIME_CHANGED);

		mContext.registerReceiver(mBroadcastReceiver, intentFilter);
	}

	public void udpateWallPaper() {
	}
}
