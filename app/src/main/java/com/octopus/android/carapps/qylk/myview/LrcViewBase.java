package com.octopus.android.carapps.qylk.myview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public abstract class LrcViewBase extends TextView implements ILrcView {
	protected int cColor = Color.GREEN, ncColor = Color.WHITE;
	protected int centerX, centerY;
	protected Paint cPaint, ncPaint;
	protected float fp;
	protected int gap=0;
	protected int index;
	protected float len;
	protected LrcPackage lrcpac;
	protected int lTime, cTime, sTime;
	protected int nextpoint, offset;
	protected boolean shadow = false;
	protected float TextSize=16.0f;
	protected Typeface Texttypeface = Typeface.DEFAULT;
	
	//ww+, for display 3lines(prev and next line)
	public int lines = 1;
	public int marginTop = 0;
	protected int centerPX, centerPY;	//prev line, for 3lines
	protected int centerNX, centerNY;	//next line, for 3lines
	protected float pnTextSize = 16.0f;
	protected int pnColor = Color.rgb(0x66, 0x66, 0x66);
	protected Paint pnPaint;	

	public LrcViewBase(Context context) {
		this(context, null, 0);
	}

	public LrcViewBase(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LrcViewBase(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void clearView() {
		setText("");
		lrcpac = null;
	}

	public LrcPackage GetLrcPackage() {
		return lrcpac;
	}

	public int GetOffset() {
		return offset;
	}

	protected void init() {
		setLrcTextSize(TextSize);
		ncPaint = new Paint();// 闈為珮浜�閮ㄥ垎
		ncPaint.setAntiAlias(true);
		ncPaint.setTextAlign(Paint.Align.CENTER);
		ncPaint.setTextSize(TextSize);
		ncPaint.setColor(ncColor);
		ncPaint.setTypeface(Texttypeface);
		ncPaint.setShadowLayer(3, 2, -1, Color.BLACK);
		cPaint = new Paint();// 楂樹寒閮�鍒�褰撳墠姝�璇�
		cPaint.setAntiAlias(true);
		cPaint.setTextAlign(Paint.Align.CENTER);
		cPaint.setTypeface(Texttypeface);
		cPaint.setTextSize(TextSize);
		cPaint.setColor(cColor);
		cPaint.setShadowLayer(3, 2, -1, Color.BLACK);
		
		pnPaint = new Paint();
		pnPaint.setAntiAlias(true);
		pnPaint.setTextAlign(Paint.Align.CENTER);
		pnPaint.setTypeface(Texttypeface);
		pnPaint.setTextSize(16.0f);
		pnPaint.setColor(pnColor);
		pnPaint.setShadowLayer(3, 2, -1, Color.BLACK);
	}

	public void initLrcIndex(int curpos) {
		int i = 0;
		int sum = lrcpac.getSum();
		while (i < sum) {
			if (curpos < lrcpac.list.get(i).beginTime) {
				break;// 鏌ヨ鍒颁簡绱㈠紩锛岃繑鍥�
			}
			i++;
		}
		index = i - 1;
		updatedata();
		updateView(curpos);
	}

	@Override
	protected abstract void onDraw(Canvas canvas);

	protected void OnNextLine() {
		index++;
		updatedata();
	}

	@Override
	protected void onSizeChanged(int w, int h, int ow, int oh) {
		super.onSizeChanged(w, h, ow, oh);
		centerX = w >> 1;
		centerY = h >> 1;

		if (lines == 3) {
			int actual_height = h - marginTop;			
			int pn_center = actual_height / 3;
			centerY = marginTop + actual_height / 2;
			centerNX = centerPX = centerX;
			centerPY = marginTop + pn_center / 2;
			centerNY = centerPY + pn_center * 2;
		}
	}

	public void setFirstColor(int color) {
		cColor = color;
		cPaint.setColor(color);
	}

	@Override
	public void setGap(int gap) {
		this.gap = gap;
	}

	public void setLrcTextSize(float size) {
		float des = getResources().getDisplayMetrics().density;
		TextSize = size * des;
		if (cPaint!=null)
			cPaint.setTextSize(TextSize);
	}
	
	public void setLyric(LrcPackage lrc) {
		this.lrcpac = lrc;
		index = -1;
		offset = 0;
	}

	public void setOffset(int offset) {
		this.offset += offset;
		nextpoint = lrcpac.list.get(index + 1).beginTime + offset;
	}

	public void setSecondColor(int color) {
		ncPaint.setColor(color);
	}

	public void setShadow(boolean shadow) {
		this.shadow = shadow;
		if (!shadow)
			cPaint.setShader(null);
	}

	protected void updatedata() {
		if (index == -1) {
			nextpoint = lrcpac.list.get(0).beginTime + offset;
			return;
		}
		if (index >= lrcpac.list.size()){//fix bug by allen
			return;
		}
		sTime = lrcpac.list.get(index).beginTime + offset;// 寮�鏃堕棿
		if (index == lrcpac.getSum() - 1) {
			nextpoint = lrcpac.duration + 1500;
			lTime = lrcpac.duration - sTime;
		} else {
			nextpoint = lrcpac.list.get(index + 1).beginTime + offset;// 鑾峰彇涓嬩竴鍙ユ椂闂磋捣鐐�
			lTime = lrcpac.list.get(index).lineTime;
		}
		len = this.getTextSize() * lrcpac.list.get(index).lrcBody.length();
	}

	public void updateView(int progress) {
		if (lrcpac == null)
			return;
		cTime = progress;
		if (nextpoint < progress) {
			OnNextLine();
		}
		this.postInvalidate();
	}
	
	//ww+ for 3lines
	public void setLines(int lines) {
		this.lines = lines;
	}
	
	public void setLines(int lines, int marginTop) {
		this.lines = lines;
		this.marginTop = marginTop;
	}
	
	public void setMarginTop(int marginTop) {
		this.marginTop = marginTop;
	}

	public void setLrcTextSizePixel(int size, boolean current) {
		float des = getResources().getDisplayMetrics().density;
		if (current) {
			TextSize = size / des;
			if (cPaint != null)
				cPaint.setTextSize(TextSize);
		} else {
			pnTextSize = size / des;
			if (pnPaint != null)
				pnPaint.setTextSize(pnTextSize);
		}
	}
	
	public void setLcrTextPrevNextColor(int color) {
		pnTextSize = color;
		pnPaint.setColor(color);
	}
}
