package com.example.android.githubsearchwithnotifications.data;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GitHubService {
    @GET("search/repositories?sort=stars")
    Call<GitHubSearchResults> searchRepos(@Query("q") String query);

    @GET("search/repositories")
    Call<GitHubSearchResults> searchRepos(@Query("q") String queryTerm, @Query("sort") String sort);
}
