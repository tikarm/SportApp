package com.tigran.projects.projectx.fragment.profile;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tigran.projects.projectx.R;
import com.tigran.projects.projectx.adapter.PhotosAdapter;
import com.tigran.projects.projectx.model.User;
import com.tigran.projects.projectx.model.UserViewModel;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.app.ActivityCompat.invalidateOptionsMenu;
import static com.tigran.projects.projectx.fragment.profile.OtherPhotosFragment.FRAGMENT_CODE;


public class PhotosFragment extends Fragment {

    private static final String TAG = "PhotosFragment";


    //view
    private RecyclerView mRecyclerView;
    private TextView textView;
    private LinearLayout layout;
    private ProgressBar mProgressBar;

    //adapter
    private PhotosAdapter mPhotosAdapter;

    private List<String> mPhotosList;
    private List<String> stateChecked;
    private User mCurrentUser;

    //viewmodel
    private UserViewModel mUserViewModel;

    //firebase
    private DatabaseReference mDatebaseReference;

    //constructor
    public PhotosFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photos, container, false);

        mUserViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        mUserViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                mCurrentUser = user;
                mPhotosList = mCurrentUser.getImages();
            }
        });
        mCurrentUser = mUserViewModel.getUser().getValue();
        mPhotosList = mCurrentUser.getImages();
        stateChecked = new ArrayList<>();


        initViews(view);

        return view;
    }


    private void initViews(View view) {
        mRecyclerView = view.findViewById(R.id.rv_photos);
        textView = view.findViewById(R.id.tv_text_adapter);
        layout = view.findViewById(R.id.layout_my_gallery);
        mProgressBar = view.findViewById(R.id.pb_photos_fragment);

        initRecyclerView();
    }

    private void initRecyclerView() {
        if (!mPhotosList.isEmpty()) {
            textView.setVisibility(View.GONE);
        }
        mPhotosAdapter = new PhotosAdapter();

        mRecyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 3));
        mRecyclerView.setAdapter(mPhotosAdapter);

        mPhotosAdapter.setOnRvItemClickListener(new PhotosAdapter.OnRvItemClickListener() {
            @Override
            public void onItemClicked(Drawable resource) {

                ImageGalleryFragment imageGalleryFragment = new ImageGalleryFragment();
                imageGalleryFragment.mPhotosList = mPhotosList;
                layout.setVisibility(View.VISIBLE);
                addFragment(imageGalleryFragment);

            }

            @Override
            public void onItemLongClicked(String item) {
                handleLongClick();
            }

            @Override
            public void onItemChecked(String item, boolean isChecked) {
                if (isChecked) {
                    stateChecked.add(item);
                    mActionMode.setTitle(++selectedItemsCount + " Chosen");

                } else {

                    if (multiSelectedMode) {
                        stateChecked.remove(item);
                    }
                    if (selectedItemsCount > 0)
                        mActionMode.setTitle(--selectedItemsCount + " Chosen");
                }

            }
        });


        mPhotosAdapter.addItems(mPhotosList);
    }

    public void handleLongClick() {
        multiSelectedMode = true;
        mActionMode = getActivity().startActionMode(modeCallBack);
        invalidateOptionsMenu(getActivity());
    }

    private boolean multiSelectedMode;
    private int selectedItemsCount;
    private boolean toRemove;

    private ActionMode mActionMode;

    private ActionMode.Callback modeCallBack = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(selectedItemsCount + " Chosen");
            mode.getMenuInflater().inflate(R.menu.menu_photos, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            selectedItemsCount = 0;
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_remove:
                    showInfoForRemove();
                    return true;
                default:
                    return false;

            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mPhotosAdapter.showAllCheckBoxes(false);
            if (!toRemove) {
                stateChecked.clear();
            }
            mPhotosAdapter.clearCheckBoxState();
            selectedItemsCount = 0;
            multiSelectedMode = false;
            mActionMode = null;
        }

    };

    private void showInfoForRemove() {
        AlertDialog alertDialog = new AlertDialog.Builder(this.getActivity())
                .setTitle("Delete Selected Images")
                .setPositiveButton("Yes", mOnRemoveClickListener)
                .setNegativeButton("No", mOnRemoveClickListener)
                .show();
    }

    private DialogInterface.OnClickListener mOnRemoveClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    removePhotosFromFirebase();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;

            }
        }
    };

    private void removePhotosFromFirebase() {
        toRemove = true;

        for (String s : stateChecked) {
            mCurrentUser.getImages().remove(s);
            mDatebaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(mCurrentUser.getId());
            mDatebaseReference.setValue(mCurrentUser);
        }

        mActionMode.finish();
        initRecyclerView();
        mPhotosAdapter.showAllCheckBoxes(false);

    }

    private void addFragment(Fragment fragment) {

        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.layout_my_gallery, fragment, FRAGMENT_CODE);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }


    }


}
