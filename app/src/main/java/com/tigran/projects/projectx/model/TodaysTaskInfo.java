package com.tigran.projects.projectx.model;

public class TodaysTaskInfo {

    //0-all are enabled, 1-only "loose weight" is disabled, 2-only "build muscles is disabled", 3-all are disabled
    private int doneTasksStatus;

    //timestamps when user finished tasks
    private long timestampLooseWeight;
    private long timestampBuildMuscles;

    //build muscles task states
    private String buildMusclesTaskName;
    private long buildMusclesUnlockLevel;

    public int getDoneTasksStatus() {
        return doneTasksStatus;
    }

    public void setDoneTasksStatus(int doneTasksStatus) {
        this.doneTasksStatus = doneTasksStatus;
    }

    public long getTimestampLooseWeight() {
        return timestampLooseWeight;
    }

    public void setTimestampLooseWeight(long timestampLooseWeight) {
        this.timestampLooseWeight = timestampLooseWeight;
    }

    public long getTimestampBuildMuscles() {
        return timestampBuildMuscles;
    }

    public void setTimestampBuildMuscles(long timestampBuildMuscles) {
        this.timestampBuildMuscles = timestampBuildMuscles;
    }

    public String getBuildMusclesTaskName() {
        return buildMusclesTaskName;
    }

    public void setBuildMusclesTaskName(String buildMusclesTaskName) {
        this.buildMusclesTaskName = buildMusclesTaskName;
    }

    public long getBuildMusclesUnlockLevel() {
        return buildMusclesUnlockLevel;
    }

    public void setBuildMusclesUnlockLevel(long buildMusclesUnlockLevel) {
        this.buildMusclesUnlockLevel = buildMusclesUnlockLevel;
    }
}
