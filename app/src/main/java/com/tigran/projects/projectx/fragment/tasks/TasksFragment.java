package com.tigran.projects.projectx.fragment.tasks;


import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tigran.projects.projectx.R;
import com.tigran.projects.projectx.model.TaskViewModel;


public class TasksFragment extends DialogFragment {
    //public static vars
    public static final String LOOSE_WEIGHT_INFO = "Loose Weight Info";
    public static final String BUILD_MUSCLES_INFO = "Build Muscles Info";

    //views
    private Button mLooseWeightButton;
    private Button mBuildMusclesButton;

    //viewModel
    private TaskViewModel mTaskViewModel;

    //constructor
    public TasksFragment() {
    }

    //************************************ LIFECYCLE METHODS ****************************************
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_corners_dialog);

        initViews(view);
        mTaskViewModel = ViewModelProviders.of(getActivity()).get(TaskViewModel.class);
        mTaskViewModel.getDoneTask().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                checkTasksValidation();
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
        int integer = mTaskViewModel.getDoneTask().getValue();
        //3000(milliseconds in a second)*60(seconds in a minute)*1440(number of minutes in  24 hours)
        long hours24inMillis = 3000 * 60 * 1440;
        long timestampStartLooseWeight = 0;
        long timestampEndLooseWeight = System.currentTimeMillis() / 1000;
        long timestampStartBuildMuscles = 0;
        long timestampEndBuildMuscles = System.currentTimeMillis() / 1000;

        switch (integer) {
            case 1:
                if (mTaskViewModel.getLooseWeightTimestamp() != null && mTaskViewModel.getLooseWeightTimestamp().getValue() != null) {
                    timestampStartLooseWeight = mTaskViewModel.getLooseWeightTimestamp().getValue();
                }

                if (timestampStartLooseWeight - hours24inMillis >= timestampEndLooseWeight) {
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
                if (mTaskViewModel.getBuildMusclesTimestamp() != null && mTaskViewModel.getBuildMusclesTimestamp().getValue() != null) {
                    timestampStartBuildMuscles = mTaskViewModel.getBuildMusclesTimestamp().getValue();
                }
                if (timestampStartBuildMuscles - hours24inMillis >= timestampEndBuildMuscles) {
                    mBuildMusclesButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    mBuildMusclesButton.setAlpha(1f);
                    mBuildMusclesButton.setEnabled(true);
                } else {
                    mBuildMusclesButton.setBackgroundColor(Color.GRAY);
                    mBuildMusclesButton.setAlpha(.7f);
                    mBuildMusclesButton.setEnabled(false);
                }
                break;
            case 3:
                if (mTaskViewModel.getLooseWeightTimestamp() != null && mTaskViewModel.getLooseWeightTimestamp().getValue() != null) {
                    timestampStartLooseWeight = mTaskViewModel.getLooseWeightTimestamp().getValue();
                }
                if (timestampStartLooseWeight - hours24inMillis >= timestampEndLooseWeight) {
                    mLooseWeightButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    mLooseWeightButton.setAlpha(1f);
                    mLooseWeightButton.setEnabled(true);
                } else {
                    mLooseWeightButton.setBackgroundColor(Color.GRAY);
                    mLooseWeightButton.setAlpha(.7f);
                    mLooseWeightButton.setEnabled(false);
                }
                if (mTaskViewModel.getBuildMusclesTimestamp() != null && mTaskViewModel.getBuildMusclesTimestamp().getValue() != null) {
                    timestampStartBuildMuscles = mTaskViewModel.getBuildMusclesTimestamp().getValue();
                }
                if (timestampStartBuildMuscles - hours24inMillis >= timestampEndBuildMuscles) {
                    mBuildMusclesButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    mBuildMusclesButton.setAlpha(1f);
                    mBuildMusclesButton.setEnabled(true);
                } else {
                    mBuildMusclesButton.setBackgroundColor(Color.GRAY);
                    mBuildMusclesButton.setAlpha(.7f);
                    mBuildMusclesButton.setEnabled(false);
                }
                break;
        }
    }


}
