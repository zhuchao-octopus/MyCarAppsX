package com.octopus.android.carapps.common.qylk.myview;

import com.octopus.android.carapps.common.qylk.lrc.LRCbean;

import java.util.List;

public class LrcPackage {
    public int duration;
    public List<LRCbean> list;

    public LrcPackage(List<LRCbean> list, int duration) {
        this.list = list;
        this.duration = duration;
    }

    public int getSum() {
        return list.size();
    }
}
