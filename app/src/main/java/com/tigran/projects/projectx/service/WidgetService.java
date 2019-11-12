package com.tigran.projects.projectx.service;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.tigran.projects.projectx.adapter.WidgetAdapter;

public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetAdapter(getApplicationContext(), intent);
    }

}
