package com.example.a5geigir;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.Room;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.a5geigir.db.AppDatabase;
import com.example.a5geigir.db.Measurement;
import com.example.a5geigir.db.Signal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PeriodicReader extends Worker {

    private final SharedPreferences prefs;
    private final LocationController locationController;
    private ReaderManager readerManager;
    private AppDatabase db;
    private Context context;

    public PeriodicReader(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        db = Room.databaseBuilder(context, AppDatabase.class, "signalDB")
                .allowMainThreadQueries().build();
        readerManager = ReaderManager.getInstance(context);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        locationController = LocationController.getInstance(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("BackgroundMonitor", "worker reading...");
        measure();

        //Re-enqueue the same worker for a periodic work every 5 seconds
        /*OneTimeWorkRequest readingWorker = new OneTimeWorkRequest.Builder(PeriodicReader.class)
                .setInitialDelay(5, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork("reading",
                ExistingWorkPolicy.REPLACE, readingWorker);*/

        return Result.success();
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
    }

}
