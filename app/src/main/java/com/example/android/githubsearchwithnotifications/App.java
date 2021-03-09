package com.example.android.githubsearchwithnotifications;

import android.app.Application;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class App extends Application {
    private static final String CHECK_STARS_WORK_NAME = "checkStars";
    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        startCheckStarsWorker();
    }

    private void startCheckStarsWorker() {
        Log.d(TAG, "enqueueing check stars worker");
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                CheckRepoStarsWorker.class,
                15,
                TimeUnit.MINUTES
        ).setConstraints(constraints).build();

        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                        CHECK_STARS_WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        workRequest
                );
    }
}
