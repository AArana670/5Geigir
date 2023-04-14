package com.example.a5geigir;

import static com.example.a5geigir.R.string.notification_upload_id;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import androidx.work.WorkerParameters;

public class UploaderService extends Service {

    private SharedPreferences prefs;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int resultCode = DataUploader.getInstance(this).upload();

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!prefs.getBoolean("silent_mode", false)) {

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("uploadChannel", getString(notification_upload_id), NotificationManager.IMPORTANCE_MIN);
            Notification notification;

            if (resultCode == DataUploader.SUCCESS) {
                //successful upload notification
                notification = new NotificationCompat.Builder(this, "uploadChannel")
                        .setContentTitle(getString(R.string.notification_upload_title_success))  //https://developer.android.com/reference/android/app/Notification.Builder.html#public-methods
                        .setAutoCancel(true)
                        .build();
            } else {
                //failed upload notification
                notification = new NotificationCompat.Builder(this, "uploadChannel")
                        .setContentTitle(getString(R.string.notification_upload_title_error))  //https://developer.android.com/reference/android/app/Notification.Builder.html#public-methods
                        .setAutoCancel(true)
                        .build();
            }

            manager.notify(2, notification);
        }
        Log.d("BackgroundMonitor", "Upload service executed");

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
