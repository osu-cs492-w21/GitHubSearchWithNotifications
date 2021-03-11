package com.example.android.githubsearchwithnotifications;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.githubsearchwithnotifications.data.BookmarkedReposRepository;
import com.example.android.githubsearchwithnotifications.data.GitHubRepo;
import com.example.android.githubsearchwithnotifications.data.GitHubService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Call;
import retrofit2.Response;

public class CheckRepoStarsWorker extends Worker {
    private static final String TAG = CheckRepoStarsWorker.class.getSimpleName();
    private static final String STARS_NOTIFICATION_GROUP = "starsNotificationGroup";

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
        Log.d(TAG, "running check stars worker");
        List<GitHubRepo> bookmarkedRepos = this.bookmarkedReposRepository.getAllBookmarkedReposSync();
        ArrayList<GitHubRepo> updatedRepos = new ArrayList<>();
        if (bookmarkedRepos != null) {
            for (GitHubRepo bookmarkedRepo : bookmarkedRepos) {
                GitHubRepo updatedRepo = fetchRepoByUrlSync(bookmarkedRepo.url);
                if (updatedRepo != null) {
                    Log.d(TAG, updatedRepo.fullName + " updated with stars: " + updatedRepo.stars);
                    this.bookmarkedReposRepository.updateBookmarkedRepo(updatedRepo);
                    if (updatedRepo.stars >= bookmarkedRepo.stars) {
                        updatedRepos.add(updatedRepo);
                    }
                }
            }
        }
        sendNotifications(updatedRepos);
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

    private void sendNotifications(@NonNull List<GitHubRepo> repos) {
        Context context = getApplicationContext();
        for (GitHubRepo repo : repos) {
            sendIndividualNotification(repo, context);
        }
        if (repos.size() > 1) {
            sendSummaryNotification(repos, context);
        }
    }

    private void sendIndividualNotification(GitHubRepo repo, Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                context.getString(R.string.stars_notification_channel)
        );
        builder.setSmallIcon(R.drawable.ic_github)
                .setContentTitle(context.getString(
                        R.string.stars_notification_title, repo.fullName
                ))
                .setContentText(context.getString(
                        R.string.stars_notification_text, repo.fullName, repo.stars
                ))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroup(STARS_NOTIFICATION_GROUP);

        Intent intent = new Intent(context, RepoDetailActivity.class);
        intent.putExtra(RepoDetailActivity.EXTRA_GITHUB_REPO, repo);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        builder.setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(
                repo.fullName.hashCode(),
                builder.build()
        );
    }

    private void sendSummaryNotification(List<GitHubRepo> repos, Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                context.getString(R.string.stars_notification_channel)
        );
        builder.setSmallIcon(R.drawable.ic_github);

        ArrayList<String> repoNames = new ArrayList<>();
        for (GitHubRepo repo : repos) {
            repoNames.add(repo.fullName);
        }

        builder.setContentText(TextUtils.join(", ", repoNames))
            .setContentTitle(context.getString(
                    R.string.stars_notification_summary_title, repos.size()
            ));

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (GitHubRepo repo : repos) {
            inboxStyle.addLine(context.getString(
                    R.string.stars_notification_text, repo.fullName, repo.stars
            ));
        }
        builder.setStyle(inboxStyle);

        Intent intent = new Intent(context, BookmarkedRepos.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        builder.setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setGroup(STARS_NOTIFICATION_GROUP)
                .setGroupSummary(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, builder.build());
    }
}
