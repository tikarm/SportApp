package com.tigran.projects.projectx.fragment.launch;


import android.app.ProgressDialog;
import androidx.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.tigran.projects.projectx.R;
import com.tigran.projects.projectx.model.User;
import com.tigran.projects.projectx.model.UserViewModel;
import com.tigran.projects.projectx.preferences.SaveSharedPreferences;

import java.util.ArrayList;
import java.util.List;

import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import static com.tigran.projects.projectx.fragment.launch.SignInFragment.DATABASE_PATH_NAME;
import static com.tigran.projects.projectx.util.NavigationHelper.onClickNavigate;


public class SignUpFragment extends Fragment {

    //shared preferences
    private SaveSharedPreferences sharedPreferences = new SaveSharedPreferences();

    //views
    private EditText mUsernameView;
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private Button mSignUpButton;
    private TextView mSuggestionSignUp;
    private TextInputLayout mUsernameLayout;
    private TextInputLayout mEmailLayout;
    private TextInputLayout mPasswordLayout;
    private TextInputLayout mConfirmPasswordLayout;

    //Dialog
    ProgressDialog mProgressDialog;

    //collections
    private List<User> mUserList = new ArrayList<>();

    //Firebase
    private FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;

    //Bundle
    private Bundle bundle;

    //User
    private User mCurrentUser;
    private UserViewModel mUserViewModel;

    //constructor
    public SignUpFragment() {

    }

    //************************************ LIFECYCLE METHODS ****************************************
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        mProgressDialog = new ProgressDialog(this.getActivity());

        initViews(view);
        setUsernameErrors();
        setEmailError();
        setPasswordError();
        setConfirmPasswordError();
        addUserToList();

        mUserViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        mFirebaseAuth = FirebaseAuth.getInstance();


        onClickNavigate(mSuggestionSignUp, R.id.action_fragment_sign_up_to_fragment_sign_in);


