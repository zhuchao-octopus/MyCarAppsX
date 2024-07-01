package com.octopus.android.carapps.btmusic;

import com.octopus.android.carapps.adapter.BTMusicNode;

import java.util.ArrayList;

public class BtListPathArrayList {
    public String mPath;
    public ArrayList<BTMusicNode> mList;

    public BtListPathArrayList(String path, ArrayList<BTMusicNode> list) {
        mPath = path;
        mList = new ArrayList<BTMusicNode>();
        mList.addAll(list);
    }
}
