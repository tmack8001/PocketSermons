package com.tmack.pocketsermons.tvleanback.presenter;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.google.android.gms.cast.MediaInfo;
import com.tmack.pocketsermons.common.model.Sermon;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {
    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        Sermon sermon = Sermon.fromMediaInfo((MediaInfo) item);

        if (sermon != null) {
            viewHolder.getTitle().setText(sermon.getTitle());
            viewHolder.getSubtitle().setText(sermon.getStudio());
            // TODO: probably need plain text here
            viewHolder.getBody().setText(sermon.getDescription());
        }
    }
}
