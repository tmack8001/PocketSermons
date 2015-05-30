package com.tmack.pocketsermons.utils;

import android.content.Context;

import com.google.sample.castcompanionlibrary.cast.exceptions.CastException;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.tmack.pocketsermons.R;
import com.tmack.pocketsermons.common.utils.Utils;

import java.io.IOException;

/**
 * Utilities for Cast Exceptions
 *
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class CastExceptionUtils {

    /*
     * Making sure public utility methods remain static
     */
    private CastExceptionUtils() {
    }

    /**
     * A utility method to handle a few types of exceptions that are commonly thrown by the cast
     * APIs in this library. It has special treatments for
     * {@link com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException}, {@link com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException} and shows an
     * "Oops" dialog conveying certain messages to the user. The following resource IDs can be used
     * to control the messages that are shown:
     * <p/>
     * <ul>
     * <li><code>R.string.connection_lost_retry</code></li>
     * <li><code>R.string.connection_lost</code></li>
     * <li><code>R.string.failed_to_perform_action</code></li>
     * </ul>
     *
     * @param context
     * @param e
     */
    public static void handleException(Context context, Exception e) {
        int resourceId = 0;
        if (e instanceof TransientNetworkDisconnectionException) {
            // temporary loss of connectivity
            resourceId = R.string.connection_lost_retry;

        } else if (e instanceof NoConnectionException) {
            // connection gone
            resourceId = R.string.connection_lost;
        } else if (e instanceof RuntimeException ||
                e instanceof IOException ||
                e instanceof CastException) {
            // something more serious happened
            resourceId = R.string.failed_to_perform_action;
        } else {
            // well, who knows!
            resourceId = R.string.failed_to_perform_action;
        }
        if (resourceId > 0) {
            Utils.showOopsDialog(context, resourceId);
        }
    }
}
