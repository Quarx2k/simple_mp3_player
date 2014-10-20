package ru.quarx2k.simplemp3player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Quarx2k on 21.10.2014.
 */
public class DownloadAsync extends AsyncTask {
    public DownloadInterface delegate = null;

    public void downloadFile(final String url, final int num, final boolean first_start) {
        final String fname = new File(url.toString()).getName();

        final File downloadPath = new File(MainActivity.destDir + fname);
        new AsyncTask<String, Integer, File>() {
            private Exception trace_error = null;

            @Override
            protected void onPreExecute() {
                delegate.processDownloadStarted(url, num);
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
                    fos = new FileOutputStream(downloadPath.toString());

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
                delegate.processDownloadFinish(first_start, downloadPath.toString(), num);
                if (trace_error != null) {
                    //TODO: Implement error message.
                }
            }
        }.execute(url);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        return null;
    }
}
