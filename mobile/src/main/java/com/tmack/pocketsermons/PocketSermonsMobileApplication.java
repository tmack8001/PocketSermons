package com.tmack.pocketsermons;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.player.VideoCastController;
import com.tmack.pocketsermons.common.PocketSermonsApplication;
import java.util.Locale;

/**
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class PocketSermonsMobileApplication extends PocketSermonsApplication {
    public static final double VOLUME_INCREMENT = 0.05;

    /*
     * (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // initialize VideoCastManager; access via singleton VideoCastManager.getInstance()
        VideoCastManager
                .initialize(this, APPLICATION_ID, null, null)
                .setVolumeStep(VOLUME_INCREMENT)
                .enableFeatures(VideoCastManager.FEATURE_NOTIFICATION |
                        VideoCastManager.FEATURE_LOCKSCREEN |
                        VideoCastManager.FEATURE_WIFI_RECONNECT |
                        VideoCastManager.FEATURE_AUTO_RECONNECT |
                        VideoCastManager.FEATURE_DEBUGGING);

        // this is the default behavior but is mentioned to make it clear that it is configurable.
        VideoCastManager.getInstance().setNextPreviousVisibilityPolicy(
            VideoCastController.NEXT_PREV_VISIBILITY_POLICY_DISABLED);

        // this is to set the launch options, the following values are the default values
        VideoCastManager.getInstance().setLaunchOptions(false, Locale.getDefault());

        // this is the default behavior but is mentioned to make it clear that it is configurable.
        VideoCastManager.getInstance().setCastControllerImmersive(true);
    }

    public static synchronized PocketSermonsMobileApplication getInstance() {
        return (PocketSermonsMobileApplication) PocketSermonsMobileApplication.sInstance;
    }
}
