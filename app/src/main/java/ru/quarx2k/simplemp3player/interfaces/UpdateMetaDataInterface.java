package ru.quarx2k.simplemp3player.interfaces;

import java.util.ArrayList;

import ru.quarx2k.simplemp3player.MusicData;

/**
 * Created by Quarx2k on 21.10.2014.
 */
public interface UpdateMetaDataInterface
{
    void readMetadataFinished(int num, ArrayList<String> data);
    void readArrayMetadataFinished(ArrayList<MusicData> mdata);
}