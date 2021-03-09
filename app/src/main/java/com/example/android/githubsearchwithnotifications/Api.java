package com.example.android.githubsearchwithnotifications;

import com.example.android.githubsearchwithnotifications.data.GitHubService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * This is a singleton class for interacting with the GitHub API via a Retrofit GitHub service.
 */
public class Api {
    private static volatile Api INSTANCE;
    private static final String BASE_URL = "https://api.github.com";

    private GitHubService gitHubService;

    private Api() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.gitHubService = retrofit.create(GitHubService.class);
    }

    /**
     * Gets the singleton instance of the `Api` class.
     */
    public static Api getInstance() {
        if (INSTANCE == null) {
            synchronized (Api.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Api();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Gets the Retrofit GitHub service.
     */
    public GitHubService getGitHubService() {
        return this.gitHubService;
    }
}
