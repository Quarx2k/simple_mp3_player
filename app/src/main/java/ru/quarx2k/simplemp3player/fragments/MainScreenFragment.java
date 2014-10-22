package ru.quarx2k.simplemp3player.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;


import ru.quarx2k.simplemp3player.R;


public class MainScreenFragment extends Fragment  {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout rLayout = (RelativeLayout) inflater.inflate(R.layout.main_screen, container, false);

        return rLayout;
    }

}