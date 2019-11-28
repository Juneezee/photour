package com.photour.ui.viewvisit;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.photour.databinding.ItemPagerBinding;
import com.photour.model.ImageElement;

import java.util.ArrayList;
import java.util.List;

public class ViewVisitAdapter extends RecyclerView.Adapter<ViewVisitAdapter.ScrollImage> {

  private static List<ImageElement> items = new ArrayList<>();

  /**
   * Setter for items
   *
   * @param items List of Uri for the adapter
   */
  void setItems(List<ImageElement> items) {
    ViewVisitAdapter.items = items;
  }

  @NonNull
  @Override
  public ScrollImage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemPagerBinding itemPagerBinding = ItemPagerBinding
            .inflate(LayoutInflater.from(parent.getContext()), parent, false);

    return new ViewVisitAdapter.ScrollImage(itemPagerBinding);
  }

  @Override
  public void onBindViewHolder(@NonNull ScrollImage holder, int position) {
    ImageElement imageElement = items.get(position);
    holder.itemPagerBinding.setImagePager(imageElement);
    holder.itemPagerBinding.executePendingBindings();
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  class ScrollImage extends  RecyclerView.ViewHolder {

    final ItemPagerBinding itemPagerBinding;

    ScrollImage(@NonNull ItemPagerBinding itemPagerBinding) {
      super(itemPagerBinding.getRoot());
      this.itemPagerBinding = itemPagerBinding;
    }
  }
}
