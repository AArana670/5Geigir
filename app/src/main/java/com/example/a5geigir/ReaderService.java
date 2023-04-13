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
    private HandlerThread handlerThread;
    private Handler handler;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotficationChannel();

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "signalDB")
                .allowMainThreadQueries().build();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        locationController = LocationController.getInstance(getApplicationContext());
        Log.d("BackgroundMonitor", "service reading...");

        Intent i = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

        Notification n = new NotificationCompat.Builder(this, "readingChannel")
                .setContentTitle("reading...")
                .setContentIntent(pi)
                .build();
        startForeground(1, n);

        handlerThread = new HandlerThread("ReadingThread");
        handlerThread.setDaemon(true);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        //enqueueReader();

        return START_STICKY;
    }

    /*private void enqueueReader() {
        Runnable runnable = new Runnable() {
            public void run() {
                measure();
                handler.postDelayed(runnable ,5000);
            }
        };
        handler.postDelayed(runnable, 5000);
    }*/

    private void createNotficationChannel() {

        NotificationChannel channel = new NotificationChannel(
                "readingChannel", "Foreground notification", NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

    }

    private void measure(){
        String moment = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

        double ubiLat = 0;
        double ubiLong = 0;
        if (!prefs.getBoolean("private_mode",false)) {
            Location ubi = locationController.getLastLocation();
            ubiLat = ubi.getLatitude();
            ubiLong = ubi.getLongitude();
        }

        int amount = (int) (Math.random()*10)+1;
        List<Integer> dBmList = new ArrayList<Integer>();

        for (int i = 0; i < amount; i++){
            int cId = i;
            int dBm = (int) ((Math.random()*-50)-20);
            String type = "5G";
            int freq = (int) ((Math.random()*400)+3400);

            Signal s = new Signal(cId, moment, ubiLat, ubiLong, dBm, type, freq);
            db.signalDao().insertSignal(s);
            dBmList.add(dBm);
            Log.d("SignalDB", "Added new; cId: "+s.cId+", moment: "+s.moment+", ubiLat: "+s.ubiLat+", ubiLong: "+ s.ubiLong+", dBm: "+s.dBm);
        }

        int meanDBm = (int)dBmList.stream().mapToDouble(a->a).average().getAsDouble();  //https://stackoverflow.com/a/31021873

        Measurement m = new Measurement(moment, meanDBm);

        db.measurementDao().insertMeasurement(m);

        //enqueueReader();
    }



    @Override
    public void onDestroy() {
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
