package com.theplatform.dfh.cp.api.output;

import java.util.List;

public class OutputStreams
{
    private List<OutputStream> video;
    private List<OutputStream> audio;
    private List<OutputStream> text;

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

    public List<OutputStream> getText()
    {
        return text;
    }

    public void setText(List<OutputStream> text)
    {
        this.text = text;
    }
}
