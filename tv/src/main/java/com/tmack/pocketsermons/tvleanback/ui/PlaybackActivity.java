package com.tmack.pocketsermons.tvleanback.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.tmack.pocketsermons.tvleanback.R;
import com.tmack.pocketsermons.common.model.Sermon;

/**
 * PlaybackOverlayActivity for video playback that loads PlaybackOverlayFragment
 *
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class PlaybackActivity extends Activity implements
        PlaybackOverlayFragment.OnPlayPauseClickedListener {

    private static final String TAG = "PlaybackActivity";

    private VideoView mVideoView;
    private MediaSession mSession;
    private LeanbackPlaybackState mPlaybackState = LeanbackPlaybackState.IDLE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playback_controls);

        loadViews();

        mSession = new MediaSession(this, "PocketSermons");
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mSession.setActive(true);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy() was called");
        super.onDestroy();
        mVideoView.suspend();
    }

    /*
     * Implementation of OnPlayPauseClickedListener
     */
    @Override
    public void onFragmentPlayPause(Sermon sermon, int position, Boolean playPause) {
        mVideoView.setVideoPath(sermon.getVideoUrl());

        if (position == 0 || mPlaybackState == LeanbackPlaybackState.IDLE) {
            setupCallbacks();
            mPlaybackState = LeanbackPlaybackState.IDLE;
        }

        if (playPause && mPlaybackState != LeanbackPlaybackState.PLAYING) {
            mPlaybackState = LeanbackPlaybackState.PLAYING;
            if (position > 0) {
                mVideoView.seekTo(position);
                mVideoView.start();
            }
        } else {
            mPlaybackState = LeanbackPlaybackState.PAUSED;
            mVideoView.pause();
        }

        updatePlaybackState(position);
        updateMetadata(sermon);
    }

    /*
     * Implementation of OnPlayPauseClickedListener
     */
    @Override
    public void onFragmentFfwRwd(Sermon sermon, int position) {
        mVideoView.setVideoPath(sermon.getVideoUrl());

        Log.d(TAG, "seek current time: " + position);
        if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
            if (position > 0) {
                mVideoView.seekTo(position);
                mVideoView.start();
            }
        }
    }

    private void updatePlaybackState(int position) {
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(getAvailableActions());
        int state = PlaybackState.STATE_PLAYING;
        if (mPlaybackState == LeanbackPlaybackState.PAUSED) {
            state = PlaybackState.STATE_PAUSED;
        }
        stateBuilder.setState(state, position, 1.0f);
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

        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, sermon.getTitle());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, sermon.getDescription());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, sermon.getCardImageUrl());

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
    }

    private void setupCallbacks() {
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                String msg = "";
                if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                    msg = getString(R.string.video_error_media_load_timeout);
                } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                    msg = getString(R.string.video_error_server_unaccessible);
                } else {
                    msg = getString(R.string.video_error_unknown_error);
                }
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
                stopPlayback();
            }
        } else {
            requestVisibleBehind(false);
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() was called");
        super.onStop();
        mSession.release();
    }

    @Override
    public void onVisibleBehindCanceled() {
        Log.d(TAG, "onVisibleBheindCanceled() was called");
        super.onVisibleBehindCanceled();
        stopPlayback();
    }

    private void stopPlayback() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        PlaybackOverlayFragment playbackOverlayFragment = (PlaybackOverlayFragment) getFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                playbackOverlayFragment.togglePlayback(false);
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                playbackOverlayFragment.togglePlayback(false);
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
                    playbackOverlayFragment.togglePlayback(false);
                } else {
                    playbackOverlayFragment.togglePlayback(true);
                }
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    /*
     * List of various states that we can be in
     */
    public enum LeanbackPlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE;
    }

    private class MediaSessionCallback extends MediaSession.Callback {
    }
}
