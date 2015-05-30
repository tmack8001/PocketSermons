package com.tmack.pocketsermons;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.utils.Utils;
import com.tmack.pocketsermons.common.PocketSermonsApplication;
import com.tmack.pocketsermons.settings.CastPreference;

/**
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class PocketSermonsMobileApplication extends PocketSermonsApplication {
    public static final double VOLUME_INCREMENT = 0.05;

    private static VideoCastManager sCastManager = null;

    /*
     * (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        APPLICATION_ID = getString(R.string.app_id);
        PROPERTY_ID = getString(R.string.ga_property_id);

        initializeCastManager();
        Utils.saveFloatToPreference(getApplicationContext(),
                VideoCastManager.PREFS_KEY_VOLUME_INCREMENT, (float) VOLUME_INCREMENT);

        sInstance = this;
    }

    public static synchronized PocketSermonsMobileApplication getInstance() {
        return (PocketSermonsMobileApplication) PocketSermonsMobileApplication.sInstance;
    }

    private void initializeCastManager() {
        sCastManager = VideoCastManager.initialize(getApplicationContext(), APPLICATION_ID, null, null);
        sCastManager.enableFeatures(
                VideoCastManager.FEATURE_NOTIFICATION |
                        VideoCastManager.FEATURE_LOCKSCREEN |
                        VideoCastManager.FEATURE_WIFI_RECONNECT |
                        VideoCastManager.FEATURE_CAPTIONS_PREFERENCE |
                        VideoCastManager.FEATURE_DEBUGGING);
        String destroyOnExitStr = com.google.sample.castcompanionlibrary.utils.Utils.getStringFromPreference(getApplicationContext(),
                CastPreference.TERMINATION_POLICY_KEY);
        sCastManager.setStopOnDisconnect(null != destroyOnExitStr
                && CastPreference.STOP_ON_DISCONNECT.equals(destroyOnExitStr));
    }

    public static VideoCastManager getCastManager() {
        if (sCastManager == null) {
            throw new IllegalStateException("Application has not been started");
        }
        return sCastManager;
    }
}
