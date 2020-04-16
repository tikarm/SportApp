package com.tigran.projects.projectx.api;

import com.tigran.projects.projectx.model.News;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("top-headlines")
    Call<News> getNews(
            @Query("country") String country,
            @Query("category") String category,
            @Query("apiKey") String apiKey,
            @Query("page") int page,
            @Query("pageSize") int size
    );

}
