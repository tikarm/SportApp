package com.tigran.projects.projectx.fragment.tasks;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.graphics.BlurMaskFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tigran.projects.projectx.R;
import com.tigran.projects.projectx.model.BuildMusclesViewModel;
import com.tigran.projects.projectx.model.TodaysTaskInfo;
import com.tigran.projects.projectx.model.User;
import com.tigran.projects.projectx.model.UserViewModel;
import com.tigran.projects.projectx.preferences.SaveSharedPreferences;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import pl.droidsonroids.gif.GifImageView;


public class BuildMusclesFragment extends Fragment {

    //static vars
    public static final String PUSH_UPS = "Push-ups";
    public static final String PULL_UPS = "Pull-ups";
    public static final String SQUATS = "Squats";
    public static final String SIT_UPS = "Sit-ups";
    public static final String[] EXERCISE_NAMES = {PUSH_UPS, PULL_UPS, SQUATS, SIT_UPS};

    //views
    private Toolbar mToolbarBuildMuscles;
    private GifImageView[] mGifImageViews = new GifImageView[4];
    private Button[] mExerciseButtons = new Button[4];
    private TextView[] mExerciseViews = new TextView[4];
    private ConstraintLayout[] mLockLayouts = new ConstraintLayout[4];
    private BottomNavigationView mBottomNavigationView;

    //preferences
    SaveSharedPreferences sharedPreferences = new SaveSharedPreferences();

    //drawable
    private int mGifImages[] = new int[4];

    //navigation
    private NavController mNavController;

    //view model
    private BuildMusclesViewModel mBuildMusclesViewModel;

    //firebase
    DatabaseReference mFirebaseDatabaseUser;

    //user
    private User mCurrentUser;
    private UserViewModel mUserViewModel;


    //constructor
    public BuildMusclesFragment() {
    }

