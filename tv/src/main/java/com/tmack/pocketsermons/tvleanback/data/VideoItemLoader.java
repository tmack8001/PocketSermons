package com.tmack.pocketsermons.tvleanback.data;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.cast.MediaInfo;
import com.tmack.pocketsermons.data.VideoProvider;
import com.tmack.pocketsermons.tvleanback.PocketSermonsApplication;

import java.util.List;
import java.util.Map;

/**
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class VideoItemLoader extends AsyncTaskLoader<Map<String, List<MediaInfo>>> {

    private static final String TAG = "VideoItemLoader";

    public VideoItemLoader(Context context) {
        super(context);
    }

    @Override
    public Map<String, List<MediaInfo>> loadInBackground() {
        try {
            VideoProvider.setContext(getContext());
            VideoProvider.retrieveMedia(PocketSermonsApplication.getInstance().getRequestQueue());
            return VideoProvider.getMediaListByChurch();
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
        // Attempt to cancel the current load task if possible
        cancelLoad();
    }
}
