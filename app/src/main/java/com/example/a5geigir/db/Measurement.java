package com.example.a5geigir.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Measurement {  //works as a collection of all the signals registered at the same time

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "moment")
    public String moment;  //moment of the measurement

    @ColumnInfo(name = "meanDBm")
    public int meanDBm;  //mean of the dBm of all measured signals


    public Measurement(String moment, int meanDBm) {
        this.moment = moment;
        this.meanDBm = meanDBm;
    }
}
