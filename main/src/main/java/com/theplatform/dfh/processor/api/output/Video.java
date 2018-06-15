package com.theplatform.dfh.processor.api.output;

public class Video
{
    private String sourceStreamReference;

    private Integer width;
    private Integer height;
    private Long bitrate;
    private String codec;
    private String profile;
    private String level;

    public String getSourceStreamReference()
    {
        return sourceStreamReference;
    }

    public void setSourceStreamReference(String sourceStreamReference)
    {
        this.sourceStreamReference = sourceStreamReference;
    }

    public Integer getWidth()
    {
        return width;
    }

    public void setWidth(Integer width)
    {
        this.width = width;
    }

    public Integer getHeight()
    {
        return height;
    }

    public void setHeight(Integer height)
    {
        this.height = height;
    }

    public Long getBitrate()
    {
        return bitrate;
    }

    public void setBitrate(Long bitrate)
    {
        this.bitrate = bitrate;
    }

    public String getCodec()
    {
        return codec;
    }

    public void setCodec(String codec)
    {
        this.codec = codec;
    }

    public String getProfile()
    {
        return profile;
    }

    public void setProfile(String profile)
    {
        this.profile = profile;
    }

    public String getLevel()
    {
        return level;
    }

    public void setLevel(String level)
    {
        this.level = level;
    }
}
