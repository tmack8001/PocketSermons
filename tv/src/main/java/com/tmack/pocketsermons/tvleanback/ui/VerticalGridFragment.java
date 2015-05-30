package com.tmack.pocketsermons.tvleanback.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;

import com.google.android.gms.cast.MediaInfo;
import com.tmack.pocketsermons.tvleanback.R;
import com.tmack.pocketsermons.common.model.Sermon;
import com.tmack.pocketsermons.data.VideoProvider;
import com.tmack.pocketsermons.tvleanback.presenter.CardPresenter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class VerticalGridFragment extends android.support.v17.leanback.app.VerticalGridFragment {
    private static final String TAG = "VerticalGridFragment";

    private static final int NUM_COLUMNS = 5;

    private ArrayObjectAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.vertical_grid_title));

        setupFragment();
    }

    private void setupFragment() {
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        mAdapter = new ArrayObjectAdapter(new CardPresenter());

        long seed = System.nanoTime();

        Map<String, List<MediaInfo>> mediaList = VideoProvider.getMediaListBySeries();

        for (Map.Entry<String, List<MediaInfo>> entry : mediaList.entrySet()) {
            List<MediaInfo> list = entry.getValue();
            Collections.shuffle(list, new Random(seed));
            for (MediaInfo media : list) {
                mAdapter.add(media);
            }
        }

        setAdapter(mAdapter);

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof MediaInfo) {
                Sermon sermon = Sermon.fromMediaInfo((MediaInfo) item);
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.MEDIA, sermon);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);
            }
        }
    }


    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
        }
    }

}
