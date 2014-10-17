package ru.quarx2k.simplemp3player;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
    ArrayList<MusicData> mMusicData = new ArrayList<MusicData>();
    MediaMetadataRetriever metaRetriver;
    CustomAdapter adapter;
    public View row;
    private MediaPlayer mediaPlayer;
    private String destDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ListView musicList = (ListView) this.findViewById(R.id.MusicList);
        adapter = new CustomAdapter(this, mMusicData, R.layout.activity_main);
        ArrayList<String> files = new ArrayList<String>();
        destDir = Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + getPackageName() + "/files/";
        File playlist = new File(destDir + "/links.txt");

        // Create dir in Android/data/
        File myFilesDir = new File(destDir);
        myFilesDir.mkdirs();

        // if Network connection not available and if playlist and music already downloaded.
        if (!isNetworkAvailable()) {
            if (playlist.exists()) {
                try {
                    files = readPlaylisfromSdcard(playlist.getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < files.size(); i++) {
                    mMusicData.add(new MusicData(files.get(i)));
                    final String item = mMusicData.get(i).name;
                    final String fname = new File(item.toString()).getName();
                    final String fullPath = destDir + fname;
                    File file = new File(fullPath);
                    Toast.makeText(getApplicationContext(), fullPath, Toast.LENGTH_SHORT).show();
                    if (file.exists()) {
                        updateMediaMetadata(fullPath, i);
                    } else {
                        mMusicData.set(i, new MusicData(files.get(i).toString() + "\n" + getString(R.string.file_not_exist)));// + "\n" + "Touch to retry"));
                    }
                }
                mMusicData.add(new MusicData(getString(R.string.network_not_available) + "\n" + "But something already downloaded!"));
            } else {
                mMusicData.add(new MusicData(getString(R.string.network_not_available) + "\n" + getString(R.string.files_not_exist)));
            }
        } else {
            try {
                files = readPlaylisfromUrl("http://www.quarx2k.ru/.mp3/links.txt");
                for (int i = 0; i < files.size(); i++) {

                    mMusicData.add(new MusicData(files.get(i)));
                    final String item = mMusicData.get(i).name;
                    final String fname = new File(item.toString()).getName();
                    final String fullPath = destDir + fname;
                    File file = new File(fullPath);
                    if (file.exists()) {
                            updateMediaMetadata(fullPath, i);
                    } else {
                        if (!isNetworkAvailable()) {
                            Toast.makeText(getApplicationContext(), getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
                        } else {
                            downloadFile(item.toString(), i);
                        }
                    }
                }
            } catch (IOException e) {
            }
        }

        musicList.setAdapter(adapter);

        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String pName = getPackageName();
                final String item = mMusicData.get(i).name;
                final String fname = new File(item.toString()).getName();
                final String fullPath = destDir + fname;
                File file = new File(fullPath);
                if (file.exists()) {
                    updateMediaMetadata(fullPath, i);
                    //  } else {
                    //      downloadFile(item.toString(), i);
                    //  }
                }

                if (row != null) {
                    row.setBackgroundResource(android.R.color.holo_orange_light);
                }
                row = view;
                view.setBackgroundResource(android.R.color.holo_orange_light);
            }
        });
    }
/*
        */

        /*
        // Read list of files  TODO for sdcard
        try {
            files = readTextfile("links.txt");
        } catch (IOException e) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("Error while downloading lis of files");
            alert.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert1 = alert.create();
            alert1.show();
            e.printStackTrace();
        }
        */
        /* //Download on click
        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String pName =  getPackageName();
                final String item = mMusicData.get(i).name;
                final String fname = new File(item.toString()).getName();
                final String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + pName + "/files/" + fname;
                File file = new File(fullPath);
                Toast.makeText(getApplicationContext(), fullPath, Toast.LENGTH_SHORT).show();
                if(file.exists()) {
                    Toast.makeText(getApplicationContext(), "This file already downloaded", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    downloadFile(item.toString(), i);
                }
            }
        }); */

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public ArrayList<String> readPlaylisfromSdcard(String fname) throws IOException {

        String strLine;
        ArrayList<String> files = new ArrayList<String>();
        FileInputStream fstream;
        fstream = new FileInputStream(fname);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        while ((strLine = br.readLine()) != null) {
            files.add(strLine);
        }

        br.close();
        return files;

    }

    public ArrayList<String> readPlaylisfromUrl(String url) throws IOException {

        String strLine;
        ArrayList<String> files = new ArrayList<String>();
        FileInputStream fstream;
        downloadFile(url, -2);
        fstream = new FileInputStream(Environment.getExternalStorageDirectory().getPath() + "/" + "links.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        while ((strLine = br.readLine()) != null) {
            files.add(strLine);
        }

        br.close();
        return files;

    }

    private void downloadFile(final String url, final int num) {
        final String pname =  this.getPackageName();
        final String fname = new File(url.toString()).getName();
        final File downloadDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + pname + "/files/" + fname);

        new AsyncTask<String, Integer, File>() {
            private Exception trace_error = null;

            @Override
            protected void onPreExecute() {
                if (num >= 0 )
                    mMusicData.set(num,new MusicData(getString(R.string.downloading) + "\n" + mMusicData.get(num).name));
                if (adapter != null)
                    adapter.notifyDataSetChanged();
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
              //  Toast.makeText(getApplicationContext(), "Download " + fname +" Finished", Toast.LENGTH_SHORT).show();
                if (num >= 0)
                    updateMediaMetadata(downloadDir.toString(), num);
            }
        }.execute(url);
    }

    public void updateMediaMetadata(String mediaFile, int num) {
        metaRetriver = new MediaMetadataRetriever();
        metaRetriver.setDataSource(mediaFile);
        long durationMsec = Long.parseLong(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        long duration = durationMsec / 1000;
        long h = duration / 3600;
        long m = (duration - h * 3600) / 60;
        long s = duration - (h * 3600 + m * 60);

        mMusicData.set(num,new MusicData(getString(R.string.artist) + metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) + "\n" +
                getString(R.string.song) + metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) + "\n" +
                getString(R.string.duration) + m + ":" + s));

        adapter.notifyDataSetChanged();
    }
}