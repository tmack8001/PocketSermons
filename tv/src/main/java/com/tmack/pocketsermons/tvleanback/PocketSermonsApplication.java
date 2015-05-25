package com.tmack.pocketsermons.tvleanback;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

/**
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class PocketSermonsApplication extends Application {
    public static final String TAG = "PocketSermons";

    private static String APPLICATION_ID;

    private static RequestQueue sRequestQueue = null;
    private static PocketSermonsApplication sInstance = null;

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
