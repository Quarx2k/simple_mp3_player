package ru.quarx2k.simplemp3player;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
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
    MediaMetadataRetriever metaRetriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArrayList<String> files = new ArrayList<String>();

        // Create dir in Android/data/
        String pName =  this.getPackageName();
        File myFilesDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + pName + "/files");
        myFilesDir.mkdirs();
        try {
           files = readPlaylisfromUrl("http://quarx2k.ru/.mp3/links.txt");
        } catch (IOException e) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("Error while downloading list of files");
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

        // Add array of files to ListView
        final ListView fileList = (ListView) findViewById(R.id.fileList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, files);
        fileList.setAdapter(adapter);

        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                String pName =  getPackageName();
                final String item = (String) parent.getItemAtPosition(position);
                final String fname = new File(item.toString()).getName();
                final String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + pName + "/files/" + fname;
                File file = new File(fullPath);
                Toast.makeText(getApplicationContext(), fullPath, Toast.LENGTH_SHORT).show();
                if(file.exists()) {
                    Toast.makeText(getApplicationContext(), "This file already downloaded", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    downloadFile(item.toString(), view);
                }
            }
        });
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

/*  TODO for sdcard
    public ArrayList<String> readPlaylisfromSdcard(String fname) throws IOException {

        String strLine;
        ArrayList<String> files = new ArrayList<String>();
        FileInputStream fstream;
        fstream = new FileInputStream(Environment.getExternalStorageDirectory().getPath() + "/" + "fname);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        while ((strLine = br.readLine()) != null) {
            files.add(strLine);
        }

        br.close();
        return files;

    }
*/
    public ArrayList<String> readPlaylisfromUrl(String url) throws IOException {

        String strLine;
        ArrayList<String> files = new ArrayList<String>();
        FileInputStream fstream;
        downloadFile(url, null);
        fstream = new FileInputStream(Environment.getExternalStorageDirectory().getPath() + "/" + "links.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        while ((strLine = br.readLine()) != null) {
            files.add(strLine);
        }

        br.close();
        return files;

    }

    private void downloadFile(final String url, final View view) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        final String pname =  this.getPackageName();
        final String fname = new File(url.toString()).getName();
        final File downloadDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + pname + "/files/" + fname);

        new AsyncTask<String, Integer, File>() {
            private Exception trace_error = null;

            @Override
            protected void onPreExecute() {
                /*
                progressDialog.setMessage("Downloading ...");
                progressDialog.setCancelable(false);
                progressDialog.setMax(100);
                progressDialog
                        .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

                progressDialog.show();
                */
                if (view != null) {
                    ((TextView) view).setText("Downloading in progress....");
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
                    alert.setTitle("Error while downloading!");
                    alert.setMessage("Please check url of file\n" + url.toString());
                    alert.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                     AlertDialog alert1 = alert.create();
                    alert1.show();
                   // progressDialog.hide();
                    return;
                }
                Toast.makeText(getApplicationContext(), "Download " + fname +" Finished", Toast.LENGTH_SHORT).show();
                updateMediaMetadata(downloadDir.toString(),view);
                //  progressDialog.hide();
            }
        }.execute(url);
    }

    public void updateMediaMetadata(String mediaFile, final View view) {
        if (view == null) {
            return;
        }
        metaRetriver = new MediaMetadataRetriever();
        metaRetriver.setDataSource(mediaFile);
        long durationMsec = Long.parseLong(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        long duration = durationMsec / 1000;
        long h = duration / 3600;
        long m = (duration - h * 3600) / 60;
        long s = duration - (h * 3600 + m * 60);

    ((TextView) view).setText(MediaMetadataRetriever.METADATA_KEY_ARTIST + " - " +
                 metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) + "\n" +
                 "Duration: " + m + ":" + s);
    }
}
