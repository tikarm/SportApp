package com.tigran.projects.projectx.fragment.topCharts;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tigran.projects.projectx.R;
import com.tigran.projects.projectx.adapter.TabAdapter;
import com.tigran.projects.projectx.model.User;
import com.tigran.projects.projectx.model.UserViewModel;
import com.tigran.projects.projectx.preferences.SaveSharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.viewpager.widget.ViewPager;

import static com.google.android.gms.common.util.CollectionUtils.setOf;
import static com.tigran.projects.projectx.fragment.launch.SignInFragment.DATABASE_PATH_NAME;


public class TopChartsFragment extends Fragment {

    //final Strings
    private static final String TAG = "TopChartsFragment";
    public static final String PULL_UPS = "Pull-ups";
    public static final String PUSH_UPS = "Push-ups";
    public static final String PARALLEL_DIPS = "Parallel Dips";
    public static final String JUGGLING = "Juggling";
    public static final String POINTS = "Points";

    //navigation
    private NavController mNavController;
    private Fragment mNavHostFragment;
    private BottomNavigationView mBottomNavigationView;

    //views
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private View view;
    private ProgressBar mProgressBar;

    //adapter
    private TabAdapter mTabAdapter;

    //Firebase
    private DatabaseReference mFirebaseDatabse;

    //viewmodel
    private UserViewModel mUserViewModel;

    //fragments
    private PullUpsFragment mPullUpsFragment;
    private PushUpsFragment mPushUpsFragment;
    private ParallelDipsFragment mParallelDipsFragment;
    private JugglingFragment mJugglingFragment;

    //user
    List<User> mUserList = new ArrayList<>();
    User otherUser;
    User currentUser;

    List<User> mPullUpsList = new ArrayList<>();
    List<User> mPushUpsList = new ArrayList<>();
    List<User> mParallelDipsList = new ArrayList<>();
    List<User> mJugglingList = new ArrayList<>();
    List<User> mPointsList = new ArrayList<>();

    private SaveSharedPreferences sharedPreferences;

    //constructor
    public TopChartsFragment() {
    }

    //************************************ LIFECYCLE METHODS ****************************************
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_top_charts, container, false);

        mUserViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        mUserViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                currentUser = user;
            }
        });
        currentUser = mUserViewModel.getUser().getValue();

        mFirebaseDatabse = FirebaseDatabase.getInstance().getReference(DATABASE_PATH_NAME);
        getUsersFromFirebase();
        initViews(view);
        setNavigationComponent();
        showBotNavBar();

        mProgressBar.setVisibility(View.GONE);
        setTabAdapter();

        return view;
    }

    //************************************** METHODS ********************************************
    private void initViews(View view) {
        mNavHostFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        mViewPager = view.findViewById(R.id.vp_top_charts);
        mProgressBar = view.findViewById(R.id.pb_top_charts);
        mBottomNavigationView = getActivity().findViewById(R.id.bottom_navigation_view_main);
    }

    private void setTabAdapter() {
        sharedPreferences = new SaveSharedPreferences();
        if (getActivity() != null) {
            mTabAdapter = new TabAdapter(getActivity().getSupportFragmentManager());
        }

        mPullUpsFragment = new PullUpsFragment(mPullUpsList, mUserList);
        mPushUpsFragment = new PushUpsFragment(mPushUpsList, mUserList);
        mParallelDipsFragment = new ParallelDipsFragment(mParallelDipsList, mUserList);
        mJugglingFragment = new JugglingFragment(mJugglingList, mUserList);

        mTabAdapter.addFragment(mPullUpsFragment, getResources().getString(R.string.pull_ups));
        mTabAdapter.addFragment(mPushUpsFragment, getResources().getString(R.string.push_ups));
        mTabAdapter.addFragment(mParallelDipsFragment, getResources().getString(R.string.parallel_dips));
        mTabAdapter.addFragment(mJugglingFragment, getResources().getString(R.string.juggling));

        mViewPager.setAdapter(mTabAdapter);
        mViewPager.setCurrentItem(sharedPreferences.getTopChartsPage(getContext()));
        addPageSelectListener();
    }

    private void setListeners() {
        mTabLayout = view.findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
//        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                switch (tab.getPosition()) {
//                    case 0:
//                        initRecyclerView(mPullUpsList, PULL_UPS);
//                        break;
//                    case 1:
//                        initRecyclerView(mPushUpsList, PUSH_UPS);
//                        break;
//                    case 2:
//                        initRecyclerView(mParallelDipsList, PARALLEL_DIPS);
//                        break;
//                    case 3:
//                        initRecyclerView(mJugglingList, JUGGLING);
//                        break;
//                    case 4:
//                        initRecyclerView(mPointsList, POINTS);
//                        break;
//                }
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//            }
//        });
    }

    private void addPageSelectListener() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                sharedPreferences.setTopChartsPage(getContext(), position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    private void getUsersFromFirebase() {
        mFirebaseDatabse.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUserList.clear();
                mPushUpsList.clear();
                mPullUpsList.clear();
                mParallelDipsList.clear();
                mJugglingList.clear();
                mPointsList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    mUserList.add(user);
                    if (user.getSkills() != null) {
                        if (user.getSkills().containsKey(PULL_UPS)) {
                            mPullUpsList.add(user);
                        }
                        if (user.getSkills().containsKey(PUSH_UPS)) {
                            mPushUpsList.add(user);

                        }
                        if (user.getSkills().containsKey(PARALLEL_DIPS)) {
                            mParallelDipsList.add(user);
                        }
                        if (user.getSkills().containsKey(JUGGLING)) {
                            mJugglingList.add(user);
                        }
                        if (user.getPoints() != null) {
                            mPointsList.add(user);
                        }
                    }
                }
                if (getContext() != null) {
                    setListeners();
                    setTabAdapter();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    static void sortItems(List<User> listToSort, String key) {

        Collections.sort(listToSort, new Comparator<User>() {
            @Override
            public int compare(User t1, User t2) {
                if (!key.equals(POINTS)) {
                    return t2.getSkills().get(key).compareTo(t1.getSkills().get(key));
                } else {
                    return t2.getPoints().compareTo(t1.getPoints());
                }
            }
        });
    }


    private void setNavigationComponent() {
        mNavController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(setOf(R.id.top_charts_fragment)).build();
//        NavigationUI.setupWithNavController(mToolbarTopCharts, mNavController, appBarConfiguration);
    }

    private void showBotNavBar() {
        mBottomNavigationView.setVisibility(View.VISIBLE);
    }

}
