package com.tmack.pocketsermons.tvleanback.model;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.UUID;

import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;

public class Sermon implements Serializable {
    static final long serialVersionUID = 727566175075960653L;
    private static long count = 0;
    private UUID id;
    private String title;
    private String description;
    private String bgImageUrl;
    private String cardImageUrl;
    private String videoUrl;
    private String studio;
    private String category;

    public Sermon() {
    }

    public Sermon(UUID id, String title, String description, String bgImageUrl, String cardImageUrl,
                  String videoUrl, String studio, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.bgImageUrl = bgImageUrl;
        this.cardImageUrl = cardImageUrl;
        this.videoUrl = videoUrl;
        this.studio = studio;
        this.category = category;
    }

    public static Sermon fromMediaInfo(MediaInfo mediaInfo) {
        Sermon sermon = null;
        if (mediaInfo != null && mediaInfo.getMetadata() != null) {
            MediaMetadata metadata = mediaInfo.getMetadata();
            //String uuid = mediaInfo.();
            String videoUrl = mediaInfo.getContentId();

            String title = metadata.getString(MediaMetadata.KEY_TITLE);
            String description = metadata.getString(MediaMetadata.KEY_SUBTITLE);
            String imageUri = null;
            if (metadata.getImages() != null && metadata.getImages().size() > 0) {
                imageUri = metadata.getImages().get(0).getUrl().toString();
            }
            String studio = metadata.getString(MediaMetadata.KEY_STUDIO);
            sermon = new Sermon(null, title, description, imageUri, imageUri, videoUrl, studio, "");
        }
        return sermon;
    }

    public static MediaInfo toMediaInfo(Sermon sermon) {
        MediaInfo mediaInfo = null;
        if (sermon != null) {
            MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
            mediaMetadata.putString(MediaMetadata.KEY_TITLE, sermon.getTitle());
            mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, sermon.getDescription());
            mediaMetadata.putString(MediaMetadata.KEY_STUDIO, sermon.getStudio());
            mediaMetadata.addImage(new WebImage(Uri.parse(sermon.getBackgroundImageUrl())));

            mediaInfo = new MediaInfo.Builder(sermon.getVideoUrl())
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType("video/mp4")
                    .setMetadata(mediaMetadata)
                    .build();
        }
        return mediaInfo;
    }

    public static long getCount() {
        return count;
    }

    public static void incCount() {
        count++;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getBackgroundImageUrl() {
        return bgImageUrl;
    }

    public void setBackgroundImageUrl(String bgImageUrl) {
        this.bgImageUrl = bgImageUrl;
    }

    public String getCardImageUrl() {
        return cardImageUrl;
    }

    public void setCardImageUrl(String cardImageUrl) {
        this.cardImageUrl = cardImageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public URI getBackgroundImageURI() {
        try {
            Log.d("BACK MOVIE: ", bgImageUrl);
            return new URI(getBackgroundImageUrl());
        } catch (URISyntaxException e) {
            Log.d("URI exception: ", bgImageUrl);
            return null;
        }
    }

    public URI getCardImageURI() {
        try {
            return new URI(getCardImageUrl());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", backgroundImageUrl='" + bgImageUrl + '\'' +
                ", backgroundImageURI='" + getBackgroundImageURI().toString() + '\'' +
                ", cardImageUrl='" + cardImageUrl + '\'' +
                '}';
    }
}
