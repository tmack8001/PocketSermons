package com.tmack.pocketsermons;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.utils.Utils;
import com.tmack.pocketsermons.settings.CastPreference;

import java.util.HashMap;

/**
 * The {@link Application} for this application.
 */
public class PocketSermonsApplication extends Application {
    public static final String TAG = "PocketSermons";
    public static final double VOLUME_INCREMENT = 0.05;

    private static String APPLICATION_ID;
    private static String PROPERTY_ID;

    private static RequestQueue mRequestQueue = null;

    private static PocketSermonsApplication sInstance = null;
    private static VideoCastManager sCastManager = null;

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     * <p/>
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER // Tracker used only in this app.
    }

    private static HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    /*
     * (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        APPLICATION_ID = getString(R.string.app_id);
        PROPERTY_ID = getString(R.string.ga_property_id);

        initializeCastManager();
        Utils.saveFloatToPreference(getApplicationContext(),
                VideoCastManager.PREFS_KEY_VOLUME_INCREMENT, (float) VOLUME_INCREMENT);

        sInstance = this;
    }

    public static synchronized PocketSermonsApplication getInstance() {
        return sInstance;
    }

    private void initializeCastManager() {
        sCastManager = VideoCastManager.initialize(getApplicationContext(), APPLICATION_ID, null, null);
        sCastManager.enableFeatures(
                VideoCastManager.FEATURE_NOTIFICATION |
                        VideoCastManager.FEATURE_LOCKSCREEN |
                        VideoCastManager.FEATURE_WIFI_RECONNECT |
                        VideoCastManager.FEATURE_CAPTIONS_PREFERENCE |
                        VideoCastManager.FEATURE_DEBUGGING);
        String destroyOnExitStr = Utils.getStringFromPreference(getApplicationContext(),
                CastPreference.TERMINATION_POLICY_KEY);
        sCastManager.setStopOnDisconnect(null != destroyOnExitStr
                && CastPreference.STOP_ON_DISCONNECT.equals(destroyOnExitStr));
    }

    public static VideoCastManager getCastManager() {
        if (sCastManager == null) {
            throw new IllegalStateException("Application has not been started");
        }
        return sCastManager;
    }

    public static synchronized Tracker getTracker(TrackerName trackerId, Context ctx) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(ctx);
            // Use to enable logging for debugging purposes
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);

            if (trackerId == TrackerName.APP_TRACKER) {
                Tracker t = analytics.newTracker(R.xml.app_tracker);

                t.enableAdvertisingIdCollection(true);
                mTrackers.put(trackerId, t);
            }
        }
        return mTrackers.get(trackerId);
    }

    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        addToRequestQueue(request, "");
    }

    public <T> void addToRequestQueue(Request<T> request, String tag) {
        // set the default tag if tag is empty
        request.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        VolleyLog.d("Adding request to queue: %s", request.getUrl());
        getRequestQueue().add(request);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
