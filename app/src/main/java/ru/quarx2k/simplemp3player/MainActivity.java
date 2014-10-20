package ru.quarx2k.simplemp3player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;

/**
 * Created by Quarx2k on 21.10.2014.
 */
public class MainActivity extends Activity {

    public static String destDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        destDir = Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + getPackageName() + "/files/";

        // Create dir in Android/data/
        File myFilesDir = new File(destDir);

        myFilesDir.mkdirs();

        Intent intent = new Intent(MainActivity.this,  OnlinePlayer.class);
        startActivity(intent);
    }
}
