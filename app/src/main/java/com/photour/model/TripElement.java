package com.photour.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;
import androidx.exifinterface.media.ExifInterface;
import androidx.room.ColumnInfo;

import com.photour.R;
import com.photour.async.AsyncDrawable;
import com.photour.async.BitmapRawTask;
import com.photour.async.BitmapTask;

public class TripElement implements Parcelable{

  @ColumnInfo(name = "visit_title")
  private String tripname;

  @ColumnInfo(name = "photoNo")
  private int photoNo;

  @ColumnInfo(name = "relative_path")
  private String displayPhoto;

  public TripElement(String tripname, int photoNo, String displayPhoto) {
    this.tripname = tripname;
    this.photoNo = photoNo;
    this.displayPhoto = displayPhoto;
  }

  /**
   * Constructor for allowing {@link Parcelable}
   *
   * @param in A {@link Parcel} object
   */
  protected TripElement(Parcel in) {
    this.tripname = in.readString();
    this.photoNo = in.readInt();
    this.displayPhoto = in.readString();
  }

  public static final Creator<TripElement> CREATOR = new Creator<TripElement>() {
    @Override
    public TripElement createFromParcel(Parcel in) {
      return new TripElement(in);
    }

    @Override
    public TripElement[] newArray(int size) {
      return new TripElement[size];
    }
  };

  public String getTripname() {
    return tripname;
  }

  public void setTripname(String tripname) {
    this.tripname = tripname;
  }

  public String getPhotoNo() {
    return "Number of Photos: "+photoNo;
  }

  public void setPhotoNo(int photoNo) {
    this.photoNo = photoNo;
  }

  public String getDisplayPhoto() {
    return displayPhoto;
  }

  public void setDisplayPhoto(String displayPhoto) {
    this.displayPhoto = displayPhoto;
  }

  /**
   * Function to load images for data binding
   *
   * @param imageView ImageView object
   * @param filepath filepath of image
   */
  @BindingAdapter({"tripBitmap"})
  public static void loadRawImage(ImageView imageView, String filepath) {
    final Context context = imageView.getContext();

    BitmapTask bitmapRawTask = new BitmapRawTask(imageView.getContext(), imageView);
    Bitmap placeholder;

    try {
      ExifInterface exifInterface = new ExifInterface(filepath);

      if (exifInterface.hasThumbnail()) {
        placeholder = exifInterface.getThumbnailBitmap();
      } else {
        placeholder = BitmapFactory
                .decodeResource(context.getResources(), R.drawable.placeholder);
      }

      final AsyncDrawable asyncDrawable =
              new AsyncDrawable(context.getResources(), placeholder, bitmapRawTask);

      imageView.setImageDrawable(asyncDrawable);
      bitmapRawTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filepath);
    } catch (Exception ignored) {
    }
  }

  /**
   * Describe the kinds of special objects contained in this Parcelable instance's marshaled
   * representation. For example, if the object will include a file descriptor in the output of
   * {@link #writeToParcel(Parcel, int)}, the return value of this method must include the {@link
   * #CONTENTS_FILE_DESCRIPTOR} bit.
   *
   * @return a bitmask indicating the set of special object types marshaled by this Parcelable
   * object instance.
   */
  @Override
  public int describeContents() { return 0; }

  /**
   * Flatten this object in to a Parcel.
   *
   * @param dest  The Parcel in which the object should be written.
   * @param flags Additional flags about how the object should be written. May be 0 or {@link
   *              #PARCELABLE_WRITE_RETURN_VALUE}.
   */
  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(tripname);
    dest.writeInt(photoNo);
    dest.writeString(displayPhoto);
  }
}
