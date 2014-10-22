package ru.quarx2k.simplemp3player.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.support.v4.app.FragmentActivity;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ru.quarx2k.simplemp3player.CustomAdapter;
import ru.quarx2k.simplemp3player.MusicData;
import ru.quarx2k.simplemp3player.R;
import ru.quarx2k.simplemp3player.helpers.Tools;
import ru.quarx2k.simplemp3player.interfaces.UpdateMetaDataInterface;

public class LocalPlayerFragment extends Fragment implements UpdateMetaDataInterface {
    private static final String TAG = "SimpleMp3Player";

    private ArrayList<MusicData> mMusicData = new ArrayList<MusicData>();
    private int current_song = -1;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private static ListView musicList = null;
    private static CustomAdapter adapter;
    private static String destDir;  //TODO Make dynamic
    private static Context ctx;

    Tools tool = new Tools();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout rLayout = (RelativeLayout) inflater.inflate(R.layout.player_acitvity, container, false);
        ctx = getActivity();
        tool.delegate = this;

        destDir = getArguments().getString("playDir");
        Log.e(TAG, "DIR: "+ destDir);

        musicList = (ListView) rLayout.findViewById(R.id.MusicList);
        adapter = new CustomAdapter(ctx, mMusicData, R.layout.music_data_list);

        initializingPlaylist(destDir);

        musicList.setAdapter(adapter);

        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i >= mMusicData.size() )
                    return;
                startPlaying(i);
            }
        });
        return  rLayout;
    }

    public void updateUi()
    {
        adapter.notifyDataSetChanged();
    }

    public void initializingPlaylist(String filePath) {
        File dir = new File(filePath);
        getSongsFromDirectory(dir);
        updateUi();
    }

    void getSongsFromDirectory(File f) {
        File[] files;
        int i = 0;
        if (f.isDirectory() && (files = f.listFiles()) != null) {
            for (File file : files) {
                String path = file.getPath();
                if (path.substring(path.length()-4, path.length()).equals(".mp3")) { // Allow only mp3.
                    mMusicData.add(new MusicData(false, null, null, null, null, file.getPath()));
                    i++;
                }
            }
            if (mMusicData != null) {
                tool.updateArrayMediaMetadata(mMusicData);
            }
        }
    }

    public void startPlaying(int song) {
        String song_path = destDir + mMusicData.get(song).getFilename();
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            if (current_song != song) {
                try {
                    mediaPlayer.setDataSource(song_path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
                current_song = song;
            }
        } else {
            current_song = song;
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(song_path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.e(TAG, "song end");
            }
        });
    }

    @Override
    public void readMetadataFinished(int num, ArrayList<String> data) {
    }

    @Override
    public void readArrayMetadataFinished(ArrayList<MusicData> mdata) {
        mMusicData = mdata;
        updateUi();
    }
}