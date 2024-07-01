package com.octopus.android.carapps.common.player;

import android.content.Context;


public class MoviePlayer extends ComMediaPlayer {
    private static final String[] VIDEO_EXTENSION = {
            ".avi", /*".wmv", ".wmp",*/
            /*".wm",*/ ".asf", ".mpg", ".mpeg", ".mpe", ".m1v", ".m2v", ".mpv2",
            /* ".dat", */".ts", ".vob", ".ogm", ".ogv", ".mp4", ".m4v", ".m4p", ".m4b", /*".3gp", ".3gpp",*/
            ".mkv", ".rm", ".ram", ".rmvb", ".flv", ".swf", ".mov", /*".3gpp",*/
    };

    public static boolean mFirstRun = true;

    public MoviePlayer(Context context) {
        super(context);
        //super.setLockTime(800);
        TAG = "MoviePlayer";
        mMediaFile = VIDEO_EXTENSION;
        mSaveDataPath = "video_data";
    }
}
