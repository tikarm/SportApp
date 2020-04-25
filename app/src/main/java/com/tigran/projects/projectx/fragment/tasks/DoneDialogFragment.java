package com.tigran.projects.projectx.fragment.tasks;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.tigran.projects.projectx.R;
import com.tigran.projects.projectx.model.TaskViewModel;


public class DoneDialogFragment extends DialogFragment {

    //public static vars
    public static final String LOOSE_WEIGHT_DONE = "Loose Weight Done";

    //views
    private TextView mDescriptionView;
    private TextView mOkView;
    private ImageView mArnoldView;

    //tasks
    private TaskViewModel mTaskViewModel;

    //navigation
    private Fragment mNavHostFragment;

    //constructor
    public DoneDialogFragment() {
    }


    //************************************ LIFECYCLE METHODS ****************************************
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_done_dialog, container, false);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_corners_dialog);

        initViews(view);

        mTaskViewModel = ViewModelProviders.of(getActivity()).get(TaskViewModel.class);



        Animation zoomAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.zoom);
        mArnoldView.startAnimation(zoomAnimation);

        mOkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTaskViewModel.setTask(LOOSE_WEIGHT_DONE);
                getDialog().dismiss();
                NavHostFragment.findNavController(mNavHostFragment).navigate(R.id.action_loose_weight_map_fragment_to_map_fragment);
            }
        });



        return view;
    }


    //************************************** METHODS ********************************************
    private void initViews(View view) {
        mDescriptionView = view.findViewById(R.id.tv_description_done);
        mArnoldView = view.findViewById(R.id.iv_arnold_done);
        mOkView = view.findViewById(R.id.btn_build_muscles_tasks);
        mNavHostFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
    }

}
