package com.tmack.pocketsermons.data;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.RequestQueue;
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
import com.tmack.pocketsermons.api.R;
import com.tmack.pocketsermons.api.SermonsResponse;
import com.tmack.pocketsermons.model.Sermon;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provider used to load video resources.
 */
public class VideoProvider {

    private static final String TAG = "VideoProvider";
    // set of favorites
    private static final Set<String> sFavoriteList = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private static Context sContext;
    private static String sCatalogUrl;
    // application cache of media content
    private static List<MediaInfo> sMediaList;
    private static ConcurrentMap<String, MediaInfo> sMediaListById;
    private static ConcurrentMap<String, List<MediaInfo>> sMediaListByChurch;
    private static ConcurrentMap<String, List<MediaInfo>> sMediaListBySeries;
    private static ConcurrentMap<String, List<MediaInfo>> sMediaListBySpeaker;

    public static void setContext(Context context) {
        if (sContext == null)
            sContext = context;
    }

    public static List<MediaInfo> getMediaList() {
        return sMediaList;
    }

    public static ConcurrentMap<String, MediaInfo> getMediaListById() {
        return sMediaListById;
    }

    public static ConcurrentMap<String, List<MediaInfo>> getMediaListByChurch() {
        return sMediaListByChurch;
    }

    public static ConcurrentMap<String, List<MediaInfo>> getMediaListBySeries() {
        return sMediaListBySeries;
    }

    public static ConcurrentMap<String, List<MediaInfo>> getMediaListBySpeaker() {
        return sMediaListBySpeaker;
    }

    public static void setFavorite(String mediaId, boolean favorite) {
        if (favorite) {
            sFavoriteList.add(mediaId);
        } else {
            sFavoriteList.remove(mediaId);
        }
    }

    public static boolean isFavorite(String mediaId) {
        return sFavoriteList.contains(mediaId);
    }

    /**
     * Return the MediaInfo for the given mediaId.
     *
     * @param mediaId The unique, non-hierarchical music ID.
     */
    public static MediaInfo getMedia(String mediaId) {
        return sMediaListById.containsKey(mediaId) ? sMediaListById.get(mediaId) : null;
    }

    private static synchronized void buildListsByChurch() {
        ConcurrentMap<String, List<MediaInfo>> newMediaListByChurch = new ConcurrentHashMap<>();

        for (MediaInfo m : sMediaList) {
            String church = m.getMetadata().getString(MediaMetadata.KEY_ALBUM_ARTIST);
            List<MediaInfo> list = newMediaListByChurch.get(church);
            if (list == null) {
                list = new ArrayList<>();
                newMediaListByChurch.put(church, list);
            }
            list.add(m);
        }
        sMediaListByChurch = newMediaListByChurch;
    }

    private static synchronized void buildListsBySeries() {
        ConcurrentMap<String, List<MediaInfo>> newMediaListBySeries = new ConcurrentHashMap<>();

        for (MediaInfo m : sMediaList) {
            String series = m.getMetadata().getString(MediaMetadata.KEY_ALBUM_TITLE);
            List<MediaInfo> list = newMediaListBySeries.get(series);
            if (list == null) {
                list = new ArrayList<>();
                newMediaListBySeries.put(series, list);
            }
            list.add(m);
        }
        sMediaListBySeries = newMediaListBySeries;
    }

    private static synchronized void buildListsBySpeaker() {
        ConcurrentMap<String, List<MediaInfo>> newMediaListBySpeaker = new ConcurrentHashMap<>();

        for (MediaInfo m : sMediaList) {
            String speaker = m.getMetadata().getString(MediaMetadata.KEY_STUDIO);
            List<MediaInfo> list = newMediaListBySpeaker.get(speaker);
            if (list == null) {
                list = new ArrayList<>();
                newMediaListBySpeaker.put(speaker, list);
            }
            list.add(m);
        }
        sMediaListBySpeaker = newMediaListBySpeaker;
    }

    public static List<MediaInfo> retrieveMedia(RequestQueue requestQueue) throws Exception {
        if (null != sMediaList) {
            return sMediaList;
        }

        sMediaList = new ArrayList<>();
        sMediaListById = new ConcurrentHashMap<>();

        // load API data
        sCatalogUrl = sContext.getString(R.string.api_host) +
                sContext.getString(R.string.api_version) + sContext.getString(R.string.sermons_path);
        requestQueue.add(new VideoProvider().parseUrl(sCatalogUrl));
        while (null == SermonsResponse.CACHED_SERMONS_RESPONSE) {
            Thread.sleep(1000);
        }
        List<Sermon> sermonList = SermonsResponse.CACHED_SERMONS_RESPONSE.getSermons();

        // convert Sermon to MediaInfo
        for (Sermon sermon : sermonList) {
            MediaInfo mediaInfo = buildMediaInfo(sermon);
            sMediaList.add(mediaInfo);
            sMediaListById.put(sermon.getId(), mediaInfo);
        }

        buildListsByChurch();
        buildListsBySeries();
        buildListsBySpeaker();
        return sMediaList;
    }

    private static MediaInfo buildMediaInfo(Sermon selectedSermon) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String sermonDateISO8601 = sdf.format(selectedSermon.getDate());

        String primarySpeaker = "";
        if (selectedSermon.getSpeakers() != null && !selectedSermon.getSpeakers().isEmpty()) {
            primarySpeaker = selectedSermon.getSpeakers().get(0).getFormattedName();
        }

        String seriesTitle = "";
        String seriesImage = "";
        if (selectedSermon.getSeries() != null) {
            seriesTitle = selectedSermon.getSeries().getTitle();
            seriesImage = selectedSermon.getSeries().getImageUri();
        }

        String churchName = "";
        if (selectedSermon.getChurch() != null) {
            churchName = selectedSermon.getChurch().getName();
        }

        return buildMediaInfo(selectedSermon.getId(), selectedSermon.getTitle(), selectedSermon.getDescription(),
                seriesTitle, primarySpeaker, churchName, sermonDateISO8601,
                selectedSermon.getVideoUri(), seriesImage, null);
    }

    private static MediaInfo buildMediaInfo(String id, String title, String subTitle,
                                            String seriesTitle, String artist,
                                            String studio, String broadcastDate,
                                            String url, String imgUrl, List<MediaTrack> tracks) {
        Log.d(TAG, "converting sermon " + title + " to MediaInfo");
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, title);
        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, subTitle);
        mediaMetadata.putString(MediaMetadata.KEY_STUDIO, artist);

        mediaMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE, seriesTitle);
        mediaMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST, studio);
        // TODO: there is an issue with invalid dates, KEY_BROADCAST_DATE has to be ISO8601 and not null
//        if (StringUtils.isNotEmpty(broadcastDate)) {
//            mediaMetadata.putString(MediaMetadata.KEY_BROADCAST_DATE, broadcastDate);
//        }

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

    protected JsonObjectRequest parseUrl(final String urlString) {
        JsonObjectRequest request = null;
        // if already requested data via API read application state
        if (SermonsResponse.CACHED_SERMONS_RESPONSE == null) {
            request = new JsonObjectRequest(urlString, null,
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
        }

        return request;
    }
}
