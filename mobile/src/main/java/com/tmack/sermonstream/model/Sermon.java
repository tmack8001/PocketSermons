package com.tmack.sermonstream.model;

import java.util.Date;
import java.util.List;

/**
 * Module
 *
 * How to convert Vimeo Video Files Using ffmpeg:
 * ffmpeg -i <input> -c:v libx264 -profile:v baseline -c:a libfaac -ar 44100 -ac 2 -b:a 128k -movflags faststart <output>
 *
 * @author Trevor Mack (drummer8001@gmail.com)
 * @since 0.1.0
 */
public class Sermon {

    private String id;
    private String title;
    private String description;
    private Date date;

    private Church church;
    private TeachingSeries series;
    private List<Speaker> speakers;

    // video or audio;
    //private ContentType contentType; // TODO: change to contentLink
    private String videoUri;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Church getChurch() {
        return church;
    }

    public void setChurch(Church church) {
        this.church = church;
    }

    public TeachingSeries getSeries() {
        return series;
    }

    public void setSeries(TeachingSeries series) {
        this.series = series;
    }

    public List<Speaker> getSpeakers() {
        return speakers;
    }

    public void setSpeakers(List<Speaker> speakers) {
        this.speakers = speakers;
    }

    public String getVideoUri() {
        return videoUri;
    }

    public void setVideoUri(String videoUri) {
        this.videoUri = videoUri;
    }
}
