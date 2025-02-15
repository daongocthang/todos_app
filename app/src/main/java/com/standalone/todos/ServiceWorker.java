package com.standalone.todos;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ServiceWorker extends Worker {

    public ServiceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            final SyncManager syncManager = new SyncManager();
            syncManager.startSync();

            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }
}
