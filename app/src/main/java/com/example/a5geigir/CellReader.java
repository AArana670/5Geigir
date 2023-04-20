package com.example.a5geigir;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellIdentityTdscdma;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoTdscdma;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.CellSignalStrengthTdscdma;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
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
    private Context context;
    private LocationController locationController;
    private SharedPreferences prefs;
    private TelephonyManager telephonyManager;

    private CellReader(Context context) {
        this.context = context;
        locationController = LocationController.getInstance(context);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public static CellReader getInstance(Context context) {
        if (instance == null)
            instance = new CellReader(context);
        return instance;
    }

    @SuppressLint("NewApi")
    public List<Signal> readCells() {

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Reading", "Trying to read cells without location permission", new Exception());
            return null;
        }


        List<CellInfo> cellList = telephonyManager.getAllCellInfo();

        List<Signal> readSignals = new ArrayList<>();
        Signal currentSignal = null;

        for (CellInfo c : cellList){

            int idx = cellList.indexOf(c);

            if (c instanceof CellInfoNr){
                currentSignal = buildSignalNr(c, idx);

            } else if (c instanceof CellInfoLte){
                currentSignal = buildSignalLte(c, idx);

            } else if (c instanceof CellInfoGsm){
                currentSignal = buildSignalGsm(c, idx);

            } else if (c instanceof CellInfoWcdma){
                currentSignal = buildSignalWcdma(c, idx);

            } else if (c instanceof CellInfoTdscdma){
                currentSignal = buildSignalTdscdma(c, idx);
            }

            if (currentSignal != null)
                readSignals.add(currentSignal);
        }

        return readSignals;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private Signal buildSignalNr(CellInfo c, int idx) {
        String moment = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

        double ubiLat = 0;
        double ubiLong = 0;
        if (!prefs.getBoolean("private_mode", false)) {
            Location ubi = locationController.getLastLocation();
            ubiLat = ubi.getLatitude();
            ubiLong = ubi.getLongitude();
        }

        CellIdentityNr cId = (CellIdentityNr) c.getCellIdentity();
        CellSignalStrengthNr cSS = (CellSignalStrengthNr) c.getCellSignalStrength();

        int pCId = cId.getPci();
        int dBm = cSS.getDbm();
        String type = "NR";
        int freq = cId.getNrarfcn();
        String provider = (String) cId.getOperatorAlphaLong();

        Signal s = new Signal(idx, pCId, moment, ubiLat, ubiLong, dBm, type, freq, provider);

        return s;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private Signal buildSignalLte(CellInfo c, int idx) {
        String moment = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

        double ubiLat = 0;
        double ubiLong = 0;
        if (!prefs.getBoolean("private_mode", false)) {
            Location ubi = locationController.getLastLocation();
            ubiLat = ubi.getLatitude();
            ubiLong = ubi.getLongitude();
        }

        CellIdentityLte cId = (CellIdentityLte) c.getCellIdentity();
        CellSignalStrengthLte cSS = (CellSignalStrengthLte) c.getCellSignalStrength();

        int pCId = cId.getPci();
        int dBm = cSS.getDbm();
        String type = "LTE";
        int freq = cId.getEarfcn();
        String provider = (String) cId.getOperatorAlphaLong();

        Signal s = new Signal(idx, pCId, moment, ubiLat, ubiLong, dBm, type, freq, provider);

        return s;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private Signal buildSignalGsm(CellInfo c, int idx) {
        String moment = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

        double ubiLat = 0;
        double ubiLong = 0;
        if (!prefs.getBoolean("private_mode", false)) {
            Location ubi = locationController.getLastLocation();
            ubiLat = ubi.getLatitude();
            ubiLong = ubi.getLongitude();
        }

        CellIdentityGsm cId = (CellIdentityGsm) c.getCellIdentity();
        CellSignalStrengthGsm cSS = (CellSignalStrengthGsm) c.getCellSignalStrength();

        int pCId = cId.getCid();
        int dBm = cSS.getDbm();
        String type = "GSM";
        int freq = cId.getArfcn();
        String provider = (String) cId.getOperatorAlphaLong();

        Signal s = new Signal(idx, pCId, moment, ubiLat, ubiLong, dBm, type, freq, provider);

        return s;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private Signal buildSignalWcdma(CellInfo c, int idx) {
        String moment = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

        double ubiLat = 0;
        double ubiLong = 0;
        if (!prefs.getBoolean("private_mode", false)) {
            Location ubi = locationController.getLastLocation();
            ubiLat = ubi.getLatitude();
            ubiLong = ubi.getLongitude();
        }

        CellIdentityWcdma cId = (CellIdentityWcdma) c.getCellIdentity();
        CellSignalStrengthWcdma cSS = (CellSignalStrengthWcdma) c.getCellSignalStrength();

        int pCId = cId.getCid();
        int dBm = cSS.getDbm();
        String type = "WCDMA";
        int freq = cId.getUarfcn();
        String provider = (String) cId.getOperatorAlphaLong();

        Signal s = new Signal(idx, pCId, moment, ubiLat, ubiLong, dBm, type, freq, provider);

        return s;
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    private Signal buildSignalTdscdma(CellInfo c, int idx) {
        String moment = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

        double ubiLat = 0;
        double ubiLong = 0;
        if (!prefs.getBoolean("private_mode", false)) {
            Location ubi = locationController.getLastLocation();
            ubiLat = ubi.getLatitude();
            ubiLong = ubi.getLongitude();
        }

        CellIdentityTdscdma cId = (CellIdentityTdscdma) c.getCellIdentity();
        CellSignalStrengthTdscdma cSS = (CellSignalStrengthTdscdma) c.getCellSignalStrength();

        int pCId = cId.getCid();
        int dBm = cSS.getDbm();
        String type = "TDSCDMA";
        int freq = cId.getUarfcn();
        String provider = (String) cId.getOperatorAlphaLong();

        Signal s = new Signal(idx, pCId, moment, ubiLat, ubiLong, dBm, type, freq, provider);

        return s;
    }
}
