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

import com.android.photour.async.AsyncDrawable;
import com.android.photour.async.BitmapWorkerTask;
import com.android.photour.MainActivity;
import com.android.photour.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ImageCard> {

  private static List<Uri> items = new ArrayList<>();
  private Context context;
  private final int IMAGE_WIDTH = 100;
  final Bitmap placeholder;

  PhotoAdapter(Context context) {
    this.context = context;
    this.placeholder = BitmapFactory.decodeResource(context.getResources(),
          R.drawable.ic_logo_vertical);
  }

  public void setItems(List<Uri> items) {
      this.items = items;
  }


  @NonNull
  @Override
  public ImageCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_image,
            parent, false);
    return new ImageCard(v);
  }

  @Override
  public void onBindViewHolder(@NonNull ImageCard holder, int position) {
    if (items.get(position) != null) {
        final String imageKey = items.get(position).toString();
        Bitmap bitmap = ((MainActivity)context).getBitmapFromMemCache(imageKey);
        if(bitmap != null) {
            holder.imageView.setImageBitmap(bitmap);
        } else {
            Uri uri = items.get(position);
            if (BitmapWorkerTask.cancelPotentialWork(uri,holder.imageView)) {
                System.out.println(position+": "+uri);
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

  // convenience method for getting data at click position
  Uri getItem(int id) {
    return items.get(id);
  }

    public static int calculateInSampleSize(
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

  @Override
  public int getItemCount() {
    return items.size();
  }

    class ImageCard extends RecyclerView.ViewHolder {

        public final ImageView imageView;

        public ImageCard(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_item);
        }
    }

}
