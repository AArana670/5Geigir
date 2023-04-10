package com.example.a5geigir;

import android.util.Log;

import java.util.zip.DataFormatException;

public class DataUploader {

    private static DataUploader instance = null;
    public static int SUCCESS = 0;
    public static int ERROR = 1;

    private DataUploader(){
    }

    public static DataUploader getInstance(){
        if (instance == null)
            instance = new DataUploader();
        return instance;
    }

    public int upload(){

        Log.d("DataTransfer", "Uploading data...");

        return DataUploader.SUCCESS;
    }

}
