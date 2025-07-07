package com.MagicDevelopers.buzzup.Modelos.Upload;

public class MediaModel {
    private String uri;
    private boolean isVideo;

    public MediaModel(String uri, boolean isVideo) {
        this.uri = uri;
        this.isVideo = isVideo;
    }

    public String getUri() {
        return uri;
    }

    public boolean isVideo() {
        return isVideo;
    }
}
