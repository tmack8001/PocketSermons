package com.tmack.pocketsermons.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.tmack.pocketsermons.PocketSermonsMobileApplication;
import com.tmack.pocketsermons.R;
import com.tmack.pocketsermons.common.utils.Utils;

/**
 * Preferences related to ChromeCast support.
 */
public class CastPreference extends PreferenceActivity
        implements OnSharedPreferenceChangeListener {

    public static final String APP_VERSION_KEY = "app_version";

    // First Time User Overlay Shown State
    public static final String FTU_SHOWN_KEY = "ftu_shown";
    public static final String VOLUME_SELECTION_KEY = "volume_target";

    private ListPreference mVolumeListPreference;
    private SharedPreferences mPrefs;
    private VideoCastManager mCastManager;
    boolean mStopOnExit;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.application_preferences);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mCastManager = VideoCastManager.getInstance();

        /* Volume settings */
        mVolumeListPreference = (ListPreference) getPreferenceScreen()
                .findPreference(VOLUME_SELECTION_KEY);
        String volValue = mPrefs.getString(VOLUME_SELECTION_KEY,
                getString(R.string.prefs_volume_default));
        String volSummary = getResources().getString(R.string.prefs_volume_title_summary, volValue);
        mVolumeListPreference.setSummary(volSummary);

        EditTextPreference versionPref = (EditTextPreference) findPreference(APP_VERSION_KEY);
        versionPref.setTitle(getString(R.string.version, Utils.getAppVersionName(this),
                getString(R.string.ccl_version)));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (VOLUME_SELECTION_KEY.equals(key)) {
            String value = sharedPreferences.getString(VOLUME_SELECTION_KEY, "");
            String summary = getResources().getString(R.string.prefs_volume_title_summary, value);
            mVolumeListPreference.setSummary(summary);
        }
    }

    public static boolean isFtuShown(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(FTU_SHOWN_KEY, false);
    }

    public static void setFtuShown(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(FTU_SHOWN_KEY, true).apply();
    }

    @Override
    protected void onResume() {
        if (null != mCastManager) {
            mCastManager.incrementUiCounter();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (null != mCastManager) {
            mCastManager.decrementUiCounter();
        }
        super.onPause();
    }
}
