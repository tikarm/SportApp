package com.tigran.projects.projectx.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BuildMusclesViewModel extends ViewModel {

    private MutableLiveData<String> mBuildMusclesMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<Long> mUnlockLevelMutableLiveData = new MutableLiveData<>();

    public void setBuildMuscles(String task){
        mBuildMusclesMutableLiveData.setValue(task);
    }

    public LiveData<String> getBuildMuscles(){
        return mBuildMusclesMutableLiveData;
    }

    public void setUnlockLevel(Long lvl){
        mUnlockLevelMutableLiveData.setValue(lvl);
    }

    public LiveData<Long> getUnlockLevel(){
        return mUnlockLevelMutableLiveData;
    }

}
