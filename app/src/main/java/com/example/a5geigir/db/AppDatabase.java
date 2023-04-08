package com.example.a5geigir.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {Signal.class, Measurement.class},
        version = 1
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SignalDao signalDao();
    public abstract MeasurementDao measurementDao();
}