package com.tmack.pocketsermons.browser;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.libraries.cast.companionlibrary.utils.Utils;
import com.tmack.pocketsermons.R;
import com.tmack.pocketsermons.data.VideoItemListLoader;
import com.tmack.pocketsermons.mediaPlayer.LocalVideoActivity;

import java.util.List;

/**
 * A list fragment representing a list of Sermons. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link SermonBrowserListFragment}.
 */
public class SermonBrowserListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<List<MediaInfo>> {

    private static final String TAG = "SermonBrowserListFragment";
    private SermonListAdapter mAdapter;

    public static SermonBrowserListFragment newInstance() {
        SermonBrowserListFragment fragment = new SermonBrowserListFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static SermonBrowserListFragment newInstance(Bundle bundle) {
        SermonBrowserListFragment fragment = new SermonBrowserListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setFastScrollEnabled(true);
        mAdapter = new SermonListAdapter(getActivity());
        setEmptyText(getString(com.tmack.pocketsermons.common.R.string.no_sermon_found));
        setListAdapter(mAdapter);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
     * android.os.Bundle)
     */
    @Override
    public Loader<List<MediaInfo>> onCreateLoader(int i, Bundle bundle) {
        return new VideoItemListLoader(getActivity());
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android
     * .support.v4.content.Loader, java.lang.Object)
     */
    @Override
    public void onLoadFinished(Loader<List<MediaInfo>> loader, List<MediaInfo> data) {
        mAdapter.setData(data);
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android
     * .support.v4.content.Loader)
     */
    @Override
    public void onLoaderReset(Loader<List<MediaInfo>> listLoader) {
        mAdapter.setData(null);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView,
     * android.view.View, int, long)
     */
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        MediaInfo selectedMedia = mAdapter.getItem(position);
        handleNavigation(selectedMedia, view, false);
    }

    private void handleNavigation(MediaInfo selectedMedia, View view, boolean autoStart) {
        Intent intent = new Intent(getActivity(), LocalVideoActivity.class);
        intent.putExtra("media", Utils.mediaInfoToBundle(selectedMedia));
        intent.putExtra("shouldStart", autoStart);
        // create the transition animation - the images in the layouts
        // of both activities are defined with android:transitionName="transtion_image_hero"
        String transitionName = getString(R.string.transition_image_hero);
        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                view.findViewById(R.id.imageView1), transitionName);
        // start the new activity
        getActivity().startActivity(intent, activityOptions.toBundle());
    }
}
