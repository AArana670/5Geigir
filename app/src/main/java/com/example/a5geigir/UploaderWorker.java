package com.example.a5geigir;

import static com.example.a5geigir.R.string.notification_upload_id;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class UploaderWorker extends Worker {

    private Context context;

    public UploaderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        int resultCode = DataUploader.getInstance(context).upload();

        NotificationChannel channel = new NotificationChannel("uploadChannel", context.getString(notification_upload_id), NotificationManager.IMPORTANCE_MIN);
        Notification notification;
        if (resultCode == DataUploader.SUCCESS){
            //successful upload notification
            notification = new NotificationCompat.Builder(context, "uploadChannel")
                    .setContentTitle(context.getString(R.string.notification_upload_title_success))  //https://developer.android.com/reference/android/app/Notification.Builder.html#public-methods
                    .build();
        }else{
            //failed upload notification
            notification = new NotificationCompat.Builder(context, "uploadChannel")
                    .setContentTitle(context.getString(R.string.notification_upload_title_error))  //https://developer.android.com/reference/android/app/Notification.Builder.html#public-methods
                    .build();
        }

        return Result.success();
    }
}
