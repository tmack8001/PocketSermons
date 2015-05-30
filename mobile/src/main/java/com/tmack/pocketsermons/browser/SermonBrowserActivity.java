package com.tmack.pocketsermons.browser;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.media.MediaRouter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.libraries.cast.companionlibrary.cast.BaseCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumer;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.widgets.MiniController;
import com.tmack.pocketsermons.PocketSermonsMobileApplication;
import com.tmack.pocketsermons.R;
import com.tmack.pocketsermons.common.PocketSermonsApplication;
import com.tmack.pocketsermons.common.utils.Utils;
import com.tmack.pocketsermons.mediaPlayer.LocalVideoActivity;
import com.tmack.pocketsermons.settings.CastPreference;


/**
 * An activity representing a list of Sermons. This activity presents a list of
 * items, which when touched, lead to a {@link LocalVideoActivity}
 * representing item details and video resource.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link SermonBrowserListFragment}.
 */
public class SermonBrowserActivity extends ActionBarActivity {

    private static final String TAG = "SermonBrowseActivity";

    private Toolbar mToolbar;

    private VideoCastManager mCastManager;
    private VideoCastConsumer mCastConsumer;
    private MiniController mMini;
    private MenuItem mediaRouteMenuItem;
    private boolean mIsHoneyCombOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check minimum Google Play Services version
        BaseCastManager.checkGooglePlayServices(this);
        setContentView(R.layout.sermon_browser);

        // TODO: If exposing deep links into your app, handle intents here.

        mCastManager = VideoCastManager.getInstance();
        mTracker = PocketSermonsApplication.getTracker(PocketSermonsApplication.TrackerName.APP_TRACKER, getApplicationContext());

        setupToolbar();
        setupMiniController();
        setupCastListener();

        // reconnect session with ChromeCast if Activity was killed or disconnected
        mCastManager.reconnectSessionIfPossible(20);

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
                Utils.
                        showToast(SermonBrowserActivity.this, R.string.connection_temp_lost);
            }

            @Override
            public void onConnectivityRecovered() {
                Utils.
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

            @Override
            public void onConnectionFailed(ConnectionResult result) {
                Log.d(TAG, "onConnectionFailed(): " + result.toString());
                Utils.showToast(SermonBrowserActivity.this, R.string.failed_to_connect);
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

    /**
     * The getActionView() method used in this method requires API 11 or above. If one needs to
     * extend this below that version, one possible solution could be using reflection and such.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showFtuOverlay() {
        Menu menu = mToolbar.getMenu();
        View view = menu.findItem(R.id.media_route_menu_item).getActionView();
        if (view != null && view instanceof MediaRouteButton) {
            new ShowcaseView.Builder(this)
                    .setTarget(new ViewTarget(view))
                    .setContentTitle(R.string.touch_to_cast)
                    .build();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mCastManager.onDispatchVolumeKeyEvent(event, PocketSermonsMobileApplication.VOLUME_INCREMENT)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() was called");
        mCastManager = VideoCastManager.getInstance();
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
