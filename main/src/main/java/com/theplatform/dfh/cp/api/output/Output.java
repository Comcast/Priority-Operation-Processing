package com.theplatform.dfh.cp.api.output;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class Output
{
    /**
     * Path for the output rendition, fasp://location/path/filename.mp4
     */
    private String url;
    /**
     * Username: Credentials to access the source
     */
    private String username;
    /**
     * Password: Credentials to access the source
     */
    private String password;

    /**
     * Format for the rendition, mp4
     */
    private String format;

    /**
     * Referencing video stream to use.
     */
    private List<String> videoOutputStreams;

    /**
     * Referencing audio streams to use.
     */
    private List<String> audioOutputStreams;

    /**
     * Text output streams to use.
     */
    private List<String> textOutputStreams;

    /**
     * Protection key information
     */
    private String protectionScheme;
    /**
     * Name value pairs for things like 'externalId'
     */
    private Map<String, String> metadata;

    @JsonProperty
    public String getUrl()
    {
        return url;
    }

    @JsonProperty
    public void setUrl(String url)
    {
        this.url = url;
    }

    @JsonProperty
    public String getUsername()
    {
        return username;
    }

    @JsonProperty
    public void setUsername(String username)
    {
        this.username = username;
    }

    @JsonProperty
    public String getPassword()
    {
        return password;
    }

    @JsonProperty
    public void setPassword(String password)
    {
        this.password = password;
    }

    @JsonProperty
    public String getFormat()
    {
        return format;
    }

    @JsonProperty
    public void setFormat(String format)
    {
        this.format = format;
    }

    @JsonProperty
    public List<String> getVideoOutputStreams()
    {
        return videoOutputStreams;
    }

    @JsonProperty
    public void setVideoOutputStreams(List<String> videoOutputStreams)
    {
        this.videoOutputStreams = videoOutputStreams;
    }

    @JsonProperty
    public List<String> getAudioOutputStreams()
    {
        return audioOutputStreams;
    }

    @JsonProperty
    public void setAudioOutputStreams(List<String> audioOutputStreams)
    {
        this.audioOutputStreams = audioOutputStreams;
    }

    @JsonProperty
    public List<String> getTextOutputStreams()
    {
        return textOutputStreams;
    }

    @JsonProperty
    public void setTextOutputStreams(List<String> textOutputStreams)
    {
        this.textOutputStreams = textOutputStreams;
    }

    @JsonProperty
    public String getProtectionScheme()
    {
        return protectionScheme;
    }

    @JsonProperty
    public void setProtectionScheme(String protectionScheme)
    {
        this.protectionScheme = protectionScheme;
    }

    @JsonProperty
    public Map<String, String> getMetadata()
    {
        return metadata;
    }

    @JsonProperty
    public void setMetadata(Map<String, String> metadata)
    {
        this.metadata = metadata;
    }
}
