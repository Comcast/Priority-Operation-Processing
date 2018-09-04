package com.theplatform.dfh.cp.api.params;

/**
 * Convenience class for AudioParamKey
 */
public class AudioStreamParams
{
    private Integer bitrate;
    private Integer channels;
    private String codec;
    private Long duration;
    private String format;
    private String id;
    private String language;
    private String profile;
    private Integer samplingRate;
    private Integer sampleSize;
    private Long streamSize;

    public Integer getBitrate()
    {
        return bitrate;
    }

    public void setBitrate(Integer bitrate)
    {
        this.bitrate = bitrate;
    }

    public Integer getChannels()
    {
        return channels;
    }

    public void setChannels(Integer channels)
    {
        this.channels = channels;
    }

    public String getCodec()
    {
        return codec;
    }

    public void setCodec(String codec)
    {
        this.codec = codec;
    }

    public Long getDuration()
    {
        return duration;
    }

    public void setDuration(Long duration)
    {
        this.duration = duration;
    }

    public String getFormat()
    {
        return format;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getProfile()
    {
        return profile;
    }

    public void setProfile(String profile)
    {
        this.profile = profile;
    }

    public Integer getSamplingRate()
    {
        return samplingRate;
    }

    public void setSamplingRate(Integer samplingRate)
    {
        this.samplingRate = samplingRate;
    }

    public Integer getSampleSize()
    {
        return sampleSize;
    }

    public void setSampleSize(Integer sampleSize)
    {
        this.sampleSize = sampleSize;
    }

    public Long getStreamSize()
    {
        return streamSize;
    }

    public void setStreamSize(Long streamSize)
    {
        this.streamSize = streamSize;
    }
}
