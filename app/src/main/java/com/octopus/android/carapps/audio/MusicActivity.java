/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.octopus.android.carapps.audio;

import com.common.util.BroadcastUtil;
import com.common.util.Kernel;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.adapter.MyListViewAdapter;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.player.ComMediaPlayer;
import com.octopus.android.carapps.common.utils.ResourceUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

/**
 * This activity plays a video from a specified URI.
 */
public class MusicActivity extends Activity {

	final static String TAG = "MusicActivity";
	MusicUI mMusicUI;
	private static MusicActivity mThis;

	private boolean mStop = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		boolean multiWindow = ResourceUtil.updateAppUi(this);
		multiWindow = ResourceUtil.isMultiWindow(this);
		if (multiWindow) {
			GlobalDef.updateMultiWindownActivity(this);
		}
		// Configuration c = getResources().getConfiguration();
		// c.smallestScreenWidthDp = 327;
		// getResources().updateConfiguration(c, null);//test

		super.onCreate(savedInstanceState);
		// DisplayMetrics dm = getResources().getDisplayMetrics();

		// Log.d(TAG, dm.widthPixels+"onCreate"+isInMultiWindowMode());

		setContentView(R.layout.screen0_music_layout);

		// setContentView(R.layout.screen0_music_layout);

		mMusicUI = mMusicUI.getInstanse(this, findViewById(R.id.screen1_main),
				0);

		updateIntent(this.getIntent());
		mThis = this;

