package ru.quarx2k.simplemp3player;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by quarx2k on 10/16/14.
 */
public class CustomAdapter extends ArrayAdapter<MusicData> {

    private Context context;
    private int layoutResourceId;
    private List<MusicData> mData;

    public CustomAdapter(Context context, ArrayList<MusicData> musicData, int layoutResourceId) {
        super(context, layoutResourceId, musicData);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.mData = musicData;
    }

    public static class Holder {
        TextView status;
        TextView artist;
        TextView name;
        TextView duration;
        TextView filename;
        TextView url;
    }


    @Override
    public void clear() {
        super.clear();
        this.mData.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Holder holder;
        Context ctx = getContext();
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResourceId, null);

            holder = new Holder();
            holder.status = (TextView) row.findViewById(R.id.status);
            holder.artist = (TextView) row.findViewById(R.id.artist);
            holder.name = (TextView) row.findViewById(R.id.name);
            holder.duration = (TextView) row.findViewById(R.id.duration);
            holder.filename = (TextView) row.findViewById(R.id.filename);
            holder.url = (TextView) row.findViewById(R.id.url);

            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        MusicData dataMusic = mData.get(position);

        if (dataMusic != null) {
            if (holder.status != null && dataMusic.status == true) {
                holder.status.setText(ctx.getString(R.string.downloading));
            } else {
                holder.status.setVisibility(View.GONE);
            }
            if (holder.artist != null && dataMusic.artist != null) {
                holder.artist.setVisibility(View.VISIBLE);
                holder.artist.setText(ctx.getString(R.string.artist) + dataMusic.artist);
            } else {
                holder.artist.setVisibility(View.GONE);
            }
            if (holder.name != null && dataMusic.name != null ) {
                holder.name.setVisibility(View.VISIBLE);
                holder.name.setText(ctx.getString(R.string.song) + dataMusic.name);
            } else {
                holder.name.setVisibility(View.GONE);
            }
            if (holder.duration != null && dataMusic.duration != null ) {
                holder.duration.setVisibility(View.VISIBLE);
                holder.duration.setText(ctx.getString(R.string.duration) + dataMusic.duration);
            } else {
                holder.duration.setVisibility(View.GONE);
            }
            if (holder.filename != null && dataMusic.filename != null ) {
                holder.filename.setVisibility(View.VISIBLE);
                holder.filename.setText(ctx.getString(R.string.file_name) + dataMusic.filename);
            } else {
                holder.filename.setVisibility(View.GONE);
            }
            if (holder.url != null && dataMusic.url != null ) {
                holder.url.setVisibility(View.VISIBLE);
                holder.url.setText(ctx.getString(R.string.url) + dataMusic.url);
            } else {
                holder.url.setVisibility(View.GONE);
            }
        }

        return row;
    }
   }
