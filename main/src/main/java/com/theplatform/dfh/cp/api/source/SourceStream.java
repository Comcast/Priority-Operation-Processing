package com.theplatform.dfh.cp.api.source;


import com.fasterxml.jackson.annotation.JsonProperty;

public class SourceStream
{
    /**
     * Source [TranscodingAPI]: Array Reference or Label Reference to the source (file) to use
     */
    private String sourceReference;
    /**
     * TrackId [TranscodingAPI]: Path to the desired track (based on MediaInfo results). E.g. “Audio[2]”
     */
    private Integer trackId;


    @JsonProperty
    public String getSourceRef()
    {
        return sourceReference;
    }

    @JsonProperty
    public void setSourceRef(String sourceReference)
    {
        this.sourceReference = sourceReference;
    }


    @JsonProperty
    public Integer getTrackId()
    {
        return trackId;
    }

    @JsonProperty
    public void setTrackId(Integer trackId)
    {
        this.trackId = trackId;
    }
}
