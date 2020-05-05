package com.tigran.projects.projectx.fragment.tasks;


import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tigran.projects.projectx.R;
import com.tigran.projects.projectx.model.BuildMusclesViewModel;
import com.tigran.projects.projectx.model.TaskViewModel;
import com.tigran.projects.projectx.model.User;
import com.tigran.projects.projectx.model.UserViewModel;
import com.tigran.projects.projectx.preferences.SaveSharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.tigran.projects.projectx.fragment.tasks.BuildMusclesFragment.PUSH_UPS;


public class TasksFragment extends DialogFragment {
    //public static vars
    public static final String LOOSE_WEIGHT_INFO = "Loose Weight Info";
    public static final String BUILD_MUSCLES_INFO = "Build Muscles Info";

    //views
    private Button mLooseWeightButton;
    private Button mBuildMusclesButton;

    //viewModel
    private TaskViewModel mTaskViewModel;
    private BuildMusclesViewModel mBuildMusclesViewModel;

    //user
    private User mCurrentUser;
    private UserViewModel mUserViewModel;

    //prefs
    private SaveSharedPreferences sharedPreferences = new SaveSharedPreferences();

    //firebase
    DatabaseReference mFirebaseDatabaseUser;


    //constructor
    public TasksFragment() {
    }

    //************************************ LIFECYCLE METHODS ****************************************
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_corners_dialog);

        initViews(view);
        mBuildMusclesViewModel = ViewModelProviders.of(getActivity()).get(BuildMusclesViewModel.class);
        mUserViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        mCurrentUser = mUserViewModel.getUser().getValue();
        mTaskViewModel = ViewModelProviders.of(getActivity()).get(TaskViewModel.class);
        mTaskViewModel.getDoneTask().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
