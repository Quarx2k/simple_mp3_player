package ru.quarx2k.simplemp3player;

import java.util.ArrayList;

/**
 * Created by quarx2k on 10/16/14.
 */
public class MusicData {
    public boolean status;
    public String name;
    public String artist;
    public String duration;
    public String filename;
    public String url;

    public MusicData(Boolean status, String artist, String name, String duration, String filename, String url) {
        this.status = status;
        this.name = name;
        this.artist = artist;
        this.duration = duration;
        this.filename = filename;
        this.url = url;
    }

    public Boolean getStatus() {
        return status;
    }

    public Boolean setStatus(Boolean status) {
        this.status = status;
        return status;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.name = duration;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String utl) {
        this.url = utl;
    }
}
