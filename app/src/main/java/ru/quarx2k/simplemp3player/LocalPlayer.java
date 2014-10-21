package ru.quarx2k.simplemp3player;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ru.quarx2k.simplemp3player.helpers.DownloadAsync;
import ru.quarx2k.simplemp3player.helpers.Tools;
import ru.quarx2k.simplemp3player.interfaces.DownloadInterface;
import ru.quarx2k.simplemp3player.interfaces.UpdateMetaDataInterface;

public class LocalPlayer extends Activity implements UpdateMetaDataInterface {
    private static final String TAG = "SimpleMp3Player";

    private ArrayList<MusicData> mMusicData = new ArrayList<MusicData>();
    private int current_song = -1;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private static ListView musicList = null;
    private static CustomAdapter adapter;
    private static String destDir = MainActivity.destDir;  //TODO Make dynamic
    private static Context ctx;

    Tools tool = new Tools();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_player_acitvity);
        ctx = getApplicationContext();

        tool.delegate = this;

        musicList = (ListView) this.findViewById(R.id.MusicList);
        adapter = new CustomAdapter(this, mMusicData, R.layout.music_data_list);

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