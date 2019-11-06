package com.android.photour.ui.photos;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.photour.ImageElement;
import com.android.photour.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PhotosFragment extends Fragment {

  private PhotosViewModel photosViewModel;
  private List<ImageElement> pictureList = new ArrayList<>();
  private RecyclerView.Adapter photoAdapter;
  private RecyclerView mRecyclerView;
  private int IMAGE_WIDTH = 100;

  /**
   * helper function to recalculate number of columns
   *
   * @param context context of application
   * @param columnWidthDp size of column
   * @return number of columns
   */
  private static int calculateNoOfColumns(Context context, float columnWidthDp) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
    return (int) (screenWidthDp / columnWidthDp + 0.5);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    for (int i = 0; i < 100; i++) {
      pictureList.add(new ImageElement(R.drawable.yui));
    }
  }

  public View onCreateView(@NonNull LayoutInflater inflater,
      ViewGroup container, Bundle savedInstanceState) {
    photosViewModel =
        ViewModelProviders.of(this).get(PhotosViewModel.class);
    View root = inflater.inflate(R.layout.fragment_photos, container, false);

    mRecyclerView = root.findViewById(R.id.grid_recycler_view);

    mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
        calculateNoOfColumns(Objects.requireNonNull(getActivity()), IMAGE_WIDTH)));
    photoAdapter = new PhotoAdapter(pictureList);
    mRecyclerView.setAdapter(photoAdapter);

    return root;
  }
}
