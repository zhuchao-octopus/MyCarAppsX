package com.octopus.android.carapps.common.player;

import android.content.Context;

public class MusicPlayer extends ComMediaPlayer {
    private static final String[] AUDIO_EXTENSION = {
            "mp3", "wav", "mpa", "mp2", "m1a", "m2a", "ogg", "m4a", "mka", "ac3", "wma", "ape", "flac"
    };
    public static boolean mFirstRun = true;

    public MusicPlayer(Context ac) {
        super(ac);
        setScan(true);
        mMediaFile = AUDIO_EXTENSION;

        TAG = "MusicPlayer";
        mSaveDataPath = "audio_data";
    }
}
