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

    public void updateMediaMetadata(String mediaFile, final String url, final int num) {
        ArrayList<String> musicData = null;
        metaRetriver = new MediaMetadataRetriever();
        metaRetriver.setDataSource(mediaFile);
        final File file = new File(mediaFile);
        String duration = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        final String artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
        final String song = metaRetriver .extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        long dur = Long.parseLong(duration);
        String sec = String.valueOf((dur % 60000) / 1000);
        String min = String.valueOf(dur / 60000);

        if (sec.length() == 1) {
            duration = "0" + min + ":0" + sec;
        }else {
            duration = "0" + min + ":" + sec;
        }
        final String fduration = duration;
        musicData = new ArrayList<String>() {
            {
                add(song);
                add(artist);
                add(fduration);
                add(file.getName());
                add(url);
            }
        };

        delegate.readMetadataFinished(num, musicData);

    }
}
