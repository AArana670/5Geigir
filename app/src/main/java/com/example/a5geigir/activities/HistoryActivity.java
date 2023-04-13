package com.example.a5geigir.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.a5geigir.DataUploader;
import com.example.a5geigir.ListAdapter;
import com.example.a5geigir.ReaderManager;
import com.example.a5geigir.R;
import com.example.a5geigir.db.AppDatabase;
import com.example.a5geigir.db.Measurement;
import com.example.a5geigir.db.Signal;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    RecyclerView signalRecyler;
    ListAdapter listAdapter;
    private AppDatabase db;
    private ReaderManager readerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        createList();
    }

    private void createList() {
        signalRecyler = findViewById(R.id.history_list);
        signalRecyler.setLayoutManager(new LinearLayoutManager(this));

        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "signalDB"
        ).allowMainThreadQueries().build();

        List<Measurement> measurementList = db.measurementDao().getMeasurements();

        listAdapter = new ListAdapter(measurementList,this);

        signalRecyler.setAdapter(listAdapter);
    }

    public void jumpToSettings(){
        Intent i = new Intent(this, SettingsActivity.class);
        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    public void uploadData(){
        DataUploader dataUploader = DataUploader.getInstance();

        int status = dataUploader.upload();
        if (status == DataUploader.SUCCESS)
            Toast.makeText(this, getText(R.string.hist_dataUploaded_success), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, getText(R.string.hist_datUploaded_error), Toast.LENGTH_SHORT).show();
        createList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_settings:
                jumpToSettings();
                break;
            case R.id.menu_refresh:
                createList();
                break;
            case R.id.menu_upload:
                uploadData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}