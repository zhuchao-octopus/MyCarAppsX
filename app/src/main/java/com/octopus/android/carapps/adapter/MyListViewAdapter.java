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


public class MyListViewAdapter extends ArrayAdapter<String> {
    private ArrayList<String> mList;
    private int mFolderNum;
    private int mFolderSet = -1;
    private int mFolderLevel;


    private int mLayout;
    private Context mActivity;
    private int mTextId;
    private int mPlayIconId;
    LayoutInflater mInflater;

    private int mCustomListFocusColor;

    public MyListViewAdapter(Context context, int layout, ArrayList<String> list, int folderNum) {
        super(context, layout, list);
        mList = new ArrayList<String>();
        for (String s : list) {
            mList.add(s);
        }
        if (folderNum == -1) {
            mFolderNum = 1;
            mFolderLevel = 2;
        } else {
            mFolderLevel = 1;
            mFolderNum = folderNum;
        }
        mLayout = layout;
        mActivity = context;
        mTextId = R.id.list_text;
        mPlayIconId = R.id.play_indicator;
        // mInflater = mActivity.getLayoutInflater();
        mInflater = LayoutInflater.from(mActivity);

        //		String value = MachineConfig
        //				.getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);
        //		if (MachineConfig.VALUE_SYSTEM_UI_KLD7_1992.equals(value)) {
        //			mCustomListFocusColor = 0xffff0000;
        //		} else {
        //			mCustomListFocusColor = context.getResources().getColor(
        //					R.color.list_hilight_colre);
        //		}

        if (GlobalDef.mListCommonColor == 0) {
            mCustomListFocusColor = context.getResources().getColor(R.color.list_hilight_colre);
        } else {
            mCustomListFocusColor = GlobalDef.mListCommonColor;
        }

    }

    public ArrayList<String> getList() {
        return mList;
    }

    public String getSelectName() {
        if (mList != null && mPos >= 0 && mPos < mList.size()) {
            return mList.get(mPos);
        }
        return null;
    }

    public int getFolderNum() {
        return mFolderNum;
    }

    public int getFolderSet() {
        return mFolderSet;
    }

    public void setFolderSet(int i) {
        mFolderSet = i;
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
        public TextView textId3Time;
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
            if (mTimeTextId != 0) {
                viewHolder.textId3Time = (TextView) convertView.findViewById(mTimeTextId);
            }
            viewHolder.playing = (ImageView) convertView.findViewById(mPlayIconId);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.text.setText(mList.get(position));
        viewHolder.index = position;
        if (mPos == position) {
            viewHolder.text.setTextColor(mCustomListFocusColor);
        } else {

            viewHolder.text.setTextColor(mActivity.getResources().getColor(R.color.list_normal_colre));
        }

        if (viewHolder.playing != null) {
            if (mPlayingType == 0) {
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
            } else if (mPlayingType == 1) {
                if (mPos == position) {
                    viewHolder.playing.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.playing.setVisibility(View.GONE);
                }
            }

            if (viewHolder.textId3Time != null) {
                if (mId3Time != null && position < mId3Time.size()) {
                    viewHolder.textId3Time.setText(mId3Time.get(position));
                }
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


    private ArrayList<String> mId3Time;
    private int mTimeTextId = 0;
    private int mPlayingType = 0;

    public void updateTimeTextId(int id) {
        mTimeTextId = id;
    }

    public void updateId3Time(ArrayList<String> s) {
        mId3Time = s;
        notifyDataSetChanged();
    }

    public void setPlayingType(int type) {
        mPlayingType = type;
    }

    public void setSelectTextColor(int c) {
        mCustomListFocusColor = c;
    }
}
