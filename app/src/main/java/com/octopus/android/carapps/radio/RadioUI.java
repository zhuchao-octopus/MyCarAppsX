package com.octopus.android.carapps.radio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.common.util.UtilCarKey;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.car.ui.GlobalDef;
import com.octopus.android.carapps.common.ui.UIBase;


public class RadioUI extends UIBase implements View.OnClickListener {

	public static final int PROCESSBUTTONSTATUS = 0x00;

	public static final int SOURCE = MyCmd.SOURCE_RADIO;

	private static RadioUI[] mUI = new RadioUI[MAX_DISPLAY];

	public static RadioUI getInstanse(Context context, View view, int index) {
		if (index >= MAX_DISPLAY) {
			return null;
		}

		mUI[index] = new RadioUI(context, view, index);

		return mUI[index];
	}

	public RadioUI(Context context, View view, int index) {
		super(context, view, index);
		mSource = SOURCE;
	}

	private static final int[] BUTTON_ON_LONG_CLICK = new int[] {
		R.id.btn_prev, R.id.btn_next,
	};
	
	private static final int[] BUTTON_ON_CLICK = new int[] {
			R.id.radio_function_button_scan,
			R.id.radio_function_button_seek_previous,
			R.id.radio_function_button_band, R.id.radio_function_button_amfm,
			R.id.radio_function_button_seek_next,
			R.id.radio_function_button_dx, R.id.eq, R.id.radio_step_up_button,
			R.id.radio_step_down_button, R.id.radio_function_button_reg,
			R.id.radio_function_button_af, R.id.radio_function_button_ta,
			R.id.radio_button_loc, R.id.radio_function_button_pty,
			R.id.radio_keyboard, R.id.btn_prev, R.id.btn_next, R.id.btn_list,
			R.id.favorit_back, R.id.radio_pty_layout, R.id.eq_mode_switch,

			R.id.radio_function_button_seek_previous_and_step,
			R.id.radio_function_button_seek_next_and_step, R.id.left_gps,
			R.id.back, R.id.home, R.id.radio_function_button_st,
			R.id.radio_button_loc2, R.id.radio_button_loc3, R.id.layout_set,
			R.id.bottom_set, R.id.channel_00, R.id.channel_01, R.id.channel_02,
			R.id.channel_03, R.id.channel_04, R.id.channel_05, R.id.channel_06,
			R.id.channel_07, R.id.channel_08, R.id.channel_09, R.id.channel_10,
			R.id.channel_11, R.id.channel_12, R.id.channel_13, R.id.channel_14,
			R.id.channel_15, R.id.channel_16, R.id.channel_17, R.id.channel_18,
			R.id.channel_19, R.id.channel_20, R.id.channel_21, R.id.channel_22,
			R.id.channel_23, R.id.channel_24, R.id.channel_25, R.id.channel_26,
			R.id.channel_27, R.id.channel_28, R.id.channel_29, R.id.fm_sel,
			R.id.am_sel,

			R.id.fm_sel2, R.id.am_sel2, R.id.radio_list_next,
			R.id.radio_list_prev, R.id.btn_band_prev, R.id.btn_band_next,
			R.id.btn_band_prev2, R.id.btn_band_next2,
			R.id.radio_function_button_ps_search};// ,R.id.mic,R.id.vol };

	// private static final int[][] VIEW_HIDE = new int[][] {
	// { R.id.common_status_bar_main }, { R.id.eq } };

	private static final int[] VIEW_HIDE2 = new int[] { R.id.flipper,
			R.id.rds_buttons };

	private static final int[] VIEW_INVISIBLE2 = new int[] { R.id.bottom_button_layout };

	public void updateFullUI() {
		if (mDisplayIndex == SCREEN1) {

			boolean fullFunction = true;
			if (mUI[0] != null && !mUI[0].mPause) {
				fullFunction = false;
			}

			for (int i = 0; i < VIEW_HIDE2.length; ++i) {
				View v = mMainView.findViewById(VIEW_HIDE2[i]);
				if (v != null) {
					if (!mRds && VIEW_HIDE2[i] == R.id.rds_buttons) {
						v.setVisibility(View.GONE);
					} else {
						v.setVisibility(fullFunction ? View.VISIBLE : View.GONE);
					}
				}
			}

			for (int i = 0; i < VIEW_INVISIBLE2.length; ++i) {
				View v = mMainView.findViewById(VIEW_INVISIBLE2[i]);
				if (v != null) {

					v.setVisibility(fullFunction ? View.VISIBLE
							: View.INVISIBLE);

				}
			}

			View v = mMainView.findViewById(R.id.radio_mark_face_view_mask);

			if (fullFunction) {
				v.setVisibility(View.GONE);
			} else {

				v.setVisibility(View.VISIBLE);
			}

		} else {
			if (mUI[1] != null && !mUI[1].mPause) {
				mUI[1].updateFullUI();
			}
		}
	}

	private void initPresentationUI() {
		for (int i : BUTTON_ON_CLICK) {
			View v = mMainView.findViewById(i);
			if (v != null) {
				if (i >= R.id.channel_00 && i <= R.id.channel_29) {
					v.setOnClickListener(mOnClickListenerChannel);
					v.setOnLongClickListener(mOnLongClickListenerChannel);
				} else {
					v.setOnClickListener(this);
					if (i == R.id.radio_function_button_seek_previous_and_step
							|| i == R.id.radio_function_button_seek_next_and_step) {
						v.setOnLongClickListener(mOnLongClickListenerSeekAndSetp);
					}
				}
			}

		}
		
		for (int i : BUTTON_ON_LONG_CLICK) {
			View v = mMainView.findViewById(i);
			if (v != null) {				
				v.setOnLongClickListener(mOnLongClickListener);				
			}
		}

		View v = mMainView.findViewById(R.id.channel_index_00);
		if (v != null) {
			String value = GlobalDef.getSystemUI();
			if (MachineConfig.VALUE_SYSTEM_UI22_1050.equals(value)
					|| MachineConfig.VALUE_SYSTEM_UI35_KLD813_2.equals(value)) {
				setTextView(R.id.channel_index_00, "P1");
				setTextView(R.id.channel_index_01, "P2");
				setTextView(R.id.channel_index_02, "P3");
				setTextView(R.id.channel_index_03, "P4");
				setTextView(R.id.channel_index_04, "P5");
				setTextView(R.id.channel_index_05, "P6");
				setTextView(R.id.channel_index_06, "P7");
				setTextView(R.id.channel_index_07, "P8");
				setTextView(R.id.channel_index_08, "P9");
				setTextView(R.id.channel_index_09, "P10");
				setTextView(R.id.channel_index_10, "P11");
				setTextView(R.id.channel_index_11, "P12");
				setTextView(R.id.channel_index_12, "P13");
				setTextView(R.id.channel_index_13, "P14");
				setTextView(R.id.channel_index_14, "P15");
				setTextView(R.id.channel_index_15, "P16");
				setTextView(R.id.channel_index_16, "P17");
				setTextView(R.id.channel_index_17, "P18");
				setTextView(R.id.channel_index_18, "P19");
				setTextView(R.id.channel_index_19, "P20");
				setTextView(R.id.channel_index_20, "P21");
				setTextView(R.id.channel_index_21, "P22");
				setTextView(R.id.channel_index_22, "P23");
				setTextView(R.id.channel_index_23, "P24");
				setTextView(R.id.channel_index_24, "P25");
				setTextView(R.id.channel_index_25, "P26");
				setTextView(R.id.channel_index_26, "P27");
				setTextView(R.id.channel_index_27, "P28");
				setTextView(R.id.channel_index_28, "P29");
				setTextView(R.id.channel_index_29, "P30");
			} else {
				setTextView(R.id.channel_index_00, "1");
				setTextView(R.id.channel_index_01, "2");
				setTextView(R.id.channel_index_02, "3");
				setTextView(R.id.channel_index_03, "4");
				setTextView(R.id.channel_index_04, "5");
				setTextView(R.id.channel_index_05, "6");
				setTextView(R.id.channel_index_06, "7");
				setTextView(R.id.channel_index_07, "8");
				setTextView(R.id.channel_index_08, "9");
				setTextView(R.id.channel_index_09, "10");
				setTextView(R.id.channel_index_10, "11");
				setTextView(R.id.channel_index_11, "12");
				setTextView(R.id.channel_index_12, "13");
				setTextView(R.id.channel_index_13, "14");
				setTextView(R.id.channel_index_14, "15");
				setTextView(R.id.channel_index_15, "16");
				setTextView(R.id.channel_index_16, "17");
				setTextView(R.id.channel_index_17, "18");
				setTextView(R.id.channel_index_18, "19");
				setTextView(R.id.channel_index_19, "20");
				setTextView(R.id.channel_index_20, "21");
				setTextView(R.id.channel_index_21, "22");
				setTextView(R.id.channel_index_22, "23");
				setTextView(R.id.channel_index_23, "24");
				setTextView(R.id.channel_index_24, "25");
				setTextView(R.id.channel_index_25, "26");
				setTextView(R.id.channel_index_26, "27");
				setTextView(R.id.channel_index_27, "28");
				setTextView(R.id.channel_index_28, "29");
				setTextView(R.id.channel_index_29, "30");
			}
		}

		String value = GlobalDef.getSystemUI();

		if (MachineConfig.VALUE_SYSTEM_UI_KLD11_200.equals(value)
				|| (GlobalDef.mRadioButtonType == GlobalDef.RADIO_BUTTON_TYPE_LONG_PRESS_SCAN)) {

			if (mMainView.findViewById(R.id.radio_function_button_scan) != null) {
				mMainView.findViewById(R.id.radio_function_button_scan)
						.setOnLongClickListener(new OnLongClickListener() {
							@Override
							public boolean onLongClick(View arg0) {
								// TODO Auto-generated method stub
								mAkRadio.sendMRDCommand(AkRadio.MRD_AS);
								return true;
							}
						});
			}
		} else if (MachineConfig.VALUE_SYSTEM_UI31_KLD7.equals(value)) {
			TextView tv = (TextView) mMainView.findViewById(R.id.freq_text);
			if (tv != null) {
				tv.setTextColor(0xffff0000);
			}

			tv = (TextView) mMainView.findViewById(R.id.freq_text_ps);
			if (tv != null) {
				tv.setTextColor(0xffff0000);
			}

			setChanne00Background(R.id.channel_00,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_01,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_02,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_03,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_04,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_05,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_06,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_07,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_08,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_09,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_10,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_11,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_12,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_13,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_14,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_15,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_16,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_17,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_18,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_19,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_20,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_21,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_22,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_23,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_24,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_25,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_26,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_27,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_28,
					R.drawable.radio_channel_button);
			setChanne00Background(R.id.channel_29,
					R.drawable.radio_channel_button);
		}

		if (mMainView.findViewById(R.id.radio_function_button_ps_search) != null) {
			mMainView.findViewById(R.id.radio_function_button_ps_search)
					.setOnLongClickListener(new OnLongClickListener() {
						@Override
						public boolean onLongClick(View arg0) {
							// TODO Auto-generated method stub
							mAkRadio.sendMRDCommand(AkRadio.MRD_AS);
							return true;
						}
					});
		}

		if (mMainView.findViewById(R.id.radio_step_up_button) != null) {
			mMainView.findViewById(R.id.radio_step_up_button)
					.setOnLongClickListener(new OnLongClickListener() {
						@Override
						public boolean onLongClick(View arg0) {
							// TODO Auto-generated method stub
							mAkRadio.sendMRDCommand(AkRadio.MRD_SEEK_UP);
							return true;
						}
					});
		}
		if (mMainView.findViewById(R.id.radio_step_down_button) != null) {
			mMainView.findViewById(R.id.radio_step_down_button)
					.setOnLongClickListener(new OnLongClickListener() {
						@Override
						public boolean onLongClick(View arg0) {
							// TODO Auto-generated method stub
							mAkRadio.sendMRDCommand(AkRadio.MRD_SEEK_DOWN);
							return true;
						}
					});
		}
	}

