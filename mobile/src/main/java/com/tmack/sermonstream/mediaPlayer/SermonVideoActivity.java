package com.tmack.sermonstream.mediaPlayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.widgets.MiniController;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tmack.sermonstream.R;
import com.tmack.sermonstream.TheMountApplication;
import com.tmack.sermonstream.settings.CastPreference;
import com.tmack.sermonstream.utils.Utils;

import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

public class SermonVideoActivity extends ActionBarActivity {

    private static final String TAG = "SermonVideoActivity";
    private static final float ASPECT_RATIO = 72f / 128;
    private final Handler mHandler = new Handler();

    // TODO: change to SermonInfo
    private MediaInfo mSelectedMedia;
    private PlaybackLocation mLocation;
    private PlaybackState mPlaybackState;

    private View mContainer;
    private VideoView mVideoView;
    private ImageView mCoverArt;

    private Toolbar mToolbar;
    private View mControllers;
    private TextView mStartText;
    private TextView mEndText;
    private SeekBar mSeekbar;
    private ImageView mPlayPause;
    private ProgressBar mLoading;

    private View mMetadataView;
    private TextView mTitleView;
    private TextView mAuthorView;
    private TextView mDescriptionView;

    private Timer mSeekbarTimer;
    private Timer mControllersTimer;

    private VideoCastManager mCastManager;
    private VideoCastConsumerImpl mCastConsumer;
    private MiniController mMini;
    protected MediaInfo mRemoteMediaInformation;

    private Point mDisplaySize;
    private boolean mShouldStartPlayback;
    private boolean mControllersVisible;
    private int mDuration;

    private Tracker mTracker;

    // used for Picasso custom Target loading
    private Target loadtarget = null;

    /*
     * indicates whether we are doing a local or a remote playback
     */
    public static enum PlaybackLocation {
        LOCAL,
        REMOTE;
    }

