package com.photour.ui.paths;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.photour.databinding.ItemPathBinding;
import com.photour.model.TripElement;
import java.util.ArrayList;
import java.util.List;

public class PathAdapter extends RecyclerView.Adapter<PathAdapter.TripCard> {

  private static List<TripElement> items = new ArrayList<>();

  /**
   * Setter for items
   *
   * @param items List of Uri for the adapter
   */
  void setItems(List<TripElement> items) {
    PathAdapter.items = items;
  }

  @NonNull
  @Override
  public TripCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemPathBinding itemPathBinding = ItemPathBinding
            .inflate(LayoutInflater.from(parent.getContext()), parent, false);

    return new TripCard(itemPathBinding);
  }

  @Override
  public void onBindViewHolder(@NonNull TripCard holder, int position) {
    TripElement tripElement = items.get(position);
    holder.itemPathBinding.setTrip(tripElement);
    holder.itemPathBinding.executePendingBindings();
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  class TripCard extends RecyclerView.ViewHolder {

    final ItemPathBinding itemPathBinding;

    TripCard(@NonNull ItemPathBinding itemPathBinding) {
        super(itemPathBinding.getRoot());
        this.itemPathBinding = itemPathBinding;
    }
  }
}
