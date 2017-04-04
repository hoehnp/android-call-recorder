package com.github.axet.callrecorder.activities;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.axet.audiolibrary.app.Storage;
import com.github.axet.audiolibrary.encoders.Factory;
import com.github.axet.callrecorder.R;
import com.github.axet.callrecorder.app.MainApplication;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static <T> T[] removeElement(Class<T> c, T[] aa, int i) {
        List<T> ll = Arrays.asList(aa);
        ll = new ArrayList<>(ll);
        ll.remove(i);
        return ll.toArray((T[]) Array.newInstance(c, ll.size()));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            String key = preference.getKey();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                Ringtone ringtone = RingtoneManager.getRingtone(
                        preference.getContext(), Uri.parse(stringValue));

                if (ringtone == null) {
                    // Clear the summary if there was a lookup error.
                    preference.setSummary(null);
                } else {
                    // Set the summary to reflect the new ringtone display
                    // name.
                    String name = ringtone.getTitle(preference.getContext());
                    preference.setSummary(name);
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    static void initPrefs(PreferenceManager manager, PreferenceScreen screen) {
        final Context context = screen.getContext();
        ListPreference enc = (ListPreference) manager.findPreference(MainApplication.PREFERENCE_ENCODING);
        String v = enc.getValue();
        CharSequence[] ee = Factory.getEncodingTexts(context);
        CharSequence[] vv = Factory.getEncodingValues(context);
        if (ee.length > 1) {
            enc.setEntries(ee);
            enc.setEntryValues(vv);

            int i = enc.findIndexOfValue(v);
            if (i == -1) {
                enc.setValueIndex(0);
            } else {
                enc.setValueIndex(i);
            }

            bindPreferenceSummaryToValue(enc);
        } else {
            screen.removePreference(enc);
        }

        final String n = context.getPackageName();
        final PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
        Preference optimization = manager.findPreference(MainApplication.PREFERENCE_OPTIMIZATION);
        if (Build.VERSION.SDK_INT < 23) {
            screen.removePreference(optimization);
        } else {
            SwitchPreference p = (SwitchPreference) optimization;
            p.setChecked(pm.isIgnoringBatteryOptimizations(n));
            p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                @TargetApi(23)
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (pm.isIgnoringBatteryOptimizations(n)) {
                        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + n));
                        context.startActivity(intent);
                    }
                    return false;
                }
            });
        }

        bindPreferenceSummaryToValue(manager.findPreference(MainApplication.PREFERENCE_RATE));
        bindPreferenceSummaryToValue(manager.findPreference(MainApplication.PREFERENCE_THEME));
        bindPreferenceSummaryToValue(manager.findPreference(MainApplication.PREFERENCE_CHANNELS));
        bindPreferenceSummaryToValue(manager.findPreference(MainApplication.PREFERENCE_DELETE));
        bindPreferenceSummaryToValue(manager.findPreference(MainApplication.PREFERENCE_FORMAT));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupActionBar();

        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
        shared.registerOnSharedPreferenceChangeListener(this);

        if (Build.VERSION.SDK_INT < 11) {
            addPreferencesFromResource(R.xml.pref_general);
            initPrefs(getPreferenceManager(), getPreferenceScreen());
        } else {
            getFragmentManager().beginTransaction().replace(android.R.id.content, new GeneralPreferenceFragment()).commit();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        final String n = getPackageName();
        if (Build.VERSION.SDK_INT >= 23) {
            SwitchPreference optimization = (SwitchPreference) findPreference(MainApplication.PREFERENCE_OPTIMIZATION);
            if (optimization != null) {
                optimization.setChecked(pm.isIgnoringBatteryOptimizations(n));
            }
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setBackgroundDrawable(new ColorDrawable(MainApplication.getActionbarColor(this)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
//        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    @TargetApi(11)
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (Storage.permitted(this, permissions))
                    ;
                else
                    Toast.makeText(this, R.string.not_permitted, Toast.LENGTH_SHORT).show();
        }
    }

    public static final String[] PERMISSIONS = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(MainApplication.PREFERENCE_THEME)) {
            finish();
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
        shared.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(11)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        public GeneralPreferenceFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
            addPreferencesFromResource(R.xml.pref_general);
            initPrefs(getPreferenceManager(), getPreferenceScreen());
        }

        @Override
        public void onResume() {
            super.onResume();
            final PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
            final String n = getActivity().getPackageName();
            if (Build.VERSION.SDK_INT >= 23) {
                SwitchPreference optimization = (SwitchPreference) findPreference(MainApplication.PREFERENCE_OPTIMIZATION);
                if (optimization != null) {
                    optimization.setChecked(pm.isIgnoringBatteryOptimizations(n));
                }
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().onBackPressed();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

}
