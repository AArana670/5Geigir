package com.example.a5geigir;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.a5geigir.activities.MainActivity;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BackgroundMonitor", "Alarm triggered");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int uploadResult = DataUploader.getInstance(context).upload();

        if (uploadResult != DataUploader.CANCEL && !prefs.getBoolean("silent_mode", false)) {
            if (uploadResult == DataUploader.SUCCESS)
                displayNotification(context, context.getString(R.string.notification_upload_title_success));
            else  //uploadResult == DataUploader.FAILURE
                displayNotification(context, context.getString(R.string.notification_upload_title_error));
        }

    }

    public void displayNotification(Context context, String msg){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    "uploadingChannel",
                    context.getString(R.string.notification_upload_id),
                    NotificationManager.IMPORTANCE_HIGH);
            mChannel.enableVibration(false);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "uploadingChannel")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(msg);

        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        notificationManager.notify(2, builder.build());
    }
}
