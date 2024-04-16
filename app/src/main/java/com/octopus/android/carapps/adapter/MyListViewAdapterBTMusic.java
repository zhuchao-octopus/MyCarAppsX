package com.octopus.android.carapps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.octopus.android.carapps.R;
import com.octopus.android.carapps.car.ui.GlobalDef;

import java.util.ArrayList;

public class MyListViewAdapterBTMusic extends ArrayAdapter<BTMusicNode> {
    private ArrayList<BTMusicNode> mList;
    private int mFolderNum;
    private int mFolderSet = -1;
    private int mFolderLevel;

    private int mLayout;
    private Context mActivity;
    private int mTextId;
    private int mPlayIconId;
    LayoutInflater mInflater;

    private int mCustomListFocusColor;

    public MyListViewAdapterBTMusic(Context context, int layout, ArrayList<BTMusicNode> list) {

        super(context, layout, list);
        //		mList = new ArrayList<String>();
        //		for (String s : list) {
        //			mList.add(s);
        //		}
        mList = list;

        mLayout = layout;
        mActivity = context;
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

        if (GlobalDef.mListCommonColor == 0) {
            mCustomListFocusColor = context.getResources().getColor(R.color.list_hilight_colre);
        } else {
            mCustomListFocusColor = GlobalDef.mListCommonColor;
        }
    }

    public ArrayList<BTMusicNode> getList() {
        return mList;
    }

    public int getFolderNum() {
        return mFolderNum;
    }

    public int getFolderSet() {
        return mFolderSet;
    }

    public void setFolderSet(int i) {
        mFolderSet = i;
        ;
    }

    public int getFolderLevel() {
        return mFolderLevel;
    }

    public void clearSelectItem() {
        if (mList == null || mPos >= mList.size()) {
            return;
        }

    }

    public int getCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public long getItemId(int position) {
        return 0;
    }

    public static class ViewHolder {
        public TextView text;
        public ImageView playing;
        public int index;

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (mList == null) return null;

        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayout, null, false);
            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) convertView.findViewById(mTextId);
            viewHolder.playing = (ImageView) convertView.findViewById(mPlayIconId);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.text.setText(mList.get(position).name);
        viewHolder.index = position;
        if (mPos == position) {
            viewHolder.text.setTextColor(mCustomListFocusColor);
        } else {

            viewHolder.text.setTextColor(mActivity.getResources().getColor(R.color.list_normal_colre));
        }

        if (viewHolder.playing != null) {
            if (position < mFolderNum) {

                viewHolder.playing.getDrawable().setLevel(mFolderLevel);

                // if (mPos == position) {
                // viewHolder.playing.setVisibility(View.VISIBLE);
                // } else {
                // viewHolder.playing.setVisibility(View.GONE);
                // }

            } else {
                viewHolder.playing.getDrawable().setLevel(0);
            }
        }

        return convertView;

    }

    private int mPos = -1;

    public void setSelectItem(int pos) {
        setSelectItemIncludeFolder(pos, true);
    }

    public void setSelectItemIncludeFolder(int pos, boolean folder) {
        if (!folder) {
            pos += mFolderNum;
        }

        if (mPos != pos) {

            mPos = pos;
            notifyDataSetChanged();

        }
    }

    public void setNextFocus() {

    }
}
