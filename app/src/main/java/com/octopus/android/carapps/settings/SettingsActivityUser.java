package com.octopus.android.carapps.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.octopus.android.carapps.R;
import com.octopus.android.carapps.common.utils.ResourceUtil;

public class SettingsActivityUser extends Activity {

    SettingsUI mUI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ResourceUtil.updateSingleUi(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        mUI = SettingsUI.getInstanse(this, findViewById(R.id.screen1_main), 0);
        mUI.mType = 1;
        mUI.onCreate();
    }

    @Override
    protected void onNewIntent(Intent it) {
        setIntent(it);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUI != null) mUI.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mUI != null) mUI.onPause();

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUI != null) mUI.onDestroy();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mUI != null) {
            mUI.mWillDestory = true;
        }

    }


}
