package ru.quarx2k.simplemp3player;

import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;

import ru.quarx2k.simplemp3player.fragments.LocalPlayerFragment;
import ru.quarx2k.simplemp3player.fragments.MainScreenFragment;
import ru.quarx2k.simplemp3player.fragments.OnlinePlayerFragment;

/**
 * Created by Quarx2k on 21.10.2014.
 */
public class MainActivity extends FragmentActivity {

    public static String destDir;

    private LocalPlayerFragment mLocalPlayerFragment;
    private OnlinePlayerFragment mOnlinePlayerFragment;
    private MainScreenFragment mMainScreenFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Fragment fragment = null;

        destDir = Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + getPackageName() + "/files/";

        mLocalPlayerFragment = new LocalPlayerFragment();
        mOnlinePlayerFragment = new OnlinePlayerFragment();
        mMainScreenFragment = new MainScreenFragment();

        // Create dir in Android/data/
        File myFilesDir = new File(destDir);
        myFilesDir.mkdirs();

        fragment = mLocalPlayerFragment; //mMainScreenFragment; //TODO Make dynamic
        Bundle args = new Bundle();
        args.putString("playDir", destDir);
        fragment.setArguments(args);
        switchContent(fragment);
    }

    public final void switchContent(final Fragment fragment) {
        getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
    }
}
