package com.theplatform.dfh.cp.api.output;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class OutputStreams
{
    private List<OutputStream> video;
    private List<OutputStream> audio;

    @JsonProperty
    public List<OutputStream> getVideo()
    {
        return video;
    }

    @JsonProperty
    public void setVideo(List<OutputStream> video)
    {
        this.video = video;
    }

    @JsonProperty
    public List<OutputStream> getAudio()
    {
        return audio;
    }

    @JsonProperty
    public void setAudio(List<OutputStream> audio)
    {
        this.audio = audio;
    }
}
