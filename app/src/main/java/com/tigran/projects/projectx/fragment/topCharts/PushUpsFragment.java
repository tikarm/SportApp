package com.tigran.projects.projectx.fragment.topCharts;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tigran.projects.projectx.R;
import com.tigran.projects.projectx.adapter.ChartsAdapter;
import com.tigran.projects.projectx.model.User;
import com.tigran.projects.projectx.model.UserViewModel;

import java.util.ArrayList;
import java.util.List;

import static com.tigran.projects.projectx.fragment.topCharts.TopChartsFragment.PULL_UPS;
import static com.tigran.projects.projectx.fragment.topCharts.TopChartsFragment.PUSH_UPS;
import static com.tigran.projects.projectx.fragment.topCharts.TopChartsFragment.sortItems;


public class PushUpsFragment extends Fragment {

    //data
    private List<User> mData;
    private List<User> mUserList = new ArrayList<>();
    private User currentUser;

    //viewmodel
    private UserViewModel mUserViewModel;

    //views
    private RecyclerView mRecyclerView;
    private Fragment mNavHostFragment;
    private TextView mNoRecordsTextView;

    //adapter
    private ChartsAdapter mChartsAdapter;

    public PushUpsFragment() {
        // Required empty public constructor
    }

    public PushUpsFragment(List<User> mData, List<User> mUserList) {
        this.mData = mData;
        this.mUserList = mUserList;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_top_charts, container, false);

        initViews(view);
        initRecyclerView();
        initTools();

        return view;
    }

    private void initViews(View view) {
        mRecyclerView = view.findViewById(R.id.rv_top_charts);
        mNavHostFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        mNoRecordsTextView = view.findViewById(R.id.tv_top_charts);

        if (mData == null || mData.isEmpty()) {
            mNoRecordsTextView.setVisibility(View.VISIBLE);
        }
    }

    private void initRecyclerView() {
        mChartsAdapter = new ChartsAdapter();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mRecyclerView.setAdapter(mChartsAdapter);

        sortItems(mData, PUSH_UPS);

        List<User> tenList = new ArrayList<>();
        for (int i = 0; i < 10 && i < mData.size(); i++) {
            tenList.add(mData.get(i));
        }

        mChartsAdapter.addItems(tenList, PUSH_UPS);

        mChartsAdapter.setOnRvItemClickListener(new ChartsAdapter.OnRvItemClickListener() {
            @Override
            public void onItemClicked(String uid) {
                for (User user : mUserList) {
                    if (user.getId().equals(uid)) {
                        if (currentUser.getId().equals(uid)) {
                            NavHostFragment.findNavController(mNavHostFragment).navigate(R.id.action_top_charts_fragment_to_my_profile_fragment);
                        } else {
                            mUserViewModel.setOtherUser(user);
                            NavHostFragment.findNavController(mNavHostFragment).navigate(R.id.action_top_charts_fragment_to_other_profile_fragment);
                        }
                    }
                }

            }
        });
    }

    private void initTools() {
        mUserViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        mUserViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                currentUser = user;
            }
        });
        currentUser = mUserViewModel.getUser().getValue();
    }
}
