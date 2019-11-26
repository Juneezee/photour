package com.photour.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class BitmapRawTask extends BitmapTask {

  /**
   * Constructor for BitmapRawTask
   *
   * @param context contextReference of activity
   * @param imageView imageView that the bitmap will be set on
   */
  public BitmapRawTask(Context context, ImageView imageView) {
    super(context, imageView);
  }

  @Override
  protected Bitmap doInBackground(String... params) {
    final Context context = contextReference.get();

//    InputStream inputStream = null;
//    try {
//      inputStream = new BufferedInputStream(context.getContentResolver().openInputStream(params[0]));
//    } catch (Exception e) {
//      e.printStackTrace();
//    }

    return BitmapFactory.decodeFile(params[0]);
  }
}
