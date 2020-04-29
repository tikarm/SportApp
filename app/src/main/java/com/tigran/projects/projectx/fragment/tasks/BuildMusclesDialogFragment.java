package com.tigran.projects.projectx.fragment.tasks;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tigran.projects.projectx.R;
import com.tigran.projects.projectx.model.BuildMusclesViewModel;
import com.tigran.projects.projectx.model.TaskViewModel;
import com.tigran.projects.projectx.model.TodaysTaskInfo;
import com.tigran.projects.projectx.model.User;
import com.tigran.projects.projectx.model.UserViewModel;
import com.tigran.projects.projectx.preferences.SaveSharedPreferences;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static com.tigran.projects.projectx.fragment.tasks.BuildMusclesFragment.PULL_UPS;
import static com.tigran.projects.projectx.fragment.tasks.BuildMusclesFragment.PUSH_UPS;
import static com.tigran.projects.projectx.fragment.tasks.BuildMusclesFragment.SIT_UPS;
import static com.tigran.projects.projectx.fragment.tasks.BuildMusclesFragment.SQUATS;


public class BuildMusclesDialogFragment extends DialogFragment {

    //views
    private TextView mTitleView;
    private TextView mDescriptionView;
    private TextView mTimerView;
    private Button[] mDoneButtons = new Button[4];
    private ImageView[] mImageViews = new ImageView[4];
    private ConstraintLayout mTimerLayout;
    private Button mStopButton;
    private View mLastLineView;

    //timer
    private CountDownTimer mCountDownTimer;

    //view model
    private BuildMusclesViewModel mBuildMusclesViewModel;
    private TaskViewModel mTaskViewModel;

    //preferences
    SaveSharedPreferences sharedPreferences = new SaveSharedPreferences();

    //user
    private User mCurrentUser;
    private UserViewModel mUserViewModel;

    //firebase
    DatabaseReference mFirebaseDatabaseUser;

    //constructor
    public BuildMusclesDialogFragment() {
    }

