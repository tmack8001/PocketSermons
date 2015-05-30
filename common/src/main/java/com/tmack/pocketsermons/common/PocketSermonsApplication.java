package com.tmack.pocketsermons.common;

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

import java.util.HashMap;

/**
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class PocketSermonsApplication extends Application {
    public static final String TAG = "PocketSermons";

    protected static String APPLICATION_ID;
    protected static String PROPERTY_ID;

    private static RequestQueue sRequestQueue = null;
    protected static PocketSermonsApplication sInstance = null;

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

        sInstance = this;
    }

    public static synchronized PocketSermonsApplication getInstance() {
        return sInstance;
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
        if (sRequestQueue == null) {
            sRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return sRequestQueue;
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
        if (sRequestQueue != null) {
            sRequestQueue.cancelAll(tag);
        }
    }
}
