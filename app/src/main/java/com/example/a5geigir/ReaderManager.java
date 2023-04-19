package com.example.a5geigir;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.example.a5geigir.db.AppDatabase;
import com.example.a5geigir.db.Measurement;
import com.example.a5geigir.db.Signal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReaderManager {

    private static ReaderManager instance = null;
    private Thread reader;
    private final ArrayList<ReaderListener> listeners = new ArrayList<ReaderListener>();
    private final SharedPreferences prefs;
    private final AppDatabase db;
    private final Context context;
    private boolean running = false;
    private int counter = 0;
    private Intent readingIntent;


    private ReaderManager(Context context) {
        this.context = context;
        createReader();

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

        @SuppressLint({"NewApi", "LocalSuppress"})
        List<Signal> signalList = CellReader.getInstance(context).readCells();

        for (Signal s : signalList){
            Log.d("SignalDB", "Added new; signalId: "+s.signalId+", cId: "+s.cId+", moment: "+s.moment+", ubiLat: "+s.ubiLat+", ubiLong: "+ s.ubiLong+", dBm: "+s.dBm+", type: "+s.type+", provider: "+s.provider);
            db.signalDao().insertSignal(s);
        }

        int meanDBm = (int)signalList.stream().mapToDouble(s->s.dBm).average().getAsDouble();  //https://stackoverflow.com/a/31021873

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

    public void addListener(ReaderListener l){
        listeners.add(l);
    }

    public void removeListener(ReaderListener l){
        listeners.remove(l);
    }

    private void notifyListeners(Measurement m) {
        for (ReaderListener l : listeners){
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
        readingIntent = new Intent(context, ReaderService.class);
        context.startForegroundService(readingIntent);

    }

    public void stop(){
        reader.interrupt();
        running = false;
        context.stopService(readingIntent);
    }
}
