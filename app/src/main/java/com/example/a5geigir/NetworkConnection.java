package com.example.a5geigir;

import android.os.AsyncTask;
import android.util.Log;

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkConnection extends AsyncTask {

    private String data;

    public NetworkConnection(String data){
        this.data = data;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String direccion = "http://157.245.35.106/signal";
        HttpURLConnection urlConnection = null;
        try {

            URL destino = new URL(direccion);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);

            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");

            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(data);
            out.close();

            int statusCode = urlConnection.getResponseCode();
            Log.d("DataTransfer", "Result status: " + statusCode);
        } catch (Exception e){
            Log.d("DataTransfer", "Error during connection");
            e.printStackTrace();
        }
        return null;
    }

}
