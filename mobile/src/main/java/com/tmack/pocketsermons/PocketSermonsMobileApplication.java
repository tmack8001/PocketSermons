package com.tmack.pocketsermons;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.utils.Utils;
import com.tmack.pocketsermons.common.PocketSermonsApplication;
import com.tmack.pocketsermons.settings.CastPreference;

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
        APPLICATION_ID = getString(R.string.app_id);
        PROPERTY_ID = getString(R.string.ga_property_id);

        // initialize VideoCastManager; access via singleton VideoCastManager.getInstance()
        VideoCastManager
                .initialize(this, APPLICATION_ID, null, null)
                .setVolumeStep(VOLUME_INCREMENT)
                .enableFeatures(VideoCastManager.FEATURE_NOTIFICATION |
                        VideoCastManager.FEATURE_LOCKSCREEN |
                        VideoCastManager.FEATURE_WIFI_RECONNECT |
                        VideoCastManager.FEATURE_DEBUGGING);
    }

    public static synchronized PocketSermonsMobileApplication getInstance() {
        return (PocketSermonsMobileApplication) PocketSermonsMobileApplication.sInstance;
    }
}
