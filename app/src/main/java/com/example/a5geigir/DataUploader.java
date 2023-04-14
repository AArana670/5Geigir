package com.example.a5geigir;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.example.a5geigir.db.AppDatabase;
import com.example.a5geigir.db.Signal;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

public class DataUploader {

    private static DataUploader instance = null;
    private Context context;
    private AppDatabase db;
    private SharedPreferences prefs;
    public static int SUCCESS = 0;
    public static int ERROR = 1;

    private DataUploader(Context context){
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        db = Room.databaseBuilder(
                context,
                AppDatabase.class,
                "signalDB"
        ).allowMainThreadQueries().build();
    }

    public static DataUploader getInstance(Context context){
        if (instance == null)
            instance = new DataUploader(context);
        return instance;
    }

    public int upload(){

        String minMoment = "0000-00-00 00:00:00";
        String lastMoment = prefs.getString("lastUploadedMoment", minMoment);

        List<Signal> uploadingSignals = db.signalDao().getSignalsSince(lastMoment);
        Log.d("DataTransfer", "Uploading " + uploadingSignals.size() + " elements since " + lastMoment);

        if (!uploadingSignals.isEmpty()) {
            String newMoment = Collections.max(uploadingSignals.stream().map(s -> s.moment).collect(Collectors.toList()));
            Log.d("DataTransfer", "New update moment " + newMoment);
            prefs.edit().putString("lastUploadedMoment", newMoment).commit();
        }

        return DataUploader.SUCCESS;
    }

    public void setupService(){
        Calendar uploadTime= Calendar.getInstance();
        uploadTime.set(Calendar.HOUR_OF_DAY, 3);
        uploadTime.set(Calendar.MINUTE, 0);
        uploadTime.set(Calendar.SECOND, 0);

        Intent i= new Intent(context, UploaderService.class);
        PendingIntent pi= PendingIntent.getActivity(context,0, i, PendingIntent.FLAG_NO_CREATE);

        AlarmManager alarmManager= (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, uploadTime.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);

        Log.d("BackgroundMonitor", "Upload alarm set");
    }

}
