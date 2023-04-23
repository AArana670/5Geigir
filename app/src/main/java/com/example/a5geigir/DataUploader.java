package com.example.a5geigir;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
            if (prefs.getBoolean("anonymous_mode", false))
                jsonList.put("token", "");  //token is sent empty to upload anonymously
            else {
                //String token = TokenProvider.getInstance(context).getToken();
                jsonList.put("token", "");
            }

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

    private int post(String data){
        String direccion = "http://157.245.35.106/signal";
        HttpURLConnection urlConnection = null;
        try {

            URL destino = new URL(direccion);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);

            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");

            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(data);
            out.close();

            int statusCode = urlConnection.getResponseCode();
            Log.d("DataTransfer", "Result status: " + statusCode);
        } catch (Exception e){
            Log.d("DataTransfer", "Error during connection");
            e.printStackTrace();
        }

        return 200;
    }

    public void setupService(){
        Calendar uploadTime= Calendar.getInstance();  //Initial event time
        uploadTime.set(Calendar.HOUR_OF_DAY, 17);
        uploadTime.set(Calendar.MINUTE, 45);
        uploadTime.set(Calendar.SECOND, 0);

        Intent i= new Intent(context, UploaderService.class);
        PendingIntent pi= PendingIntent.getActivity(context,0, i, PendingIntent.FLAG_NO_CREATE);

        AlarmManager alarmManager= (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, uploadTime.getTimeInMillis(), pi);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, uploadTime.getTimeInMillis(),
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);

        Log.d("BackgroundMonitor", "Upload alarm set: "+pi);
    }

}
