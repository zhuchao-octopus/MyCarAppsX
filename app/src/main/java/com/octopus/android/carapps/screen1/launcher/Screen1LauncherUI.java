package com.octopus.android.carapps.screen1.launcher;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.common.util.AppConfig;
import com.common.util.MyCmd;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.common.presentation.PresentationUI;
import com.octopus.android.carapps.common.ui.UIBase;

public class Screen1LauncherUI extends UIBase implements View.OnClickListener {

	public static Screen1LauncherUI getInstanse(Context context, View view,
			int index) {
		if (index >= MAX_DISPLAY) {
			return null;
		}

		Screen1LauncherUI ui = new Screen1LauncherUI(context, view, index);

		return ui;
	}

	public Screen1LauncherUI(Context context, View view, int index) {
		super(context, view, index);
	}

	private static final int[] BUTTON_ON_CLICK = new int[] { R.id.button_radio,
			R.id.button_bluetooth, R.id.button_bluetooth_music,
			R.id.button_dvd, R.id.button_music, R.id.button_video,
			R.id.button_bluetooth_auxin };

	public void onCreate() {

		for (int i : BUTTON_ON_CLICK) {
			View v = mMainView.findViewById(i);
			if (v != null) {
				v.setOnClickListener(this);
			}
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		resetHideAppUI();
		prepareScreenSaver();
	}

	private void resetHideAppUI() {

		boolean hideDvd = AppConfig.isHidePackage("com.my.dvd.DVDPlayer");
		
//		View v =  mMainView
//				.findViewById(R.id.all_apps_scrolview);
//
//		if (v != null) {
//			if (v instanceof HorizontalScrollView) {
//				HorizontalScrollView new_name = (HorizontalScrollView) v;
//
//				DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
//
//				int width = dm.widthPixels;
//
//				if (!hideDvd) {
//
//					if (dm.widthPixels == 1024 && dm.heightPixels == 600) {
//						width = 1028;
//					} else {
//						width = 960;
//					}
//
//				}
//				
//				
//				ViewGroup.LayoutParams lp;
//				lp = v.getLayoutParams();
//				lp.width = mApplications.size() * ICON_SIZE;
//				mLayout.setLayoutParams(lp);
//				
//			}
//		}
		

		if (!hideDvd) {
			mMainView.findViewById(R.id.button_dvd).setVisibility(View.VISIBLE);
		} else {
			mMainView.findViewById(R.id.button_dvd).setVisibility(View.GONE);
		}
	}

	public void onPause() {
		super.onPause();
		mHandler.removeMessages(0);
	}

	public void onClick(View view) {
		int source = MyCmd.SOURCE_NONE;
        int id = view.getId();
        if (id == R.id.button_radio) {
            source = MyCmd.SOURCE_RADIO;
        } else if (id == R.id.button_music) {
            source = MyCmd.SOURCE_MUSIC;
        } else if (id == R.id.button_dvd) {
            source = MyCmd.SOURCE_DVD;
        } else if (id == R.id.button_video) {// PresentationUI.updateVideoShow(true);
            source = MyCmd.SOURCE_VIDEO;
        } else if (id == R.id.button_bluetooth) {
            source = MyCmd.SOURCE_BT;
        } else if (id == R.id.button_bluetooth_music) {
            source = MyCmd.SOURCE_BT_MUSIC;
        } else if (id == R.id.button_bluetooth_auxin) {
            source = MyCmd.SOURCE_AUX;
        } else {
            return;
        }
		if (source != MyCmd.SOURCE_NONE) {
			// BroadcastUtil.sendToCarServiceSetSource(mContext, source);
			PresentationUI mPresentationUI = PresentationUI.getInstanse();
			if (mPresentationUI != null) {
				mPresentationUI.update(source, true);
			}
		}
	}

	private final static int SCREEN_SAVER_TIME = 60000;

	private void prepareScreenSaver() {
		mHandler.removeMessages(0);
		mHandler.sendEmptyMessageDelayed(0, SCREEN_SAVER_TIME);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {

			super.handleMessage(msg);
		}
	};

}
