package com.tmack.pocketsermons.utils;

import android.content.Context;

import com.google.android.libraries.cast.companionlibrary.cast.exceptions.NoConnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.tmack.pocketsermons.R;
import com.tmack.pocketsermons.common.utils.Utils;

import junit.framework.TestCase;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Utils.class)
public class CastExceptionUtilsTest extends TestCase {

    @Mock
    private Context context;

    @Override
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    public void testHandleTransientNetworkDisconnectionException() throws Exception {
        PowerMockito.spy(Utils.class);
        PowerMockito.doNothing().when(Utils.class, "showOopsDialog", Mockito.eq(context), Mockito.eq(R.string.connection_lost_retry));
        CastExceptionUtils.handleException(context, new TransientNetworkDisconnectionException());
        PowerMockito.verifyStatic(Mockito.times(1));
    }

    public void testHandleNoConnectionException() throws Exception {
        PowerMockito.spy(Utils.class);
        PowerMockito.doNothing().when(Utils.class, "showOopsDialog", Mockito.eq(context), Mockito.eq(R.string.connection_lost));
        CastExceptionUtils.handleException(context, new NoConnectionException());
        PowerMockito.verifyStatic(Mockito.times(1));
    }

    public void testHandleRuntimeException() throws Exception {
        PowerMockito.spy(Utils.class);
        PowerMockito.doNothing().when(Utils.class, "showOopsDialog", Mockito.eq(context), Mockito.eq(R.string.failed_to_perform_action));
        CastExceptionUtils.handleException(context, new RuntimeException());
        PowerMockito.verifyStatic(Mockito.times(1));
    }

    public void testHandleUnknownException() throws Exception {
        PowerMockito.spy(Utils.class);
        PowerMockito.doNothing().when(Utils.class, "showOopsDialog", Mockito.eq(context), Mockito.eq(R.string.failed_to_perform_action));
        CastExceptionUtils.handleException(context, new Exception());
        PowerMockito.verifyStatic(Mockito.times(1));
    }
}