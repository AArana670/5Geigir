package com.example.a5geigir.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface MeasurementDao {

    @Query("SELECT * FROM measurement ORDER BY moment")
    List<Measurement> getMeasurements();

    @Query("SELECT * FROM measurement ORDER BY moment DESC LIMIT 1")
    Measurement getLastMeasurement();

    @Insert
    void insertMeasurement(Measurement measurement);

}
