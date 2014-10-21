package ru.quarx2k.simplemp3player.helpers;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import ru.quarx2k.simplemp3player.MainActivity;
import ru.quarx2k.simplemp3player.MusicData;
import ru.quarx2k.simplemp3player.interfaces.DownloadInterface;
import ru.quarx2k.simplemp3player.interfaces.UpdateMetaDataInterface;

/**
 * Created by Quarx2k on 21.10.2014.
 */
public class Tools  {
    private MediaMetadataRetriever metaRetriver;
    public UpdateMetaDataInterface delegate = null;

    public boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public ArrayList<String> readTxtPlaylistfromSdcard(String filePath) throws IOException {

        String strLine;
        ArrayList<String> files = new ArrayList<String>();
        FileInputStream fstream = new FileInputStream(filePath);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        while ((strLine = br.readLine()) != null) {
            files.add(strLine);
        }

        br.close();
        return files;
    }

    private String getDuriaton(long dur) {
        String sec = String.valueOf((dur % 60000) / 1000);
        String min = String.valueOf(dur / 60000);
        String duration;

        if (sec.length() == 1) {
            duration = "0" + min + ":0" + sec;
        }else {
            duration = "0" + min + ":" + sec;
        }
       return duration;
    }

    public void updateArrayMediaMetadata(ArrayList<MusicData> musicData) {
        metaRetriver = new MediaMetadataRetriever();
        File file;
        String mediaFile;
        String artist;
        String duration;
        String dur;
        String song;
        int i;
        if (musicData != null) {  //Update whole array of files
            for (i = 0; i < musicData.size(); i++) {
                mediaFile = musicData.get(i).getUrl();
                file = new File(mediaFile);
                metaRetriver.setDataSource(mediaFile);
                dur = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
                song = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                duration = getDuriaton(Long.parseLong(dur));
                musicData.set(i, new MusicData(false, song, artist, duration, file.getName(), null));
            }
            delegate.readArrayMetadataFinished(musicData);
        }
    }


    public void updateMediaMetadata(String mediaFile, final String url, final int num) {
        ArrayList<String> musicData = null;
        metaRetriver = new MediaMetadataRetriever();
        final String artist;
        String dur;
        final String duration;
        final File file;
        final String song;

        metaRetriver.setDataSource(mediaFile);
        file = new File(mediaFile);
        dur = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
        song = metaRetriver .extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        duration = getDuriaton(Long.parseLong(dur));

        musicData = new ArrayList<String>() {
            {
                add(song);
                add(artist);
                add(duration);
                add(file.getName());
                add(url);
            }
        };
        delegate.readMetadataFinished(num, musicData);
    }
}
