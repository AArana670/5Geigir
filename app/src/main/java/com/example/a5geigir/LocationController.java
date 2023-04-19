package com.example.a5geigir;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationRequest;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationController {

    private final Context context;
    private static LocationController instance = null;
    private Location ubi;
    private final FusedLocationProviderClient fusedLocationClient;

    private LocationController(Context ctx) {

        context = ctx;

        ubi = new Location("");
        ubi.setLatitude(0);
        ubi.setLongitude(0);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        getLastLocation();

    }

    public static LocationController getInstance(Context context){
        if (instance == null)
            instance = new LocationController(context);
        return instance;
    }

    public Location getLastLocation(){

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY,null)
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    ubi = location;
                }
            });
        }

        return ubi;
    }

}
