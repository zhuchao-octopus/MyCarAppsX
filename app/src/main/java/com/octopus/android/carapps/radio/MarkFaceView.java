package com.octopus.android.carapps.radio;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.common.util.MachineConfig;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.car.ui.GlobalDef;

public class MarkFaceView extends View {
	private Paint mPaint;
	public static float mFrequencyNum;
	// private static int FM_X_VALUE = 6;
	// private static int FM_Y_VALUE = 1705;
	private static float AM_X_VALUE = (float) 0.4;
	private static float AM_Y_VALUE = 174;
	private static final int POINTER_WIDTH = 2;
	private static int POINTER_HEIGHT = 150;
	private int mPointY = 40;

	private static final short MAX_FM_FREQUENCY = 10800;
	private static final short MIN_FM_FREQUENCY = 8750;
	private static int MAX_AM_FREQUENCY = 1750;
	private static int MIN_AM_FREQUENCY = 531;

	private static final int MAX_FM_FREQUENCY_EAST_EU = 7400;
	private static final int MIN_FM_FREQUENCY_EAST_EU = 6500;
	private static final int MAX_FM_FREQUENCY_JPN = 9000;
	private static final int MIN_FM_FREQUENCY_JPN = 7600;

	private short mMaxFmFrequency = 0;// MAX_FM_FREQUENCY;
	private short mMinFmFrequency = 0;// MIN_FM_FREQUENCY;
	

	private short mMaxAmFrequency = 0;// MAX_FM_FREQUENCY;
	private short mMinAmFrequency = 0;// MIN_FM_FREQUENCY;

	private float mFmXValue;// = FM_X_VALUE;
	private float mFmYValue;// = FM_Y_VALUE;

	private int START_FREQ_PX = 42;
	private int END_FREQ_PX = 668;

	private Bitmap mFreqPoint;
	/*
	 * how to define the FM_X_VALUE FM_Y_VALUE get the 2 point2 , and its x
	 * value in UI.
	 * 
	 * for example, 87.5 in 95 dip, 108.00 in 900 dip. you can get 2 equations.
	 * 
	 * 87.5 = (95+FM_Y_VALUE)/FM_X_VALUE 108 = (900+FM_Y_VALUE)/FM_X_VALUE
	 * 
	 * you can get the 2 values
	 * 
	 * by allen . you can do like this
	 * 
	 * 87.5 in 95 dip, 108.00 in 900 dip. a = 95, b = 900,
	 * 
	 * FM_Y_VALUE = (87.5 * b - 108 * a)/(108-87.5) FM_X_VALUE = (a + *
	 * FM_Y_VALUE)/87.5
	 * 
	 * now you can only set the START_FREQ_PX & END_FREQ_PX
	 */

	private boolean mIsAM;
	private float mRealNum;
	private boolean mIsInTouch;

	private RadioUI mRadioActivity;

	public MarkFaceView(Context context) {
		super(context);
	}

