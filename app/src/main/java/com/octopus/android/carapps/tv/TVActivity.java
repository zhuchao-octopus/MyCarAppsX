package com.octopus.android.carapps.tv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.common.util.BroadcastUtil;
import com.common.util.Kernel;
import com.common.util.MyCmd;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.common.ui.GlobalDef;
import com.octopus.android.carapps.common.ui.UIBase;
import com.octopus.android.carapps.common.utils.ResourceUtil;


public class TVActivity extends Activity implements OnGestureListener {
    UIBase mUI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //boolean multiWindow = ResourceUtil.updateAppUi(this);
        //if (multiWindow) {
        //    GlobalDef.updateMultiWindowActivity(this);
        //}

        super.onCreate(savedInstanceState);

        TV.init(this);

        if (TV.mType == 0) {
            setContentView(R.layout.tv_player);
            mUI = TvUI.getInstanse(this, findViewById(R.id.screen1_main), 0);
            detector = new GestureDetector(this, this);
        } else {
            int layout = R.layout.tv_player_cvbs;
            switch (TV.mType) {
                case 1:
                    break;
                case 2:
                    layout = R.layout.tv_player_cvbs2_asuka_hr600;
                    break;

                case 3:
                    layout = R.layout.tv_player_cvbs_touch;
                    detector = new GestureDetector(this, mSimpleOnGestureListener);
                    break;
            }
            setContentView(layout);
            mUI = TvUICvbs.getInstanse(this, findViewById(R.id.screen1_main), 0);
            //BroadcastUtil.sendToCarService(this, MyCmd.Cmd.SET_DTV_CMD, 0x3000000 | TV.mType);
        }
        mUI.onCreate();
    }

    GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            TV.sendTouch((int) e.getX(), (int) e.getY());
            return super.onSingleTapConfirmed(e);

        }

        /**
         * 双击发生时的通知
         * @param e
         * @return
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {//双击事件
            if (mUI instanceof TvUICvbs) {
                TvUICvbs new_name = (TvUICvbs) mUI;
                new_name.toggleFullScreen();
            }
            return super.onDoubleTap(e);
        }

        /**
         * 双击手势过程中发生的事件，包括按下、移动和抬起事件
         * @param e
         * @return
         */
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }
    };

    @Override
    protected void onNewIntent(Intent it) {
        setIntent(it);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mUI != null) mUI.onResume();
        GlobalDef.openGps(this, getIntent());
        setIntent(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mUI != null) {
            mUI.onPause();
            //mUI.onDestroy();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUI != null) mUI.onDestroy();

        if (GlobalDef.mSource == mUI.mSource /*&& BTMusicService.mPlayStatus >= BTMusicService.A2DP_INFO_CONNECTED*/) {
            BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mUI instanceof TvUICvbs) {
            TvUICvbs new_name = (TvUICvbs) mUI;
            new_name.mWillDestory = true;
        }
        if (GlobalDef.mSource == mUI.mSource /*&& BTMusicService.mPlayStatus >= BTMusicService.A2DP_INFO_CONNECTED*/) {
            BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
        }
    }

    private int verticalMinDistance = 50;
    private int minVelocity = 0;

    private GestureDetector detector;

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1.getX() - e2.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity) {

            BroadcastUtil.sendToCarService(this, MyCmd.Cmd.SET_SCREEN0_SOURCE, 0);

            Kernel.doKeyEvent(Kernel.KEY_HOMEPAGE);
            // Toast.makeText(this, "向左手势", Toast.LENGTH_SHORT).show();
        } else if (e2.getX() - e1.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity) {

            Kernel.doKeyEvent(Kernel.KEY_BACK);
            // Toast.makeText(this, "向右手势", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (detector != null) {
            return detector.onTouchEvent(event);
        }
        return false;
    }


    @Override
    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

}