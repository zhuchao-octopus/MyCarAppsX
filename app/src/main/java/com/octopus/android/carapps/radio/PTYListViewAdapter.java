package com.octopus.android.carapps.radio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.octopus.android.carapps.R;
import com.octopus.android.carapps.car.ui.GlobalDef;

import java.util.ArrayList;
import java.util.List;

public class PTYListViewAdapter extends BaseAdapter {
    private List<View> PTYTypeList;
    public static String[] mPty_data = {
            "NONE", "NEWS", "AFFAIRS", "INFO", "SPORTS", "EDUCATE", "DRAMA", "CULTURE", "SCIENCE", "VARIED", "POP M", "ROCK M", "EASY M", "LIGHT M", "CLASSICS", "OTHER M", "WEATHERE", "FINANCE",
            "CHILDREN", "SOCIAL", "RELIGION", "PHONE IN", "TRAVEL", "LEISURE", "JAZZ", "COUNTRY", "NATION M", "OLDIES", "FOLK M", "DOCUMENT", "TEST", "ALARM !"
    };
    private static final int PTY_MAX = 32;
    private Context mContext;
    private int mCustomListFocusColor;

    public void updatePtyData(Context context) {
        for (int i = 0; i < mPty_data.length; ++i) {
            mPty_data[i] = context.getString(R.string.pty00 + i);
        }
    }

    public PTYListViewAdapter(Context context, int id) {
        PTYTypeList = new ArrayList<View>(PTY_MAX);
        View v;
        TextView tv;
        mContext = context;
        //	    LayoutInflater inflater = context.getLayoutInflater();

        LayoutInflater inflater = LayoutInflater.from(context);

        updatePtyData(context);
        for (int i = 0; i < PTY_MAX; i++) {
            v = inflater.inflate(id, null, false);
            tv = (TextView) v.findViewById(R.id.list_text);
            tv.setText(mPty_data[i]);
            v.setTag(i);
            PTYTypeList.add(v);
        }

        //		String value = MachineConfig
        //				.getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);
        //		String value = GlobalDef.getSystemUI();
        //
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

    public int getCount() {
        // TODO Auto-generated method stub
        return PTY_MAX;
    }

    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return PTYTypeList.get(position);
    }


    private void setTextColor(View v, int color) {
        if (v == null) return;

        TextView tv = (TextView) v.findViewById(R.id.list_text);
        if (tv != null) {
            tv.setTextColor(color);
            v.postInvalidate();
        }
    }

    private int mPos = -1;

    public void setSelectItem(int pos) {
        if (PTYTypeList == null || pos >= PTYTypeList.size()) {
            return;
        }

        if (mPos != pos) {

            setTextColor(PTYTypeList.get(pos), mCustomListFocusColor);

            if (mPos != -1) {
                setTextColor(PTYTypeList.get(mPos), mContext.getResources().getColor(R.color.list_normal_colre));
            }

            mPos = pos;
        }
        notifyDataSetChanged();
    }

}
