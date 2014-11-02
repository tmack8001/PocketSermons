package com.tmack.sermonstream.model.responses;

import com.tmack.sermonstream.model.Sermon;

import java.util.List;

/**
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class SermonsResponse {

    public static SermonsResponse CACHED_SERMONS_RESPONSE;

    private List<Sermon> sermons;

    public List<Sermon> getSermons() {
        return sermons;
    }

    public void setSermons(List<Sermon> sermons) {
        this.sermons = sermons;
    }
}
