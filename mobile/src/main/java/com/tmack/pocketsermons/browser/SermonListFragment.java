package com.tmack.pocketsermons.browser;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.libraries.cast.companionlibrary.utils.Utils;
import com.squareup.picasso.Picasso;
import com.tmack.pocketsermons.R;
import com.tmack.pocketsermons.common.model.Sermon;
import com.tmack.pocketsermons.data.VideoItemListLoader;
import com.tmack.pocketsermons.data.VideoProvider;
import com.tmack.pocketsermons.mediaPlayer.LocalVideoActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * A list fragment representing a list of Sermons. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link SermonListFragment}.
 */
public class SermonListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<List<MediaInfo>> {

    private final String TAG = getClass().getSimpleName();

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private SimpleSermonRecyclerViewAdapter mAdapter;
    private List<MediaInfo> mMediaInfo = new ArrayList<>();

    private boolean loading;
    private int visibleItemCount, visibleThreshold, previousTotal, totalItemCount, firstVisibleItem;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_sermon_list, container, false);
        mLayoutManager = new LinearLayoutManager(mRecyclerView.getContext());

        //setup variables
        visibleThreshold = 5;

        setupRecyclerView();

        return mRecyclerView;
    }

    private void setupRecyclerView() {
        mRecyclerView.setLayoutManager(mLayoutManager);

        // set on scroll listener to load more data when close to bottom of list
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {
                    Log.i(TAG, "end of list reached");

                    // fetch more

                    loading = true;
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadVideoData();
    }

    private void loadVideoData() {
        VideoProvider.setContext(getActivity());
        getLoaderManager().initLoader(0, null, this);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
     * android.os.Bundle)
     */
    @Override
    public Loader<List<MediaInfo>> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG, "VideoItemListLoader created");
        return new VideoItemListLoader(getActivity());
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android
     * .support.v4.content.Loader, java.lang.Object)
     */
    @Override
    public void onLoadFinished(Loader<List<MediaInfo>> loader, List<MediaInfo> data) {
        mAdapter = new SimpleSermonRecyclerViewAdapter(getActivity(), data);
        mRecyclerView.setAdapter(mAdapter);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android
     * .support.v4.content.Loader)
     */
    @Override
    public void onLoaderReset(Loader<List<MediaInfo>> listLoader) {
        //mAdapter.mValues = null;
    }

    protected static void handleNavigation(Context context, MediaInfo selectedMedia, View view, boolean autoStart) {
        Intent intent = new Intent(context, LocalVideoActivity.class);
        intent.putExtra("media", Utils.mediaInfoToBundle(selectedMedia));
        intent.putExtra("shouldStart", autoStart);
        // create the transition animation - the images in the layouts
        // of both activities are defined with android:transitionName="transtion_image_hero"
        String transitionName = context.getString(R.string.transition_image_hero);
        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) context,
                view.findViewById(R.id.imageView1), transitionName);
        // start the new activity
        context.startActivity(intent, activityOptions.toBundle());
    }

    private static class SimpleSermonRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleSermonRecyclerViewAdapter.ViewHolder> {

        private List<MediaInfo> mValues;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mTitleView;
            public final TextView mDescriptionView;
            public final ImageView mImageView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mTitleView = (TextView) view.findViewById(R.id.textView1);
                mDescriptionView = (TextView) view.findViewById(R.id.textView2);
                mImageView = (ImageView) view.findViewById(R.id.imageView1);
            }
        }

        public MediaInfo getValueAt(int position) {
            return mValues.get(position);
        }

        public SimpleSermonRecyclerViewAdapter(Context context, List<MediaInfo> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sermon_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final MediaInfo mediaInfo = mValues.get(position);
            Sermon sermon = Sermon.fromMediaInfo(mediaInfo);

            holder.mTitleView.setText(sermon.getTitle());
            holder.mDescriptionView.setText(Html.fromHtml(sermon.getDescription()));

            // start detail activity
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SermonListFragment.handleNavigation(v.getContext(),
                            mediaInfo, holder.mView, false);
                }
            });

            // load image into imageView
            Picasso.with(holder.mView.getContext())
                    .load(sermon.getBackgroundImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .fit().into(holder.mImageView);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }
    }
}
