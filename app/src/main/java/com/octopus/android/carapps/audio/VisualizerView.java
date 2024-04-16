package com.octopus.android.carapps.audio;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.octopus.android.carapps.R;

public class VisualizerView extends View implements OnDataCaptureListener {
    private byte[] mBytes;

    private float[] mPoints;

    private float[] mHats;

    private Rect mRect = new Rect();

    private Paint mPaint = new Paint();

    private float mStrokeWidth;

    private static final int COLUMN = 0;

    private static final int WAVEFORM = 1;

    private static final int FFT = 2;

    private static final int COLUMN_NUM = 30;

    private static final int COLUMN_SPEED = 6;

    private static int mCurrentStyle = FFT;


    public static final int BAR_WIDTH = 4;

    private BitmapDrawable mDrawable;

    private static boolean isReDraw;

    private int mResId = R.drawable.ic_spectrum_bg;


    private Bitmap mBitmap;

    private void init() {
        isReDraw = true;
        mBytes = null;
        mPoints = null;
        switch (mCurrentStyle) {
            case WAVEFORM:
                mStrokeWidth = 2f;
                mPaint.setStrokeWidth(mStrokeWidth);
                mPaint.setAntiAlias(true);
                mPaint.setColor(0x9969A8ED);

                break;
            case COLUMN:
                mStrokeWidth = BAR_WIDTH;
                mPaint.setStrokeWidth(mStrokeWidth);
                mPaint.setAntiAlias(false);
                mPaint.setColor(0xffffffff);
                break;
            case FFT:
                mStrokeWidth = 1f;
                mPaint.setStrokeWidth(mStrokeWidth);
                mPaint.setAntiAlias(false);
                mPaint.setColor(0xff69A8ED);
                break;
        }
        isReDraw = false;
    }

    public VisualizerView(Context context) {
        super(context);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        isReDraw = false;
        init();
        mBitmap = BitmapFactory.decodeResource(getResources(), mResId);
        mDrawable = new BitmapDrawable(mBitmap);
    }

    public void updateWaveFormData(byte[] mbyte) {
        if (mCurrentStyle == WAVEFORM) {
            mBytes = mbyte;
            invalidate();
        }
    }

