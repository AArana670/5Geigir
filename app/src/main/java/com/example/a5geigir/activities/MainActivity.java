package com.example.a5geigir.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.DialogFragment;
import androidx.room.Room;

import com.example.a5geigir.DialogListener;
import com.example.a5geigir.NetworkListener;
import com.example.a5geigir.NetworkManager;
import com.example.a5geigir.PermissionDialog;
import com.example.a5geigir.R;
import com.example.a5geigir.db.AppDatabase;
import com.example.a5geigir.db.Measurement;
import com.example.a5geigir.db.Signal;

public class MainActivity extends AppCompatActivity implements DialogListener, NetworkListener {


    private AppDatabase db;
    private NetworkManager networkManager;
    private NotificationCompat.Builder builder;
    NotificationManagerCompat compat;
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

        networkManager = NetworkManager.getInstance(this);

        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "signalDB"
        ).allowMainThreadQueries().build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){  //Create notification channel if the version is Oreo or greater
            NotificationChannel channel = new NotificationChannel("measuring", "measuring_notification", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        builder = new NotificationCompat.Builder(this, "measuring");
        builder.setContentTitle(getString(R.string.notification_measuring_title));  //https://developer.android.com/reference/android/app/Notification.Builder.html#public-methods
        builder.setContentText(getString(R.string.notification_measuring_desc));
        builder.setSmallIcon(R.mipmap.ic_launcher_adaptive_fore);
        builder.setAutoCancel(true);
        //builder.setOngoing(true);

        //https://developer.android.com/develop/ui/views/notifications/navigation#build_a_pendingintent_with_a_back_stack
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(resultPendingIntent);

        compat = NotificationManagerCompat.from(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void displayState() {  //In case the thread was already running, opening the app will restore the display
        Button btn = (Button) findViewById(R.id.main_btn);

        if (networkManager.isRunning()){
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
        Button btn = (Button) findViewById(R.id.main_btn);

        if (networkManager.isRunning()) {  //If it was measuring, stop it
            stopMeasure();
            showLastMeasurement();
            btn.setText(R.string.main_measureStart);
            networkManager.removeListener(this);
        } else {  //If it was not measuring, start it
            if (!hasPermissions()) {  //Insist on the permission request
                DialogFragment dialog = new PermissionDialog();
                dialog.show(getSupportFragmentManager(), "permission_dialog");
            }else {  //If already has permissions
                startMeasure();
                showCurrentMeasurement(/*null*/);
                btn.setText(R.string.main_measureStop);
                networkManager.addListener(this);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startMeasure() {
        networkManager.run();
    }

    public void stopMeasure(){
        networkManager.stop();
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
        measurementTitle.setText(getText(R.string.currentMeasurement_cont)+": "+networkManager.getCount());
        Signal s = db.signalDao().getLastSignal();
        if (s != null) {
            measurementMoment.setText(s.moment);
            measurementDBm.setText(s.dBm+"");
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
        networkManager.removeListener(this);
        if (networkManager.isRunning())
            buildNotification();
    }

    @Override
    protected void onResume() {
        super.onResume();

        measurementTitle = findViewById(R.id.main_measurement_title);
        measurementDBm = findViewById(R.id.main_measurement_dBm);
        measurementMoment = findViewById(R.id.main_measurement_moment);
        measurementBar = findViewById(R.id.main_measurement_bar);

        networkManager = NetworkManager.getInstance(this);

        displayState();

        if (networkManager.isRunning()) {
            networkManager.addListener(this);
            cancelNotification();
        }
    }

    private boolean hasPermissions(){
        if (!(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED))
            return false;
        return !prefs.getBoolean("silent_mode", false) || ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
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

    @SuppressLint("MissingPermission")//Every permission is checked in switchState
    private void buildNotification(){
        if (!prefs.getBoolean("silent_mode", false))
            compat.notify(1,builder.build());
    }

    private void cancelNotification(){
        compat.cancel(1);
    }
}