package com.github.axet.callrecorder.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.github.axet.callrecorder.R;

import java.lang.reflect.Method;
import java.util.Locale;

public class MainApplication extends com.github.axet.audiolibrary.app.MainApplication {
    public static final String PREFERENCE_DELETE = "delete";
    public static final String PREFERENCE_FORMAT = "format";
    public static final String PREFERENCE_CALL = "call";
    public static final String PREFERENCE_OPTIMIZATION = "optimization";
    public static final String PREFERENCE_DETAILS_CONTACT = "_contact";
    public static final String PREFERENCE_DETAILS_CALL = "_call";
    public static final String PREFERENCE_SOURCE = "source";
    public static final String PREFERENCE_FILTER_IN = "filter_in";
    public static final String PREFERENCE_FILTER_OUT = "filter_out";
    public static final String PREFERENCE_DONE_NOTIFICATION = "done_notification";
    public static final String PREFERENCE_MIXERPATHS = "mixer_paths";

    public static final String CALL_OUT = "out";
    public static final String CALL_IN = "in";

    @SuppressLint("PrivateApi")
    public static String getprop(String key) {
        try {
            Class klass = Class.forName("android.os.SystemProperties");
            Method method = klass.getMethod("get", String.class);
            return (String) method.invoke(null, key);
        } catch (Exception e) {
            Log.d(TAG, "no system prop", e);
            return null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final SharedPreferences defaultValueSp = getSharedPreferences(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, Context.MODE_PRIVATE);
        if (!defaultValueSp.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false)) {
            PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
            MixerPaths m = new MixerPaths();
            if (!m.isSupported() || !m.isEnabled()) {
                SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor e = shared.edit();
                e.putString(MainApplication.PREFERENCE_ENCODING, Storage.EXT_3GP);
                e.commit();
            }
        }
    }

    @Override
    public int getUserTheme() {
        return getTheme(this, R.style.RecThemeLight, R.style.RecThemeDark);
    }

    public static String getContact(Context context, Uri f) {
        final SharedPreferences shared = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String p = getFilePref(f) + PREFERENCE_DETAILS_CONTACT;
        return shared.getString(p, null);
    }

    public static void setContact(Context context, Uri f, String id) {
        final SharedPreferences shared = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String p = getFilePref(f) + PREFERENCE_DETAILS_CONTACT;
        SharedPreferences.Editor editor = shared.edit();
        editor.putString(p, id);
        editor.commit();
    }

    public static String getCall(Context context, Uri f) {
        final SharedPreferences shared = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String p = getFilePref(f) + PREFERENCE_DETAILS_CALL;
        return shared.getString(p, null);
    }

    public static void setCall(Context context, Uri f, String id) {
        final SharedPreferences shared = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String p = getFilePref(f) + PREFERENCE_DETAILS_CALL;
        SharedPreferences.Editor editor = shared.edit();
        editor.putString(p, id);
        editor.commit();
    }

    public static String getString(Context context, Locale locale, int id, Object... formatArgs) {
        return getStringNewRes(context, locale, id, formatArgs);
    }

    public static String getStringNewRes(Context context, Locale locale, int id, Object... formatArgs) {
        Resources res;
        Configuration conf = new Configuration(context.getResources().getConfiguration());
        if (Build.VERSION.SDK_INT >= 17)
            conf.setLocale(locale);
        else
            conf.locale = locale;
        res = new Resources(context.getAssets(), context.getResources().getDisplayMetrics(), conf);
        String str;
        if (formatArgs.length == 0)
            str = res.getString(id);
        else
            str = res.getString(id, formatArgs);
        new Resources(context.getAssets(), context.getResources().getDisplayMetrics(), context.getResources().getConfiguration()); // restore side effect
        return str;
    }

    public static String[] getStrings(Context context, Locale locale, int id) {
        Resources res;
        Configuration conf = new Configuration(context.getResources().getConfiguration());
        if (Build.VERSION.SDK_INT >= 17)
            conf.setLocale(locale);
        else
            conf.locale = locale;
        res = new Resources(context.getAssets(), context.getResources().getDisplayMetrics(), conf);
        String[] str;
        str = res.getStringArray(id);
        new Resources(context.getAssets(), context.getResources().getDisplayMetrics(), context.getResources().getConfiguration()); // restore side effect
        return str;
    }
}
