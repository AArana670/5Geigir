package com.example.a5geigir.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface MeasurementDao {

    @Query("SELECT * FROM measurement")
    List<Measurement> getMeasurements();

    @Insert
    void insertMeasurement(Measurement measurement);

}