    //************************************ LIFECYCLE METHODS ****************************************
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_build_muscles_dialog, container, false);
        initViews(view);

        mUserViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        mUserViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                mCurrentUser = user;
            }
        });
        mCurrentUser = mUserViewModel.getUser().getValue();
        mTaskViewModel = ViewModelProviders.of(getActivity()).get(TaskViewModel.class);
        mBuildMusclesViewModel = ViewModelProviders.of(getActivity()).get(BuildMusclesViewModel.class);
        mBuildMusclesViewModel.getBuildMuscles().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                switch (s) {
                    case PUSH_UPS:
                        mTitleView.setText(R.string.push_ups);
                        mDescriptionView.setText(getString(R.string.push_ups_description));
                        break;
                    case PULL_UPS:
                        mTitleView.setText(R.string.pull_ups);
                        mDescriptionView.setText(getString(R.string.pull_ups_description));
                        break;
                    case SQUATS:
                        mTitleView.setText(R.string.squats);
                        mDescriptionView.setText(getString(R.string.squats_description));
                        break;
                    case SIT_UPS:
                        mTitleView.setText(R.string.sit_ups);
                        mDescriptionView.setText(getString(R.string.sit_ups_description));
                        break;
                }
            }
        });

        for (int i = 0; i < mDoneButtons.length - 1; i++) {
            doneButtonAction(mDoneButtons[i], mDoneButtons[i + 1], mImageViews[i]);
        }

        mDoneButtons[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoneButtons[3].setVisibility(View.INVISIBLE);
                mImageViews[3].setVisibility(View.VISIBLE);
                if (/*mBuildMusclesViewModel.getUnlockLevel().getValue() < 3*/
                        sharedPreferences.getBuildMusclesUnlockLevel(getContext()) < 3) {
                    Log.e("TAG", "onClick: " + sharedPreferences.getBuildMusclesUnlockLevel(getContext()));
//                    setBuildMusclesTaskUnlockLevelForCurrentUserAndUpdateInFirebase(mBuildMusclesViewModel.getUnlockLevel().getValue() + 1);
                    setBuildMusclesTaskUnlockLevelForCurrentUserAndUpdateInFirebase(sharedPreferences.getBuildMusclesUnlockLevel(getContext()) + 1);

                    //to trigger view-model onChanged in BuildMusclesFragment to open next exercise
                    mBuildMusclesViewModel.setUnlockLevel(sharedPreferences.getBuildMusclesUnlockLevel(getContext()) + 1);
                    sharedPreferences.setBuildMusclesUnlockLevel(getContext(), sharedPreferences.getBuildMusclesUnlockLevel(getContext()) + 1);

                } else {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    DoneDialogFragment tasksFragment = new DoneDialogFragment();
                    tasksFragment.show(fm, null);
                    Button mSitUpsButton = getActivity().findViewById(R.id.btn_sit_ups_build_muscles);
                    mSitUpsButton.setEnabled(false);
                    if (/*mTaskViewModel.getDoneTask().getValue() == null*/ sharedPreferences.getDoneTask(getContext()) == null) {
                        mTaskViewModel.setDoneTask(2);
                        sharedPreferences.setDoneTask(getContext(), 2);
                        setTodaysTaskInfoForCurrentUserAndUpdateInFirebase(2, System.currentTimeMillis() / 1000);
                    } else {
                        mTaskViewModel.setDoneTask(3);
                        sharedPreferences.setDoneTask(getContext(), 3);
                        setTodaysTaskInfoForCurrentUserAndUpdateInFirebase(3, System.currentTimeMillis() / 1000);
                    }
                    mTaskViewModel.setBuildMusclesTimestamp(System.currentTimeMillis() / 1000);
                    sharedPreferences.setBuildMusclesTimestamp(getContext(), System.currentTimeMillis() / 1000);
//                    sharedPreferences.setTask(getContext(), mTaskViewModel.getDoneTask().getValue());

                }
                dismiss();
            }
        });
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNextSet();
            }
        });

        return view;
    }

    //************************************** METHODS ********************************************
    private void initViews(View view) {
        mTitleView = view.findViewById(R.id.tv_title_muscles_dialog);
        mDescriptionView = view.findViewById(R.id.tv_description_muscles_dialog);
        mTimerView = view.findViewById(R.id.tv_timer_muscles_dialog);

        mDoneButtons[0] = view.findViewById(R.id.btn_done1_muscles_dialog);
        mDoneButtons[1] = view.findViewById(R.id.btn_done2_muscles_dialog);
        mDoneButtons[2] = view.findViewById(R.id.btn_done3_muscles_dialog);
        mDoneButtons[3] = view.findViewById(R.id.btn_done4_muscles_dialog);

        mImageViews[0] = view.findViewById(R.id.iv_tick1_muscles_dialog);
        mImageViews[1] = view.findViewById(R.id.iv_tick2_muscles_dialog);
        mImageViews[2] = view.findViewById(R.id.iv_tick3_muscles_dialog);
        mImageViews[3] = view.findViewById(R.id.iv_tick4_muscles_dialog);

        mStopButton = view.findViewById(R.id.btn_stop_timer_muscles_dialog);

        mLastLineView = view.findViewById(R.id.line4_mucles_dialog);

        mTimerLayout = view.findViewById(R.id.layout_timer_muscles_dialog);
    }

    private void doneButtonAction(Button b1, Button b2, ImageView nextImageView) {
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimerLayout.setVisibility(View.VISIBLE);
                mLastLineView.setVisibility(View.VISIBLE);
                b1.setVisibility(View.INVISIBLE);
                nextImageView.setVisibility(View.VISIBLE);

                mCountDownTimer = new CountDownTimer(90000, 1000) {
                    int counter = 90;

                    public void onTick(long millisUntilFinished) {
                        int currentMinute = counter / 60;
                        int currentSeconds = counter % 60;
                        NumberFormat f = new DecimalFormat("00");
                        mTimerView.setText(String.format("%s:%s", f.format(currentMinute), f.format(currentSeconds)));
                        counter--;
                    }

                    public void onFinish() {
                        this.cancel();
                        mTimerLayout.setVisibility(View.GONE);
                        mTimerView.setText(getString(R.string.finished));
                        openNextSet();
                    }
                }.start();
            }
        });
    }

    private void openNextSet() {
        for (int i = 0; i < mDoneButtons.length; i++) {
            if (!mDoneButtons[i].isEnabled()) {
                mDoneButtons[i].setTextColor(getResources().getColor(R.color.green));
                mDoneButtons[i].setEnabled(true);
                mCountDownTimer.cancel();
                mLastLineView.setVisibility(View.GONE);
                mTimerLayout.setVisibility(View.GONE);
                break;
            }
        }
    }

    private void updateUserInFirebase() {
        mFirebaseDatabaseUser = FirebaseDatabase.getInstance().getReference("users");
        mFirebaseDatabaseUser.child(mCurrentUser.getId()).setValue(mCurrentUser);
    }

    private void setTodaysTaskInfoForCurrentUserAndUpdateInFirebase(int doneTask, long timestamp) {
        TodaysTaskInfo todaysTaskInfo;
        if (mCurrentUser.getTodaysTaskInfo() == null) {
            todaysTaskInfo = new TodaysTaskInfo();
        } else {
            todaysTaskInfo = mCurrentUser.getTodaysTaskInfo();
        }
        todaysTaskInfo.setDoneTasksStatus(doneTask);
        todaysTaskInfo.setTimestampBuildMuscles(timestamp);
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
