package com.android.photour.ui.photos;

import android.os.Bundle;
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
  private final int IMAGE_WIDTH = 100;


  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initDataset();
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    photosViewModel = ViewModelProviders.of(this).get(PhotosViewModel.class);
    View root = inflater.inflate(R.layout.fragment_photos, container, false);

    mRecyclerView = root.findViewById(R.id.grid_recycler_view);
    mRecyclerView.setLayoutManager(new GridLayoutManager(
        getActivity(),
        photosViewModel.calculateNoOfColumns(Objects.requireNonNull(getActivity()), IMAGE_WIDTH))
    );

    photoAdapter = new PhotoAdapter(pictureList);
    mRecyclerView.setAdapter(photoAdapter);

    return root;
  }

  private void initDataset() {
    for (int i = 0; i < 100; i++) {
      pictureList.add(new ImageElement(R.drawable.yui));
    }
  }
}
