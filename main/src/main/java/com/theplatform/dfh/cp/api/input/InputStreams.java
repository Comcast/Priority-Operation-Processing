package com.theplatform.dfh.cp.api.input;

import java.util.List;

public class InputStreams
{
    private List<InputStream> video;
    private List<InputStream> audio;
    private List<InputStream> text;
    private List<InputStream> image;

    public List<InputStream> getVideo()
    {
        return video;
    }

    public void setVideo(List<InputStream> video)
    {
        this.video = video;
    }

    public List<InputStream> getAudio()
    {
        return audio;
    }

    public void setAudio(List<InputStream> audio)
    {
        this.audio = audio;
    }

    public List<InputStream> getText()
    {
        return text;
    }

    public void setText(List<InputStream> text)
    {
        this.text = text;
    }

    public List<InputStream> getImage()
    {
        return image;
    }

    public void setImage(List<InputStream> image)
    {
        this.image = image;
    }
}
