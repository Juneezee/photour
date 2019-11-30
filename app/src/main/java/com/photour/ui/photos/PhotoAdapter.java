package com.photour.ui.photos;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.photour.databinding.ItemPhotoBinding;
import com.photour.model.Photo;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for images on {@link PhotosFragment}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ImageCard> {

  private static List<Photo> items = new ArrayList<>();

  /**
   * Accessor for items
   *
   * @param items List of Uri for the adapter
   */
  public void setItems(List<Photo> items) {
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
    ItemPhotoBinding itemPhotoBinding = ItemPhotoBinding
        .inflate(LayoutInflater.from(parent.getContext()), parent, false);

    return new ImageCard(itemPhotoBinding);
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
    Photo photo = items.get(position);
    holder.itemPhotoBinding.setPhoto(photo);
    holder.itemPhotoBinding.executePendingBindings();
  }

  /**
   * Getter for data set size
   *
   * @return int Size of the data set
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

    final ItemPhotoBinding itemPhotoBinding;

    ImageCard(@NonNull ItemPhotoBinding itemPhotoBinding) {
      super(itemPhotoBinding.getRoot());
      this.itemPhotoBinding = itemPhotoBinding;
    }
  }
}
