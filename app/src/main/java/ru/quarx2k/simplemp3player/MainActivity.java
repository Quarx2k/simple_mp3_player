package ru.quarx2k.simplemp3player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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

public class MainActivity extends Activity {
    private static final String TAG = "SimpleMp3Player";

    private ArrayList<MusicData> mMusicData = new ArrayList<MusicData>();
    private MediaMetadataRetriever metaRetriver;
    private int current_song = -1;
    private View row;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private static ListView musicList = null;
    private static CustomAdapter adapter;
    private static String destDir;
    private static String url_playlist = "http://www.quarx2k.ru/.mp3/links.txt";
    private static String playlist_name = "links.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicList = (ListView) this.findViewById(R.id.MusicList);
        adapter = new CustomAdapter(this, mMusicData, R.layout.music_data_list);
        ArrayList<String> files = new ArrayList<String>();
        destDir = Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + getPackageName() + "/files/";
        final File playlist = new File(destDir + playlist_name);

        // Create dir in Android/data/
        File myFilesDir = new File(destDir);
        myFilesDir.mkdirs();

        // if Network connection not available and if playlist and music already downloaded.
        if (!isNetworkAvailable()) {
            if (playlist.exists()) {
                try {
                    files = readPlaylistfromSdcard(playlist_name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < files.size(); i++) {
                    final String fname = destDir + new File(files.get(i)).getName();
                    mMusicData.add(new MusicData(false, null, null, null, null, files.get(i)));
                    File file = new File(fname);
                    if (file.exists()) {
                        updateMediaMetadata(fname, i);
                    } else {
                        mMusicData.set(i, new MusicData(false, null, fname, null, getString(R.string.file_not_exist), files.get(i)));
                    }
                }
            } else {
                mMusicData.add(new MusicData(false, getString(R.string.network_not_available), null, null, getString(R.string.files_not_exist), null));
            }
        } else {
           downloadFile((url_playlist), -1, true);
        }

        musicList.setAdapter(adapter);

        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i >= mMusicData.size() )
                    return;

                File file = new File(destDir + mMusicData.get(i).getFilename());

                if (!file.exists()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.file_not_exist), Toast.LENGTH_SHORT).show();
                    return;
                }
                startPlaying(file.getPath(), i, view);
            }
        });
    }

    public void fistStartInit() {
        ArrayList<String> files = new ArrayList<String>();
        try {
            files = readPlaylistfromSdcard(playlist_name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < files.size(); i++) {
            final String fname = destDir + new File(files.get(i)).getName();
            File file = new File(fname);
            mMusicData.add(new MusicData(false, null, null, null, null, files.get(i)));
            if (file.exists()) {
                updateMediaMetadata(fname, i);
            } else {
                downloadFile(mMusicData.get(i).getUrl(), i, false);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void startPlaying(String path, int song, View view) {

       if (row != null) {
           row.setBackgroundResource(android.R.color.white);
       }

        row = view;
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            view.setBackgroundResource(android.R.color.white);
            if (current_song != song) {
                view.setBackgroundResource(android.R.color.holo_green_light);
                try {
                    mediaPlayer.setDataSource(path);
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
            view.setBackgroundResource(android.R.color.holo_green_light);
            current_song = song;
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(path);
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
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public ArrayList<String> readPlaylistfromSdcard(String fname) throws IOException {

        String strLine;
        ArrayList<String> files = new ArrayList<String>();
        FileInputStream fstream = new FileInputStream(destDir + fname);;
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        while ((strLine = br.readLine()) != null) {
            files.add(strLine);
        }

        br.close();
        return files;
    }

    private void downloadFile(final String url, final int num, final boolean first_start) {
        final String fname = new File(url.toString()).getName();
        final File downloadDir = new File(destDir + fname);

        new AsyncTask<String, Integer, File>() {
            private Exception trace_error = null;

            @Override
            protected void onPreExecute() {
                if (num >= 0 && adapter != null) {
                    mMusicData.set(num, new MusicData(true, null, null, null, null, url));
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            protected File doInBackground(String... params) {
                final URL url;
                HttpURLConnection urlConnection;
                InputStream inputStream;
                int totalSize;
                int downloadedSize;
                byte[] buffer;
                int bufferLength;

                File file = null;
                FileOutputStream fos = null;
                try {
                    url = new URL(params[0]);
                    urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();
                    fos = new FileOutputStream(downloadDir.toString());

                    inputStream = urlConnection.getInputStream();

                    totalSize = urlConnection.getContentLength();
                    downloadedSize = 0;

                    buffer = new byte[1024];
                    bufferLength = 0;

                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, bufferLength);
                        downloadedSize += bufferLength;
                        publishProgress(downloadedSize, totalSize);
                    }

                    fos.close();
                    inputStream.close();

                    return file;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    trace_error = e;
                } catch (IOException e) {
                    e.printStackTrace();
                    trace_error = e;
                }

                return null;
            }

            @Override
            protected void onPostExecute(File file) {
                if (trace_error != null) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setTitle(getString(R.string.download_err));
                    alert.setMessage(getString(R.string.check_url) + "\n" + url.toString());
                    alert.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                     AlertDialog alert1 = alert.create();
                    alert1.show();
                    return;
                }

                if (first_start){
                    fistStartInit();
                    return;
                }

                if (num >= 0)
                    updateMediaMetadata(downloadDir.toString(), num);
            }
        }.execute(url);
    }

    public void updateMediaMetadata(String mediaFile, int num) {
        metaRetriver = new MediaMetadataRetriever();
        metaRetriver.setDataSource(mediaFile);
        File file = new File(mediaFile);
        String duration = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
        String song = metaRetriver .extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

        long dur = Long.parseLong(duration);
        String sec = String.valueOf((dur % 60000) / 1000);
        String min = String.valueOf(dur / 60000);

        if (sec.length() == 1) {
            duration = "0" + min + ":0" + sec;
        }else {
            duration = "0" + min + ":" + sec;
        }
        String fileName = file.getName();
        String url = mMusicData.get(num).getUrl();

        mMusicData.set(num ,new MusicData(false, artist, song, duration , fileName, url));

        adapter.notifyDataSetChanged();
    }
}