    //************************************ LIFECYCLE METHODS ****************************************
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_build_muscles, container, false);

        initViews(view);
        setNavigationComponent();
        hideBotNavBar();

        for (int i = 1; i < mExerciseViews.length; i++) {
            blurTextView(mExerciseViews[i]);
        }

        for (int i = 0; i < mExerciseButtons.length; i++) {
            openExerciseDialog(mExerciseButtons[i], EXERCISE_NAMES[i]);
        }


        mBuildMusclesViewModel = ViewModelProviders.of(getActivity()).get(BuildMusclesViewModel.class);
        mUserViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        mUserViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                mCurrentUser = user;
            }
        });
        mCurrentUser = mUserViewModel.getUser().getValue();


        if (mBuildMusclesViewModel.getUnlockLevel().getValue() == null || mBuildMusclesViewModel.getUnlockLevel().getValue() == 0) {
//            mBuildMusclesViewModel.setUnlockLevel(0);
            Log.e("hhhh", "pref  " + sharedPreferences.getBuildMusclesUnlockLevel(getContext()));

            mBuildMusclesViewModel.setUnlockLevel(sharedPreferences.getBuildMusclesUnlockLevel(getContext()) - 1);
            setBuildMusclesTaskUnlockLevelForCurrentUserAndUpdateInFirebase(sharedPreferences.getBuildMusclesUnlockLevel(getContext()) - 1);
        }


        if (mBuildMusclesViewModel.getUnlockLevel().getValue() != null) {
            for (int i = 1; i <= mBuildMusclesViewModel.getUnlockLevel().getValue(); i++) {
                mExerciseButtons[i].setEnabled(true);
                mExerciseButtons[i - 1].setEnabled(false);
                mGifImageViews[i].setImageResource(mGifImages[i]);
                mLockLayouts[i].setVisibility(View.INVISIBLE);
                clearBlurFromTextView(mExerciseViews[i]);

            }
        }

        mBuildMusclesViewModel.getUnlockLevel().observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long lvl) {
                if (mBuildMusclesViewModel.getUnlockLevel().getValue() != null) {
                    for (int i = 1; i <= lvl; i++) {
                        mExerciseButtons[i].setEnabled(true);
                        mExerciseButtons[i - 1].setEnabled(false);
                        mGifImageViews[i].setImageResource(mGifImages[i]);
                        mLockLayouts[i].setVisibility(View.INVISIBLE);
                        clearBlurFromTextView(mExerciseViews[i]);
                    }
                }
            }
        });

        return view;
    }


    //************************************** METHODS ********************************************
    private void initViews(View view) {
        mToolbarBuildMuscles = view.findViewById(R.id.toolbar_build_muscles);
        mExerciseButtons[0] = view.findViewById(R.id.btn_push_ups_build_muscles);
        mExerciseButtons[1] = view.findViewById(R.id.btn_pull_ups_build_muscles);
        mExerciseButtons[2] = view.findViewById(R.id.btn_squats_build_muscles);
        mExerciseButtons[3] = view.findViewById(R.id.btn_sit_ups_build_muscles);
        mGifImageViews[1] = view.findViewById(R.id.giv_pull_ups_build_muscles);
        mGifImageViews[2] = view.findViewById(R.id.giv_squats_build_muscles);
        mGifImageViews[3] = view.findViewById(R.id.giv_sit_ups_build_muscles);
        mExerciseViews[1] = view.findViewById(R.id.tv_pull_ups_build_muscles);
        mExerciseViews[2] = view.findViewById(R.id.tv_squats_ups_build_muscles);
        mExerciseViews[3] = view.findViewById(R.id.tv_sit_ups_build_muscles);
        mLockLayouts[1] = view.findViewById(R.id.layout_lock_pull_ups_build_muscles);
        mLockLayouts[2] = view.findViewById(R.id.layout_lock_squats_build_muscles);
        mLockLayouts[3] = view.findViewById(R.id.layout_lock_sit_ups_build_muscles);
        mGifImages[1] = R.drawable.pull_ups_gif;
        mGifImages[2] = R.drawable.squats_gif;
        mGifImages[3] = R.drawable.sit_ups_gif;
        mBottomNavigationView = getActivity().findViewById(R.id.bottom_navigation_view_main);
    }

    private void openExerciseDialog(Button b, String s) {
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                BuildMusclesDialogFragment buildMusclesDialogFragment = new BuildMusclesDialogFragment();
                mBuildMusclesViewModel.setBuildMuscles(s);
                setBuildMusclesTaskForCurrentUserAndUpdateInFirebase(s);
                buildMusclesDialogFragment.show(fm, null);
            }
        });
    }

    private void blurTextView(TextView textView) {
        textView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        float radius = textView.getTextSize() / 3;
        BlurMaskFilter filter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL);
        textView.getPaint().setMaskFilter(filter);
    }

    private void clearBlurFromTextView(TextView textView) {
        textView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        textView.getPaint().setMaskFilter(null);
    }

    private void setNavigationComponent() {
        mNavController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(mToolbarBuildMuscles, mNavController);
    }

    private void hideBotNavBar() {
        mBottomNavigationView.setVisibility(View.GONE);
    }

    private void updateUserInFirebase() {
        mFirebaseDatabaseUser = FirebaseDatabase.getInstance().getReference("users");
        mFirebaseDatabaseUser.child(mCurrentUser.getId()).setValue(mCurrentUser);
    }

    private void setBuildMusclesTaskForCurrentUserAndUpdateInFirebase(String taskName) {
        TodaysTaskInfo todaysTaskInfo;
        if (mCurrentUser.getTodaysTaskInfo() == null) {
            todaysTaskInfo = new TodaysTaskInfo();
        } else {
            todaysTaskInfo = mCurrentUser.getTodaysTaskInfo();
        }
        todaysTaskInfo.setBuildMusclesTaskName(taskName);
        mCurrentUser.setTodaysTaskInfo(todaysTaskInfo);

        updateUserInFirebase();
    }

    private void setBuildMusclesTaskUnlockLevelForCurrentUserAndUpdateInFirebase(long level) {
        TodaysTaskInfo todaysTaskInfo;
        if (mCurrentUser.getTodaysTaskInfo() == null) {
            todaysTaskInfo = new TodaysTaskInfo();
        } else {
            todaysTaskInfo = mCurrentUser.getTodaysTaskInfo();
        }
        todaysTaskInfo.setBuildMusclesUnlockLevel(level);
        mCurrentUser.setTodaysTaskInfo(todaysTaskInfo);

        updateUserInFirebase();
    }

}