    /*
     * List of various states that we can be in
     */
    public static enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);
        loadViews();

        // setup ChromeCast Manager
        mCastManager = TheMountApplication.getVideoCastManager(getApplicationContext());
        mTracker = TheMountApplication.getTracker(TheMountApplication.TrackerName.APP_TRACKER, getApplicationContext());
        setupToolbar();
        setupControlsCallbacks();
        setupMiniController();
        setupCastListener();

        Bundle b = getIntent().getExtras();
        if (null != b) {
            mSelectedMedia = com.google.sample.castcompanionlibrary.utils.Utils
                    .toMediaInfo(getIntent().getBundleExtra("media"));
            mShouldStartPlayback = b.getBoolean("shouldStart", false);
            int startPosition = b.getInt("startPosition", 0);

            mVideoView.setVideoURI(Uri.parse(mSelectedMedia.getContentId()));
            Log.d("TAG", "Setting url of the VideoView to: " + mSelectedMedia.getContentId());

            if (mShouldStartPlayback) {
                // this will be the case only if we are coming from the
                // CastControllerActivity by disconnecting from a device
                mPlaybackState = PlaybackState.PLAYING;
                updatePlaybackLocation(PlaybackLocation.LOCAL);
                if (startPosition > 0) {
                    mVideoView.seekTo(startPosition);
                }
                mVideoView.start();
                startControllersTimer();
            } else {
                // we should load the video but pause it
                // and show the album art.
                if (mCastManager.isConnected()) {
                    updatePlaybackLocation(PlaybackLocation.REMOTE);
                } else {
                    updatePlaybackLocation(PlaybackLocation.LOCAL);
                }
                mPlaybackState = PlaybackState.PAUSED;
                updatePlayButton(mPlaybackState);
            }
        }
        if (null != mTitleView) {
            updateMetadata(true);
        }
        pageView(mTracker);
    }

    private void colorize(Bitmap photo) {
        Palette palette = Palette.generate(photo);
        applyPalette(palette);
    }

    private void applyPalette(Palette palette) {
        View metaData = findViewById(R.id.metadataView);

        Palette.Swatch vibrant = palette.getVibrantSwatch();
        if (null != vibrant) {
            metaData.setBackgroundColor(palette.getVibrantSwatch().getRgb());
            mTitleView.setTextColor(vibrant.getTitleTextColor());
            mAuthorView.setTextColor(vibrant.getBodyTextColor());
        } else {
            metaData.setBackgroundColor(getResources().getColor(R.color.default_metadata_background));
            mTitleView.setTextColor(getResources().getColor(R.color.primary_text_black));
            mAuthorView.setTextColor(getResources().getColor(R.color.secondary_text_black));
        }
    }

    private void pageView(Tracker tracker) {
        tracker.setScreenName(getString(R.string.ga_sermon_video_view));

        HitBuilders.ScreenViewBuilder screenViewBuilder = new HitBuilders.ScreenViewBuilder();
        try {
            String videoId = mSelectedMedia.getCustomData().getString("id");
            screenViewBuilder.set("&videoId", videoId);
        } catch (JSONException e) {
            // just ignore and go on
        }

        tracker.send(screenViewBuilder.build());
    }

    private void trackEvent(Tracker tracker, String category, String action,
                            String label, Long value) {
        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action);

        if (null != label && !label.trim().isEmpty()) {
            eventBuilder.setLabel(label);
        }
        if (null != value) {
            eventBuilder.setValue(value);
        }

        try {
            String videoId = mSelectedMedia.getCustomData().getString("id");
            eventBuilder.set("&videoId", videoId);
        } catch (JSONException e) {
            // just ignore and go on
        }

        tracker.send(eventBuilder.build());
    }

    private void setupCastListener() {
        mCastConsumer = new VideoCastConsumerImpl() {
            @Override
            public void onApplicationConnected(ApplicationMetadata appMetadata,
                                               String sessionId, boolean wasLaunched) {
                Log.d(TAG, "onApplicationLaunched() is reached");
                if (null != mSelectedMedia) {

                    if (mPlaybackState == PlaybackState.PLAYING) {
                        mVideoView.pause();
                        try {
                            int currentPosition = mVideoView.getCurrentPosition();
                            loadRemoteMedia(currentPosition, true);
                            finish();
                        } catch (Exception e) {
                            com.tmack.sermonstream.utils.Utils
                                    .handleException(SermonVideoActivity.this, e);
                        }
                    } else {
                        updatePlaybackLocation(PlaybackLocation.REMOTE);
                    }
                }
            }

            @Override
            public void onApplicationDisconnected(int errorCode) {
                Log.d(TAG, "onApplicationDisconnected() is reached with errorCode: " + errorCode);
                updatePlaybackLocation(PlaybackLocation.LOCAL);
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "onDisconnected() is reached");
                mPlaybackState = PlaybackState.PAUSED;
                mLocation = PlaybackLocation.LOCAL;
            }

            @Override
            public void onRemoteMediaPlayerMetadataUpdated() {
                try {
                    mRemoteMediaInformation = mCastManager.getRemoteMediaInformation();
                } catch (Exception e) {
                    // silent
                }
            }

            @Override
            public void onFailed(int resourceId, int statusCode) {

            }

            @Override
            public void onConnectionSuspended(int cause) {
                com.tmack.sermonstream.utils.Utils.showToast(SermonVideoActivity.this,
                        R.string.connection_temp_lost);
            }

            @Override
            public void onConnectivityRecovered() {
                com.tmack.sermonstream.utils.Utils.showToast(SermonVideoActivity.this,
                        R.string.connection_recovered);
            }

        };
    }

    private void updatePlaybackLocation(PlaybackLocation location) {
        this.mLocation = location;
        if (location == PlaybackLocation.LOCAL) {
            if (mPlaybackState == PlaybackState.PLAYING ||
                    mPlaybackState == PlaybackState.BUFFERING) {
                setCoverArtStatus(null);
                startControllersTimer();
            } else {
                stopControllersTimer();
                setCoverArtStatus(com.google.sample.castcompanionlibrary.utils.Utils
                        .getImageUrl(mSelectedMedia, 0));
            }
            getSupportActionBar().setTitle("");
        } else {
            stopControllersTimer();
            setCoverArtStatus(com.google.sample.castcompanionlibrary.utils.Utils
                    .getImageUrl(mSelectedMedia, 0));
            updateControllersVisibility(true);
        }
    }

    private void play(int position) {
        startControllersTimer();
        switch (mLocation) {
            case LOCAL:
                mVideoView.seekTo(position);
                mVideoView.start();
                break;
            case REMOTE:
                mPlaybackState = PlaybackState.BUFFERING;
                updatePlayButton(mPlaybackState);
                try {
                    mCastManager.play(position);
                } catch (Exception e) {
                    com.tmack.sermonstream.utils.Utils.
                            handleException(SermonVideoActivity.this, e);
                }
                break;
            default:
                break;
        }
        restartTrickplayTimer();
    }

    private void togglePlayback() {
        stopControllersTimer();
        switch (mPlaybackState) {
            case PAUSED:
                switch (mLocation) {
                    case LOCAL:
                        mVideoView.start();
                        if (!mCastManager.isConnecting()) {
                            Log.d(TAG, "Playing locally...");
                            mCastManager.clearPersistedConnectionInfo(VideoCastManager.CLEAR_SESSION);
                        }
                        mPlaybackState = PlaybackState.PLAYING;
                        startControllersTimer();
                        restartTrickplayTimer();
                        updatePlaybackLocation(PlaybackLocation.LOCAL);
                        break;
                    case REMOTE:
                        try {
                            mCastManager.checkConnectivity();
                            Log.d(TAG, "Playing remotely...");
                            loadRemoteMedia(0, true);
                            finish();
                        } catch (Exception e) {
                            com.tmack.sermonstream.utils.Utils.
                                    handleException(SermonVideoActivity.this, e);
                            return;
                        }
                        break;
                    default:
                        break;
                }
                break;

            case PLAYING:
                mPlaybackState = PlaybackState.PAUSED;
                mVideoView.pause();
                break;

            case IDLE:
                mVideoView.setVideoURI(Uri.parse(mSelectedMedia.getContentId()));
                mVideoView.seekTo(0);
                mVideoView.start();
                mPlaybackState = PlaybackState.PLAYING;
                restartTrickplayTimer();
                break;

            default:
                break;
        }
        updatePlayButton(mPlaybackState);
    }

    private void loadRemoteMedia(int position, boolean autoplay) {
        mCastManager.startCastControllerActivity(this, mSelectedMedia, position, autoplay);
    }

    private void setCoverArtStatus(String url) {
        if (null != url) {
           if (loadtarget == null) loadtarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    // do something with the Bitmap
                    colorize(bitmap);
                    mCoverArt.setImageBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    mCoverArt.setImageDrawable(errorDrawable);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    mCoverArt.setImageDrawable(placeHolderDrawable);
                }
            };

            Picasso.with(this).load(url)
                    .placeholder(R.drawable.default_video)
                    .into(loadtarget);

            // typical Picasso implementation replaced by custom one
            //Picasso.with(this).load(url)
            //        .placeholder(R.drawable.default_video)
            //        .into(mCoverArt);
            mCoverArt.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.INVISIBLE);
        } else {
            mCoverArt.setVisibility(View.GONE);
            mVideoView.setVisibility(View.VISIBLE);
        }
    }

    private void stopTrickplayTimer() {
        Log.d(TAG, "Stopped TrickPlay Timer");
        if (null != mSeekbarTimer) {
            mSeekbarTimer.cancel();
        }
    }

    private void restartTrickplayTimer() {
        stopTrickplayTimer();
        mSeekbarTimer = new Timer();
        mSeekbarTimer.scheduleAtFixedRate(new UpdateSeekbarTask(), 100, 1000);
        Log.d(TAG, "Restarted TrickPlay Timer");
    }

    private void stopControllersTimer() {
        if (null != mControllersTimer) {
            mControllersTimer.cancel();
        }
    }

    private void startControllersTimer() {
        if (null != mControllersTimer) {
            mControllersTimer.cancel();
        }
        if (mLocation == PlaybackLocation.REMOTE) {
            return;
        }
        mControllersTimer = new Timer();
        mControllersTimer.schedule(new HideControllersTask(), 5000);
    }

    // should be called from the main thread
    private void updateControllersVisibility(boolean show) {
        if (show) {
            getSupportActionBar().show();
            mControllers.setVisibility(View.VISIBLE);
        } else {
            getSupportActionBar().hide();
            mControllers.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() was called");
        if (mLocation == PlaybackLocation.LOCAL) {
            if (null != mSeekbarTimer) {
                mSeekbarTimer.cancel();
                mSeekbarTimer = null;
            }
            if (null != mControllersTimer) {
                mControllersTimer.cancel();
            }
            // since we are playing locally, we need to stop the playback of
            // video (if user is not watching, pause it!)
            mVideoView.pause();
            mPlaybackState = PlaybackState.PAUSED;
            updatePlayButton(PlaybackState.PAUSED);
        }
        mCastManager.removeVideoCastConsumer(mCastConsumer);
        mMini.removeOnMiniControllerChangedListener(mCastManager);
        mCastManager.decrementUiCounter();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() was called");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy is called");
        if (null != mCastManager) {
            mMini.removeOnMiniControllerChangedListener(mCastManager);
            mCastManager.removeMiniController(mMini);
            mCastManager.clearContext(this);
            mCastConsumer = null;
        }
        stopControllersTimer();
        stopTrickplayTimer();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart was called");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume was called");
        mCastManager = TheMountApplication.getVideoCastManager(this);
        mCastManager.addVideoCastConsumer(mCastConsumer);
        mCastManager.incrementUiCounter();
        super.onResume();
    }

    private class HideControllersTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateControllersVisibility(false);
                    mControllersVisible = false;
                }
            });
        }
    }

    private class UpdateSeekbarTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ;
                    if (mLocation == PlaybackLocation.LOCAL) {
                        int currentPos = mVideoView.getCurrentPosition();
                        updateSeekbar(currentPos, mDuration);
                    }
                }
            });
        }
    }

    private void setupControlsCallbacks() {
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                Log.e(TAG, "OnErrorListener.onError(): VideoView encountered an " +
                        "error, what: " + what + ", extra: " + extra);
                String message;
                if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                    message = getString(R.string.video_error_media_load_timeout);
                } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                    message = getString(R.string.video_error_server_unaccessible);
                } else {
                    message = getString(R.string.video_error_unknown_error);
                }
                com.google.sample.castcompanionlibrary.utils.Utils
                        .showErrorDialog(SermonVideoActivity.this, message);
                mVideoView.stopPlayback();
                mPlaybackState = PlaybackState.IDLE;
                updatePlayButton(mPlaybackState);
                return true;
            }
        });

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.d(TAG, "onPrepared is reached");
                mDuration = mediaPlayer.getDuration();
                mEndText.setText(com.google.sample.castcompanionlibrary.utils.Utils
                        .formatMillis(mDuration));
                restartTrickplayTimer();
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopTrickplayTimer();
                mPlaybackState = PlaybackState.IDLE;
                updatePlayButton(PlaybackState.IDLE);
            }
        });

        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (!mControllersVisible) {
                    updateControllersVisibility(true);
                }
                startControllersTimer();
                return false;
            }
        });

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mStartText.setText(com.google.sample.castcompanionlibrary.utils.Utils
                        .formatMillis(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopTrickplayTimer();
                mVideoView.pause();
                stopControllersTimer();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mPlaybackState == PlaybackState.PLAYING) {
                    play(seekBar.getProgress());
                } else if (mPlaybackState != PlaybackState.IDLE) {
                    mVideoView.seekTo(seekBar.getProgress());
                }
                startControllersTimer();
            }
        });

        mPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trackEvent(mTracker, getString(R.string.ga_clicks_category),
                        getString(R.string.ga_button_action),
                        getString(R.string.ga_play_pause),
                        (mPlaybackState == PlaybackState.PLAYING) ? Long.valueOf(0) : Long.valueOf(1)); // 0 pause, 1 play
                togglePlayback();
            }
        });
    }

    // TODO: add volume increment functionality

    private void updateSeekbar(int position, int duration) {
        mSeekbar.setProgress(position);
        mSeekbar.setMax(duration);
        mStartText.setText(com.google.sample.castcompanionlibrary.utils.Utils
                .formatMillis(position));
        mEndText.setText(com.google.sample.castcompanionlibrary.utils.Utils
                .formatMillis(duration));
    }

    private void updatePlayButton(PlaybackState state) {
        switch (state) {
            case PLAYING:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_av_pause_dark));
                break;
            case PAUSED:
            case IDLE:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_av_play_dark));
                break;
            case BUFFERING:
                mPlayPause.setVisibility(View.INVISIBLE);
                mLoading.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
            updateMetadata(false);
            mContainer.setBackgroundColor(getResources().getColor(R.color.black));
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            updateMetadata(true);
            mContainer.setBackgroundColor(getResources().getColor(R.color.white));
        }
    }

    private void updateMetadata(boolean visible) {
        if (!visible) {
            mMetadataView.setVisibility(View.GONE);
            mTitleView.setVisibility(View.GONE);
            mAuthorView.setVisibility(View.GONE);
            mDescriptionView.setVisibility(View.GONE);

            // set layout of videoView
            mDisplaySize = Utils.getDisplaySize(this);
            RelativeLayout.LayoutParams layoutParams = new
                    RelativeLayout.LayoutParams(mDisplaySize.x,
                    mDisplaySize.y + getSupportActionBar().getHeight());
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            mVideoView.setLayoutParams(layoutParams);
            mVideoView.invalidate();
        } else {
            MediaMetadata mediaMetadata = mSelectedMedia.getMetadata();
            mTitleView.setText(mediaMetadata.getString(MediaMetadata.KEY_TITLE));
            mAuthorView.setText(mediaMetadata.getString(MediaMetadata.KEY_STUDIO));
            mDescriptionView.setText(Html.fromHtml(mediaMetadata.getString(MediaMetadata.KEY_SUBTITLE)));

            mMetadataView.setVisibility(View.VISIBLE);
            mTitleView.setVisibility(View.VISIBLE);
            mAuthorView.setVisibility(View.VISIBLE);
            mDescriptionView.setVisibility(View.VISIBLE);

            // set layout of videoView
            mDisplaySize = Utils.getDisplaySize(this);
            RelativeLayout.LayoutParams layoutParams = new
                    RelativeLayout.LayoutParams(mDisplaySize.x,
                    (int) (mDisplaySize.x * ASPECT_RATIO));
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            mVideoView.setLayoutParams(layoutParams);
            mVideoView.invalidate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(SermonVideoActivity.this, CastPreference.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    private void setupToolbar() {
        // Configure the Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // TODO: animate to fade in/out as user scrolls (if ever implemented ScrollView on this screen)

        // Handle Back Navigation
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SermonVideoActivity.this.onBackPressed();
            }
        });
    }

    private void setupMiniController() {
        mMini = (MiniController) findViewById(R.id.miniController1);
        mCastManager.addMiniController(mMini);
    }

    private void loadViews() {
        mVideoView = (VideoView) findViewById(R.id.videoView1);
        mCoverArt = (ImageView) findViewById(R.id.coverArtView);
        mTitleView = (TextView) findViewById(R.id.titleView1);
        mAuthorView = (TextView) findViewById(R.id.artistView);
        mDescriptionView = (TextView) findViewById(R.id.descriptionView);
        mDescriptionView.setMovementMethod(new ScrollingMovementMethod());

        mStartText = (TextView) findViewById(R.id.startText);
        mEndText = (TextView) findViewById(R.id.endText);
        mSeekbar = (SeekBar) findViewById(R.id.seekBar1);
        mPlayPause = (ImageView) findViewById(R.id.playPauseView1);
        mLoading = (ProgressBar) findViewById(R.id.loadingView);

        // load containers
        mContainer = findViewById(R.id.container);
        mControllers = findViewById(R.id.controllers);
        mMetadataView = findViewById(R.id.metadataView);
    }
}
