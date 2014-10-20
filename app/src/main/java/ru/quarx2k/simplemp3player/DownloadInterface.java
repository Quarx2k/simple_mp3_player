package ru.quarx2k.simplemp3player;

/**
 * Created by Quarx2k on 21.10.2014.
 */
public interface DownloadInterface
{
    void processDownloadFinish(Boolean first_start, String filname, int num);
    void processDownloadStarted(String filname, int num);

}