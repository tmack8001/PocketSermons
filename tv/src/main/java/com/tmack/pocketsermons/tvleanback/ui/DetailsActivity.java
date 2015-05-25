package com.tmack.pocketsermons.tvleanback.ui;

import android.app.Activity;
import android.os.Bundle;

import com.tmack.pocketsermons.tvleanback.R;

public class DetailsActivity extends Activity {

    public static final String SHARED_ELEMENT_NAME = "hero";
    public static final String MEDIA = "Media";
    public static final String NOTIFICATION_ID = "NotificationId";

    /* Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details);
    }
}
