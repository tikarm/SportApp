package com.tigran.projects.projectx.fragment.tasks;


import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.tigran.projects.projectx.R;
import com.tigran.projects.projectx.model.TaskViewModel;
import com.tigran.projects.projectx.util.GpsUtils;

import androidx.navigation.fragment.NavHostFragment;

import java.util.List;

import static com.tigran.projects.projectx.fragment.tasks.TasksFragment.BUILD_MUSCLES_INFO;
import static com.tigran.projects.projectx.fragment.tasks.TasksFragment.LOOSE_WEIGHT_INFO;


public class TaskInfoFragment extends DialogFragment {

    //public static vars
    public static final String LOOSE_WEIGHT = "Loose Weight";
    public static final String BUILD_MUSCLES = "Build Muscles";

    //views
    private TextView mTitleView;
    private TextView mDescriptionView;
    private Button mOkButton;

    //navigation
    private Fragment mNavHostFragment;

    //view model
    private TaskViewModel mTaskViewModel;

    //constructor
    public TaskInfoFragment() {
    }

    //************************************ LIFECYCLE METHODS ****************************************
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_info, container, false);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_corners_dialog);
        initViews(view);


        mTaskViewModel = ViewModelProviders.of(getActivity()).get(TaskViewModel.class);
        mTaskViewModel.getTask().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s != null) {
                    switch (s) {
                        case LOOSE_WEIGHT_INFO:
                            mTitleView.setText("Loose Weight");
                            mDescriptionView.setText("Reach red marker and you will burn about 50 calories! " +
                                    "Do this task every day and in a month you will loose 1kg!");
                            break;
                        case BUILD_MUSCLES_INFO:
                            mTitleView.setText("Build Muscles");
                            mDescriptionView.setText("More strength is more muscles. Do this task every day and you will have great body.");
                            break;
                    }
                }
            }
        });


        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTaskViewModel != null) {
                    switch (mTaskViewModel.getTask().getValue()) {
                        case LOOSE_WEIGHT_INFO:
                            if (!isLocationEnabled(getContext())) {
                                requestPermissions(getContext());
                            } else {
                                mTaskViewModel.setTask(LOOSE_WEIGHT);
                                NavHostFragment.findNavController(mNavHostFragment).navigate(R.id.action_map_fragment_to_loose_weight_map_fragment);

                                getDialog().dismiss();
                            }
                            break;
                        case BUILD_MUSCLES_INFO:
                            NavHostFragment.findNavController(mNavHostFragment).navigate(R.id.action_map_fragment_to_build_muscles_fragment);
                            mTaskViewModel.setTask(null);
                            getDialog().dismiss();
                            break;
                    }
                }
            }
        });
        return view;
    }


    //************************************** METHODS ********************************************
    private void initViews(View view) {
        mTitleView = view.findViewById(R.id.tv_title_task_info);
        mDescriptionView = view.findViewById(R.id.tv_description_task_info);
        mOkButton = view.findViewById(R.id.btn_build_muscles_tasks);
        mNavHostFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
    }

    public void requestPermissions(Context context) {
        new GpsUtils(context).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
//                isGPS = isGPSEnable;

            }
        });


        Dexter.withActivity((Activity) context)
                .withPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(new MultiplePermissionsListener() {


            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
//                if (report.isAnyPermissionPermanentlyDenied()) {
//                    requestPermissions(context);
//                }
//                if (!isLocationEnabled(context)) {
//                    requestPermissions(context);
//                } else {
//                    mTaskViewModel.setTask(LOOSE_WEIGHT);
//                }


            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

            }

        }).check();
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;

        try {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return locationMode != Settings.Secure.LOCATION_MODE_OFF;

    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Your GPS seems to be disabled, please enable it in order to continue.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