	private static final String TAG = "RadioActivity";

	// private static TextView current_frequency;
	private TextView mTVBand;
	private TextView mTVUnit;
	private ListView mPty_list;
	private GridView mGridViewPty_list;
	private ListView mListViewFreqs;
	private GridView mGirdViewFreqs;
	private GridView mGirdViewFavoriteList;
	private View mGirdViewFavoriteParent;
	// private TextView current_frequency_unit;
	private MarkFaceView mMarkFace;
	private ImageView mFreqDigital1;
	private ImageView mFreqDigital2;
	private ImageView mFreqDigital3;
	private ImageView mFreqDigitalp;
	private ImageView mFreqDigitalp1;
	private ImageView mFreqDigitalp2;

	private boolean mIsPtyShow;
	// private ViewFlipper mViewFlipper;
	private PTYListViewAdapter mAdapter;
	private MyListViewAdapter mFreqsAdapter;
	private MyListViewAdapter mFavoriteListAdapter;
	private AkRadio mAkRadio;// = new AkRadio();
	private TextView mPtyName;

	private int mCurrentButtonId = -1;

	private void initHardware() {

		RadioService.setHandler(mHandler, mDisplayIndex);

		if (mAkRadio == null) {
			mAkRadio = RadioService.getRadioHadware();

			if (mAkRadio != null) {

				mFreqsAdapter.setAkRadio(mAkRadio);
				if (mAkRadio.mMRDFreqency == 0) {
					int i = 12;
					while (i > 0) {
						i--;
						Util.doSleep(50);
						if (mAkRadio.mMRDFreqency != 0) {
							break;
						}
					}
					if (i == 0) {
						mAkRadio.mMRDFreqency = (short) getDataEx(RADIO_SAVE,
								KEY_FREQ);
						mAkRadio.mMRDBand = (byte) getDataEx(RADIO_SAVE,
								KEY_BAUD);
						if (mAkRadio.mMRDFreqency == 0) {
							mAkRadio.mMRDFreqency = 8750;
							mAkRadio.mMRDBand = AkRadio.MRD_FM1;
						}
					}
				}

				doUpdateFreqency();
				updateList();

				mHandler.postDelayed(new Runnable() {
					public void run() {
						if (mAkRadio != null) {
							mAkRadio.sendMRDCommand(AkRadio.MRD_QUERY_RADIO_INFO);
							mAkRadio.sendMRDCommand(AkRadio.MRD_QUERY_RDS_INFO);
						}
					}
				}, 100);
			}
		}

	}

	private void deinitHardware() {

		RadioService.setHandler(null, mDisplayIndex);
		if (mAkRadio != null) {

		}
		mAkRadio = null;
	}

	private boolean mRds;

