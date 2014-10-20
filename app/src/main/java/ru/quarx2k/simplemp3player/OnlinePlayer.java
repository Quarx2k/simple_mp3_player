package ru.quarx2k.simplemp3player;

import android.app.Activity;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ru.quarx2k.simplemp3player.helpers.DownloadAsync;
import ru.quarx2k.simplemp3player.helpers.Tools;
import ru.quarx2k.simplemp3player.interfaces.DownloadInterface;
import ru.quarx2k.simplemp3player.interfaces.UpdateMetaDataInterface;

public class OnlinePlayer extends Activity implements DownloadInterface, UpdateMetaDataInterface {
    private static final String TAG = "SimpleMp3Player";

    private ArrayList<MusicData> mMusicData = new ArrayList<MusicData>();
    private int current_song = -1;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private static ListView musicList = null;
    private static CustomAdapter adapter;
    private static String destDir = MainActivity.destDir;
    private static String url_playlist = "http://www.quarx2k.ru/.mp3/links.txt";  //TODO Make dynamic
    private static Context ctx;
    DownloadAsync downloadTask = new DownloadAsync();
    Tools tool = new Tools();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_player_acitvity);
        ctx = getApplicationContext();

        downloadTask.delegate = this;
        tool.delegate = this;

        musicList = (ListView) this.findViewById(R.id.MusicList);
        adapter = new CustomAdapter(this, mMusicData, R.layout.music_data_list);

        initializingPlaylist(true);

        musicList.setAdapter(adapter);

        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i >= mMusicData.size() )
                    return;

                File file = new File(destDir + mMusicData.get(i).getFilename());

                if (!file.exists()) {
                    Toast.makeText(ctx, getString(R.string.file_not_exist), Toast.LENGTH_SHORT).show();
                    return;
                }
                startPlaying(i);
            }
        });
    }

    public void updateUi()
    {
        adapter.notifyDataSetChanged();
    }

    public void initializingPlaylist(Boolean first_start) {
        ArrayList<String> files = null;
        final File playlist = new File(destDir + new File(url_playlist).getName());

        if (playlist.exists()) {
            try {
                files = tool.readTxtPlaylistfromSdcard(playlist.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (first_start && tool.isNetworkAvailable(ctx)) {  //Download or update playlist at first start.
            downloadTask.downloadFile((url_playlist), -1, true);
            return;
        } else if (!tool.isNetworkAvailable(ctx) && files == null) { //Notify if playlist not exist and network not available.
            mMusicData.add(new MusicData(false, getString(R.string.network_not_available), null, null, getString(R.string.files_not_exist), null));
        } else { //Get info from playlist.
            for (int i = 0; i < files.size(); i++) {
                final String fname = destDir + new File(files.get(i)).getName();
                File file = new File(fname);
                mMusicData.add(new MusicData(false, null, null, null, null, files.get(i)));
                if (file.exists()) {
                    tool.updateMediaMetadata(fname, files.get(i), i);
                } else {
                    if (!tool.isNetworkAvailable(ctx)) {
                        mMusicData.set(i, new MusicData(false, null, file.getName(), null, getString(R.string.file_not_exist), files.get(i)));
                    } else {
                        downloadTask.downloadFile(mMusicData.get(i).getUrl(), i, false);
                    }
                }
            }
        }
        updateUi();
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
    public void processDownloadFinish(Boolean first_start, String fname, String url, int num) {
        if (first_start) {
            initializingPlaylist(false);
        } else {
        tool.updateMediaMetadata(fname, url, num);
      }
    }

    @Override
    public void processDownloadStarted(String url, int num) {
        if (num >= 0) {
            mMusicData.set(num, new MusicData(true, null, null, null, null, url));
            updateUi();
        }
    }

    @Override
    public void readMetadataFinished(int num, ArrayList<String> data) {
            mMusicData.set(num ,new MusicData(false, data.get(0), data.get(1), data.get(2), data.get(3), data.get(4)));
            updateUi();
    }
}