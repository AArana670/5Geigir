package com.example.a5geigir.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.zip.Inflater;

public class MeasurementActivity extends AppCompatActivity {

    private int currentPos = 0;
    private List<Signal> signalList;
    private ProgressBar signalBar;

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
        //Fill the info about the signal
        Signal currentSignal = signalList.get(currentPos);

        TextView signalDate = findViewById(R.id.measurement_date);
        signalDate.setText(currentSignal.moment.split(" ")[0]);

        TextView signalTime = findViewById(R.id.measurement_time);
        signalTime.setText(currentSignal.moment.split(" ")[1]);

        TextView signalDBm = findViewById(R.id.measurement_dBm_value);
        signalDBm.setText(currentSignal.dBm+" dBm");

        signalBar = findViewById(R.id.measurement_dBm_bar);
        signalBar.setProgress(currentSignal.dBm);
        setProgressColor(currentSignal.dBm);

        TextView signalCId = findViewById(R.id.measurement_cId_value);
        signalCId.setText(currentSignal.cId+"");

        TextView signalUbiLat = findViewById(R.id.measurement_ubiLat_value);
        signalUbiLat.setText(currentSignal.ubiLat+"");

        TextView signalUbiLong = findViewById(R.id.measurement_ubiLong_value);
        signalUbiLong.setText(currentSignal.ubiLong+"");

        TextView signalFreq = findViewById(R.id.measurement_freq_value);
        signalFreq.setText(currentSignal.freq+"");

        TextView signalType = findViewById(R.id.measurement_type_value);
        signalType.setText(currentSignal.type+"");


        //Currently displaying signal page indicator
        LinearLayout pageIndicator = findViewById(R.id.measurement_pageIndicator);
        pageIndicator.removeAllViews();
        View dot;
        LinearLayout.LayoutParams params;
        for (int i = 0; i < signalList.size(); i++){
            //20dp x 20dp for each dot, LayoutParams receives pixels
            params = new LinearLayout.LayoutParams((int) (20*getResources().getDisplayMetrics().density), (int) (20*getResources().getDisplayMetrics().density));
            dot = new View(this);
            if (i == currentPos)
                dot.setBackgroundResource(R.drawable.selected_dot);
            else
                dot.setBackgroundResource(R.drawable.default_dot);
            dot.setLayoutParams(params);
            pageIndicator.addView(dot);
        }
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