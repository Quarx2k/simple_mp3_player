package ru.quarx2k.simplemp3player.interfaces;

/**
 * Created by Quarx2k on 21.10.2014.
 */
public interface DownloadInterface
{
    void processDownloadFinish(Boolean first_start, String filname, String url, int num);
    void processDownloadStarted(String filname, int num);

}