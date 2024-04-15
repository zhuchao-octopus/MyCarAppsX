package com.octopus.android.carapps.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.AttributeSet;
import android.util.Log;

import com.common.util.MachineConfig;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.car.ui.GlobalDef;

public class MyListViewAdapterDvd extends ArrayAdapter<String> {

	private int mLayout;
	private Context mActivity;
	private int mTextId;
	private int mPlayIconId;

	private int mNum;
	private String mTrack;

	LayoutInflater mInflater;
	private int mCustomListFocusColor;
	public MyListViewAdapterDvd(Context context, int layout, String track,
			int num) {
		super(context, layout);
		mNum = num;
		mLayout = layout;
		mActivity = context;
		mTrack = track;
		mTextId = R.id.list_text;
		mPlayIconId = R.id.play_indicator;
		// mInflater = mActivity.getLayoutInflater();
		mInflater = LayoutInflater.from(mActivity);

//		String value = MachineConfig
//				.getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);
////		if (MachineConfig.VALUE_SYSTEM_UI_KLD7_1992.equals(value)) {
////			mCustomListFocusColor = 0xffff0000;
////		} else {
//			mCustomListFocusColor = context.getResources().getColor(
//					R.color.list_hilight_colre);
////		}
		
		
		if (GlobalDef.mListCommonColor == 0){
			mCustomListFocusColor = context.getResources().getColor(
					R.color.list_hilight_colre);
		} else {
			mCustomListFocusColor = GlobalDef.mListCommonColor;
		}
	}

	public int getCount() {
		return mNum;
	}

	public static class ViewHolder {
		public TextView text;
		public ImageView playing;
		public int index;

	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (mTrack == null || mNum == 0)
			return null;

		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(mLayout, null, false);
			viewHolder = new ViewHolder();
			viewHolder.text = (TextView) convertView.findViewById(mTextId);
			viewHolder.playing = (ImageView) convertView
					.findViewById(mPlayIconId);
			convertView.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.text.setText((mTrack + (position + 1)));
		viewHolder.index = position;
		if (mPos == position) {
			viewHolder.text.setTextColor(mCustomListFocusColor);
		} else {

			viewHolder.text.setTextColor(mActivity.getResources().getColor(
					R.color.list_normal_colre));
		}

		return convertView;

	}

	private int mPos = -1;

	public void setSelectItem(int pos) {



		if (mPos != pos) {

			mPos = pos;
			notifyDataSetChanged();

		}
	}
	

}
