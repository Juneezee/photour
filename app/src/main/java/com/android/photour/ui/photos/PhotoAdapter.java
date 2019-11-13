package com.android.photour.ui.photos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.photour.ImageElement;
import com.android.photour.R;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.View_Holder> {

  private static List<ImageElement> items;

  PhotoAdapter(List<ImageElement> items) {
    PhotoAdapter.items = items;
  }

  class View_Holder extends RecyclerView.ViewHolder {
    private final ImageView imageView;

    View_Holder(View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_item);
    }
  }

  @NonNull
  @Override
  public View_Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    //Inflate the layout, initialize the View Holder
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_photos_sort,
        parent, false);
    //    context = parent.getContext();
    return new View_Holder(v);
  }

  @Override
  public void onBindViewHolder(@NonNull View_Holder holder, int position) {
    if (items.get(position) != null) {
      holder.mItem = mValues.get(position);
      holder.mImageView.setImageURI(mValues.get(position).uri);
      holder.mDateView.setText(mValues.get(position).date);

      Bitmap myBitmap = BitmapFactory.decodeFile(items.get(position).getFile().getAbsolutePath());
      holder.imageView.setImageBitmap(myBitmap);

      holder.imageView.setOnClickListener(view -> {
        // INSERT CODE TO ENTER IMAGE HERE
      });
    }
  }

  // convenience method for getting data at click position
  ImageElement getItem(int id) {
    return items.get(id);
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  class View_Holder extends RecyclerView.ViewHolder {

    public final RecyclerView sortedRecyclerView;
    public final TextView sortedTitleView;


    public View_Holder(@NonNull View itemView) {
      super(itemView);
      sortedTitleView = itemView.findViewById(R.id.sortedTitleView);
      sortedRecyclerView = itemView.findViewById(R.id.sortedRecyclerView);
    }


  }

}
