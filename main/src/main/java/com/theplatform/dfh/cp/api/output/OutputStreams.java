package com.theplatform.dfh.cp.api.output;

import java.util.List;

public class OutputStreams
{
    private List<OutputStream> video;
    private List<OutputStream> audio;

    public List<OutputStream> getVideo()
    {
        return video;
    }

    public void setVideo(List<OutputStream> video)
    {
        this.video = video;
    }

    public List<OutputStream> getAudio()
    {
        return audio;
    }

    public void setAudio(List<OutputStream> audio)
    {
        this.audio = audio;
    }
}
