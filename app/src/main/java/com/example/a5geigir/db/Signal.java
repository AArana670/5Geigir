package com.example.a5geigir.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(primaryKeys = {"signalId","moment"})
public class Signal{

    @NonNull
    @ColumnInfo(name = "signalId")
    public int signalId;  //identifier of the instance, no meaningful information

    @ColumnInfo(name = "cId")
    public int cId;  //Physical cell identifier

    @NonNull
    @ColumnInfo(name = "moment")  //equal to the moment of the Measurement
    public String moment;  //Time of measurement in yyyy-MM-dd HH:mm:ss format

    @ColumnInfo(name = "ubiLat")
    public double ubiLat;  //Latitude of measurement position

    @ColumnInfo(name = "ubiLong")
    public double ubiLong;  //Longitude of measurement position

    @ColumnInfo(name = "dBm")
    public int dBm;  //Signal strength in dBm

    @NonNull
    @ColumnInfo(name = "type")
    public String type;  //Network type (NR, LTE, ...)

    @ColumnInfo(name = "frequency")
    public int freq;  //Radio frequency in MHz

    @ColumnInfo(name = "provider")
    public String provider;  //Telephony provider

    public Signal(@NonNull int signalId, int cId, @NonNull String moment, double ubiLat, double ubiLong, int dBm, String type, int freq, String provider) {
        this.signalId = signalId;
        this.cId = cId;
        this.moment = moment;
        this.ubiLat = ubiLat;
        this.ubiLong = ubiLong;
        this.dBm = dBm;
        this.type = type;
        this.freq = freq;
        this.provider = provider;
    }

}
