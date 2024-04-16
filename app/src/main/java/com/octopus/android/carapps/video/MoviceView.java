package com.octopus.android.carapps.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;

public class MoviceView extends SurfaceView {

    //private Handler mHandler = null;

    private GestureDetector mGestureDetector;

    public void setGestureDetector(GestureDetector gestureDetector) {
        mGestureDetector = gestureDetector;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (mGestureDetector != null) {
            return mGestureDetector.onTouchEvent(event);
        }
        return false;
    }


    public MoviceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //mHandler = ((MoviceActivity)context).getHandler();
    }

}
