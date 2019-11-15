package com.android.photour.ui.photos;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.photour.ImageElement;
import com.android.photour.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PhotosFragment extends Fragment {

  private PhotosViewModel photosViewModel;
  private List<ImageElement> pictureList = new ArrayList<ImageElement>();
  private RecyclerView.Adapter photoAdapter;
  private RecyclerView mRecyclerView;
  private final int IMAGE_WIDTH = 100;



  public View onCreateView(
      @NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    photosViewModel = new ViewModelProvider(this).get(PhotosViewModel.class);
    View root = inflater.inflate(R.layout.fragment_photos, container, false);

    photosViewModel.images.observe(getViewLifecycleOwner(), imageElements -> {
      photoAdapter = new PhotoAdapter(imageElements, getContext());
      mRecyclerView.setAdapter(photoAdapter);
    });

    mRecyclerView = root.findViewById(R.id.grid_recycler_view);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


//    photoAdapter = new PhotoAdapter();
    mRecyclerView.setAdapter(photoAdapter);



    return root;
  }

}
