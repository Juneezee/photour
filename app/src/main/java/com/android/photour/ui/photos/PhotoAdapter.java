package com.android.photour.ui.photos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.photour.ImageElement;
import com.android.photour.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.SortCard> {

  private static List<ImageElement> items = new ArrayList<>();
  private Context context;
  private final int IMAGE_WIDTH = 150;

  PhotoAdapter(Context context) {
    this.context = context;
  }

  public void setItems(List<ImageElement> items) {
      this.items = items;
  }


  @NonNull
  @Override
  public SortCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_photos_sort,
            parent, false);
    return new SortCard(v);
  }

  @Override
  public void onBindViewHolder(@NonNull SortCard holder, int position) {
    if (items.get(position) != null) {

        holder.imageElement = items.get(position);
        holder.sortedTitleView.setText(items.get(position).getTitle());

        holder.sortedRecyclerView.setAdapter(new ImageAdapter(items.get(position).getUris(), context));
        holder.sortedRecyclerView.setLayoutManager(new GridLayoutManager(
                context,
                PhotosViewModel.calculateNoOfColumns(Objects.requireNonNull(context), IMAGE_WIDTH)));
    }

//      holder.imageView.setOnClickListener(view -> {
//        // INSERT CODE TO ENTER IMAGE HERE
//      });
//    }
  }

  // convenience method for getting data at click position
  ImageElement getItem(int id) {
    return items.get(id);
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  class SortCard extends RecyclerView.ViewHolder {

    public final View sortedView;
    public final ImageView imageView;
    public final RecyclerView sortedRecyclerView;
    public final TextView sortedTitleView;
    public ImageElement imageElement;


    public SortCard(@NonNull View itemView) {
      super(itemView);
      sortedView = itemView;
      imageView = itemView.findViewById(R.id.sorted_image_view);
      sortedTitleView = itemView.findViewById(R.id.sorted_title_view);
      sortedRecyclerView = itemView.findViewById(R.id.sorted_recycler_view);
    }
  }

}
