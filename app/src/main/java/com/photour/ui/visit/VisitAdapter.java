package com.photour.ui.visit;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.photour.databinding.ItemPagerBinding;
import com.photour.helper.FileHelper;
import com.photour.model.Photo;
import com.photour.ui.photo.PhotoZoomFragment;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for photos on {@link VisitFragment}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class VisitAdapter extends RecyclerView.Adapter<VisitAdapter.ScrollImage> {

  private List<Photo> items = new ArrayList<>();
  private ItemPagerBinding itemPagerBinding;

  /**
   * Setter for items
   *
   * @param items List of Uri for the adapter
   */
  void setItems(List<Photo> items) {
    this.items = items;
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
  public ScrollImage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    itemPagerBinding = ItemPagerBinding
        .inflate(LayoutInflater.from(parent.getContext()), parent, false);

    return new VisitAdapter.ScrollImage(itemPagerBinding);
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
  public void onBindViewHolder(@NonNull ScrollImage holder, int position) {
    Photo photo = items.get(position);
    holder.itemPagerBinding.setAdapter(this);
    holder.itemPagerBinding.setPhotoPager(photo);
    holder.itemPagerBinding.executePendingBindings();
  }

  /**
   * Navigate to {@link PhotoZoomFragment} to show the image in full screen and allow zoom
   *
   * @param photo The {@link Photo} object
   */
  public void zoomPhoto(Photo photo) {
    if (FileHelper.fileExist(photo.filePath())) {
      Navigation.findNavController(itemPagerBinding.getRoot())
          .navigate(VisitFragmentDirections.actionZoomPhoto(photo));
    }
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
  class ScrollImage extends RecyclerView.ViewHolder {

    final ItemPagerBinding itemPagerBinding;

    ScrollImage(@NonNull ItemPagerBinding itemPagerBinding) {
      super(itemPagerBinding.getRoot());
      this.itemPagerBinding = itemPagerBinding;
    }
  }
}