		mMusicUI.onCreate();

	}

	private void updateIntent(Intent it) {
//		Log.d(TAG, "updateIntent:" + it);
		if (it != null) {
			int page = it.getIntExtra("page", 0);
//			Log.d(TAG, "updateIntent:" + page);
			if (page != 0) {
				mMusicUI.mStartActivityNewPage = page;
			} else {
				Log.d(TAG, it.getData() + ":updateIntent:" + it.getType());
				if ("audio".equals(it.getType())) {
					Uri uri = it.getData();
					if (uri != null) {
						mMusicUI.addExternalFiles(uri.getPath());
					}
				}
			}
			
			page = it.getIntExtra("first_poweron", 0);
			if (page != 0) {
				mMusicUI.mFirstPowerOn = page;
			}
			
			if (Util.isAndroidQ()){
				it.putExtra("page", 0);
				it.putExtra("first_poweron", 0);
				it.setDataAndType(null, null);
				setIntent(it);
			}
		}
	}

	@Override
	protected void onNewIntent(Intent it) {		
		updateIntent(it);
		setIntent(it);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d(TAG, "onStart");
		mStop = false;
		if (Util.isAndroidQ()) {
			updateIntent(getIntent());
		}
		if (mMusicUI != null)
			mMusicUI.onResume();

		mMusicUI.mGpsRunAfter = GlobalDef.openGps(this, getIntent());
		setIntent(null);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d(TAG, "onStop");
		
		mStop = true;
		if (mMusicUI != null)
			mMusicUI.onPause();
	}

	public boolean mUpdateUIResource = false;
	@Override
	protected void onResume() {
		if (mUpdateUIResource) {
			ResourceUtil.updateAppUi(this);
			mUpdateUIResource = false;
		}
		super.onResume();
		resumeConfiguration = getResources().getConfiguration();
//		Log.d(TAG, "onResume"+getCurrentFocus());

	}

	protected void onPause() {
		super.onPause();
//		Log.d(TAG, "onPause"+getCurrentFocus());
		// if (mMusicUI != null)
		// mMusicUI.onPause();
		setIntent(null);
		if (Util.isAndroidQ()) {
			mMusicUI.mStartActivityNewPage = 0;
		}
	}

	public static void finishActivity(Context context) {
		if (mThis != null) {
			if (mThis.mMusicUI != null) {
				mThis.mMusicUI.mWillDestory = true;
			}
			BroadcastUtil.sendToCarServiceSetSource(mThis, MyCmd.SOURCE_MX51);
			mThis.finish();
		}
	}
	
	public static void setIntentForAndroidQ(Intent it) {
		if (mThis != null) {
			mThis.setIntent(it);
		}
	}

	public static void updateBySystemConfig(boolean f) {
		if (mThis != null) {
			ResourceUtil.updateAppUi(mThis);
			// boolean restart = false;
			// if (mThis.mMusicUI != null)
			// restart = !mThis.mMusicUI.mPause;
			//
			if (f) {
				mThis.finish();
			} else {
				if (mThis.mMusicUI != null){
					mThis.mUpdateUIResource = mThis.mMusicUI.mPause; 
				}
			}
			// if(restart){
			// UtilSystem.doRunActivity(mThis, "com.car.ui",
			// "com.my.audio.MusicActivity");
			// }
		}
	}

	protected void onDestroy() {		
		super.onDestroy();
		mHandler.removeCallbacks(recreateRunnable);
		if (mMusicUI != null)
			mMusicUI.onDestroy();

		// BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
		if (mThis == this) {
			mThis = null;
		}

		if (GlobalDef.mSource == MusicUI.SOURCE) {
			BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
		}

//		Log.d("aaaaa", "onMultiWindowModeChanged");
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (mMusicUI != null) {
			mMusicUI.mWillDestory = true;
		}
		BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
	}

	private ComMediaPlayer mMediaPlayer;

	@Override
	public boolean dispatchKeyEvent(KeyEvent arg0) {
		// TODO Auto-generated method stub
		Log.d("ccdd", arg0.getAction() + "   dispatchKeyEvent:"
				+ getCurrentFocus());


//		View v = getCurrentFocus();
//		if (v.getId() == R.id.music_page_scroll_view){
//			v.clearFocus();
//		}
		
		int keyCode = arg0.getKeyCode();

		if (arg0.getAction() == KeyEvent.ACTION_UP) {

			Log.d("ccdd", "down:" + getCurrentFocus());
			switch (keyCode) {
//			case KeyEvent.KEYCODE_DPAD_DOWN:
//			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				return true;
			default:
				return super.dispatchKeyEvent(arg0);
			}
		} else if (arg0.getAction() == KeyEvent.ACTION_DOWN) {
			Log.d("ccdd", "down:" + getCurrentFocus());
			switch (keyCode) {
//			case KeyEvent.KEYCODE_DPAD_DOWN:
//			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				return keyevntTranslate(keyCode);
			default:
				return super.dispatchKeyEvent(arg0);
			}
		}

		return super.dispatchKeyEvent(arg0);
	}

	// public boolean onKeyDown(int keyCode, KeyEvent event) {

	// Log.d("ccdd", ""+getCurrentFocus());
	// switch (keyCode) {
	// // case KeyEvent.KEYCODE_DPAD_UP: {
	// // return doButtonUp();
	// //
	// // }
	// case KeyEvent.KEYCODE_F7:
	// return rollKeyTranslate2(MyCmd.Keycode.KEY_ROOL_LEFT);
	// case KeyEvent.KEYCODE_F8:
	// return rollKeyTranslate2(MyCmd.Keycode.KEY_ROOL_RIGHT);
	// case KeyEvent.KEYCODE_DPAD_DOWN:
	// case KeyEvent.KEYCODE_DPAD_UP:
	// case KeyEvent.KEYCODE_DPAD_LEFT:
	// case KeyEvent.KEYCODE_DPAD_RIGHT:
	// return keyevntTranslate(keyCode);
	// case KeyEvent.KEYCODE_TAB:
	// return rollKeyTranslate(event.isShiftPressed() ?
	// MyCmd.Keycode.KEY_ROOL_LEFT
	// : MyCmd.Keycode.KEY_ROOL_RIGHT);
	// default:
	// return super.onKeyDown(keyCode, event);
	// }
	// return true;
	// }

	private int[] LIST_ID = new int[] { R.id.tv_all_list, R.id.tv_sd_list,
			R.id.tv_sd2_list, R.id.tv_usb_list, R.id.tv_usb2_list,
			R.id.tv_usb3_list, R.id.tv_usb4_list };

	private int getVisibleListView() {
		for (int i : LIST_ID) {
			View v = findViewById(i);
			if (v != null) {
				if (v.getVisibility() == View.VISIBLE) {
					return i;
				}
			}
		}
		return 0;
	}

	private int isNeedToRoll(int id, int keyCode) {
		int ret = 0;

		if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef
				.getSystemUI())
				|| MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef
						.getSystemUI())) {

			
			if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (id == R.id.btn_local || id == R.id.btn_usb || id == R.id.btn_usb2 || id == R.id.btn_usb3 || id == R.id.btn_usb4 || id == R.id.btn_sd || id == R.id.btn_sd2) {
                    ret = R.id.prev;
                }
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (id == R.id.tv_all_list) {
                    ret = R.id.next;
                }
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (id == R.id.tv_all_list) {
                    ret = R.id.tv_all_list;
                }
			}
			
			if (ret == 0){
				if (id == R.id.music_page_scroll_view){
					ret = R.id.btn_add_all;
					View v = getCurrentFocus();
					if (v.getId() == R.id.music_page_scroll_view){
						v.clearFocus();
					}
				}
			}
			

		} if (ResourceUtil.baseSW >= 320 &&  ResourceUtil.baseSW < 340){
			if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (id == R.id.prev || id == R.id.pp) {
                    ret = getVisibleListView();
                }
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (id == R.id.tv_all_list || id == R.id.tv_sd_list || id == R.id.tv_sd2_list || id == R.id.tv_usb_list || id == R.id.tv_usb2_list || id == R.id.tv_usb3_list || id == R.id.tv_usb4_list) {
                    ret = R.id.prev;
                }
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (id == R.id.tv_all_list || id == R.id.tv_sd_list || id == R.id.tv_sd2_list || id == R.id.tv_usb_list || id == R.id.tv_usb2_list || id == R.id.tv_usb3_list || id == R.id.tv_usb4_list) {
                    ret = R.id.prev;
                }
			}

		
			
		}
		
		
		return ret;
	}

	private boolean keyevntTranslateLR(int keyCode) {
		boolean ret = false;
		View v = getCurrentFocus();
		if (v != null) {

			int id = isNeedToRoll(v.getId(), keyCode);
			if (id != 0) {
				v.clearFocus();
				findViewById(id).requestFocus();
				ret = true;
			}

		}
		return ret;

	}

	private boolean keyevntTranslate(int keyCode) {
		boolean ret = false;
		View v = getCurrentFocus();

		if (v != null) {

			int id = isNeedToRoll(v.getId(), keyCode);
			if (id != 0) {
				v.clearFocus();
				findViewById(id).requestFocus();
				ret = true;
			}

		}

		return ret;

	}

	private boolean rollKeyTranslate2(int keyCode) {
		boolean ret = false;
		View v = getCurrentFocus();

		int nextFocus = 0;
		int id = 0;
		if (v != null) {
			id = v.getId();
		}
		if (id == R.id.tv_all_list || id == R.id.tv_local_list
				|| id == R.id.tv_sd_list || id == R.id.tv_sd2_list
				|| id == R.id.tv_usb_list || id == R.id.tv_usb2_list
				|| id == R.id.tv_usb3_list || id == R.id.tv_usb4_list) {
			int key = 0;

			View child = ((ListView) v).getSelectedView();
			if (child != null) {
				MyListViewAdapter adapter = (MyListViewAdapter) (((ListView) v)
						.getAdapter());
				MyListViewAdapter.ViewHolder vh = (MyListViewAdapter.ViewHolder) child
						.getTag();

				if (child != null) {
					switch (keyCode) {
					case MyCmd.Keycode.KEY_ROOL_RIGHT:

						if (vh.index < adapter.getCount() - 1) {
							key = Kernel.KEY_DOWN;
						}
						break;
					case MyCmd.Keycode.KEY_ROOL_LEFT:
						if (vh.index > 0) {
							key = Kernel.KEY_UP;
						}
						break;
					}
				}
				ret = true;
			} else {
				key = Kernel.KEY_DOWN;
			}

			if (key != 0) {
				if (key < Kernel.KEY_MAX) {
					Kernel.doKeyEvent(key);
					ret = true;
				}
			} else if (nextFocus != 0) {
				v.clearFocus();
				findViewById(nextFocus).requestFocus();
				ret = true;
			}
		} else {
			id = R.id.tv_all_list;
			if (findViewById(R.id.tv_all_list).getVisibility() == View.VISIBLE) {
				id = R.id.tv_all_list;
			} else if (findViewById(R.id.tv_local_list).getVisibility() == View.VISIBLE) {
				id = R.id.tv_local_list;
			} else if (findViewById(R.id.tv_sd_list).getVisibility() == View.VISIBLE) {
				id = R.id.tv_sd_list;
			} else if (findViewById(R.id.tv_sd2_list).getVisibility() == View.VISIBLE) {
				id = R.id.tv_sd2_list;
			} else if (findViewById(R.id.tv_usb_list).getVisibility() == View.VISIBLE) {
				id = R.id.tv_usb_list;
			} else if (findViewById(R.id.tv_usb2_list).getVisibility() == View.VISIBLE) {
				id = R.id.tv_usb2_list;
			} else if (findViewById(R.id.tv_usb3_list).getVisibility() == View.VISIBLE) {
				id = R.id.tv_usb3_list;
			} else if (findViewById(R.id.tv_usb4_list).getVisibility() == View.VISIBLE) {
				id = R.id.tv_usb4_list;
			}
			findViewById(id).requestFocus();
		}

		return ret;

	}

	private boolean rollKeyTranslate(int keyCode) {
		boolean ret = false;
		View v = getCurrentFocus();

		int nextFocus = 0;
		if (v != null) {
			int id = v.getId();// isNeedToRoll(v.getId(), keyCode);
			int key = 0;

			switch (keyCode) {
			case MyCmd.Keycode.KEY_ROOL_RIGHT:
                // case R.id.home:
                // case R.id.prev:
                // case R.id.pp:
                // case R.id.next:
                // case R.id.repeat:
                // case R.id.shuffle:
                // key = Kernel.KEY_RIGHT;
                // break;
                // case R.id.up:
                // nextFocus = R.id.down;
                // break;
                if (id == R.id.full) {
                    key = Kernel.KEY_MAX;
                }
				break;
			case MyCmd.Keycode.KEY_ROOL_LEFT:
                // case R.id.back:
                // case R.id.pp:
                // case R.id.next:
                // case R.id.repeat:
                // case R.id.shuffle:
                // key = Kernel.KEY_LEFT;
                // break;
                // case R.id.down:
                // nextFocus = R.id.up;
                // break;
                if (id == R.id.btn_all) {
                    key = Kernel.KEY_MAX;
                }
				break;
			}

			/*switch (id) {
			case R.id.tv_all_list:
			case R.id.tv_sd_list:
			case R.id.tv_sd2_list:
			case R.id.tv_usb_list:
			case R.id.tv_usb2_list:
			case R.id.tv_usb3_list:
			case R.id.tv_usb4_list:
				if (mMediaPlayer == null) {
					mMediaPlayer = MediaPlaybackService.getMediaPlayer();
					if (mMediaPlayer == null) {
						break;
					}
				}
				View child = ((ListView) v).getSelectedView();
				if (child != null) {
					MyListViewAdapter.ViewHolder vh = (MyListViewAdapter.ViewHolder) child
							.getTag();
					if (child != null) {
						switch (keyCode) {
						case MyCmd.Keycode.KEY_ROOL_RIGHT:

							if (vh.index < mMediaPlayer.getCurSongNum() - 1) {
								key = Kernel.KEY_DOWN;
							}
							break;
						case MyCmd.Keycode.KEY_ROOL_LEFT:
							if (vh.index > 0) {
								key = Kernel.KEY_UP;
							}
							break;
						}
					}
					ret = true;
				} else {
					key = Kernel.KEY_DOWN;
				}
				break;

			}*/
			if (key != 0) {
				if (key < Kernel.KEY_MAX) {
					Kernel.doKeyEvent(key);
					ret = true;
				}
			} else if (nextFocus != 0) {
				v.clearFocus();
				findViewById(nextFocus).requestFocus();
				ret = true;
			}
		}

		return ret;

	}

	private MotionEvent mMotionEventDelayed = null;
	private final Handler mHandler = new Handler(Looper.myLooper()) {

		@Override
		public void handleMessage(Message msg) {
			if (mMotionEventDelayed != null) {
				dispatchTouchEvent(mMotionEventDelayed);
			}
		}
	};

	private boolean mFor3Finger = false;
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if (GlobalDef.mTouch3Switch) {			
			if (ev.getAction() == MotionEvent.ACTION_DOWN) {
				if (mMotionEventDelayed == null) {
					mMotionEventDelayed = ev;//.copy();
					mHandler.removeMessages(0);
					mHandler.sendEmptyMessageDelayed(0, 60);
					return true;
				} else {
					mMotionEventDelayed = null;
				}
			} else if (ev.getAction() == MotionEvent.ACTION_POINTER_3_DOWN) {
				mHandler.removeMessages(0);
				mFor3Finger = true;
				ev.setAction(MotionEvent.ACTION_UP);
				return super.dispatchTouchEvent(ev);
			} else if (ev.getAction() == MotionEvent.ACTION_UP) {
				mMotionEventDelayed = null;
				mHandler.removeMessages(0);
				if (mFor3Finger) {
					mFor3Finger = false;
					return true;
				}
			}

			if (mFor3Finger || mMotionEventDelayed != null) {
				return true;
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	private Configuration resumeConfiguration = null;
	private int recreateScreenWidthDp = 0;
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
//			Log.d(TAG, "onConfigurationChanged\n" + newConfig + "\n" + resumeConfiguration);
		if (resumeConfiguration != null	&& newConfig != null
			&& resumeConfiguration.screenWidthDp != newConfig.screenWidthDp) {
			Log.w(TAG, "onConfigurationChanged " + resumeConfiguration.screenWidthDp  + ", " + newConfig.screenWidthDp + ", " + recreateScreenWidthDp + ", " + ResourceUtil.isMultiWindow(this));
			if (recreateScreenWidthDp != newConfig.screenWidthDp && !ResourceUtil.isMultiWindow(this)) {
				mHandler.postDelayed(recreateRunnable, 500);
			}
			recreateScreenWidthDp = newConfig.screenWidthDp; 
		}
	}

	private Runnable recreateRunnable = new Runnable(){
		@Override
		public void run() {
			Log.e(TAG, "do recreate");
			recreate();
		}
	};
}