    public void updateFftData(byte[] mbyte) {
        if (mCurrentStyle != WAVEFORM) {
            mBytes = mbyte;
            invalidate();
        }
    }

    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);

        if (mBytes == null || isReDraw) {
            return;
        }
        //        if (MusicUtils.sService != null) {
        try {
            //                if (MusicUtils.sService.isPlaying()) {
            switch (mCurrentStyle) {
                case WAVEFORM:
                    drawWaveform(canvas);
                    break;
                case COLUMN:
                    drawColumn(canvas);
                    break;
                case FFT:
                    drawFft(canvas);
                    break;

            }
            //                }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            //                e.printStackTrace();
        }
        //        }
    }

    private void drawColumn(Canvas canvas) {
        if (mPoints == null || mPoints.length < COLUMN_NUM * 4) {
            mPoints = new float[COLUMN_NUM * 4];
        }
        if (mHats == null) {
            mHats = new float[COLUMN_NUM * 4];
        }

        // drawable.setTileModeY(TileMode.REPEAT );
        // bitmap = Bitmap.createBitmap(100, 20, Config.ARGB_8888);
        for (int i = 0; i < COLUMN_NUM; i++) {
            if (mBytes[i] <= 0) {
                mBytes[i] = 1;
            }
            mPoints[i * 4] = mRect.width() * i / COLUMN_NUM;
            float p = mRect.height() - mBytes[i];
            //			if (p > (mPoints[i * 4 + 1]+ COLUMN_SPEED)) {
            //				mPoints[i * 4 + 1] = mPoints[i * 4 + 1] + COLUMN_SPEED;
            //			} else {
            mPoints[i * 4 + 1] = p;
            //			}
            //			mPoints[i * 4 + 1] = mRect.height() - mBytes[i];
            mPoints[i * 4 + 2] = mRect.width() * i / COLUMN_NUM;
            mPoints[i * 4 + 3] = mRect.height();
            if (mDrawable != null) {
                mDrawable.setBounds((int) mPoints[i * 4], (int) mPoints[i * 4 + 1], (int) mPoints[i * 4 + 2] + BAR_WIDTH, (int) mPoints[i * 4 + 3]);
                // mDrawable.draw(canvas);

                Rect src = new Rect(0, mBitmap.getHeight() - (((int) mPoints[i * 4 + 3]) - ((int) mPoints[i * 4 + 1])), mBitmap.getWidth(), mBitmap.getHeight());
                Rect dst = new Rect((int) mPoints[i * 4], (int) mPoints[i * 4 + 1], (int) mPoints[i * 4 + 2] + BAR_WIDTH, (int) mPoints[i * 4 + 3]);
                canvas.drawBitmap(mBitmap, src, dst, null);
            }
        }

        // canvas.drawLines(mPoints, mPaint);

        for (int i = 0; i < COLUMN_NUM; i++) {

            mHats[i * 4] = mRect.width() * i / COLUMN_NUM;
            if (mPoints[i * 4 + 1] < mHats[i * 4 + 1]) mHats[i * 4 + 1] = mPoints[i * 4 + 1] - COLUMN_SPEED;
            else if (mPoints[i * 4 + 1] != 0) mHats[i * 4 + 1] += COLUMN_SPEED;
            else mHats[i * 4 + 1] = 0;
            mHats[i * 4 + 2] = mRect.width() * i / COLUMN_NUM + BAR_WIDTH;
            mHats[i * 4 + 3] = mHats[i * 4 + 1];
        }
        //    	canvas.drawArc(mHats, 0, 360, true, mPaint);
        canvas.drawLines(mHats, mPaint);
    }

    private void drawWaveform(Canvas canvas) {
        if (mPoints == null || mPoints.length < mBytes.length * 4) {
            mPoints = new float[mBytes.length * 4];
        }

        int i = 0;
        for (; i < mBytes.length / 2; i++) {
            mPoints[i * 4] = mRect.width() * i / (mBytes.length / 2);
            mPoints[i * 4 + 1] = mRect.height() / 2 + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;
            mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length / 2);
            mPoints[i * 4 + 3] = mRect.height() / 2 + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;
        }
        for (int j = 0; j < mBytes.length / 2 - 1; i++, j++) {
            mPoints[i * 4] = mRect.width() * j / (mBytes.length / 2);
            mPoints[i * 4 + 1] = mRect.height() / 2 + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;
            mPoints[i * 4 + 2] = mRect.width() * (j + 1) / (mBytes.length / 2);
            mPoints[i * 4 + 3] = mRect.height() / 2 + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;
        }

        canvas.drawPoints(mPoints, mPaint);
    }

    private void drawFft(Canvas canvas) {
        if (mPoints == null || mPoints.length < 100 * 4) {
            mPoints = new float[100 * 4];
            canvas.drawColor(Color.TRANSPARENT);
        }

        for (int i = 0; i < 100; i++) {
            if (mBytes[i] < 0) {
                mBytes[i] = 127;
            }
            mPoints[i * 4] = mRect.width() * i / 100;
            mPoints[i * 4 + 1] = mRect.height() - mBytes[i];
            mPoints[i * 4 + 2] = mRect.width() * i / 100;
            mPoints[i * 4 + 3] = mRect.height();
        }
        canvas.drawLines(mPoints, mPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //        if (event.getAction() == MotionEvent.ACTION_DOWN) {
        //            ++mCurrentStyle;
        //            if (mCurrentStyle > FFT)
        //                mCurrentStyle = 0;
        //            init();
        //        }
        return super.onTouchEvent(event);
    }

    public void increaseStyle() {
        ++mCurrentStyle;
        if (mCurrentStyle > FFT) mCurrentStyle = 0;
        init();
    }

    public void setColumnStyle() {
        mCurrentStyle = COLUMN;
        init();
    }

    public void setWaveStyle() {
        mCurrentStyle = WAVEFORM;
        init();
    }

    public void setFFTStyle() {
        mCurrentStyle = FFT;
        init();
    }


    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {

        updateWaveFormData(waveform);
    }

    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        byte[] model = new byte[fft.length / 2 + 1];
        model[0] = (byte) Math.abs(fft[1]);
        int j = 1;

        for (int i = 2; i < fft.length / 2; ) {

            model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);
            i += 2;
            j++;

        }
        updateFftData(model);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mRect.set(0, 0, getWidth(), getHeight());
        // TODO Auto-generated method stub
        super.onLayout(changed, left, top, right, bottom);
    }


}
