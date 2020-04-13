package com.tigran.projects.projectx.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EventViewModel extends ViewModel {

    private MutableLiveData<Event> mEventMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsEditMutableLiveData = new MutableLiveData<>();
    private boolean toEdit;

    public void setEvent(Event event){
        mEventMutableLiveData.setValue(event);
    }

    public LiveData<Event> getEvent(){
        return mEventMutableLiveData;
    }

    public boolean isToEdit() {
        return toEdit;
    }

    public void setToEdit(boolean toEdit) {
        this.toEdit = toEdit;
    }
}
