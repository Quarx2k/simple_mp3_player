package ru.quarx2k.simplemp3player;

import android.content.Context;
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
        TextView header;
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

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResourceId, null);

            holder = new Holder();
            holder.header = (TextView) row.findViewById(R.id.textview);


            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        MusicData dataMusic = mData.get(position);

        if (dataMusic != null)
            holder.header.setText(dataMusic.name);

        return row;
    }
}
