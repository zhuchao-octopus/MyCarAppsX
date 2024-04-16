package com.octopus.android.carapps.radio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.common.util.MachineConfig;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.car.ui.GlobalDef;

public class MyListViewAdapter extends ArrayAdapter<String> {

    private int mLayout;
    private Context mActivity;
    private int mTextId;
    private LayoutInflater mInflater;
    private int MIN_COUNT = AkRadio.MIN_FREQ_LIST;
    private AkRadio mAkRadio;

    //	private int MAX_COUNT = AkRadio.MIN_FREQ_LIST;

    private int mCustomListFocusColor;


    public MyListViewAdapter(Context context, int layout) {
        super(context, layout);

        mLayout = layout;
        mActivity = context;
        mTextId = R.id.list_text;

        mInflater = LayoutInflater.from(mActivity);

        //		String value = MachineConfig
        //				.getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);
        //		String value = GlobalDef.getSystemUI();
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

        if (MachineConfig.VALUE_SYSTEM_UI_KLD12_80.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI19_KLD1.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI28_7451.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI35_KLD813_2.equals(GlobalDef.getSystemUI()) || MachineConfig.VALUE_SYSTEM_UI43_3300_1.equals(GlobalDef.getSystemUI())) {
            mLineNum = 2;
            mRowNum = 3;
        }
    }

    public int mRowNum = 0;
    public int mLineNum = 0;

    public void clearRowLine() {
        mRowNum = 0;
        mLineNum = 0;
    }

    public int indexToPage(int index) {
        int one_page_num = mLineNum * mRowNum;
        int total_page = MIN_COUNT / one_page_num;
        int i = 1;
        for (; i < total_page; ++i) {
            if (index < one_page_num * i) {
                break;
            }
        }
        return i - 1;
    }

    public int toCustomIndex(int index) {

        //Log.d("ccd", "<<" + index);
        if (mLineNum > 0) {
            int one_line_num = MIN_COUNT / 2;
            int one_page_num = mLineNum * mRowNum;
            //			int total_page = AkRadio.MIN_FREQ_LIST / one_page_num;

            if (index >= mRowNum) {

                int row = 0;

                row = (index % one_line_num) / mRowNum;

                if (index < one_line_num) {
                    index = row * one_page_num + (index - (mRowNum * row));
                } else {
                    index = row * one_page_num + ((index - one_line_num) - (mRowNum * row)) + mRowNum;
                }

            }
            // return index;
        }
        //Log.d("ccd", ">>" + index);
        return index;
    }

    // public void updateFreqs(short[] freqs) {
    // for (int i = 0; i < freqs.length; ++i) {
    // mMRDFreqs[i] = freqs[i];
    // }
    // }
    public void setAkRadio(AkRadio radio) {
        mAkRadio = radio;
    }

    public void setCount(int count) {
        MIN_COUNT = count;
    }

    public int getCount() {
        int i = 0;

        if (mAkRadio != null) {
            for (i = 0; i < mAkRadio.mMRDFreqs.length; ++i) {
                if (mAkRadio.mMRDFreqs[i] == 0) {
                    break;
                }
            }
        }

        if (i < MIN_COUNT) {
            i = MIN_COUNT;
        }

        return i;
    }

    public long getItemId(int position) {
        return 0;
    }

    public static class ViewHolder {
        public TextView text1;
        public TextView text2;
        public TextView text3;
        public TextView textPS;
        public int index;

    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayout, null, false);
            viewHolder = new ViewHolder();
            viewHolder.text1 = (TextView) convertView.findViewById(R.id.list_text1);
            viewHolder.text2 = (TextView) convertView.findViewById(R.id.list_text2);
            viewHolder.text3 = (TextView) convertView.findViewById(R.id.list_text3);
            viewHolder.textPS = (TextView) convertView.findViewById(R.id.list_ps_name);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        position = toCustomIndex(position);
        if (MachineConfig.VALUE_SYSTEM_UI35_KLD813.equals(GlobalDef.getSystemUI())) {
            viewHolder.text1.setText("P" + (position + 1));
        } else {
            viewHolder.text1.setText((position + 1) + "");
        }
        if (mAkRadio != null) {
            int freq = mAkRadio.mMRDFreqs[position];
            if (freq == 0) {
                freq = mAkRadio.mFreqMin;
            }
            if (mAkRadio.mMRDBand < AkRadio.MRD_AM) {
                String ps = mAkRadio.getFreqPS(freq);
                if (ps == null || viewHolder.textPS == null) {
                    String s = (freq / 100) + ".";
                    if ((freq % 100) < 10) {
                        s += "0";
                    }
                    s += (freq % 100);

                    viewHolder.text2.setText(s);
                    viewHolder.text3.setText("MHz");

                    viewHolder.text2.setVisibility(View.VISIBLE);
                    viewHolder.text3.setVisibility(View.VISIBLE);
                    if (viewHolder.textPS != null) {
                        viewHolder.textPS.setVisibility(View.GONE);
                    }
                } else {
                    if (viewHolder.textPS != null) {
                        viewHolder.textPS.setText(ps);
                        viewHolder.textPS.setVisibility(View.VISIBLE);

                        viewHolder.text2.setVisibility(View.GONE);
                        viewHolder.text3.setVisibility(View.GONE);
                    }
                }

            } else {
                viewHolder.text2.setText("" + freq);
                viewHolder.text3.setText("KHz");
            }

            // if (mPos != mAkRadio.mCurPlayIndex) {
            // mPos = mAkRadio.mCurPlayIndex;
            // }

            if (mPos == position) {
                viewHolder.text1.setTextColor(mCustomListFocusColor);
                viewHolder.text2.setTextColor(mCustomListFocusColor);
                viewHolder.text3.setTextColor(mCustomListFocusColor);
                if (viewHolder.textPS != null) {
                    viewHolder.textPS.setTextColor(mCustomListFocusColor);
                }
            } else {

                viewHolder.text1.setTextColor(mActivity.getResources().getColor(R.color.list_normal_colre));
                viewHolder.text2.setTextColor(mActivity.getResources().getColor(R.color.list_normal_colre));
                viewHolder.text3.setTextColor(mActivity.getResources().getColor(R.color.list_normal_colre));
                if (viewHolder.textPS != null) {
                    viewHolder.textPS.setTextColor(mActivity.getResources().getColor(R.color.list_normal_colre));
                }

            }
        }
        viewHolder.index = position;

        return convertView;

    }

    private int mPos = 0;

    public void setSelectItem(int pos) {
        setSelectItemIncludeFolder(pos, true);
    }

    public void setSelectItemIncludeFolder(int pos, boolean folder) {
        if (mPos != pos) {

            mPos = pos;
            notifyDataSetChanged();

        }
    }
}