        return view;
    }

    //************************************** METHODS ********************************************
    private void addUserToList() {
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mConfirmPasswordView.getText().toString().equals(mPasswordView.getText().toString())
                        && mConfirmPasswordView.getText().length() != 0) {
                    mConfirmPasswordLayout.setError(getResources().getString(R.string.confirm_password_enter));
                }else{
                    mConfirmPasswordLayout.setError(null);
                }

                if (mUsernameView.getText().length() == 0) {
                    mUsernameLayout.setError(getResources().getString(R.string.username_enter));
                } else if (mEmailView.getText().length() == 0) {
                    mEmailView.setError(getResources().getString(R.string.email_enter));
                } else if (mPasswordView.getText().length() == 0) {
                    mPasswordLayout.setError(getResources().getString(R.string.password_enter));
                } else if (mConfirmPasswordView.getText().length() == 0) {
                    mConfirmPasswordLayout.setError(getResources().getString(R.string.confirm_password_enter));
                } else if (mUsernameLayout.getError() == null && mEmailView.getError() == null
                        && mPasswordLayout.getError() == null && mConfirmPasswordLayout.getError() == null) {
                    if (!mConfirmPasswordView.getText().toString().equals(mPasswordView.getText().toString())
                            && mConfirmPasswordView.getText().length() != 0) {
                        mConfirmPasswordLayout.setError(getResources().getString(R.string.confirm_password_matching));
                    } else {
                        mCurrentUser = new User();
                        mCurrentUser.setUsername(mUsernameView.getText().toString());
                        mCurrentUser.setEmail(mEmailView.getText().toString());

                        mUserList.add(mCurrentUser);
                        registerUserToFirebase(mCurrentUser);//add user to firebase
                        mUserViewModel.setUser(mCurrentUser);
                        openAccount(v);
                    }

                }
            }
        });
    }

    private void setConfirmPasswordError() {
        mConfirmPasswordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!mConfirmPasswordView.getText().toString().equals(mPasswordView.getText().toString())
                            && mConfirmPasswordView.getText().length() != 0) {
                        mConfirmPasswordLayout.setError(getResources().getString(R.string.confirm_password_enter));
                    }else{
                        mConfirmPasswordLayout.setError(null);
                    }
                }
            }
        });
    }

    private void setPasswordError() {
        mPasswordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (mPasswordView.getText().length() < 6 && mPasswordView.getText().length() != 0) {
                        mPasswordLayout.setError(getResources().getString(R.string.password_length));
                    }else{
                        mPasswordLayout.setError(null);
                    }
                }
            }
        });
    }

    private void setEmailError() {
        mEmailView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!isValidEmail(mEmailView.getText().toString().trim()) && mEmailView.getText().length() != 0) {
                        mEmailView.setError(getResources().getString(R.string.email_wrong));
                    }else{
                        mEmailView.setError(null);
                    }
                }
            }
        });
    }


    private void setUsernameErrors() {
        mUsernameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 20) {
                    mUsernameLayout.setError(getResources().getString(R.string.username_length_long));
                    mUsernameView.setTextColor(Color.RED);
                } else {
                    mUsernameView.setTextColor(Color.BLACK);
                    mUsernameLayout.setError(null);
                }
                if (!s.toString().matches("[a-zA-Z0-9]*")) {
                    mUsernameLayout.setError(getResources().getString(R.string.username_content));
                }else{
                    mUsernameLayout.setError(null);
                }
                for (User u : mUserList) {
                    if (u.getUsername().equals(s.toString())) {
                        mUsernameLayout.setError(getResources().getString(R.string.username_existence));
                        mUsernameView.setTextColor(Color.RED);
                    }else{
                        mUsernameLayout.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mUsernameView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (mUsernameView.getText().length() < 6 && mUsernameView.getText().length() != 0) {
                        mUsernameView.setError(getResources().getString(R.string.username_length_short));
                    }else{
                        mUsernameLayout.setError(null);
                    }
                }
            }
        });
    }


    private boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    private void openAccount(View v) {
        sharedPreferences.setLoggedIn(getActivity(), true);
        NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.menu_item_log_out, true).build();
        Navigation.findNavController(v).navigate(R.id.action_fragment_sign_up_to_map_fragment, null, navOptions);
    }


    private void initViews(View v) {
        mUsernameView = v.findViewById(R.id.etv_username_sign_up);
        mEmailView = v.findViewById(R.id.etv_email_sign_up);
        mPasswordView = v.findViewById(R.id.etv_password_sign_up);
        mConfirmPasswordView = v.findViewById(R.id.etv_confirm_password_sign_up);
        mSignUpButton = v.findViewById(R.id.btn_register_sign_up);
        mSuggestionSignUp = v.findViewById(R.id.tv_suggestion_sign_up);
        mUsernameLayout = v.findViewById(R.id.til_username_sign_up);
        mEmailLayout = v.findViewById(R.id.til_email_sign_up);
        mPasswordLayout = v.findViewById(R.id.til_password_sign_up);
        mConfirmPasswordLayout = v.findViewById(R.id.til_confirm_password_sign_up);
    }

    private void registerUserToFirebase(User user) {
        mProgressDialog.setMessage("Creating account ...");
        mProgressDialog.show();
        final User mUser = user;

        mFirebaseAuth.createUserWithEmailAndPassword(user.getEmail(), mPasswordView.getText().toString())
                .addOnCompleteListener(this.getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        mProgressDialog.dismiss();
                        //checking if success
                        if (task.isSuccessful()) {
                            //registered
                            FirebaseDatabase.getInstance()
                                    .getReference(DATABASE_PATH_NAME)
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(mUser)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                addUidToUser();
//                                                mFirebaseUser = mFirebaseAuth.getCurrentUser();


                                                //Toast.makeText(getContext(), "User Added To Firebase",Toast.LENGTH_SHORT).show();

                                            } else {
                                                //Toast.makeText(getContext(), "User Not Added To Firebase",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        } else {
                            String s = task.getException().getMessage();
                            Toast.makeText(getActivity(), "" + s, Toast.LENGTH_SHORT).show();
                            //not registered
                        }
                    }
                });
    }

    private void addUidToUser() {
        mCurrentUser.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        FirebaseDatabase.getInstance()
                .getReference(DATABASE_PATH_NAME)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(mCurrentUser);
    }

}
