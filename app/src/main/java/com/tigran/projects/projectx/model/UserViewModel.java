package com.tigran.projects.projectx.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {
    private MutableLiveData<User> mUserMutableLiveData = new MutableLiveData<>();

    private MutableLiveData<User> mOtherUserLiveData = new MutableLiveData<>();

    public void setUser(User user){
        mUserMutableLiveData.setValue(user);
    }

    public LiveData<User> getUser(){
        return mUserMutableLiveData;
    }

    public void setOtherUser(User user){
        mOtherUserLiveData.setValue(user);
    }

    public LiveData<User> getOtherUser(){
        return mOtherUserLiveData;
    }
}
