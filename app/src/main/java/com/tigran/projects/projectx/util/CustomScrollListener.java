package com.tigran.projects.projectx.util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public abstract class CustomScrollListener extends RecyclerView.OnScrollListener {

    @SuppressWarnings("FieldCanBeLocal")
    private final int VISIBLE_THRESHOLD = 10;
    private int currentPage = 1;
    private int previousTotalItemCount = 0;
    private boolean loading = true;

    private LinearLayoutManager mLayoutManager;

    public CustomScrollListener(LinearLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView view, int dx, int dy) {
        int lastVisibleItemPosition = 0;
        int totalItemCount = mLayoutManager.getItemCount();


        lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();


        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        if (!loading && (lastVisibleItemPosition + VISIBLE_THRESHOLD) > totalItemCount) {
            currentPage++;
            onLoadMore(currentPage, totalItemCount, view);
            loading = true;
        }
    }

    public abstract void onLoadMore(int page, int totalItemsCount, RecyclerView view);

}

