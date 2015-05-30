package com.tmack.pocketsermons.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
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

    // Termination on Disconnect Policy
    public static final String TERMINATION_POLICY_KEY = "termination_policy";
    public static final String STOP_ON_DISCONNECT = "1";
    public static final String CONTINUE_ON_DISCONNECT = "0";

    private SharedPreferences mPrefs;
    private VideoCastManager mCastManager;
    boolean mStopOnExit;
    private ListPreference mTerminationListPreference;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.application_preferences);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        mCastManager = PocketSermonsMobileApplication.getCastManager();

        // -- Termination Policy -------------------//
        mTerminationListPreference = (ListPreference) getPreferenceScreen().findPreference(
                TERMINATION_POLICY_KEY);
        mTerminationListPreference.setSummary(getTerminationSummary(mPrefs));
        mCastManager.setStopOnDisconnect(mStopOnExit);

        EditTextPreference versionPref = (EditTextPreference) findPreference(APP_VERSION_KEY);
        versionPref.setTitle(getString(R.string.version, Utils.getAppVersionName(this),
                getString(R.string.ccl_version)));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (TERMINATION_POLICY_KEY.equals(key)) {
            mTerminationListPreference.setSummary(getTerminationSummary(sharedPreferences));
            mCastManager.setStopOnDisconnect(mStopOnExit);
        }
    }

    private String getTerminationSummary(SharedPreferences sharedPreferences) {
        String valueString = sharedPreferences.getString(TERMINATION_POLICY_KEY, "0");
        String[] labels = getResources().getStringArray(R.array.prefs_termination_policy_names);
        int value = CONTINUE_ON_DISCONNECT.equals(valueString) ? 0 : 1;
        mStopOnExit = value != 0;
        return labels[value];
    }

    public static boolean isFtuShown(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(FTU_SHOWN_KEY, false);
    }

    public static void setFtuShown(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(FTU_SHOWN_KEY, true).commit();
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
