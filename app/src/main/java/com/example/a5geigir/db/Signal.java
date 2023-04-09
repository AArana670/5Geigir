package com.example.a5geigir.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(primaryKeys = {"cId","moment"})
public class Signal{

    @NonNull
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

    @ColumnInfo(name = "type")
    public String type;  //Network type (4G, 5G, ...)

    @ColumnInfo(name = "frequency")
    public int freq;  //Radio frequency in MHz

    public Signal(@NonNull int cId, @NonNull String moment, double ubiLat, double ubiLong, int dBm, String type, int freq) {
        this.cId = cId;
        this.moment = moment;
        this.ubiLat = ubiLat;
        this.ubiLong = ubiLong;
        this.dBm = dBm;
        this.type = type;
        this.freq = freq;
    }

}
