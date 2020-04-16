package com.tigran.projects.projectx.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.tigran.projects.projectx.model.User;

public class SaveSharedPreferences {

    public static final String TAG = SaveSharedPreferences.class.getSimpleName();
    public static final String USER_KEY = "user_key";
    public static final String ZOOM_BUTTONS = "zoom_buttons";
    public static final String THEME = "theme change";
    public static final String ENABLED_TASKS = "enabled tasks";
    public static final String TIME = "time";
    public static final String BUILD_MUSCLES_LEVEL = "level";
    public static final String TOP_CHARTS_PAGE = "top_charts_page";

    public static final String RED = "red";

    User mUser;

    static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }


    public void setLoggedIn(Context context, boolean loggedIn) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(TAG, loggedIn);
        editor.apply();
    }

    public boolean getLoggedStatus(Context context) {
        return getPreferences(context).getBoolean(TAG, false);
    }

    public void setZoom(Context context, boolean isZoom) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(ZOOM_BUTTONS, isZoom);
        editor.apply();
    }

    public boolean getZoom(Context context) {
        return getPreferences(context).getBoolean(ZOOM_BUTTONS, false);
    }

    public void setTheme(Context context, String color) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(THEME, color);
        editor.apply();
    }

    public String getTheme(Context context) {
        return getPreferences(context).getString(THEME, RED);
    }

    public void setTask(Context context, Integer i) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(ENABLED_TASKS, i);
        editor.apply();
    }

    public Integer getTask(Context context) {
        return getPreferences(context).getInt(ENABLED_TASKS, 0);
    }

    public void setTime(Context context, long i) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putLong(TIME, i);
        editor.apply();
    }

    public long getTime(Context context) {
        return getPreferences(context).getLong(TIME, 0);
    }

    public void setBuildMusclesUnlockLevel(Context context, long i) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putLong(BUILD_MUSCLES_LEVEL, i);
        editor.apply();
    }

    public long getBuildMusclesUnlockLevel(Context context) {
        Log.e("hhhh", "pref  " + getPreferences(context).getLong(BUILD_MUSCLES_LEVEL, 0));
        return getPreferences(context).getLong(BUILD_MUSCLES_LEVEL, 0);
    }

    public void setCurrentUser(Context context, User mUser) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        Gson gson = new Gson();
        String json = gson.toJson(mUser);
        editor.putString(USER_KEY, json);
        editor.apply();
    }

    public User getCurrentUser(Context context) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        Gson gson = new Gson();
        String json = getPreferences(context).getString(USER_KEY, "");
        User mUser = gson.fromJson(json, User.class);
        return mUser;
    }

    public void setTopChartsPage(Context context, int page) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(TOP_CHARTS_PAGE, page);
        editor.apply();
    }

    public int getTopChartsPage(Context context) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        int page = getPreferences(context).getInt(TOP_CHARTS_PAGE, 0);
        return page;
    }

}
