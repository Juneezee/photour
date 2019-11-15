package com.android.photour.ui.photos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.photour.R;

import java.util.Objects;

public class PhotosSortFragment extends Fragment {

    private final int IMAGE_WIDTH = 100;
    private PhotosViewModel photosViewModel;
    private RecyclerView.Adapter photoAdapter;
    private RecyclerView sortedRecyclerView;

    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        photosViewModel = new ViewModelProvider(this).get(PhotosViewModel.class);
        android.view.View root = inflater.inflate(R.layout.fragment_photos, container, false);

        sortedRecyclerView = root.findViewById(R.id.sorted_recycler_view);
        sortedRecyclerView.setLayoutManager(new GridLayoutManager(
                getActivity(),
                photosViewModel.calculateNoOfColumns(Objects.requireNonNull(getActivity()), IMAGE_WIDTH))
        );


//        photoAdapter = new PhotoAdapter();
//        sortedRecyclerView.setAdapter(photoAdapter);


        return root;
    }

}
