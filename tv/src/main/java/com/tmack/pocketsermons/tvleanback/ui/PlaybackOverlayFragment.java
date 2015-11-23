package com.tmack.pocketsermons.tvleanback.ui;

import android.app.Activity;
import android.content.Intent;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRow.FastForwardAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.PlayPauseAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.RewindAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipNextAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipPreviousAction;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.libraries.cast.companionlibrary.utils.Utils;
import com.tmack.pocketsermons.tvleanback.R;
import com.tmack.pocketsermons.data.VideoProvider;
import com.tmack.pocketsermons.common.model.Sermon;
import com.tmack.pocketsermons.tvleanback.presenter.CardPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/*
 * Class for video playback with media control
 */
public class PlaybackOverlayFragment extends android.support.v17.leanback.app.PlaybackOverlayFragment {
    private static final String TAG = PlaybackOverlayFragment.class.getSimpleName();
    private static final boolean SHOW_DETAIL = true;
    private static final boolean HIDE_MORE_ACTIONS = false;
    private static final int PRIMARY_CONTROLS = 5;
    private static final boolean SHOW_IMAGE = PRIMARY_CONTROLS <= 5;
    private static final int BACKGROUND_TYPE = PlaybackOverlayFragment.BG_LIGHT;
    private static final int CARD_WIDTH = 150;
    private static final int CARD_HEIGHT = 240;
    private static final int DEFAULT_UPDATE_PERIOD = 1000;
    private static final int UPDATE_PERIOD = 16;
    private static final int SIMULATED_BUFFERED_TIME = 10000;
    private static final int CLICK_TRACKING_DELAY = 1000;
    private static final int INITIAL_SPEED = 10000;

    private final Handler mClickTrackingHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private ArrayObjectAdapter mPrimaryActionsAdapter;
    private PlayPauseAction mPlayPauseAction;
    private FastForwardAction mFastForwardAction;
    private RewindAction mRewindAction;
    private SkipNextAction mSkipNextAction;
    private SkipPreviousAction mSkipPreviousAction;
    private PlaybackControlsRow mPlaybackControlsRow;
    private ArrayList<Sermon> mItems = new ArrayList<>();
    private int mCurrentItem;
    private Handler mHandler;
    private Runnable mRunnable;
    private MediaInfo mSelectedMedia;
    private int mFfwRwdSpeed = INITIAL_SPEED;
    private Timer mClickTrackingTimer;
    private int mClickCount;

    private MediaController mMediaController;
    private MediaController.Callback mMediaControllerCallback = new MediaControllerCallback();
    private int mCurrentPlaybackState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mItems = new ArrayList<>();
        mSelectedMedia = Utils.bundleToMediaInfo(getActivity().getIntent()
                .getBundleExtra(DetailsActivity.MEDIA));
        Sermon sermon = Sermon.fromMediaInfo(mSelectedMedia);

        // TODO: fix to have List<Sermon> so I can get other series videos as "KEY_STUDIO" == speaker name
        Map<String, List<MediaInfo>> mediaListBySpeaker = VideoProvider.getMediaListBySpeaker();

        if (mediaListBySpeaker != null) {
            for (Map.Entry<String, List<MediaInfo>> entry : mediaListBySpeaker.entrySet()) {
                if (sermon.getStudio().contains(entry.getKey())) {
                    List<MediaInfo> list = entry.getValue();
                    if (list != null && !list.isEmpty()) {
                        for (int j = 0; j < list.size(); j++) {
                            // TODO: fix this class to not require the current video to be in the "Related Videos" array
                            mItems.add(Sermon.fromMediaInfo(list.get(j)));
                            if (sermon.getTitle()
                                    .contentEquals(list.get(j).getMetadata().getString(MediaMetadata.KEY_TITLE))) {
                                mCurrentItem = j;
                            }
                        }
                    }
                }
            }
        }

        mHandler = new Handler();

        setBackgroundType(BACKGROUND_TYPE);
        setFadingEnabled(false);

        setupRows();

        setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                       RowPresenter.ViewHolder rowViewHolder, Row row) {
                Log.i(TAG, "onItemSelected: " + item + " row " + row);
            }
        });
        setOnItemViewClickedListener(new ItemViewClickedListener());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMediaController = activity.getMediaController();
        mMediaController.registerCallback(mMediaControllerCallback);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        stopProgressAutomation();
        mRowsAdapter = null;
        super.onStop();
    }

    @Override
    public void onDetach() {
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mMediaControllerCallback);
        }
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setupRows() {
        ClassPresenterSelector ps = new ClassPresenterSelector();

        PlaybackControlsRowPresenter playbackControlsRowPresenter;
        if (SHOW_DETAIL) {
            playbackControlsRowPresenter = new PlaybackControlsRowPresenter(
                    new DescriptionPresenter());
        } else {
            playbackControlsRowPresenter = new PlaybackControlsRowPresenter();
        }
        playbackControlsRowPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            public void onActionClicked(Action action) {
                if (action.getId() == mPlayPauseAction.getId()) {
                    togglePlayback(mPlayPauseAction.getIndex() == PlayPauseAction.PLAY);
                } else if (action.getId() == mSkipNextAction.getId()) {
                    next(mCurrentPlaybackState == PlaybackState.STATE_PLAYING);
                } else if (action.getId() == mSkipPreviousAction.getId()) {
                    prev(mCurrentPlaybackState == PlaybackState.STATE_PLAYING);
                } else if (action.getId() == mFastForwardAction.getId()) {
                    fastForward();
                } else if (action.getId() == mRewindAction.getId()) {
                    fastRewind();
                }
                if (action instanceof PlaybackControlsRow.MultiAction) {
                    notifyChanged(action);
                }
            }
        });
        playbackControlsRowPresenter.setSecondaryActionsHidden(HIDE_MORE_ACTIONS);

        ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
        ps.addClassPresenter(ListRow.class, new ListRowPresenter());
        mRowsAdapter = new ArrayObjectAdapter(ps);

        addPlaybackControlsRow();
        addOtherRows();

        setAdapter(mRowsAdapter);
    }

    public void togglePlayback(boolean playPause) {
        if (playPause) {
            mMediaController.getTransportControls().play();
        } else {
            mMediaController.getTransportControls().pause();
        }
    }

    private void addPlaybackControlsRow() {
        if (SHOW_DETAIL) {
            mPlaybackControlsRow = new PlaybackControlsRow(mSelectedMedia);
        } else {
            mPlaybackControlsRow = new PlaybackControlsRow();
        }
        mRowsAdapter.add(mPlaybackControlsRow);

        updatePlaybackRow();

        ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
        mPrimaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mPlaybackControlsRow.setPrimaryActionsAdapter(mPrimaryActionsAdapter);

        mPlayPauseAction = new PlayPauseAction(getActivity());
        mSkipNextAction = new PlaybackControlsRow.SkipNextAction(getActivity());
        mSkipPreviousAction = new PlaybackControlsRow.SkipPreviousAction(getActivity());
        mFastForwardAction = new PlaybackControlsRow.FastForwardAction(getActivity());
        mRewindAction = new PlaybackControlsRow.RewindAction(getActivity());

        mPrimaryActionsAdapter.add(mSkipPreviousAction);
        if (PRIMARY_CONTROLS > 3) {
            mPrimaryActionsAdapter.add(new PlaybackControlsRow.RewindAction(getActivity()));
        }
        mPrimaryActionsAdapter.add(mPlayPauseAction);
        if (PRIMARY_CONTROLS > 3) {
            mPrimaryActionsAdapter.add(new PlaybackControlsRow.FastForwardAction(getActivity()));
        }
        mPrimaryActionsAdapter.add(mSkipNextAction);
    }

    private void notifyChanged(Action action) {
        ArrayObjectAdapter adapter = mPrimaryActionsAdapter;
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
        }
    }

    private void updatePlaybackRow() {
        mPlaybackControlsRow.setCurrentTime(0);
        mPlaybackControlsRow.setBufferedProgress(0);
        mRowsAdapter.notifyArrayItemRangeChanged(0, 1);
    }

    private void updateSermonView(String title, String studio, String cardImageUrl, long duration) {
        if (mPlaybackControlsRow.getItem() != null) {
            Sermon sermon = (Sermon) mPlaybackControlsRow.getItem();
            sermon.setTitle(title);
            sermon.setStudio(studio);
        }
        mPlaybackControlsRow.setTotalTime((int) duration);

        if (SHOW_IMAGE) {
            updateVideoImage(cardImageUrl);
        }
    }

    private void addOtherRows() {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        for (Sermon sermon : mItems) {
            listRowAdapter.add(sermon);
        }
        HeaderItem header = new HeaderItem(0, getString(R.string.related_movies));
        mRowsAdapter.add(new ListRow(header, listRowAdapter));

    }

    private int getUpdatePeriod() {
        if (getView() == null || mPlaybackControlsRow.getTotalTime() <= 0) {
            return DEFAULT_UPDATE_PERIOD;
        }
        return Math.max(UPDATE_PERIOD, mPlaybackControlsRow.getTotalTime() / getView().getWidth());
    }

    private void startProgressAutomation() {
        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    int updatePeriod = getUpdatePeriod();
                    int currentTime = mPlaybackControlsRow.getCurrentTime() + updatePeriod;
                    int totalTime = mPlaybackControlsRow.getTotalTime();
                    mPlaybackControlsRow.setCurrentTime(currentTime);
                    mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);

                    if (totalTime > 0 && totalTime <= currentTime) {
                        stopProgressAutomation();
                        next(true);
                    } else {
                        mHandler.postDelayed(this, updatePeriod);
                    }
                }
            };
            mHandler.postDelayed(mRunnable, getUpdatePeriod());
        }
    }

    private void next(boolean autoPlay) {
        if (++mCurrentItem >= mItems.size()) {
            mCurrentItem = 0;
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean(PlaybackActivity.AUTO_PLAY, autoPlay);
        if (autoPlay) {
            mCurrentPlaybackState = PlaybackState.STATE_PAUSED;
        }
        mMediaController.getTransportControls().playFromMediaId(mItems.get(mCurrentItem).getId().toString(), bundle);
        mFfwRwdSpeed = INITIAL_SPEED;
    }

    private void prev(boolean autoPlay) {
        if (--mCurrentItem < 0) {
            mCurrentItem = mItems.size() - 1;
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean(PlaybackActivity.AUTO_PLAY, autoPlay);
        if (autoPlay) {
            mCurrentPlaybackState = PlaybackState.STATE_PAUSED;
        }
        mMediaController.getTransportControls().playFromMediaId(mItems.get(mCurrentItem).getId().toString(), bundle);
        mFfwRwdSpeed = INITIAL_SPEED;
    }

    private void fastForward() {
        startClickTrackingTimer();
        mMediaController.getTransportControls().fastForward();
    }

    private void fastRewind() {
        startClickTrackingTimer();
        mMediaController.getTransportControls().rewind();
    }

    private void stopProgressAutomation() {
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
            mRunnable = null;
        }
    }

    protected void updateVideoImage(String uri) {
        // TODO: change to Picasso?
        Glide.with(getActivity())
                .load(uri)
                .centerCrop()
                .into(new SimpleTarget<GlideDrawable>(CARD_WIDTH, CARD_HEIGHT) {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        mPlaybackControlsRow.setImageDrawable(resource);
                        mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());
                    }
                });
    }

    private void startClickTrackingTimer() {
        if (null != mClickTrackingTimer) {
            mClickCount++;
            mClickTrackingTimer.cancel();
        } else {
            mClickCount = 0;
            mFfwRwdSpeed = INITIAL_SPEED;
        }
        mClickTrackingTimer = new Timer();
        mClickTrackingTimer.schedule(new UpdateFfwRwdSpeedTask(), CLICK_TRACKING_DELAY);
    }

    static class DescriptionPresenter extends AbstractDetailsDescriptionPresenter {
        @Override
        protected void onBindDescription(ViewHolder viewHolder, Object item) {
            Sermon sermon = Sermon.fromMediaInfo((MediaInfo) item);
            viewHolder.getTitle().setText(sermon.getTitle());
            viewHolder.getSubtitle().setText(sermon.getDescription());
        }
    }

    private class UpdateFfwRwdSpeedTask extends TimerTask {

        @Override
        public void run() {
            mClickTrackingHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mClickCount == 0) {
                        mFfwRwdSpeed = INITIAL_SPEED;
                    } else if (mClickCount == 1) {
                        mFfwRwdSpeed *= 2;
                    } else if (mClickCount >= 2) {
                        mFfwRwdSpeed *= 4;
                    }
                    mClickCount = 0;
                    mClickTrackingTimer = null;
                }
            });
        }
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            MediaInfo mediaInfo = null;
            if (item instanceof MediaInfo) {
                mediaInfo = (MediaInfo) item;
            } else if (item instanceof Sermon) {
                mediaInfo = Sermon.toMediaInfo((Sermon) item);
            }

            if (mediaInfo != null) {
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), PlaybackActivity.class);
                intent.putExtra(DetailsActivity.MEDIA, Utils.mediaInfoToBundle(mediaInfo));

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);
            }
        }
    }

    private class MediaControllerCallback extends MediaController.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            Log.d(TAG, "playback state changed: " + state.getState());
            if (state.getState() == PlaybackState.STATE_PLAYING && mCurrentPlaybackState != PlaybackState.STATE_PLAYING) {
                mCurrentPlaybackState = PlaybackState.STATE_PLAYING;
                startProgressAutomation();
                setFadingEnabled(true);
                mPlayPauseAction.setIndex(PlayPauseAction.PAUSE);
                mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlayPauseAction.PAUSE));
                notifyChanged(mPlayPauseAction);
            } else if (state.getState() == PlaybackState.STATE_PAUSED && mCurrentPlaybackState != PlaybackState.STATE_PAUSED){
                mCurrentPlaybackState = PlaybackState.STATE_PAUSED;
                stopProgressAutomation();
                setFadingEnabled(false);
                mPlayPauseAction.setIndex(PlayPauseAction.PLAY);
                mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlayPauseAction.PLAY));
                notifyChanged(mPlayPauseAction);
            }

            int currentTime = (int) state.getPosition();
            mPlaybackControlsRow.setCurrentTime(currentTime);
            mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);
        }

        @Override
        public void onMetadataChanged(android.media.MediaMetadata metadata) {
            Log.d(TAG, "received update for media metadata");
            updateSermonView(
                    metadata.getString(android.media.MediaMetadata.METADATA_KEY_DISPLAY_TITLE),
                    metadata.getString(android.media.MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE),
                    metadata.getString(android.media.MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI),
                    metadata.getLong(android.media.MediaMetadata.METADATA_KEY_DURATION)
            );
        }
    }

}
