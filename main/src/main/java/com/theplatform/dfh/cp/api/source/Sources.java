package com.theplatform.dfh.cp.api.source;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Sources: This includes URLs, creds,
 * and key metadata for the media files that DFH will process (video, audio, text tracks, etc.)
 * and metadata that serves as an input to DFH (e.g. chapter info for Ad Conditioning)
 */
public class Sources
{
    private List<Video> video;
    private List<Text> text;

    @JsonProperty
    public List<Video> getVideo()
    {
        return video;
    }

    @JsonProperty
    public void setVideo(List<Video> video)
    {
        this.video = video;
    }

    @JsonProperty
    public List<Text> getText()
    {
        return text;
    }

    @JsonProperty
    public void setText(List<Text> text)
    {
        this.text = text;
    }
}
