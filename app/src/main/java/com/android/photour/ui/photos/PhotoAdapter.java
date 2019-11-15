package com.android.photour.ui.photos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.View_Holder> {

  private static List<ImageElement> items;
  private Context context;

  PhotoAdapter(List<ImageElement> items, Context context) {
    PhotoAdapter.items = items;
    this.context = context;
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
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_photos_sort,
        parent, false);
    return new View_Holder(v);
  }

  @Override
  public void onBindViewHolder(@NonNull View_Holder holder, int position) {
    if (items.get(position) != null) {
      holder.imageElement = items.get(position);
      holder.sortedTitleView.setText(items.get(position).getTitle());
      List<Uri> uriList = items.get(position).getUris();
      System.out.println(uriList);
//       loop images
      try {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uriList.get(0));
        holder.sortedRecyclerView.setImageBitmap(bitmap);
      } catch (IOException e) {
        e.printStackTrace();
      }
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

  class View_Holder extends RecyclerView.ViewHolder {

    public final View sortedView;
    public final ImageView imageView;
    public final RecyclerView sortedRecyclerView;
    public final TextView sortedTitleView;
    public ImageElement imageElement;


    public View_Holder(@NonNull View itemView) {
      super(itemView);
      sortedView = itemView;
      imageView = itemView.findViewById(R.id.sorted_image_view);
      sortedTitleView = itemView.findViewById(R.id.sorted_title_view);
      sortedRecyclerView = itemView.findViewById(R.id.sorted_recycler_view);
    }


  }

}
