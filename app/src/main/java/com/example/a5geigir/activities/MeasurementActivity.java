package com.example.a5geigir.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.a5geigir.R;
import com.example.a5geigir.db.AppDatabase;
import com.example.a5geigir.db.Signal;

import java.util.List;

public class MeasurementActivity extends AppCompatActivity {

    private int currentPos = 0;
    private List<Signal> signalList;
    private TextView signalDate;
    private TextView signalTime;
    private TextView signalDBm;
    private ProgressBar signalBar;
    private TextView signalCId;
    private TextView signalUbiLat;
    private TextView signalUbiLong;
    private TextView signalFreq;
    private TextView signalType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_measurement);


        Bundle extras = getIntent().getExtras();
        String moment = extras.getString("moment");

        AppDatabase db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "signalDB"
        ).allowMainThreadQueries().build();

        signalList = db.signalDao().getSignalsAt(moment);  //Get the data of the selected measurement
        showSignal();
    }

    private void showSignal() {
        Signal currentSignal = signalList.get(currentPos);

        signalDate = (TextView) findViewById(R.id.measurement_date);
        signalDate.setText(currentSignal.moment.split(" ")[0]);

        signalTime = (TextView) findViewById(R.id.measurement_time);
        signalTime.setText(currentSignal.moment.split(" ")[1]);

        signalDBm = (TextView) findViewById(R.id.measurement_dBm_value);
        signalDBm.setText(currentSignal.dBm+"");

        signalBar = (ProgressBar) findViewById(R.id.measurement_dBm_bar);
        signalBar.setProgress(currentSignal.dBm);
        setProgressColor(currentSignal.dBm);

        signalCId = (TextView) findViewById(R.id.measurement_cId_value);
        signalCId.setText(currentSignal.cId+"");

        signalUbiLat = (TextView) findViewById(R.id.measurement_ubiLat_value);
        signalUbiLat.setText(currentSignal.ubiLat+"");

        signalUbiLong = (TextView) findViewById(R.id.measurement_ubiLong_value);
        signalUbiLong.setText(currentSignal.ubiLong+"");

        signalFreq = (TextView) findViewById(R.id.measurement_freq_value);
        signalFreq.setText(currentSignal.freq+"");

        signalType = (TextView) findViewById(R.id.measurement_type_value);
        signalType.setText(currentSignal.type+"");
    }

    public void showPrev(View v){
        if(currentPos > 0)
            currentPos--;
        showSignal();
    }

    public void showNext(View v){
        if(currentPos < signalList.size()-1)
            currentPos++;
        showSignal();
    }

    private void setProgressColor(int p){
        if (p < -50) {
            signalBar.getProgressDrawable().setColorFilter(  //https://stackoverflow.com/questions/2020882/how-to-change-progress-bars-progress-color-in-android
                    Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);
        }else if (p < -30){
            signalBar.getProgressDrawable().setColorFilter(
                    Color.rgb(255,88,53), android.graphics.PorterDuff.Mode.SRC_IN);
        }else{
            signalBar.getProgressDrawable().setColorFilter(
                    Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    public void jumpToSettings(){
        Intent i = new Intent(this, SettingsActivity.class);
        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_settings:
                jumpToSettings();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("position", currentPos);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        currentPos = savedInstanceState.getInt("position");
        showSignal();
        super.onRestoreInstanceState(savedInstanceState);
    }
}