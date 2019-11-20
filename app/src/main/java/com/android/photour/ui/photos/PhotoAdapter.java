package com.android.photour.ui.photos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.photour.MainActivity;
import com.android.photour.R;
import com.android.photour.async.AsyncDrawable;
import com.android.photour.async.BitmapWorkerTask;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for images on {@link PhotosFragment}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ImageCard> {

  private static List<Uri> items = new ArrayList<>();
  final Bitmap placeholder;
  private Context context;

  /**
   * Constructor for PhotoAdapter
   *
   * @param context Context of MainActivity
   */
  PhotoAdapter(Context context) {
    this.context = context;
    this.placeholder = BitmapFactory
        .decodeResource(context.getResources(), R.drawable.ic_logo_vertical);
  }

  /**
   * Calculate the size of bitmap that will need to be reduced to to fit the given dimension.
   * Referenced Android Developer: Loading Large Bitmaps Efficiently
   *
   * @param options BitmapFactory options to perform the compression
   * @param reqWidth required width
   * @param reqHeight required height
   * @see <a href="https://developer.android.com/topic/performance/graphics/load-bitmap"></a>
   */
  private static int calculateInSampleSize(
      BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

      final int halfHeight = height / 2;
      final int halfWidth = width / 2;

      // Calculate the largest inSampleSize value that is a power of 2 and keeps both
      // height and width larger than the requested height and width.
      while ((halfHeight / inSampleSize) >= reqHeight
          && (halfWidth / inSampleSize) >= reqWidth) {
        inSampleSize *= 2;
      }
    }

    return inSampleSize;
  }

  /**
   * Function to reduce bitmap size Referenced Android Developer: Loading Large Bitmaps Efficiently
   *
   * @param context Context of MainActivity
   * @param resUri Uri of the image
   * @param reqWidth required width
   * @param reqHeight required height
   * @return the compressed bitmap
   * @throws FileNotFoundException thrown if Uri for image is invalid
   * @see <a href="https://developer.android.com/topic/performance/graphics/load-bitmap"></a>
   */
  public static Bitmap decodeSampledBitmapFromResource(Context context, Uri resUri,
      int reqWidth, int reqHeight) throws FileNotFoundException {
    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    InputStream inputStream = null;
    try {
      inputStream = context.getContentResolver().openInputStream(resUri);
      Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
      options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
      inputStream.close();
      if (options.inSampleSize <= 1) {
        return bitmap;
      } else {
        inputStream = context.getContentResolver().openInputStream(resUri);
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeStream(inputStream,
            null, options);
        inputStream.close();
        return bitmap;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Accessor for items
   *
   * @param items List of Uri for the adapter
   */
  void setItems(List<Uri> items) {
    PhotoAdapter.items = items;
  }

  /**
   * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an
   * item.
   *
   * This new ViewHolder should be constructed with a new View that can represent the items of the
   * given type. You can either create a new View manually or inflate it from an XML layout file.
   *
   * @param parent The ViewGroup into which the new View will be added after it is bound to an
   * adapter position.
   * @param viewType The view type of the new View.
   */
  @NonNull
  @Override
  public ImageCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_image,
        parent, false);
    return new ImageCard(v);
  }

  /**
   * Called by RecyclerView to display the data at the specified position. This method should update
   * the contents of the itemView to reflect the item at the given position.
   *
   * @param holder The ViewHolder which should be updated to represent the contents of the item at
   * the given position in the data set.
   * @param position The position of the item within the adapter's data set.
   */
  @Override
  public void onBindViewHolder(@NonNull ImageCard holder, int position) {
    if (items.get(position) != null) {
      final String imageKey = items.get(position).toString();
      Bitmap bitmap = ((MainActivity) context).getBitmapFromMemCache(imageKey);
      if (bitmap != null) {
        holder.imageView.setImageBitmap(bitmap);
      } else {
        Uri uri = items.get(position);
        if (BitmapWorkerTask.cancelPotentialWork(uri, holder.imageView)) {
          BitmapWorkerTask task = new BitmapWorkerTask(context, holder.imageView);
          final AsyncDrawable asyncDrawable =
              new AsyncDrawable(context.getResources(), placeholder, task);
          holder.imageView.setImageDrawable(asyncDrawable);
          task.execute(uri);
        }
      }
    }

//      holder.imageView.setOnClickListener(view -> {
//        // INSERT CODE TO ENTER IMAGE HERE
//      });
//    }
  }

  /**
   * Getter for data set size
   *
   * @return data set size
   */
  @Override
  public int getItemCount() {
    return items.size();
  }

  /**
   * Class for viewHolder
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  class ImageCard extends RecyclerView.ViewHolder {

    final ImageView imageView;

    ImageCard(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_item);
    }
  }

}
