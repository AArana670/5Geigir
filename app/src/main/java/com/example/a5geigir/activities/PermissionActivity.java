package com.example.a5geigir.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.example.a5geigir.R;

import java.util.Locale;

public class PermissionActivity extends AppCompatActivity {

    private int step = 1;  //1: Welcome screen,  2: Permission screen
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (!prefs.getBoolean("firstrun", true)) {  //If the parameter did not exist, it counts as true
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);  //skip this part if it is not the first run
        }

        setLanguage();
        setTheme();

        //Remove title bar:  https://www.geeksforgeeks.org/how-to-remove-title-bar-from-the-activity-in-android/
        getSupportActionBar().hide();

        setContentView(R.layout.activity_permission);

        if (step == 1){
            ((TextView)findViewById(R.id.permission_title)).setText(R.string.welcome_title);
            ((TextView)findViewById(R.id.permission_desc)).setText(R.string.welcome_msg);
            ((Button)findViewById(R.id.permission_btn)).setText(R.string.welcome_btn);
        }else{
            ((TextView)findViewById(R.id.permission_title)).setText(R.string.permission_title);
            ((TextView)findViewById(R.id.permission_desc)).setText(R.string.permission_msg);
            ((Button)findViewById(R.id.permission_btn)).setText(R.string.permission_btn);
        }
    }

    public void jumpToNext(View v){
        if (step == 1){
            //change layout texts to next step (permissions request)
            ((TextView)findViewById(R.id.permission_title)).setText(R.string.permission_title);
            ((TextView)findViewById(R.id.permission_desc)).setText(R.string.permission_msg);
            ((Button)findViewById(R.id.permission_btn)).setText(R.string.permission_btn);

            step ++;
        }else{
            //Request permissions
            String[] permissions = {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.INTERNET
            };
            ActivityCompat.requestPermissions(this, permissions, 1);

            // Set the preference as already run for the first time
            prefs.edit().putBoolean("firstrun", false).commit();

            // Next activity displayed at onRequestPermissionsResult
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Jump to Main Activity
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    public void setLanguage() {

        String lang = prefs.getString("language","def");

        if (lang == "def")
            lang = this.getApplicationContext().getResources().getConfiguration().getLocales().get(0).getLanguage();

        Locale nuevaloc = new Locale(lang);  //Language codes: https://omegat.sourceforge.io/manual-standard/es/appendix.languages.html
        Locale.setDefault(nuevaloc);
        Configuration configuration = getBaseContext().getResources().getConfiguration();
        configuration.setLocale(nuevaloc);
        configuration.setLayoutDirection(nuevaloc);

        Context context =
                getBaseContext().createConfigurationContext(configuration);
        getBaseContext().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
    }

    public void setTheme(){

        String theme = prefs.getString("theme", "def");

        switch (theme){
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;

            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;

            case "def":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

    }

}