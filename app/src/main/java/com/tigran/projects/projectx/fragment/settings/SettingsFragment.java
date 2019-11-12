package com.tigran.projects.projectx.fragment.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.tigran.projects.projectx.R;
import com.tigran.projects.projectx.model.UserViewModel;
import com.tigran.projects.projectx.preferences.SaveSharedPreferences;

import java.util.ArrayList;
import java.util.List;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import static com.google.android.gms.common.util.CollectionUtils.setOf;
import static com.tigran.projects.projectx.fragment.launch.SignInFragment.DATABASE_PATH_NAME;


public class SettingsFragment extends Fragment {

    //navigation
    private NavController mNavController;

    //views
    private Toolbar mToolbarSettings;
    private Spinner mThemeSpinner;
    private Switch mNotificationsSwitch;
    private Switch mMapZoomSwitch;
    private Switch mUserInfoSwitch;
    private TextView mFontSizeView;
    private TextView mLanguageView;
    private TextView mDeleteAccountView;

    //colors
    Color colorRed;
    Color colorBlue;
    Color colorGreen;
    Color colorViolet;

    //Firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    //Dialog
    ProgressDialog mProgressDialog;

    //navigation
    private Fragment mNavHostFragment;

    //viewmodel
    private UserViewModel mUserViewModel;


    //preferences
    SaveSharedPreferences sharedPreferences = new SaveSharedPreferences();


    //constructor
    public SettingsFragment() {
    }


    //************************************ LIFECYCLE METHODS ****************************************
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mProgressDialog = new ProgressDialog(getContext());
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mUserViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);


        initViews(view);
        setSettingsToolbar();

        setNavigationComponent();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_settings, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    //************************************** METHODS ********************************************
    private void initViews(View view) {
        mToolbarSettings = view.findViewById(R.id.toolbar_settings);
        mNotificationsSwitch = view.findViewById(R.id.sw_notifications_settings);
        mMapZoomSwitch = view.findViewById(R.id.sw_zoom_buttons_settings);
        mUserInfoSwitch = view.findViewById(R.id.sw_user_info_settings);
        mDeleteAccountView = view.findViewById(R.id.tv_delete_account_settings);
        mThemeSpinner = view.findViewById(R.id.spinner_theme);
        mNavHostFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        mDeleteAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle("Are you sure, you want to delete your Account?");
                dialog.setMessage("Deleting this account will result" +
                        " completely removing your account from the system");
                dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mProgressDialog.setMessage("Deleting Account...");
                        mProgressDialog.show();
                        mFirebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mProgressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    FirebaseDatabase.getInstance()
                                            .getReference(DATABASE_PATH_NAME)
                                            .child(mFirebaseUser.getUid())
                                            .removeValue();
                                    mUserViewModel.setUser(null);
                                    mUserViewModel.setOtherUser(null);
                                    sharedPreferences.setLoggedIn(getContext(), false);

                                    Toast.makeText(getContext(), "Account was successfully deleted", Toast.LENGTH_LONG);
                                    //TODO fix error Harut
                                    NavHostFragment.findNavController(mNavHostFragment).navigate(R.id.action_settings_fragment_to_launch_fragment);

                                } else {
                                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG);
                                }
                            }

                        });
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alertDialog = dialog.create();
                alertDialog.show();
            }
        });

        mMapZoomSwitch.setChecked(sharedPreferences.getZoom(getContext()));
        mMapZoomSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if (isChecked) {
                    sharedPreferences.setZoom(getContext(), true);
                } else {
                    sharedPreferences.setZoom(getContext(), false);
                }
            }
        });

        initSpinner();

    }

    private void initSpinner() {
        List<String> mSpinnerColorList = new ArrayList<>();


        mSpinnerColorList.add("Choose color...");
        mSpinnerColorList.add("Red");
        mSpinnerColorList.add("Blue");
        mSpinnerColorList.add("Green");
        mSpinnerColorList.add("Violet");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mSpinnerColorList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mThemeSpinner.setAdapter(adapter);

        mThemeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String currentItemName = parent.getItemAtPosition(position).toString();

                if (position == 0) {
//                    ((TextView) view).setTextColor(Color.GRAY);
                    view.setVisibility(View.INVISIBLE);
                } else {
                    for (String s : mSpinnerColorList) {
                        if (s.equals(currentItemName)) {
                            sharedPreferences.setTheme(getContext(), s);

                            getActivity().finish();
                            final Intent intent = getActivity().getIntent();
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getActivity().startActivity(intent);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setNavigationComponent() {
        mNavController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(setOf(R.id.settings_fragment)).build();
        NavigationUI.setupWithNavController(mToolbarSettings, mNavController, appBarConfiguration);
    }

    private void setSettingsToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mToolbarSettings);
        setHasOptionsMenu(true);
    }
}
