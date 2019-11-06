package com.android.photour.ui.photos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.photour.ImageElement;
import com.android.photour.R;
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
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_image,
        parent, false);
    //    context = parent.getContext();
    return new View_Holder(v);
  }

  @Override
  public void onBindViewHolder(@NonNull View_Holder holder, int position) {
    System.out.println(position);
    if (items.get(position) != null) {
      if (items.get(position).getImage() != -1) {
        holder.imageView.setImageResource(items.get(position).getImage());
      } else if (items.get(position).getFile() != null) {
        Bitmap myBitmap = BitmapFactory.decodeFile(items.get(position).getFile().getAbsolutePath());
        holder.imageView.setImageBitmap(myBitmap);
      }
//            holder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(context, ShowImageActivity.class);
//                    intent.putExtra("position", position);
//                    context.startActivity(intent);
//                }
//            });
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

}
