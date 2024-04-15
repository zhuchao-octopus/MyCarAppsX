package com.octopus.android.carapps.appwidget;

import android.appwidget.AppWidgetProvider;
/**
 * Created by Comp on 01.05.2015.
 */
public class MediaAppWidgetProvider extends AppWidgetProvider {

	public final static String ACTION_WIDGET_KEY_PREX = "com.car.ui.keyprex";
	public final static String ACTION_WIDGET_MUSIC = ACTION_WIDGET_KEY_PREX
			+ "m=";
	public final static String ACTION_WIDGET_RADIO = ACTION_WIDGET_KEY_PREX
			+ "r=";
	public final static String ACTION_WIDGET_BT_MUSIC = ACTION_WIDGET_KEY_PREX
			+ "b=";
	public final static String ACTION_WIDGET_DVD = ACTION_WIDGET_KEY_PREX
			+ "d=";

//	private static final int TIME_LAYOUT = 0;
//	private static final int MUSIC_LAYOUT = 1;
//	private static final int FM_LAYOUT = 2;
//	private static final int BLUETOOTH_LAYOUT = 3;
//	private static final int IPOD_LAYOUT = 4;
//
//	public static final int SWITCH_ON = 10520;
//	public static final int SWITCH_OFF = 10521;
//
//	private static boolean IsHour24;
//	private static final String ACTION_SECONDS_TICK = "com.car.ui.second_update";
//
//	private static boolean digitalTimeFlash;
//	private static int dateformattype = -1;
//	private final static Intent secondIntent = new Intent(ACTION_SECONDS_TICK);
//
//	final String LOG_TAG = "MediaAppWidgetProvider";
//
//	private volatile static int mTopMode = TIME_LAYOUT;
//
//	// private RemoteViews widgetView;
//	private static RemoteViews timeView;
//	private static RemoteViews musicView;
//	private static RemoteViews fmView;
//	private static RemoteViews bluetoothView;
//	private int[] mTopLayout = { R.id.timelayout, R.id.musiclayout,
//			R.id.fmlayout, R.id.bluetoothlayout };
//
//	@Override
//	public void onEnabled(Context context) {
//		super.onEnabled(context);
//		Log.d(LOG_TAG, "onEnabled");
//	}
//
//	@Override
//	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
//			int[] appWidgetIds) {
//		super.onUpdate(context, appWidgetManager, appWidgetIds);
//	}
//
//	@Override
//	public void onDeleted(Context context, int[] appWidgetIds) {
//		super.onDeleted(context, appWidgetIds);
//		Log.d(LOG_TAG, "onDeleted " + Arrays.toString(appWidgetIds));
//	}
//
//	@Override
//	public void onDisabled(Context context) {
//		super.onDisabled(context);
//		Log.d(LOG_TAG, "onDisabled");
//	}
//
//	private String createInfo(final Intent intent) {
//		final Bundle bundle = intent.getExtras();
//
//		if (bundle == null) {
//			return "EMPTY";
//		}
//
//		final Set<String> keySet = bundle.keySet();
//		if (keySet.size() == 0) {
//			return "EMPTY";
//		}
//
//		final StringBuilder builder = new StringBuilder();
//
//		for (final String key : keySet) {
//			builder.append(key).append(": ").append(bundle.get(key))
//					.append("\n");
//		}
//
//		return builder.toString();
//	}
//
//	private void updateView(Context context) {
//		displayTop(context, mTopMode);
//	}
//
//	@Override
//	public void onReceive(Context context, Intent intent) {
//		context = context.getApplicationContext();
//		checkViews(context);
//		String action = intent.getAction();
//		Log.v(LOG_TAG, "receive_" + action + "\n" + createInfo(intent));
//
//		if (action.equals(MyCmd.BROADCAST_CMD_FROM_MUSIC)) {
//			processMusic(context, intent);
//		} else if (action.equals(MyCmd.BROADCAST_ACTIVITY_STATUS)) {
//			String s = intent.getStringExtra(MyCmd.EXTRA_COMMON_CMD);
//			if (!s.startsWith("com.android.launcher")) {
//				stopColonFlash();
//			} else {
//				startColonFlash(context);
//			}
//		} else if (action.equals(AppWidgetManager.ACTION_APPWIDGET_ENABLED)) {
//			displayTop(context, TIME_LAYOUT);
//		} else if (action.equals(AppWidgetManager.ACTION_APPWIDGET_DELETED)) {
//			stopColonFlash();
//		} else if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
//			updateView(context);
//		} else if (action.equals(AppWidgetManager.ACTION_APPWIDGET_DISABLED)) {
//			stopColonFlash();
//		} else if (action.equals(ACTION_SECONDS_TICK)) {
//			flashColon(context);
//			startColonFlash(context);
//		} else if (action.equals(Intent.ACTION_TIME_CHANGED)) {
//			if (mTopMode == TIME_LAYOUT) {
//				displayTop(context, TIME_LAYOUT);
//			}
//		} else if (action.equals(MyCmd.BROADCAST_CMD_FROM_BT)) {
//			processBluetooth(context, intent);
//		} else if (action.equals(MyCmd.BROADCAST_CAR_SERVICE_SEND)) {
//			int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, 0);
//			switch (cmd) {
//			case MyCmd.Cmd.MCU_RADIO_RECEIVE_DATA:
//				processRadio(context, intent);
//
//				break;
//			case MyCmd.Cmd.SOURCE_CHANGE:
//			case MyCmd.Cmd.RETURN_CURRENT_SOURCE:
//				mSource = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA,
//						MyCmd.SOURCE_NONE);
//				updateSource(context);
//				break;
//			}
//		}
//
//	}
//
//	private void processMusic(final Context context, final Intent intent) {
//
//		if (mSource == MyCmd.SOURCE_MUSIC) {
//			int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, 0);
//			int curtime = 0;
//			int totaltime = 0;
//			Bitmap bmp = null;
//
//			if (cmd == MyCmd.Cmd.MUSIC_SEND_PLAY_STATUS) {
//
//				int data = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
//
//				bmp = (Bitmap) intent.getExtra(MyCmd.EXTRA_COMMON_OBJECT);
//
//				if (bmp != null) {
//					musicView.setImageViewBitmap(R.id.musicalumb, (Bitmap) bmp);
//				} else {
//					musicView.setImageViewResource(R.id.musicalumb,
//							R.drawable.ico_music);
//				}
//				String name = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA4);
//
//				musicView.setTextViewText(R.id.musicname, name);
//
//				if (data != 1) {
//					musicView.setViewVisibility(R.id.musicplay, View.VISIBLE);
//					musicView.setViewVisibility(R.id.musicpause, View.GONE);
//				} else {
//					musicView.setViewVisibility(R.id.musicplay, View.GONE);
//					musicView.setViewVisibility(R.id.musicpause, View.VISIBLE);
//				}
//
//			}
//
//			if (cmd == MyCmd.Cmd.MUSIC_SEND_PLAY_STATUS
//					|| cmd == MyCmd.Cmd.MUSIC_SEND_PLAY_TIME) {
//				curtime = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA2, -1);
//				totaltime = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA3, -1);
//
//				musicView.setTextViewText(R.id.musiccurtime,
//						stringForTime(curtime));
//				musicView.setTextViewText(R.id.musicdurtime,
//						stringForTime(totaltime));
//				musicView.setProgressBar(R.id.music_progress, totaltime,
//						curtime, false);
//			}
//
//			updateMusicLayout(context);
//		}
//
//	}
//
//	public static final int A2DP_INFO_INITIAL = 0;
//	public static final int A2DP_INFO_READY = 1;
//	public static final int A2DP_INFO_CONNECTING = 2;
//	public static final int A2DP_INFO_CONNECTED = 3;
//	public static final int A2DP_INFO_PLAY = 4;
//	public static final int A2DP_INFO_PAUSED = 5;
//
//	private void processBluetooth(final Context context, final Intent intent) {
//
//		int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, 0);
//
//		switch (cmd) {
//		case MyCmd.Cmd.BT_SEND_A2DP_STATUS: {
//			int state = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
//
//			if (state != A2DP_INFO_PLAY) {
//				bluetoothView.setViewVisibility(R.id.bluetoothplay,
//						View.VISIBLE);
//				bluetoothView.setViewVisibility(R.id.bluetoothpause, View.GONE);
//			} else {
//				bluetoothView.setViewVisibility(R.id.bluetoothplay, View.GONE);
//				bluetoothView.setViewVisibility(R.id.bluetoothpause,
//						View.VISIBLE);
//			}
//
//			if (state < 3) {
//				bluetoothView.setTextViewText(R.id.bluetooth_title, "");
//			}
//			updateBluetoothLayout(context);
//		}
//			break;
//		case MyCmd.Cmd.BT_SEND_ID3_INFO: {
//			String title = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA);
//			bluetoothView.setTextViewText(R.id.bluetooth_title, title);
//
//			title = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA3);
//			bluetoothView.setTextViewText(R.id.bluetooth_albumn, title);
//			updateBluetoothLayout(context);
//		}
//			break;
//		case MyCmd.Cmd.BT_SEND_TIME_STATUS: {
//
//			// updateView();
//		}
//		default:
//			return;
//		}
//
//	}
//
//	private long lastTime;
//
//	public int mCurPlayIndex = 0;
//
//	public static final int MRD_FM1 = 0;
//	public static final int MRD_FM2 = 1;
//	public static final int MRD_FM3 = 2;
//	public static final int MRD_AM = 3;
//	public static final int MRD_AM2 = 3;
//
//	int mFMNum = 1;
//
//	public String getCurBaundText(int baud) {
//		String s;
//		int index = 1;
//
//		if (MachineConfig.VALUE_SYSTEM_UI22_1050
//				.equals(GlobalDef.getSystemUI())) {
//			index = mCurPlayIndex / 6;
//			if (baud >= MRD_AM) {
//				s = "AM";
//
//				if (index < 0 || index > 2) {
//					index = 0;
//				}
//			} else {
//				s = "FM";
//				if (index < 0 || index > 5) {
//					index = 0;
//				}
//			}
//
//			s += (index + 1);
//		} else {
//			if (baud >= MRD_AM) {
//				s = "AM";
//				index = baud - MRD_AM;
//			} else {
//				s = "FM";
//				index = baud - MRD_FM1;
//			}
//
//			if (mFMNum == 3) {
//				s += (index + 1);
//			}
//		}
//		return s;
//	}
//
//	private Bitmap getRadioBitmap(Context context, int level) {
//		try {
//			String packageName = context.getPackageName();
//			int j = context.getResources().getIdentifier("num_radio_" + level,
//					"drawable", packageName);
//			Drawable d = context.getDrawable(j);
//			return ((BitmapDrawable) d).getBitmap();
//		} catch (Exception localException) {
//		}
//
//		return null;
//	}
//
//	private void processRadio(Context context, Intent intent) {
//		if (mTopMode != FM_LAYOUT) {
//			return;
//		}
//
//		int freq = 0;
//		int channel = intent.getIntExtra("channel", 1);
//		int freqMin = intent.getIntExtra("freqmin", 85);
//		int freqMax = intent.getIntExtra("freqmax", 100);
//		int band = 0;
//
//		byte[] buf = intent.getByteArrayExtra(MyCmd.EXTRA_COMMON_DATA);
//		if (buf != null && buf.length > 2) {
//			switch (buf[1]) {
//			case 0x2:
//				if (buf.length > 11) {
//					channel = (int) (buf[3] & 0xff);
//					band = buf[2];
//					freq = (short) ((((int) buf[4] & 0xff) << 8) | ((int) buf[5]) & 0xff);
//					freqMin = (short) ((((int) buf[8] & 0xff) << 8) | ((int) buf[9]) & 0xff);
//					freqMax = (short) ((((int) buf[10] & 0xff) << 8) | ((int) buf[11]) & 0xff);
//				}
//				break;
//			}
//		}
//
//		if (freq == 0) {
//			return;
//		}
//
//		int fmTypeResource;
//
//		final long currentTime = System.currentTimeMillis();
//		if ((currentTime - lastTime) < 60 * 5000) {
//			return;
//		}
//
//		lastTime = currentTime;
//
//		if (band <= 2) {
//			fmTypeResource = findFmResource(context, "fm" + (band + 1));
//			fmView.setImageViewResource(R.id.fmband2, R.drawable.num_mhz);
//
//			fmView.setViewVisibility(R.id.num_dot, View.VISIBLE);
//			if (freq >= 10000) {
//				fmView.setViewVisibility(R.id.radionum1, View.VISIBLE);
//				fmView.setImageViewBitmap(R.id.radionum1,
//						getRadioBitmap(context, 1));
//			} else {
//				fmView.setViewVisibility(R.id.radionum1, View.GONE);
//			}
//			fmView.setViewVisibility(R.id.radionum2, View.VISIBLE);
//
//			fmView.setImageViewBitmap(R.id.radionum2,
//					getRadioBitmap(context, (freq / 1000) % 10));
//
//			fmView.setImageViewBitmap(R.id.radionum3,
//					getRadioBitmap(context, (freq / 100) % 10));
//
//			fmView.setImageViewBitmap(R.id.radionum4,
//					getRadioBitmap(context, (freq / 10) % 10));
//
//			fmView.setImageViewBitmap(R.id.radionum5,
//					getRadioBitmap(context, freq % 10));
//
//		} else {
//			fmTypeResource = findFmResource(context, "am" + (band - 2));
//			fmView.setImageViewResource(R.id.fmband2, R.drawable.num_khz);
//
//			fmView.setViewVisibility(R.id.radionum1, View.GONE);
//			fmView.setViewVisibility(R.id.num_dot, View.GONE);
//
//			if (freq >= 1000) {
//				fmView.setViewVisibility(R.id.radionum2, View.VISIBLE);
//
//				fmView.setImageViewBitmap(R.id.radionum2,
//						getRadioBitmap(context, (freq / 1000) % 10));
//			} else {
//				fmView.setViewVisibility(R.id.radionum2, View.GONE);
//			}
//
//			fmView.setImageViewBitmap(R.id.radionum3,
//					getRadioBitmap(context, (freq / 100) % 10));
//			fmView.setImageViewBitmap(R.id.radionum4,
//					getRadioBitmap(context, (freq / 10) % 10));
//			fmView.setImageViewBitmap(R.id.radionum5,
//					getRadioBitmap(context, freq % 10));
//		}
//
//		fmView.setImageViewResource(R.id.fmband1, fmTypeResource);
//
//		// fmView.setImageViewBitmap(R.id.fmfre, getFmBitmap(context, str3));
//
//		fmView.setViewVisibility(R.id.fmfre, View.VISIBLE);
//
//		updateFmLayout(context);
//	}
//
//	private void displayTop(Context context, int paramInt) {
//		mTopMode = paramInt;
//
//		if (mTopMode == TIME_LAYOUT) {
//			updateTimeBar(context);
//			startColonFlash(context);
//		} else {
//			stopColonFlash();
//			switch (mTopMode) {
//			case MUSIC_LAYOUT:
//				updateMusicLayout(context);
//				break;
//			case FM_LAYOUT:
//				updateFmLayout(context);
//				break;
//			case BLUETOOTH_LAYOUT:
//				updateBluetoothLayout(context);
//				break;
//			}
//		}
//	}
//
//	private void checkFormat(Context context) {
//		if (dateformattype == -1) {
//			IsHour24 = DateFormat.is24HourFormat(context);
//
//			String s = SystemConfig.getProperty(context,
//					SystemConfig.KEY_DATE_FORMAT);
//			if (s == null) {
//				if (MachineConfig.VALUE_SYSTEM_UI16_7099.equals(GlobalDef
//						.getSystemUI())) {
//					s = "MM/dd/yyyy";
//				} else if (MachineConfig.VALUE_SYSTEM_UI41_2007
//						.equals(GlobalDef.getSystemUI())) {
//					s = "dd/MM/yyyy";
//				} else {
//					s = "yyyy/MM/dd";
//				}
//			}
//
//			if (s.equals("MM/dd/yyyy")) {
//				dateformattype = 1;
//			} else if (s.equals("dd/MM/yyyy")) {
//				dateformattype = 2;
//			} else if (s.equals("yyyy/MM/dd")) {
//				dateformattype = 0;
//			} else {
//				dateformattype = 2;
//			}
//		}
//	}
//
//	private void updateTimeBar(Context context) {
//		Log.i(LOG_TAG, "updateTimeBar");
//		checkFormat(context);
//		Time time = new Time();
//		time.setToNow();
//		updateDataWidget();
//		updateTime(context, time);
//	}
//
//	private void updateDataWidget() {
//		if (dateformattype == 1) {
//			timeView.setCharSequence(R.id.textclock, "setFormat12Hour",
//					"EEEE MM/dd/yyyy aa");
//			timeView.setCharSequence(R.id.textclock, "setFormat24Hour",
//					"EEEE MM/dd/yyyy");
//			return;
//		}
//		if (this.dateformattype == 2) {
//			timeView.setCharSequence(R.id.textclock, "setFormat12Hour",
//					"EEEE dd/MM/yyyy aa");
//			timeView.setCharSequence(R.id.textclock, "setFormat24Hour",
//					"EEEE dd/MM/yyyy");
//			return;
//		}
//
//		timeView.setCharSequence(R.id.textclock, "setFormat12Hour",
//				"EEEE yyyy/MM/dd aa");
//		timeView.setCharSequence(R.id.textclock, "setFormat24Hour",
//				"EEEE yyyy/MM/dd");
//	}
//
//	private static int mTimeMinute = 0;
//
//	private void updateTime(Context context, Time paramTime) {
//
//		if ((!IsHour24) && (paramTime.hour > 12)) {
//			paramTime.hour = (-12 + paramTime.hour);
//		}
//
//		timeView.setImageViewResource(R.id.hour1,
//				findTimeResource(context, paramTime.hour / 10 % 10));
//		timeView.setImageViewResource(R.id.hour2,
//				findTimeResource(context, paramTime.hour % 10));
//		timeView.setImageViewResource(R.id.minute1,
//				findTimeResource(context, paramTime.minute / 10 % 10));
//		timeView.setImageViewResource(R.id.minute2,
//				findTimeResource(context, paramTime.minute % 10));
//
//		mTimeMinute = paramTime.minute;
//
//		updateTimeLayout(context);
//
//	}
//
//	private void startColonFlash(Context context) {
//		stopColonFlash();
//		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_COLON, context),
//				1000);
//	}
//
//	private void stopColonFlash() {
//		mHandler.removeMessages(MSG_COLON);
//	}
//
//	private static final int MSG_COLON = 0;
//	private static Handler mHandler = new Handler() {
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case MSG_COLON:
//				Context context = (Context) msg.obj;
//				// mHandler.removeMessages(MSG_COLON);
//				// mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_COLON,
//				// context), 1000);
//				context.sendBroadcast(secondIntent);
//				break;
//			}
//			super.handleMessage(msg);
//		}
//	};
//
//	private void flashColon(Context context) {
//		Log.i(LOG_TAG, digitalTimeFlash + ":flashColon:" + this);
//		int visible;
//		if (digitalTimeFlash) {
//			digitalTimeFlash = false;
//			visible = View.INVISIBLE;
//		} else {
//			digitalTimeFlash = true;
//			visible = View.VISIBLE;
//		}
//		timeView.setViewVisibility(R.id.colon, visible);
//
//		Time time = new Time();
//		time.setToNow();
//		if (mTimeMinute != time.minute) {
//			updateTime(context, time);
//		}
//
//		updateTimeLayout(context);
//	}
//
//	private int findTimeResource(Context context, int number) {
//		try {
//			String packageName = context.getPackageName();
//			int j = context.getResources().getIdentifier("num_time_" + number,
//					"drawable", packageName);
//			return j;
//		} catch (Exception localException) {
//		}
//		return R.drawable.num_time_0;
//	}
//
//	private String stringForTime(int timeMs) {
//		int totalSeconds = timeMs / 1000;
//
//		int seconds = totalSeconds % 60;
//		int minutes = (totalSeconds / 60) % 60;
//		int hours = totalSeconds / 3600;
//
//		if (hours > 0) {
//			return String.format("%d:%02d:%02d", hours, minutes, seconds)
//					.toString();
//		} else {
//			return String.format("%02d:%02d", minutes, seconds).toString();
//		}
//	}
//
//	private String getFmstr(int value) {
//		Log.i("LauncherGetFM", "value = " + value);
//		int i = value / 10000;
//		if (i == 0) {
//			Log.i("LauncherGetFM", "i = 0");
//			int i1 = value / 1000;
//			int i2 = value / 100 % 10;
//			int i3 = value / 10 % 10;
//			int i4 = value % 10;
//			Log.i("LauncherGetFM", "i1 = " + i1 + ", i2 = " + i2 + ", i3 = "
//					+ i3 + ", i4 = " + i4);
//			return "" + i1 + i2 + "." + i3 + i4;
//		}
//		int j = value / 1000 % 10;
//		int k = value / 100 % 10;
//		int m = value / 10 % 10;
//		int n = value % 10;
//		Log.i("LauncherGetFM", "i = " + i + ", j = " + j + ", k = " + ", m = "
//				+ m + ", n = " + n);
//		return "" + i + j + k + "." + m + n;
//	}
//
//	private int findFmResource(Context context, String postfix) {
//		try {
//			String packageName = context.getPackageName();
//			int j = context.getResources().getIdentifier("num_" + postfix,
//					"drawable", packageName);
//			return j;
//		} catch (Exception localException) {
//		}
//		return R.drawable.num_fm1;
//	}
//
//	private void checkViews(Context context) {
//		// if (widgetView == null) {
//		// widgetView = new RemoteViews(context.getPackageName(),
//		// R.layout.widget);
//		// }
//		if (timeView == null) {
//			timeView = new RemoteViews(context.getPackageName(),
//					R.layout.timelayout);
//			setTimeButtonsAction(context, timeView);
//		}
//		if (musicView == null) {
//			musicView = new RemoteViews(context.getPackageName(),
//					R.layout.musiclayout);
//			setMusicButtonsAction(context, musicView);
//		}
//		if (fmView == null) {
//			fmView = new RemoteViews(context.getPackageName(),
//					R.layout.fmlayout);
//			setFmButton(context, fmView);
//		}
//		if (bluetoothView == null) {
//			bluetoothView = new RemoteViews(context.getPackageName(),
//					R.layout.bluetoothlayout);
//			setBluetoothButton(context, bluetoothView);
//		}
//	}
//
//	private void updateTimeLayout(Context context) {
//		if (mTopMode != TIME_LAYOUT) {
//			return;
//		}
//
//		updateWidget(context, timeView);
//	}
//
//	private void updateMusicLayout(Context context) {
//		if (mTopMode != MUSIC_LAYOUT) {
//			return;
//		}
//		// setMusicButtonsAction(context, musicView);
//		updateWidget(context, musicView);
//	}
//
//	private void setMusicButtonsAction(Context context, RemoteViews view) {
//
//		Intent it;
//		PendingIntent pendingIntent;
//
//		it = new Intent(ACTION_WIDGET_MUSIC + MyCmd.Keycode.PREVIOUS);
//		pendingIntent = PendingIntent.getBroadcast(context, 0, it, 0);
//
//		view.setOnClickPendingIntent(R.id.musicpre, pendingIntent);
//
//		it = new Intent(ACTION_WIDGET_MUSIC + MyCmd.Keycode.NEXT);
//		pendingIntent = PendingIntent.getBroadcast(context, 0, it, 0);
//		view.setOnClickPendingIntent(R.id.musicnext, pendingIntent);
//
//		it = new Intent(ACTION_WIDGET_MUSIC + MyCmd.Keycode.PLAY_PAUSE);
//		pendingIntent = PendingIntent.getBroadcast(context, 0, it, 0);
//		view.setOnClickPendingIntent(R.id.musicplay, pendingIntent);
//		view.setOnClickPendingIntent(R.id.musicpause, pendingIntent);
//
//		Intent startMusic = new Intent(ACTION_MAIN);
//		startMusic.setComponent(new ComponentName("com.car.ui",
//				"com.my.audio.MusicActivity"));
//		pendingIntent = PendingIntent.getActivity(context, 0, startMusic, 0);
//		view.setOnClickPendingIntent(R.id.musicalumb, pendingIntent);
//	}
//
//	private void setTimeButtonsAction(Context context, RemoteViews view) {
//
//		PendingIntent pendingIntent;
//
//		Intent startMusic = new Intent(ACTION_MAIN);
//		startMusic.setComponent(new ComponentName("com.android.deskclock",
//				"com.android.deskclock.DeskClock"));
//		pendingIntent = PendingIntent.getActivity(context, 0, startMusic, 0);
//		view.setOnClickPendingIntent(R.id.timeclock, pendingIntent);
//	}
//
//	private void updateFmLayout(Context context) {
//		if (mTopMode != FM_LAYOUT) {
//			return;
//		}
//
//		// setFmButton(context, fmView);
//		updateWidget(context, fmView);
//	}
//
//	private void setFmButton(Context context, RemoteViews view) {
//
//		Intent it;
//		PendingIntent pendingIntent;
//
//		it = new Intent(ACTION_WIDGET_RADIO + MyCmd.Keycode.PREVIOUS);
//		pendingIntent = PendingIntent.getBroadcast(context, 0, it, 0);
//
//		view.setOnClickPendingIntent(R.id.fmpre, pendingIntent);
//
//		it = new Intent(ACTION_WIDGET_RADIO + MyCmd.Keycode.NEXT);
//		pendingIntent = PendingIntent.getBroadcast(context, 0, it, 0);
//		view.setOnClickPendingIntent(R.id.fmnext, pendingIntent);
//
//		Intent startMusic = new Intent(ACTION_MAIN);
//		startMusic.setComponent(new ComponentName("com.car.ui",
//				"com.my.radio.RadioActivity"));
//		pendingIntent = PendingIntent.getActivity(context, 0, startMusic, 0);
//		view.setOnClickPendingIntent(R.id.fmfre, pendingIntent);
//
//	}
//
//	private void updateBluetoothLayout(Context context) {
//		if (mTopMode != BLUETOOTH_LAYOUT) {
//			return;
//		}
//
//		// setBluetoothButton(context, bluetoothView);
//		updateWidget(context, bluetoothView);
//	}
//
//	private void setBluetoothButton(Context context, RemoteViews view) {
//
//		Intent it;
//		PendingIntent pendingIntent;
//
//		it = new Intent(ACTION_WIDGET_BT_MUSIC + MyCmd.Keycode.PREVIOUS);
//		pendingIntent = PendingIntent.getBroadcast(context, 0, it, 0);
//
//		view.setOnClickPendingIntent(R.id.bluetoothpre, pendingIntent);
//
//		it = new Intent(ACTION_WIDGET_BT_MUSIC + MyCmd.Keycode.NEXT);
//		pendingIntent = PendingIntent.getBroadcast(context, 0, it, 0);
//		view.setOnClickPendingIntent(R.id.bluetoothnext, pendingIntent);
//
//		it = new Intent(ACTION_WIDGET_BT_MUSIC + MyCmd.Keycode.PLAY_PAUSE);
//		pendingIntent = PendingIntent.getBroadcast(context, 0, it, 0);
//		view.setOnClickPendingIntent(R.id.bluetoothplay, pendingIntent);
//		view.setOnClickPendingIntent(R.id.bluetoothpause, pendingIntent);
//
//		Intent startMusic = new Intent(ACTION_MAIN);
//		startMusic.setComponent(new ComponentName("com.car.ui",
//				"com.my.btmusic.BTMusicActivity"));
//		pendingIntent = PendingIntent.getActivity(context, 0, startMusic, 0);
//		view.setOnClickPendingIntent(R.id.bluetoothalumb, pendingIntent);
//
//	}
//
//	private void updateWidget(Context context, RemoteViews view) {
//		ComponentName appWidget = new ComponentName(context,
//				MediaAppWidgetProvider.class);
//		AppWidgetManager awm = AppWidgetManager.getInstance(context);
//		awm.updateAppWidget(appWidget, view);
//	}
//
//	// //////////allen
//	private static int mSource;
//
//	private void updateSource(Context context) {
//		switch (mSource) {
//		case MyCmd.SOURCE_MUSIC:
//			displayTop(context, MUSIC_LAYOUT);
//			break;
//		case MyCmd.SOURCE_RADIO:
//			displayTop(context, FM_LAYOUT);
//			break;
//		case MyCmd.SOURCE_BT_MUSIC:
//			displayTop(context, BLUETOOTH_LAYOUT);
//			break;
//		default:
//			displayTop(context, TIME_LAYOUT);
//			break;
//		}
//	}

}
