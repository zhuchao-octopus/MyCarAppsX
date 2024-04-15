package com.octopus.android.carapps.radio;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;

public class MyScrollView extends HorizontalScrollView {
	private int lastScrollX;
	private int downScrollX = -1;

	public MyScrollView(Context context) {
		this(context, null);
	}

	public MyScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		Object tag = getTag();
		if (tag instanceof String) {
			String new_name = (String) tag;
			String[] ss = new_name.split(":");
			mDefaultPage = Integer.valueOf(ss[1]);
			ss = ss[0].split(",");
			if (ss != null && ss.length > 0) {
				PAGE = new int[ss.length];
				for (int i = 0; i < ss.length; ++i) {
					try {
						PAGE[i] = Integer.valueOf(ss[i]);
					} catch (Exception e) {

					}
				}
			}
		}
	}

	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub
		super.onLayout(arg0, arg1, arg2, arg3, arg4);
//		Log.d("aabb", "onLayout:");
		if (mDefaultPage != -1) {
			mPage = mDefaultPage;
			scrollTo(PAGE[mDefaultPage], 0);
			mDefaultPage = -1;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		if (mDefaultPage != -1) {
			// scrollTo(PAGE[mDefaultPage], 0);
			// mDefaultPage = -1;
		} else {
			checkInNewPage();
		}

//		Log.d("aabb", "onDraw:");

		super.onDraw(canvas);
	}

	private final static int MSG_TOUCH = 0;
	private final static int MSG_CALLBACK = 1;
	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_TOUCH:
				if (downScrollX == -1) {
					return;
				}
				int scrollX = MyScrollView.this.getScrollX();

				// 此时的距离和记录下的距离不相等，在隔5毫秒给handler发送消息

				// Log.d("aabb", scrollX + ":" + lastScrollX + ":" +
				// downScrollX);
				if (lastScrollX != scrollX) {
					lastScrollX = scrollX;
					handler.sendEmptyMessageDelayed(MSG_TOUCH, 5);
					// handler.sendMessageDelayed(handler.obtainMessage(), 5);
				} else {
					if (downScrollX < -1) {
						doPage(downScrollX == -3, scrollX);
					} else {
						doPage(scrollX);
					}

					downScrollX = -1;
				}
				break;
			case MSG_CALLBACK:
				if (mICallBack != null) {
					mICallBack.callback(msg.arg1, msg.arg2);
				}
				break;
			}

		};

	};

	OnGestureListener mOnGestureListener = new OnGestureListener() {
		@Override
		public boolean onDown(MotionEvent e) {
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
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// Log.d("ttdd", (e2.getX() - e1.getX()) + ":onFling:" + velocityX);
			if (velocityX > 0) {
				downScrollX = -2;
			} else {
				downScrollX = -3;
			}
			// doNextPage(velocityX > 0);
			return false;
		}
	};
	GestureDetector mygesture = new GestureDetector(mOnGestureListener);

	private boolean mTouchDown =false;
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (downScrollX == -1) {
			downScrollX = getScrollX();
		}
