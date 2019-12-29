package com.tigran.projects.projectx.fragment.profile;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tigran.projects.projectx.R;
import com.tigran.projects.projectx.adapter.ViewPagerAdapter;

import java.util.List;

import static com.tigran.projects.projectx.fragment.profile.OtherPhotosFragment.FRAGMENT_CODE;

public class ImageGalleryFragment extends Fragment
{

    private static final String TAG = "ImageGalleryFragment";

    private ViewPagerAdapter mViewPagerAdapter;
    private ViewPager mViewPager;
    private Button mCloseButton;

    public  List<String> mPhotosList;

    public ImageGalleryFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_gallery, container, false);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mCloseButton = view.findViewById(R.id.iv_close_photo_gallery);
        mViewPagerAdapter = new ViewPagerAdapter(getContext(),mPhotosList);
        mViewPager = view.findViewById(R.id.vp_gallery);
        mViewPager.setAdapter(mViewPagerAdapter);

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                ImageGalleryFragment myFragment = (ImageGalleryFragment)getFragmentManager().findFragmentByTag(FRAGMENT_CODE);
                fragmentTransaction.remove(myFragment);
                fragmentTransaction.commit();
            }
        });

    }
}
