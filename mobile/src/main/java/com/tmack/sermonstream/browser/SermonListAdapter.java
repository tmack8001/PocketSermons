package com.tmack.sermonstream.browser;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.squareup.picasso.Picasso;
import com.tmack.sermonstream.R;

import java.util.List;

/**
 * An {@link ArrayAdapter} to populate the list of sermons.
 */
// TODO: change to RecyclerView
public class SermonListAdapter extends ArrayAdapter<MediaInfo> {

    private final Context mContext;

    /**
     * Constructor to create an {@link ArrayAdapter}
     * @param context   the context of the adapter
     */
    public SermonListAdapter(Context context) {
        super(context, 0);
        this.mContext = context;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        MediaMetadata mediaMetadata = getItem(position).getMetadata();

        if (null == convertView) {
            convertView = inflater.inflate(R.layout.browse_row, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView1);
            holder.titleView = (TextView) convertView.findViewById(R.id.textView1);
            holder.descriptionView = (TextView) convertView.findViewById(R.id.textView2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (!mediaMetadata.getImages().isEmpty()) {
            Picasso.with(mContext).load(mediaMetadata.getImages().get(0).getUrl().toString())
                    .placeholder(R.drawable.placeholder)
                    .fit().into(holder.imageView);
        }
        holder.titleView.setText(mediaMetadata.getString(MediaMetadata.KEY_TITLE));
        holder.descriptionView.setText(Html.fromHtml(mediaMetadata.getString(MediaMetadata.KEY_SUBTITLE)));

        return convertView;
    }

    private class ViewHolder {
        TextView titleView;
        TextView descriptionView;
        ImageView imageView;
    }

    public void setData(List<MediaInfo> data) {
        clear();
        if (null != data) {
            for (MediaInfo item : data) {
                add(item);
            }
        }
    }
}
