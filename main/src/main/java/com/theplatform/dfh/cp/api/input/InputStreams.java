package com.theplatform.dfh.cp.api.input;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class InputStreams
{
    private List<InputStream> video;
    private List<InputStream> audio;
    private List<InputStream> text;
    private List<InputStream> image;

    @JsonProperty
    public List<InputStream> getVideo()
    {
        return video;
    }

    @JsonProperty
    public void setVideo(List<InputStream> video)
    {
        this.video = video;
    }

    @JsonProperty
    public List<InputStream> getAudio()
    {
        return audio;
    }

    @JsonProperty
    public void setAudio(List<InputStream> audio)
    {
        this.audio = audio;
    }

    @JsonProperty
    public List<InputStream> getText()
    {
        return text;
    }

    @JsonProperty
    public void setText(List<InputStream> text)
    {
        this.text = text;
    }

    @JsonProperty
    public List<InputStream> getImage()
    {
        return image;
    }

    @JsonProperty
    public void setImage(List<InputStream> image)
    {
        this.image = image;
    }
}
