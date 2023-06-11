package com.example.a5geigir;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.example.a5geigir.activities.MainActivity;
import com.example.a5geigir.db.AppDatabase;
import com.example.a5geigir.db.Measurement;
import com.example.a5geigir.db.Signal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReaderService extends Service {

    private AppDatabase db;
    private SharedPreferences prefs;
    private LocationController locationController;
    private ReaderManager manager;

    private HandlerThread handlerThread;
    private Handler handler;
    private Runnable runnable;


    @Override
    public void onCreate() {
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "signalDB")
                .allowMainThreadQueries().build();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        locationController = LocationController.getInstance(getApplicationContext());
        manager = ReaderManager.getInstance(getApplicationContext());

        handlerThread = new HandlerThread("ReadingThread");
        handlerThread.setDaemon(true);
        handlerThread.start();

        handler = new Handler(handlerThread.getLooper());

        runnable = new Runnable() {
            @Override
            public void run() {
                manager.measure();
                int delay = (int) (Float.parseFloat(prefs.getString("speed", "1"))*1000);
                handler.postDelayed(runnable, delay);  //delay in millis
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BackgroundMonitor", "service started");

        Intent i = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_MUTABLE);

        //Necessary notification, cannot be silenced from preferences
        createNotficationChannel();
        Notification notification = new NotificationCompat.Builder(this, "readingChannel")
                .setContentIntent(pi)
                .setContentTitle(getString(R.string.notification_measuring_title))  //https://developer.android.com/reference/android/app/Notification.Builder.html#public-methods
                .setContentText(getString(R.string.notification_measuring_desc))
                .build();
        startForeground(1, notification);

        handler.removeCallbacks(runnable);
        handler.post(runnable);

        return START_STICKY;
    }

    private void createNotficationChannel() {

        NotificationChannel channel = new NotificationChannel(
                "readingChannel", getString(R.string.notification_measuring_id), NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        stopSelf();

        handler.removeCallbacks(runnable);
        handlerThread.quitSafely();

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
