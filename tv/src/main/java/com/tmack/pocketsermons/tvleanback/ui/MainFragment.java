package com.tmack.pocketsermons.tvleanback.ui;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.common.images.WebImage;
import com.google.android.libraries.cast.companionlibrary.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tmack.pocketsermons.tvleanback.R;
import com.tmack.pocketsermons.data.VideoItemMapLoader;
import com.tmack.pocketsermons.data.VideoProvider;
import com.tmack.pocketsermons.tvleanback.PicassoBackgroundManagerTarget;
import com.tmack.pocketsermons.tvleanback.presenter.CardPresenter;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main class to show BrowseFragment with header and rows of videos
 *
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class MainFragment extends BrowseFragment implements
        LoaderManager.LoaderCallbacks<Map<String, List<MediaInfo>>> {
    private static final String TAG = "MainFragment";

    private static int BACKGROUND_UPDATE_DELAY = 300;
    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private Uri mBackgroundURI;
    private Target mBackgroundTarget;
    private BackgroundManager mBackgroundManager;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        loadVideoData();

        prepareBackgroundManager();
        setupUIElements();
        setupEventListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mBackgroundTarget = new PicassoBackgroundManagerTarget(mBackgroundManager);

        mDefaultBackground = getResources().getDrawable(R.drawable.default_background, getActivity().getTheme());
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        setBadgeDrawable(getResources().getDrawable(R.drawable.pocketsermons_banner, getActivity().getTheme()));
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // setup headers
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        // set fastLane (or headers) background color
        setBrandColor(getResources().getColor(R.color.fastlane_background));
        // set search icon color
        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));
    }

    private void loadVideoData() {
        VideoProvider.setContext(getActivity());
        getLoaderManager().initLoader(0, null, this);
    }

    private void setupEventListeners() {
        // TODO: add search activity
        setOnSearchClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
                        .show();
            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());

    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
     * android.os.Bundle)
     */
    @Override
    public Loader<Map<String, List<MediaInfo>>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "VideoItemMapLoader created");
        return new VideoItemMapLoader(getActivity());
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android
     * .support.v4.content.Loader, java.lang.Object)
     */
    @Override
    public void onLoadFinished(Loader<Map<String, List<MediaInfo>>> loader,
                               Map<String, List<MediaInfo>> data) {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter();

        int i = 0;

        if (data != null) {
            for (Map.Entry<String, List<MediaInfo>> entry : data.entrySet()) {
                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                List<MediaInfo> list = entry.getValue();

                for (int j = 0; j < list.size(); j++) {
                    listRowAdapter.add(list.get(j));
                }

                HeaderItem header = new HeaderItem(i, entry.getKey());
                mRowsAdapter.add(new ListRow(header, listRowAdapter));
                i++;
            }
        }

        // TODO: customize preferences header and row items
        HeaderItem gridHeader = new HeaderItem(i, getString(R.string.preferences));

        GridItemPresenter mGridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        gridRowAdapter.add(getResources().getString(R.string.grid_view));
        gridRowAdapter.add(getResources().getString(R.string.send_feedback));
        gridRowAdapter.add(getResources().getString(R.string.personal_settings));
        mRowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

        setAdapter(mRowsAdapter);

        // TODO: build recommendation engine support for home screen tiles
        // updateRecommendations();
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadReset(android
     * .support.v4.content.Loader)
     */
    @Override
    public void onLoaderReset(Loader<Map<String, List<MediaInfo>>> loader) {
        mRowsAdapter.clear();
    }

    protected void setDefaultBackground(Drawable background) {
        mDefaultBackground = background;
    }

    protected void setDefaultBackground(int resourceId) {
        mDefaultBackground = getResources().getDrawable(resourceId, getActivity().getTheme());
    }

    protected void updateBackground(Uri uri) {
        Picasso.with(getActivity())
                .load(uri.toString())
                .resize(mMetrics.widthPixels, mMetrics.heightPixels)
                .centerCrop()
                .error(mDefaultBackground)
                .into(mBackgroundTarget);
        mBackgroundTimer.cancel();
    }

    protected void updateBackground(Drawable drawable) {
        BackgroundManager.getInstance(getActivity()).setDrawable(drawable);
    }

    protected void clearBackground() {
        BackgroundManager.getInstance(getActivity()).setDrawable(mDefaultBackground);
    }

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBackgroundURI != null) {
                        updateBackground(mBackgroundURI);
                    }
                }
            });

        }
    }

    private class GridItemPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(getResources().getColor(R.color.default_background));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }

    private class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof MediaInfo) {
                MediaInfo mediaInfo = (MediaInfo) item;
                Log.d(TAG, "MediaInfo: " + mediaInfo.toString());


                // TODO: change MEDIA to Sermon object instead of the gms-cast:MediaInfo object
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.MEDIA, Utils.mediaInfoToBundle(mediaInfo));

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);
            } else if (item instanceof String) {
                // TODO: figure out what these views are for... thinking just examples
                if (((String) item).contains(getString(R.string.grid_view))) {
                    Intent intent = new Intent(getActivity(), VerticalGridActivity.class);
                    startActivity(intent);
                    //} else if (((String) item).contains(getString(R.string.error_fragment))) {
                    //    Intent intent = new Intent(getActivity(), BrowseErrorActivity.class);
                    //    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof MediaInfo) {
                List<WebImage> images = ((MediaInfo) item).getMetadata().getImages();
                if (images != null && images.size() > 0) {
                    mBackgroundURI = images.get(0).getUrl();
                    startBackgroundTimer();
                }
            }
        }
    }
}