//                checkTasksValidation();
            }
        });
        checkTasksValidation();


        FragmentManager fm = getActivity().getSupportFragmentManager();
        TaskInfoFragment taskInfoFragment = new TaskInfoFragment();


        mLooseWeightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTaskViewModel.setTask(LOOSE_WEIGHT_INFO);
                getDialog().dismiss();
                taskInfoFragment.show(fm, null);
            }
        });


        mBuildMusclesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTaskViewModel.setTask(BUILD_MUSCLES_INFO);
                getDialog().dismiss();
                taskInfoFragment.show(fm, null);
            }
        });

        return view;
    }

    //************************************** METHODS ********************************************
    private void initViews(View view) {
        mLooseWeightButton = view.findViewById(R.id.btn_loose_weight_tasks);
        mBuildMusclesButton = view.findViewById(R.id.btn_build_muscles_tasks);
    }

    private void checkTasksValidation() {
        int integer = sharedPreferences.getDoneTask(getContext());

        //3000(milliseconds in a second)*60(seconds in a minute)*1440(number of minutes in  24 hours)
//        long hours24inMillis = 3000 * 60 * 1440;
        long hours24inMillis = 86400000;
        long timestampStartLooseWeight = 0;
        long timestampEndLooseWeight = System.currentTimeMillis() / 1000;
        long timestampStartBuildMuscles = 0;
        long timestampEndBuildMuscles = System.currentTimeMillis() / 1000;
        Date dateStartLooseWeight;
        Date dateEndLooseWeight;
        Date dateStartBuildMuscles;
        Date dateEndBuildMuscles;

        switch (integer) {
            case 1:
                if (sharedPreferences.getBuildMusclesTimestamp(getContext()) != null) {
                    timestampStartLooseWeight = sharedPreferences.getLooseWeightTimestamp(getContext());
                }

                dateStartLooseWeight = new Date(timestampStartLooseWeight * 1000);
                dateEndLooseWeight = new Date(timestampEndLooseWeight * 1000);

                if (Math.abs(dateEndLooseWeight.getTime() - dateStartLooseWeight.getTime()) > hours24inMillis) {
                    mLooseWeightButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    mLooseWeightButton.setAlpha(1f);
                    mLooseWeightButton.setEnabled(true);
                } else {
                    mLooseWeightButton.setBackgroundColor(Color.GRAY);
                    mLooseWeightButton.setAlpha(.7f);
                    mLooseWeightButton.setEnabled(false);
                }

                break;
            case 2:
                if (sharedPreferences.getBuildMusclesTimestamp(getContext()) != null) {
                    timestampStartBuildMuscles = sharedPreferences.getBuildMusclesTimestamp(getContext());
                }

                dateStartBuildMuscles = new Date(timestampStartBuildMuscles * 1000);
                dateEndBuildMuscles = new Date(timestampEndBuildMuscles * 1000);

                if (Math.abs(dateEndBuildMuscles.getTime() - dateStartBuildMuscles.getTime()) > hours24inMillis) {
                    mBuildMusclesButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    mBuildMusclesButton.setAlpha(1f);
                    mBuildMusclesButton.setEnabled(true);

                    if (sharedPreferences.getBuildMusclesUnlockLevel(getContext()) == 3) {
                        sharedPreferences.setBuildMusclesUnlockLevel(getContext(), 0);
                        mBuildMusclesViewModel.setBuildMuscles(PUSH_UPS);
                        mBuildMusclesViewModel.setUnlockLevel(0l);
                    }
                } else {
                    mBuildMusclesButton.setBackgroundColor(Color.GRAY);
                    mBuildMusclesButton.setAlpha(.7f);
                    mBuildMusclesButton.setEnabled(false);
                }

                break;
            case 3:
                int doneTaskStatus = 3;
                boolean isLooseWeightValid = false;
                boolean isBuildMusclesValid = false;
                if (sharedPreferences.getLooseWeightTimestamp(getContext()) != null) {
                    timestampStartLooseWeight = sharedPreferences.getLooseWeightTimestamp(getContext());
                }

                if (sharedPreferences.getBuildMusclesTimestamp(getContext()) != null) {
                    timestampStartBuildMuscles = sharedPreferences.getBuildMusclesTimestamp(getContext());
                }

                dateStartBuildMuscles = new Date(timestampStartBuildMuscles * 1000);
                dateEndBuildMuscles = new Date(timestampEndBuildMuscles * 1000);

                if (Math.abs(dateEndBuildMuscles.getTime() - dateStartBuildMuscles.getTime()) > hours24inMillis) {
                    mBuildMusclesButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    mBuildMusclesButton.setAlpha(1f);
                    mBuildMusclesButton.setEnabled(true);

                    if (sharedPreferences.getBuildMusclesUnlockLevel(getContext()) == 3) {
                        sharedPreferences.setBuildMusclesUnlockLevel(getContext(), 0);
                        mBuildMusclesViewModel.setBuildMuscles(PUSH_UPS);
                        mBuildMusclesViewModel.setUnlockLevel(0l);
                    }
                    isBuildMusclesValid = true;

                } else {
                    mBuildMusclesButton.setBackgroundColor(Color.GRAY);
                    mBuildMusclesButton.setAlpha(.7f);
                    mBuildMusclesButton.setEnabled(false);
                }

                dateStartLooseWeight = new Date(timestampStartLooseWeight * 1000);
                dateEndLooseWeight = new Date(timestampEndLooseWeight * 1000);

                if (Math.abs(dateEndLooseWeight.getTime() - dateStartLooseWeight.getTime()) > hours24inMillis) {
                    mLooseWeightButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    mLooseWeightButton.setAlpha(1f);
                    mLooseWeightButton.setEnabled(true);

                    isLooseWeightValid = true;
                } else {
                    mLooseWeightButton.setBackgroundColor(Color.GRAY);
                    mLooseWeightButton.setAlpha(.7f);
                    mLooseWeightButton.setEnabled(false);
                }

                if (isLooseWeightValid && isBuildMusclesValid) {
                    doneTaskStatus = 0;
                } else if (isBuildMusclesValid) {
                    doneTaskStatus = 1;
                } else if (isLooseWeightValid) {
                    doneTaskStatus = 2;
                } else {
                    doneTaskStatus = 3;
                }

                if (doneTaskStatus == 3) {
                    Toast.makeText(getContext(), "You have done all today's tasks. Come back tomorrow!", Toast.LENGTH_LONG).show();
                }

                mCurrentUser.getTodaysTaskInfo().setDoneTasksStatus(doneTaskStatus);
                mCurrentUser.getTodaysTaskInfo().setBuildMusclesUnlockLevel(sharedPreferences.getBuildMusclesUnlockLevel(getContext()));
                mCurrentUser.getTodaysTaskInfo().setBuildMusclesTaskName(mBuildMusclesViewModel.getBuildMuscles().getValue());
                updateUserInFirebase();
                sharedPreferences.setDoneTask(getContext(), doneTaskStatus);

                break;
        }
    }

    private void updateUserInFirebase() {
        mFirebaseDatabaseUser = FirebaseDatabase.getInstance().getReference("users");
        mFirebaseDatabaseUser.child(mCurrentUser.getId()).setValue(mCurrentUser);
    }
}
