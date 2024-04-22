package com.tr.onjestslowo.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.tr.tools.Logger;

public class SettingsActivity extends AppCompatActivity {

    public static String LOG_TAG = "SettingsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new BasicPreferenceFragment())
                .commit();
    }

    public static void notifySharedPreferenceChanged(Context ctx) {
        Logger.debug(LOG_TAG, "notifiying in activity");
        AppPreferences.getInstance(ctx).invalidate();
    }

    public static void activatePreferenceSummary(Preference pref)
    {
        if (pref != null) {
            pref.setSummaryProvider(new Preference.SummaryProvider<EditTextPreference>() {
                @Override
                public CharSequence provideSummary(EditTextPreference preference) {
                    return preference.getText();
                }
            });
        }
    }

    public static class BasicPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            String prefStoreHowLongRes = this.getResources().getString(R.string.pref_reading_store_how_long);
            activatePreferenceSummary(findPreference(prefStoreHowLongRes));
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Logger.debug(LOG_TAG, key + " changed");
            notifySharedPreferenceChanged(getContext());
        }
    }

    public static class AdvancedPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_advanced, rootKey);

            String preProxyHost = this.getResources().getString(R.string.pref_wifi_proxy_host);
            activatePreferenceSummary(findPreference(preProxyHost));
            String prefProxyPort = this.getResources().getString(R.string.pref_wifi_proxy_port);
            activatePreferenceSummary(findPreference(prefProxyPort));
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Logger.debug(LOG_TAG, key + " changed");
            notifySharedPreferenceChanged(getContext());
        }
    }
}