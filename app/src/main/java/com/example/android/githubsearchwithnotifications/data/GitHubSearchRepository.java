package com.example.android.githubsearchwithnotifications.data;

import android.text.TextUtils;
import android.util.Log;

import com.example.android.githubsearchwithnotifications.Api;
import com.example.android.githubsearchwithnotifications.utils.GitHubUtils;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GitHubSearchRepository {
    private static final String TAG = GitHubSearchRepository.class.getSimpleName();

    private MutableLiveData<List<GitHubRepo>> searchResults;
    private MutableLiveData<LoadingStatus> loadingStatus;

    private String currentQuery;
    private String currentSort;
    private String currentLanguage;
    private String currentUser;
    private boolean currentInName;
    private boolean currentInDescription;
    private boolean currentInReadme;

    private GitHubService gitHubService;

    public GitHubSearchRepository() {
        this.searchResults = new MutableLiveData<>();
        this.searchResults.setValue(null);

        this.loadingStatus = new MutableLiveData<>();
        this.loadingStatus.setValue(LoadingStatus.SUCCESS);

        this.gitHubService = Api.getInstance().getGitHubService();
    }

    public LiveData<List<GitHubRepo>> getSearchResults() {
        return this.searchResults;
    }

    public LiveData<LoadingStatus> getLoadingStatus() {
        return this.loadingStatus;
    }

    private boolean shouldExecuteSearch(String query) {
        return !TextUtils.equals(query, this.currentQuery)
                || this.getLoadingStatus().getValue() == LoadingStatus.ERROR;
    }

    private boolean shouldExecuteSearch(String query, String sort, String language, String user,
                                        boolean inName, boolean inDescription,boolean inReadme) {
        return !TextUtils.equals(query, this.currentQuery)
                || !TextUtils.equals(sort, this.currentSort)
                || !TextUtils.equals(language, this.currentLanguage)
                || !TextUtils.equals(user, this.currentUser)
                || this.currentInName != inName
                || this.currentInDescription != inDescription
                || this.currentInReadme != inReadme
                || this.getLoadingStatus().getValue() == LoadingStatus.ERROR;
    }

    public void loadSearchResults(String query) {
        if (this.shouldExecuteSearch(query)) {
            Log.d(TAG, "running new search for this query: " + query);
            this.currentQuery = query;
            this.executeSearch(query, null);
        } else {
            Log.d(TAG, "using cached search results for this query: " + query);
        }
    }

    public void loadSearchResults(String query, String sort, String language, String user,
                                  boolean inName, boolean inDescription, boolean inReadme) {
        if (this.shouldExecuteSearch(query, sort, language, user, inName, inDescription, inReadme)) {
            Log.d(TAG, "running new search for this query: " + query);
            this.currentQuery = query;
            this.currentSort = sort;
            this.currentLanguage = language;
            this.currentUser = user;
            this.currentInName = inName;
            this.currentInDescription = inDescription;
            this.currentInReadme = inReadme;
            String queryTerm = GitHubUtils.buildGitHubSearchQueryTerm(query, language, user,
                    inName, inDescription, inReadme);
            this.executeSearch(queryTerm, sort);
        } else {
            Log.d(TAG, "using cached search results for this query: " + query);
        }
    }

    private void executeSearch(String queryTerm, @Nullable String sort) {
        Call<GitHubSearchResults> results;
        if (sort != null) {
            results = this.gitHubService.searchRepos(queryTerm, sort);
        } else {
            results = this.gitHubService.searchRepos(queryTerm);
        }
        this.searchResults.setValue(null);
        this.loadingStatus.setValue(LoadingStatus.LOADING);
        results.enqueue(new Callback<GitHubSearchResults>() {
            @Override
            public void onResponse(Call<GitHubSearchResults> call, Response<GitHubSearchResults> response) {
                if (response.code() == 200) {
                    searchResults.setValue(response.body().items);
                    loadingStatus.setValue(LoadingStatus.SUCCESS);
                } else {
                    loadingStatus.setValue(LoadingStatus.ERROR);
                }
            }

            @Override
            public void onFailure(Call<GitHubSearchResults> call, Throwable t) {
                t.printStackTrace();
                loadingStatus.setValue(LoadingStatus.ERROR);
            }
        });
    }
}
