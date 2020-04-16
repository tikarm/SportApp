package com.tigran.projects.projectx.fragment.profile;


import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tigran.projects.projectx.R;
import com.tigran.projects.projectx.adapter.PhotosAdapter;
import com.tigran.projects.projectx.model.User;
import com.tigran.projects.projectx.model.UserViewModel;

import java.util.List;

public class OtherPhotosFragment extends Fragment {

    private static final String TAG = "OtherPhotosFragment";
    public static final String FRAGMENT_CODE = "Gallery";


    //view
    private RecyclerView mRecyclerView;
    private TextView textView;
    private LinearLayout layout;
    private BottomNavigationView mBottomNavigationView;
    private ConstraintLayout mLayout;

    //adapter
    private PhotosAdapter mPhotosAdapter;


    //
    private List<String> mPhotosList;
    private User mOtherUser;

    //viewmodel
    private UserViewModel mUserViewModel;


    //constructor
    public OtherPhotosFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other_photos, container, false);

        mUserViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        mUserViewModel.getOtherUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                mOtherUser = user;
                mPhotosList = mOtherUser.getImages();
            }
        });
        mOtherUser = mUserViewModel.getOtherUser().getValue();
        mPhotosList = mOtherUser.getImages();


        initViews(view);
        hideBotNavBar();

        return view;
    }


    private void initViews(View view) {
        mRecyclerView = view.findViewById(R.id.rv_other_photos);
        textView = view.findViewById(R.id.tv_other_text_adapter);
        layout = view.findViewById(R.id.layout_gallery);
        mLayout = view.findViewById(R.id.cl_other_photos_fragment);
        mBottomNavigationView = getActivity().findViewById(R.id.bottom_navigation_view_main);

        initRecyclerView();
    }

    private void initRecyclerView() {
        if (!mPhotosList.isEmpty()) {
            textView.setVisibility(View.GONE);
        }
        mPhotosAdapter = new PhotosAdapter();

        mPhotosAdapter.setOnRvItemClickListener(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 3));
        mRecyclerView.setAdapter(mPhotosAdapter);

        mPhotosAdapter.setOnOtherRvItemClickListener(new PhotosAdapter.OnOtherRvItemClickListener() {
            @Override
            public void onItemClicked(Drawable resource, String url) {

                ImageGalleryFragment imageGalleryFragment = new ImageGalleryFragment(url);
                imageGalleryFragment.mPhotosList = mPhotosList;
                setClickListenerForImageGalleryFragment(imageGalleryFragment);
                layout.setVisibility(View.VISIBLE);
                mLayout.setForeground(getResources().getDrawable(R.drawable.shape_window_dim));
                addFragment(imageGalleryFragment);

            }
        });

        mPhotosAdapter.addItems(mPhotosList);
    }

    private void setClickListenerForImageGalleryFragment(ImageGalleryFragment imageGalleryFragment) {
        imageGalleryFragment.setClickListener(new ImageGalleryFragment.clickListener() {
            @Override
            public void onCloseClicked() {
                removeForeground();
            }
        });

    }

    public void removeForeground() {
        mLayout.setForeground(null);
    }

    private void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.layout_gallery, fragment, FRAGMENT_CODE);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void hideBotNavBar() {
        mBottomNavigationView.setVisibility(View.GONE);
    }
}
