package com.example.a5geigir;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.example.a5geigir.db.AppDatabase;
import com.example.a5geigir.db.Signal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CellReader {

    private static CellReader instance = null;
    private LocationController locationController;
    private SharedPreferences prefs;

    private CellReader(Context context){
        locationController = LocationController.getInstance(context);

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static CellReader getInstance(Context context){
        if(instance == null)
            instance = new CellReader(context);
        return instance;
    }

    public List<Signal> readCells(){
        String moment = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

        double ubiLat = 0;
        double ubiLong = 0;
        if (!prefs.getBoolean("private_mode",false)) {
            Location ubi = locationController.getLastLocation();
            ubiLat = ubi.getLatitude();
            ubiLong = ubi.getLongitude();
        }

        int amount = (int) (Math.random()*10)+1;

        List<Signal> readSignals = new ArrayList<Signal>();

        for (int i = 0; i < amount; i++){
            int cId = i;
            int dBm = (int) ((Math.random()*-50)-20);
            String type = "NR";
            int freq = (int) ((Math.random()*400)+3400);
            String provider = "E Corp";

            Signal s = new Signal(i, cId, moment, ubiLat, ubiLong, dBm, type, freq, provider);

            readSignals.add(s);
        }

        return readSignals;
    }

}
