/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

import com.common.util.SystemConfig;
import com.common.util.Util;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.common.ui.UIBase;

import java.io.File;
import java.util.ArrayList;

public class WallpaperUI extends UIBase implements
        AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {

    private static final String TAG = "WallpaperUI";

    public final static String PATH_WALLPAPER = SystemConfig.PATH_WALLPAPER;
    public final static String PATH_DEFAULT_WALLPAPER0 = SystemConfig.PATH_DEFAULT_WALLPAPER0;
    public final static String PATH_DEFAULT_WALLPAPER1 = SystemConfig.PATH_DEFAULT_WALLPAPER1;
    private static String PATH_WALLPAPER_SMALL = "/mnt/paramter/wallpaper/small/";

    public final static String BROADCAST_SCREEN1_WALLPAPER_CHANGE = "com.car.ui.BROADCAST_SCREEN1_WALLPAPER_CHANGE";

    private ArrayList<String> mThumbs = new ArrayList<String>();
    private WallpaperLoader mLoader;
    private WallpaperDrawable mWallpaperDrawable = new WallpaperDrawable();

    private static WallpaperUI[] mUI = new WallpaperUI[MAX_DISPLAY];

    public static WallpaperUI getInstanse(Context context, View view, int index) {
        if (index >= MAX_DISPLAY) {
            return null;
        }

        mUI[index] = new WallpaperUI(context, view, index);

        return mUI[index];
    }

    public WallpaperUI(Context context, View view, int index) {
        super(context, view, index);
        mType = index;
    }

    public int mType = 0;
    View mView;

    public void onCreate() {
        super.onCreate();

        findWallpapers();

        mView = mMainView;// inflater.inflate(R.layout.wallpaper_chooser,
        // container,
        // false);
        mView.setBackground(mWallpaperDrawable);

        final Gallery gallery = (Gallery) mMainView.findViewById(R.id.gallery);
        gallery.setCallbackDuringFling(false);
        gallery.setOnItemSelectedListener(this);
        gallery.setAdapter(new ImageAdapter(mContext));

        View setButton = mMainView.findViewById(R.id.set);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectWallpaper(gallery.getSelectedItemPosition());
            }
        });

        View cancelButton = mMainView.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quit();
            }
        });
    }

    private void cancelLoader() {
        if (mLoader != null
                && mLoader.getStatus() != WallpaperLoader.Status.FINISHED) {
            mLoader.cancel(true);
            mLoader = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelLoader();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void quit() {
        if (mType == 0) {
            if (mContext instanceof Activity) {
                Activity activity = (Activity) mContext;
                activity.setResult(Activity.RESULT_OK);
                activity.finish();
            }
        } else {
            if (mCurWallPapaer != null) {
                Intent it = new Intent(
                        WallpaperUI.BROADCAST_SCREEN1_WALLPAPER_CHANGE);
                it.setPackage(mContext.getPackageName());
                mContext.sendBroadcast(it);
            }
        }
    }

    @SuppressLint("ServiceCast")
    private void selectWallpaper(int position) {
        try {
            if (mType == 0) {
                if (mContext instanceof Activity) {
                    Activity activity = (Activity) mContext;

                    WallpaperManager wpm = (WallpaperManager) activity
                            .getSystemService(Context.WALLPAPER_SERVICE);

                    wpm.setBitmap(mWallpaperDrawable.getBitmap());

                    if (0 != SystemConfig.getIntProperty(mContext,
                            SystemConfig.KEY_CE_STYLE
                    )) {

                        SystemConfig.setProperty(mContext,
                                SystemConfig.KEY_CE_STYLE_WALLPAPER_NAME,
                                mCurWallPapaer);
                    } else {
                        SystemConfig.setProperty(mContext,
                                SystemConfig.KEY_SCREEN0_WALLPAPER_NAME,
                                mCurWallPapaer);
                    }


                    activity.setResult(Activity.RESULT_OK);
                    activity.finish();
                }
            } else {
                if (mCurWallPapaer != null) {
                    SystemConfig.setProperty(mContext,
                            SystemConfig.KEY_SCREEN1_WALLPAPER_NAME,
                            mCurWallPapaer);

                    Intent it = new Intent(
                            WallpaperUI.BROADCAST_SCREEN1_WALLPAPER_CHANGE);
                    it.setPackage(mContext.getPackageName());
                    mContext.sendBroadcast(it);
                }

            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to set wallpaper: " + e);
        }
    }

    // Click handler for the Dialog's GridView
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        selectWallpaper(position);
    }

    // Selection handler for the embedded Gallery view
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        if (mLoader != null
                && mLoader.getStatus() != WallpaperLoader.Status.FINISHED) {
            mLoader.cancel();
        }
        mLoader = (WallpaperLoader) new WallpaperLoader().execute(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void findWallpapers() {

        File filepath = new File(PATH_WALLPAPER_SMALL);
        if (!filepath.exists()) {
            PATH_WALLPAPER_SMALL = PATH_WALLPAPER;
            filepath = new File(PATH_WALLPAPER_SMALL);
        }
        if (filepath.exists()) {

            File[] files = filepath.listFiles();
            for (File file : files) {
                if (!file.isDirectory()) {
                    File f = new File(PATH_WALLPAPER + file.getName());
                    if (f.exists()) {
                        mThumbs.add(file.getName());
                    }
                }
            }
        }
    }

    private class ImageAdapter extends BaseAdapter implements ListAdapter,
            SpinnerAdapter {
        private LayoutInflater mLayoutInflater;

        ImageAdapter(Context activity) {
            mLayoutInflater = LayoutInflater.from(activity);// activity.getLayoutInflater();
        }

        public int getCount() {
            return mThumbs.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mLayoutInflater.inflate(R.layout.wallpaper_item, parent,
                        false);
            } else {
                view = convertView;
            }

            ImageView image = (ImageView) view
                    .findViewById(R.id.wallpaper_image);

            // int thumbRes = mThumbs.get(position);
            // image.setImageResource(thumbRes);

            Drawable thumbDrawable = Drawable
                    .createFromPath(PATH_WALLPAPER_SMALL
                            + mThumbs.get(position));

            image.setImageDrawable(thumbDrawable);

            // Drawable thumbDrawable = image.getDrawable();
            if (thumbDrawable != null) {
                thumbDrawable.setDither(true);
            } else {
                Log.e(TAG,
                        "Error decoding thumbnail resId="
                                + mThumbs.get(position) + " for wallpaper #"
                                + position);
            }

            return view;
        }
    }

    private String mCurWallPapaer = null;

    class WallpaperLoader extends AsyncTask<Integer, Void, Bitmap> {
        WallpaperLoader() {
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            if (isCancelled())
                return null;
            try {
                mCurWallPapaer = mThumbs.get(params[0]);
                return BitmapFactory.decodeFile(PATH_WALLPAPER
                        + mThumbs.get(params[0]));
            } catch (Exception e) {
                mCurWallPapaer = null;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap b) {
            if (b == null)
                return;

            if (!isCancelled()) {
                if (mWallpaperDrawable.getBitmap() != null) {
                    mWallpaperDrawable.getBitmap().recycle();
                }

                mWallpaperDrawable.setBitmap(b);
                mView.postInvalidate();
                mLoader = null;
            } else {
                b.recycle();
            }
        }

        void cancel() {
            super.cancel(true);
        }
    }

    /**
     * Custom drawable that centers the bitmap fed to it.
     */
    static class WallpaperDrawable extends Drawable {

        Bitmap mBitmap;
        int mIntrinsicWidth;
        int mIntrinsicHeight;
        Matrix mMatrix;

        public Bitmap getBitmap() {
            return mBitmap;
        }

        /* package */void setBitmap(Bitmap bitmap) {
            mBitmap = bitmap;
            if (mBitmap == null)
                return;
            mIntrinsicWidth = mBitmap.getWidth();
            mIntrinsicHeight = mBitmap.getHeight();
            mMatrix = null;
        }

        @Override
        public void draw(Canvas canvas) {
            if (mBitmap == null)
                return;

            if (mMatrix == null) {
                final int vwidth = canvas.getWidth();
                final int vheight = canvas.getHeight();
                final int dwidth = mIntrinsicWidth;
                final int dheight = mIntrinsicHeight;

                float scale = 1.0f;

                if (dwidth < vwidth || dheight < vheight) {
                    scale = Math.max((float) vwidth / (float) dwidth,
                            (float) vheight / (float) dheight);
                }

                float dx = (vwidth - dwidth * scale) * 0.5f + 0.5f;
                float dy = (vheight - dheight * scale) * 0.5f + 0.5f;

                mMatrix = new Matrix();
                mMatrix.setScale(scale, scale);
                mMatrix.postTranslate((int) dx, (int) dy);
            }

            canvas.drawBitmap(mBitmap, mMatrix, null);
        }

        @Override
        public int getOpacity() {
            return android.graphics.PixelFormat.OPAQUE;
        }

        @Override
        public void setAlpha(int alpha) {
            // Ignore
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            // Ignore
        }
    }

    public void udpateWallPaper() {
    }
}
