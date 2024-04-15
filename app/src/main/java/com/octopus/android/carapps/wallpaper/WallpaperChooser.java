/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.octopus.android.carapps.wallpaper;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.octopus.android.carapps.R;

public class WallpaperChooser extends Activity {
	@SuppressWarnings("unused")
	private static final String TAG = "FileManager.WallpaperChooser";

	WallpaperUI mUI;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.wallpaper_chooser);

		mUI = WallpaperUI.getInstanse(this, findViewById(R.id.screen1_main), 0);
		mUI.onCreate();
		updateIntent(getIntent());
	}

	private void updateIntent(Intent it) {
		if (it != null) {
			int i = it.getIntExtra("type", 0);
			if (i != 0) {
				mUI.mType = 1;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mUI != null)
			mUI.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mUI != null)
			mUI.onPause();

		finish();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mUI != null)
			mUI.onDestroy();

	}
}
