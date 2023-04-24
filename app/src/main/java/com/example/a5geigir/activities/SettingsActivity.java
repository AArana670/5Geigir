package com.example.a5geigir.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.a5geigir.R;
import com.example.a5geigir.TokenProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerChangeListener();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void registerChangeListener () {
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                manageChange(sharedPreferences, key);
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    private void manageChange(SharedPreferences sharedPreferences, String key){
        if (key.equals("language")) {
            updateLanguage(sharedPreferences);
            refresh();
        }else if (key.equals("theme")) {
            updateTheme(sharedPreferences);
            refresh();
        }
    }

    private void updateTheme(SharedPreferences sharedPreferences) {

        String theme = sharedPreferences.getString("theme", "def");

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

    private void updateLanguage(SharedPreferences prefs) {

        String lang = prefs.getString("language","def");

        if (lang.equals("def")) {
            lang = this.getApplicationContext().getResources().getConfiguration().getLocales().get(0).getLanguage();
        }

        Locale newLoc = new Locale(lang);  //https://omegat.sourceforge.io/manual-standard/es/appendix.languages.html
        Locale.setDefault(newLoc);
        Configuration configuration =
                getBaseContext().getResources().getConfiguration();
        configuration.setLocale(newLoc);
        configuration.setLayoutDirection(newLoc);

        Context context =
                getBaseContext().createConfigurationContext(configuration);
        getBaseContext().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
    }

    public void refresh(){
        Intent intent = getIntent();
        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(intent);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            setPreferencesFromResource(R.xml.preferences, rootKey);

            Preference tokenPref = findPreference("token");  //Currently unused feature
            if (tokenPref != null) {
                tokenPref.setSummaryProvider(new Preference.SummaryProvider<Preference>() {  //https://developer.android.com/develop/ui/views/components/settings/customize-your-settings#java
                    @Override
                    public CharSequence provideSummary(Preference preference) {  //Set the summary of Token field
                        String token = TokenProvider.getShortenedToken(getContext());
                        if (token == null)
                            return getString(R.string.loading);
                        return token;
                    }
                });
            }
        }
    }
}