package com.example.a5geigir;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

public class PeriodicReader extends Worker {

    private ReaderManager readerManager;
    private Context context;

    public PeriodicReader(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        readerManager = ReaderManager.getInstance(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("WorkerMonitor", "reading...");


        OneTimeWorkRequest readingWorker = new OneTimeWorkRequest.Builder(PeriodicReader.class)
                .setInitialDelay(5, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork("reading",
                ExistingWorkPolicy.REPLACE, readingWorker);
        return Result.success();
    }
}
