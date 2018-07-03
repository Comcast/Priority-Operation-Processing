package com.theplatform.dfh.cp.api.source;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.theplatform.dfh.cp.api.AbstractStream;

public class SourceStream extends AbstractStream
{
    /**
     * TrackId [TranscodingAPI]: Path to the desired track (based on MediaInfo results). E.g. “AudioStream[2]”
     */
    private Integer trackId;

    @JsonProperty
    public String getSourceRef()
    {
        return getReference();
    }

    @JsonProperty
    public void setSourceRef(String sourceReference)
    {
        setReference(sourceReference);
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
