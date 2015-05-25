package com.tmack.pocketsermons.model;

import com.tmack.pocketsermons.utils.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

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

    private String _id;
    private String permalink;
    private String title;
    private String description;
    private Date date;

    private Church church;
    private TeachingSeries series;
    private List<Speaker> speakers;

    private String audioUri;
    private String videoUri;

    public String getId() {
        return StringUtils.isNotEmpty(_id) ? _id : UUID.randomUUID().toString();
    }

    public void setId(String id) {
        this._id = id;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
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

    public String getAudioUri() {
        return audioUri;
    }

    public void setAudioUri(String audioUri) {
        this.audioUri = audioUri;
    }

    public String getVideoUri() {
        return videoUri;
    }

    public void setVideoUri(String videoUri) {
        this.videoUri = videoUri;
    }
}
