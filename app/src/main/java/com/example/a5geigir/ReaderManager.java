package com.example.a5geigir;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import androidx.preference.PreferenceManager;
import androidx.room.Room;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.a5geigir.db.AppDatabase;
import com.example.a5geigir.db.Measurement;
import com.example.a5geigir.db.Signal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReaderManager {

    private static ReaderManager instance = null;
    private Thread reader;
    private final ArrayList<NetworkListener> listeners = new ArrayList<NetworkListener>();
    private final SharedPreferences prefs;
    private final AppDatabase db;
    private final Context context;
    private boolean running = false;
    private int counter = 0;
    private final LocationController locationController;
    private Intent readingIntent;


    private ReaderManager(Context context) {
        this.context = context;
        createReader();

        locationController = LocationController.getInstance(context);

        db = Room.databaseBuilder(
                context,
                AppDatabase.class,
                "signalDB"
        ).allowMainThreadQueries().build();

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private void createReader() {
        reader = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(5000);
                        Measurement m = measure();
                        counter++;
                        notifyListeners(null);
                    }
                } catch (InterruptedException e) {
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private Measurement measure() {
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

        return m;
    }

    public static ReaderManager getInstance(Context context){
        if (instance == null){
            instance = new ReaderManager(context);
        }
        return instance;
    }

    public void addListener(NetworkListener l){
        listeners.add(l);
    }

    public void removeListener(NetworkListener l){
        listeners.remove(l);
    }

    private void notifyListeners(Measurement m) {
        for (NetworkListener l : listeners){
            l.onNetworkUpdate(m);
        }
    }

    public boolean isRunning(){
        return running;
    }

    public int getCount(){
        return counter;
    }

    public void run(){
        createReader();
        reader.start();
        running = true;
        counter = 0;

        /*PeriodicWorkRequest readingWorker = new PeriodicWorkRequest.Builder(PeriodicReader.class,
                5, TimeUnit.SECONDS).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("reading",
                ExistingPeriodicWorkPolicy.REPLACE, readingWorker);*/
        readingIntent = new Intent(context, ReaderService.class);
        context.startForegroundService(readingIntent);

    }

    public void stop(){
        reader.interrupt();
        running = false;
        //WorkManager.getInstance(context).cancelUniqueWork("reading");
        context.stopService(readingIntent);
    }
}
