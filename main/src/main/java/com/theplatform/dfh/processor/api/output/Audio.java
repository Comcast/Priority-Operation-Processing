package com.theplatform.dfh.processor.api.output;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Audio
{
    private String sourceStreamReference;
    private Long bitrate;
    private String language;

    @JsonProperty
    public String getSourceStream()
    {
        return sourceStreamReference;
    }

    @JsonProperty
    public void setSourceStream(String sourceStreamReference)
    {
        this.sourceStreamReference = sourceStreamReference;
    }

    @JsonProperty
    public Long getBitrate()
    {
        return bitrate;
    }

    @JsonProperty
    public void setBitrate(Long bitrate)
    {
        this.bitrate = bitrate;
    }

    @JsonProperty
    public String getLanguage()
    {
        return language;
    }

    @JsonProperty
    public void setLanguage(String language)
    {
        this.language = language;
    }
}
