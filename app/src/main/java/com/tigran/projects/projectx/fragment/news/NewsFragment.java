package com.tigran.projects.projectx.fragment.news;

import android.os.Bundle;

import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ProgressBar;
import android.widget.Toast;

import com.tigran.projects.projectx.R;
import com.tigran.projects.projectx.activity.MainActivity;
import com.tigran.projects.projectx.adapter.NewsAdapter;
import com.tigran.projects.projectx.api.ApiClient;
import com.tigran.projects.projectx.api.ApiInterface;
import com.tigran.projects.projectx.model.Article;
import com.tigran.projects.projectx.model.News;
import com.tigran.projects.projectx.util.CustomScrollListener;

import java.util.ArrayList;
import java.util.List;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.gms.common.util.CollectionUtils.setOf;


public class NewsFragment extends Fragment {

    //static vars
    public static final String API_KEY = "65e92264bb9f4960a49d934c9e2051a9";
    public static final String TAG = MainActivity.class.getSimpleName();

    private List<Article> mArticles = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private RecyclerView mRecyclerView;
    private NewsAdapter mNewsAdapter;

    //views
    private ProgressBar mProgressBar;
//    private Toolbar mToolbarNews;

    //variables
    private boolean isLastPage = false;

    //navigation
    private NavController mNavController;

    //constructor
    public NewsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        initViews(view);

        setNewsToolbar();
        setNavigationComponent();
//        initRecyclerView();

        loadJson(1);

        return view;
    }

    private void initRecyclerView() {
        layoutManager = new LinearLayoutManager(getContext());
        CustomScrollListener scrollListener = new CustomScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadJson(page);
            }
        };

        mNewsAdapter = new NewsAdapter(mArticles, getContext());
        mRecyclerView.setAdapter(mNewsAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addOnScrollListener(scrollListener);
    }


    private void initViews(View view) {
        mRecyclerView = view.findViewById(R.id.rv_news);
        mProgressBar = view.findViewById(R.id.pb_news);
//        mToolbarNews = view.findViewById(R.id.toolbar_news);
    }

    private void loadJson(int page) {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<News> call = apiInterface.getNews("gb", "sports", API_KEY, page, 100);
        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && response.body().getArticle() != null) {
                    if (!mArticles.isEmpty()) {
                        mArticles.clear();
                    }
                    mArticles = response.body().getArticle();
                    if (mNewsAdapter == null) {
                        initRecyclerView();
                    } else {
                        mNewsAdapter.swapData(mArticles);
                    }
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    Log.e(TAG, "onResponse: No result");
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {

            }
        });
    }

    private void setNewsToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
//        activity.setSupportActionBar(mToolbarNews);
    }

    private void setNavigationComponent() {
        mNavController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(setOf(R.id.news_fragment)).build();
//        NavigationUI.setupWithNavController(mToolbarNews, mNavController, appBarConfiguration);
    }


}
