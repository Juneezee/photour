package com.android.photour;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.FileNotFoundException;

import static com.android.photour.ui.photos.ImageAdapter.decodeSampledBitmapFromResource;

public class BitmapWorkerTask extends AsyncTask<Uri, Void, Bitmap> {

    Context context;

    public BitmapWorkerTask(Context context) {
        this.context = context;
    }

    @Override
    protected Bitmap doInBackground(Uri... params) {
        Bitmap bitmap;
        try {
            bitmap = decodeSampledBitmapFromResource(
                    context, params[0], 100, 100);
            ((MainActivity)context).addBitmapToMemoryCache(String.valueOf(params[0]), bitmap);
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}