package com.tmack.pocketsermons.tvleanback.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.libraries.cast.companionlibrary.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tmack.pocketsermons.tvleanback.R;
import com.tmack.pocketsermons.common.model.Sermon;
import com.tmack.pocketsermons.data.VideoProvider;
import com.tmack.pocketsermons.tvleanback.PicassoBackgroundManagerTarget;
import com.tmack.pocketsermons.tvleanback.presenter.CardPresenter;
import com.tmack.pocketsermons.tvleanback.presenter.DetailsDescriptionPresenter;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class VideoDetailsFragment extends DetailsFragment {
    private static final String TAG = "DetailsFragment";

    private static final int ACTION_WATCH_MEDIA = 1;
    private static final int ACTION_FAVORITE = 2;

    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;

    private static final int NUM_COLS = 10;

    private static final int NO_NOTIFICATION = -1;

    private MediaInfo mSelectedMedia;

    private ArrayObjectAdapter mAdapter;
    private ClassPresenterSelector mPresenterSelector;

    private BackgroundManager mBackgroundManager;
    private Drawable mDefaultBackground;
    private Target mBackgroundTarget;
    private DisplayMetrics mMetrics;

    public static int dpToPx(int dp, Context ctx) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);

        prepareBackgroundManager();

        mSelectedMedia = Utils.bundleToMediaInfo(getActivity().getIntent()
                .getBundleExtra(DetailsActivity.MEDIA));
        if (mSelectedMedia != null || checkGlobalSearchIntent()) {
            removeNotification(getActivity().getIntent()
                    .getIntExtra(DetailsActivity.NOTIFICATION_ID, NO_NOTIFICATION));
            setupAdapter();
            setupDetailsOverviewRow();
            setupDetailsOverviewRowPresenter();
            setupMovieListRow();
            setupMovieListRowPresenter();
            updateBackground(Sermon.fromMediaInfo(mSelectedMedia));
            setOnItemViewClickedListener(new ItemViewClickedListener());
        } else {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private boolean checkGlobalSearchIntent() {
        return false;
    }

    private void removeNotification(int notificationId) {
        if (notificationId != NO_NOTIFICATION) {
            NotificationManager notificationManager = (NotificationManager) getActivity()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        }
    }


    // TODO: same as in MainFragment... needs refactoring
    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mBackgroundTarget = new PicassoBackgroundManagerTarget(mBackgroundManager);

        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    protected void updateBackground(Sermon sermon) {
        if (sermon != null && sermon.getBackgroundImageURI() != null) {
            updateBackground(sermon.getBackgroundImageURI());
        }
    }

    protected void updateBackground(URI uri) {
        Log.d(TAG, "uri" + uri);
        Log.d(TAG, "metrics" + mMetrics.toString());
        Picasso.with(getActivity())
                .load(uri.toString())
                .resize(mMetrics.widthPixels, mMetrics.heightPixels)
                .centerCrop()
                .error(mDefaultBackground)
                .into(mBackgroundTarget);
    }

    private void setupAdapter() {
        mPresenterSelector = new ClassPresenterSelector();
        mAdapter = new ArrayObjectAdapter(mPresenterSelector);
        setAdapter(mAdapter);
    }

    private void setupDetailsOverviewRow() {
        Log.d(TAG, "setupDetailsOverviewRow: " + mSelectedMedia.toString());
        final DetailsOverviewRow row = new DetailsOverviewRow(mSelectedMedia);
        row.setImageDrawable(getResources().getDrawable(R.drawable.default_background));
        int width = dpToPx(DETAIL_THUMB_WIDTH, getActivity());
        int height = dpToPx(DETAIL_THUMB_HEIGHT, getActivity());
        Glide.with(getActivity())
                .load(Sermon.fromMediaInfo(mSelectedMedia).getCardImageUrl())
                .centerCrop()
                .error(R.drawable.default_background)
                .into(new SimpleTarget<GlideDrawable>(width, height) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable>
                                                        glideAnimation) {
                        Log.d(TAG, "details overview card image url ready: " + resource);
                        row.setImageDrawable(resource);
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                    }
                });

        row.addAction(new Action(ACTION_WATCH_MEDIA, getResources().getString(R.string.watch_sermon)));
        row.addAction(new Action(ACTION_FAVORITE, getResources().getString(R.string.favorite)));

        mAdapter.add(row);
    }

    private void setupDetailsOverviewRowPresenter() {
        // Set detail background and style
        DetailsOverviewRowPresenter detailsPresenter =
                new DetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
        detailsPresenter.setBackgroundColor(getResources().getColor(R.color.selected_background));
        detailsPresenter.setStyleLarge(true);

        // Hook up transition element
        detailsPresenter.setSharedElementEnterTransition(getActivity(),
                DetailsActivity.SHARED_ELEMENT_NAME);

        detailsPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == ACTION_WATCH_MEDIA) {
                    Intent intent = new Intent(getActivity(), PlaybackActivity.class);
                    intent.putExtra(DetailsActivity.MEDIA, Utils.mediaInfoToBundle(mSelectedMedia));
                    startActivity(intent);
                } else {
                    // TODO: add action to favorite a media clip
                    Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
    }

    private void setupMovieListRow() {
        // TODO: change to list of videos from same speaker, same series, and same church (to allow user to choose how to beige)
        // TODO: need to switch to Map<String, List<Sermon>> to really do what I want here
        String subcategories[] = {getString(R.string.related_movies)};
        Map<String, List<MediaInfo>> media = VideoProvider.getMediaListBySpeaker();

        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        if (media != null) {
            for (Map.Entry<String, List<MediaInfo>> entry : media.entrySet()) {
                // select only the list that relates to this speaker
                if (mSelectedMedia.getMetadata().getString(MediaMetadata.KEY_STUDIO).contains(entry.getKey())) {
                    List<MediaInfo> list = entry.getValue();
                    for (int j = 0; j < list.size(); j++) {
                        // only add videos that aren't this one (later to be optimized via an API call
                        if (!mSelectedMedia.getMetadata().getString(MediaMetadata.KEY_TITLE)
                                .contentEquals(list.get(j).getMetadata().getString(MediaMetadata.KEY_TITLE))) {
                            listRowAdapter.add(list.get(j));
                        }
                    }
                    break;
                }
            }
        }

        HeaderItem header = new HeaderItem(0, subcategories[0]);
        mAdapter.add(new ListRow(header, listRowAdapter));
    }

    private void setupMovieListRowPresenter() {
        mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
    }

    // TODO: this is also shared from MainActivity maybe make external?
    private class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof MediaInfo) {
                MediaInfo mediaInfo = (MediaInfo) item;
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.MEDIA, Utils.mediaInfoToBundle(mediaInfo));

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);
            }
        }
    }
}