	private void initRds() {
		String s = MachineConfig.getProperty(MachineConfig.KEY_RDS);
		mRds = !MachineConfig.VALUE_OFF.equals(s);
		if (mRds) {
			setViewVisible(mMainView, R.id.rds_buttons, View.VISIBLE);
			setViewVisible(mMainView, R.id.rds_texts, View.VISIBLE);
			setViewVisible(mMainView, R.id.rds_tag, View.VISIBLE);
		} else {
			setViewVisible(mMainView, R.id.rds_buttons, View.GONE);
			setViewVisible(mMainView, R.id.rds_texts, View.GONE);
			setViewVisible(mMainView, R.id.rds_tag, View.GONE);

			if (mAkRadio != null) {
				mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_TA_OFF);
				mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_AF_OFF);
			}

		}
	}

	public void onCreate() {
		super.onCreate();
		init();

		mPtyName = (TextView) mMainView
				.findViewById(R.id.radio_current_pty_type_name);

		initPresentationUI();
		initFocusChange();
	}

	private void init() {
		// mViewFlipper = (ViewFlipper) mMainView.findViewById(R.id.flipper);
		getListVisibleNum();
		mTVBand = (TextView) mMainView.findViewById(R.id.radio_fm_text);

		mTVUnit = (TextView) mMainView.findViewById(R.id.radio_unit_text);
		mMarkFace = (MarkFaceView) mMainView
				.findViewById(R.id.radio_mark_face_view);
		if (mMarkFace != null) {
			mMarkFace.setRaidoActivity(this);
		}
		mFreqDigital1 = (ImageView) mMainView.findViewById(R.id.rds_digit_1);
		mFreqDigital2 = (ImageView) mMainView.findViewById(R.id.rds_digit_2);
		mFreqDigital3 = (ImageView) mMainView.findViewById(R.id.rds_digit_3);
		mFreqDigitalp = (ImageView) mMainView.findViewById(R.id.rds_digit_p);
		mFreqDigitalp1 = (ImageView) mMainView.findViewById(R.id.rds_digit_p1);
		mFreqDigitalp2 = (ImageView) mMainView.findViewById(R.id.rds_digit_p2);

		int id = 0;
		if (mDisplayIndex == 0) {
			id = R.layout.tl_list_radio_freq;
		} else {
			if (GlobalDef.mScreen1Size == 0) {
				id = R.layout.tl_list_1024;
			} else {
				id = R.layout.tl_list_800;
			}
		}

		View v = mMainView.findViewById(R.id.radio_pty_list);
		if (v instanceof ListView) {
			mPty_list = (ListView) v;
		} else if (v instanceof GridView) {
			mGridViewPty_list = (GridView) v;
		}

		// ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		// android.R.layout.simple_list_item_1, mPty_data);
		mAdapter = new PTYListViewAdapter(mContext, id);
		if (mPty_list != null) {
			mPty_list.setAdapter(mAdapter);
			mPty_list.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// Log.e(TAG, " click position=" + position);
					mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_PTY_TYPE, position);

					showPtyList(false);
				}

			});
		} else if (mGridViewPty_list != null) {
			mGridViewPty_list.setAdapter(mAdapter);
			mGridViewPty_list.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// Log.e(TAG, " click position=" + position);
					mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_PTY_TYPE, position);
					showPtyList(false);
				}

			});
		}

		v = mMainView.findViewById(R.id.radio_frequency_list);
		if (v instanceof ListView) {
			mListViewFreqs = (ListView) v;
		} else if (v instanceof GridView) {
			mGirdViewFreqs = (GridView) v;
		}

		View vv = mMainView.findViewById(R.id.radio_frequency_scrollview);
		if (vv != null) {
			mHorizontalScrollView = (HorizontalScrollView) vv;
		}

		id = 0;
		if (mDisplayIndex == 0) {
			id = R.layout.tl_list_radio;
		} else {
			if (GlobalDef.mScreen1Size == 0) {
				id = R.layout.tl_list_radio_1024;
			} else {
				id = R.layout.tl_list_radio_800;
			}
		}

		mFreqsAdapter = new MyListViewAdapter(mContext, id);
		if (mListViewFreqs != null) {
			mListViewFreqs.setAdapter(mFreqsAdapter);
			mListViewFreqs.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (mAkRadio != null) {

						GlobalDef.reactiveSource(mContext, SOURCE,
								RadioService.mAudioFocusListener);
						mLongPressSeekButton = false;
						mAkRadio.sendMRDCommand(AkRadio.MRD_LIST_INDEX,
								position);
					}

				}

			});

			mListViewFreqs
					.setOnItemLongClickListener(new OnItemLongClickListener() {
						public boolean onItemLongClick(AdapterView<?> parent,
								View view, int position, long id) {

							mAkRadio.sendMRDCommand(AkRadio.MRD_SAVE, position);
							mAkRadio.addFreqPS(mAkRadio.mMRDFreqency,
									mAkRadio.mStrRdsPs);
							return true;
						}

					});
		} else if (mGirdViewFreqs != null) {
			mGirdViewFreqs.setAdapter(mFreqsAdapter);

			mGirdViewFreqs
					.setOnItemSelectedListener(new OnItemSelectedListener() {
						@Override
						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub

							if (mHorizontalScrollView instanceof MyScrollView) {

								int page = mFreqsAdapter.toCustomIndex(arg2);
								Log.d("abc", page + "");
								page = mFreqsAdapter.indexToPage(page);
								Log.d("abc", page + "");

								MyScrollView new_name = (MyScrollView) mHorizontalScrollView;
								new_name.scrollToPage(page);
							}

						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub
							// Log.d(TAG, "onNothingSelected");
						}
					});
			mGirdViewFreqs.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					final int pp = position;
					mHandler.post(new Runnable() {
						public void run() {
							if (mAkRadio != null) {

								GlobalDef.reactiveSource(mContext, SOURCE,
										RadioService.mAudioFocusListener);
								int new_pos = mFreqsAdapter.toCustomIndex(pp);
								mLongPressSeekButton = false;
								mAkRadio.sendMRDCommand(AkRadio.MRD_LIST_INDEX,
										new_pos);
							}
						}
					});

				}

			});

			mGirdViewFreqs
					.setOnItemLongClickListener(new OnItemLongClickListener() {
						public boolean onItemLongClick(AdapterView<?> parent,
								View view, int position, long id) {

							position = mFreqsAdapter.toCustomIndex(position);
							mAkRadio.sendMRDCommand(AkRadio.MRD_SAVE, position);
							mAkRadio.addFreqPS(mAkRadio.mMRDFreqency,
									mAkRadio.mStrRdsPs);
							return true;
						}

					});
		}

	}

	public void updateFreq(float freq) {
		if (mAkRadio != null && freq != -1) {
			short f;
			if (freq < 500) { // fm
				f = (short) (freq * 100);
			} else {
				f = (short) freq;
			}
			if (mAkRadio.mMRDFreqency != f) {
				mAkRadio.mMRDFreqency = f;
				mAkRadio.sendMRDCommand(AkRadio.MRD_WRITE_FREQENCY);
			}
		}
	}

	public void updateAmFm(byte mAmFm) {
		if (mAkRadio != null && mAmFm != -1) {
			if (mAkRadio.mMRDBand != mAmFm) {
				mAkRadio.mMRDBand = mAmFm;
				mAkRadio.sendMRDCommand(AkRadio.MRD_WRITE_BAND);
				mAmFm = -1;
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		// GlobalDef.requestAudioFocus(RadioService.mAudioFocusListener);
		// BroadcastUtil.sendToCarServiceSetSource(mContext, SOURCE);

		GlobalDef.reactiveSource(mContext, SOURCE,
				RadioService.mAudioFocusListener);

		BroadcastUtil.sendToCarServiceSetSource(mContext, SOURCE);
		Log.d(TAG, "onResume");

		// if (mAkRadio != null) {
		// mAkRadio.sendMRDCommand(AkRadio.MRD_QUERY_RADIO_INFO);
		// mAkRadio.sendMRDCommand(AkRadio.MRD_QUERY_RDS_INFO);
		// }
		initHardware();

		initRds();

		updateFullUI();
		GlobalDef.requestEQInfo();
		if (GlobalDef.mAutoTest && mDisplayIndex == 0) {
			mAutoToCarService = 0;
		}
		// mHandler.sendEmptyMessageDelayed(0xffffff, 500);

	}

	public boolean mWillDestory = false;

	@Override
	public void onPause() {
		super.onPause();

		if (mAkRadio != null) {
			saveData(KEY_FREQ, mAkRadio.mMRDFreqency);
			saveData(KEY_BAUD, mAkRadio.mMRDBand);
			deinitHardware();
		}

		if (mDisplayIndex == SCREEN0 && !mWillDestory) {
			updateFullUI();
		}
		stopFlipFreq();
	}

	private void doKeyBaud() {

		// mAkRadio.mMRDBand = (byte) ((mAkRadio.mMRDBand == 0) ? 3 : 0);
		// mAkRadio.sendMRDCommand(AkRadio.MRD_WRITE_BAND);
		mAkRadio.doKeyBaud();
	}

	public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.radio_function_button_dx) {
            mAkRadio.mMRDFlag ^= mAkRadio.MRD_INFO_FLAG_LOCDX;
            mAkRadio.sendMRDCommand(AkRadio.MRD_WRITE_LOCDX);
            // case R.id.radio_function_button_ps: {
            // processButtonStatus(view.getId());
            // mAkRadio.sendMRDCommand(AkRadio.MRD_PS);
            // break;
            // }
            // case R.id.radio_function_button_as: {
            // processButtonStatus(view.getId());
            // mAkRadio.sendMRDCommand(AkRadio.MRD_AS);
            // break;
            // }
        } else if (id == R.id.radio_function_button_seek_previous) {// mAkRadio.sendMRDCommand(AkRadio.MRD_QUERY_RADIO_INFO);
            mAkRadio.sendMRDCommand(AkRadio.MRD_SEEK_UP);
            GlobalDef.reactiveSource(mContext, SOURCE,
                    RadioService.mAudioFocusListener);
        } else if (id == R.id.radio_function_button_band) {
            doKeyBaud();
            GlobalDef.reactiveSource(mContext, SOURCE,
                    RadioService.mAudioFocusListener);
        } else if (id == R.id.radio_function_button_amfm) {
            mAkRadio.sendMRDCommand(AkRadio.MRD_WRITE_BAND2);
        } else if (id == R.id.fm_sel) {
            mAkRadio.mMRDBand = 0;
            mAkRadio.sendMRDCommand(AkRadio.MRD_WRITE_BAND);
        } else if (id == R.id.am_sel) {
            mAkRadio.mMRDBand = 3;
            mAkRadio.sendMRDCommand(AkRadio.MRD_WRITE_BAND);
        } else if (id == R.id.fm_sel2) {
            if (mAkRadio.mMRDBand < AkRadio.MRD_AM) {
                mAkRadio.mMRDBand = (byte) ((mAkRadio.mMRDBand + 1) % 3);

            } else {
                mAkRadio.mMRDBand = AkRadio.MRD_FM1;
            }
            mAkRadio.sendMRDCommand(AkRadio.MRD_WRITE_BAND);
        } else if (id == R.id.am_sel2) {
            if (mAkRadio.mMRDBand < AkRadio.MRD_AM
                    || mAkRadio.mMRDBand == AkRadio.MRD_AM2) {
                mAkRadio.mMRDBand = AkRadio.MRD_AM;
            } else {
                mAkRadio.mMRDBand = AkRadio.MRD_AM2;
            }

            mAkRadio.sendMRDCommand(AkRadio.MRD_WRITE_BAND);
        } else if (id == R.id.radio_list_next) {
            doListPage(true);
        } else if (id == R.id.radio_list_prev) {
            doListPage(false);
        } else if (id == R.id.radio_function_button_seek_previous_and_step) {
            doKeySeekAndStepPrev();
        } else if (id == R.id.radio_function_button_seek_next_and_step) {
            doKeySeekAndStepNext();
        } else if (id == R.id.radio_function_button_seek_next) {// mAkRadio.sendMRDCommand(AkRadio.MRD_QUERY_RDS_INFO);

            mAkRadio.sendMRDCommand(AkRadio.MRD_SEEK_DOWN);
            GlobalDef.reactiveSource(mContext, SOURCE,
                    RadioService.mAudioFocusListener);
        } else if (id == R.id.radio_function_button_scan) {
            String value = GlobalDef.getSystemUI();
            if (!(MachineConfig.VALUE_SYSTEM_UI_KLD11_200.equals(value)
                    || (GlobalDef.mRadioButtonType == GlobalDef.RADIO_BUTTON_TYPE_LONG_PRESS_SCAN))
                    || ((mAkRadio.mMRDFlag & 0x80) != 0)) {
                mAkRadio.sendMRDCommand(AkRadio.MRD_AS);
                GlobalDef.reactiveSource(mContext, SOURCE,
                        RadioService.mAudioFocusListener);
            }
        } else if (id == R.id.radio_function_button_ps_search) {
            mAkRadio.sendMRDCommand(AkRadio.MRD_PS);
        } else if (id == R.id.eq) {
            try {
                Intent it = new Intent(mContext.getString(R.string.app_eq));
                mContext.startActivity(it);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } else if (id == R.id.radio_step_up_button) {
            mAkRadio.sendMRDCommand(AkRadio.MRD_STEP_UP);
            GlobalDef.reactiveSource(mContext, SOURCE,
                    RadioService.mAudioFocusListener);
        } else if (id == R.id.radio_step_down_button) {
            mAkRadio.sendMRDCommand(AkRadio.MRD_STEP_DOWN);
            GlobalDef.reactiveSource(mContext, SOURCE,
                    RadioService.mAudioFocusListener);
        } else if (id == R.id.btn_band_prev) {
            mAkRadio.doKeyBaudEx(true);
        } else if (id == R.id.btn_band_next) {
            mAkRadio.doKeyBaudEx(false);
        } else if (id == R.id.btn_band_prev2) {
            mAkRadio.doKeyAMFMSwitch(true);
        } else if (id == R.id.btn_band_next2) {
            mAkRadio.doKeyAMFMSwitch(false);
        } else if (id == R.id.radio_function_button_pty) {// Button button = (Button) view;
            if (mIsPtyShow) {
                showPtyList(false);
                // button.setText(mContext
                // .getString(R.string.radio_function_text_pty));
            } else {
                showPtyList(true);
                // button.setText(mContext
                // .getString(R.string.radio_function_text_frq));
            }
            // mViewFlipper.showNext();

            View v = mMainView.findViewById(R.id.layout_set);
            if (v != null) {
                v.setVisibility(View.GONE);
            }
        } else if (id == R.id.radio_function_button_reg) {
            ToggleButton rge = (ToggleButton) view;

            if (rge.isChecked()) {
                mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_REG_ON);
            } else {
                mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_REG_OFF);
            }
        } else if (id == R.id.radio_function_button_ta) {// ToggleButton ta = (ToggleButton) view;
            // if (ta.isChecked()) {
            // mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_TA_ON);
            // } else {
            // mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_TA_OFF);
            // }
            if (((mAkRadio.mMRDRdsFlag & 0x20) != 0)) {
                mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_TA_OFF);
            } else {
                mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_TA_ON);
            }
        } else if (id == R.id.radio_function_button_af) {
            if ((mAkRadio.mMRDRdsFlag & 0x40) != 0) {
                mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_AF_OFF);
            } else {
                mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_AF_ON);
            }
            // ToggleButton af = (ToggleButton) view;
            // if (af.isChecked()) {
            // mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_AF_ON);
            // } else {
            // mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_AF_OFF);
            // }
        } else if (id == R.id.radio_function_button_st) {
            if ((mAkRadio.mMRDFlag & 0x2) != 0) {
                mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_ST_OFF);
            } else {
                mAkRadio.sendMRDCommand(AkRadio.MRD_RDS_ST_ON);
            }
        } else if (id == R.id.radio_button_loc3 || id == R.id.radio_button_loc2 || id == R.id.radio_button_loc) {
            if ((mAkRadio.mMRDFlag & AkRadio.MRD_INFO_FLAG_LOCDX) == 0) {
                mAkRadio.mMRDFlag |= AkRadio.MRD_INFO_FLAG_LOCDX;
            } else {
                mAkRadio.mMRDFlag &= ~AkRadio.MRD_INFO_FLAG_LOCDX;
            }

            if (view instanceof ToggleButton) {
                ToggleButton loc = (ToggleButton) view;

                if ((mAkRadio.mMRDFlag & AkRadio.MRD_INFO_FLAG_LOCDX) != 0) {
                    loc.setChecked(true);
                } else {
                    loc.setChecked(false);
                }
            }

            View v = mMainView.findViewById(R.id.radio_loc);
            if (v != null) {
                if ((mAkRadio.mMRDFlag & AkRadio.MRD_INFO_FLAG_LOCDX) != 0) {
                    v.setVisibility(View.VISIBLE);
                } else {
                    v.setVisibility(View.INVISIBLE);
                }
            }

            mAkRadio.sendMRDCommand(AkRadio.MRD_WRITE_LOCDX);
        } else if (id == R.id.radio_keyboard) {
            showKeyboard();
        } else if (id == R.id.bottom_set || id == R.id.layout_set) {
            toggleSetUI();
        } else if (id == R.id.btn_next) {
            mAkRadio.sendMRDCommand(AkRadio.MRD_NEXT_LIST_CHANNEL);
        } else if (id == R.id.btn_prev) {
            mAkRadio.sendMRDCommand(AkRadio.MRD_PREV_LIST_CHANNEL);
        } else if (id == R.id.btn_list) {
            showFavoriteList();
        } else if (id == R.id.favorit_back) {
            if (mGirdViewFavoriteParent != null) {
                mGirdViewFavoriteParent.setVisibility(View.GONE);
            }
        } else if (id == R.id.radio_pty_layout) {
            showPtyList(false);
        } else if (id == R.id.eq_mode_switch) {
            GlobalDef.swtichEQMode();
        } else if (id == R.id.left_gps) {// UtilCarKey.doKeyGps(mContext);
            BroadcastUtil.sendToCarService(mContext,
                    MyCmd.Cmd.APP_REQUEST_SEND_KEY, MyCmd.Keycode.NAVIGATION);
        } else if (id == R.id.back) {
            if (SCREEN0 == mDisplayIndex) {

                ((Activity) mContext).finish();

                BroadcastUtil.sendToCarServiceSetSource(mContext,
                        MyCmd.SOURCE_AV_OFF);

            } else {
                // dismiss();
            }
        } else if (id == R.id.home) {
            mContext.startActivity(new Intent(Intent.ACTION_MAIN).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP).addCategory(
                    Intent.CATEGORY_HOME));
        }
		/*else if (id == R.id.mic) {
            UtilCarKey.doKeyMic(mContext);
        } else if (id == R.id.vol) {
            Intent it = new Intent(MyCmd.BROADCAST_START_VOLUMESETTINGS_COMMON);
            it.setPackage("com.my.out");
            mContext.sendBroadcast(it);
        }*/
	}

	private void showPtyList(boolean show) {

		View v = mMainView.findViewById(R.id.radio_pty_layout);
		if (v == null) {
			if (!show) {
				mMainView.findViewById(R.id.radio_frequency_list)
						.setVisibility(View.VISIBLE);
				mMainView.findViewById(R.id.radio_pty_list).setVisibility(
						View.GONE);
				// if (mMainView.findViewById(R.id.HorizontalScrollView_pty) !=
				// null) {
				// mMainView.findViewById(R.id.HorizontalScrollView_pty)
				// .setVisibility(View.GONE);
				// }
			} else {
				mMainView.findViewById(R.id.radio_frequency_list)
						.setVisibility(View.GONE);
				mMainView.findViewById(R.id.radio_pty_list).setVisibility(
						View.VISIBLE);
				// if (mMainView.findViewById(R.id.HorizontalScrollView_pty) !=
				// null) {
				// mMainView.findViewById(R.id.HorizontalScrollView_pty)
				// .setVisibility(View.VISIBLE);
				// }
			}
			Button button = (Button) mMainView
					.findViewById(R.id.radio_function_button_pty);
			if (show) {
				button.setText(mContext
						.getString(R.string.radio_function_text_pty));
			} else {
				button.setText(mContext
						.getString(R.string.radio_function_text_frq));
			}
		} else {
			v.setVisibility(show ? View.VISIBLE : View.GONE);
			// Button button = (Button)
			// mMainView.findViewById(R.id.radio_function_button_pty);
			// if (show) {
			// button.setText(mContext
			// .getString(R.string.radio_function_text_pty));
			// } else {
			// button.setText(mContext
			// .getString(R.string.radio_function_text_frq));
			// }
		}
		mIsPtyShow = show;
	}

	private void showFavoriteList() {
		if (mGirdViewFavoriteList == null) {
			mGirdViewFavoriteList = (GridView) mMainView
					.findViewById(R.id.grid_favorite_list);
			mGirdViewFavoriteParent = mMainView
					.findViewById(R.id.favorite_list);

			if (mGirdViewFavoriteList != null) {
				mGirdViewFavoriteList.setVisibility(View.VISIBLE);

				mFavoriteListAdapter = new MyListViewAdapter(mContext,
						R.layout.tl_list_radio_favorite);

				mFavoriteListAdapter.clearRowLine();
				if (mAkRadio.mMRDBand < AkRadio.MRD_AM) {
					mFavoriteListAdapter.setCount(AkRadio.FM_FREQ_LIST);
				} else {
					mFavoriteListAdapter.setCount(AkRadio.AM_FREQ_LIST);
				}

				mFavoriteListAdapter.setAkRadio(mAkRadio);
				mGirdViewFavoriteList.setAdapter(mFavoriteListAdapter);
				mGirdViewFavoriteList
						.setOnItemClickListener(new OnItemClickListener() {
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {
								if (mAkRadio != null) {

									GlobalDef.reactiveSource(mContext, SOURCE,
											RadioService.mAudioFocusListener);

									mAkRadio.sendMRDCommand(
											AkRadio.MRD_LIST_INDEX, position);

									mLongPressSeekButton = false;
									if (mGirdViewFavoriteParent != null) {
										mGirdViewFavoriteParent
												.setVisibility(View.GONE);
									}
								}

							}

						});
				mGirdViewFavoriteList
				.setOnItemLongClickListener(new OnItemLongClickListener() {
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {

						position = mFavoriteListAdapter.toCustomIndex(position);
						mAkRadio.sendMRDCommand(AkRadio.MRD_SAVE, position);
//						mAkRadio.addFreqPS(mAkRadio.mMRDFreqency,
//								mAkRadio.mStrRdsPs);
						return true;
					}

				});

			}
		}

		if (mFavoriteListAdapter != null) {
			if (mAkRadio.mMRDFreqs[mAkRadio.mCurPlayIndex] == mAkRadio.mMRDFreqency) {
				mFavoriteListAdapter.setSelectItem(mAkRadio.mCurPlayIndex);
			} else {
				mFavoriteListAdapter.setSelectItem(-1);
			}			
			mFavoriteListAdapter.notifyDataSetChanged();
		}

		if (mGirdViewFavoriteParent != null) {
			mGirdViewFavoriteParent.setVisibility(View.VISIBLE);
		}

	}

	RadioKeyboardDialog mRadioKeyboardDialog;

	private void toggleSetUI() {
		View v = mMainView.findViewById(R.id.layout_set);
		if (v != null) {
			if (v.getVisibility() == View.VISIBLE) {
				v.setVisibility(View.GONE);
			} else {
				v.setVisibility(View.VISIBLE);
			}
		}
	}

	private void showKeyboard() {
		if (mRadioKeyboardDialog == null) {
			mRadioKeyboardDialog = new RadioKeyboardDialog(mContext,
					mOnClickRadioKeyboardDialogOk);
		}

		mRadioKeyboardDialog.show();
	}

	private int stringToBaud(String s) {
		int num = 0;
		if (s != null) {
			if (mAkRadio.mMRDBand < AkRadio.MRD_AM) {
				int i = s.indexOf(".");
				try {
					if (i > 0 && i < s.length()) {
						int h;
						h = Integer.valueOf(s.substring(0, i));
						num = Integer.valueOf(s.substring(i + 1, s.length()));
						if (num < 10 && ((s.length() - i) == 2)) {
							num = num * 10;
						}
						num = h * 100 + num;
					} else {
						i = Integer.valueOf(s);
						num = i * 100;
					}

				} catch (Exception e) {
					num = 0;
				}
			} else {
				try {
					num = Integer.valueOf(s);
				} catch (Exception e) {
					num = 0;
				}
			}
		}
		return num;
	}

	private View.OnClickListener mOnClickRadioKeyboardDialogOk = new View.OnClickListener() {
		public void onClick(View v) {
			String s = mRadioKeyboardDialog.getText();
			short baud = (short) stringToBaud(s);
			if (baud >= mAkRadio.mFreqMin && baud <= mAkRadio.mFreqMax) {
				setFrequencyNumber(baud, true);
			} else {
				Toast.makeText(mContext, "Incorrect freq!", Toast.LENGTH_LONG)
						.show();
			}
			mRadioKeyboardDialog.hide();
		}
	};

	public void setFrequencyNumber(short frequency_number, boolean is_send_cmd) {
		if (is_send_cmd) {
			mAkRadio.mMRDFreqency = frequency_number;
			mAkRadio.sendMRDCommand(AkRadio.MRD_WRITE_FREQENCY);
		} else {
			if (mAkRadio.mMRDBand < AkRadio.MRD_AM) {
				SetFmFreqTextImage(frequency_number);
			} else {
				SetAmFreqTextImage(frequency_number);
			}
		}
	}

	public void processButtonStatus(int id) {
		if (mCurrentButtonId != -1) {
			ToggleButton currentButton = (ToggleButton) mMainView
					.findViewById(mCurrentButtonId);
			if (currentButton != null && currentButton.isChecked())
				currentButton.setChecked(false);
			mCurrentButtonId = -1;
			if (id != -1) {
				ToggleButton my = (ToggleButton) mMainView.findViewById(id);
				if (my != null && my.isChecked())
					my.setChecked(false);
			}
		} else {
			mCurrentButtonId = id;
		}
	}

	private int mKeySelect = 0;

	private void doKeyNum(int index) {
		mKeySelect = index;

	}

	// /////////////for out..
	private final static String RADIO_SAVE = "com.my.radio.SAVE";
	private final static String KEY_FREQ = "freq";
	private final static String KEY_BAUD = "baud";

	private void saveData(String s, int v) {
		saveDataEx(RADIO_SAVE, s, v);
	}

	private void saveDataEx(String data, String s, int v) {
		SharedPreferences.Editor sharedata = mContext.getSharedPreferences(
				data, 0).edit();
		sharedata.putInt(s, v);
		sharedata.commit();
	}

	private int getDataEx(String data, String s) {
		SharedPreferences sharedata = mContext.getSharedPreferences(data, 0);
		return sharedata.getInt(s, 0);
	}

	private final static boolean USE_TEXTVIEW_FREQ = true;

	private void SetFmFreqTextImage(short freq) {
		if (USE_TEXTVIEW_FREQ) {
			TextView tv = (TextView) mMainView.findViewById(R.id.freq_text);
			if (tv != null) {
				tv.setVisibility(View.VISIBLE);
				TextView tv_ps = (TextView) mMainView
						.findViewById(R.id.freq_text_ps);

				if (mAkRadio.mStrRdsPs != null
						&& mAkRadio.mStrRdsPs.length() > 0) {
					if (tv_ps != null) {
						tv_ps.setText(mAkRadio.mStrRdsPs);
						tv_ps.setVisibility(View.VISIBLE);
						tv.setVisibility(View.GONE);
					} else {
						View tv_ps2 = mMainView
								.findViewById(R.id.freq_text_ps2);
						if (tv_ps2 == null) {
							tv.setText(mAkRadio.mStrRdsPs);
						}
					}
				} else {
					// tv.setText(String.format("%d.%02d", freq / 100, freq %
					// 100));
					if (tv_ps != null) {
						tv_ps.setVisibility(View.GONE);
					}
				}

				String s = (freq / 100) + ".";
				if ((freq % 100) < 10) {
					s += "0";
				}
				s += (freq % 100);
				tv.setText(s);
			}
		} else {
			if (freq == 0) {

			}

			if (freq >= 10000) {
				mFreqDigital1.setVisibility(View.VISIBLE);
				mFreqDigital1.getBackground().setLevel((((freq / 10000)) % 10));
			} else {
				mFreqDigital1.setVisibility(View.GONE);
			}

			mFreqDigitalp.setVisibility(View.VISIBLE);
			mFreqDigitalp2.setVisibility(View.VISIBLE);
			int a = Math.round(freq * 100);
			a = a % 10;
			mFreqDigitalp2.getBackground()
					.setLevel((int) (((int) (freq)) % 10));
			mFreqDigitalp1.getBackground().setLevel(
					(int) (((int) (freq / 10)) % 10));
			mFreqDigital3.getBackground().setLevel(
					(int) (((int) (freq / 100)) % 10));
			mFreqDigital2.getBackground().setLevel(
					(int) (((int) (freq / 1000)) % 10));
		}
	}

	private void SetAmFreqTextImage(int freq) {
		if (USE_TEXTVIEW_FREQ) {
			TextView tv = (TextView) mMainView.findViewById(R.id.freq_text);
			if (tv != null) {
				tv.setText(freq + "");
				tv.setVisibility(View.VISIBLE);
			}
			TextView tv_ps = (TextView) mMainView
					.findViewById(R.id.freq_text_ps);
			if (tv_ps != null) {
				tv_ps.setVisibility(View.GONE);
			}
		} else {
			if (freq >= 1000) {
				mFreqDigital1.setVisibility(View.VISIBLE);
				mFreqDigital1.getBackground().setLevel(
						(int) (((int) (freq / 1000)) % 10));
			} else {
				mFreqDigital1.setVisibility(View.GONE);
			}

			mFreqDigitalp.setVisibility(View.GONE);
			mFreqDigitalp2.setVisibility(View.GONE);

			mFreqDigitalp1.getBackground()
					.setLevel((int) (((int) (freq)) % 10));
			mFreqDigital3.getBackground().setLevel(
					(int) (((int) (freq / 10)) % 10));
			mFreqDigital2.getBackground().setLevel(
					(int) (((int) (freq / 100)) % 10));
		}
	}

	private int mAutoToCarService = -1;
	// kld
	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			// if (msg.what == 0xffffff){
			// Configuration c = mContext.getResources().getConfiguration();
			// Log.d("addd", ""+c);
			// mHandler.sendEmptyMessageDelayed(0xffffff, 500);
			// return ;
			// }

			if (msg.what == AkRadio.MRD_INIT) {

				initHardware();
			} else if (msg.what == GlobalDef.MSG_UPDATE_EQ_MODE) {
				GlobalDef.updateEQModeButton(mMainView, R.id.eq_mode_switch);
			} else {
				if (mAkRadio != null) {
					switch (msg.what) {
					case AkRadio.MRD_INFO_FREQENCY:
						if (GlobalDef.mAutoTest && mDisplayIndex == 0) {
							// if (mAutoToCarService == 0) {
							// mAutoToCarService = mAkRadio.mMRDFreqency;
							// mAkRadio.sendMRDCommand(AkRadio.MRD_AS);
							// } else {
							// if (mAutoToCarService != mAkRadio.mMRDFreqency
							// && (mAkRadio.mMRDFlag & 0x80) == 0) {
							// BroadcastUtil.sendToCarService(mContext,
							// MyCmd.Cmd.AUTO_TEST_RESULT,
							// MyCmd.SOURCE_RADIO, 0);
							// mAutoToCarService = -1;
							// }
							// }
						}
						doUpdateFreqency();
						break;
					case AkRadio.MRD_INFO_FREQS:
						mPreChannelSel = -1;
						updateList();
						break;
					case AkRadio.MRD_INIT:
						break;
					case AkRadio.MRD_RDS_INFO:
						updateRdsInfo();
						break;
					case AkRadio.MRD_RDS_PTY:
						updatePtyInfo();
						break;
					case AkRadio.MRD_RDS_PS:
						updatePSInfo();
						break;
					case AkRadio.MRD_RDS_RT:
						updateRTInfo();
						break;
					default:
						// mrdCallback(msg.what);
						break;
					}
				} else {
					// Log.d("ffff", "????");
				}
			}
			super.handleMessage(msg);
		}

	};

	private void updateRTInfo() {
		TextView tv = (TextView) mMainView.findViewById(R.id.radio_rds_rt);
		if (tv != null) {
			try {
				String name = new String(mAkRadio.mMRDRdsRt);
				if (name.trim().length() <= 0) {
					name = "";
				}
				tv.setText(name.trim());
			} catch (Exception e) {

			}
		}
	}

	private void updatePSInfo() {
		// TextView tv = (TextView) mMainView.findViewById(R.id.radio_rds_ps);
		// String name = new String(mAkRadio.mMRDRdsPs);
		// if (name.trim().length() <= 0) {
		// name = "";
		// }
		// tv.setText(name.trim());

		TextView tv = (TextView) mMainView.findViewById(R.id.freq_text);
		if (tv != null) {
			TextView tv_ps2 = (TextView) mMainView
					.findViewById(R.id.freq_text_ps2);
			if (mAkRadio.mStrRdsPs != null && mAkRadio.mStrRdsPs.length() > 0) {

				TextView tv_ps = (TextView) mMainView
						.findViewById(R.id.freq_text_ps);

				if (tv_ps != null) {
					tv_ps.setText(mAkRadio.mStrRdsPs);
					tv_ps.setVisibility(View.VISIBLE);
					tv.setVisibility(View.GONE);
				} else {

					if (tv_ps2 == null) {
						tv.setText(mAkRadio.mStrRdsPs);
					} else {
						tv_ps2.setText(mAkRadio.mStrRdsPs);
					}
				}

			} else {
				if (tv_ps2 != null) {
					tv_ps2.setText("");
				}
			}

			// else {
			// tv.setText(freq+"");
			// }
		}
	}

	private void updateRdsInfo() {

		View v;

		if (MachineConfig.VALUE_SYSTEM_UI35_KLD813_2.equals(GlobalDef
				.getSystemUI())) {
			int colorNormal = mContext.getResources().getColor(
					R.color.radio_tag_normal);
			int colorSelect = mContext.getResources().getColor(
					R.color.radio_tag_select);
			v = mMainView.findViewById(R.id.radio_tag_color_af);

			if ((mAkRadio.mMRDRdsFlag & 0x40) != 0) {
				((TextView) v).setTextColor(colorSelect);
			} else {
				((TextView) v).setTextColor(colorNormal);
			}
			v = mMainView.findViewById(R.id.radio_tag_color_ta);

			if ((mAkRadio.mMRDRdsFlag & 0x20) != 0) {
				((TextView) v).setTextColor(colorSelect);
			} else {
				((TextView) v).setTextColor(colorNormal);
			}

		} else {
			v = mMainView.findViewById(R.id.radio_function_button_af);

			if (v instanceof ToggleButton) {
				ToggleButton af = (ToggleButton) v;
				if ((mAkRadio.mMRDRdsFlag & 0x40) != 0) {
					af.setChecked(true);
				} else {
					af.setChecked(false);
				}

				af = (ToggleButton) mMainView
						.findViewById(R.id.radio_function_button_ta);
				if (af != null) {
					if ((mAkRadio.mMRDRdsFlag & 0x20) != 0) {
						af.setChecked(true);
					} else {
						af.setChecked(false);
					}
				}
			} else if (v instanceof ImageView) {

				if ((mAkRadio.mMRDRdsFlag & 0x40) != 0) {
					((ImageView) v).setImageResource(R.drawable.btn_on);
				} else {
					((ImageView) v).setImageResource(R.drawable.btn_off);
				}

				v = mMainView.findViewById(R.id.radio_function_button_ta);
				if (v != null) {
					if ((mAkRadio.mMRDRdsFlag & 0x20) != 0) {
						((ImageView) v).setImageResource(R.drawable.btn_on);
					} else {
						((ImageView) v).setImageResource(R.drawable.btn_off);
					}
				}
			}

			v = mMainView.findViewById(R.id.radio_tp);
			if (v != null) {
				if ((mAkRadio.mMRDRdsFlag & 0x80) != 0) {
					v.setVisibility(View.VISIBLE);
				} else {
					v.setVisibility(View.INVISIBLE);
				}
			}
		}
	}

	private void updatePtyInfo() {
		String name = "";
		if (mAkRadio.mMRDRdsPty[1] > 0
				&& mAkRadio.mMRDRdsPty[1] < PTYListViewAdapter.mPty_data.length) {
			name = PTYListViewAdapter.mPty_data[mAkRadio.mMRDRdsPty[1]];
		}
		mAdapter.updatePtyData(mContext);
		mAdapter.setSelectItem(mAkRadio.mMRDRdsPty[0]);
		if (mPtyName != null) {
			mPtyName.setText(name.trim());
		}

		View v = mMainView.findViewById(R.id.radio_function_button_pty);
		if (v != null) {
			if (v instanceof ToggleButton) {
				ToggleButton new_name = (ToggleButton) v;
				if (mAkRadio.mMRDRdsPty[0] == 0) {
					new_name.setChecked(false);
				} else {
					new_name.setChecked(true);
				}
			} else if (v instanceof ImageView) {
				if (!MachineConfig.VALUE_SYSTEM_UI35_KLD813_2.equals(GlobalDef
						.getSystemUI())) {
					if (mAkRadio.mMRDRdsPty[0] != 0) {
						((ImageView) v).setImageResource(R.drawable.btn_on);
					} else {
						((ImageView) v).setImageResource(R.drawable.btn_off);
					}
				}
			}
		}

		if (MachineConfig.VALUE_SYSTEM_UI35_KLD813_2.equals(GlobalDef
				.getSystemUI())) {
			int colorNormal = mContext.getResources().getColor(
					R.color.radio_tag_normal);
			int colorSelect = mContext.getResources().getColor(
					R.color.radio_tag_select);
			v = mMainView.findViewById(R.id.radio_tag_color_pty);

			if (mAkRadio.mMRDRdsPty[0] != 0) {
				((TextView) v).setTextColor(colorSelect);
			} else {
				((TextView) v).setTextColor(colorNormal);
			}

		}
	}

	/**
	 * 
	 */
	private void doUpdateFreqency() {
		try {
			changeFreqListNum();
			View v;

			if (mAkRadio.mMRDBand < AkRadio.MRD_AM) {
				// tmp_current_frequency = (float) ((float)
				// mAkRadio.mMRDFreqency /
				// 100.0);

				setViewEnable(mMainView, R.id.radio_button_loc, true);
				setViewVisible(mMainView, R.id.radio_button_loc3, View.VISIBLE);
				setViewEnable(mMainView, R.id.radio_function_button_st, true);

				// allen
				// hide am no used button
				setViewVisible(mMainView, R.id.radio_button_loc, View.VISIBLE);
				setViewVisible(mMainView, R.id.radio_function_button_st,
						View.VISIBLE);

				SetFmFreqTextImage(mAkRadio.mMRDFreqency);
				if (mMarkFace != null) {
					mMarkFace.setmIsAM(false);
					mMarkFace.setFmFrequencyRange(mAkRadio.mFreqMin,
							mAkRadio.mFreqMax);
				}

				mTVBand.setText(mAkRadio.getCurBaundText());
				mTVUnit.setText("MHz");

				MarkFaceView.mFrequencyNum = (float) ((float) mAkRadio.mMRDFreqency / 100.0);

				if (mRds) {
					v = mMainView.findViewById(R.id.rds_buttons);
					if (v != null) {
						v.setVisibility(View.VISIBLE);
					}

					setViewVisible(mMainView, R.id.rds_tag, View.VISIBLE);
				}

				v = mMainView.findViewById(R.id.radio_loc);
				if (v != null) {
					if ((mAkRadio.mMRDFlag & AkRadio.MRD_INFO_FLAG_LOCDX) != 0) {
						v.setVisibility(View.VISIBLE);
					} else {
						v.setVisibility(View.INVISIBLE);
					}
				}
			} else {

				setViewVisible(mMainView, R.id.radio_button_loc3, View.GONE);
				setViewVisible(mMainView, R.id.radio_loc, View.INVISIBLE);
				setViewEnable(mMainView, R.id.radio_button_loc, false);
				setViewEnable(mMainView, R.id.radio_function_button_st, false);
				// allen
				// hide am no used button
				if (MachineConfig.VALUE_SYSTEM_UI45_8702_2.equals(GlobalDef
						.getSystemUI())) {

					setViewVisible(mMainView, R.id.radio_button_loc, View.GONE);
					setViewVisible(mMainView, R.id.radio_function_button_st,
							View.GONE);
				} else {

					setViewVisible(mMainView, R.id.radio_button_loc,
							View.INVISIBLE);
					setViewVisible(mMainView, R.id.radio_function_button_st,
							View.INVISIBLE);
				}

				SetAmFreqTextImage(mAkRadio.mMRDFreqency);
				if (mMarkFace != null) {
					mMarkFace.setmIsAM(true);
					mMarkFace.setAmFrequencyRange(mAkRadio.mFreqMin,
							mAkRadio.mFreqMax);
				}
				mTVBand.setText(mAkRadio.getCurBaundText());
				mTVUnit.setText("KHz");

				MarkFaceView.mFrequencyNum = (float) ((float) mAkRadio.mMRDFreqency);
				if (mRds) {
					v = mMainView.findViewById(R.id.rds_buttons);
					if (v != null) {
						v.setVisibility(View.INVISIBLE);
					}
					setViewVisible(mMainView, R.id.rds_tag, View.INVISIBLE);
				}

			}
			if (mMarkFace != null) {
				mMarkFace.postInvalidate();
			}

			v = mMainView.findViewById(R.id.radio_st);
			if (v != null) {
				if ((mAkRadio.mMRDFlag & 0x10) != 0) {
					v.setVisibility(View.VISIBLE);
				} else {
					v.setVisibility(View.INVISIBLE);
				}
			}

			v = mMainView.findViewById(R.id.radio_button_loc);
			if (v instanceof ToggleButton) {
				ToggleButton loc = (ToggleButton) mMainView
						.findViewById(R.id.radio_button_loc);
				if (loc != null) {
					if ((mAkRadio.mMRDFlag & AkRadio.MRD_INFO_FLAG_LOCDX) != 0) {
						loc.setChecked(true);
					} else {
						loc.setChecked(false);
					}
				}
			} else if (v instanceof ImageView) {
				String s = (String)v.getTag();
				if ("s".equals(s)){
					if ((mAkRadio.mMRDFlag & AkRadio.MRD_INFO_FLAG_LOCDX) != 0) {
						v.setSelected(true);
					} else {
						v.setSelected(false);
					}
				}
			}

			if (MachineConfig.VALUE_SYSTEM_UI35_KLD813_2.equals(GlobalDef
					.getSystemUI())) {
				int colorNormal = mContext.getResources().getColor(
						R.color.radio_tag_normal);
				int colorSelect = mContext.getResources().getColor(
						R.color.radio_tag_select);
				v = mMainView.findViewById(R.id.radio_tag_color_st);

				if ((mAkRadio.mMRDFlag & 0x2) == 0
						&& (mAkRadio.mMRDBand < AkRadio.MRD_AM)) {
					((TextView) v).setTextColor(colorSelect);
				} else {
					((TextView) v).setTextColor(colorNormal);
				}
				v = mMainView.findViewById(R.id.radio_tag_color_loc);

				if ((mAkRadio.mMRDFlag & AkRadio.MRD_INFO_FLAG_LOCDX) != 0
						&& (mAkRadio.mMRDBand < AkRadio.MRD_AM)) {

					((TextView) v).setTextColor(colorSelect);
				} else {
					((TextView) v).setTextColor(colorNormal);
				}

				v = mMainView.findViewById(R.id.radio_tag_color_ams);

				if ((mAkRadio.mMRDFlag & AkRadio.MRD_INFO_FLAG_AS) != 0) {
					((TextView) v).setTextColor(colorSelect);
				} else {
					((TextView) v).setTextColor(colorNormal);
				}
				v = mMainView.findViewById(R.id.radio_tag_color_aps);

				if ((mAkRadio.mMRDFlag & AkRadio.MRD_INFO_FLAG_PS) != 0) {
					((TextView) v).setTextColor(colorSelect);
				} else {
					((TextView) v).setTextColor(colorNormal);
				}

			}

			v = mMainView.findViewById(R.id.radio_button_loc2);
			if (v instanceof ImageView) {

				if (mAkRadio.mMRDBand < AkRadio.MRD_AM) {
					v.setEnabled(true);
					if ((mAkRadio.mMRDFlag & AkRadio.MRD_INFO_FLAG_LOCDX) != 0) {
						((ImageView) v).setImageResource(R.drawable.btn_on);
					} else {
						((ImageView) v).setImageResource(R.drawable.btn_off);
					}
				} else {
					v.setEnabled(false);
					((ImageView) v).setImageResource(R.drawable.btn_off);
				}
			}

			updateList();
			updateAllChannelBandText();

			v = mMainView.findViewById(R.id.radio_function_button_st);
			if (v instanceof ToggleButton) {
				ToggleButton loc = (ToggleButton) v;
				if (loc != null) {
					if ((mAkRadio.mMRDFlag & 0x2) == 0) {
						loc.setChecked(true);
					} else {
						loc.setChecked(false);
					}
				}
				if (mAkRadio.mMRDBand < AkRadio.MRD_AM) {
					v.setVisibility(View.VISIBLE);
				} else {
					v.setVisibility(View.INVISIBLE);
				}

			} else if (v instanceof ImageView) {

				if (MachineConfig.VALUE_SYSTEM_UI21_RM12.equals(GlobalDef
						.getSystemUI())
						|| MachineConfig.VALUE_SYSTEM_UI45_8702_2
								.equals(GlobalDef.getSystemUI())) {
					if (mAkRadio.mMRDBand < AkRadio.MRD_AM) {
						if ((mAkRadio.mMRDFlag & 0x2) == 0) {
							setViewVisible(mMainView, R.id.radio_st_tag,
									View.VISIBLE);
						} else {
							setViewVisible(mMainView, R.id.radio_st_tag,
									View.GONE);
						}
					} else {
						setViewVisible(mMainView, R.id.radio_st_tag, View.GONE);
					}

				} else if (!MachineConfig.VALUE_SYSTEM_UI35_KLD813_2
						.equals(GlobalDef.getSystemUI())) {
					if (mAkRadio.mMRDBand < AkRadio.MRD_AM) {
						v.setEnabled(true);
						if ((mAkRadio.mMRDFlag & 0x2) == 0) {
							((ImageView) v).setImageResource(R.drawable.btn_on);
						} else {
							((ImageView) v)
									.setImageResource(R.drawable.btn_off);
						}
					} else {
						v.setEnabled(false);
						((ImageView) v).setImageResource(R.drawable.btn_off);
					}
				}
			}

			updateFlipFreq();
		} catch (Exception e) {

			Log.d(TAG, "doUpdateFreqency fail:" + e);
		}
	}

	private boolean isFreqsFixBaud() {// freq list not fix the baud.
		boolean ret = true;
		if (mAkRadio.mMRDFreqs[0] < mAkRadio.mFreqMin
				|| mAkRadio.mMRDFreqs[0] > mAkRadio.mFreqMax) {
			ret = false;
		}
		// Log.d("addd", "isFreqsFixBaud:"+ret);
		return ret;
	}

	private void updateList() {
		// Log.d("addd", "updateList:"+mAkRadio.mCurPlayIndex);
		if (!isFreqsFixBaud()) {
			return;
		}

		if (mAkRadio.mCurPlayIndex >= 0
				&& mAkRadio.mCurPlayIndex < AkRadio.MIN_FREQ_LIST) {

			if (mAkRadio.mMRDFreqs[mAkRadio.mCurPlayIndex] == mAkRadio.mMRDFreqency) {
				mFreqsAdapter.setSelectItem(mAkRadio.mCurPlayIndex);
				mFreqsAdapter.notifyDataSetChanged();

				if (mListViewFreqs != null) {
					if (((mListViewFreqs.getLastVisiblePosition()) < mAkRadio.mCurPlayIndex)
							|| (mListViewFreqs.getFirstVisiblePosition() >= mAkRadio.mCurPlayIndex)) {
						mListViewFreqs.setSelection(mAkRadio.mCurPlayIndex);
						mListViewFreqs.invalidate();
					}
				} else if (mGirdViewFreqs != null) {
					// Log.d("abc",
					// mGirdViewFreqs.getFirstVisiblePosition()+"");
					// if (((mGirdViewFreqs.getFirstVisiblePosition() + 5) <
					//
					// mAkRadio.mCurPlayIndex)
					// || (mGirdViewFreqs.getFirstVisiblePosition() >=
					//
					// mAkRadio.mCurPlayIndex)) {
					// mGirdViewFreqs
					// .smoothScrollToPosition(mAkRadio.mCurPlayIndex);
					// mGirdViewFreqs.invalidate();
					// mGirdViewFreqs.smoothScrollByOffset(mAkRadio.mCurPlayIndex);
					// }
					if (mHorizontalScrollView != null) {
						if (mFreqsAdapter.mLineNum == 0) {
							int columnWidth = mGirdViewFreqs.getColumnWidth();
							if (columnWidth <= 0) {
								columnWidth = mGirdColumnWidth;
							}
							int len = columnWidth * mListVisibleNum;
							int total = AkRadio.MIN_FREQ_LIST * columnWidth;
							int end = ((mHorizontalScrollView.getScrollX() + len) % total)
									/ columnWidth;
							int start = end - mListVisibleNum;

							if (mAkRadio.mCurPlayIndex >= end) {

								int page = (mAkRadio.mCurPlayIndex / mListVisibleNum);
								int x = len * page;
								mHorizontalScrollView.smoothScrollTo(x, 0);
							} else if (mAkRadio.mCurPlayIndex <= start) {

								int page = (mAkRadio.mCurPlayIndex / mListVisibleNum);
								int x = len * page;
								mHorizontalScrollView.smoothScrollTo(x, 0);
							}
						} else {
							if (mHorizontalScrollView instanceof MyScrollView) {
								int page = mFreqsAdapter
										.indexToPage(mAkRadio.mCurPlayIndex);
								MyScrollView new_name = (MyScrollView) mHorizontalScrollView;
								new_name.scrollToPage(page);
							}
						}
						// Log.d("abc", mAkRadio.mCurPlayIndex+":"+end);
						// mHorizontalScrollView.smoothScrollTo(x, 0);
					}
				} else {
					updateButtonChannel(mAkRadio.mCurPlayIndex);
				}
			} else {

				updateButtonChannel(-1);
				mFreqsAdapter.setSelectItem(-1);
				mFreqsAdapter.notifyDataSetChanged();
			}
		}

	}

	private HorizontalScrollView mHorizontalScrollView;
	private int mListVisibleNum = 6;
	private int mGirdColumnWidth = 138;

	private void getListVisibleNum() {
		DisplayMetrics ds = mContext.getResources().getDisplayMetrics();

		int widthSize = ds.widthPixels;// MeasureSpec.getSize(widthMeasureSpec);

		// String value = MachineConfig
		// .getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);
		String value = GlobalDef.getSystemUI();

		if (MachineConfig.VALUE_SYSTEM_UI_KLD3_8702.equals(value)
				|| MachineConfig.VALUE_SYSTEM_UI45_8702_2.equals(value)
				|| MachineConfig.VALUE_SYSTEM_UI_KLD15_6413.equals(value)
				|| MachineConfig.VALUE_SYSTEM_UI32_KLD8.equals(value)
				|| MachineConfig.VALUE_SYSTEM_UI34_KLD9.equals(value)
				|| MachineConfig.VALUE_SYSTEM_UI37_KLD10.equals(value)) {
			if (ds.widthPixels == 800) {
				mListVisibleNum = 5;
				mGirdColumnWidth = 160;
			} else if (ds.widthPixels == 1280 /* && ds.heightPixels == 720 */) {
				mListVisibleNum = 6;
				mGirdColumnWidth = 180;
			} else if (ds.widthPixels == 800) {
				mListVisibleNum = 6;
				mGirdColumnWidth = 170;
			} else {
				mListVisibleNum = 6;
				mGirdColumnWidth = 157;
			}
		} else if (MachineConfig.VALUE_SYSTEM_UI44_KLD007.equals(value)) {
			if (ds.widthPixels == 800) {
				mListVisibleNum = 5;
				mGirdColumnWidth = 160;
			} else if (ds.widthPixels == 1280 && ds.heightPixels == 720) {
				mListVisibleNum = 6;
				mGirdColumnWidth = 180;
			} else {
				mListVisibleNum = 6;
				mGirdColumnWidth = 140;
			}
		} else if (MachineConfig.VALUE_SYSTEM_UI21_RM12.equals(value)) {
			if (ds.widthPixels == 800) {
				mListVisibleNum = 5;
				mGirdColumnWidth = 160;
			} else if (ds.widthPixels == 1280 /* && ds.heightPixels == 720 */) {
				mListVisibleNum = 6;
				mGirdColumnWidth = 213;
			} else {
				mListVisibleNum = 6;
				mGirdColumnWidth = 170;
			}
		} else if (MachineConfig.VALUE_SYSTEM_UI_KLD10_887.equals(value)) {
			if (ds.widthPixels == 800) {
				mListVisibleNum = 5;
				mGirdColumnWidth = 138;
			} else if (ds.widthPixels == 1280 && ds.heightPixels == 480) {
				mListVisibleNum = 8;
				mGirdColumnWidth = 138;
			} else if (ds.widthPixels == 1280 && ds.heightPixels == 720) {
				mListVisibleNum = 6;
				mGirdColumnWidth = 176;
			} else {
				mListVisibleNum = 6;
				mGirdColumnWidth = 138;
			}
		}  else if (MachineConfig.VALUE_SYSTEM_UI43_3300_1.equals(value)) {
			if (ds.widthPixels == 800) {
				mListVisibleNum = 5;
				mGirdColumnWidth = 266;
			} else if (ds.widthPixels == 1280 && ds.heightPixels == 480) {
				mListVisibleNum = 8;
				mGirdColumnWidth = 426;
			} else if (ds.widthPixels == 1280 && ds.heightPixels == 720) {
				mListVisibleNum = 6;
				mGirdColumnWidth = 426;
			}  else if (ds.widthPixels == 1920 && ds.heightPixels == 1080) {
				mListVisibleNum = 6;
				mGirdColumnWidth = 560;
			} else {
				mListVisibleNum = 6;
				mGirdColumnWidth = 280;
			}
		} else if (MachineConfig.VALUE_SYSTEM_UI35_KLD813.equals(value)) {
			if (ds.widthPixels == 800) {
				mListVisibleNum = 5;
				mGirdColumnWidth = 160;
			} else if (ds.widthPixels == 1280 /* && ds.heightPixels == 720 */) {
				mListVisibleNum = 6;
				mGirdColumnWidth = 213;
			} else {
				mListVisibleNum = 6;
				mGirdColumnWidth = 170;
			}
		} else {
			
			if (ds.widthPixels == 800) {
				mListVisibleNum = 5;
				mGirdColumnWidth = 266;
			} else if (ds.widthPixels == 1280 && ds.heightPixels == 480) {
				mListVisibleNum = 8;
				mGirdColumnWidth = 426;
			} else if (ds.widthPixels == 1280 && ds.heightPixels == 720) {
				mListVisibleNum = 6;
				mGirdColumnWidth = 426;
			} else if (ds.widthPixels < 1024 && ds.heightPixels == 480) {
				mListVisibleNum = 5;
				mGirdColumnWidth = 320;
			} else if (ds.widthPixels == 1920 && ds.heightPixels == 1080) {
				mListVisibleNum = 6;
				mGirdColumnWidth = 639;
			} else if (ds.widthPixels == 949) {
				mListVisibleNum = 6;
				mGirdColumnWidth = 315;
			} else {
				mListVisibleNum = 6;
				mGirdColumnWidth = 341;
			}
//			Log.d("kkd", ":"+ds.widthPixels);
		}
	}

	// new ui
	private void setViewVisible(View mainView, int id, int visibility) {
		View v = mainView.findViewById(id);
		if (v != null) {
			v.setVisibility(visibility);
		}
	}

	private void setViewEnable(View mainView, int id, boolean b) {
		View v = mainView.findViewById(id);
		if (v != null) {
			v.setEnabled(b);
		}
	}

	private OnClickListener mOnClickListenerChannel = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Log.d("ccc", "onClick!!!");
			// TODO Auto-generated method stub
			int position = getChannel00Position(arg0.getId());// -R.id.channel_00;
			if (position != -1) {
				// GlobalDef.reactiveSource(mContext, SOURCE,
				// RadioService.mAudioFocusListener);
				//
				mLongPressSeekButton = false;
				mAkRadio.sendMRDCommand(AkRadio.MRD_LIST_INDEX, position);
			}

		}
	};

	private OnLongClickListener mOnLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View arg0) {
            int id = arg0.getId();
            if (id == R.id.btn_next) {
                mAkRadio.sendMRDCommand(AkRadio.MRD_SEEK_DOWN);
            } else if (id == R.id.btn_prev) {
                mAkRadio.sendMRDCommand(AkRadio.MRD_SEEK_UP);
            } else {
                return false;
            }
			return true;
		}
	};
	
	private final OnLongClickListener mOnLongClickListenerChannel = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View arg0) {
			// TODO Auto-generated method stub
			int position = getChannel00Position(arg0.getId());// -R.id.channel_00;
			if (position != -1) {
				mUpdateChannell = position;
				mAkRadio.sendMRDCommand(AkRadio.MRD_SAVE, position);
				mAkRadio.addFreqPS(mAkRadio.mMRDFreqency, mAkRadio.mStrRdsPs);
			}
			return false;
		}
	};

	private void updateButtonChannel(int index) {
		Log.d("abc", "updateButtonChannel" + index);

		View v = mMainView.findViewById(R.id.channel_00);
		if (v == null) {
			return;
		}

		if (index != -1) {
			if (mHorizontalScrollView == null) {
				mHorizontalScrollView = (HorizontalScrollView) mMainView
						.findViewById(R.id.radio_frequency_scrollview);
			}

			if (mHorizontalScrollView instanceof MyScrollView) {
				int page = mAkRadio.mCurPlayIndex / 6;
				MyScrollView new_name = (MyScrollView) mHorizontalScrollView;
				new_name.scrollToPage(page);
			}
		}

		updateChannel00Text(index);

		// index = R.id.channel_00 + index;
		// int i = 0;
		// for (; i < AkRadio.MIN_FREQ_LIST; ++i) {
		// v = mMainView.findViewById(i+R.id.channel_00);
		// if (v != null) {
		//
		//
		// if (index == i) {
		// v.setBackgroundResource(R.drawable.c_bg_button_sel);
		// } else {
		// v.setBackgroundResource(R.drawable.c_bg_button);
		// }
		//
		// if (mAkRadio != null) {
		// int freq = mAkRadio.mMRDFreqs[i];
		// if (freq == 0) {
		// freq = mAkRadio.mFreqMin;
		// }
		// if (mAkRadio.mMRDBand < AkRadio.MRD_AM) {
		// String ps = mAkRadio.getFreqPS(freq);
		// if (ps == null) {
		// String s = (freq / 100) + ".";
		// if ((freq % 100) < 10) {
		// s += "0";
		// }
		// s += (freq % 100);
		//
		// if(MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef.getSystemUI())){
		//
		// } else {
		// s += " MHz";
		// }
		// ((TextView)v).setText(s);
		//
		// } else {
		// ((TextView)v).setText(ps);
		// }
		//
		// } else {
		// String s = freq+" ";
		// if(MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef.getSystemUI())){
		//
		// } else {
		// s += "KHz";
		// }
		// ((TextView)v).setText(s);
		// }
		// }
		// }
		// }

	}

	private void doListPage(boolean next) {

		if (mHorizontalScrollView instanceof MyScrollView) {

			MyScrollView new_name = (MyScrollView) mHorizontalScrollView;

			int page = new_name.getCurPage();

			if (next) {
				page++;
			} else {
				if (page == 0) {
					return;
				}
				page--;
			}
			new_name.postInvalidate();
			new_name.scrollToPage(page);

		}

	}

	private boolean mLongPressSeekButton = false;

	private void doKeySeekAndStepNext() {
		if (mLongPressSeekButton) {
			mAkRadio.sendMRDCommand(AkRadio.MRD_STEP_DOWN);
		} else {
			mAkRadio.sendMRDCommand(AkRadio.MRD_SEEK_DOWN);
		}
	}

	private void doKeySeekAndStepPrev() {
		if (mLongPressSeekButton) {
			mAkRadio.sendMRDCommand(AkRadio.MRD_STEP_UP);
		} else {
			mAkRadio.sendMRDCommand(AkRadio.MRD_SEEK_UP);
		}
	}

	private OnLongClickListener mOnLongClickListenerSeekAndSetp = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View arg0) {
			// int i = arg0.getId();
			mLongPressSeekButton = !mLongPressSeekButton;
			// if (i == R.id.radio_function_button_seek_previous_and_step){
			// doKeySeekAndStepPrev();
			// } else {
			// doKeySeekAndStepNext();
			// }

			// if(mLongPressSeekButton){
			// mAkRadio.sendMRDCommand(AkRadio.MRD_STEP_UP);
			// } else {
			// mAkRadio.sendMRDCommand(AkRadio.MRD_SEEK_UP);
			// }

			return false;
		}
	};

	private final Handler mHandlerFlipFreq = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			updateFlipFreq();
		}
	};

	private void stopFlipFreq() {
		mFlipShow = true;
		mHandlerFlipFreq.removeMessages(0);
	}

	private boolean mFlipShow = true;

	private void updateFlipFreq() {
		mHandlerFlipFreq.removeMessages(0);
		if ((mAkRadio.mMRDStrength & 0x80) != 0) {
			mFlipShow = !mFlipShow;
			mHandlerFlipFreq.sendEmptyMessageDelayed(0, 800);
		} else {
			mFlipShow = true;
		}

		int id;

		if (mAkRadio.mStrRdsPs != null && mAkRadio.mStrRdsPs.length() > 0) {
			id = R.id.freq_text_ps;
		} else {
			id = R.id.freq_text;
		}

		if (mFlipShow) {
			setViewVisible(mMainView, id, View.VISIBLE);
			// setViewVisible(mMainView, R.id.freq_text_ps, View.VISIBLE);
		} else {
			setViewVisible(mMainView, id, View.INVISIBLE);
			// setViewVisible(mMainView, R.id.freq_text_ps, View.GONE);
		}
	}

	private void changeFreqListNum() {

		if (mFavoriteListAdapter != null) {
			if (mAkRadio.mMRDBand < AkRadio.MRD_AM) {
				mFavoriteListAdapter.setCount(AkRadio.FM_FREQ_LIST);
			} else {
				mFavoriteListAdapter.setCount(AkRadio.AM_FREQ_LIST);
			}
			mFavoriteListAdapter.notifyDataSetChanged();

		}

		if (mFreqsAdapter != null) {
			if (mAkRadio.mMRDBand < AkRadio.MRD_AM) {
				mFreqsAdapter.setCount(AkRadio.FM_FREQ_LIST);
			} else {
				mFreqsAdapter.setCount(AkRadio.AM_FREQ_LIST);
			}
			mFreqsAdapter.notifyDataSetChanged();

		}

		if (mGirdViewFreqs != null) {
			View v = mMainView.findViewById(R.id.radio_gridview_layout);
			if (v != null) {
				int w;
				int num;
				if (mAkRadio.mMRDBand < AkRadio.MRD_AM) {
					num = AkRadio.FM_FREQ_LIST;
				} else {
					num = AkRadio.AM_FREQ_LIST;
				}

				if (mFreqsAdapter.mLineNum != 0) {
					num = num / mFreqsAdapter.mLineNum;
				}
				w = num * mGirdColumnWidth;

				ViewGroup.LayoutParams lp = v.getLayoutParams();
				lp.width = w;
				v.setLayoutParams(lp);

			}
		} else {
			View v = mMainView
					.findViewById(R.id.radio_scrollview_content_width);
			if (v != null) {
				int w;

				View v2 = mMainView.findViewById(R.id.radio_am_hide_layout);
				if (mAkRadio.mMRDBand < AkRadio.MRD_AM) {
					w = mContext.getResources().getInteger(
							R.integer.radio_grid_fm_widht);
					v2.setVisibility(View.VISIBLE);
				} else {
					w = mContext.getResources().getInteger(
							R.integer.radio_grid_am_widht);
					v2.setVisibility(View.GONE);
				}
				ViewGroup.LayoutParams lp = v.getLayoutParams();
				lp.width = w;
				v.setLayoutParams(lp);

			}

		}
	}

	private void setTextView(int id, String s) {
		View v = mMainView.findViewById(id);
		if (v instanceof TextView) {
			TextView new_name = (TextView) v;
			new_name.setText(s);
		}
	}

	private int mPreChannelSel = -1;
	private int mPreChannelBaud = -1;

	private int mUpdateChannell = -1;

	private void updateChannel00Text(int index) {
		// Log.d("ccd", "updateChannel00Text:"+mPreChannelSel);
		if (mPreChannelSel == -1 || mPreChannelBaud != mAkRadio.mMRDBand) {
			setChanne00Text(R.id.channel_00, index, 0);
			setChanne00Text(R.id.channel_01, index, 1);
			setChanne00Text(R.id.channel_02, index, 2);
			setChanne00Text(R.id.channel_03, index, 3);
			setChanne00Text(R.id.channel_04, index, 4);
			setChanne00Text(R.id.channel_05, index, 5);
			setChanne00Text(R.id.channel_06, index, 6);
			setChanne00Text(R.id.channel_07, index, 7);
			setChanne00Text(R.id.channel_08, index, 8);
			setChanne00Text(R.id.channel_09, index, 9);
			setChanne00Text(R.id.channel_10, index, 10);
			setChanne00Text(R.id.channel_11, index, 11);
			setChanne00Text(R.id.channel_12, index, 12);
			setChanne00Text(R.id.channel_13, index, 13);
			setChanne00Text(R.id.channel_14, index, 14);
			setChanne00Text(R.id.channel_15, index, 15);
			setChanne00Text(R.id.channel_16, index, 16);
			setChanne00Text(R.id.channel_17, index, 17);
			setChanne00Text(R.id.channel_18, index, 18);
			setChanne00Text(R.id.channel_19, index, 19);
			setChanne00Text(R.id.channel_20, index, 20);
			setChanne00Text(R.id.channel_21, index, 21);
			setChanne00Text(R.id.channel_22, index, 22);
			setChanne00Text(R.id.channel_23, index, 23);
			setChanne00Text(R.id.channel_24, index, 24);
			setChanne00Text(R.id.channel_25, index, 25);
			setChanne00Text(R.id.channel_26, index, 26);
			setChanne00Text(R.id.channel_27, index, 27);
			setChanne00Text(R.id.channel_28, index, 28);
			setChanne00Text(R.id.channel_29, index, 29);

			mPreChannelBaud = mAkRadio.mMRDBand;
		} else {
			if (mUpdateChannell != -1) {
				setChanne00Text(getPosChannel00(mUpdateChannell), index,
						mUpdateChannell);
				mUpdateChannell = -1;
			}
			View v;
			int id = getPosChannel00(mPreChannelSel);
			if (id != 0) {
				v = mMainView.findViewById(id);
				if (v != null) {
					v.setSelected(false);
				}
			}
			id = getPosChannel00(index);
			if (id != 0) {
				v = mMainView.findViewById(id);
				v.setSelected(true);
				clearFocus(v);
			}
		}
		mPreChannelSel = index;
	}

	private void clearFocus(View v2) {
		if (MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef
				.getSystemUI())
				|| MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef
						.getSystemUI())) {

			if (mContext instanceof Activity) {
				View v = ((Activity) mContext).getCurrentFocus();
				if (v != null) {
					v.clearFocus();
					v2.requestFocus();
					v2.requestFocusFromTouch();
				}
			}
		}
	}

	private void setChanne00Background(int id, int resid) {
		View v = mMainView.findViewById(id);
		if (v != null) {
			v.setBackgroundResource(resid);
		}
	}

	private Drawable mBGButton;
	private Drawable mBGButtonSel;

	private void setChanne00Text(int id, int index, int i) {

		View v = mMainView.findViewById(id);
		if (v != null) {

			// Log.d("ccd", index+":"+i+":"+mAkRadio.mMRDFreqs[i]);
			// if (mBGButton == null) {
			// mBGButton = mContext.getResources().getDrawable(
			// R.drawable.c_bg_button);
			// }
			// if (mBGButtonSel == null) {
			// mBGButtonSel = mContext.getResources().getDrawable(
			// R.drawable.c_bg_button_sel);
			// }
			// v.setBackground(mBGButton);

			if (index == i) {
				v.setSelected(true);
				// v.setBackground(mBGButtonSel);
				// v.setBackgroundResource(R.drawable.c_bg_button_sel);
			} else {
				v.setSelected(false);
				// v.setBackground(mBGButton);
				// v.setBackgroundResource(R.drawable.c_bg_button);
			}

			if (mAkRadio != null) {
				int freq = mAkRadio.mMRDFreqs[i];
				if (freq == 0) {
					freq = mAkRadio.mFreqMin;
				}
				if (mAkRadio.mMRDBand < AkRadio.MRD_AM) {
					String ps = mAkRadio.getFreqPS(freq);
					if (MachineConfig.VALUE_SYSTEM_UI22_1050.equals(GlobalDef
							.getSystemUI())
							|| MachineConfig.VALUE_SYSTEM_UI35_KLD813_2
									.equals(GlobalDef.getSystemUI())) {
						ps = null;
					}
					if (ps == null) {
						String s = (freq / 100) + ".";
						if ((freq % 100) < 10) {
							s += "0";
						}
						s += (freq % 100);

						if (MachineConfig.VALUE_SYSTEM_UI21_RM10_2
								.equals(GlobalDef.getSystemUI())
								|| MachineConfig.VALUE_SYSTEM_UI22_1050
										.equals(GlobalDef.getSystemUI())
								|| MachineConfig.VALUE_SYSTEM_UI35_KLD813_2
										.equals(GlobalDef.getSystemUI())) {

						} else {
							s += " MHz";
						}
						((TextView) v).setText(s);

					} else {
						((TextView) v).setText(ps);
					}

				} else {
					String s = freq + " ";
					if (MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef
							.getSystemUI())
							|| MachineConfig.VALUE_SYSTEM_UI22_1050
									.equals(GlobalDef.getSystemUI())) {

					} else {
						s += "KHz";
					}
					((TextView) v).setText(s);
				}
			}
		}

	}

	public int getChannel00Position(int id) {
		int ret = -1;
        if (id == R.id.channel_00) {
            ret = 0;
        } else if (id == R.id.channel_01) {
            ret = 1;
        } else if (id == R.id.channel_02) {
            ret = 2;
        } else if (id == R.id.channel_03) {
            ret = 3;
        } else if (id == R.id.channel_04) {
            ret = 4;
        } else if (id == R.id.channel_05) {
            ret = 5;
        } else if (id == R.id.channel_06) {
            ret = 6;
        } else if (id == R.id.channel_07) {
            ret = 7;
        } else if (id == R.id.channel_08) {
            ret = 8;
        } else if (id == R.id.channel_09) {
            ret = 9;
        } else if (id == R.id.channel_10) {
            ret = 10;
        } else if (id == R.id.channel_11) {
            ret = 11;
        } else if (id == R.id.channel_12) {
            ret = 12;
        } else if (id == R.id.channel_13) {
            ret = 13;
        } else if (id == R.id.channel_14) {
            ret = 14;
        } else if (id == R.id.channel_15) {
            ret = 15;
        } else if (id == R.id.channel_16) {
            ret = 16;
        } else if (id == R.id.channel_17) {
            ret = 17;
        } else if (id == R.id.channel_18) {
            ret = 18;
        } else if (id == R.id.channel_19) {
            ret = 19;
        } else if (id == R.id.channel_20) {
            ret = 20;
        } else if (id == R.id.channel_21) {
            ret = 21;
        } else if (id == R.id.channel_22) {
            ret = 22;
        } else if (id == R.id.channel_23) {
            ret = 23;
        } else if (id == R.id.channel_24) {
            ret = 24;
        } else if (id == R.id.channel_25) {
            ret = 25;
        } else if (id == R.id.channel_26) {
            ret = 26;
        } else if (id == R.id.channel_27) {
            ret = 27;
        } else if (id == R.id.channel_28) {
            ret = 28;
        } else if (id == R.id.channel_29) {
            ret = 29;
        }
		return ret;
	}

	public int getPosChannel00(int channel) {
		int ret = 0;
		switch (channel) {
		case 0:
			ret = R.id.channel_00;
			break;
		case 1:
			ret = R.id.channel_01;
			break;
		case 2:
			ret = R.id.channel_02;
			break;
		case 3:
			ret = R.id.channel_03;
			break;
		case 4:
			ret = R.id.channel_04;
			break;
		case 5:
			ret = R.id.channel_05;
			break;
		case 6:
			ret = R.id.channel_06;
			break;
		case 7:
			ret = R.id.channel_07;
			break;
		case 8:
			ret = R.id.channel_08;
			break;
		case 9:
			ret = R.id.channel_09;
			break;
		case 10:
			ret = R.id.channel_10;
			break;
		case 11:
			ret = R.id.channel_11;
			break;
		case 12:
			ret = R.id.channel_12;
			break;
		case 13:
			ret = R.id.channel_13;
			break;
		case 14:
			ret = R.id.channel_14;
			break;
		case 15:
			ret = R.id.channel_15;
			break;
		case 16:
			ret = R.id.channel_16;
			break;
		case 17:
			ret = R.id.channel_17;
			break;
		case 18:
			ret = R.id.channel_18;
			break;
		case 19:
			ret = R.id.channel_19;
			break;
		case 20:
			ret = R.id.channel_20;
			break;
		case 21:
			ret = R.id.channel_21;
			break;
		case 22:
			ret = R.id.channel_22;
			break;
		case 23:
			ret = R.id.channel_23;
			break;
		case 24:
			ret = R.id.channel_24;
			break;
		case 25:
			ret = R.id.channel_25;
			break;
		case 26:
			ret = R.id.channel_26;
			break;
		case 27:
			ret = R.id.channel_27;
			break;
		case 28:
			ret = R.id.channel_28;
			break;
		case 29:
			ret = R.id.channel_29;
			break;
		}
		return ret;
	}

	private static final int[] BUTTON_ON_FOCUS_CHANGE = new int[] {
			R.id.channel_00, R.id.channel_01, R.id.channel_02, R.id.channel_03,
			R.id.channel_04, R.id.channel_05, R.id.channel_06, R.id.channel_07,
			R.id.channel_08, R.id.channel_09, R.id.channel_10, R.id.channel_11,
			R.id.channel_12, R.id.channel_13, R.id.channel_14, R.id.channel_15,
			R.id.channel_16, R.id.channel_17, R.id.channel_18, R.id.channel_19,
			R.id.channel_20, R.id.channel_21, R.id.channel_22, R.id.channel_23,
			R.id.channel_24, R.id.channel_25, R.id.channel_26, R.id.channel_27,
			R.id.channel_28, R.id.channel_29, };

	private void initFocusChange() {
		if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef
				.getSystemUI())
				|| MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef
						.getSystemUI())) {
			if (mDisplayIndex == 0) {
				for (int id : BUTTON_ON_FOCUS_CHANGE) {
					View v = mMainView.findViewById(id);
					if (v != null) {
						v.setOnFocusChangeListener(mOnFocusChangeListener);
					}
				}
			}
		}
	}

	private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			// TODO Auto-generated method stub
			int page = 0;
            int arg0Id = arg0.getId();
            if (arg0Id == R.id.channel_00 || arg0Id == R.id.channel_01 || arg0Id == R.id.channel_02 || arg0Id == R.id.channel_03 || arg0Id == R.id.channel_04 || arg0Id == R.id.channel_05) {
                page = 0;
            } else if (arg0Id == R.id.channel_06 || arg0Id == R.id.channel_07 || arg0Id == R.id.channel_08 || arg0Id == R.id.channel_09 || arg0Id == R.id.channel_10 || arg0Id == R.id.channel_11) {
                page = 1;
            } else if (arg0Id == R.id.channel_12 || arg0Id == R.id.channel_13 || arg0Id == R.id.channel_14 || arg0Id == R.id.channel_15 || arg0Id == R.id.channel_16 || arg0Id == R.id.channel_17) {
                page = 2;
            } else if (arg0Id == R.id.channel_18 || arg0Id == R.id.channel_19 || arg0Id == R.id.channel_20 || arg0Id == R.id.channel_21 || arg0Id == R.id.channel_22 || arg0Id == R.id.channel_23) {
                page = 3;
            } else if (arg0Id == R.id.channel_24 || arg0Id == R.id.channel_25 || arg0Id == R.id.channel_26 || arg0Id == R.id.channel_27 || arg0Id == R.id.channel_28 || arg0Id == R.id.channel_29) {
                page = 4;
            }
			if (mHorizontalScrollView instanceof MyScrollView) {
				// int page = mFreqsAdapter.indexToPage(mAkRadio.mCurPlayIndex);
				MyScrollView new_name = (MyScrollView) mHorizontalScrollView;
				new_name.scrollToPage(page);
			}
			if (arg1) {
				int id = getPosChannel00(mAkRadio.mCurPlayIndex);
				if (id != 0) {
					View v = mMainView.findViewById(id);
					if (v != null) {
						v.setSelected(false);
					}
				}
			}
		}
	};

	private void updateAllChannelBandText() {
		View v = mMainView.findViewById(R.id.channel_baud_index_00);
		if (v != null) {
			String s = "";

			if (AkRadio.mFMNum > 1) {
				if (mAkRadio.mMRDBand <= AkRadio.MRD_AM) {
					s = "FM" + (mAkRadio.mMRDBand + 1);
				} else {
					s = "AM" + (mAkRadio.mMRDBand - AkRadio.MRD_AM + 1);
				}
			} else {
			}

			setTextView(R.id.channel_baud_index_00, s);
			setTextView(R.id.channel_baud_index_01, s);
			setTextView(R.id.channel_baud_index_02, s);
			setTextView(R.id.channel_baud_index_03, s);
			setTextView(R.id.channel_baud_index_04, s);
			setTextView(R.id.channel_baud_index_05, s);
			setTextView(R.id.channel_baud_index_06, s);
			setTextView(R.id.channel_baud_index_07, s);
			setTextView(R.id.channel_baud_index_08, s);
			setTextView(R.id.channel_baud_index_09, s);
			setTextView(R.id.channel_baud_index_10, s);
			setTextView(R.id.channel_baud_index_11, s);
			setTextView(R.id.channel_baud_index_12, s);
			setTextView(R.id.channel_baud_index_13, s);
			setTextView(R.id.channel_baud_index_14, s);
			setTextView(R.id.channel_baud_index_15, s);
			setTextView(R.id.channel_baud_index_16, s);
			setTextView(R.id.channel_baud_index_17, s);
			setTextView(R.id.channel_baud_index_18, s);
			setTextView(R.id.channel_baud_index_19, s);
			setTextView(R.id.channel_baud_index_20, s);
			setTextView(R.id.channel_baud_index_21, s);
			setTextView(R.id.channel_baud_index_22, s);
			setTextView(R.id.channel_baud_index_23, s);
			setTextView(R.id.channel_baud_index_24, s);
			setTextView(R.id.channel_baud_index_25, s);
			setTextView(R.id.channel_baud_index_26, s);
			setTextView(R.id.channel_baud_index_27, s);
			setTextView(R.id.channel_baud_index_28, s);
			setTextView(R.id.channel_baud_index_29, s);
		}
	}

}
