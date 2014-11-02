package com.tmack.sermonstream.browser;

import android.net.Uri;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.common.images.WebImage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tmack.sermonstream.TheMountApplication;
import com.tmack.sermonstream.model.Sermon;
import com.tmack.sermonstream.model.responses.SermonsResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Provider used to load video resources.
 */
public class VideoProvider {

    private static final String TAG = "VideoProvider";

    // application cache of media content
    private static List<MediaInfo> mediaList;

    protected void parseUrl(final String urlString) {
        // if already requested data via API read application state
        if (SermonsResponse.CACHED_SERMONS_RESPONSE == null) {
            JsonObjectRequest request = new JsonObjectRequest(urlString, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "API request completed");
                            try {
                                VolleyLog.v("Response:%n %s", response.toString(4));

                                GsonBuilder builder = new GsonBuilder();
                                builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                Gson gson = builder.create();

                                // cache the list of sermons within the Application Cache
                                // TODO: replace with Database entries for the Sermons API response
                                SermonsResponse.CACHED_SERMONS_RESPONSE = gson.fromJson(response.toString(), SermonsResponse.class);
                            } catch (JSONException e) {
                                Log.d(TAG, "API request error", e);
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.d(TAG, "API request failed", volleyError);
                            VolleyLog.e("Error: ", volleyError.getMessage());
                            // TODO: display error to user regarding network errors
                        }
                    });

            TheMountApplication.getInstance().addToRequestQueue(request);
        }
    }

    public static List<MediaInfo> buildMedia(String url) throws Exception {
        if (null != mediaList) {
            return mediaList;
        }

        mediaList = new ArrayList<MediaInfo>();
        // load API data
        new VideoProvider().parseUrl(url);
        while (null == SermonsResponse.CACHED_SERMONS_RESPONSE) {
            Thread.sleep(1000);
        }
        List<Sermon> sermonList = SermonsResponse.CACHED_SERMONS_RESPONSE.getSermons();

        // convert Sermon to MediaInfo
        for (Sermon sermon : sermonList) {
            mediaList.add(buildMediaInfo(sermon));
        }

        return mediaList;
    }

    private static MediaInfo buildMediaInfo(Sermon selectedSermon) {
        return buildMediaInfo(selectedSermon.getId(), selectedSermon.getTitle(), selectedSermon.getDescription(),
                selectedSermon.getSeries().getTitle(), selectedSermon.getSpeakers().get(0).getFormattedName(),
                selectedSermon.getChurch().getName(), selectedSermon.getDate().toString(),
                selectedSermon.getVideoUri(), selectedSermon.getSeries().getImageUri(), null);
    }

    private static MediaInfo buildMediaInfo(String id, String title, String subTitle, String seriesTitle,
                                            String artist, String studio, String broadcastDate,
                                            String url, String imgUrl, List<MediaTrack> tracks) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, title);
        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, subTitle);
        mediaMetadata.putString(MediaMetadata.KEY_STUDIO, artist);

        mediaMetadata.addImage(new WebImage(Uri.parse(imgUrl)));

        JSONObject customData = null;
        try {
            customData = new JSONObject().put("id", id);
        } catch (JSONException e) {
            // not likely, but if so just ignore.
        }

        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(getMediaType())
                .setMetadata(mediaMetadata)
                .setMediaTracks(tracks)
                .setCustomData(customData)
                .build();
    }

    private static String getMediaType() {
        return "video/mp4";
    }
}