//		Log.d("aabb1", "ACTION_DOWN:"+ev.getAction());
		switch (ev.getAction()) {
		case MotionEvent.ACTION_UP:
			lastScrollX = this.getScrollX();

			handler.sendEmptyMessageDelayed(MSG_TOUCH, 5);
			// handler.sendMessageDelayed(handler.obtainMessage(), 5);

			 callBackMsg(CMD_TOUCH_EVENT, MotionEvent.ACTION_UP);
			mTouchDown = false;
			break;

		case MotionEvent.ACTION_DOWN:
			mPreNewPage = -1;
			callBackMsg(CMD_TOUCH_EVENT, MotionEvent.ACTION_DOWN);
			mTouchDown = true;
//			Log.d("aabb1", "ACTION_DOWN:");
			break;
		 case MotionEvent.ACTION_MOVE:
		// mPreNewPage = -1;
//		 Log.d("aabb", "ACTION_MOVE:");
			 callBackMsg(CMD_TOUCH_EVENT, MotionEvent.ACTION_MOVE);
			 break;
		}
		mygesture.onTouchEvent(ev);
		return super.onTouchEvent(ev);
	}

	private int mPage = 0;

	private int[] PAGE = { 0, 1024 };
	private int mDefaultPage = -1;

	private void doPage(int x) {
		int i = 0;
		for (i = 0; i < (PAGE.length - 1); ++i) {
			if (x >= PAGE[i] && x <= (PAGE[i] + ((PAGE[i + 1] - PAGE[i]) / 2))) {
				smoothScrollTo(PAGE[i], 0);
				mPage = i;
				break;
			} else if (x <= PAGE[i + 1]
					&& x > (PAGE[i] + ((PAGE[i + 1] - PAGE[i]) / 2))) {
				smoothScrollTo(PAGE[i + 1], 0);
				mPage = i + 1;
				break;
			}
		}
		showPageIndex(mPage);
	}

	public void scrollToPage(int page) {
		if (page >= 0 && page < PAGE.length && mPage != page) {
			mPage = page;
			smoothScrollTo(PAGE[page], 0);
			callBackMsg(CMD_NEWPAGE, page);
		}
	}

	private void doPage(boolean b, int x) {
		int i = -1;
		if (b) {
			if (mPage < (PAGE.length - 1)) {
				for (i = PAGE.length - 2; i >= 0; --i) {
					if (x > PAGE[i]) {
						smoothScrollTo(PAGE[i + 1], 0);
						mPage = i + 1;
						break;
					}
				}
			}
		} else {
			if (mPage > 0) {
				for (i = 1; i <= (PAGE.length - 1); ++i) {
					if (x <= PAGE[i]) {
						smoothScrollTo(PAGE[i - 1], 0);
						mPage = i - 1;
						break;
					}
				}
			}
		}
		if (i != -1) {
			// mPage = i;
			showPageIndex(mPage);
		} else {
			doPage(x);
		}

		Log.d("aaa", b + "doPage:" + x);
	}

	private void showPageIndex(int index) {

		// for (int i = 0; i < 3; ++i) {
		//
		// View v = getRootView().findViewById(R.id.page1_ce+i);
		// if(v!=null){
		// try{
		// if(index==i){
		// v.getBackground().setLevel(1);
		// }else{
		// v.getBackground().setLevel(0);
		// }
		// }catch (Exception e){
		//
		// }
		// }
		// }
	}

	private int mPreNewPage = -1;

	private void checkInNewPage() {

		boolean left = true;
		if ((getScrollX() - downScrollX) > 0) {
			left = false;
		}

		int newPage = -1;
		for (int i = 0; i < (PAGE.length - 1); ++i) {
			if (getScrollX() > PAGE[i] && getScrollX() < PAGE[i + 1]) {
				newPage = i;
				if (!left) {
					newPage++;
				}
				// Log.d("aaa", newPage + ":checkInNewPage"+mPreNewPage);
				break;
			}
		}

		if (newPage != -1 && mPreNewPage != newPage) {
			mPreNewPage = newPage;
			callBackMsg(CMD_NEWPAGE, newPage);
			// Log.d("aaa1", newPage + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}

	}

	private void callBackMsg(int cmd, int visible_page) {
		if (mICallBack != null) {
			handler.sendMessage(handler.obtainMessage(MSG_CALLBACK, cmd,
					visible_page));
		}
	}

	public static final int CMD_NEWPAGE = 0;
	public static final int CMD_TOUCH_EVENT = 1;

	public static interface ICallBack {
		public void callback(int cmd, int visible_page);
	};

	private ICallBack mICallBack;

	public void setCallback(ICallBack cb) {
		mICallBack = cb;
	}

	public int getCurPage() {
		return mPage;
	}
	public boolean getTouchDown(){
		return mTouchDown;
	}
}
