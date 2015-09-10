package com.tmack.pocketsermons.tvleanback.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.tmack.pocketsermons.common.utils.Utils;
import com.tmack.pocketsermons.data.VideoProvider;
import com.tmack.pocketsermons.tvleanback.R;
import com.tmack.pocketsermons.common.model.Sermon;

/**
 * PlaybackOverlayActivity for video playback that loads PlaybackOverlayFragment
 *
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class PlaybackActivity extends Activity {
    public static final String AUTO_PLAY = "auto_play";
    private static final String TAG = PlaybackActivity.class.getSimpleName();
    private static final int SEEK_DURATION = 10 * 1000; // 10 seconds

    private VideoView mVideoView;
    private MediaSession mSession;
    private String mCurrentVideoPath;
    private int mPosition = 0;
    private long mStartTimeMillis;
    private long mDuration = -1;

    private LeanbackPlaybackState mPlaybackState = LeanbackPlaybackState.IDLE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createMediaSession();

        setContentView(R.layout.playback_controls);
        loadViews();
        playPause(true);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy() was called");
        super.onDestroy();
        stopPlayback();
        mVideoView.suspend();
        mSession.release();
    }

    private void setPosition(int position) {
        if (position > mDuration) {
            mPosition = (int) mDuration;
        } else if (position < 0) {
            mPosition = 0;
        } else {
            mPosition = position;
        }
        mStartTimeMillis = System.currentTimeMillis();
        Log.d(TAG, "position set to " + mPosition);
    }

    private void createMediaSession() {
        if (mSession == null) {
            mSession = new MediaSession(this, "PocketSermons");
            mSession.setCallback(new MediaSessionCallback());
            mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                    MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

            mSession.setActive(true);

            setMediaController(new MediaController(this, mSession.getSessionToken()));
        }
    }

    private void playPause(boolean doPlay) {
        if (mPlaybackState == LeanbackPlaybackState.IDLE) {
            setupCallbacks();
        }

        if (doPlay && mPlaybackState != LeanbackPlaybackState.PLAYING) {
            mPlaybackState = LeanbackPlaybackState.PLAYING;
            if (mPosition > 0) {
                mVideoView.seekTo(mPosition);
            }
            mVideoView.start();
            mStartTimeMillis = System.currentTimeMillis();
        } else {
            mPlaybackState = LeanbackPlaybackState.PAUSED;
            int timeElapsedSinceStart = (int) (System.currentTimeMillis() - mStartTimeMillis);
            setPosition(mPosition + timeElapsedSinceStart);
            mVideoView.pause();
        }
        updatePlaybackState();
    }

    private void updatePlaybackState() {
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(getAvailableActions());
        int state = PlaybackState.STATE_PLAYING;
        if (mPlaybackState == LeanbackPlaybackState.PAUSED ||
                mPlaybackState == LeanbackPlaybackState.IDLE) {
            state = PlaybackState.STATE_PAUSED;
        }
        stateBuilder.setState(state, mPosition, 1.0f);
        mSession.setPlaybackState(stateBuilder.build());
    }

    private long getAvailableActions() {
        long actions = PlaybackState.ACTION_PLAY |
                PlaybackState.ACTION_PLAY_FROM_MEDIA_ID;

        if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
            actions |= PlaybackState.ACTION_PAUSE;
        }

        return actions;
    }

    private void updateMetadata(final Sermon sermon) {
        final MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();

        String title = sermon.getTitle().replace("_", " -");

        metadataBuilder.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, sermon.getId().toString());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, sermon.getTitle());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, sermon.getStudio());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION,
                sermon.getDescription());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, sermon.getCardImageUrl());
        metadataBuilder.putLong(MediaMetadata.METADATA_KEY_DURATION, mDuration);

        // And at minimum the title and artist for legacy support
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, title);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, sermon.getStudio());

        Glide.with(this)
                .load(Uri.parse(sermon.getCardImageUrl()))
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(500, 500) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap);
                        mSession.setMetadata(metadataBuilder.build());
                    }
                });

        mSession.setMetadata(metadataBuilder.build());
    }

    private void loadViews() {
        mVideoView = (VideoView) findViewById(R.id.videoView);
        mVideoView.setFocusable(false);
        mVideoView.setFocusableInTouchMode(false);

        Sermon sermon = getIntent().getParcelableExtra(DetailsActivity.MEDIA);
        setVideoPath(sermon.getVideoUrl());
        updateMetadata(sermon);
    }

    private void setupCallbacks() {
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                String msg;
                if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                    msg = getString(R.string.video_error_media_load_timeout);
                } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                    msg = getString(R.string.video_error_server_unaccessible);
                } else {
                    msg = getString(R.string.video_error_unknown_error);
                }
                Utils.showToast(PlaybackActivity.this, msg);
                mVideoView.stopPlayback();
                mPlaybackState = LeanbackPlaybackState.IDLE;
                return false;
            }
        });

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
                    mVideoView.start();
                }
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlaybackState = LeanbackPlaybackState.IDLE;
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() was called");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause() was called");
        super.onPause();
        if (mVideoView.isPlaying()) {
            if (!requestVisibleBehind(true)) {
                // Try to play behind launcher, but if it fails, stop playback.
                playPause(false);
            }
        } else {
            requestVisibleBehind(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "pausing playback in onStop()");
        playPause(false);
    }

    @Override
    public void onVisibleBehindCanceled() {
        Log.d(TAG, "pausing playback in onVisibleBehindCanceled()");
        playPause(false);
        super.onVisibleBehindCanceled();
    }

    private void stopPlayback() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }

    /*
     * List of various states that we can be in
     */
    public enum LeanbackPlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE;
    }

    private class MediaSessionCallback extends MediaSession.Callback {
        @Override
        public void onPlay() {
            playPause(true);
        }

        @Override
        public void onPause() {
            playPause(false);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Sermon sermon = Sermon.fromMediaInfo(VideoProvider.getMedia(mediaId));
            if (sermon != null) {
                setVideoPath(sermon.getVideoUrl());
                mPlaybackState = LeanbackPlaybackState.PAUSED;
                updateMetadata(sermon);
                playPause(extras.getBoolean(AUTO_PLAY));
            }
        }

        @Override
        public void onSeekTo(long pos) {
            setPosition((int) pos);
            mVideoView.seekTo(mPosition);
            updatePlaybackState();
        }

        @Override
        public void onFastForward() {
            Log.d(TAG, "received fastForward in MediaSession.Callback");
            if (mDuration != -1) {
                setPosition(mVideoView.getCurrentPosition() + SEEK_DURATION);
                mVideoView.seekTo(mPosition);
                updatePlaybackState();
            }
        }

        @Override
        public void onRewind() {
            Log.d(TAG, "received rewind in MediaSession.Callback");
            setPosition(mVideoView.getCurrentPosition() - SEEK_DURATION);
            mVideoView.seekTo(mPosition);
            updatePlaybackState();
        }
    }

    private void setVideoPath(String videoUrl) {
        mVideoView.setVideoPath(videoUrl);
        mCurrentVideoPath = videoUrl;
        mStartTimeMillis = 0;
        mDuration = Utils.getDuration(videoUrl);
    }
}
