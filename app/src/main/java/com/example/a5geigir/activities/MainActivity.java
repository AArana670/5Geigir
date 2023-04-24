package com.example.a5geigir.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.room.Room;

import com.example.a5geigir.DataUploader;
import com.example.a5geigir.DialogListener;
import com.example.a5geigir.ReaderListener;
import com.example.a5geigir.ReaderManager;
import com.example.a5geigir.PermissionDialog;
import com.example.a5geigir.R;
import com.example.a5geigir.TokenProvider;
import com.example.a5geigir.db.AppDatabase;
import com.example.a5geigir.db.Measurement;
import com.example.a5geigir.db.Signal;

public class MainActivity extends AppCompatActivity implements DialogListener, ReaderListener {


    private AppDatabase db;
    private ReaderManager readerManager;
    private TextView measurementTitle;
    private TextView measurementDBm;
    private TextView measurementMoment;
    private ProgressBar measurementBar;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().show();  //Show back the action bar after hiding it in PermissionActivity

        setContentView(R.layout.activity_main);

        readerManager = ReaderManager.getInstance(this);

        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "signalDB"
        ).allowMainThreadQueries().build();

        //https://developer.android.com/develop/ui/views/notifications/navigation#build_a_pendingintent_with_a_back_stack
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        DataUploader.getInstance(this).setupService();

        TokenProvider.initialize(this);  //Token request anticipated to have it ready when needed
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void displayState() {  //In case the thread was already running, opening the app will restore the display
        Button btn = findViewById(R.id.main_btn);

        if (readerManager.isRunning()){
            showCurrentMeasurement(/*null*/);
            btn.setText(R.string.main_measureStop);
        }else{
            showLastMeasurement();
            btn.setText(R.string.main_measureStart);
        }

    }

    public void jumpToSettings(){
        Intent i = new Intent(this, SettingsActivity.class);
        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    public void jumpToHistory(View v){
        Intent i = new Intent(this, HistoryActivity.class);
        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    public void switchState(View v){
        Button btn = findViewById(R.id.main_btn);

        if (readerManager.isRunning()) {  //If it was measuring, stop it
            stopMeasure();
            showLastMeasurement();
            btn.setText(R.string.main_measureStart);
            readerManager.removeListener(this);
        } else {  //If it was not measuring, start it
            if (!hasPermissions()) {  //Insist on the permission request
                DialogFragment dialog = new PermissionDialog();
                dialog.show(getSupportFragmentManager(), "permission_dialog");
            }else {  //If already has permissions
                startMeasure();
                showCurrentMeasurement(/*null*/);
                btn.setText(R.string.main_measureStop);
                readerManager.addListener(this);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startMeasure() {
        readerManager.run();
    }

    public void stopMeasure(){
        readerManager.stop();
    }

    private void showLastMeasurement() {
        //currentMeasurementPanel.setVisibility(View.INVISIBLE);
        if (db.measurementDao().getMeasurements().isEmpty()) {
            measurementTitle.setText(getText(R.string.main_getStarted));
            measurementMoment.setText("");
            measurementDBm.setText("");
            measurementBar.setProgress(measurementBar.getMin());
            setProgressColor(measurementBar.getMin());
        }else{
            measurementTitle.setText(getText(R.string.lastMeasurement_title));
            Signal s = db.signalDao().getLastSignal();
            measurementMoment.setText(s.moment);
            measurementDBm.setText(s.dBm + " dBm");
            measurementBar.setProgress(s.dBm);
            setProgressColor(s.dBm);
        }
    }

    private void showCurrentMeasurement() {
        measurementTitle.setText(getText(R.string.currentMeasurement_cont)+": "+ readerManager.getCount());
        Signal s = db.signalDao().getLastSignal();
        if (s != null) {
            measurementMoment.setText(s.moment);
            measurementDBm.setText(s.dBm+" dBm");
            measurementBar.setProgress(s.dBm);
            setProgressColor(s.dBm);
        }
    }

    private void setProgressColor(int p){
        if (p < -50) {
            measurementBar.getProgressDrawable().setColorFilter(  //https://stackoverflow.com/questions/2020882/how-to-change-progress-bars-progress-color-in-android
                    Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);
        }else if (p < -30){
            measurementBar.getProgressDrawable().setColorFilter(
                    Color.rgb(255,88,53), android.graphics.PorterDuff.Mode.SRC_IN);
        }else{
            measurementBar.getProgressDrawable().setColorFilter(
                    Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    public void clearDB(View v){  //Method for debugging purposes only
        db.signalDao().clearSignals();
        Log.d("SignalDB", "All signals have been deleted");
        showLastMeasurement();
    }

    @Override
    public void positiveAnswer() {  //PermissionDialog has accepted the permission request
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void negativeAnswer() {  //PermissionDialog has denied the permission request
        Toast.makeText(this, getString(R.string.dialog_permissions_cancelled), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNetworkUpdate(Measurement m) {  //A new signal has been created
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showCurrentMeasurement();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        readerManager.removeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        measurementTitle = findViewById(R.id.main_measurement_title);
        measurementDBm = findViewById(R.id.main_measurement_dBm);
        measurementMoment = findViewById(R.id.main_measurement_moment);
        measurementBar = findViewById(R.id.main_measurement_bar);

        readerManager = ReaderManager.getInstance(this);

        displayState();

        if (readerManager.isRunning())
            readerManager.addListener(this);
    }

    private boolean hasPermissions(){
        if (!(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED))

            return false;
        return true;
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
}