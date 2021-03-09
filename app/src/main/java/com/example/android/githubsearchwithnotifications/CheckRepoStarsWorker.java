package com.example.android.githubsearchwithnotifications;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.android.githubsearchwithnotifications.data.BookmarkedReposRepository;
import com.example.android.githubsearchwithnotifications.data.GitHubRepo;
import com.example.android.githubsearchwithnotifications.data.GitHubService;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Call;
import retrofit2.Response;

public class CheckRepoStarsWorker extends Worker {
    private static final String TAG = CheckRepoStarsWorker.class.getSimpleName();

    private BookmarkedReposRepository bookmarkedReposRepository;
    private GitHubService gitHubService;

    public CheckRepoStarsWorker(@NonNull Context context, @NonNull WorkerParameters parameters) {
        super(context, parameters);
        this.bookmarkedReposRepository = new BookmarkedReposRepository((Application) getApplicationContext());
        this.gitHubService = Api.getInstance().getGitHubService();
    }

    @NonNull
    @Override
    public Result doWork() {
        List<GitHubRepo> bookmarkedRepos = this.bookmarkedReposRepository.getAllBookmarkedReposSync();
        if (bookmarkedRepos != null) {
            for (GitHubRepo bookmarkedRepo : bookmarkedRepos) {
                GitHubRepo updatedRepo = fetchRepoByUrlSync(bookmarkedRepo.url);
                if (updatedRepo != null) {
                    Log.d(TAG, updatedRepo.fullName + " updated with stars: " + updatedRepo.stars);
                    this.bookmarkedReposRepository.updateBookmarkedRepo(updatedRepo);
                }
            }
        }
        return Result.success();
    }

    private GitHubRepo fetchRepoByUrlSync(String url) {
        Call<GitHubRepo> results = this.gitHubService.getRepoByUrl(url);
        Response<GitHubRepo> response;
        try {
            response = results.execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (response.code() == 200) {
            return response.body();
        } else {
            Log.d(TAG, "error fetching repo with URL: " + response.toString());
            return null;
        }
    }
}
