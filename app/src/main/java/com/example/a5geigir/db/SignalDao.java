package com.example.a5geigir.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SignalDao {

    @Query("SELECT * FROM signal ORDER BY moment")
    List<Signal> getSignals();

    @Query("SELECT * FROM signal WHERE moment > :moment")
    List<Signal> getSignalsSince(String moment);  //Currently unused feature

    @Query("SELECT * FROM signal WHERE moment = :moment")
    List<Signal> getSignalsAt(String moment);

    @Query("SELECT * FROM signal ORDER BY moment DESC LIMIT 1")
    Signal getLastSignal();

    @Insert
    void insertSignal(Signal signal);

    @Query("DELETE FROM signal WHERE moment < :moment")
    void deleteSignalsUntil(String moment);  //Currently unused feature

    @Query("DELETE FROM signal")
    void clearSignals();  //Method for debugging purposes only

}