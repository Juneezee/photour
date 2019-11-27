package com.photour.ui.visits;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.photour.databinding.ItemVisitBinding;
import com.photour.model.Visit;
import com.photour.ui.visits.VisitAdapter.VisitCard;
import java.util.ArrayList;
import java.util.List;

public class VisitAdapter extends RecyclerView.Adapter<VisitCard> {

  private static List<Visit> items = new ArrayList<>();

  /**
   * Setter for items
   *
   * @param items List of Uri for the adapter
   */
  void setItems(List<Visit> items) {
    VisitAdapter.items = items;
  }

  @NonNull
  @Override
  public VisitCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemVisitBinding itemVisitBinding = ItemVisitBinding
        .inflate(LayoutInflater.from(parent.getContext()), parent, false);

    return new VisitCard(itemVisitBinding);
  }

  @Override
  public void onBindViewHolder(@NonNull VisitCard holder, int position) {
    Visit visit = items.get(position);
    holder.itemVisitBinding.setVisit(visit);
    holder.itemVisitBinding.executePendingBindings();
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  class VisitCard extends RecyclerView.ViewHolder {

    final ItemVisitBinding itemVisitBinding;

    VisitCard(@NonNull ItemVisitBinding itemVisitBinding) {
      super(itemVisitBinding.getRoot());
      this.itemVisitBinding = itemVisitBinding;
    }
  }
}
