package com.tmack.sermonstream.browser;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.media.MediaRouter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.sample.castcompanionlibrary.cast.BaseCastManager;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.IVideoCastConsumer;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.widgets.MiniController;
import com.tmack.sermonstream.R;
import com.tmack.sermonstream.TheMountApplication;
import com.tmack.sermonstream.settings.CastPreference;


/**
 * An activity representing a list of Sermons. This activity presents a list of
 * items, which when touched, lead to a {@link com.tmack.sermonstream.mediaPlayer.SermonVideoActivity}
 * representing item details and video resource.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link SermonBrowserListFragment}.
 */
public class SermonBrowserActivity extends ActionBarActivity {

    private static final String TAG = "SermonBrowseActivity";

    private Toolbar mToolbar;

    private VideoCastManager mCastManager;
    private IVideoCastConsumer mCastConsumer;
    private MiniController mMini;
    private MenuItem mediaRouteMenuItem;
    boolean mIsHoneyCombOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check minimum Google Play Services version
        BaseCastManager.checkGooglePlayServices(this);
        setContentView(R.layout.sermon_browser);

        // TODO: If exposing deep links into your app, handle intents here.

        mCastManager = TheMountApplication.getVideoCastManager(getApplicationContext());
        mTracker = TheMountApplication.getTracker(TheMountApplication.TrackerName.APP_TRACKER, getApplicationContext());

        setupToolbar();
        setupMiniController();
        setupCastListener();

        // reconnect session with ChromeCast if Activity was killed or disconnected
        mCastManager.reconnectSessionIfPossible(this, false);

        pageView(mTracker);
    }

    private void pageView(Tracker tracker) {
        tracker.setScreenName(getString(R.string.ga_sermon_video_view));
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void setupCastListener() {
        mCastConsumer = new VideoCastConsumerImpl() {

            @Override
            public void onFailed(int resourceId, int statusCode) {

            }

            @Override
            public void onConnectionSuspended(int cause) {
                Log.d(TAG, "onConnectionSuspended() was called with cause: " + cause);
                com.tmack.sermonstream.utils.Utils.
                        showToast(SermonBrowserActivity.this, R.string.connection_temp_lost);
            }

            @Override
            public void onConnectivityRecovered() {
                com.tmack.sermonstream.utils.Utils.
                        showToast(SermonBrowserActivity.this, R.string.connection_recovered);
            }

            @Override
            public void onCastDeviceDetected(final MediaRouter.RouteInfo info) {
                if (!CastPreference.isFtuShown(SermonBrowserActivity.this) && mIsHoneyCombOrAbove) {
                    CastPreference.setFtuShown(SermonBrowserActivity.this);

                    Log.d(TAG, "Route is visible: " + info);
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (mediaRouteMenuItem.isVisible()) {
                                Log.d(TAG, "Cast Icon is visible: " + info.getName());
                                showFtuOverlay();
                            }
                        }
                    }, 1000);
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        mediaRouteMenuItem = mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(SermonBrowserActivity.this, CastPreference.class);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                break;
        }
        return true;
    }

    private void showFtuOverlay() {
        new ShowcaseView.Builder(this)
                .setTarget(new ActionViewTarget(this, ActionViewTarget.Type.MEDIA_ROUTE_BUTTON))
                .setContentTitle(R.string.touch_to_cast)
                .build();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() was called");
        mCastManager = TheMountApplication.getVideoCastManager(this);
        if (null != mCastManager) {
            mCastManager.addVideoCastConsumer(mCastConsumer);
            mCastManager.incrementUiCounter();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        mCastManager.decrementUiCounter();
        mCastManager.removeVideoCastConsumer(mCastConsumer);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy is called");
        if (null != mCastManager) {
            mMini.removeOnMiniControllerChangedListener(mCastManager);
            mCastManager.removeMiniController(mMini);
            mCastManager.clearContext(this);
        }
        super.onDestroy();
    }

    private void setupToolbar() {
        // Configure the Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }

        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    private void setupMiniController() {
        mMini = (MiniController) findViewById(R.id.miniController1);
        mCastManager.addMiniController(mMini);
    }
}
