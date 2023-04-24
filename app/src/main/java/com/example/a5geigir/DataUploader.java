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

import org.json.JSONObject;

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DataUploader {

    private static DataUploader instance = null;
    private final Context context;
    private final AppDatabase db;
    private final SharedPreferences prefs;
    public static final int SUCCESS = 0;
    public static final int CANCEL = 1;
    public static final int ERROR = 2;

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
        List<Signal> uploadingSignals = getUploadData();
        Log.d("DataTransfer", "Uploading " + uploadingSignals.size() + " elements");

        if (uploadingSignals.isEmpty()) {
            Log.d("DataTransfer", "No data to be uploaded");
            return DataUploader.CANCEL;
        }

        JSONObject jsonList = new JSONObject();

        try {
            jsonList.put("token", "dummy");

            for (Signal s : uploadingSignals) {
                JSONObject signalJson = new JSONObject();
                signalJson.put("dBm", s.dBm);
                signalJson.put("moment", s.moment);
                signalJson.put("ubiLat", s.ubiLat);
                signalJson.put("ubiLong", s.ubiLong);
                signalJson.put("cId", s.cId);
                signalJson.put("freq", s.freq);
                signalJson.put("type", s.type);

                jsonList.accumulate("signals", signalJson);
            }

        }catch (Exception e){
            e.printStackTrace();
            return DataUploader.ERROR;
        }

        String requestBody = jsonList.toString();

        new NetworkConnection(requestBody).execute("http://157.245.35.106/signal");

        String newMoment = Collections.max(uploadingSignals.stream().map(s -> s.moment).collect(Collectors.toList()));
        Log.d("DataTransfer", "New update moment " + newMoment);
        prefs.edit().putString("lastUploadedMoment", newMoment).commit();

        return DataUploader.SUCCESS;
    }

    private List<Signal> getUploadData(){
        String minMoment = "0000-00-00 00:00:00";
        String lastMoment = prefs.getString("lastUploadedMoment", minMoment);

        List<Signal> uploadingSignals = db.signalDao().getSignalsSince(lastMoment)
                /*.stream().filter(signal -> (signal.ubiLat != 0 && signal.ubiLong != 0))
                .collect(Collectors.toList())*/;

        return uploadingSignals;
    }

    public void setupService(){
        Calendar calendar = Calendar.getInstance();  //Set first trigger at 3:00 today, then repeat daily
        calendar.set(Calendar.HOUR_OF_DAY, 3);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);

        Log.d("BackgroundMonitor", "Upload alarm set: "+pi);
    }

}
