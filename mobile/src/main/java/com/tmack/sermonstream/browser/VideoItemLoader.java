package com.tmack.sermonstream.browser;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.android.gms.cast.MediaInfo;

import java.util.List;

/**
 * Async loader for video resources. This will allow loading of a list of
 * videos in the background so the UI thread doesn't get blocked.
 */
public class VideoItemLoader extends AsyncTaskLoader<List<MediaInfo>> {

    private static final String TAG = "VideoItemLoader";

    private String mUrl;

    public VideoItemLoader(Context context, String url) {
        super(context);
        this.mUrl = url;
    }

    /**
     * Request to load list of videos in the background.
     */
    @Override
    public List<MediaInfo> loadInBackground() {
        try {
            return VideoProvider.buildMedia(mUrl);
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch media data", e);
            return null;
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }
}