	public MarkFaceView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.RadioMarkFaceView);

	
		

		END_FREQ_PX = a.getInt(
				R.styleable.RadioMarkFaceView_end_freq_px,
				0);
		if(END_FREQ_PX == 0){
			END_FREQ_PX = context.getResources().getInteger(R.integer.end_freq_px);
		}
		
		START_FREQ_PX = a.getInt(
				R.styleable.RadioMarkFaceView_start_freq_px,
				0);
		if(START_FREQ_PX == 0){
			START_FREQ_PX = context.getResources().getInteger(
					R.integer.start_freq_px);
		}		
        		

		mPointY = a.getInt(R.styleable.RadioMarkFaceView_point_y, 0);
		
		STEP[0] = a.getInt(R.styleable.RadioMarkFaceView_freq_x1, 0);
		STEP[1] = a.getInt(R.styleable.RadioMarkFaceView_freq_x2, 0);
		STEP[2] = a.getInt(R.styleable.RadioMarkFaceView_freq_x3, 0);
		STEP[3] = a.getInt(R.styleable.RadioMarkFaceView_freq_x4, 0);
		STEP[4] = a.getInt(R.styleable.RadioMarkFaceView_freq_x5, 0);
		STEP[5] = a.getInt(R.styleable.RadioMarkFaceView_freq_x6, 0);
		
		if(mPointY == 0){
			mPointY = context.getResources().getInteger(R.integer.point_y);
		}
		
		if(STEP[0] == 0){
			for (int i = 0; i < STEP.length; ++i) {
				STEP[i] = context.getResources().getInteger(R.integer.freq_x1 + i);
			}
		}
		

		mColorPoint = context.getResources().getColor(R.color.freq_point);
		
		FREQ_Y = context.getResources().getInteger(R.integer.freq_text_y);
		
		mPaint = new Paint();	
		
		AM_Y_VALUE = (MIN_AM_FREQUENCY * END_FREQ_PX - MAX_AM_FREQUENCY
				* START_FREQ_PX)
				/ (MAX_AM_FREQUENCY - MIN_AM_FREQUENCY);// FM_X_VALUE;
		AM_X_VALUE = (START_FREQ_PX + AM_Y_VALUE) / MIN_AM_FREQUENCY;// FM_Y_VALUE;

		mTextSize = context.getResources().getInteger(R.integer.freq_text_size);
		setFmFrequencyRange(MIN_FM_FREQUENCY, MAX_FM_FREQUENCY);
		
		int bitmap_point = context.getResources().getInteger(R.integer.freq_bitmap_point);
		
		if (bitmap_point == 1) {
			Drawable d;
			if (!MachineConfig.VALUE_SYSTEM_UI31_KLD7.equals(GlobalDef.getSystemUI())){
				d = context.getResources().getDrawable(
						R.drawable.freq_point);
			} else {
				d = context.getResources().getDrawable(
						R.drawable.freq_point2);
				mPointY = context.getResources().getInteger(R.integer.point_y2);
			}
			
			if (d != null) {
				if (d instanceof BitmapDrawable) {
					BitmapDrawable new_name = (BitmapDrawable) d;
					mFreqPoint = new_name.getBitmap();
				}
			}
		}
	}

	public MarkFaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private int mColorPoint = 0xF02323;
	private int mTextSize = 18;
	
	private static int FREQ_Y = 34;

	int[] STEP = new int[] { 5, 134, 272, 410, 548, 662 };

	@Override
	protected void onDraw(Canvas canvas) {
		mPaint.setColor(Color.parseColor("#FFFFFF"));
		mPaint.setTextSize(mTextSize);
		mPaint.setAntiAlias(true);
		
		if (mIsAM) {
			if (!mIsInTouch && mFrequencyNum != 0) {
				mRealNum = mFrequencyNum * AM_X_VALUE - AM_Y_VALUE
						+ POINTER_WIDTH;
			}
			if (STEP[0] > -1) {
				canvas.drawText("500", STEP[0], FREQ_Y, mPaint);
			}
			if (STEP[1] > -1) {
				canvas.drawText("750", STEP[1], FREQ_Y, mPaint);
			}
			if (STEP[2] > -1) {
				canvas.drawText("1000", STEP[2], FREQ_Y, mPaint);
			}
			if (STEP[3] > -1) {
				canvas.drawText("1250", STEP[3], FREQ_Y, mPaint);
			}
			if (STEP[4] > -1) {
				canvas.drawText("1500", STEP[4], FREQ_Y, mPaint);
			}
			if (STEP[5] > -1) {
				canvas.drawText("1750", STEP[5], FREQ_Y, mPaint);
			}
		} else {
			if (!mIsInTouch && mFrequencyNum != 0) {
				mRealNum = mFrequencyNum * mFmXValue - mFmYValue
						+ POINTER_WIDTH;
			}

			if (STEP[0] > -1) {
				int step = ((mMaxFmFrequency - mMinFmFrequency) / (STEP.length - 1));
				int n;
				for (int i = 0; i < STEP.length - 1; ++i) {
					if(STEP[i]>-1){
					n = (mMinFmFrequency + i * step);
					canvas.drawText((n / 100) + "." + ((n % 100)/10), STEP[i],
							FREQ_Y, mPaint);
					}
				}

				canvas.drawText((mMaxFmFrequency / 100) + "."
						+ ((mMaxFmFrequency % 100)/10), STEP[STEP.length - 1],
						FREQ_Y, mPaint);
			}
		}

		if (mEnable) {
			mPaint.setColor(mColorPoint);
			if (mFreqPoint == null) {
				canvas.drawRect(mRealNum - POINTER_WIDTH, mPointY, mRealNum,
						POINTER_HEIGHT, mPaint);
			} else {
				canvas.drawBitmap(mFreqPoint, mRealNum - (mFreqPoint.getWidth()/2), mPointY, null);
			}

		}

		if (mFreqPoint == null) {
			mPaint.setAlpha(80);
			canvas.drawLine(mRealNum, mPointY, mRealNum, POINTER_HEIGHT, mPaint);
			mPaint.setAlpha(50);
			canvas.drawLine(mRealNum + 1, mPointY, mRealNum + 1,
					POINTER_HEIGHT, mPaint);
			mPaint.setAlpha(10);
			canvas.drawLine(mRealNum + 2, mPointY, mRealNum + 2,
					POINTER_HEIGHT, mPaint);
		}
		// super.onDraw(canvas);
	}

	public boolean ismIsAM() {
		return mIsAM;
	}

	public void setmIsAM(boolean mIsAM) {
		this.mIsAM = mIsAM;
	}

	public void setFmFrequencyRange(short min, short max) {
		if (min == 0 || max == 0) {
			return;
		}

		float minf = min / 100.0f;
		float maxf = max / 100.0f;
		if (mMaxFmFrequency != max || mMinFmFrequency != min) {
			mFmYValue = (minf * END_FREQ_PX - maxf * START_FREQ_PX)
					/ (maxf - minf);// FM_X_VALUE;
			mFmXValue = (START_FREQ_PX + mFmYValue) / minf;// FM_Y_VALUE;

			mMaxFmFrequency = max;
			mMinFmFrequency = min;
		}
	}

	public void setAmFrequencyRange(short min, short max) {
		mMaxAmFrequency = max;
		mMinAmFrequency = min;
//		if (min == 0 || max == 0) {
//			return;
//		}
//
////		float minf = min / 100.0f;
////		float maxf = max / 100.0f;
//		if (MAX_AM_FREQUENCY != max || MAX_AM_FREQUENCY != min) {
//			
////			mFmYValue = (minf * END_FREQ_PX - maxf * START_FREQ_PX)
////					/ (maxf - minf);// FM_X_VALUE;
////			mFmXValue = (START_FREQ_PX + mFmYValue) / minf;// FM_Y_VALUE;
//
//			AM_Y_VALUE = (MIN_AM_FREQUENCY * END_FREQ_PX - MAX_AM_FREQUENCY
//					* START_FREQ_PX)
//					/ (MAX_AM_FREQUENCY - MIN_AM_FREQUENCY);// FM_X_VALUE;
//			AM_X_VALUE = (START_FREQ_PX + AM_Y_VALUE) / MIN_AM_FREQUENCY;// FM_Y_VALUE;
//			
//			MAX_AM_FREQUENCY = max;
//			MIN_AM_FREQUENCY = min;
//		}
	}
	private boolean mEnable = true;

	public void setEnable(boolean b) {
		mEnable = b;
		postInvalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d("eec", "radio + onTouchEvent");
		if (!mEnable)
			return true;

		final int action = event.getAction();

		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mIsInTouch = true;
			break;
		case MotionEvent.ACTION_MOVE:
			short tmp_frequency;
			if (!mIsAM) {
				tmp_frequency = (short) (((event.getX() + mFmYValue) / mFmXValue) * 100);

				if (tmp_frequency > mMaxFmFrequency) {
					tmp_frequency = mMaxFmFrequency;
				} else if (tmp_frequency < mMinFmFrequency) {
					tmp_frequency = mMinFmFrequency;
				}
				mRealNum = (tmp_frequency * mFmXValue / 100) - mFmYValue
						+ POINTER_WIDTH;
			} else {
				tmp_frequency = (short) ((event.getX() + AM_Y_VALUE) / AM_X_VALUE);

				float f = (event.getX() + AM_Y_VALUE);
				f = f / AM_X_VALUE;

				// Log.d("tt", "" + event.getX() + ":" + tmp_frequency + ":"
				// + AM_X_VALUE + ":" + AM_Y_VALUE);

				if (tmp_frequency > mMaxAmFrequency) {
					tmp_frequency = (short)mMaxAmFrequency;
				} else if (tmp_frequency < mMinAmFrequency) {
					tmp_frequency = (short)mMinAmFrequency;
				}
				mRealNum = (tmp_frequency * AM_X_VALUE) - AM_Y_VALUE
						+ POINTER_WIDTH;
			}
			if (mRadioActivity != null) {
				mRadioActivity.setFrequencyNumber(tmp_frequency, false);
			}
			postInvalidate();
			break;
		case MotionEvent.ACTION_UP:
			int t_frequency;
			if (!mIsAM) {
				tmp_frequency = (short) (((event.getX() + mFmYValue) / mFmXValue) * 100);
				if (tmp_frequency > mMaxFmFrequency) {
					tmp_frequency = mMaxFmFrequency;
				} else if (tmp_frequency < mMinFmFrequency) {
					tmp_frequency = mMinFmFrequency;
				}
			} else {
				tmp_frequency = (short) ((event.getX() + AM_Y_VALUE) / AM_X_VALUE);
				if (tmp_frequency > mMaxAmFrequency) {
					tmp_frequency =(short) mMaxAmFrequency;
				} else if (tmp_frequency < mMinAmFrequency) {
					tmp_frequency =(short) mMinAmFrequency;
				}
			}
			mIsInTouch = false;
			if (mRadioActivity != null) {
				mRadioActivity.setFrequencyNumber(tmp_frequency, true);
			}
			mFrequencyNum = 0;
			// postInvalidate();
			break;
		}
		return true;
	}

	public void setRaidoActivity(RadioUI ac) {
		mRadioActivity = ac;
	}

}
