package com.tmack.pocketsermons.tvleanback.ui;

import android.app.Activity;
import android.os.Bundle;

import com.tmack.pocketsermons.tvleanback.R;

/**
 * VerticalGridActivity that loads VerticalGridFragment
 *
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class VerticalGridActivity extends Activity {

    /*
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vertical_grid);
        getWindow().setBackgroundDrawableResource(R.drawable.grid_bg);
    }
}
